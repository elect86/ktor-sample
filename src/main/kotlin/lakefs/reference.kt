@file:OptIn(ExperimentalTypeInference::class)
@file:Suppress("NAME_SHADOWING")

package lakefs

import io.lakefs.clients.sdk.ApiClient
import io.lakefs.clients.sdk.ApiException
import io.lakefs.clients.sdk.model.*
import java.io.File
import kotlin.experimental.ExperimentalTypeInference

/**
 * Return a reference to a lakeFS commit.
 *
 * @param repoId the repository holding the commit
 * @param id a reference expression to the commit
 *
 * Any reference expression can be used as a reference_id, for example:
 *
 *  - 'main' (head of 'main' branch)
 *  - 'main@' (head of 'main' branch, only committed objects)
 *  - 'my_tag~3' (3 commits before 'my_tag')
 *
 *  See https://docs.lakefs.io/understand/model.html#ref-expressions for details.
 */
open class Reference(val repoId: String,
                     val id: String,
                     client: ApiClient) : BaseLakeFSObject(client) {

    /**
     * Returns an object generator for this reference, the generator can yield either a StoredObject or a CommonPrefix
     * object depending on the listing parameters provided.
     *
     * @param maxAmount Stop showing changes after this amount
     * @param after Return items after this value
     * @param prefix Return items prefixed with this value
     * @param delimiter Group common prefixes by this delimiter
    //     *             :param kwargs: Additional Keyword Arguments to send to the server
    //     *             :raise NotFoundException: if this reference or other_ref does not exist
    //     *             :raise NotAuthorizedException: if user is not authorized to perform this operation
    //     *             :raise ServerException: for any other errors
     */
    fun objects(maxAmount: Int? = null,
                after: String? = null,
                prefix: String? = null,
                delimiter: String): Iterator<WithPath/*StoredObject | CommonPrefix*/> =
        iterator {
            for (res in generateListing(maxAmount, after) { after ->
                client.objectsApi.listObjects(repoId, id)
                    .after(after)
                    .prefix(prefix)
                    .delimiter(delimiter)
                    .execute()
            }) {
                val clazz = when (res.pathType) {
                    ObjectStats.PathTypeEnum.OBJECT -> ObjectInfo(res)
                    else -> CommonPrefix(res.path)
                }
                yield(clazz)
            }
        }

    /**
     * Returns an object generator for this reference, the generator can yield either a StoredObject or a CommonPrefix
     * object depending on the listing parameters provided.
     *
     * @param maxAmount Stop showing changes after this amount
     * @param after Return items after this value
     * @param prefix Return items prefixed with this value
     * @param delimiter Group common prefixes by this delimiter
    //     *             :param kwargs: Additional Keyword Arguments to send to the server
    //     *             :raise NotFoundException: if this reference or other_ref does not exist
    //     *             :raise NotAuthorizedException: if user is not authorized to perform this operation
    //     *             :raise ServerException: for any other errors
     */
    fun objects(maxAmount: Int? = null,
                after: String? = null,
                prefix: String? = null): Iterator<ObjectInfo> =
        iterator {
            for (res in generateListing(maxAmount, after) { after ->
                client.objectsApi.listObjects(repoId, id)
                    .after(after)
                    .prefix(prefix)
                    .execute()
            }) {
                require(res.pathType == ObjectStats.PathTypeEnum.OBJECT)
                yield(ObjectInfo(res))
            }
        }


    internal var monkeyLogCommits: ((repository: String, ref: String) -> CommitList)? = null

    /**
     * Returns a generator of commits starting with this reference id
     *
     * @param maxAmount (Optional) limits the amount of results to return from the server
    //     *         :param kwargs: Additional Keyword Arguments to send to the server
    //     *         :raise NotFoundException: if reference by this id does not exist
    //     *         :raise NotAuthorizedException: if user is not authorized to perform this operation
    //     *         :raise ServerException: for any other errors
     */
    fun log(maxAmount: Int? = null, after: String? = null, amount: Int? = null, limit: Boolean? = null): Iterator<Commit> =
        iterator {
            for (res in generateListing(maxAmount, after) { after ->
                monkeyLogCommits?.invoke(repoId, id) ?: client.refsApi.logCommits(repoId, id)
                    .after(after)
                    .amount(amount)
                    .limit(limit)
                    .execute()
            })
                yield(res)
        }

    /**
     * Returns the underlying commit referenced by this reference id
     *
    //     *         :raise NotFoundException: if this reference does not exist
    //     *         :raise NotAuthorizedException: if user is not authorized to perform this operation
    //     *         :raise ServerException: for any other errors
     */
    var commit: Commit? = null
        get() {
            if (field == null)
                field = withApiExceptionHandler {
                    client.commitsApi.getCommit(repoId, id).execute()
                }
            return field
        }


    internal var monkeyDiffRefs: ((repository: String, leftRef: String, rightLeft: String) -> DiffList)? = null

    /**
     * Returns a diff generator of changes between this reference and other_ref
     *
     * @param otherRef The other ref to diff against
     * @param maxAmount Stop showing changes after this amount
     * @param after Return items after this value
     * @param prefix Return items prefixed with this value
     * @param delimiter Group common prefixes by this delimiter
    //     *         :param kwargs: Additional Keyword Arguments to send to the server
    //     *         :raise NotFoundException: if this reference or other_ref does not exist
    //     *         :raise NotAuthorizedException: if user is not authorized to perform this operation
    //     *         :raise ServerException: for any other errors
     */
    fun diff(otherRef: ReferenceType,
             maxAmount: Int? = null,
             after: String? = null,
             amount: Int? = null,
             prefix: String? = null,
             delimiter: String? = null,
             type: String? = null): Iterator<Change> {
        val otherRefId = getId(otherRef)
        return iterator {
            for (diff in generateListing(maxAmount, after) { after ->
                monkeyDiffRefs?.invoke(repoId, id, otherRefId) ?: client.refsApi.diffRefs(repoId, id, otherRefId)
                    .after(after)
                    .amount(amount)
                    .prefix(prefix)
                    .delimiter(delimiter)
                    .type(type)
                    .execute()
            })
                yield(Change(diff))
        }
    }

    /**
     * Merge this reference into destination branch
     *
     * @param destinationBranch The merge destination (either ID or branch object)
    //     *         :param kwargs: Additional Keyword Arguments to send to the server
     * @return The reference id of the merge commit
    //     *         :raise NotFoundException: if reference by this id does not exist, or branch doesn't exist
    //     *         :raise NotAuthorizedException: if user is not authorized to perform this operation
    //     *         :raise ServerException: for any other errors
     */
    fun mergeInto(destinationBranch: ReferenceType, message: String? = null): String {
        val branchId = getId(destinationBranch)
        //        val merge = lakefs_sdk.Merge(** kwargs)
        val merge = when {
            message != null -> Merge().apply { message(message) }
            else -> null
        }
        val res = client.refsApi.mergeIntoBranch(repoId, id, branchId).merge(merge).execute()
        return res.reference
    }

    /**
     * Returns an Object class representing a lakeFS object with this repo id, reference id and path
     *
     * @param path The object's path
     */
    fun storedObject(path: String): StoredObject = StoredObject(repoId, id, path, client)
    infix fun `object`(path: String): File = client.objectsApi.getObject(repoId, id, path).execute()
}

