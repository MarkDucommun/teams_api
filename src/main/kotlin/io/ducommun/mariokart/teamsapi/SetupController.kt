package io.ducommun.mariokart.teamsapi

import com.amazonaws.services.s3.model.ObjectMetadata
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class SetupController {

    @GetMapping("/setup")
    fun setup() {
        val files = client.listObjects("mariokart-database").objectSummaries.map { it.key }

        listOf("images", "races", "scores", "players").forEach {

            if (!files.contains(it)) {
                client.putObject("mariokart-database", it, "".byteInputStream(), ObjectMetadata())
            }
        }
    }
}