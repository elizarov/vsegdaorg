package org.vsegda.data

import com.google.appengine.api.datastore.*
import org.vsegda.util.*

class DataItem {
    var key: Key? = null
    var streamId: Long = 0L
    var value: Double = 0.0
    var timeMillis: Long = 0L

    // non-persistent!!!
    private var _stream: DataStream? = null

    val time: String
        get() = TimeUtil.formatDateTime(timeMillis)

    val ago: String
        get() = TimeUtil.formatDateTimeDifference(timeMillis, System.currentTimeMillis())

    val formatClass: String
        get() = TimeUtil.getFormatClass(timeMillis, System.currentTimeMillis())

    val isRecent: Boolean
        get() = timeMillis >= System.currentTimeMillis() - DataArchive.RECENT_TIME_INTERVAL

    var stream: DataStream
        get() = _stream ?: DataStream(streamId)
        set(stream) {
            if (streamId == 0L)
                streamId = stream.streamId
            else
                require(streamId == stream.streamId)
            _stream = stream
        }

    /**
     * Returns stream id or tag of the stream if defined.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    val streamCode: String
        get() = _stream?.code ?: streamId.toString()

    constructor()

    constructor(line: String, now: Long) {
        val tokens =
            line.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        require(tokens.size in 2..3) { "Invalid line format: $line" }
        stream = DataStream()
        stream.tag = tokens[0]
        try {
            value = tokens[1].toDouble()
            timeMillis = if (tokens.size < 3) now else Math.min(TimeUtil.parseTime(tokens[2], now), now)
        } catch (e: NumberFormatException) {
            throw IllegalArgumentException("Invalid line format: " + line, e)
        }
    }

    @JvmOverloads
    constructor(streamId: Long, value: Double, timeMillis: Long, key: Key? = null) {
        this.key = key
        this.streamId = streamId
        this.value = value
        this.timeMillis = timeMillis
    }

    constructor(stream: DataStream, value: Double, timeMillis: Long) {
        this.streamId = stream.streamId
        this.stream = stream
        this.value = value
        this.timeMillis = timeMillis
    }

    override fun toString(): String = "$streamCode,$value,$time"

    companion object {
        val ORDER_BY_TIME = compareBy<DataItem> { it.timeMillis }
    }
}
