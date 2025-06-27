package com.monkopedia.otli

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

class StructTest {
    val component1 = """int32_t otli_test_A_component1(otli_test_A* thiz) {

                return thiz->a;
            }"""
    val component2 = """int32_t otli_test_A_component2(otli_test_A* thiz) {

                return thiz->b;
            }"""
    val toString = """int otli_test_A_toString(otli_test_A* thiz, const char* buffer, size_t n) {
                return snprintf(buffer, n, "A(a="PRId32", b="PRId32")", thiz->a, thiz->b);
            }"""
    val hashCode = """int32_t otli_test_A_hashCode(otli_test_A* thiz) {

                int32_t result = thiz->a;
                result = ((result * 31) + thiz->b);
                return result;
            }"""
    val equals = """bool otli_test_A_equals(otli_test_A* thiz, otli_test_A* other) {
                if (thiz->a != other->a) {
                    return false;
                }
            
                if (thiz->b != other->b) {
                    return false;
                }

                return true;
            }"""

    @Test
    fun `parse data class test`() = transformTest(
        otliCode = """
            package otli.test
            data class A(
                val a: Int,
                val b: Int
            )
        """.trimIndent(),
        expected = """
            #include <stdbool.h>
            #include <stdint.h>
            #include <stdio.h>

            $component1
            $component2
            $toString
            $hashCode
            $equals
            
            
        """.trimIndent()
    )

    @Test
    fun `parse data class method`() = transformTest(
        otliCode = """
            package otli.test
            data class A(
                val a: Int,
                val b: Int
            ) {
                fun aPlusOne() = a + this.a + 1
            }
        """.trimIndent(),
        expected = """
            #include <stdbool.h>
            #include <stdint.h>
            #include <stdio.h>

            int32_t otli_test_A_aPlusOne(otli_test_A* thiz) {

                return ((thiz->a + thiz->a) + 1);
            }
            $component1
            $component2
            $toString
            $hashCode
            $equals
            
            
        """.trimIndent()
    )

    @Test
    fun `parse data class header`() = transformTest(
        otliCode = """
            package otli.test
            data class A(
                val a: Int,
                val b: Int
            )
        """.trimIndent(),
        expected = """
        """.trimIndent(),
        file = File("/tmp/data_class_header.kt").toPath()
    ) { files ->
        assertEquals(
            """
            #ifndef __DATA_CLASS_HEADER_KT_H__
            #define __DATA_CLASS_HEADER_KT_H__
            #include <stdbool.h>
            #include <stdint.h>
            
            typedef struct {
                int32_t a;
                int32_t b;
            } otli_test_A;

            int32_t otli_test_A_component1(otli_test_A* thiz);
            int32_t otli_test_A_component2(otli_test_A* thiz);
            int otli_test_A_toString(otli_test_A* thiz, const char* buffer, size_t n);
            int32_t otli_test_A_hashCode(otli_test_A* thiz);
            bool otli_test_A_equals(otli_test_A* thiz, otli_test_A* other);
            
            #endif // __DATA_CLASS_HEADER_KT_H__
            
            
            """.trimIndent(),
            files["otli_test_data_class_header.kt.h"]
        )
    }

    @Test
    fun `parse data call copy`() = transformTest(
        otliCode = """
            package otli.test
            data class A(
                val a: Int,
                val b: Int
            )
            
            val a = A(1, 2)
            val b = a.copy(2)
        """.trimIndent(),
        expected = """
            #include <stdbool.h>
            #include <stdint.h>
            #include <stdio.h>

            $component1
            $component2
            $toString
            $hashCode
            $equals
            
            otli_test_A _a = {a : 1, b : 2};
            otli_test_A _b = {a : 2, b : _a.b};
            
        """.trimIndent()
    )
}
