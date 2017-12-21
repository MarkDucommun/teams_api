package io.ducommun.mariokart.teamsapi

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.GetObjectRequest
import com.amazonaws.services.s3.model.ObjectMetadata
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.ducommun.mariokart.teamsapi.AssetController.FileType.*
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam.MODE_EXPLICIT

@SpringBootApplication
class TeamsApiApplication

fun main(args: Array<String>) {
    runApplication<TeamsApiApplication>(*args)
}

private val awsStaticCredentialsProvider = AWSStaticCredentialsProvider(BasicAWSCredentials(
    "", ""
))

private val client = AmazonS3ClientBuilder
    .standard()
    .withRegion(Regions.US_WEST_2)
    .withCredentials(awsStaticCredentialsProvider)
    .build()

val mapper = ObjectMapper().registerKotlinModule()

val databasePath = "src/main/resources/temp-database"

fun <T> AmazonS3.retrieve(name: String, csvToData: List<String>.() -> T): List<T> {

    val getObjectRequest = GetObjectRequest("mariokart-database", name)

    val file = File("$databasePath/$name-${System.currentTimeMillis()}.csv")

    getObject(getObjectRequest, file)

    return file
        .readLines()
        .map { it.split(",") }
        .filterNot { it.isEmpty() }
        .map(csvToData)
        .apply { file.delete() }
}

fun <T> AmazonS3.overwrite(name: String, list: List<T>, dataToCsv: T.() -> String) {

    val byteInputStream = list.joinToString("\n", transform = dataToCsv).byteInputStream()

    putObject("mariokart-database", name, byteInputStream, ObjectMetadata())
}

val <T> T.asJson: String get() = mapper.writeValueAsString(this)

data class Player(
    val id: Int,
    val name: String
)

fun Player.toCsvLine(): String = "$id,$name"

private fun List<Player>.overwritePlayers() {
    client.overwrite("players", this, Player::toCsvLine)
}

private val players
    get() = client.retrieve("players", {
        Player(
            id = first().trim().toInt(),
            name = last().trim()
        )
    })

data class Image(
    val id: Int,
    val key: String,
    val hasRace: Boolean
)

fun Image.toCsvLine(): String = "$id,$key,$hasRace"

private fun List<Image>.overwriteImages() {
    client.overwrite("images", this, Image::toCsvLine)
}

private val images: List<Image>
    get() = client.retrieve("images", {
        Image(
            id = this[0].trim().toInt(),
            key = this[1].trim(),
            hasRace = this[2].trim() == "true"
        )
    })

data class Race(
    val id: Int,
    val imageId: Int,
    val playerIds: List<Int>
)

fun Race.toCsvLine(): String = "$id,$imageId,${playerIds.joinToString(",")}"

private val races
    get() = client.retrieve("races", {
        Race(
            id = this[0].trim().toInt(),
            imageId = this[1].trim().toInt(),
            playerIds = drop(2).map { it.trim().toInt() }
        )
    })

private fun List<Race>.overwriteRaces() {
    client.overwrite("races", this, Race::toCsvLine)
}

data class Score(
    val id: Int,
    val raceId: Int,
    val value: Int
)

fun Score.toCsvLine(): String = "$id,$raceId,$value"

private val scores
    get() = client.retrieve("scores", {
        Score(
            id = this[0].trim().toInt(),
            raceId = this[1].trim().toInt(),
            value = this[2].trim().toInt()
        )
    })

private fun List<Score>.overwriteScores() {
    client.overwrite("scores", this, Score::toCsvLine)
}

@RestController
class PlayerController {

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

@RestController
class ScoreController {

    @PostMapping(value = "/scores")
    fun create(@RequestParam raceId: Int, @RequestParam score: Int) {

    }
}

@RestController
class UploadController {

    @PostMapping(value = "/images/{id}/delete")
    fun delete(@PathVariable id: Int) {

        images.filterNot { it.id == id }.overwriteImages()
    }

    @GetMapping(value = "/photos/last", produces = arrayOf("image/jpeg"))
    fun photos(): Mono<ByteArray> {

        val key = client.listObjects("mariokart-photos").objectSummaries.map { it.key }.lastOrNull()

        return if (key != null) {

            val file = File("src/main/resources/photos/temp/${System.currentTimeMillis()}").apply { createNewFile() }

            val objectStream = client.getObject("mariokart-photos", key).objectContent

            val fileOutputStream = FileOutputStream(file)

            val readBuffer = ByteArray(1024)

            var readLength = objectStream.read(readBuffer)

            while (readLength > 0) {
                fileOutputStream.write(readBuffer, 0, readLength)
                readLength = objectStream.read(readBuffer)
            }
            objectStream.close()
            fileOutputStream.close()

            val readBytes = file.readBytes()

            file.delete()

            Mono.just(readBytes)
        } else {
            Mono.just(File("src/main/resources/photos/12/19/16:23:34.jpeg").readBytes())
        }
    }

