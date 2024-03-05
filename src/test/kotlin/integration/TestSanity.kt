package integration

import lakefs.*
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test

class TestSanity : ConfTest() {

    @Test
    fun testRepositorySanity() {
        var repo = setupRepo()
        repo = Repo(repo.properties!!.id, repo.client)  // test the lakefs.repository function works properly
        val defaultBranch = "main"
        val expectedProperties = RepositoryProperties(id = repo.properties!!.id,
                                                      defaultBranch = defaultBranch,
                                                      storageNamespace = storageNamespace,
                                                      creationDate = repo.properties!!.creationDate)
        assert(repo.properties == expectedProperties)

        // Create with allow exists
        val newData = repo.create(storageNamespace, defaultBranch, true, existOk = true)
        assert(newData.properties == repo.properties)

        // Try to create twice and expect conflict
        assertThrows<ConflictException> {
            repo.create(storageNamespace, defaultBranch, true)
        }

        // Get metadata
        val md = repo.metadata
        assert(md.isEmpty())

        // List branches
        val branches = repo.branches
        assert(branches.size == 1)

        // Delete repository
        repo.delete()

        // Delete non existent
        assertThrows<NotFoundException> {
            repo.delete()
        }
    }

    @Test
    fun testBranchSanity() {
        val repo = setupRepo()
        val branchName = "test_branch"

        val mainBranch = repo branch "main"
        val newBranch = repo.branch(branchName).create("main")
        assert(newBranch.repoId == repo.properties!!.id)
        assert(newBranch.id == branchName)
        assert(newBranch.head.id == mainBranch.head.id)

        newBranch.delete()
        assertThrows<NotFoundException> {
            newBranch.head // pylint: disable = pointless-statement
        }
    }

    @Test
    fun testRefSanity() {
        val repo = setupRepo()
        val refId = "main"
        val ref = repo.ref(refId)
        assert(ref.repoId == repo.properties!!.id)
        assert(ref.id == refId)
        assert(ref.commit!!.metadata!!.isEmpty())
        assert(ref.commit!!.message == "Repository created")
    }

    @Test
    fun testTagSanity() {
        val repo = setupRepo()
        val tagName = "test_tag"
        val tag = repo tag tagName

        // expect not found
        assertThrows<NotFoundException> {
            tag.commit
        }

        val branch = repo branch "main"
        val commit = branch.commit!!
        val res = tag.create(commit)
        assert(res == tag)
        assert(tag.id == tagName)
        assert(tag.commit!!.metadata == commit.metadata)
        assert(tag.commit!!.message == commit.message)

        // Create again
        assertThrows<ConflictException> {
            tag.create(commit.id)
        }

        // Create again with exist_ok
        val tag2 = tag.create(tagName, true)
        assert(tag2 == tag)

        // Delete tag
        tag.delete()

        // expect not found
        assertThrows<NotFoundException> {
            tag.commit
        }

        // Delete twice
        assertThrows<NotFoundException> {
            tag.delete()
        }

        // Create again
        tag.create(commit.id)
    }

    @Test
    fun testObjectSanity() {
        val repo = setupRepo()
        val branch = "main"
        val file = newTempFile.apply { writeText("test_data") }
        val path = "test_obj"
        val metadata = mapOf("foo" to "bar")
//        val headers = mapOf("Accept" to "application/json",
//                            "Content-Type" to "application/octet-stream") + metadata.mapKeys { (k, _) -> "$LAKEFS_METADATA_PREFIX$k" }
//        println(headers)
//        //        obj = lakefs.WriteableObject(repository_id=repo.properties.id, reference_id="main", path=path, client=clt).upload(
//        //            data=data, metadata=metadata)
//        val resourcePath = "/repositories/${repo.id}/branches/$branch/objects"
//        val queryParams = URI(hashMapOf("path" to path).encode())
//        println(queryParams)
//        testClient.updateParamsForAuth(arrayOf("basic_auth"), null, headers, null, resourcePath, "POST", queryParams)
        val stats = testClient.objectsApi.uploadObject(repo.properties!!.id, branch, path).content(file).execute()
        //        with obj . reader () as fd:
        //        assert fd . read () == data
        println(stats)
        //        val stats = obj.stat()
        assert(stats.path == path)
        assert(stats.mtime <= System.currentTimeMillis())
        assert(stats.sizeBytes!! == file.length())
//        println(stats.metadata)
        //        assert(stats.metadata == metadata)
        assert(stats.contentType == "application/octet-stream")

        //        obj.delete()
        //        with expect_exception_context (ObjectNotFoundException):
        //        obj.stat()
    }
}