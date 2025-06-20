package com.monkopedia.otli.builders

import com.monkopedia.otli.type.WrappedType
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.types.getClass
import org.jetbrains.kotlin.ir.util.packageFqName

val nativeMaps = mapOf(
    "kotlin.Unit" to "void",
    "kotlin.Long" to "int64_t",
    "kotlin.Int" to "int32_t",
    "kotlin.Short" to "int16_t",
    "kotlin.Byte" to "int8_t",
    "kotlin.ULong" to "uint64_t",
    "kotlin.UInt" to "uint32_t",
    "kotlin.UShort" to "uint16_t",
    "kotlin.UByte" to "uint8_t",
    "kotlin.Float" to "float",
    "kotlin.Double" to "double",
    "kotlin.String" to "char *",
    "kotlin.Char" to "char",
    "kotlin.Boolean" to "bool"
)

typealias ResolvedType = WrappedType

fun ResolvedType(type: IrType): WrappedType {
    nativeMaps[type.classFqName?.asString()]?.let { return WrappedType(it) }
    type.getClass()?.takeIf { it.isData }?.let {
        return WrappedType(it.typeName())
    }
    return error("Can't handle $type ${type.classFqName?.asString()}")
}

fun IrClass.typeName(): String {
    return packageFqName?.asString()?.plus(".")?.replace(".", "_").orEmpty() + name.asString()
}
