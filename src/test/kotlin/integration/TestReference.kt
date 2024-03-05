package integration

import io.lakefs.clients.sdk.model.Diff
import lakefs.repo
import kotlin.test.Test

class TestReference : ConfTest() {

    @Test
    fun testReferenceLog() {
        val branch = setupBranchWithCommits()

        for ((i, c) in branch.log(maxAmount = 199).withIndex())
            assert(c.message == "commit $i")

        val commits = branch.log(maxAmount = 2000).asSequence().toList()
        for ((i, c) in commits.dropLast(1).withIndex())  // Ignore initial/last commit
            assert(c.message == "commit $i")
        assert(commits.count() == 200)

        assert(branch.log(limit = true, amount = 10, maxAmount = 100).asSequence().count() == 10)
    }

    @Test
    fun testReferenceDiff() {
        val branch = setupBranchWithCommits()

        val commits = branch.log(maxAmount = 2).asSequence().toList()
        assert(branch.diff(branch.commit!!.id).asSequence().count() == 0)
        var changes = branch.diff(commits[0].id, type = "two_dot").asSequence().toList()
        assert(changes.isEmpty())

        changes = branch.diff(commits[1].id, type = "two_dot").asSequence().toList()
        assert(changes.size == 1)
        assert(changes[0].path == "test1")
        assert(changes[0].type == Diff.TypeEnum.REMOVED)

        val otherBranch = testClient.repo(branch.repoId).branch("other_branch").create("test_branch")
        otherBranch.storedObject("prefix1/test1").upload(data = "data1")
        otherBranch.storedObject("prefix2/test2").upload(data = "data2")
        otherBranch.commit("other commit")

        changes = branch.diff(otherBranch).asSequence().toList()
        assert(changes.size == 2)

        changes = branch.diff(otherBranch, prefix = "prefix2").asSequence().toList()
        assert(changes.size == 1)
        assert(changes[0].path == "prefix2/test2")
        assert(changes[0].type == Diff.TypeEnum.ADDED)
    }

    @Test
    fun testReferenceMergeInto() {
        val branch = setupBranchWithCommits()
        val repo = testClient repo branch.repoId
        val main = repo branch "main"

        val commits = branch.log(maxAmount = 2).asSequence().toList()
        val otherBranch = repo.branch("test_reference_merge_into").create(main)
        val ref = repo.ref(commits[1].id)
        ref.mergeInto(otherBranch, message = "Merge1")
        assert(otherBranch.commit!!.message == "Merge1")
        assert(otherBranch.log(maxAmount = 2).asSequence().toList()[1].id == commits[1].id)

        branch.mergeInto(otherBranch.id, message = "Merge2")
        //        println(otherBranch.commit!!.message)
        //        assert(otherBranch.commit!!.message == "Merge2")
        assert(otherBranch.log(maxAmount = 3).asSequence().toList()[2].id == commits[0].id)
    }

    @Test
    fun testReferenceObjects() {
        val repo = setupRepo()
        val testBranch = repo branch "main"
        val pathAndData = listOf("a", "b", "bar/a", "bar/b", "bar/c", "c", "foo/a", "foo/b", "foo/c")
        for (s in pathAndData)
            testBranch.storedObject(s).upload(s)

        val objects = testBranch.objects().asSequence().toList()
        assert(objects.size == pathAndData.size)
        for (obj in objects)
            assert(obj.path in pathAndData)

        val expected = listOf("a", "b", "bar/", "c", "foo/")
        var i = 0
        for (obj in testBranch.objects(delimiter = "/")) {
            i += 1
            assert(obj.path in expected)
        }
        assert(i == expected.size)
    }
}