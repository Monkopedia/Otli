package otli

import kotlin.test.*

class LetTest {

    @Test
    fun testExec() {
        val x = 5
        x.let {
            assertEquals(5, it)
        }
    }

    @Test
    fun testInitialization() {
        val x = 5.let { it + 1 }
        assertEquals(6, x)
    }

    @Test
    fun testNested() {
        val x = 2.let { first ->
            3.let { second ->
                first + second
            }
        }
        assertEquals(5, x)
    }

    @Test
    fun testNestedUnit() {
        2.let { first ->
            3.let { second ->
                assertEquals(5, first + second)
            }
        }
    }

    @Test
    fun testNestedInnerUnit() {
        var x = 0
        2.let { first ->
            x = 3.let { second ->
                first + second
            }
        }
        assertEquals(5, x)
    }

    @Test
    fun testNestedOuterUnit() {
        2.let { first ->
            assertEquals(5, 3.let { second ->
                first + second
            })
        }
    }
}
