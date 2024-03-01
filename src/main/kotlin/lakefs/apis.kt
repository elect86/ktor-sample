package lakefs

import io.lakefs.clients.sdk.*
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import kotlin.reflect.jvm.javaGetter

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

val topLevelClass = object{}.javaClass.enclosingClass

fun main() {
//    MethodHandles.loo
//    println(topLevelClass)
//    println(this::class ApiClient::actionsApi.getter.invoke(ApiClient()))
//    for(method in topLevelClass::class.kcl)
//    for(method in MethodHandles.lookup().lookupClass().declaredMethods) {
//        method.isAccessible = true
//        method.
//    }
//        println(method)
//    method.isAccessible = true
//    println(method.invoke(null, 9.0)) // returns 3.0
//    println(method.invoke(null, 16.0)) // returns 3.0
}