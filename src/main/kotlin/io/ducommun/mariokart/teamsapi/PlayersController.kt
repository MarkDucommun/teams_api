package io.ducommun.mariokart.teamsapi

import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono

@RestController
class PlayersController {

    @PostMapping(value = "/players/{id}/delete")
    fun delete(@PathVariable id: Int) {
        players.filterNot { it.id == id }.overwritePlayers()
    }

    @PostMapping(value = "/players")
    fun storePlayer(@RequestParam name: String) {

        players.run {

            val lastId = sortedByDescending { it.id }.firstOrNull()?.id ?: 0

            val newPlayerList = plus(Player(id = lastId + 1, name = name))

            newPlayerList.overwritePlayers()
        }
    }

    @GetMapping(value = "/players", produces = arrayOf("application/json"))
    fun players(): Mono<String> {
        return players.asJson.toMono()
    }
}