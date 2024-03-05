package integration

import io.lakefs.clients.sdk.model.Diff
import io.lakefs.clients.sdk.model.ResetCreation
import lakefs.Branch
import kotlin.test.Test

class TestBranch : ConfTest() {

    @Test
    fun testRevert() {
        val repo = setupRepo()
        val testBranch = repo branch "main"
        val initialContent = "test_content"
        testBranch.storedObject("test_object").upload(initialContent)
        testBranch.commit("test_commit", mapOf("test_key" to "test_value"))

        val overrideContent = "override_test_content"
//        val obj = testBranch.storedObject("test_object").upload(overrideContent)
        testBranch.commit("override_data")

        assert(testBranch.`object`("test_object").readText() == overrideContent)

        val c = testBranch.revert(testBranch.head)
        assert(c.message.startsWith("Revert"))

        assert(testBranch.`object`("test_object").readText() == initialContent)
    }

    @Test
    fun testCherryPick() {
        val repo = setupRepo()
        val mainBranch = repo branch "main"
        val testBranch = repo.branch("testest").create("main")

        val initialContent = "test_content"
        testBranch.storedObject("test_object").upload(initialContent)
        val testCommit = testBranch.commit("test_commit", mapOf("test_key" to "test_value")).commit!!

        val cherryPicked = mainBranch.cherryPick(testBranch.head)
        assert(testBranch.`object`("test_object").exists())
        // SHAs are not equal, so we exclude them from eq checks.
        assert(cherryPicked.message == testCommit.message)
        // cherry - picks have origin and source ref name attached as metadata (at minimum),
        // so we only check that the additional user -supplied metadata is present .
        assert(testCommit.metadata!!.size <= cherryPicked.metadata!!.size)
        // check that the cherry -pick origin is exactly testest@ HEAD.
        assert(cherryPicked.metadata!!["cherry-pick-origin"] == testCommit.id)
    }

    fun uploadData(branch: Branch, pathAndData: List<String>, multiplier: Int = 1) {
        for (s in pathAndData)
            branch.storedObject(s).upload(s.repeat(multiplier))
    }

    fun validateUncommittedChanges(branch: Branch, expected: List<String>, changeType: Diff.TypeEnum = Diff.TypeEnum.ADDED, prefix: String = "") {
        var count = 0
        for ((index, change) in branch.uncommitted(maxAmount = 10, prefix = prefix).withIndex()) {
            assert(change.path == expected[index])
            assert(change.pathType == Diff.PathTypeEnum.OBJECT)
            assert(change.type == changeType)
            assert(change.sizeBytes!!.toInt() == if (changeType == Diff.TypeEnum.REMOVED) 0 else expected[index].length)
            count += 1
            if (count != expected.size)
                error("Expected ${expected.size} changes, got $count")
        }
    }

//    @Test
    fun testResetChanges() {
        val repo = setupRepo()
        val testBranch = repo branch "main"
        val paths = listOf("a", "b", "bar/a", "bar/b", "bar/c", "c", "foo/a", "foo/b", "foo/c")
        uploadData(testBranch, paths)

        validateUncommittedChanges(testBranch, paths)

        validateUncommittedChanges(testBranch, listOf("bar/a", "bar/b", "bar/c"), prefix = "bar")
        testBranch.resetChanges(ResetCreation.TypeEnum.OBJECT, "bar/a")
        validateUncommittedChanges(testBranch, listOf("a", "b", "bar/b", "bar/c", "c", "foo/a", "foo/b", "foo/c"))

        testBranch.resetChanges(ResetCreation.TypeEnum.OBJECT, "bar/")

        validateUncommittedChanges(testBranch, listOf("a", "b", "bar/b", "bar/c", "c", "foo/a", "foo/b", "foo/c"))

        testBranch.resetChanges(ResetCreation.TypeEnum.COMMON_PREFIX, "foo/")
        validateUncommittedChanges(testBranch, listOf("a", "b", "bar/b", "bar/c", "c"))

        testBranch.resetChanges()
        validateUncommittedChanges(testBranch, emptyList())
    }
}