package com.monkopedia.otli.clang

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
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.StableRef
import kotlinx.cinterop.toLong
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import platform.posix.usleep

class ClangServiceImpl : ClangService {
    @OptIn(ExperimentalNativeApi::class)
    override suspend fun hello(u: Unit): String = "Clang from ${Platform.osFamily}"
}

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
