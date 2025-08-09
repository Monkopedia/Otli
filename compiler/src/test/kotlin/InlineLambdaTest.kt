package com.monkopedia.otli

import kotlin.test.Test

class InlineLambdaTest {

    @Test
    fun let() = transformTest(
        """
            val x = 5.let {
                it * it
            }
        """.trimIndent(),
        """
            #include <stdint.h>
            #include "OtliScopes.h"
            
            OTLI_LET(int32_t, int32_t, it, letRet, 5, {
                letRet = (it * it);
            });
            int32_t x = letRet;
            
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
            #include "OtliScopes.h"
            
            void main() {
            
                OTLI_LET_UNIT(int32_t, it, 5, {
                        printf(PRId32, (it * it));
            
                });
                void;
            }
            
        """.trimIndent()
    )

    @Test
    fun run() = transformTest(
        """
            val x = 5.run {
                this * this
            }
        """.trimIndent(),
        """
            #include <stdint.h>
            #include "OtliScopes.h"
            
            OTLI_LET(int32_t, int32_t, _this_run, letRet, 5, {
                letRet = (_this_run * _this_run);
            });
            int32_t x = letRet;
            
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
            #include "OtliScopes.h"
            
            void main() {
            
                OTLI_LET_UNIT(int32_t, _this_run, 5, {
                        printf(PRId32, (_this_run * _this_run));
            
                });
                void;
            }
            
        """.trimIndent()
    )

    @Test
    fun also() = transformTest(
        """
            var y = 0
            val x = 5.also {
                y = it * it
            }
        """.trimIndent(),
        """
            #include <stdint.h>
            #include "OtliScopes.h"
            
            int32_t y = 0;
            OTLI_LET_UNIT(int32_t, it, 5, {
                    y = (it * it);
            
            });
            int32_t x = it;
            
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
            #include "OtliScopes.h"
            
            void main() {
            
                OTLI_LET_UNIT(int32_t, it, 5, {
                        printf(PRId32, (it * it));
            
                });
                it;
            }
            
        """.trimIndent()
    )
}
