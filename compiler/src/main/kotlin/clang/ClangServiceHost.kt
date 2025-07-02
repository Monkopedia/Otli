package com.monkopedia.otli.clang

import com.monkopedia.ksrpc.jni.JavaJniContinuation
import com.monkopedia.ksrpc.jni.JniConnection
import com.monkopedia.ksrpc.jni.JniSerialization
import com.monkopedia.ksrpc.jni.NativeUtils
import com.monkopedia.ksrpc.jni.newTypeConverter
import com.monkopedia.ksrpc.jni.withConverter
import com.monkopedia.ksrpc.ksrpcEnvironment
import com.monkopedia.ksrpc.toStub
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.CoroutineScope

class ClangServiceHost {
    external fun createEnv(): Long
    external fun registerService(connection: JniConnection, output: JavaJniContinuation<Int>)
}

suspend fun CoroutineScope.getClangService(): ClangService {
    NativeUtils.loadLibraryFromJar("/libs/libnative.${extension()}")
    val clangHost = ClangServiceHost()
    val env = ksrpcEnvironment(JniSerialization()) { }
    val nativeEnvironent = clangHost.createEnv()
    val connection = JniConnection(this, env, nativeEnvironent)
    suspendCoroutine<Int> {
        clangHost.registerService(connection, it.withConverter(newTypeConverter<Any?>().int))
    }
    return connection.defaultChannel().toStub()
}

private fun extension(): String =
    if (NativeUtils::class.java.getResourceAsStream("/libs/libnative.so") != null) {
        "so"
    } else {
        "dylib"
    }
