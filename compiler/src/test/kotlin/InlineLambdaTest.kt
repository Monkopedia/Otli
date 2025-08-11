package com.monkopedia.otli

import kotlin.test.Test

class InlineLambdaTest {

    @Test
    fun let() = transformTest(
        """
            fun main() {
                val x = 5.let {
                    it * it
                }
            }
        """.trimIndent(),
        """
            #include <stdint.h>
            
            void main() {
            
                int32_t letRet;
                int32_t it;
                it = 5;
                letRet = (it * it);
                int32_t x = letRet;
            }
            
        """.trimIndent()
    )

    @Test
    fun letUnit() = transformTest(
        $$"""
            fun main() {
                5.let {
                    println("${it * it}")
                }
            }
        """.trimIndent(),
        """
            #include <stdint.h>
            
            void main() {
            
                int32_t it;
                it = 5;
                printf(PRId32, (it * it));
                void;
            }
            
        """.trimIndent()
    )

    @Test
    fun run() = transformTest(
        """
            fun main() {
                val x = 5.run {
                    this * this
                }
            }
        """.trimIndent(),
        """
            #include <stdint.h>
            
            void main() {
            
                int32_t letRet;
                int32_t _this_run;
                _this_run = 5;
                letRet = (_this_run * _this_run);
                int32_t x = letRet;
            }
            
        """.trimIndent()
    )

    @Test
    fun runUnit() = transformTest(
        $$"""
            fun main() {
                5.run {
                    println("${this * this}")
                }
            }
        """.trimIndent(),
        """
            #include <stdint.h>
            
            void main() {
            
                int32_t _this_run;
                _this_run = 5;
                printf(PRId32, (_this_run * _this_run));
                void;
            }
            
        """.trimIndent()
    )

    @Test
    fun also() = transformTest(
        """
            fun main() {
                var y = 0
                val x = 5.also {
                    y = it * it
                }
            }
        """.trimIndent(),
        """
            #include <stdint.h>
            
            void main() {
            
                int32_t y = 0;
                int32_t it;
                it = 5;
                y = (it * it);
                int32_t x = it;
            }
            
        """.trimIndent()
    )

    @Test
    fun alsoUnit() = transformTest(
        $$"""
            fun main() {
                5.also {
                    println("${it * it}")
                }
            }
        """.trimIndent(),
        """
            #include <stdint.h>
            
            void main() {
            
                int32_t it;
                it = 5;
                printf(PRId32, (it * it));
                it;
            }
            
        """.trimIndent()
    )

    @Test
    fun apply() = transformTest(
        """
            fun main() {
                var y = 0
                val x = 5.apply {
                    y = this * this
                }
            }
        """.trimIndent(),
        """
            #include <stdint.h>
            
            void main() {
            
                int32_t y = 0;
                int32_t _this_apply;
                _this_apply = 5;
                y = (_this_apply * _this_apply);
                int32_t x = _this_apply;
            }
            
        """.trimIndent()
    )

    @Test
    fun applyUnit() = transformTest(
        $$"""
            fun main() {
                5.apply {
                    println("${this * this}")
                }
            }
        """.trimIndent(),
        """
            #include <stdint.h>
            
            void main() {
            
                int32_t _this_apply;
                _this_apply = 5;
                printf(PRId32, (_this_apply * _this_apply));
                _this_apply;
            }
            
        """.trimIndent()
    )
}
