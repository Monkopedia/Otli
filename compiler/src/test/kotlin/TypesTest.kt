package com.monkopedia.otli

import kotlin.test.Test

class TypesTest {

    @Test
    fun int64() = transformTest(
        otliCode = """
                    val x = 2L
        """.trimIndent(),
        expected = """
                    #include <stdint.h>
                    
                    int64_t x = 2;
                    
        """.trimIndent()
    )

    @Test
    fun uint64() = transformTest(
        otliCode = """
                    val x = 2uL
        """.trimIndent(),
        expected = """
                    #include <stdint.h>
                    
                    uint64_t x = 2;
                    
        """.trimIndent()
    )
    @Test
    fun int32() = transformTest(
        otliCode = """
                    val x = 2
        """.trimIndent(),
        expected = """
                    #include <stdint.h>
                    
                    int32_t x = 2;
                    
        """.trimIndent()
    )

    @Test
    fun uint32() = transformTest(
        otliCode = """
                    val x = 2u
        """.trimIndent(),
        expected = """
                    #include <stdint.h>
                    
                    uint32_t x = 2;
                    
        """.trimIndent()
    )
    @Test
    fun int16() = transformTest(
        otliCode = """
                    val x = 2.toShort()
        """.trimIndent(),
        expected = """
                    #include <stdint.h>
                    
                    int16_t x = 2;
                    
        """.trimIndent()
    )

    @Test
    fun uint16() = transformTest(
        otliCode = """
                    val x = 2.toUShort()
        """.trimIndent(),
        expected = """
                    #include <stdint.h>
                    
                    uint16_t x = (uint16_t)2;
                    
        """.trimIndent()
    )
    @Test
    fun int8() = transformTest(
        otliCode = """
                    val x = 2.toByte()
        """.trimIndent(),
        expected = """
                    #include <stdint.h>
                    
                    int8_t x = 2;
                    
        """.trimIndent()
    )

    @Test
    fun uint8() = transformTest(
        otliCode = """
                    val x = 2u.toUByte()
        """.trimIndent(),
        expected = """
                    #include <stdint.h>
                    
                    uint8_t x = (uint8_t)2;
                    
        """.trimIndent()
    )
}
