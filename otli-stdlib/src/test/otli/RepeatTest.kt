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
