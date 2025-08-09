package otli

import kotlin.test.*

class RunTest {

    @Test
    fun testExec() {
        val x = 5
        x.run {
            assertEquals(5, this)
        }
    }

    @Test
    fun testInitialization() {
        val x = 5.run { this + 1 }
        assertEquals(6, x)
    }

    @Test
    fun testNested() {
        val x = 2.run {
            val first = this
            3.run {
                val second = this
                first + second
            }
        }
        assertEquals(5, x)
    }

    @Test
    fun testNestedUnit() {
        2.run {
            val first = this
            3.run {
                val second = this
                assertEquals(5, first + second)
            }
        }
    }

    @Test
    fun testNestedInnerUnit() {
        var x = 0
        2.run {
            val first = this
            x = 3.run {
                val second = this
                first + second
            }
        }
        assertEquals(5, x)
    }

    @Test
    fun testNestedOuterUnit() {
        2.run {
            val first = this
            assertEquals(5, 3.run {
                val second = this
                first + second
            })
        }
    }
}
