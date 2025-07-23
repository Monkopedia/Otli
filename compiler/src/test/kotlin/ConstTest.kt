package com.monkopedia.otli

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

class ConstTest {
    @Test
    fun `test const as define`() = transformTest(
        """
        private const val MY_CONST = 4L
        const val PUBLIC_CONST = true
        
        const val MY_STR = "A string value"
        private val INTERNAL_STR = MY_STR
        """.trimIndent(),
        "unused",
        File("/tmp/constants.kt").also {
            it.deleteOnExit()
        }.toPath()
    ) { files ->
        assertEquals(setOf("constants.kt.h", "constants.kt.c"), files.keys)
        assertEquals(
            """
            #define MY_CONST 4
            #include "constants.kt.h"
            
            
            
            
            char* INTERNAL_STR = "A string value";
            
            """.trimIndent(),
            files["constants.kt.c"]
        )
        assertEquals(
            """
                #ifndef __CONSTANTS_KT_H__
                #define __CONSTANTS_KT_H__
                #define PUBLIC_CONST true
                #define MY_STR "A string value"



                #endif // __CONSTANTS_KT_H__
                
                
            """.trimIndent(),
            files["constants.kt.h"]
        )
    }
}
