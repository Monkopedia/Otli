package com.monkopedia.otli

import java.io.File
import kotlin.math.exp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.expect

class TestTest {
    @Test
    fun testClass() {
        transformTest(
            """
                import kotlin.test.*
                
                class TestClass {
                    @Test
                    fun myFirstTest() {
                        assertEquals(1, 1)
                    }
                
                }
                
            """.trimIndent(),
            "unused",
            File("/tmp/assertion.kt").also {
                it.deleteOnExit()
            }.toPath()
        ) {files ->
            assertEquals(setOf("assertion.kt.h", "assertion.kt.c", "test_main.c"), files.keys)
            assertEquals("""
                #ifndef __ASSERTION_KT_H__
                #define __ASSERTION_KT_H__
                #include <CUnit/CUnit.h>
                
                void TestClass_myFirstTest();
                extern CU_TestInfo TestClass_tests[];
                
                #endif // __ASSERTION_KT_H__
                
                
            """.trimIndent(), files["assertion.kt.h"])
            assertEquals("""
                #include <CUnit/CUnit.h>
                #include "assertion.kt.h"
                
                void TestClass_myFirstTest() {
    
                    CU_assertImplementation(1 == 1, 5, "assertEquals(1, 1)", "assertion.kt", "myFirstTest", CU_FALSE);
                }
                CU_TestInfo TestClass_tests[] = {
                    {"myFirstTest", TestClass_myFirstTest}, 
                    {NULL}
                };
                
                
            """.trimIndent(), files["assertion.kt.c"])
            assertEquals("""
                #include <CUnit/Basic.h>
                #include <CUnit/CUnit.h>
                #include <stddef.h>
                #include <stdio.h>
                #include <stdlib.h>
                #include "assertion.kt.h"
                
                int main() {
                    CU_SuiteInfo target_suites[] = {
                        {"TestClass", NULL, NULL, NULL, NULL, TestClass_tests}, 
                        CU_SUITE_INFO_NULL
                    };
                    CU_ErrorAction error_action = CUEA_IGNORE;
                    if (CUE_SUCCESS != CU_initialize_registry()) {
                        return CU_get_error();
                    }

                    if (CU_register_suites(target_suites) != CUE_SUCCESS) {
                        fprintf(stderr, "suite registration failed - %s\n", CU_get_error_msg());
                        exit(1);
                    }

                    CU_basic_set_mode(CU_BRM_VERBOSE);
                    printf("\nTests completed with return value %d\n", CU_basic_run_tests());
                    CU_set_error_action(error_action);
                    CU_cleanup_registry();
                    return CU_get_error();
                }
                
            """.trimIndent(), files["test_main.c"])
        }
    }

    @Test
    fun testRepeat() = transformTest(
        otliCode = """
            package otli

            import kotlin.test.*

            class RepeatTest {

                @Test
                fun testRepeat() {
                    var c = 0;
                    repeat(5) {
                        c++
                    }
                    assertEquals(5, c);
                }
            }
        """.trimIndent(),
        expected = """
            #include <CUnit/CUnit.h>
            #include <stdint.h>
            #include "OtliLoops.h"
            #include "otli_repeating_test.kt.h"

            void otli_RepeatTest_testRepeat() {

                int32_t c = 0;
                OTLI_REPEAT(5, {
                        int32_t tmp0 = c;
                        c = (tmp0 + 1);
                        tmp0;

                });
                CU_assertImplementation(5 == c, 12, "assertEquals(5, c)", "repeating_test.kt", "testRepeat", CU_FALSE);
            }
            CU_TestInfo otli_RepeatTest_tests[] = {
                {"testRepeat", otli_RepeatTest_testRepeat}, 
                {NULL}
            };
            
            
        """.trimIndent(),
        file = File("/tmp/", "repeating_test.kt").toPath()
    )
}
