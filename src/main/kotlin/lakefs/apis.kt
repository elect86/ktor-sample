package lakefs

import io.lakefs.clients.sdk.*

// APIs
val ApiClient.actionsApi: ActionsApi
    get() = ActionsApi(this)
val ApiClient.authApi: AuthApi
    get() = AuthApi(this)
val ApiClient.branchesApi: BranchesApi
    get() = BranchesApi(this)
val ApiClient.commitsApi: CommitsApi
    get() = CommitsApi(this)
val ApiClient.configApi: ConfigApi
    get() = ConfigApi(this)
val ApiClient.experimentalApi: ExperimentalApi
    get() = ExperimentalApi(this)
val ApiClient.healthCheckApi: HealthCheckApi
    get() = HealthCheckApi(this)
val ApiClient.importApi: ImportApi
    get() = ImportApi(this)
val ApiClient.internalApi: InternalApi
    get() = InternalApi(this)
val ApiClient.metadataApi: MetadataApi
    get() = MetadataApi(this)
val ApiClient.objectsApi: ObjectsApi
    get() = ObjectsApi(this)
val ApiClient.refsApi: RefsApi
    get() = RefsApi(this)
val ApiClient.repositoriesApi: RepositoriesApi
    get() = RepositoriesApi(this)
val ApiClient.stagingApi: StagingApi
    get() = StagingApi(this)
val ApiClient.tagsApi: TagsApi
    get() = TagsApi(this)