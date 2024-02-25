package lakefs

import io.lakefs.clients.sdk.ApiClient
import io.lakefs.clients.sdk.ApiException
import io.lakefs.clients.sdk.model.TagCreation

class Tag(repositoryId: String,
          tagId: String,
          client: ApiClient): Reference(repositoryId, tagId, client) {

    /**
     * Create a tag from the given source_ref
     *
     * @param sourceRef: The reference to create the tag on (either ID or Reference object)
     * @param existOk: If True returns the existing Tag reference otherwise raises exception
     * @return A lakeFS SDK Tag object
//     *         :raise NotAuthorizedException: if user is not authorized to perform this operation
//     *         :raise NotFoundException: if source_ref_id doesn't exist on the lakeFS server
//     *         :raise ServerException: for any other errors.
     */
    fun create(sourceRef: ReferenceType, existOk: Boolean = false): Tag {
        val sourceRefId = getId(sourceRef)
        val tagCreation = TagCreation().id(id).ref(sourceRefId)

        try {
            client.tagsApi.createTag(repoId, tagCreation).execute()
        } catch (e: ApiException) {
            if (!existOk) throw e
        }

        return this
    }

    /**
     * Delete the tag from the lakeFS server
     *
//     *         :raise NotAuthorizedException: if user is not authorized to perform this operation
//     *         :raise NotFoundException: if source_ref_id doesn't exist on the lakeFS server
//     *         :raise ServerException: for any other errors
     */
    fun delete() {
        client.tagsApi.deleteTag(repoId, id).execute()
        commit = null
    }
}