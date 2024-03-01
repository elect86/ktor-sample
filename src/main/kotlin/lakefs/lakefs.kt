package lakefs

import io.lakefs.clients.sdk.*
import io.lakefs.clients.sdk.auth.ApiKeyAuth
import io.lakefs.clients.sdk.auth.HttpBasicAuth
import io.lakefs.clients.sdk.auth.HttpBearerAuth
import io.lakefs.clients.sdk.model.Repository

object lakeFS {

    //    val repositories
    //        get() {
    //
    //        }
}

fun defaulApiClient(block: ScopedClient.() -> Unit): ApiClient = ScopedClient().apply(block).defaultApiClient

class ScopedClient {

    val defaultApiClient = ApiClient()

    var basePath: String = ""
        set(value) {
            defaultApiClient.basePath = value
            field = value
        }

    // Configure HTTP basic authorization: basic_auth
    fun basicAuth(block: HttpBasicAuth.() -> Unit) {
        val basicAuth = defaultApiClient.getAuthentication("basic_auth") as HttpBasicAuth
        basicAuth.block()
    }

    // Configure API key authorization: cookie_auth
    fun cookieAuth(block: ApiKeyAuth.() -> Unit) {
        val cookieAuth = defaultApiClient.getAuthentication("cookie_auth") as ApiKeyAuth
        cookieAuth.block()
    }

    // Configure API key authorization: oidc_auth
    fun oidcAuth(block: ApiKeyAuth.() -> Unit) {
        val oidcAuth = defaultApiClient.getAuthentication("oidc_auth") as ApiKeyAuth
        oidcAuth.block()
    }

    // Configure API key authorization: saml_auth
    fun samlAuth(block: ApiKeyAuth.() -> Unit) {
        val samlAuth = defaultApiClient.getAuthentication("saml_auth") as ApiKeyAuth
        samlAuth.block()
    }

    // Configure HTTP bearer authorization: jwt_token
    fun jwtToken(block: HttpBearerAuth.() -> Unit) {
        val jwtToken = defaultApiClient.getAuthentication("jwt_token") as HttpBearerAuth
        jwtToken.block()
    }

}

val ApiClient.repos: List<Repo>
    get() = repos()

fun ApiClient.repos(prefix: String? = null, after: String? = null, amount: Int? = null): List<Repo> =
    repositoriesApi.listRepositories()
        .prefix(prefix)
        .after(after)
        .amount(amount)
        .execute().results.map { Repo(it.id, this) }

fun ApiClient.repo(id: String) = Repo(id, this)

//class RepositoryProperties(LenientNamedTuple):
//    """
//    Represent a lakeFS repository's properties
//    """
//id: str
//creation_date: int
//default_branch: str
//storage_namespace: str


fun gen() = iterator {
    var x = 0
    while (true) {
        yield(x)
        x++
    }
}

fun demo() {
    val numbers = gen()
    repeat(3) {
        val nextNumber = numbers.next()
        println(nextNumber)
    }
}

fun main() {
    demo()
    println()
    demo()
}