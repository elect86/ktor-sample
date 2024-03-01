package lakefs

import kotlin.test.Test

class TestTag : Common() {

    /** Ensure tags can only be created in repo context */
    @Test
    fun testTagCreation() {
        val client = testClient
        val repo = Repo("test_repo", client)
        val tag = repo tag "test_tag"
        assert(tag.repoId == "test_repo")
        assert(tag.id == "test_tag")
    }
}