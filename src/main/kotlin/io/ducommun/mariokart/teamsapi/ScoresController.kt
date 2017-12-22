package io.ducommun.mariokart.teamsapi

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class ScoresController {

    @PostMapping(value = "/scores")
    fun create(@RequestParam raceId: Int, @RequestParam score: Int) {

    }
}