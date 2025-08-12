package otli

import kotlin.test.*

class IfTest {

    @Test
    fun testIfBranch() {
        val v = true
        if (v) {
            // Expected
        } else {
            fail("Should not reach else branch")
        }
    }

    @Test
    fun testElseBranch() {
        val v = false
        if (v) {
            fail("Should not reach if branch")
        } else {
            // Expected
        }
    }

    @Test
    fun testElseIfBranch() {
        val v = false
        if (v) {
            fail("Should not reach if branch")
        } else if (true) {
            // Expected
        } else {
            fail("Should not reach else branch")
        }
    }

    @Test
    fun testWhen() {
        for (i in 0 until 8) {
            when (i) {
                0 -> assertEquals(0, i)
                1 -> assertEquals(1, i)
                2 -> assertEquals(2, i)
                3 -> assertEquals(3, i)
                4 -> assertEquals(4, i)
                5 -> assertEquals(5, i)
                6 -> assertEquals(6, i)
                else -> assertEquals(7, i)
            }
        }
    }

    @Test
    fun testWhenNoSubject() {
        for (i in 0 until 8) {
            when {
                i == 0 -> assertEquals(0, i)
                i == 1 -> assertEquals(1, i)
                i == 2 -> assertEquals(2, i)
                i == 3 -> assertEquals(3, i)
                i == 4 -> assertEquals(4, i)
                i == 5 -> assertEquals(5, i)
                i == 6 -> assertEquals(6, i)
                else -> assertEquals(7, i)
            }
        }
    }

    @Test
    fun testWhenNoSubject_complex() {
        for (i in 0 until 8) {
            when {
                i.let { it * it } == 0 -> assertEquals(0, i)
                i.let { it * it } == 1 -> assertEquals(1, i)
                i.let { it * it } == 4 -> assertEquals(2, i)
                i.let { it * it } == 9 -> assertEquals(3, i)
                i.let { it * it } == 16 -> assertEquals(4, i)
                i.let { it * it } == 25 -> assertEquals(5, i)
                i.let { it * it } == 36 -> assertEquals(6, i)
                else -> assertEquals(7, i)
            }
        }
    }
}
