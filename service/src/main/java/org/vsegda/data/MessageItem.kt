package org.vsegda.data

import org.vsegda.util.*
import kotlin.math.*

class MessageItem(
    var queueId: Long = 0,
    var messageIndex: Long = 0
) {
    var text: String = ""
    var timeMillis: Long = 0

    val time: String
        get() = formatDateTime(timeMillis)

    val ago: String
        get() = formatDateTimeDifference(timeMillis, System.currentTimeMillis())

    val formatClass: String
        get() = formatTimeClass(timeMillis, System.currentTimeMillis())

    constructor(line: String, now: Long) : this() {
        val tokens = line.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (tokens.size < 2 || tokens.size > 4)
            throw IllegalArgumentException("Invalid line format: " + line)
        try {
            queueId = tokens[0].toLong()
            text = tokens[1]
            timeMillis = if (tokens.size < 3) now else min(parseTime(tokens[2], now), now)
            messageIndex = if (tokens.size < 4) 0 else tokens[3].toLong()
        } catch (e: NumberFormatException) {
            throw IllegalArgumentException("Invalid line format: " + line, e)
        }
    }

    override fun toString(): String = "$queueId,$text,$time,$messageIndex"
}
