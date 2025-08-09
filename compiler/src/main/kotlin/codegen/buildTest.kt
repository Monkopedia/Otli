package com.monkopedia.otli.codegen

import com.monkopedia.otli.builders.Call
import com.monkopedia.otli.builders.CodeBuilder
import com.monkopedia.otli.builders.FileSymbol
import com.monkopedia.otli.builders.GroupSymbol
import com.monkopedia.otli.builders.Included
import com.monkopedia.otli.builders.InlineArrayDefinition
import com.monkopedia.otli.builders.Raw
import com.monkopedia.otli.builders.ResolvedType
import com.monkopedia.otli.builders.Return
import com.monkopedia.otli.builders.StringSymbol
import com.monkopedia.otli.builders.Symbol
import com.monkopedia.otli.builders.buildArray
import com.monkopedia.otli.builders.buildIf
import com.monkopedia.otli.builders.define
import com.monkopedia.otli.builders.function
import com.monkopedia.otli.builders.nullArray
import com.monkopedia.otli.builders.op
import com.monkopedia.otli.builders.reference
import com.monkopedia.otli.builders.type
import com.monkopedia.otli.builders.varScope
import com.monkopedia.otli.type.WrappedType
import com.monkopedia.otli.type.WrappedTypeReference
import org.jetbrains.kotlin.backend.jvm.ir.getKtFile
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.name
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.util.file
import org.jetbrains.kotlin.ir.util.fqNameForIrSerialization
import org.jetbrains.kotlin.ir.util.isFakeOverriddenFromAny
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.name.FqName

private val IrConstructorCall.isBeforeTestAnnotation: Boolean
    get() {
        return type.classFqName?.asString() == "kotlin.test.BeforeTest"
    }
private val IrConstructorCall.isAfterTestAnnotation: Boolean
    get() {
        return type.classFqName?.asString() == "kotlin.test.AfterTest"
    }
private val IrConstructorCall.isTestAnnotation: Boolean
    get() {
        return type.classFqName?.asString() == "kotlin.test.Test"
    }
private val IrConstructorCall.hasTestAnnotation: Boolean
    get() {
        return isTestAnnotation || isBeforeTestAnnotation || isAfterTestAnnotation
    }
val IrDeclaration.isAfterTestDeclaration: Boolean
    get() = (this is IrFunction && this.annotations.any { it.isAfterTestAnnotation }) ||
        this is IrProperty
val IrDeclaration.isBeforeTestDeclaration: Boolean
    get() = (this is IrFunction && this.annotations.any { it.isBeforeTestAnnotation }) ||
        this is IrProperty
val IrDeclaration.isTestDeclaration: Boolean
    get() = (this is IrFunction && this.annotations.any { it.isTestAnnotation }) ||
        this is IrProperty
val IrDeclaration.hasTestDeclaration: Boolean
    get() = (
        (
            this is IrFunction &&
                (this.annotations.any { it.hasTestAnnotation } || isFakeOverriddenFromAny())
            ) ||
            this is IrProperty ||
            (this is IrConstructor && this.parameters.isEmpty())
        )
val IrClass.isTestClass: Boolean
    get() = declarations.all { it.hasTestDeclaration }

@OptIn(UnsafeDuringIrConstructionAPI::class)
fun CodegenVisitor.buildTest(cls: IrClass, data: CodeBuilder): Symbol = GroupSymbol().apply {
    cls.declarations.filterIsInstance<IrProperty>().forEach {
        symbolList.add(visitProperty(it, data))
    }
    val functions = cls.declarations.filterIsInstance<IrFunction>()
    val befores = functions.filter { it.isBeforeTestDeclaration }
    val afters = functions.filter { it.isAfterTestDeclaration }
    val tests = functions.filter { it.isTestDeclaration }
    befores.forEach {
        symbolList.add(visitFunction(it, data))
    }
    afters.forEach {
        symbolList.add(visitFunction(it, data))
    }
    tests.forEach {
        symbolList.add(visitFunction(it, data))
    }
    val hasBeforesOrAfters = befores.isNotEmpty() || afters.isNotEmpty()
    val arrayArgs = if (hasBeforesOrAfters) {
        tests.map { test ->
            val wrapperName = methodName(test, suffix = "_wrapper")
            symbolList.add(
                data.function {
                    this.name = wrapperName
                    body {
                        befores.forEach {
                            addSymbol(Call(methodName(it)))
                        }
                        addSymbol(Call(methodName(test)))
                        afters.forEach {
                            addSymbol(Call(methodName(it)))
                        }
                    }
                }
            )
            buildArray {
                add(StringSymbol("${test.name}"))
                add(wrapperName)
            }
        }
    } else {
        tests.map {
            buildArray {
                add(StringSymbol("${it.name}"))
                add(methodName(it))
            }
        }
    } + nullArray()
    symbolList.add(
        data.define(
            cls,
            classTestsName(cls),
            ResolvedType("CU_TestInfo"),
            isArray = true,
            constructorArgs = arrayArgs
        )
    )
}

fun classTestsName(cls: IrClass): String = (
    cls.fqNameForIrSerialization.asString().replace(".", "_")
        .takeIf { it.isNotEmpty() }?.plus("_") ?: ""
    ) + "tests"

fun headerName(cls: IrClass): String = headerName(cls.file)

