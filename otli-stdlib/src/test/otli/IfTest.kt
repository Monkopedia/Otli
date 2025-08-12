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
}
