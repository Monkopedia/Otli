package com.monkopedia.otli

import kotlin.test.Test

class MemoryTest {
    @Test
    fun `parse data class test`() = transformTest(
        otliCode = """
            package otli.test
            data class A(
                val a: Int
            )
        """.trimIndent(),
        expected = """
            #include <stdint.h>
            
            typedef struct {
                int32_t a;
            } otli_test_A;
            
            
            
        """.trimIndent()
    )
}
