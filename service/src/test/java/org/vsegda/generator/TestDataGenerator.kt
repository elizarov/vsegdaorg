package org.vsegda.generator

import org.vsegda.data.*
import org.vsegda.util.*
import java.io.*
import java.net.*
import java.util.*

fun main(args: Array<String>) {
    val url = "http://localhost:8080/data.csv"
    val streamCode = "TST"
    val n = 10 * 24 * 12 // 10 days of items (every 5 mins)
    val baos = ByteArrayOutputStream()
    var lastValue = 50.0
    val rnd = Random()
    var millis = 0L
    with(baos.writer()) {
        repeat(n) {
            val value = lastValue + 10 * (rnd.nextDouble() - 0.5)
            val minutes = millis / MINUTE
            write("$streamCode,${value.fmt(2)},${minutes}m\r\n")
            lastValue = value
            millis -= TIME_PRECISION
        }
        flush()
    }
    println("Sending ${baos.size()} bytes to $url")
    with (URL(url).openConnection() as HttpURLConnection) {
        requestMethod = "PUT"
        doOutput = true
        outputStream.write(baos.toByteArray())
        println("Response $responseCode: $responseMessage")
    }
}

private fun Double.fmt(i: Int): String = String.format("%.${i}f", this)
