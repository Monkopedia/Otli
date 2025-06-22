package com.monkopedia.otli

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

class StructTest {
    @Test
    fun `parse data class test`() = transformTest(
        otliCode = """
            package otli.test
            data class A(
                val a: Int
            )
        """.trimIndent(),
        expected = """
            #include <stdbool.h>
            #include <stdint.h>
            #include <stdio>

            int32_t otli_test_A_component1(otli_test_A* thiz) {

                return thiz->a;
            }
            int otli_test_A_toString(otli_test_A* thiz, const char* buffer, size_t n) {
                return snprintf(buffer, n, "A(a="PRId32")", thiz->a);
            }
            int32_t otli_test_A_hashCode(otli_test_A* thiz) {

                return thiz->a;
            }
            bool otli_test_A_equals(otli_test_A* thiz, otli_test_A* other) {
                if (thiz->a != other->a) {
                    return false;
                }

                return true;
            }
            
            
        """.trimIndent()
    )

    @Test
    fun `parse data class method`() = transformTest(
        otliCode = """
            package otli.test
            data class A(
                val a: Int
            ) {
                fun aPlusOne() = a + this.a + 1
            }
        """.trimIndent(),
        expected = """
            #include <stdbool.h>
            #include <stdint.h>
            #include <stdio>

            int32_t otli_test_A_aPlusOne(otli_test_A* thiz) {

                return ((thiz->a + thiz->a) + 1);
            }
            int32_t otli_test_A_component1(otli_test_A* thiz) {

                return thiz->a;
            }
            int otli_test_A_toString(otli_test_A* thiz, const char* buffer, size_t n) {
                return snprintf(buffer, n, "A(a="PRId32")", thiz->a);
            }
            int32_t otli_test_A_hashCode(otli_test_A* thiz) {

                return thiz->a;
            }
            bool otli_test_A_equals(otli_test_A* thiz, otli_test_A* other) {
                if (thiz->a != other->a) {
                    return false;
                }

                return true;
            }
            
            
        """.trimIndent()
    )

    @Test
    fun `parse data class header`() = transformTest(
        otliCode = """
            package otli.test
            data class A(
                val a: Int
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
            } otli_test_A;

            int32_t otli_test_A_component1(otli_test_A* thiz);
            int otli_test_A_toString(otli_test_A* thiz, const char* buffer, size_t n);
            int32_t otli_test_A_hashCode(otli_test_A* thiz);
            bool otli_test_A_equals(otli_test_A* thiz, otli_test_A* other);
            
            #endif // __DATA_CLASS_HEADER_KT_H__
            
            
            """.trimIndent(),
            files["otli_test_data_class_header.kt.h"]
        )
    }
}
