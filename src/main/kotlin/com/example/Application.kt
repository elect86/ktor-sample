package com.example

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.lakefs.clients.sdk.ApiException
import io.lakefs.clients.sdk.BranchesApi
import io.lakefs.clients.sdk.Configuration
import io.lakefs.clients.sdk.auth.HttpBasicAuth
import io.lakefs.clients.sdk.model.Diff
import lakefs.objectsApi
import net.pwall.json.schema.JSONSchema


// change port
fun main() {
    embeddedServer(Netty, port = 8082, host = "127.0.0.1", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureRouting()
    install(ContentNegotiation) {
        json()
    }
}

val ListEntriesLimitMax = 1_000

fun Application.configureRouting() {
    routing {
        post("/webhooks/format") {

            val DATASETjson = "DATASET.json"
            val SCHEMAjson = "SCHEMA.json"

            /*
            BodySchema
            {
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

            val bodySchema = call.receive<BodySchema>()
            //            println(bodySchema)

            if (bodySchema.commitMetadata?.get("disable") == "1") {
                call.respond(io.ktor.http.HttpStatusCode.OK, "no checks\n")
                return@post
            }
            val repo = bodySchema.repositoryId
            val branch = bodySchema.branchId

            val defaultClient = Configuration.getDefaultApiClient()
            defaultClient.setBasePath("http://localhost:8000/api/v1")

            // Configure HTTP basic authorization: basic_auth
            val basic_auth = defaultClient.getAuthentication("basic_auth") as HttpBasicAuth
            basic_auth.username = "AKIAIOSFOLQUICKSTART"
            basic_auth.password = "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY"

            val api = BranchesApi(defaultClient)
            val diffList = api.diffBranch(repo, branch).amount(ListEntriesLimitMax).execute()
            diffList.results.forEach { println(it.path) }

            val schemaFile =
                try {
                    defaultClient.objectsApi.getObject(repo, branch, SCHEMAjson).execute()
                } catch (e: ApiException) {
                    call.respond(io.ktor.http.HttpStatusCode.NotFound, "$SCHEMAjson not found\n")
                    return@post
                }

            val schema = JSONSchema.parse(schemaFile)

            fun check(path: String, files: List<String>) {
                //                println("==========[check]==========")
                //                for (file in files) println(file)
                val (paths, normalFiles) = files.partition { '/' in it }
                if ("DATASET.json" in normalFiles) {
                    val datasetFile = defaultClient.objectsApi.getObject(repo, branch, "$path$DATASETjson").execute()
                    if (!schema.validate(datasetFile.readText()))
                        throw NotConformantDatasetException("$path$DATASETjson is not conformant to the Schema")
                    return
                }
                if (paths.isEmpty())
                    throw NoDatasetFoundException("No $DATASETjson found in $path")
                val mapDirs = paths.groupBy({ it.substringBefore('/') + '/' }) { it.substringAfter('/') }
                //                println("=====[dirs]=====")
                //                println(mapDirs)
                for ((dir, dirFiles) in mapDirs)
                    check("$path$dir", dirFiles)
            }

            // Since suspension functions (`call::respond`) can be called only within coroutine body
            // (which `check` isn't), then we will respond accordingly to the exception thrown type
            try {
                val files = diffList.results.filter { it.type == Diff.TypeEnum.ADDED }.map { it.path }
                check("", files)
            } catch (ex: NotConformantDatasetException) {
                call.respond(io.ktor.http.HttpStatusCode.NotAcceptable, "${ex.message}\n")
                return@post
            } catch (ex: NoDatasetFoundException) {
                call.respond(io.ktor.http.HttpStatusCode.NotAcceptable, "${ex.message}\n")
                return@post
            }

            // if we made so far, everything is fine
            call.respond(io.ktor.http.HttpStatusCode.OK, "$DATASETjson found and conformant\n")
        }
    }
}