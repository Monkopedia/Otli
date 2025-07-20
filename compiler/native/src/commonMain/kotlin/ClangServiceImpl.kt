@file:OptIn(ExperimentalForeignApi::class)

package com.monkopedia.otli.clang

import clang.CXCursor
import com.monkopedia.jni.JNIEnvVar
import com.monkopedia.jni.jobject
import com.monkopedia.jnitest.com.monkopedia.ksrpc.jni.JNIDispatcher
import com.monkopedia.jnitest.com.monkopedia.ksrpc.jni.JavaJniContinuationConverter
import com.monkopedia.jnitest.initThread
import com.monkopedia.ksrpc.jni.JniSerialization
import com.monkopedia.ksrpc.jni.NativeConnection
import com.monkopedia.ksrpc.jni.newTypeConverter
import com.monkopedia.ksrpc.ksrpcEnvironment
import com.monkopedia.ksrpc.serialized
import kotlin.experimental.ExperimentalNativeApi
import kotlinx.cinterop.Arena
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.StableRef
import kotlinx.cinterop.toLong
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import platform.posix.usleep

class ClangServiceImpl : ClangService {
    @OptIn(ExperimentalNativeApi::class)
    override suspend fun index(config: ClangIndexConfig): ClangElementIterator =
        ClangElementIteratorImpl(config)
}

class ClangElementIteratorImpl(config: ClangIndexConfig) : ClangElementIterator {
    val index = createIndex(0, 0) ?: error("Failed to create Index")
    private val scope = Arena()
    private val includes = generateIncludes(
        (config.compiler + config.includePaths.joinToString("") { " -I$it" })
    )
    private val tu = scope.parseHeader(
        index,
        config.targetFile,
        includes,
        args = config.compilerFlags.toTypedArray() +
            includes.map { "-I$it" }.toTypedArray()
    )

    init {
        scope.defer { index.dispose() }
    }

    private val iterator by lazy {
        tu.cursor.mapChildren {
            it.toClangElement()
        }.iterator()
    }

    override suspend fun close() {
        super.close()
        scope.clear()
    }

    override suspend fun next(u: Unit): List<ClangElement> = List(500) {
        if (iterator.hasNext()) iterator.next() else null
    }.filterNotNull()
}

private fun CValue<CXCursor>.toClangElement(): ClangElement = ClangElement(
    spelling.toKString() ?: "UKN",
    type.spelling.toKString() ?: "UKN",
    kind.commonKind,
    extend.getStartLocation().file?.getPath() ?: "NOPATH",
    mapChildren { it.toClangElement() }
)

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("Java_com_monkopedia_otli_clang_ClangServiceHost_registerService")
fun registerService(env: CPointer<JNIEnvVar>, clazz: jobject, input: jobject, output: jobject) {
    initThread(env)
    try {
        val jniSerialized = NativeConnection.convertTo(input)
        val javaContinuation = JavaJniContinuationConverter<Int>(env).convertTo(output)
        GlobalScope.launch(JNIDispatcher) {
            runCatching {
                val service: ClangService = ClangServiceImpl()
                jniSerialized.registerDefault(service.serialized(jniSerialized.env))
                0
            }.let {
                javaContinuation.resumeWith(newTypeConverter<jobject>().int, it)
            }
        }
    } catch (t: Throwable) {
        t.printStackTrace()
        usleep(1000000u)
    }
}

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("Java_com_monkopedia_otli_clang_ClangServiceHost_createEnv")
fun createEnv(env: CPointer<JNIEnvVar>, clazz: jobject): Long {
    initThread(env)
    try {
        val env = ksrpcEnvironment(JniSerialization()) {}
        return StableRef.create(env).asCPointer().toLong()
    } catch (t: Throwable) {
        t.printStackTrace()
        usleep(1000000u)
        return -1
    }
}
