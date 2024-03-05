package utests

import io.lakefs.clients.sdk.ApiException
import io.lakefs.clients.sdk.model.Repository
import io.lakefs.clients.sdk.model.RepositoryCreation
import lakefs.ConflictException
import lakefs.NotAuthorizedException
import lakefs.NotFoundException
import lakefs.RepositoryProperties
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test

class TestRepo : Common() {

    @Test
    fun testRepositoryCreation() {

        val repo = testRepo

        repo.monkeyCreateRepository = { repositoryCreation: RepositoryCreation ->
            assert(repositoryCreation.name == TEST_REPO.name)
            assert(repositoryCreation.storageNamespace == TEST_REPO.storageNamespace)
            assert(repositoryCreation.defaultBranch == TEST_REPO.defaultBranch)
            assert(repositoryCreation.sampleData == TEST_REPO.sampleData)
            Repository()
                .id(TEST_REPO.name)
                .creationDate(System.currentTimeMillis())
                .storageNamespace(TEST_REPO.storageNamespace)
                .defaultBranch(TEST_REPO.defaultBranch)
        }
        repo.create(storageNamespace = TEST_REPO.storageNamespace,
                    defaultBranch = TEST_REPO.defaultBranch!!,
                    includeSamples = TEST_REPO.sampleData!!)
    }

    @Test
    fun testRepositoryCreationAlreadyExists() {
        val repo = testRepo
        //        ex = lakefs_sdk.exceptions.ApiException(status = http.HTTPStatus.CONFLICT.value)

        repo.monkeyCreateRepository = { throw ConflictException() }

        // Expect success when exist_ok = True
        val existing = Repository()
            .id(TEST_REPO.name)
            .defaultBranch("main")
            .storageNamespace("s3://existing-namespace")
            .creationDate(12345)

        repo.monkeyGetRepository = { existing }

        val res = repo.create(storageNamespace = TEST_REPO.storageNamespace,
                              defaultBranch = TEST_REPO.defaultBranch!!,
                              includeSamples = TEST_REPO.sampleData!!,
                              existOk = true)

        assert(res.properties == RepositoryProperties(existing))

        // Expect fail on exists
        assertThrows<ConflictException> {
            repo.create(storageNamespace = TEST_REPO.storageNamespace,
                        defaultBranch = TEST_REPO.defaultBranch!!,
                        includeSamples = TEST_REPO.sampleData!!)
        }

        // Expect fail on exists
        repo.monkeyCreateRepository = { throw NotAuthorizedException() }
        assertThrows<NotAuthorizedException> {
            repo.create(storageNamespace = TEST_REPO.storageNamespace,
                        defaultBranch = TEST_REPO.defaultBranch!!,
                        includeSamples = TEST_REPO.sampleData!!)
        }
    }

    @Test
    fun testDeleteRepository() {

        val repo = testRepo
        repo.monkeyDeleteRepository = { }
        repo.delete()

        // Not found
        repo.monkeyDeleteRepository = { throw NotFoundException() }
        assertThrows<NotFoundException> { repo.delete() }

        // Unauthorized
        repo.monkeyDeleteRepository = { throw NotAuthorizedException() }
        assertThrows<NotAuthorizedException> { repo.delete() }

        // Other error
        repo.monkeyDeleteRepository = { throw ApiException() }
        assertThrows<ApiException> { repo.delete() }
    }

    //    def test_create_repository_no_authentication(monkeypatch):
    //    with lakectl_no_config_context(monkeypatch):
    //    from lakefs.repository import Repository
    //    repo = Repository("test-repo", None)
    //    with expect_exception_context(NoAuthenticationFound):
    //    repo.create(storage_namespace=TEST_REPO_ARGS.storage_namespace)
    //
    //    # update credentials and retry create
    //    monkeypatch.setattr(lakefs_sdk.RepositoriesApi, "create_repository", monkey_create_repository)
    //    with env_var_context():
    //    from lakefs import config as client_config
    //    # Create a new client with env vars
    //    os.environ[client_config._LAKECTL_ENDPOINT_ENV] = "endpoint"
    //    os.environ[client_config._LAKECTL_SECRET_ACCESS_KEY_ENV] = "secret"
    //    os.environ[client_config._LAKECTL_ACCESS_KEY_ID_ENV] = "key"
    //    repo.create(storage_namespace=TEST_REPO_ARGS.storage_namespace,
    //    default_branch=TEST_REPO_ARGS.default_branch)
}