/**
 * Generic generator function, for lakefs-sdk listings functionality
 * @param func: The listing function
// *     :param args: The function args
 * @param maxAmount The max amount of objects to generate
// *     :param kwargs: The function kwargs
// *     :return: A generator based on the listing function
 */
@OverloadResolutionByLambdaReturnType
@JvmName("genRef")
fun generateListing(maxAmount: Int? = null, after: String? = null,
                    func: (after: String?) -> RefList) = iterator<Ref> {
    var maxAmount = maxAmount
    var after = after
    var hasMore = true
    while (hasMore) {
        val page = func(after)
        hasMore = page.pagination.hasMore
        after = page.pagination.nextOffset
        for (res in page.results) {
            yield(res)

            if (maxAmount != null) {
                maxAmount -= 1
                if (maxAmount <= 0)
                    return@iterator
            }
        }
    }
}

/**
 * Generic generator function, for lakefs-sdk listings functionality
 * @param func: The listing function
// *     :param args: The function args
 * @param maxAmount The max amount of objects to generate
// *     :param kwargs: The function kwargs
// *     :return: A generator based on the listing function
 */
@OverloadResolutionByLambdaReturnType
@JvmName("genCommit")
fun generateListing(maxAmount: Int? = null, after: String? = null,
                    func: (after: String?) -> CommitList) = iterator<Commit> {
    var maxAmount = maxAmount
    var after = after
    var hasMore = true
    while (hasMore) {
        val page = func(after)
        hasMore = page.pagination.hasMore
        after = page.pagination.nextOffset
        for (res in page.results) {
            yield(res)

            if (maxAmount != null) {
                maxAmount -= 1
                if (maxAmount <= 0)
                    return@iterator
            }
        }
    }
}

/**
 * Generic generator function, for lakefs-sdk listings functionality
 * @param func: The listing function
// *     :param args: The function args
 * @param maxAmount The max amount of objects to generate
// *     :param kwargs: The function kwargs
// *     :return: A generator based on the listing function
 */
@OverloadResolutionByLambdaReturnType
@JvmName("genDiff")
fun generateListing(maxAmount: Int? = null, after: String? = null,
                    func: (after: String?) -> DiffList) = iterator<Diff> {
    var maxAmount = maxAmount
    var after = after
    var hasMore = true
    while (hasMore) {
        val page = func(after)
        hasMore = page.pagination.hasMore
        after = page.pagination.nextOffset
        for (res in page.results) {
            yield(res)

            if (maxAmount != null) {
                maxAmount -= 1
                if (maxAmount <= 0)
                    return@iterator
            }
        }
    }
}


/**
 * Generic generator function, for lakefs-sdk listings functionality
 * @param func: The listing function
// *     :param args: The function args
 * @param maxAmount The max amount of objects to generate
// *     :param kwargs: The function kwargs
// *     :return: A generator based on the listing function
 */
@OverloadResolutionByLambdaReturnType
@JvmName("genObjectStats")
fun generateListing(maxAmount: Int? = null, after: String? = null,
                    func: (after: String?) -> ObjectStatsList) = iterator<ObjectStats> {
    var maxAmount = maxAmount
    var after = after
    var hasMore = true
    while (hasMore) {
        val page = func(after)
        hasMore = page.pagination.hasMore
        after = page.pagination.nextOffset
        for (res in page.results) {
            yield(res)

            if (maxAmount != null) {
                maxAmount -= 1
                if (maxAmount <= 0)
                    return@iterator
            }
        }
    }
}

typealias ReferenceType = Any //Union[str, Reference, Commit]

fun getId(ref: ReferenceType): String = when (ref) {
    is String -> ref
    is Reference -> ref.id
    else -> (ref as Commit).id
}