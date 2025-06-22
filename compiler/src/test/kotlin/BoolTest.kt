package com.monkopedia.otli

import org.junit.Test

class BoolTest {

    @Test
    fun testDeclaration() = transformTest(
        """
            var value = true
        """.trimIndent(),
        """
            #include <stdbool.h>
            
            bool value = true;
            
        """.trimIndent()
    )
}
