package io.ducommun.mariokart.teamsapi


internal data class Player(
    val id: Int,
    val name: String
)

internal fun Player.toCsvLine(): String = "$id,$name"

internal fun List<Player>.overwritePlayers() {
    client.overwrite("players", this, Player::toCsvLine)
}

internal val players
    get() = client.retrieve("players", {
        Player(
            id = first().trim().toInt(),
            name = last().trim()
        )
    })