fun headerName(cls: IrFile): String = cls.packageFqName.pkgPrefix() + cls.name + ".h"

fun FqName.pkgPrefix(): String = asString().takeIf {
    it.isNotEmpty()
}?.replace(".", "_")?.plus("_").orEmpty()

fun CodegenVisitor.buildTestMethod(
    expression: IrCall?,
    name: String,
    arguments: List<IrExpression?>,
    data: CodeBuilder,
    pkg: String = ""
): Symbol = when (name) {
    "assertEquals" -> {
        if (arguments.size > 3 || arguments.size < 2) {
            error("Unsupported version of assertEquals")
        }
        val ktFile = currentFile?.getKtFile()
        val text = ktFile?.text
        val condition = expression?.let {
            text?.substring(it.startOffset, it.endOffset)
        }
        val line = text?.substring(0, expression?.startOffset ?: 0)?.count { it == '\n' }
            ?: 0
        val strFile = ktFile?.name
        val strFunction = currentFunction?.name?.asString()
        Call(
            Included("CU_assertImplementation", "CUnit/CUnit.h", true),
            buildEquals(data, arguments[0]!!, arguments[1]!!),
            Raw(line.toString()),
            StringSymbol("$condition"),
            StringSymbol("$strFile"),
            StringSymbol("$strFunction"),
            Raw("CU_FALSE")
        )
    }

    else -> error("Unhandled test method $pkg.$name")
}

val FPRINTF = Included("fprintf", "stdio.h", true)
val PRINTF = Included("printf", "stdio.h", true)
val STDERR = Included("stderr", "stdio.h", true)
val NULL = Included("NULL", "stddef.h", true)
val CU_SUITE_INFO = WrappedTypeReference("CU_SuiteInfo")
val CU_ERROR_ACTION = WrappedTypeReference("CU_ErrorAction")
val CUE_SUCCESS = Raw("CUE_SUCCESS")
val CU_BRM_VERBOSE = Raw("CU_BRM_VERBOSE")
val CU_SUITE_INFO_NULL = Raw("CU_SUITE_INFO_NULL")
val CUEA_IGNORE = Raw("CUEA_IGNORE")
val EXIT = Included("exit", "stdlib.h", true)
val CU_INITIALIZE_REGISTRY = Included("CU_initialize_registry", "CUnit/CUnit.h", true)
val CU_REGISTER_SUITES = Included("CU_register_suites", "CUnit/CUnit.h", true)
val CU_GET_ERROR_MSG = Included("CU_get_error_msg", "CUnit/CUnit.h", true)
val CU_BASIC_RUN_TESTS = Included("CU_basic_run_tests", "CUnit/Basic.h", true)
val CU_BASIC_SET_MODE = Included("CU_basic_set_mode", "CUnit/Basic.h", true)
val CU_SET_ERROR_ACTION = Included("CU_set_error_action", "CUnit/CUnit.h", true)
val CU_CLEANUP_REGISTRY = Included("CU_cleanup_registry", "CUnit/CUnit.h", true)
val CU_GET_ERROR = Included("CU_get_error", "CUnit/CUnit.h", true)

fun CodegenVisitor.buildTestMain(classes: List<IrClass>, data: CodeBuilder): Symbol =
    FileSymbol(data, "test_main.c").apply {
        data.varScope(true) {
            groupSymbol.symbolList.add(
                function {
                    this.name = Raw("main")
                    this.retType = type(WrappedType("int"))

                    body {
                        val targetSuites = +define(
                            "target_suites",
                            "target_suites",
                            CU_SUITE_INFO,
                            isArray = true,
                            constructorArgs =
                            classes.map {
                                InlineArrayDefinition(
                                    StringSymbol("${it.kotlinFqName.asString()}"),
                                    NULL,
                                    NULL,
                                    NULL,
                                    NULL,
                                    Included(classTestsName(it), headerName(it), false)
                                )
                            } + CU_SUITE_INFO_NULL
                        )
                        val errorAction = +define(
                            "error_action",
                            "error_action",
                            CU_ERROR_ACTION,
                            initializer = CUEA_IGNORE
                        )

                        +buildIf {
                            condition = CUE_SUCCESS.op("!=", Call(CU_INITIALIZE_REGISTRY))
                            ifBlock {
                                +Return(Call(CU_GET_ERROR))
                            }
                        }
                        +buildIf {
                            condition =
                                Call(
                                    CU_REGISTER_SUITES,
                                    targetSuites.reference
                                ).op("!=", CUE_SUCCESS)
                            ifBlock {
                                +Call(
                                    FPRINTF,
                                    STDERR,
                                    StringSymbol("suite registration failed - %s\n"),
                                    Call(CU_GET_ERROR_MSG)
                                )
                                +Call(EXIT, Raw("1"))
                            }
                        }
                        +Call(CU_BASIC_SET_MODE, CU_BRM_VERBOSE)
                        +Call(
                            PRINTF,
                            StringSymbol("\nTests completed with return value %d\n"),
                            Call(CU_BASIC_RUN_TESTS)
                        )
                        +Call(CU_SET_ERROR_ACTION, errorAction.reference)

                        +Call(CU_CLEANUP_REGISTRY)
                        +Return(Call(CU_GET_ERROR))
                    }
                }
            )
        }
    }
