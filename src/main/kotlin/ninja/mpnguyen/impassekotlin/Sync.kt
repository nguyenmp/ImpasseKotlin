package ninja.mpnguyen.impassekotlin


interface CloudStorageProvider {
    fun update(version : String, content : FileRepresentation)
    fun update() : FileRepresentation
}


class DropboxProvider : CloudStorageProvider {
    val token
}


class SyncedData {
    val storage_providers = Array<CloudStorageProvider>()
    val version : String
    fun pull() : FileRepresentation {
        // TODO: Download from all the cloud providers
        // TODO: Discard any that dont authenticate and warn user of compromise
        // TODO: Bucket by versions
        // TODO: From most recent to least recent, pick one that has >2 OK
    }

    fun push(value : FileRepresentation) {
        // TODO: For each cloud provider, try to update
        // TODO:
        // TODO: Replay changes on new data
    }
}


