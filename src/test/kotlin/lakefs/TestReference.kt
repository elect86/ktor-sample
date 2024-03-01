package lakefs

import io.lakefs.clients.sdk.model.Commit
import io.lakefs.clients.sdk.model.CommitList
import io.lakefs.clients.sdk.model.Diff
import io.lakefs.clients.sdk.model.DiffList
import io.lakefs.clients.sdk.model.Pagination
import kotlin.test.Test

class TestReference : Common() {

    val testRef: Reference
        get() {
            val client = testClient
            val repo = Repo("test_repo", client)
            return repo ref "test_reference"
        }


//    fun testReferenceCreation() {
//        val ref = testRef
//        assert(ref.repoId == "test_repo")
//        assert(ref.id == "test_reference")
//    }

    @Test
    fun testReferenceLog() {
        val ref = testRef
        var idx = 0
        val pages = 10
        val itemsPerPage = 100

        ref.monkeyLogCommits = { _, _ ->
            val results = List(itemsPerPage) {
                val index = itemsPerPage * idx + it
                Commit()
                    .id(index.toString())
                    .parents(listOf(""))
                    .committer("Committer-$index")
                    .message("Message-$index")
                    .creationDate(index.toLong())
                    .metaRangeId("")
            }
            idx += 1
            val pagination = Pagination()
                .hasMore(idx < pages)
                .nextOffset("")
                .maxPerPage(itemsPerPage)
                .results(itemsPerPage)

            CommitList()
                .pagination(pagination)
                .results(results)
        }

        var i = 0
        // Test log entire history
        for ((index, c) in ref.log().withIndex()) {
            i = index
            assert(index == c.id.toInt())
        }

        assert(i + 1 == pages * itemsPerPage)

        // Test log with limit
        idx = 0
        var maxAmount = 123
        assert(ref.log(maxAmount).asSequence().count() == maxAmount)

        // Test limit more than amount
        idx = 0
        maxAmount = pages * itemsPerPage * 2
        assert(ref.log(maxAmount).asSequence().count() == pages * itemsPerPage)
    }

    @Test
    fun testReferenceDiff() {
        val ref = testRef
        var idx = 0
        val pages = 10
        val itemsPerPage = 100

        ref.monkeyDiffRefs = { _, _, _ ->
            val results = List(itemsPerPage) {
                val index = itemsPerPage * idx + it
                Diff()
                    .type(Diff.TypeEnum.ADDED)
                    .path(index.toString())
                    .pathType(Diff.PathTypeEnum.OBJECT)
                    .sizeBytes(index.toLong())
            }
            idx += 1
            val pagination = Pagination()
                .hasMore(idx < pages)
                .nextOffset("")
                .maxPerPage(itemsPerPage)
                .results(itemsPerPage)

            DiffList()
                .pagination(pagination)
                .results(results)
        }

        // Test log entire history
        var i = 0
        for ((index, c) in ref.diff("other_ref").withIndex()) {
            i = index
            assert(i.toLong() == c.sizeBytes!!)
            assert(i == c.path.toInt())
        }

        assert(i + 1 == pages * itemsPerPage)

        // Test log with limit
        idx = 0
        var maxAmount = 123
        assert(ref.diff("other_ref", maxAmount).asSequence().count() == maxAmount)

        // Test limit more than amount
        idx = 0
        maxAmount = pages * itemsPerPage * 2
        assert(ref.diff("other_ref", maxAmount).asSequence().count() == pages * itemsPerPage)
    }
}