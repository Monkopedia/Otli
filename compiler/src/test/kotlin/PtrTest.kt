package com.monkopedia.otli

import kotlin.test.Test

class PtrTest {

    val cls = """data class MyCls(
                    val a: Int,
                    var b: Boolean
                )"""
    val clsBoilerPlate = """int32_t MyCls_component1(MyCls* thiz) {
    
                    return thiz->a;
                }
                bool MyCls_component2(MyCls* thiz) {
    
                    return thiz->b;
                }
                int MyCls_toString(MyCls* thiz, const char* buffer, size_t n) {
                    return snprintf(buffer, n, "MyCls(a="PRId32", b=%d)", thiz->a, thiz->b);
                }
                int32_t MyCls_hashCode(MyCls* thiz) {
    
                    int32_t result = thiz->a;
                    result = ((result * 31) + (int32_t)thiz->b);
                    return result;
                }
                bool MyCls_equals(MyCls* thiz, MyCls* other) {
                    if (thiz->a != other->a) {
                        return false;
                    }
    
                    if (thiz->b != other->b) {
                        return false;
                    }
    
                    return true;
                }"""

    @Test
    fun `test create and get`() = transformTest(
        """
                import otli.*
                
                $cls
                val inst = MyCls(2, false)
                val adr = inst.adr();
                val v = adr.get();
                
        """.trimIndent(),
        """
                #include <stdbool.h>
                #include <stdint.h>
                #include <stdio.h>
    
                $clsBoilerPlate
    
                MyCls inst = {.a = 2, .b = false};
                MyCls* adr = &inst;
                MyCls v = *adr;
                
        """.trimIndent()
    )

    @Test
    fun `test alloc`() = transformTest(
        """
                import otli.*
                
                $cls
                fun main() {
                    val inst = alloc(MyCls(2, false))
                    val direct = alloc(inst.get())
                    val real = direct.get()
                    val another = alloc(real.adr().get())
                    val cpd = alloc(inst.get().copy(4))
                }
                
        """.trimIndent(),
        """
                #include <malloc.h>
                #include <stdbool.h>
                #include <stdint.h>
                #include <stdio.h>
    
                $clsBoilerPlate
    
                void main() {
                
                    MyCls* _tmp_MyCls = (MyCls*)malloc(sizeof(MyCls));
                    _tmp_MyCls->a = 2;
                    _tmp_MyCls->b = false;
                    MyCls* inst = _tmp_MyCls;
                    MyCls* __tmp_MyCls = (MyCls*)malloc(sizeof(MyCls));
                    __tmp_MyCls->a = inst->a;
                    __tmp_MyCls->b = inst->b;
                    MyCls* direct = __tmp_MyCls;
                    MyCls real = *direct;
                    MyCls* ___tmp_MyCls = (MyCls*)malloc(sizeof(MyCls));
                    ___tmp_MyCls->a = real.a;
                    ___tmp_MyCls->b = real.b;
                    MyCls* another = ___tmp_MyCls;
                    MyCls* ____tmp_MyCls = (MyCls*)malloc(sizeof(MyCls));
                    ____tmp_MyCls->a = 4;
                    ____tmp_MyCls->b = inst->b;
                    MyCls* cpd = ____tmp_MyCls;
                }
                
        """.trimIndent()
    )
}
