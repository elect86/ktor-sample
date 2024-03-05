package integration

import lakefs.Repo
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import kotlin.test.Test

class TestRepository : ConfTest() {

    val NUM_PREFIXES = 10
    val NUM_ELEM_PER_PREFIX = 20

    //    @pytest.fixture(name="setup_repo_with_branches_and_tags", scope="session")
    fun setupRepoWithBranchesAndTags(): Repo {
        val repo = setupRepo(getStorageNamespace("branches-and-tags"),
                             "branches-and-tags")
        for (i in 0..<NUM_PREFIXES)
            for (j in 0..<NUM_ELEM_PER_PREFIX) {
                //                println("branches%02d-%02d".format(i, j))
                val b = repo.branch("branches%02d-%02d".format(i, j)).create("main")
                repo.tag("tags%02d-%02d".format(i, j)).create(b)
            }

        return repo
    }

    @ParameterizedTest
    @ValueSource(strings = ["branches", "tags"])
    fun testRepositoryListings(attr: String) {
        val repo = setupRepoWithBranchesAndTags()

        var total = NUM_PREFIXES * NUM_ELEM_PER_PREFIX
        val amount = when (attr) {
            "branches" -> {
                total += 1  // Including main
                repo.branches(total) // we need to overwrite `total`, because the default value is 100, too small for our case
            }
            else -> repo.tags(total)
        }.asSequence().count()

        assert(amount == total)

        val after = 9
        val res = when (attr) {
            "branches" -> repo.branches(maxAmount = 100, prefix = "${attr}01", after = "${attr}01-%02d".format(after))
            else -> repo.tags(maxAmount = 100, prefix = "${attr}01", after = "${attr}01-%02d".format(after))
        }
        assert(res.asSequence().count() == 10)
        for ((i, b) in res.withIndex())
            assert(b.id == "${attr}01-%02d".format(i + after + 1))
    }
}