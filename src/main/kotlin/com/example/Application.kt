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
import io.lakefs.clients.sdk.model.Diff
import lakefs.objectsApi
import net.pwall.json.schema.JSONSchema


// change port
fun main() {
    embeddedServer(Netty, port = 8080, host = "127.0.0.1", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureRouting()
    install(ContentNegotiation) {
        json()
    }
}

fun Application.configureRouting() {
    routing {
        post("/webhooks/format") {

            val DATASETjson = "DATASET.json"

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

            val defaultClient = Configuration.getDefaultApiClient()
            defaultClient.setBasePath("http://localhost:8000/api/v1")

            // Configure HTTP basic authorization: basic_auth
            val basic_auth = defaultClient.getAuthentication("basic_auth") as HttpBasicAuth
            basic_auth.username = "AKIAIOSFOLQUICKSTART"
            basic_auth.password = "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY"

            val api = BranchesApi(defaultClient)
            val diffList = api.diffBranch(bodySchema.repositoryId, bodySchema.branchId).execute()
            diffList.results.forEach { println(it.path) }

            val files = diffList.results.filter { it.type == Diff.TypeEnum.ADDED }.map { it.path }
            fun check(files: List<String>) {
//                println("[check] $files")
                val (dirs, normalFiles) = files.partition { '/' in it }
                if (DATASETjson in normalFiles)
                    return
                if (dirs.isNotEmpty())
                    check(dirs.map { it.substringAfter('/') })
            }

            check(files)
            val datasetDiffs = diffList.results.filter {
                it.type == Diff.TypeEnum.ADDED && it.path.substringAfterLast('/') == DATASETjson
            }
            if (datasetDiffs.size > 1) {
                call.respond(io.ktor.http.HttpStatusCode.NotAcceptable, "multiple $DATASETjson found\n")
                return@post
            }
            val datasetDiff = datasetDiffs.firstOrNull()
            if (datasetDiff == null) {
                call.respond(io.ktor.http.HttpStatusCode.NotFound, "$DATASETjson not found\n")
                return@post
            }

            val schemaFile = defaultClient.objectsApi.getObject(bodySchema.repositoryId, bodySchema.branchId, "SCHEMA.json").execute()
            val schema = JSONSchema.parse(schemaFile)
            val datasetFile = defaultClient.objectsApi.getObject(bodySchema.repositoryId, bodySchema.branchId, datasetDiff.path).execute()
            val validation = schema.validate(datasetFile.readText())

            if (!validation)
                call.respond(io.ktor.http.HttpStatusCode.NotAcceptable, "$DATASETjson is not conformant\n")
            else
                call.respond(io.ktor.http.HttpStatusCode.OK, "$DATASETjson found and conformant\n")
        }
    }
}