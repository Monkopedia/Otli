package com.monkopedia.otli

import kotlin.test.Test

class TypesTest {

    @Test
    fun int64() = transformTest(
        otliCode = """
                    val x = 2L
        """.trimIndent(),
        expected = """
                    
                    int64_t x = 2;
                    
        """.trimIndent()
    )

    @Test
    fun uint64() = transformTest(
        otliCode = """
                    val x = 2uL
        """.trimIndent(),
        expected = """
                    
                    uint64_t x = 2;
                    
        """.trimIndent()
    )
    @Test
    fun int32() = transformTest(
        otliCode = """
                    val x = 2
        """.trimIndent(),
        expected = """
                    
                    int32_t x = 2;
                    
        """.trimIndent()
    )

    @Test
    fun uint32() = transformTest(
        otliCode = """
                    val x = 2u
        """.trimIndent(),
        expected = """
                    
                    uint32_t x = 2;
                    
        """.trimIndent()
    )
    @Test
    fun int16() = transformTest(
        otliCode = """
                    val x = 2.toShort()
        """.trimIndent(),
        expected = """
                    
                    int16_t x = 2;
                    
        """.trimIndent()
    )

    @Test
    fun uint16() = transformTest(
        otliCode = """
                    val x = 2.toUShort()
        """.trimIndent(),
        expected = """
                    
                    uint16_t x = (uint16_t)2;
                    
        """.trimIndent()
    )
    @Test
    fun int8() = transformTest(
        otliCode = """
                    val x = 2.toByte()
        """.trimIndent(),
        expected = """
                    
                    int8_t x = 2;
                    
        """.trimIndent()
    )

    @Test
    fun uint8() = transformTest(
        otliCode = """
                    val x = 2u.toUByte()
        """.trimIndent(),
        expected = """
                    
                    uint8_t x = (uint8_t)2;
                    
        """.trimIndent()
    )
}
