package com.example

import io.lakefs.clients.sdk.BranchesApi
import io.lakefs.clients.sdk.InternalApi
import io.lakefs.clients.sdk.ObjectsApi
import io.lakefs.clients.sdk.RepositoriesApi
import lakefs.branchesApi
import lakefs.defaulApiClient
import lakefs.repo
import lakefs.repos

fun main() {
    val defaultClient = defaulApiClient {
//        val ip = "192.168.11.104"
        val ip = "localhost"
        basePath = "http://$ip:8000/api/v1"
        // Configure HTTP basic authorization: basic_auth
        basicAuth {
            username = "AKIAIOSFOLQUICKSTART"
            password = "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY"
//            username = "AKIAJO5KUOU6ZQUPWGCQ"
//            password = "KzHyu4+T/ayjwRezQhJkpcbOmaDDl1AZVhKa4s4/"
        }

//                println(repos)
//        for (repo in repos) {
//            println(repo.id)
//            val tagsIDs = repo.tags.map { it.id }
//        }
//
//        val repo = repo("example-repo")
//            .create(storageNamespace = "local://home/elect/Downloads/lakefs",
//                    existOk = true)

//        val branch = repo("example-repo3").main
//
//        repo.branch("dev").create(sourceReference = "main")
//        // could also be a commit ID or a tag
//
//        //list all branches in the repository
//        for (b in repo.branches)
//            print(branch.id)
//
//        // same idea for tags
//        for (tag in repo.tags)
//            print(tag.id)
//
//        // we can also pass optional query parameters when listing:
//        for (tag in repo.tags(prefix = "dev-"))
//            print(tag.id)
//
//        // or with a list comprehension
//        val tagIDs = repo.tags.map { it.id }
//
//        // Read the latest 10 commits
//        for (commit in branch.log(maxAmount = 10))
//            print(commit.message)
//
//        // Passing a delimiter will return lakefs.CommonPrefix and lakefs.ObjectInfo objects
//        for (entry in branch.objects(delimiter = "/", prefix = "my_directory/"))
//            print(entry.path)
//
//        // to list recursively, omit the delimiter.
//        // Listing will return only lakefs . ObjectInfo objects
//        for (obj in branch.objects(maxAmount = 100))
//            print("f'${obj.path} (size: ${obj.sizeBytes})'")
//
//        // let's calculate the size of a directory!
//        val totalBytes = branch.objects(prefix = "my_directory/").asSequence().sumOf { it.sizeBytes!! }
//
//        branch.`object`("data/example.yaml")
//        repo.tag("v1").storedObject("")
//
//        // read from a tag
//        val tag = repo("example-repo").tag("v1")
//        tag.`object`("data/example.txt").readText()
//
//        // ...or a commit:
//        val commit = repo("example-repo").commit("abc123")
//        commit.`object`("data/example.txt").readText()
////        defaultApiClient.branchesApi.cre
    }

    val api = BranchesApi(defaultClient)
    val diffList = api.diffBranch("test", "main").execute()
    diffList.results.forEach { println(it.path) }

//    val api = RepositoriesApi(defaultClient)
//    val repositoryList = api.listRepositories().execute()
//    println(repositoryList.toJson())
//    //    println(repositoryList)
//
//    println(InternalApi(defaultClient).lakeFSVersion.execute().version)
//
//    val objectsApi = ObjectsApi(defaultClient)
//    val objectStatsList = objectsApi.listObjects("quickstart", "main").execute()
//    println(objectStatsList)
//    val readme = objectStatsList.results.first { it.path == "README.md" }
//    val readme2 = objectsApi.getObject("quickstart", "main", "README.md").execute()
//    //    println(Path(readme.physicalAddress.substringAfter("local://")).readText())
//    //    println(readme2.readText())
}