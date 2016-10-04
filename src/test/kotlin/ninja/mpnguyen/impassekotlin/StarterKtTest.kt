package ninja.mpnguyen.impassekotlin

import org.junit.Assert.assertEquals
import org.junit.Test

class StarterKtTest {

    @Test
    fun testSha256() {
        val store = KeyValueStore()
        val representation = FileRepresentation(store, "hello world".toCharArray())
        val other_store = representation.open("hello world".toCharArray())
        assertEquals(store.value, other_store.value)
    }
}