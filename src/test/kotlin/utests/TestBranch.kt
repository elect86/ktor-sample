package utests

import io.lakefs.clients.sdk.model.Commit
import io.lakefs.clients.sdk.model.CommitCreation
import io.lakefs.clients.sdk.model.Ref
import io.lakefs.clients.sdk.model.RevertCreation
import lakefs.ConflictException
import lakefs.Repo
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test

class TestBranch : Common() {

    @Test
    // Ensure branches can only be created in repo context
    fun testBranchCreation() {
        val repo = Repo("test_repo", testClient)
        val branch = repo branch "test_branch"
        assert(branch.repoId == "test_repo")
        assert(branch.id == "test_branch")
    }

    @Test
    fun testBranchCreate() {

        val branch = testBranch
        val source = "main"

        branch.monkeyCreateBranch = { repoName, branchCreation ->
            assert(repoName == branch.repoId)
            assert(branchCreation.name == branch.id)
            assert(branchCreation.source == source)
        }

        branch.create(source)
    }

//    @Test
    fun testBranchCreateAlreadyExists() {
        val branch = testBranch
        val source = "main"

        branch.monkeyCreateBranch = { _, _ -> throw ConflictException() }

        // Expect success when exist_ok = True
        val res = branch.create(source, existOk = true)
        assert(res.id == branch.id)
        assert(res.repoId == branch.repoId)

        // Expect fail on exists
        assertThrows<ConflictException> { branch.create(source) }
    }

    @Test
    fun testBranchHead() {
        val branch = testBranch
        val commitId = "1234"

        branch.monkeyGetBranch = { repoName: String, branchName: String ->
            assert(repoName == branch.repoId)
            assert(branchName == branch.id)
            Ref().commitId(commitId).id(branch.id)
        }
        val res = branch.head
        assert(res.id == commitId)
        assert(res.repoId == branch.repoId)
    }


    @Test
    fun testBranchCommit() {
        val branch = testBranch
        val md = mapOf("key" to "value")
        val commitId = "1234"
        val commitMessage = "test message"

        branch.monkeyCommit = { repoName: String, branchName: String, commitCreation: CommitCreation ->
            assert(repoName == branch.repoId)
            assert(branchName == branch.id)
            assert(commitCreation.message == commitMessage)
            assert(commitCreation.metadata == md)
            assert(commitCreation.allowEmpty!!)
            Commit()
                .id(commitId)
                .parents(listOf(""))
                .committer("Committer")
                .message(commitMessage)
                .creationDate(123)
                .metaRangeId("")
        }
        val res = branch.commit(commitMessage, metadata = md, allowEmpty = true/*, "ignoredField" to "test"*/)
        assert(res.id == commitId)
    }

    @Test
    fun testBranchDelete() {
        val branch = testBranch

        branch.monkeyDeleteBranch = { repoName: String, branchName: String ->
            assert(repoName == branch.repoId)
            assert(branchName == branch.id)
        }
        branch.delete()
    }

    @Test
    fun testBranchRevert() {
        val branch = testBranch
        val refId = "ab1234"
        var expectedParent = 0

        // Test default parent number
        branch.monkeyRevertBranch = { repoName: String, branchName: String, revertBranchCreation: RevertCreation ->
            assert(repoName == branch.repoId)
            assert(branchName == branch.id)
            assert(revertBranchCreation.ref == refId)
            assert(revertBranchCreation.parentNumber == expectedParent) // default value
        }
        branch.monkeyGetCommit = { repoName: String, refName: String ->
            assert(repoName == branch.repoId)
            assert(refName == branch.id)
            Commit()
                .id(refId)
                .parents(listOf(""))
                .committer("Committer")
                .message("Message")
                .creationDate(0)
                .metaRangeId("")
        }
        branch.revert(refId)
        expectedParent = 2
        // Test set parent number
        branch.revert(refId, 2)

        // Test set invalid parent number
        assertThrows<IllegalArgumentException> { branch.revert(refId, -1) }

        expectedParent = 0
        // reference_id passed, but not reference
        //        with pytest . warns (DeprecationWarning, match = "reference_id is deprecated.*"):
        //        branch.revert(None, reference_id = refId)

        // neither reference nor reference_id passed
        //        with pytest . raises (ValueError, match = ".* must be specified"):
        //        branch.revert(None)

        // both are passed, prefer ``reference_id``
        //        with pytest . warns (DeprecationWarning, match = "reference_id is deprecated.*"):
        //        # this is not a high-quality test, but it would throw if the revert API
        //        # was called with reference "hello" due to the monkey - patching above
        //        # always returning "ab1234" as ref ID .
        //        c = branch.revert(refId, reference_id = "hello")
        //        assert c . id == ref_id
    }
}