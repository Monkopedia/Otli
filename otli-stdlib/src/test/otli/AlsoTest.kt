package otli

import kotlin.test.*

class AlsoTest {

    @Test
    fun testExec() {
        val x = 5
        x.also {
            assertEquals(5, it)
        }
    }

    @Test
    fun testInitialization() {
        val x = 5.also { it + 1 }
        assertEquals(5, x)
    }

    @Test
    fun testNested() {
        val x = 2.also { first ->
            3.also { second ->
                first + second
            }
        }
        assertEquals(2, x)
    }

    @Test
    fun testNestedUnit() {
        2.also { first ->
            3.also { second ->
                assertEquals(5, first + second)
            }
        }
    }

    @Test
    fun testNestedInnealsoit() {
        var x = 0
        2.also { first ->
            x = 3.also { second ->
                first + second
            }
        }
        assertEquals(3, x)
    }

    @Test
    fun testNestedOuterUnit() {
        2.also { first ->
            assertEquals(3, 3.also { second ->
                first + second
            })
        }
    }
}
