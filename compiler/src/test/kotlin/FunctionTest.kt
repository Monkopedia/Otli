package com.monkopedia.otli

import kotlin.test.Test

class FunctionTest {


    @Test
    fun `basic main`() = transformTest(
        otliCode = """
                    fun main(): Int {
                        return 0
                    }
        """.trimIndent(),
        expected = """
                    #include <stdint.h>
                    
                    int32_t main() {
                    
                        return 0;
                    }
                    
        """.trimIndent()
    )


    @Test
    fun `add function`() = transformTest(
        otliCode = """
                    fun add(x: Int, y: Int): Int {
                        return x + y
                    }
        """.trimIndent(),
        expected = """
                    #include <stdint.h>
                    
                    int32_t add(int32_t x, int32_t y) {
                    
                        return (x + y);
                    }
                    
        """.trimIndent()
    )

    @Test
    fun `basic main expression`() = transformTest(
        otliCode = """
                    fun main(): Int = 0
        """.trimIndent(),
        expected = """
                    #include <stdint.h>
                    
                    int32_t main() {
                    
                        return 0;
                    }
                    
        """.trimIndent()
    )


    @Test
    fun `add function expression`() = transformTest(
        otliCode = """
                    fun add(x: Int, y: Int): Int = x + y
        """.trimIndent(),
        expected = """
                    #include <stdint.h>
                    
                    int32_t add(int32_t x, int32_t y) {
                    
                        return (x + y);
                    }
                    
        """.trimIndent()
    )

    @Test
    fun `call function test`() = transformTest(
        otliCode = """
                    package com.monkopedia.otli
                    val a = 5
                    val b = 3
                    fun add(x: Int, y: Int): Int {
                        return x + y
                    }
                    val c = add(a, b)
        """.trimIndent(),
        expected = """
                    #include <stdint.h>
                    
                    int32_t a = 5;
                    int32_t b = 3;
                    int32_t com_monkopedia_otli_add(int32_t x, int32_t y) {
                    
                        return (x + y);
                    }
                    int32_t c = add(a, b);
                    
        """.trimIndent()
    )


    @Test
    fun `add function external`() = transformTest(
        otliCode = """
                    external fun addition(x: Int, y: Int): Int
                    
                    fun add(x: Int, y: Int): Int = addition(x, y)
        """.trimIndent(),
        expected = """
                    #include <stdint.h>
                    
                    
                    int32_t add(int32_t x, int32_t y) {
                    
                        return addition(x, y);
                    }
                    
        """.trimIndent()
    )

//    @Test
//    fun `create lambda`() = transformTest(
//        otliCode = """
//                    val a = 5
//                    val b = 3
//                    fun add(x: Int, y: Int): Int {
//                        return (x + y).also { println(it) }
//                    }
//                    val c = add(a, b)
//        """.trimIndent(),
//        expected = """
//                    int32_t a = 5;
//                    int32_t b = 3;
//                    int32_t add(int32_t x, int32_t y) {
//
//                        return (x + y);
//                    }
//                    int32_t c = add(a, b);
//
//        """.trimIndent()
//
//    )
}
