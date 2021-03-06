package io.ducommun.mariokart.teamsapi

import io.ducommun.mariokart.teamsapi.FileType.*
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import java.io.File

@RestController
class UploadAssetController {

    val cache = false

    @GetMapping(value = "/upload.css")
    fun uploadCss(): Mono<String> = CSS.retrieve

    @GetMapping(value = "/upload.js")
    fun uploadJs(): Mono<String> = JS.retrieve

    @GetMapping(value = "/upload")
    fun uploadHtml(): Mono<String> = HTML.retrieve

    private val FileType.retrieve: Mono<String>
        get() = if (cache) fileCache[this] ?: file.apply { fileCache[this@retrieve] = this } else file

    private val FileType.file
        get() = String(File("src/main/resources/upload.${name.toLowerCase()}").readBytes()).toMono()

    private val fileCache = FileCache()
}