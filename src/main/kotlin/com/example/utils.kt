@file:UseSerializers(InstantSerializer::class, EventTypeSerializer::class)

package com.example

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.time.Instant

enum class EventType {
    preCommit, postCommit,
    preMerge, postMerge,
    preCreateBranch, postCreateBranch,
    preDeleteBranch, postDeleteBranch,
    preCreateTag, postCreateTag,
    preDeleteTag, postDeleteTag;

//    val isTag: Boolean
//        get() = name.endsWith("Tag")
//    val isBranch: Boolean
//        get() = name.endsWith("Branch")

    companion object {
        infix fun of(string: String): EventType {
            val key = string.replace("-", "")
            return entries.first { it.name.lowercase() == key }
        }
    }
}

typealias Tuples = Map<String, String>

//suspend fun ApplicationCall.bodySchema(): BodySchema {
//    fun String.trimQuotes() = drop(1).dropLast(1)
//    fun String.tuples() = split(',').map { it.substringBefore(':').trimQuotes() to it.substringAfter(':').trimQuotes() }
//    val metaName = "\"commit_metadata\":{"
//    val tagIdName = "\"tag_id\""
//    var text = receiveText().drop(1).dropLast(1)
//    var tagId: String? = null
//    if (tagIdName in text) {
//        tagId = tagIdName + text.substringAfter(tagIdName)
//        text = text.substringBefore(tagIdName)
//    }
//    var metadata: Tuples? = null
//    if (metaName in text) {
//        metadata = text.substringAfter(metaName).dropLast(1).tuples()
//        text = text.substringBefore(metaName)
//    }
//    if (tagId != null)
//        text += "$tagIdName:$tagId"
//    return BodySchema(text.tuples(), metadata)
//}
//class BodySchema(tuples: Tuples,
//                 val metadata: Tuples?) {
//    val eventType = EventType of tuples[0].second
//    val eventTime = Instant.parse(tuples[1].second)
//    val actionName = tuples[2].second
//    val hookId = tuples[3].second
//    val repositoryId = tuples[4].second
//    val branchId: String?
//    val sourceRef: String
//    val commitMessage: String?
//    val committer: String?
//    val tagId: String?
//
//    init {
//        var i = 5
//        branchId = if (eventType.isTag) null else tuples[i++].second
//        sourceRef = tuples[i++].second
//        commitMessage = if (eventType.isBranch) null else tuples[i++].second
//        committer = if (eventType.isBranch) null else tuples[i++].second
//        tagId = if (eventType.isTag) tuples[i].second else null
//    }
//}

@Serializable
data class BodySchema(@SerialName("event_type")
                      val eventType: EventType,
                      @SerialName("event_time")
                      val eventTime: Instant,
                      @SerialName("action_name")
                      val actionName: String,
                      @SerialName("hook_id")
                      val hookId: String,
                      @SerialName("repository_id")
                      val repositoryId: String,
                      @SerialName("branch_id")
                      val branchId: String? = null,
                      @SerialName("source_ref")
                      val sourceRef: String,
                      @SerialName("commit_message")
                      val commitMessage: String? = null,
                      val committer: String?,
                      @SerialName("commit_metadata")
                      val commitMetadata: Tuples? = null,
                      @SerialName("tag_id")
                      val tagId: String? = null)

class NotConformantDatasetException : Exception {
    constructor() : super()
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
    constructor(cause: Throwable) : super(cause)
}

class NoDatasetFoundException : Exception {
    constructor() : super()
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
    constructor(cause: Throwable) : super(cause)
}
