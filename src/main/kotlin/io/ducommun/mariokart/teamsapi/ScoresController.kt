package io.ducommun.mariokart.teamsapi

import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono

@RestController
class ScoresController {

    @PostMapping(value = "/scores")
    fun create(@RequestParam raceId: Int, @RequestParam score: Int) {

        scores.run {

            val lastId = sortedByDescending { it.id }.firstOrNull()?.id ?: 0

            plus(
                Score(
                    id = lastId + 1,
                    raceId = raceId,
                    value = score
                )
            ).overwriteScores()

            races.markHasScore(raceId)
        }
    }

    @PostMapping(value = "/scores/{id}/delete")
    fun delete(@PathVariable id: Int) {
        scores.filterNot { it.id == id }.overwriteScores()
    }

    @GetMapping(value = "/scores", produces = arrayOf("application/json"))
    fun scores(): Mono<String> = scores.asJson.toMono()

    @PostMapping(value = "/scores/{id}/update")
    fun update(
        @PathVariable id: Int,
        @RequestParam raceId: Int? = null,
        @RequestParam("score") value: Int? = null
    ) {

        scores.run {

            val score = find { it.id == id } ?: return

            val updatedScore = score.copy(
                raceId = raceId ?: score.raceId,
                value = value ?: score.value
            )

            updateScore(updatedScore)
        }
    }
}