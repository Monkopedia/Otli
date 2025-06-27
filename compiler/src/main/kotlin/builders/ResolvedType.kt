package com.monkopedia.otli.builders

import com.monkopedia.otli.type.WrappedType
import com.monkopedia.otli.type.WrappedType.Companion.pointerTo
import org.jetbrains.kotlin.backend.jvm.codegen.anyTypeArgument
import org.jetbrains.kotlin.fir.backend.utils.getArrayElementType
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.types.getClass
import org.jetbrains.kotlin.ir.types.typeOrFail
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
    "kotlin.Boolean" to "bool",
    "kotlin.ranges.IntRange" to "int32_t[2]",
    "kotlin.collections.IntIterator" to "int32_t"
)

typealias ResolvedType = WrappedType

fun ResolvedType(type: IrType): WrappedType {
    nativeMaps[type.classFqName?.asString()]?.let { return WrappedType(it) }
    type.getClass()?.takeIf { it.isData }?.let {
        return WrappedType(it.typeName())
    }
    if (type.classFqName?.asString() == "otli.Ptr") {
        return pointerTo(ResolvedType((type as IrSimpleType).arguments[0].typeOrFail))
    }
    return error("Can't handle $type ${type.classFqName?.asString()}")
}

fun IrClass.typeName(): String = packageFqName?.asString()?.takeIf {
    it.isNotEmpty()
}?.replace(".", "_")?.plus("_")
    .orEmpty() + name.asString()
