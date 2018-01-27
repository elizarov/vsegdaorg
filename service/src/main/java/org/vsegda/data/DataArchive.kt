package org.vsegda.data

import com.google.appengine.api.datastore.Key
import org.vsegda.util.TimeUtil

import java.io.EOFException
import java.util.ArrayList

class DataArchive {
    var key: Key? = null
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
            if (encodedItems != null) with(DeltaDecoder(firstValue, firstTimeMillis, encodedItems)) {
                for (i in 1 until count) {
                    try {
                        result.add(DataItem(streamId, readValue(), readTime()))
                    } catch (e: EOFException) {
                        break
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
            if (size > MAX_ENCODED_SIZE)
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
            (encodedItems?.run { "#{count=$count,high=$highValue,low=$lowValue,bytes=$size" } ?: "")
                
    companion object {
        /**
         * Each archive keeps data for one day.
         */
        const val ARCHIVE_INTERVAL = TimeUtil.DAY

        /**
         * Data items usually come every 5 minutes and we round times in archive to it.
         */
        const val TIME_PRECISION = 5 * TimeUtil.MINUTE

        /**
         * Estimated number of items per archive.
         */
        const val COUNT_ESTIMATE = (ARCHIVE_INTERVAL / TIME_PRECISION).toInt()

        /**
         * Data is considered recent for 2 days (then it is archived or deleted).
         */
        const val RECENT_TIME_INTERVAL = 2 * TimeUtil.DAY

        /**
         * Limit for max size of data archive blob
         * :todo: can be increased
         */
        const val MAX_ENCODED_SIZE = 1500
    }
}
