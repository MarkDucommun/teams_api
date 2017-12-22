package io.ducommun.mariokart.teamsapi

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import java.io.File

@RestController
class LeaderboardAssetController {

    val cache = false

    @GetMapping(value = "/leaderboard.css")
    fun css(): Mono<String> = FileType.CSS.retrieve

    @GetMapping(value = "/leaderboard.js")
    fun js(): Mono<String> = FileType.JS.retrieve

    @GetMapping(value = "/leaderboard")
    fun html(): Mono<String> = FileType.HTML.retrieve

    @GetMapping(value = "/noto.ttf")
    fun noto(): Mono<String> = String(File("src/main/resources/NotoMono-Regular.ttf").readBytes()).toMono()

    @GetMapping(value = "/leaderboard.json", produces = arrayOf("application/json"))
    fun leaderboard(): Mono<String> {
        val scores = scores
        val players = players
        val races = races

        return scores
            .sortedByDescending { it.value }
            .mapNotNull { score -> races.find { it.id == score.raceId }?.to(score) }
            .mapIndexed { rank, (race, score) ->
                mapOf("race" to mapOf(
                    "rank" to rank + 1,
                    "score"  to score.value,
                    "players" to race.playerIds.mapNotNull { playerId -> players.find { it.id == playerId }?.name?.split(" ")?.run { "${first()} ${last().first()}" } }.sorted().joinToString(",\n")
                ))
            }.asJson.toMono()
    }

    private val FileType.retrieve: Mono<String>
        get() = if (cache) fileCache[this] ?: file.apply { fileCache[this@retrieve] = this } else file

    private val FileType.file
        get() = String(File("src/main/resources/leaderboard.${name.toLowerCase()}").readBytes()).toMono()

    private val fileCache = FileCache()
}