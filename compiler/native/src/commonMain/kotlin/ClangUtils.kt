@file:OptIn(ExperimentalForeignApi::class)

package com.monkopedia.otli.clang

import clang.CXChildVisitResult
import clang.CXCursor
import clang.CXCursorKind
import clang.clang_visitChildren
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.StableRef
import kotlinx.cinterop.asStableRef
import kotlinx.cinterop.staticCFunction

private val CValue<CXCursor>.templatedName: String
    get() {
//        if (numTemplateArguments != 0) {
//            return spelling.toKString() + "<" + (0 until numTemplateArguments).joinToString(",") {
//                when (getTemplateArgumentKind(it.toUInt())) {
//                    CXTemplateArgumentKind_Null -> "__NULL__"
//                    CXTemplateArgumentKind_Type -> getTemplateArgumentType(it.toUInt()).spelling.toKString() ?: ""
//                    CXTemplateArgumentKind_Declaration -> "__DECL__"
//                    CXTemplateArgumentKind_NullPtr -> "__NULL_PTR__"
//                    CXTemplateArgumentKind_Integral -> "__INTEGRAL__"
//                    CXTemplateArgumentKind_Template -> "__TYPE__"
//                    CXTemplateArgumentKind_TemplateExpansion -> "__TEMPLATE_EXPANSION__"
//                    CXTemplateArgumentKind_Expression -> "__EXPRESSION__"
//                    CXTemplateArgumentKind_Pack -> "__PACK__"
//                    CXTemplateArgumentKind_Invalid -> "__INVALID__"
//                }
//            } + ">"
//        }
        return spelling.toKString() ?: ""
    }

val CValue<CXCursor>?.fullyQualified: String
    get() =
        if (this == null || this == CXCursor.NULL) {
            ""
        } else if (kind == CXCursorKind.CXCursor_TranslationUnit) {
            ""
        } else {
            val res = semanticParent.fullyQualified
            if (res.isNotEmpty()) {
                "$res::$templatedName"
            } else {
                templatedName ?: ""
            }
        }

typealias ChildVisitor = (child: CValue<CXCursor>, parent: CValue<CXCursor>) -> Unit

val recurseVisitor =
    staticCFunction {
            child: CValue<CXCursor>,
            parent: CValue<CXCursor>,
            children: clang.CXClientData?
        ->
        children!!.asStableRef<ChildVisitor>().get().invoke(child, parent)
        CXChildVisitResult.CXChildVisit_Recurse
    }

val visitor =
    staticCFunction {
            child: CValue<CXCursor>,
            _: CValue<CXCursor>,
            children: clang.CXClientData?
        ->
        children!!
            .asStableRef<(CValue<CXCursor>) -> Unit>()
            .get()
            .invoke(child)
        CXChildVisitResult.CXChildVisit_Continue
    }

inline fun CValue<CXCursor>.forEachRecursive(noinline childHandler: ChildVisitor) {
    val ptr = StableRef.create(childHandler)
    clang_visitChildren(this, recurseVisitor, ptr.asCPointer())
}

inline fun CValue<CXCursor>.forEach(noinline childHandler: (CValue<CXCursor>) -> Unit) {
    val ptr = StableRef.create(childHandler)
    clang_visitChildren(this, visitor, ptr.asCPointer())
}

inline fun CValue<CXCursor>.filterChildrenRecursive(
    crossinline filter: (CValue<CXCursor>) -> Boolean
): List<CValue<CXCursor>> = mutableListOf<CValue<CXCursor>>().also { list ->
    forEachRecursive { child, _ ->
        if (filter(child)) {
            list.add(child)
        }
    }
}

inline fun CValue<CXCursor>.filterChildren(
    crossinline filter: (CValue<CXCursor>) -> Boolean
): List<CValue<CXCursor>> = mutableListOf<CValue<CXCursor>>().also { list ->
    forEach {
        if (filter(it)) {
            list.add(it)
        }
    }
}

inline fun <T> CValue<CXCursor>.mapChildren(crossinline filter: (CValue<CXCursor>) -> T): List<T> =
    mutableListOf<T>().also { list ->
        forEach {
            list.add(filter(it))
        }
    }

val CValue<CXCursor>.allChildren: Collection<CValue<CXCursor>>
    get() {
        return mutableListOf<CValue<CXCursor>>().also { list ->
            forEachRecursive { child, _ ->
                list.add(child)
            }
        }
    }

val CValue<CXCursor>.children: Collection<CValue<CXCursor>>
    get() {
        return mutableListOf<CValue<CXCursor>>().also { list ->
            forEach(list::add)
        }
    }
