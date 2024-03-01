package lakefs

import io.lakefs.clients.sdk.model.RepositoryCreation
import org.junit.jupiter.api.io.TempDir
import java.io.File

open class Common {

    @field:TempDir
    lateinit var tempFolder: File

    val testClient = defaulApiClient {
        basePath = "http://localhost:8000/api/v1"
        basicAuth {
            username = "AKIAIOSFOLQUICKSTART"
            password = "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY"
        }
    }

    val TEST_REPO
        get() = RepositoryCreation()
            .name("test-repo")
            .storageNamespace("local:/$tempFolder")
            .defaultBranch("default-branch")
            .sampleData(true)

    val testRepo
        get() = Repo(TEST_REPO.name, testClient)
    val testBranch
        get() = testRepo branch "test_branch"
}