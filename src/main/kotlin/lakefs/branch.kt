package lakefs

import io.lakefs.clients.sdk.ApiClient
import io.lakefs.clients.sdk.ApiException
import io.lakefs.clients.sdk.model.BranchCreation
import io.lakefs.clients.sdk.model.CherryPickCreation
import io.lakefs.clients.sdk.model.Commit
import io.lakefs.clients.sdk.model.CommitCreation
import io.lakefs.clients.sdk.model.ObjectErrorList
import io.lakefs.clients.sdk.model.PathList
import io.lakefs.clients.sdk.model.Ref
import io.lakefs.clients.sdk.model.ResetCreation
import io.lakefs.clients.sdk.model.RevertCreation

open class BaseBranch(repoId: String,
                      id: String,
                      client: ApiClient) : Reference(repoId, id, client) {

    /**
     * Returns a writable object using the current repo id, reference and path
     *
     * @param path The object's path
     */
    //    fun `object`(path: String): File = client.objectsApi.getObject(repoId, id, path).execute()

    /**
     * Returns a diff generator of uncommitted changes on this branch
     *
     * @param maxAmount Stop showing changes after this amount
     * @param after Return items after this value
     * @param prefix Return items prefixed with this value
    //     *         :param kwargs: Additional Keyword Arguments to send to the server
    //     *         :raise NotFoundException: if branch or repository do not exist
    //     *         :raise NotAuthorizedException: if user is not authorized to perform this operation
    //     *         :raise ServerException: for any other errors
     */
    fun uncommitted(maxAmount: Int? = null, after: String? = null, prefix: String? = null): Iterator<Change> =
        iterator {
            for (diff in generateListing(maxAmount, after) { after ->
                client.branchesApi.diffBranch(repoId, id).after(after).prefix(prefix).execute()
            })
                yield(Change(diff))
        }

    /**
     * Delete objects from lakeFS
     *
     * This method can be used to delete single/multiple objects from branch. It accepts both str and StoredObject
     * types as well as Iterables of these types.
     * Using this method is more performant than sequentially calling delete on objects as it saves the back and forth
     * from the server.
     *
     * This can also be used in combination with object listing. For example:
     *
    //     *             .. code-block:: python
    //     *
    //     *                 import lakefs
    //     *
    //     *                 branch = lakefs.repository("<repository_name>").branch("<branch_name>")
    //     *                 # list objects on a common prefix
    //     *                 objs = branch.objects(prefix="my-object-prefix/", max_amount=100)
    //     *                 # delete objects which have "foo" in their name
    //     *                 branch.delete_objects([o.path for o in objs if "foo" in o.path])
     *
     *             :param object_paths: a single path or an iterable of paths to delete
     *             :raise NotFoundException: if branch or repository do not exist
     *             :raise NotAuthorizedException: if user is not authorized to perform this operation
     *             :raise ServerException: for any other errors
     */
    fun deleteObjects(objectPaths: Any/*str | StoredObject | Iterable[str | StoredObject]*/): ObjectErrorList? {
        val paths = when (objectPaths) {
            is String -> listOf(objectPaths)
            is StoredObject -> listOf(objectPaths.path)
            else -> when {
                (objectPaths as List<*>)[0] is StoredObject -> (objectPaths as List<StoredObject>).map { it.path }
                else -> objectPaths as List<String>
            }
        }
        return client.objectsApi.deleteObjects(repoId, id, PathList().paths(paths)).execute()
    }

    /**
     * Reset uncommitted changes (if any) on this branch
     *
     * @param pathType the type of path to reset ('common_prefix', 'object', 'reset' - for all changes)
     * @param path the path to reset (optional) - if path_type is 'reset' this parameter is ignored
    //     *         :raise ValidationError: if path_type is not one of the allowed values
    //     *         :raise NotFoundException: if branch or repository do not exist
    //     *         :raise NotAuthorizedException: if user is not authorized to perform this operation
    //     *         :raise ServerException: for any other errors
     */
    fun resetChanges(pathType: ResetCreation.TypeEnum = ResetCreation.TypeEnum.RESET,
                     path: String? = null) {
        val resetCreation = ResetCreation().path(path).type(pathType)
        client.branchesApi.resetBranch(repoId, id, resetCreation).execute()
    }
}

