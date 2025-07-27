package com.monkopedia.otli

import kotlin.test.Test

class CNameTest {

    @Test
    fun `test call function name and import`() = transformTest(
        """
            import otli.CName
            import otli.CImport
            
            @CImport("my_header.h")
            @CName("my_c_method")
            external fun someMethod(a: Int, b: Int): Int
            
            val a = someMethod(1, 5)
        """.trimIndent(),
        """
            #include <stdint.h>
            #include "my_header.h"
            
            
            int32_t a = my_c_method(1, 5);
            
        """.trimIndent()
    )

    @Test
    fun `test call function name and system import`() = transformTest(
        """
            import otli.CName
            import otli.CImport
            
            @CImport("my_header_system.h", true)
            @CName("my_c_method")
            external fun someMethod(a: Int, b: Int): Int
            
            val a = someMethod(1, 5)
        """.trimIndent(),
        """
            #include <my_header_system.h>
            #include <stdint.h>
            
            
            int32_t a = my_c_method(1, 5);
            
        """.trimIndent()
    )

    @Test
    fun `test call function name and file import`() = transformTest(
        """
            @file:CImport("my_header.h")
            import otli.CName
            import otli.CImport
            
            @CName("my_c_method")
            external fun someMethod(a: Int, b: Int): Int
            
            val a = someMethod(1, 5)
        """.trimIndent(),
        """
            #include <stdint.h>
            #include "my_header.h"
            
            
            int32_t a = my_c_method(1, 5);
            
        """.trimIndent()
    )

    @Test
    fun `test reference var name and import`() = transformTest(
        """
            import otli.CName
            import otli.CImport
            
            @CImport("my_header.h")
            @CName("my_c_variable")
            external val myExternalValue: Int
            
            val a = myExternalValue
        """.trimIndent(),
        """
            #include <stdint.h>
            #include "my_header.h"
            
            
            int32_t a = my_c_variable;
            
        """.trimIndent()
    )

    @Test
    fun `test reference var name and system import`() = transformTest(
        """
            import otli.CName
            import otli.CImport
            
            @CImport("my_header_system.h", true)
            @CName("my_c_variable")
            external val myExternalValue: Int
            
            val a = myExternalValue
        """.trimIndent(),
        """
            #include <my_header_system.h>
            #include <stdint.h>
            
            
            int32_t a = my_c_variable;
            
        """.trimIndent()
    )

    @Test
    fun `test reference var name and file import`() = transformTest(
        """
            @file:CImport("my_header.h")
            import otli.CName
            import otli.CImport
            
            @CName("my_c_variable")
            external val myExternalValue: Int
            
            val a = myExternalValue
        """.trimIndent(),
        """
            #include <stdint.h>
            #include "my_header.h"
            
            
            int32_t a = my_c_variable;
            
        """.trimIndent()
    )

    @Test
    fun `test reference struct name and import`() = transformTest(
        """
            import otli.CName
            import otli.CImport
            
            @CImport("my_header.h")
            @CName("my_struct_t")
            external class MyStruct(
                @CName("a_val")
                val aVal: Int,
                @CName("b_val")
                val bVal: Boolean
            )
            
            val a = MyStruct(5, true)
        """.trimIndent(),
        """
            #include "my_header.h"
            
            
            my_struct_t a = {.a_val = 5, .b_val = true};
            
        """.trimIndent()
    )

    @Test
    fun `test reference struct name and system import`() = transformTest(
        """
            import otli.CName
            import otli.CImport
            
            @CImport("my_header_system.h", true)
            @CName("my_struct_system_t")
            external class MyStructSystem(
                @CName("a_val")
                val aVal: Int,
                @CName("b_val")
                val bVal: Boolean
            )
            
            val a = MyStructSystem(5, true)
        """.trimIndent(),
        """
            #include <my_header_system.h>
            
            
            my_struct_system_t a = {.a_val = 5, .b_val = true};
            
        """.trimIndent()
    )

    @Test
    fun `test reference struct name and file import`() = transformTest(
        """
            @file:CImport("my_header.h")
            import otli.CName
            import otli.CImport
            
            @CName("my_struct_t")
            external class MyStructSystem(
                @CName("a_val")
                val aVal: Int,
                @CName("b_val")
                val bVal: Boolean
            )
            
            val a = MyStructSystem(5, true)
        """.trimIndent(),
        """
            #include "my_header.h"
            
            
            my_struct_t a = {.a_val = 5, .b_val = true};
            
        """.trimIndent()
    )

    @Test
    fun `test reference enum name and import`() = transformTest(
        """
            import otli.CName
            import otli.CNames
            import otli.CImport
            
            @CImport("my_header.h")
            @CName("my_enum_t")
            @CNames("first_value", "second_value")
            external enum class MyEnum {
                FIRST_VALUE,
                SECOND_VALUE
            }
            
            val a = MyEnum.SECOND_VALUE
        """.trimIndent(),
        """
            #include "my_header.h"
            
            
            my_enum_t a = second_value;
            
        """.trimIndent()
    )

    @Test
    fun `test reference enum name and system import`() = transformTest(
        """
            import otli.CName
            import otli.CNames
            import otli.CImport
            
            @CImport("my_header_system.h", true)
            @CName("my_enum_system_t")
            @CNames("first_value", "second_value")
            external enum class MyEnum {
                FIRST_VALUE,
                SECOND_VALUE
            }
            
            val a = MyEnum.SECOND_VALUE
        """.trimIndent(),
        """
            #include <my_header_system.h>
            
            
            my_enum_system_t a = second_value;
            
        """.trimIndent()
    )

    @Test
    fun `test reference enum name and file import`() = transformTest(
        """
            @file:CImport("my_header.h")
            import otli.CName
            import otli.CNames
            import otli.CImport
            
            @CName("my_enum_t")
            @CNames("first_value", "second_value")
            external enum class MyEnum {
                FIRST_VALUE,
                SECOND_VALUE
            }
            
            val a = MyEnum.SECOND_VALUE
        """.trimIndent(),
        """
            #include "my_header.h"
            
            
            my_enum_t a = second_value;
            
        """.trimIndent()
    )
}
