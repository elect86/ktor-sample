package lakefs

import io.lakefs.clients.sdk.model.Diff
import io.lakefs.clients.sdk.model.Diff.PathTypeEnum
import io.lakefs.clients.sdk.model.Diff.TypeEnum
import io.lakefs.clients.sdk.model.ObjectStats
import io.lakefs.clients.sdk.model.Repository
import java.time.Instant

interface WithPath {
    val path: String
}

/** NamedTuple representing a diff change between two refs in lakeFS */
data class Change(val type: TypeEnum,
                  val path: String,
                  val pathType: PathTypeEnum,
                  val sizeBytes: Long? = null) {

    constructor(diff: Diff) : this(diff.type, diff.path, diff.pathType, diff.sizeBytes)

    //    def __repr__(self):
    //    return f'Change(type="{self.type}", path="{self.path}", path_type="{self.path_type}")'

}

/** Represent a lakeFS object's stats */
data class ObjectInfo(override val path: String,
                      val physicalAddress: String,
                      val checksum: String,
                      val mtime: Long,
                      val physicalAddressExpiry: Long? = null,
                      val sizeBytes: Long? = null,
                      val metadata: Map<String, String>? = null,
                      val contentType: String? = null): WithPath {

    constructor(objectStats: ObjectStats): this(objectStats.path,
                                                objectStats.physicalAddress,
                                                objectStats.checksum,
                                                objectStats.mtime,
                                                objectStats.physicalAddressExpiry,
                                                objectStats.sizeBytes,
                                                objectStats.metadata,
                                                objectStats.contentType)

    //def __repr__(self):
    //return f'ObjectInfo(path="{self.path}")'
}

/** Represents a common prefix in lakeFS */
data class CommonPrefix(override val path: String): WithPath {

//    def __repr__(self):
//    return f'CommonPrefix(path="{self.path}")'
}

/** Represent a lakeFS repository's properties */
data class RepositoryProperties(
    val id: String,
    val creationDate: Instant,
    val defaultBranch: String,
    val storageNamespace: String) {

    constructor(repository: Repository) : this(repository.id,
                                               Instant.ofEpochMilli(repository.creationDate),
                                               repository.defaultBranch,
                                               repository.storageNamespace)
}
