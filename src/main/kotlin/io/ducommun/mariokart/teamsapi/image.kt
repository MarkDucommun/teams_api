package io.ducommun.mariokart.teamsapi

internal data class Image(
    val id: Int,
    val key: String,
    val hasRace: Boolean
)

internal fun Image.toCsvLine(): String = "$id,$key,$hasRace"

internal fun List<Image>.overwriteImages() {
    client.overwrite("images", this, Image::toCsvLine)
}

internal val images: List<Image>
    get() = client.retrieve(
        "images", {
        Image(
            id = this[0].trim().toInt(),
            key = this[1].trim(),
            hasRace = this[2].trim() == "true"
        )
    }
    )

internal fun List<Image>.markHasRace(id: Int) {

    val image = find { it.id == id } ?: return

    updateImage(image.copy(hasRace = true))
}

internal fun List<Image>.updateImage(image: Image) {

    filterNot { it.id == image.id }.plus(image).overwriteImages()
}
