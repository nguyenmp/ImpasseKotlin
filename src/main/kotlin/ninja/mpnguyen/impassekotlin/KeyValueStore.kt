package ninja.mpnguyen.impassekotlin

import java.util.*

data class Folder(
        val name : String,
        val id : Int,
        val folders : Array<Folder>,
        val values : Array<Value>
) {
    override fun equals(other: Any?): Boolean {
        return super.equals(other)
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }
}

data class Value (
    val name : String,
    val id : Int,
    val value : String
)

class KeyValueStore {
    val folders : Map<Int, Folder>
    val values : Map<Int, Value>
    val parents : Map<Int, Int>
    val entities : TreeSet<Int>

    constructor() {
        folders = HashMap<Int, Folder>()
        values = HashMap<Int, Value>()
        parents = HashMap<Int, Int>()
        entities = TreeSet<Int>()
    }

    constructor(bytes : ByteArray) {
        val string = String(bytes, Charsets.UTF_8)
        folders = HashMap<Int, Folder>()
        values = HashMap<Int, Value>()
        parents = HashMap<Int, Int>()
        entities = TreeSet<Int>()
    }

    fun toBytes() : ByteArray {
        val string = "TODO"
        return string.toByteArray(Charsets.UTF_8)
    }
}