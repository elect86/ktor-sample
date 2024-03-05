package integration

import lakefs.Branch
import lakefs.Repo
import lakefs.defaulApiClient
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.net.URLEncoder
import java.util.UUID

open class ConfTest {

    @field:TempDir
    lateinit var tempFolder: File

    val newTempFile
        get() = File(tempFolder, System.currentTimeMillis().toString()).apply { createNewFile() }

    val testClient = defaulApiClient {
        basePath = "http://localhost:8000/api/v1"
        basicAuth {
            username = "AKIAIOSFOLQUICKSTART"
            password = "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY"
        }
    }

    val LAKEFS_METADATA_PREFIX = "x-lakefs-meta-"

    val LAKEFS_STORAGE_PREFIX = "local:/"

    fun getStorageNamespace(testName: String) = "$LAKEFS_STORAGE_PREFIX$tempFolder/${UUID.randomUUID()}/$testName"

    val storageNamespace by lazy { LAKEFS_STORAGE_PREFIX + tempFolder }

    fun setupRepo(nameSpace: String = storageNamespace,
                  name: String = "repo",
                  defaultBranch: String = "main"): Repo {
        val clt = testClient
        val repoName = name + System.currentTimeMillis()
        return Repo(repoName, clt).create(nameSpace, defaultBranch)
    }

    fun HashMap<String, String>.encode(): String =
        keys.stream()
            .map { key -> "$key=${this[key].orEmpty().encode()}" }
            .reduce { p1: String?, p2: String? -> "$p1&$p2" }
            .map { s -> "?$s" }
            .orElse("")

    fun String.encode() = URLEncoder.encode(this, Charsets.UTF_8)

    //    @pytest.fixture(scope="session")
    fun setupBranchWithCommits(): Branch {
        val repo = setupRepo(getStorageNamespace("branch-with-commits"),
                             "branch-with-commits")
        val branch = repo.branch("test_branch").create("main")
        val commitNum = 199
        for (i in 0..<commitNum) {
            val obj = branch.storedObject("test1")
            if (i % 2 == 0)
                obj.upload("test_data")
            else
                obj.delete()
            branch.commit("commit ${commitNum - i - 1}")
        }
        return branch
    }
}