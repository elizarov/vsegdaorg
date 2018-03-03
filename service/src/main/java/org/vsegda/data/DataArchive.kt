package org.vsegda.data

import org.vsegda.util.*
import java.io.*
import java.util.*

class DataArchive {
    var archiveId: Long = 0L
    var streamId: Long = 0L
    var count: Int = 0
    var firstValue: Double = 0.0
    var firstTimeMillis: Long = 0L
    var highValue: Double = 0.0
    var lowValue: Double = 0.0
    var encodedItems: ByteArray? = null

    // Some DataArchives were encoded with count that is too high.
    // just ignore EOFException and break to decode only as much as possible
    val items: List<DataItem>
        get() {
            val result = ArrayList<DataItem>(count)
            result.add(DataItem(streamId, firstValue, firstTimeMillis))
            encodedItems?.let {
                with(DeltaDecoder(firstValue, firstTimeMillis, it)) {
                    for (i in 1 until count) {
                        try {
                            result.add(DataItem(streamId, readValue(), readTime()))
                        } catch (e: EOFException) {
                            break
                        }
                    }
                }
            }
            return result
        }

    constructor()

    constructor(streamId: Long) {
        this.streamId = streamId
    }

    // encodes only up to a limit. Use getCount
    fun encodeItems(items: Collection<DataItem>) {
        if (items.isEmpty())
            throw IllegalArgumentException("empty")
        val it = items.iterator()
        val firstItem = it.next()
        val encoder = DeltaEncoder(firstItem.value, firstItem.timeMillis)
        firstValue = encoder.lastValue
        firstTimeMillis = encoder.lastTimeMillis
        highValue = firstValue
        lowValue = firstValue
        count = 1
        var lastGoodSize = encoder.size()
        while (it.hasNext()) {
            val item = it.next()
            val value = item.value
            encoder.writeValue(value)
            encoder.writeTime(item.timeMillis)
            val size = encoder.size()
            if (size > MAX_ARCHIVE_ENCODED_SIZE)
                break
            lastGoodSize = size
            highValue = Math.max(highValue, value)
            lowValue = Math.min(lowValue, value)
            count++
        }
        encodedItems = encoder.toByteArray(lastGoodSize)
    }

    override fun toString(): String =
        "$streamId,$firstValue,${TimeUtil.formatDateTime(firstTimeMillis)}" +
            (encodedItems?.run { "#{count=$count,high=$highValue,low=$lowValue,bytes=$size}" } ?: "")
}
