package com.example

import net.pwall.json.schema.JSONSchema
import java.io.File

fun main() {
    val dataset = File("/home/elect/Downloads/DATASET.json").readText()

    val schemaFile = File("/home/elect/Downloads/SCHEMA.json")
    val schema = JSONSchema.parse(schemaFile)
    println(schema.validate(dataset))
    val output = schema.validateBasic(dataset)
    output.errors?.forEach {
        println("${it.error}, ${it.instanceLocation}, ${it.keywordLocation}, ${it.absoluteKeywordLocation}")
    }
}