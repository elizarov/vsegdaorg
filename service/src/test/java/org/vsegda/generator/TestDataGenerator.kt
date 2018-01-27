package org.vsegda.generator

import java.io.*
import java.net.*
import java.util.*

fun main(args: Array<String>) {
    val url = "http://localhost:8080/data.csv"
    val streamCode = "TST"
    val fromTime = 10.0 // days
    val n = 100 // items
    val baos = ByteArrayOutputStream()
    var lastValue = 50.0
    val rnd = Random()
    with(baos.writer()) {
        repeat(n) { index ->
            val value = lastValue + 10 * (rnd.nextDouble() - 0.5)
            val time = (n - index) * fromTime / n
            write("$streamCode,$value,${time}d\r\n")
            lastValue = value
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