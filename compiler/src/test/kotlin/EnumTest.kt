package com.monkopedia.otli

import kotlin.test.Test

class EnumTest {

    @Test
    fun `basic enum`() = transformTest(
        otliCode = """
                    enum class MyEnum {
                        FIRST_VALUE,
                        SECOND_VALUE
                    }
        """.trimIndent(),
        expected = """
                    
                    typedef enum {
                        FIRST_VALUE,
                        SECOND_VALUE,
                    } MyEnum;
                    
                    
        """.trimIndent()
    )
}
