package com.monkopedia.otli.clang

import com.monkopedia.ksrpc.RpcService
import com.monkopedia.ksrpc.annotation.KsMethod
import com.monkopedia.ksrpc.annotation.KsService

@KsService
interface ClangService : RpcService {

    @KsMethod("/hello")
    suspend fun hello(u: Unit = Unit): String
}
