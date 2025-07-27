package com.monkopedia.otli.codegen

import com.monkopedia.otli.builders.Include
import com.monkopedia.otli.builders.Symbol
import org.jetbrains.kotlin.DeprecatedForRemovalCompilerApi
import org.jetbrains.kotlin.backend.jvm.ir.fileParentOrNull
import org.jetbrains.kotlin.ir.declarations.IrAnnotationContainer
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrConstantArray
import org.jetbrains.kotlin.ir.expressions.IrVararg
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.util.getPackageFragment
import org.jetbrains.kotlin.ir.util.getValueArgument
import org.jetbrains.kotlin.name.Name

fun IrFunction.functionName(): String {
    val pkg = getPackageFragment().packageFqName.asString()
    cName()?.let { return it }
    return (
        pkg.split(".").filter {
            it.isNotEmpty()
        } + name.asString()
        ).joinToString("_") { it.lowercase() }
}

fun IrAnnotationContainer.include(): Symbol? {
    val annotation = annotations
        .find { it.type.classFqName?.asString() == "otli.CImport" }
        ?: return fileImport()
    val str = (annotation.getValueArgument(Name.identifier("file")) as? IrConst)
        ?.value?.toString() ?: return fileImport()
    val system = (annotation.getValueArgument(Name.identifier("isSystem")) as? IrConst)
        ?.value?.toString()?.toBoolean() ?: false
    return Include(str, system)
}

private fun IrAnnotationContainer.fileImport(): Symbol? =
    (this as? IrDeclaration)?.fileParentOrNull?.include()

fun IrAnnotationContainer.cName(): String? = (
    annotations
        .find { it.type.classFqName?.asString() == "otli.CName" }
        ?.getValueArgument(Name.identifier("cName")) as? IrConst
    )?.value?.toString()

fun IrAnnotationContainer.cNames(): List<String>? = (
    annotations
        .find { it.type.classFqName?.asString() == "otli.CNames" }
        ?.getValueArgument(Name.identifier("cName")) as? IrVararg
    )?.elements?.map { (it as IrConst).value.toString() }
