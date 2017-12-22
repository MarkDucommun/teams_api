package io.ducommun.mariokart.teamsapi

import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono

@RestController
class RacesController {

    @GetMapping(value = "/races/last")
    fun last(): Mono<String> {
        return races.last().let { race ->
            images.find { race.imageId == it.id }?.run {
                mapOf(
                    "race" to mapOf(
                        "id" to race.id,
                        "image" to mapOf(
                            "key" to key,
                            "path" to "/photos?key=$key"
                        )
                    )
                ).asJson.toMono()
            } ?: Mono.just("")
        }
    }

    @PostMapping(value = "/races/{id}/delete")
    fun delete(@PathVariable id: Int) {
        races.filterNot { it.id == id }.overwriteRaces()
    }

    @PostMapping(value = "/races/{id}/update")
    fun update(
        @PathVariable id: Int,
        @RequestParam imageId: Int? = null,
        @RequestParam players: String? = null
    ) {

        races.run {

            val race = find { it.id == id } ?: return

            val updatedRace = race.copy(
                imageId = imageId ?: race.imageId,
                playerIds = players?.split(",")?.map { it.trim().toInt() } ?: race.playerIds
            )

            updateRace(updatedRace)
        }
    }

    @PostMapping(value = "/races")
    fun create(
        @RequestParam imageId: Int,
        @RequestParam players: String
    ) {

        races.run {

            val lastId = sortedByDescending { it.id }.firstOrNull()?.id ?: 0

            plus(
                Race(
                    id = lastId + 1,
                    imageId = imageId,
                    playerIds = players.split(",").map { it.trim().toInt() }.take(4)
                )
            ).overwriteRaces()

            images.markHasRace(imageId)
        }
    }

    @GetMapping(value = "/races", produces = arrayOf("application/json"))
    fun races(): Mono<String> = races.asJson.toMono()
}