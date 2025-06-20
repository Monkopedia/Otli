package com.monkopedia.otli

import kotlin.test.Test

class PrintTest {
    @Test
    fun `call print test`() = transformTest(
        otliCode = """
            val a = 5
            fun main() {
                println("A = ${'$'}a")
            }
        """.trimIndent(),
        expected = """
            
            int32_t a = 5;
            void main() {
            
                printf("A = "PRId32, a);
            }
            
        """.trimIndent()
    )
}
