package io.ducommun.mariokart.teamsapi

import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam

@RestController
class ImagesController {

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
                        compressionMode = ImageWriteParam.MODE_EXPLICIT
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