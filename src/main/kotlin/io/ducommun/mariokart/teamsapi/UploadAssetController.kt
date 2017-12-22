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
        get() = String(File("src/main/resources/score.${name.toLowerCase()}").readBytes()).toMono()

    private val fileCache = FileCache()
}

@RestController
class ScoreAssetController {

    val cache = false

    @GetMapping(value = "/score.css")
    fun css(): Mono<String> = CSS.retrieve

    @GetMapping(value = "/score.js")
    fun js(): Mono<String> = JS.retrieve

    @GetMapping(value = "/score")
    fun html(): Mono<String> = HTML.retrieve

    private val FileType.retrieve: Mono<String>
        get() = if (cache) fileCache[this] ?: file.apply { fileCache[this@retrieve] = this } else file

    private val FileType.file
        get() = String(File("src/main/resources/score.${name.toLowerCase()}").readBytes()).toMono()

    private val fileCache = FileCache()
}

data class FileCache(
    private var html: Mono<String>? = null,
    private var css: Mono<String>? = null,
    private var js: Mono<String>? = null
) {
    operator fun get(type: FileType): Mono<String>? =
        when (type) {
            HTML -> html
            CSS -> css
            JS -> js
        }

    operator fun set(type: FileType, value: Mono<String>) {

        when (type) {
            HTML -> html = value
            CSS -> css = value
            JS -> js = value
        }
    }
}

enum class FileType {
    HTML, CSS, JS
}