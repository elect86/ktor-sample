package com.example

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.lakefs.clients.sdk.BranchesApi
import io.lakefs.clients.sdk.Configuration
import io.lakefs.clients.sdk.auth.HttpBasicAuth
import kotlinx.serialization.json.Json


fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}


fun Application.module() {
    configureRouting()
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
        })
    }
}

/*{
 "event_type":"pre-commit"
 "event_time":"2024-02-15T08:31:53Z"
 "action_name":"Dataset"
 "hook_id":"dataset_validator"
 "repository_id":"quickstart"
 "branch_id":"main"
 "source_ref":"main"
 "commit_message":"a"
 "committer":"quickstart"
 }*/

fun Application.configureRouting() {
    routing {
        post("/webhooks/format") {
            //            println(call.receiveText())
            //            val schema = call.bodySchema()
            //            println(schema.eventType)
            //            println(schema.eventTime)
            //            println(schema.actionName)
            //            println(schema.hookId)
            //            println(schema.repositoryId)
            //            println(schema.branchId)
            //            println(schema.sourceRef)
            //            println(schema.commitMessage)
            //            println(schema.committer)
            //            println(schema.metadata)
            //            println(schema.tagId)

            val schema = call.receive<BodySchema>()
            println(schema)
            call.respondText("ciao")
            //            val apiClient = ApiClient().apply {
            //                setUsername("AKIAIOSFOLQUICKSTART")
            //                setPassword("wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY")
            //            }

            val defaultClient = Configuration.getDefaultApiClient()
            defaultClient.setBasePath("http://localhost:8000/api/v1")

            // Configure HTTP basic authorization: basic_auth
            val basic_auth = defaultClient.getAuthentication("basic_auth") as HttpBasicAuth
            basic_auth.username = "AKIAIOSFOLQUICKSTART"
            basic_auth.password = "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY"


            // Configure API key authorization: cookie_auth
            //            val cookie_auth = defaultClient.getAuthentication("cookie_auth") as ApiKeyAuth
            //            cookie_auth.apiKey = "YOUR API KEY"
            //
            //
            //            // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
            //            //cookie_auth.setApiKeyPrefix("Token");
            //
            //            // Configure API key authorization: oidc_auth
            //            val oidc_auth = defaultClient.getAuthentication("oidc_auth") as ApiKeyAuth
            //            oidc_auth.apiKey = "YOUR API KEY"
            //
            //
            //            // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
            //            //oidc_auth.setApiKeyPrefix("Token");
            //
            //            // Configure API key authorization: saml_auth
            //            val saml_auth = defaultClient.getAuthentication("saml_auth") as ApiKeyAuth
            //            saml_auth.apiKey = "YOUR API KEY"
            //
            //
            //            // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
            //            //saml_auth.setApiKeyPrefix("Token");
            //
            //            // Configure HTTP bearer authorization: jwt_token
            //            val jwt_token = defaultClient.getAuthentication("jwt_token") as HttpBearerAuth
            //            jwt_token.bearerToken = "BEARER TOKEN"

            val api = BranchesApi(defaultClient)
//            val diffList = api.diffBranch(schema.repositoryId, schema.branchId).execute()
//            println(diffList)
//            println(diffList.toJson())
//            println(diffList.results)
//            println(diffList.pagination)
//            println(diffList.additionalProperties)
            //            println(request.execute().results)

            //            val apiInstance = ActionsApi(defaultClient)
            //            val repository = "quickstart" // String |
            //            val runId = "runId_example" // String |
            //            try {
            //                val result = apiInstance.getRun(repository, runId)
            //                    .execute()
            //                println(result)
            //            } catch (e: ApiException) {
            //                System.err.println("Exception when calling ActionsApi#getRun")
            //                System.err.println("Status code: " + e.code)
            //                System.err.println("Reason: " + e.responseBody)
            //                System.err.println("Response headers: " + e.responseHeaders)
            //                e.printStackTrace()
            //            }
        }
    }
}
