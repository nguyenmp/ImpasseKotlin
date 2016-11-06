package ninja.mpnguyen.impassekotlin

interface Delta {
    fun apply(store : KeyValueStore) : KeyValueStore
}

class Delete(val id : Int) : Delta {
    override fun apply(store: KeyValueStore): KeyValueStore {
        delete(id, store)
    }

    private fun delete(val id : Int, store : KeyValueStore) : KeyValueStore {
        store.entities.remove(id)
        store.
    }
}

class Move(val id : Int, val new_parent : Int) : Delta {
}

class Rename(val id : Int, val new_name : Int) : Delta {
}

class Revalue(val id : Int, val new_value : Int) : Delta {
}

class CreateV(val id : Int, val new_value : Int) : Delta {
}

private fun rebase(
        deltas : Array<Delta>,
        original : KeyValueStore,
        updated : KeyValueStore
)