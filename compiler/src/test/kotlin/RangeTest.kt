package com.monkopedia.otli

import kotlin.test.Test

class RangeTest {
    @Test
    fun testRangeCreation() = transformTest(
        """
            val x = IntRange(2, 5)
            val start = x.start
            val first = x.first
            val last = x.last
            val endInclusive = x.endInclusive
            val isEmpty = x.isEmpty()
            val other = 1 until 4
        """.trimIndent(),
        """
            #include <stdbool.h>
            #include <stdint.h>
            
            int32_t x[2] = {2, 5};
            int32_t start = x[0];
            int32_t first = x[0];
            int32_t last = x[1];
            int32_t endInclusive = x[1];
            bool isEmpty = x[0] > x[1];
            int32_t other[2] = {1, 4};
            
        """.trimIndent()
    )

    @Test
    fun testIteration() = transformTest(
        """
            fun main() {
                for (i in 0 until 5) {
                    println("Value: ${'$'}i")
                }
            }
        """.trimIndent(),
        """
            #include <stdint.h>
            
            void main() {
            
                int32_t tmp0_iterator = 0 - 1;
                while (tmp0_iterator < 5 - 1) {
                    int32_t i = ++tmp0_iterator;
                    printf("Value: "PRId32, i);
            
                }
            
            }
            
        """.trimIndent()
    )

    @Test
    fun testWhileLt() = transformTest(
        """
            fun main() {
                var i = 0
                while (i < 5) {
                    println("Value: ${'$'}i")
                    i++
                }
            }
        """.trimIndent(),
        """
            #include <stdint.h>
            
            void main() {
            
                int32_t i = 0;
                while (i < 5) {
                    printf("Value: "PRId32, i);
                    int32_t tmp0 = i;
                    i = (tmp0 + 1);
                    tmp0;
            
                }
            
            }
            
        """.trimIndent()
    )

    @Test
    fun testWhileGt() = transformTest(
        """
            fun main() {
                var i = 0
                while (i > 5) {
                    println("Value: ${'$'}i")
                    i--
                }
            }
        """.trimIndent(),
        """
            #include <stdint.h>
            
            void main() {
            
                int32_t i = 0;
                while (i > 5) {
                    printf("Value: "PRId32, i);
                    int32_t tmp0 = i;
                    i = (tmp0 - 1);
                    tmp0;
            
                }
            
            }
            
        """.trimIndent()
    )

    @Test
    fun testWhileLte() = transformTest(
        """
            fun main() {
                var i = 0
                while (i <= 5) {
                    println("Value: ${'$'}i")
                    i++
                }
            }
        """.trimIndent(),
        """
            #include <stdint.h>
            
            void main() {
            
                int32_t i = 0;
                while (i <= 5) {
                    printf("Value: "PRId32, i);
                    int32_t tmp0 = i;
                    i = (tmp0 + 1);
                    tmp0;
            
                }
            
            }
            
        """.trimIndent()
    )

    @Test
    fun testWhileGte() = transformTest(
        """
            fun main() {
                var i = 0
                while (i >= 5) {
                    println("Value: ${'$'}i")
                    i--
                }
            }
        """.trimIndent(),
        """
            #include <stdint.h>
            
            void main() {
            
                int32_t i = 0;
                while (i >= 5) {
                    printf("Value: "PRId32, i);
                    int32_t tmp0 = i;
                    i = (tmp0 - 1);
                    tmp0;
            
                }
            
            }
            
        """.trimIndent()
    )
}
