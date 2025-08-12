package com.monkopedia.otli

import kotlin.test.Test

class IfTest {

    @Test
    fun `if test`() = transformTest(
        """
            fun main() {
                val x = 5
                if (x > 2) {
                    println("A")
                }
            }
        """.trimIndent(),
        """
            #include <stdint.h>
            #include <stdio.h>
            
            void main() {
            
                int32_t x = 5;
                if (x > 2) {
                    printf("A");
                }
            
            
            }
            
        """.trimIndent()
    )

    @Test
    fun `if else test`() = transformTest(
        """
            fun main() {
                val x = 5
                if (x > 2) {
                    println("A")
                } else {
                    println("B")
                }
            }
        """.trimIndent(),
        """
            #include <stdint.h>
            #include <stdio.h>
            
            void main() {
            
                int32_t x = 5;
                if (x > 2) {
                    printf("A");
                } else {
                    printf("B");
                }
            
            
            }
            
        """.trimIndent()
    )

    @Test
    fun `if else if test`() = transformTest(
        """
            fun main() {
                val x = 5
                if (x > 2) {
                    println("A")
                } else if (x < 0) {
                    println("B")
                } else {
                    println("C")
                }
            }
        """.trimIndent(),
        """
            #include <stdint.h>
            #include <stdio.h>
            
            void main() {
            
                int32_t x = 5;
                if (x > 2) {
                    printf("A");
                } else if (x < 0) {
                    printf("B");
                } else {
                    printf("C");
                }
            
            
            }
            
        """.trimIndent()
    )

    @Test
    fun `test when`() = transformTest(
        """
            fun main() {
                when (true) {
                    true -> println("A")
                    false -> println("B")
                }
            }
        """.trimIndent(),
        """
            #include <stdbool.h>
            #include <stdio.h>

            void main() {

                bool tmp0_subject = true;
                if (tmp0_subject == true) {
                    printf("A");
                } else if (tmp0_subject == false) {
                    printf("B");
                }


            }
            
        """.trimIndent()
    )
}