/** Class representing a branch in lakeFS. */
class Branch(repositoryId: String,
             branchId: String,
             client: ApiClient) : BaseBranch(repositoryId, branchId, client) {

    /** For branches override the default _get_commit method to ensure we always fetch the latest head */
    //    def get_commit(self):
    //        self._commit = None
    //        return super().get_commit()

    /**
     * Cherry-pick a given reference onto the branch.
     *
     * @param reference ID of the reference to cherry-pick.
     * @param parentNumber When cherry-picking a merge commit, the parent number (starting from 1)
     *                      with which to perform the diff. The default branch is parent 1.
     * @return The cherry-picked commit at the head of the branch.
    //     *         :raise NotFoundException: If either the repository or target reference do not exist.
    //     *         :raise NotAuthorizedException: If the user is not authorized to perform this operation.
    //     *         :raise ServerException: For any other errors.
     */
    fun cherryPick(reference: ReferenceType, parentNumber: Int? = null): Commit {
        val ref = getId(reference)
        val cherryPickCreation = CherryPickCreation().ref(ref).parentNumber(parentNumber)
        return client.branchesApi.cherryPick(repoId, id, cherryPickCreation).execute()
    }


    internal var monkeyCreateBranch: ((repository: String, branchCreation: BranchCreation) -> Unit)? = null

    /**
     * Create a new branch in lakeFS from this object
     *
    //     *         Example of creating a new branch:
    //     *
    //     *         .. code-block:: python
    //     *
    //     *             import lakefs
    //     *
    //     *             branch = lakefs.repository("<repository_name>").branch("<branch_name>").create("<source_reference>")
     *
     * @param sourceReference The reference to create the branch from (reference ID, object or Commit object)
     * @param existOk If False will throw an exception if a branch by this name already exists. Otherwise,
     *             return the existing branch without creating a new one
     * @return The lakeFS SDK object representing the branch
    //     *         :raise NotFoundException: if repo, branch or source reference id does not exist
    //     *         :raise ConflictException: if branch already exists and exist_ok is False
    //     *         :raise NotAuthorizedException: if user is not authorized to perform this operation
    //     *         :raise ServerException: for any other errors
     */
    fun create(sourceReference: ReferenceType, existOk: Boolean = false): Branch {
        val referenceId = getId(sourceReference)
        val branchCreation = BranchCreation().name(id).source(referenceId)
        try {
            monkeyCreateBranch?.invoke(repoId, branchCreation) ?: client.branchesApi.createBranch(repoId, branchCreation).execute()
        } catch (ex: ApiException) {
            if (!existOk)
                throw ex.specificException
        }
        return this
    }

    internal var monkeyGetBranch: ((repository: String, branch: String) -> Ref)? = null

    /** Get the commit reference this branch is pointing to */
    val head: Reference
        get() = withApiExceptionHandler {
            val branch = monkeyGetBranch?.invoke(repoId, id) ?: client.branchesApi.getBranch(repoId, id).execute()
            Reference(repoId, branch.commitId, client)
        }


    internal var monkeyCommit: ((repository: String, branch: String, commitCreation: CommitCreation) -> Commit)? = null

    /**
     * Commit changes on the current branch
     *
     * @param message Commit message
     * @param metadata Metadata to attach to the commit
    //     *         :param kwargs: Additional Keyword Arguments for commit creation
     * @return The new reference after the commit
    //     *         :raise NotFoundException: if branch by this id does not exist
    //     *         :raise ForbiddenException: if commit is not allowed on this branch
    //     *         :raise NotAuthorizedException: if user is not authorized to perform this operation
    //     *         :raise ServerException: for any other errors
     */
    fun commit(message: String, metadata: Map<String, String>? = null, allowEmpty: Boolean? = null/*, ignoredField: String? = null*/): Reference {
        val commitsCreation = CommitCreation().message(message).metadata(metadata).allowEmpty(allowEmpty)

        //        with api_exception_handler ():
        val c = monkeyCommit?.invoke(repoId, id, commitsCreation) ?: client.commitsApi.commit(repoId, id, commitsCreation).execute()
        return Reference(repoId, c.id, client)
    }


    internal var monkeyDeleteBranch: ((repository: String, branch: String) -> Unit)? = null

    /**
     * Delete branch from lakeFS server
     *
    //     *         :raise NotFoundException: if branch or repository do not exist
    //     *         :raise NotAuthorizedException: if user is not authorized to perform this operation
    //     *         :raise ForbiddenException: for branches that are protected
    //     *         :raise ServerException: for any other errors
     */
    fun delete() = monkeyDeleteBranch?.invoke(repoId, id) ?: client.branchesApi.deleteBranch(repoId, id).execute()


    internal var monkeyRevertBranch: ((repository: String, branch: String, revertCreation: RevertCreation) -> Unit)? = null
    internal var monkeyGetCommit: ((repository: String, commitId: String) -> Commit)? = null

    /**
     * revert the changes done by the provided reference on the current branch
     *
    //     *         :param reference_id: (Optional) The reference ID to revert
    //     *
    //     *             .. deprecated:: 0.4.0
    //     *                 Use ``reference`` instead.
     *
     * @param parentNumber when reverting a merge commit, the parent number (starting from 1) relative to which to
     *             perform the revert. The default for non merge commits is 0
     * @param reference the reference to revert
     * @return The commit created by the revert
    //     *         :raise NotFoundException: if branch by this id does not exist
    //     *         :raise NotAuthorizedException: if user is not authorized to perform this operation
    //     *         :raise ServerException: for any other errors
     */
    fun revert(reference: ReferenceType, parentNumber: Int = 0): Commit {
        require(parentNumber >= 0) { "parentNumber must be greater than or equal to 0" }

        val revertCreation = RevertCreation().ref(getId(reference)).parentNumber(parentNumber)
        monkeyRevertBranch?.invoke(repoId, id, revertCreation) ?: client.branchesApi.revertBranch(repoId, id, revertCreation).execute()
        return monkeyGetCommit?.invoke(repoId, id) ?: client.commitsApi.getCommit(repoId, id).execute()
    }

    //    def import_data(self, commit_message: str = "", metadata: Optional[dict] = None) -> ImportManager:
    //    """
    //        Import data to lakeFS
    //
    //        :param metadata: metadata to attach to the commit
    //        :param commit_message: once the data is imported, a commit is created with this message. If default (empty)
    //            message is provided, uses the default server commit message for imports.
    //        :return: an ImportManager object
    //        """
    //    return ImportManager(self._repo_id, self._id, commit_message, metadata, self._client)

    //    @contextmanager
    //    def transact(self, commit_message: str = "", commit_metadata: Optional[Dict] = None,
    //    delete_branch_on_error: bool = True) -> _Transaction:
    //    """
    //        Create a transaction for multiple operations.
    //        Transaction allows for multiple modifications to be performed atomically on a branch,
    //        similar to a database transaction.
    //        It ensures that the branch remains unaffected until the transaction is successfully completed.
    //        The process includes:
    //
    //        1. Creating an ephemeral branch from this branch
    //        2. Perform object operations on ephemeral branch
    //        3. Commit changes
    //        4. Merge back to source branch
    //        5. Delete ephemeral branch
    //
    //        Using a transaction the code for this flow will look like this:
    //
    //        .. code-block:: python
    //
    //            import lakefs
    //
    //            branch = lakefs.repository("<repository_name>").branch("<branch_name>")
    //            with branch.transact(commit_message="my transaction") as tx:
    //                for obj in tx.objects(prefix="prefix_to_delete/"):  # Delete some objects
    //                    obj.delete()
    //
    //                # Create new object
    //                tx.object("new_object").upload("new object data")
    //
    //        Note that unlike database transactions, lakeFS transaction does not take a "lock" on the branch, and therefore
    //        the transaction might fail due to changes in source branch after the transaction was created.
    //
    //        :param commit_message: once the transaction is committed, a commit is created with this message
    //        :param commit_metadata: user metadata for the transaction commit
    //        :param delete_branch_on_error: Defaults to True. Ensures ephemeral branch is deleted on error.
    //        :return: a Transaction object to perform the operations on
    //        """
    //    with Transaction(self._repo_id, self._id, commit_message, commit_metadata, delete_branch_on_error,
    //    self._client) as tx:
    //    yield tx
}