    @GetMapping(value = "/photos", params = arrayOf("key"), produces = arrayOf("image/jpeg"))
    fun getPhoto(
        @RequestParam key: String
    ): Mono<ByteArray> {

        val exists = client.listObjects("mariokart-photos").objectSummaries.map { it.key }.contains(key)

        if (exists) {

            val name = System.currentTimeMillis().toString()
            val pathName = "src/main/resources/photos/temp/$name"

            val file = File(pathName).apply { createNewFile() }

            val objectStream = client.getObject("mariokart-photos", key).objectContent

            val fileOutputStream = FileOutputStream(file)

            val readBuffer = ByteArray(1024)

            var readLength = objectStream.read(readBuffer)

            while (readLength > 0) {
                fileOutputStream.write(readBuffer, 0, readLength)
                readLength = objectStream.read(readBuffer)
            }
            objectStream.close()
            fileOutputStream.close()

            val readBytes = file.readBytes()

            file.delete()

            return Mono.just(readBytes)
        }

        return Mono.just(ByteArray(0))
    }

    @GetMapping(value = "/images", produces = arrayOf("application/json"))
    fun all(): Mono<String> {
        return images.asJson.toMono()
    }

    @PostMapping(value = "/images/{id}/update")
    fun update(
        @PathVariable id: Int,
        @RequestParam key: String? = null,
        @RequestParam hasRace: Boolean? = null
    ) {

        images.run {

            val image = find { it.id == id } ?: return

            val updatedImage = image.copy(
                hasRace = hasRace ?: image.hasRace,
                key = key ?: image.key
            )

            updateImage(updatedImage)
        }
    }

    @PostMapping(value = "/photos", consumes = arrayOf("multipart/form-data"), produces = arrayOf("application/json"))
    fun upload(@RequestPart file: Mono<FilePart>): Mono<String> {

        return file.map { it: FilePart ->

            val now = LocalDateTime.now()

            val dayPath = "${now.year}/${now.monthValue}/${now.dayOfMonth}"

            val path = "src/main/resources/photos/temp"

            File(path).mkdirs()

            val name = "${now.hour}:${now.minute}:${now.second}"

            val fileType = it.filename().split(".").last()

            val uncompressed = File("$path/$name.$fileType").apply { createNewFile() }

            val compressed = File("$path/$name-compressed.jpeg")

            it.transferTo(uncompressed)

            val image = ImageIO.read(uncompressed)

            ImageIO.getImageWritersByFormatName("jpg").next().run {

                output = ImageIO.createImageOutputStream(FileOutputStream(compressed))

                defaultWriteParam.run {
                    if (canWriteCompressed()) {
                        compressionMode = MODE_EXPLICIT
                        compressionQuality = 0.07f
                    }

                    write(null, IIOImage(image, null, null), this)

                    client.putObject("mariokart-photos", "$dayPath/$name", compressed)

                    uncompressed.delete()
                    compressed.delete()

                    val images = images

                    val lastId = images.sortedByDescending { it.id }.firstOrNull()?.id ?: 0

                    val newImage = Image(id = lastId + 1, key = "$dayPath/$name", hasRace = false)

                    images.plus(newImage).overwriteImages()

                    mapOf("image" to mapOf(
                        "path" to "photos?key=${newImage.key}",
                        "name" to newImage.key,
                        "id" to newImage.id
                    )).asJson
                }
            }
        }
    }
}

@RestController
class RaceController {

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

            plus(Race(
                id = lastId + 1,
                imageId = imageId,
                playerIds = players.split(",").map { it.trim().toInt() }.take(4)
            )).overwriteRaces()

            images.markHasRace(imageId)
        }
    }

    @GetMapping(value = "/races", produces = arrayOf("application/json"))
    fun races(): Mono<String> = races.asJson.toMono()
}

fun List<Image>.markHasRace(id: Int) {

    val image = find { it.id == id } ?: return

    updateImage(image.copy(hasRace = true))
}

fun List<Image>.updateImage(image: Image) {

    filterNot { it.id == image.id }.plus(image).overwriteImages()
}

fun List<Race>.updateRace(race: Race) {

    filterNot { it.id == race.id }.plus(race).overwriteRaces()
}

@RestController
class AssetController {

    val cache = false

    @GetMapping(value = "/upload.css")
    fun uploadCss(): Mono<String> = CSS.retrieve

    @GetMapping(value = "/upload.js")
    fun uploadJs(): Mono<String> = JS.retrieve

    @GetMapping(value = "/upload")
    fun uploadHtml(): Mono<String> = HTML.retrieve

    private val FileType.retrieve: Mono<String>
        get() {

            return if (cache) {
                fileCache[this] ?: Mono
                    .just(String(File("src/main/resources/upload.${name.toLowerCase()}").readBytes()))
                    .apply { fileCache[this@retrieve] = this }
            } else {
                Mono.just(String(File("src/main/resources/upload.${name.toLowerCase()}").readBytes()))
            }
        }

    private val fileCache = FileCache()

    data class FileCache(
        private var html: Mono<String>? = null,
        private var css: Mono<String>? = null,
        private var js: Mono<String>? = null
    ) {
        operator fun get(type: FileType): Mono<String>? =
            when (type) {
                HTML -> html
                CSS -> css
                JS -> js
            }

        operator fun set(type: FileType, value: Mono<String>) {

            when (type) {
                HTML -> html = value
                CSS -> css = value
                JS -> js = value
            }
        }
    }

    enum class FileType {
        HTML, CSS, JS
    }
}

@RestController
class SetupController {

    @GetMapping("/setup")
    fun setup() {
        val files = client.listObjects("mariokart-database").objectSummaries.map { it.key }

        listOf("images", "races", "scores", "players").forEach {

            if (!files.contains(it)) {
                client.putObject("mariokart-database", it, "".byteInputStream(), ObjectMetadata())
            }
        }
    }
}