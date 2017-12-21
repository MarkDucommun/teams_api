package io.ducommun.mariokart.teamsapi

import junit.framework.Assert.assertEquals
import org.junit.Test
import java.io.File

class JpegMaker {

    @Test
    fun `it should make a JPEG`() {

        val expectedBytes = File("src/test/resources/apple.jpeg").readBytes()

        val incomingStringBytes = File("src/test/resources/formUpload.txt").readBytes()

        assertEquals(expectedBytes.size, incomingStringBytes.size)

        val expectedString = String(expectedBytes)

        val incomingString = String(expectedBytes)

        expectedString.lines().zip(incomingString.lines()).forEach { (expected, actual) ->

            assertEquals(expected, actual)
        }
    }
}