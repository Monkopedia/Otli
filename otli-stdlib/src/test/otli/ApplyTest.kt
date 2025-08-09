package otli

import kotlin.test.*

class ApplyTest {

    @Test
    fun testExec() {
        val x = 5
        x.apply {
            assertEquals(5, this)
        }
    }

    @Test
    fun testInitialization() {
        val x = 5.apply { this + 1 }
        assertEquals(5, x)
    }

    @Test
    fun testNested() {
        val x = 2.apply {
            val first = this
            3.apply {
                val second = this
                first + second
            }
        }
        assertEquals(2, x)
    }

    @Test
    fun testNestedUnit() {
        2.apply {
            val first = this
            3.apply {
                val second = this
                assertEquals(5, first + second)
            }
        }
    }

    @Test
    fun testNestedInnerUnit() {
        var x = 0
        2.apply {
            val first = this
            x = 3.apply {
                val second = this
                first + second
            }
        }
        assertEquals(3, x)
    }

    @Test
    fun testNestedOuterUnit() {
        2.apply {
            val first = this
            assertEquals(3, 3.apply {
                val second = this
                first + second
            })
        }
    }
}
