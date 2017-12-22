package io.ducommun.mariokart.teamsapi

internal data class Score(
    val id: Int,
    val raceId: Int,
    val value: Int
)

internal fun Score.toCsvLine(): String = "$id,$raceId,$value"

internal val scores
    get() = client.retrieve("scores", {
        Score(
            id = this[0].trim().toInt(),
            raceId = this[1].trim().toInt(),
            value = this[2].trim().toInt()
        )
    })

internal fun List<Score>.overwriteScores() {
    client.overwrite("scores", this, Score::toCsvLine)
}

internal fun List<Score>.updateScore(score: Score) {

    filterNot { it.id == score.id }.plus(score).overwriteScores()
}