package com.example

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import io.lakefs.clients.sdk.BranchesApi
import io.lakefs.clients.sdk.BranchesApi.APIcreateBranchRequest
import io.lakefs.clients.sdk.model.BranchCreation
import io.mockk.every
import io.mockk.mockkStatic
import kotlin.test.*

class ApplicationTest {
//    @Test
//    fun testRoot() = testApplication {
//        application {
//            configureRouting()
//        }
//        client.get("/").apply {
//            assertEquals(HttpStatusCode.OK, status)
//            assertEquals("Hello World!", bodyAsText())
//        }
//    }
}