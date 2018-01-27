package org.vsegda.service

import org.vsegda.data.*
import org.vsegda.shared.*
import org.vsegda.storage.*
import org.vsegda.util.*
import java.io.*
import java.util.*
import java.util.concurrent.*

object DataItemService {
    val DEFAULT_SPAN: TimePeriod = TimePeriod.valueOf(1, TimePeriodUnit.WEEK)
    const val DEFAULT_N = 2500

    private const val MAX_CACHED_LIST_SIZE = (1.5 * DEFAULT_N).toInt()

    private val listCache = ConcurrentHashMap<Long, ListEntry>()

    private fun addDataItem(dataItem: DataItem) {
        DataItemStorage.storeDataItem(dataItem)
        listCache[dataItem.streamId]?.apply {
            synchronized(this) {
                items.add(dataItem)
                val size = items.size
                if (size >= 2 && DataItem.ORDER_BY_TIME.compare(items[size - 1], items[size - 2]) < 0)
                    items.sortWith(DataItem.ORDER_BY_TIME)
                if (size > MAX_CACHED_LIST_SIZE)
                    items.subList(0, size - DEFAULT_N).clear()
            }
        }
    }

    fun addDataItems(items: List<DataItem>) = items.forEach { addDataItem(it) }

    // DON'T REMOVE THEM FROM THE CACHE BY DESIGN AS THEY TYPICALLY MOVE TO ARCHIVES
    fun removeDataItems(stream: DataStream, items: List<DataItem>) =
        items.forEach {
            require(it.streamId == stream.streamId)
            DataItemStorage.deleteDataItem(it)
        }

    fun refreshCache(streamId: Long) {
        performItemsQuery(streamId, null, null, DEFAULT_N, true)
    }

    fun getFirstDataItem(stream: DataStream): DataItem? =
        DataItemStorage.loadFirstDataItem(stream.streamId)?.also { it.stream = stream }

    fun getLastDataItem(stream: DataStream): DataItem {
        val items = getDataItems(stream, null, null, 1)
        return if (items.isEmpty()) DataItem(stream, java.lang.Double.NaN, 0) else items[0]
    }

    fun getDataItems(stream: DataStream, from: TimeInstant?, to: TimeInstant?, n: Int): List<DataItem> {
        var n = n
        if (stream.mode == DataStreamMode.LAST) {
            if (to != null)
                return emptyList()
            n = 1
        }
        // try cache
        val result: List<DataItem>? = listCache[stream.streamId]?.run {
            synchronized(this) {
                val size = items.size
                var fromIndex = 0
                var toIndex = size
                if (from != null) {
                    while (fromIndex < size && items[fromIndex].timeMillis < from.time())
                        fromIndex++
                }
                if (to != null) {
                    while (toIndex > fromIndex && items[toIndex - 1].timeMillis >= to.time())
                        toIndex--
                }
                val fromTime = from?.time() ?: 0
                // can safely return from cache?
                if (fromTime <= fromTime || fromIndex > 0 || toIndex - fromIndex >= n)
                    items.subList(Math.max(fromIndex, toIndex - n), toIndex).toList()
                else
                    null
            }
        }
        // perform query if not found in cache
        return fillStream(stream, result ?: performItemsQuery(stream.streamId, from, to, n, false))
    }

    private fun performItemsQuery(
        streamId: Long,
        from: TimeInstant?,
        to: TimeInstant?,
        n: Int,
        forceCacheUpdate: Boolean
    ): List<DataItem> {
        // query both recent items and archive
        val items = ArrayList<DataItem>()
        items.addAll(DataItemStorage.queryDataItems(streamId, from, to, n))
        if (items.size < n)
            items.addAll(DataArchiveStorage.queryItemsFromDataArchives(streamId, from, to, n - items.size))
        items.sortWith(DataItem.ORDER_BY_TIME)
        if (items.size > n)
            items.subList(0, items.size - n).clear() // remove extra items
        // update cache if needed (only when querying up to now)
        if (to == null) {
            val fromTime = from?.time() ?: if (items.size < n) 0 else items[0].timeMillis
            val oldCacheEntry = listCache[streamId]
            if (forceCacheUpdate || oldCacheEntry == null ||
                oldCacheEntry.items.size <= items.size ||
                oldCacheEntry.fromTime >= fromTime
            ) {
                val cacheItems = if (items.size <= MAX_CACHED_LIST_SIZE)
                    items
                else
                    ArrayList(items.subList(items.size - MAX_CACHED_LIST_SIZE, items.size))
                listCache[streamId] = ListEntry(fromTime, cacheItems)
            }
        }
        return items
    }

    private fun fillStream(stream: DataStream, items: List<DataItem>): List<DataItem> =
        items.onEach { it.stream = stream }

    fun updateStreamId(fromId: Long, toId: Long) {
        listCache.remove(fromId)
        listCache.remove(toId)
        DataItemStorage.updateStreamId(fromId, toId)
        DataArchiveStorage.updateStreamId(fromId, toId)
    }

    fun removeAllDataItems(stream: DataStream) {
        listCache.remove(stream.streamId)
        DataItemStorage.removeAllByStreamId(stream.streamId)
        DataArchiveStorage.removeAllByStreamId(stream.streamId)

    }

    private class ListEntry internal constructor(
        internal var fromTime: Long,
        internal var items: MutableList<DataItem>
    ) : Serializable {
        companion object {
            private const val serialVersionUID = 4488592054732566662L
        }
    }
}
