package lakefs

import io.lakefs.clients.sdk.ApiClient
import io.lakefs.clients.sdk.ApiException
import io.lakefs.clients.sdk.model.Ref
import io.lakefs.clients.sdk.model.Repository
import io.lakefs.clients.sdk.model.RepositoryCreation

class Repo(val id: String,
           client: ApiClient) : BaseLakeFSObject(client), AutoCloseable {

    /** Return the repository's properties object */
    var properties: RepositoryProperties? = null
        get() {
            if (field == null) {
                val repo = client.repositoriesApi.getRepository(id).execute()
                field = RepositoryProperties(repo)
            }
            return field
        }


    internal var monkeyCreateRepository: ((repositoryCreation: RepositoryCreation) -> Repository)? = null
    internal var monkeyGetRepository: ((id: String) -> Repository)? = null

    /**
     * Create a new repository in lakeFS from this object
     *
     * @param storageNamespace Repository's storage namespace
     * @param defaultBranch The default branch for the repository. If None, use server default name
     * @param includeSamples Whether to include sample data in repository creation
     * @param existOk If False will throw an exception if a repository by this name already exists.
     * Otherwise, return the existing repository without creating a new one
    //     * @param kwargs: Additional Keyword Arguments to send to the server
     * @return: The lakeFS SDK object representing the repository
    //     *         :raise NotAuthorizedException: if user is not authorized to perform this operation
    //     *         :raise ServerException: for any other errors
     */
    fun create(storageNamespace: String,
               defaultBranch: String = "main",
               includeSamples: Boolean = false,
               existOk: Boolean = false): Repo {

        val repo = try {
            val repositoryCreation = RepositoryCreation()
                .name(id)
                .storageNamespace(storageNamespace)
                .defaultBranch(defaultBranch)
                .sampleData(includeSamples)
            monkeyCreateRepository?.invoke(repositoryCreation) ?: client.repositoriesApi.createRepository(repositoryCreation).execute()
        } catch (ex: ApiException) {
            if (existOk)
                monkeyGetRepository?.invoke(id) ?: client.repositoriesApi.getRepository(id).execute()
            else
                throw ex.specificException
        }
        properties = RepositoryProperties(repo)
        return this
    }


    internal var monkeyDeleteRepository: (() -> Unit)? = null
    /** Delete repository from lakeFS server */
    fun delete() = withApiExceptionHandler {
        monkeyDeleteRepository?.invoke() ?: client.repositoriesApi.deleteRepository(id).execute()
    }

    /**
     * Return a branch object using the current repository id and client
     * @param branchId name of the branch
     */
    infix fun branch(branchId: String) = Branch(id, branchId, client)

    /** Accessor for `main` branch */
    val main: Branch
        get() = branch("main")

    /**
     * Return a reference object using the current repository id and client
     * @param commitId id of the commit reference
     */
    infix fun commit(commitId: String) = Reference(id, commitId, client)

    /**
     * Return a reference object using the current repository id and client
     * @param refId branch name, commit id or tag id
     */
    infix fun ref(refId: String) = Reference(id, refId, client)

    /**
     * Return a tag object using the current repository id and client
     * @param tagId name of the tag
     */
    infix fun tag(tagId: String) = Tag(id, tagId, client)

    /**
     * Returns a generator listing for branches on the given repository
     *
     *         :param max_amount: Stop showing changes after this amount
     *         :param after: Return items after this value
     *         :param prefix: Return items prefixed with this value
     *         :param kwargs: Additional Keyword Arguments to send to the server
     *         :raise NotFoundException: if repository does not exist
     *         :raise NotAuthorizedException: if user is not authorized to perform this operation
     *         :raise ServerException: for any other errors
     */
    fun branches(maxAmount: Int? = null, after: String? = null, prefix: String? = null): Iterator<Branch> =
        iterator {
            for (res in generateListing(maxAmount, after) { after -> client.branchesApi.listBranches(id).after(after).prefix(prefix).execute() })
                yield(Branch(id, res.id, client))
        }

    val branches: MutableList<Ref>
        get() = client.branchesApi.listBranches(id).execute().results

    /**
     * Returns a generator listing for tags on the given repository
     *
     * @param maxamount Stop showing changes after this amount
     *         :param after: Return items after this value
     *         :param prefix: Return items prefixed with this value
     *         :param kwargs: Additional Keyword Arguments to send to the server
     *         :raise NotFoundException: if repository does not exist
     *         :raise NotAuthorizedException: if user is not authorized to perform this operation
     *         :raise ServerException: for any other errors
     */
    fun tags(maxAmount: Int? = null, after: String? = null, prefix: String? = null): Iterator<Tag> =
        iterator {
            for (res in generateListing(maxAmount, after) { after -> client.tagsApi.listTags(id).after(after).prefix(prefix).execute() })
                yield(Tag(id, res.id, client))
        }

    val tags: MutableList<Ref>
        get() = client.tagsApi.listTags(id).execute().results

    /** Returns the repository metadata */
    val metadata: MutableMap<String, String>
        get() = client.repositoriesApi.getRepositoryMetadata(id).execute()

    override fun close() = delete()
}