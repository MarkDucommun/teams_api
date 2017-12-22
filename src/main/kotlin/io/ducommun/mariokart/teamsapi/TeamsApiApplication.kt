package io.ducommun.mariokart.teamsapi

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.GetObjectRequest
import com.amazonaws.services.s3.model.ObjectMetadata
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling
import java.io.File

@SpringBootApplication
@EnableScheduling
class TeamsApiApplication

fun main(args: Array<String>) {
    runApplication<TeamsApiApplication>(*args)
}

val mapper = ObjectMapper().registerKotlinModule()

val databasePath = "src/main/resources/temp-database"

fun <T> AmazonS3.retrieve(name: String, csvToData: List<String>.() -> T): List<T> {

    val getObjectRequest = GetObjectRequest("mariokart-database", name)

    val file = File("$databasePath/$name-${System.currentTimeMillis()}.csv")

    getObject(getObjectRequest, file)

    return file
        .readLines()
        .map { it.split(",") }
        .filterNot { it.isEmpty() }
        .map(csvToData)
        .apply { file.delete() }
}

fun <T> AmazonS3.overwrite(name: String, list: List<T>, dataToCsv: T.() -> String) {

    val byteInputStream = list.joinToString("\n", transform = dataToCsv).byteInputStream()

    putObject("mariokart-database", name, byteInputStream, ObjectMetadata())
}

val <T> T.asJson: String get() = mapper.writeValueAsString(this)