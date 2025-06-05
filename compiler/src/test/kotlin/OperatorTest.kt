package com.monkopedia.otli

import kotlin.test.Test

class OperatorTest {

    @Test
    fun `addition test`() = transformTest(
        otliCode = """
                    val x = 2
                    val y = 3
                    val z = x + y
        """.trimIndent(),
        expected = """
                    int32_t x = 2;
                    int32_t y = 3;
                    int32_t z = (x + y);
                    
        """.trimIndent()
    )

    @Test
    fun `unsigned addition test`() = transformTest(
        otliCode = """
                    val x = 2u
                    val y = 3u
                    val z = x + y
        """.trimIndent(),
        expected = """
                    uint32_t x = 2;
                    uint32_t y = 3;
                    uint32_t z = (x + y);
                    
        """.trimIndent()
    )

    @Test
    fun `subtraction test`() = transformTest(
        otliCode = """
                    val x = 2
                    val y = 3
                    val z = x - y
        """.trimIndent(),
        expected = """
                    int32_t x = 2;
                    int32_t y = 3;
                    int32_t z = (x - y);
                    
        """.trimIndent()
    )

    @Test
    fun `unsigned subtraction test`() = transformTest(
        otliCode = """
                    val x = 2u
                    val y = 3u
                    val z = x - y
        """.trimIndent(),
        expected = """
                    uint32_t x = 2;
                    uint32_t y = 3;
                    uint32_t z = (x - y);
                    
        """.trimIndent()
    )

    @Test
    fun `multiplication test`() = transformTest(
        otliCode = """
                    val x = 2
                    val y = 3
                    val z = x * y
        """.trimIndent(),
        expected = """
                    int32_t x = 2;
                    int32_t y = 3;
                    int32_t z = (x * y);
                    
        """.trimIndent()
    )

    @Test
    fun `unsigned multiplication test`() = transformTest(
        otliCode = """
                    val x = 2u
                    val y = 3u
                    val z = x * y
        """.trimIndent(),
        expected = """
                    uint32_t x = 2;
                    uint32_t y = 3;
                    uint32_t z = (x * y);
                    
        """.trimIndent()
    )

    @Test
    fun `division test`() = transformTest(
        otliCode = """
                    val x = 2
                    val y = 3
                    val z = x / y
        """.trimIndent(),
        expected = """
                    int32_t x = 2;
                    int32_t y = 3;
                    int32_t z = (x / y);
                    
        """.trimIndent()
    )

    @Test
    fun `unsigned division test`() = transformTest(
        otliCode = """
                    val x = 2u
                    val y = 3u
                    val z = x / y
        """.trimIndent(),
        expected = """
                    uint32_t x = 2;
                    uint32_t y = 3;
                    uint32_t z = (x / y);
                    
        """.trimIndent()
    )

    @Test
    fun `casting test`() = transformTest(
        otliCode = """
                    val x = 2
                    val y = 3u
                    val z = x + y.toInt()
        """.trimIndent(),
        expected = """
                    int32_t x = 2;
                    uint32_t y = 3;
                    int32_t z = (x + (int32_t)y);
                    
        """.trimIndent()
    )

    @Test
    fun `compound test`() = transformTest(
        otliCode = """
                    val x = 2
                    val y = 3
                    val z = (x + y) * y / x
        """.trimIndent(),
        expected = """
                    int32_t x = 2;
                    int32_t y = 3;
                    int32_t z = (((x + y) * y) / x);
                    
        """.trimIndent()
    )
}
