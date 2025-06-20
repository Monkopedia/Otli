package com.monkopedia.otli.codegen

import com.monkopedia.otli.builders.CodeBuilder
import com.monkopedia.otli.builders.Raw
import com.monkopedia.otli.builders.ResolvedType
import com.monkopedia.otli.builders.Symbol
import com.monkopedia.otli.type.isString
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrConstKind
import org.jetbrains.kotlin.ir.expressions.IrStringConcatenation

fun CodegenVisitor.buildString(
    expression: IrStringConcatenation,
    data: CodeBuilder
): Symbol {
    error("Not implemented")
}

fun CodegenVisitor.convertArgs(
    expression: IrStringConcatenation,
    data: CodeBuilder
): Array<Symbol> {
    val mappings = expression.arguments.joinToString("") {
        when (it) {
            is IrConst -> {
                when (it.kind) {
                    IrConstKind.Boolean -> "\"${it.value}\""
                    IrConstKind.Byte -> "\"${it.value}\""
                    IrConstKind.Char -> "\"${it.value}\""
                    IrConstKind.Double -> "\"${it.value}\""
                    IrConstKind.Float -> "\"${it.value}\""
                    IrConstKind.Int -> "\"${it.value}\""
                    IrConstKind.Long -> "\"${it.value}\""
                    IrConstKind.Null -> "\"${it.value}\""
                    IrConstKind.Short -> "\"${it.value}\""
                    IrConstKind.String -> "\"${it.value}\""
                }
            }

            else -> {
                val type = ResolvedType(it.type)
                if (!type.isArray && type.isNative) {
                    if (type.isPointer) {
                        if (type.isString) {
                            "\"%s\""
                        } else {
                            "\"%p\""
                        }
                    } else {
                        when (type.coreType) {
                            "uint8_t" -> "PRId8"
                            "uint16_t" -> "PRId16"
                            "uint32_t" -> "PRId32"
                            "uint64_t" -> "PRId64"
                            "int8_t" -> "PRId8"
                            "int16_t" -> "PRId16"
                            "int32_t" -> "PRId32"
                            "int64_t" -> "PRId64"
                            "size_t" -> "\"%lu\""
                            "void" -> error("Cannot include Unit in format string")
                            "bool" -> "\"%d\""
                            "char" -> "\"%c\""
                            "signed char" -> "\"%hhd\""
                            "unsigned char" -> "\"%hu\""
                            "short" -> "\"%hd\""
                            "signed short" -> "\"%hd\""
                            "unsigned short" -> "\"%hu\""
                            "int" -> "\"%d\""
                            "signed int" -> "\"%d\""
                            "unsigned int" -> "\"%u\""
                            "long" -> "\"%ld\""
                            "signed long" -> "\"%ld\""
                            "unsigned long" -> "\"%lu\""
                            "long long" -> "\"%lld\""
                            "signed long long" -> "\"%lld\""
                            "unsigned long long" -> "\"%llu\""
                            "float" -> "\"%f\""
                            "double" -> "\"%lf\""
                            else -> error("Cannot include $type in format string")
                        }
                    }
                } else {
                    "\"%s\""
                }
            }
        }
    }.replace("\"\"", "")
    val arguments = expression.arguments.filter { it !is IrConst }.map {
        val resolvedType = ResolvedType(it.type)
        if (resolvedType.isNative && !resolvedType.isArray) {
            it.accept(this, data)
        } else if (resolvedType.isString) {
            it.accept(this, data)
        } else {
            buildCall(null, "toString", emptyList(), data, receiver = it)
        }
    }
    return (listOf(Raw(mappings)) + arguments).toTypedArray()
}
