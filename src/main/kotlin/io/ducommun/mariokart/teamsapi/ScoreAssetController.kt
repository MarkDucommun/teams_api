package io.ducommun.mariokart.teamsapi

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import java.io.File

@RestController
class ScoreAssetController {

    val cache = false

    @GetMapping(value = "/score.css")
    fun css(): Mono<String> = FileType.CSS.retrieve

    @GetMapping(value = "/score.js")
    fun js(): Mono<String> = FileType.JS.retrieve

    @GetMapping(value = "/score")
    fun html(): Mono<String> = FileType.HTML.retrieve

    private val FileType.retrieve: Mono<String>
        get() = if (cache) fileCache[this] ?: file.apply { fileCache[this@retrieve] = this } else file

    private val FileType.file
        get() = String(File("src/main/resources/score.${name.toLowerCase()}").readBytes()).toMono()

    private val fileCache = FileCache()
}