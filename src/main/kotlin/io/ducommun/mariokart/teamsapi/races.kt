package io.ducommun.mariokart.teamsapi

internal data class Race(
    val id: Int,
    val imageId: Int,
    val playerIds: List<Int>,
    val hasScore: Boolean = false
)

internal fun Race.toCsvLine(): String = "$id,$imageId,$hasScore,${playerIds.joinToString(",")}"

internal val races
    get() = client.retrieve("races", {
        Race(
            id = this[0].trim().toInt(),
            imageId = this[1].trim().toInt(),
            hasScore = this[2].trim() == "true",
            playerIds = drop(3).map { it.trim().toInt() }
        )
    })

internal fun List<Race>.overwriteRaces() {
    client.overwrite("races", this, Race::toCsvLine)
}

internal fun List<Race>.updateRace(race: Race) {

    filterNot { it.id == race.id }.plus(race).overwriteRaces()
}

internal fun List<Race>.markHasScore(id: Int) {

    val race = find { it.id == id } ?: return

    updateRace(race.copy(hasScore = true))
}