package com.monkopedia.otli.clang

import com.monkopedia.ksrpc.RpcService
import com.monkopedia.ksrpc.annotation.KsMethod
import com.monkopedia.ksrpc.annotation.KsService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow

@KsService
interface ClangService : RpcService {

    @KsMethod("/index")
    suspend fun index(config: ClangIndexConfig): ClangElementIterator
}

@KsService
interface ClangElementIterator : RpcService {
    @KsMethod("/next")
    suspend fun next(u: Unit = Unit): List<ClangElement>
}

val ClangElementIterator.consumeAsFlow: Flow<ClangElement>
    get() = channelFlow {
        try {
            while (true) {
                next().takeIf { it.isNotEmpty() }?.forEach { send(it) }
                    ?: break
            }
        } finally {
            this@consumeAsFlow.close()
        }
    }
