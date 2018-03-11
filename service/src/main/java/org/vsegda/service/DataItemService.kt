package org.vsegda.service

import org.vsegda.data.*
import org.vsegda.request.*
import org.vsegda.shared.*
import org.vsegda.storage.*
import org.vsegda.util.*
import java.util.*
import java.util.concurrent.*
import kotlin.math.*

private val ITEM_BY_VALUE = compareBy(DataItem::value)

object DataItemService : Logged {
    private val cache = ConcurrentHashMap<Long, CachedItems>()

    private fun addDataItem(dataItem: DataItem) {
        DataItemStorage.storeDataItem(dataItem)
        cache[dataItem.streamId]?.apply { addCached(dataItem) }
    }

    private fun CachedItems.addCached(dataItem: DataItem) = synchronized(this) {
        items.add(dataItem)
        val size = items.size
        if (size >= 2 && DataItem.BY_TIME.compare(items[size - 1], items[size - 2]) < 0)
            items.sortWith(DataItem.BY_TIME)
        if (size > MAX_CACHED_ITEMS)
            items.subList(0, size - PRELOAD_CACHED_ITEMS).clear()
    }

    fun addDataItems(items: List<DataItem>) = items.forEach { addDataItem(it) }

    // DON'T REMOVE THEM FROM THE CACHE BY DESIGN AS THEY MOVE TO ARCHIVES
    fun removeDataItems(stream: DataStream, items: List<DataItem>) {
        items.forEach { require(it.streamId == stream.streamId) }
        DataItemStorage.deleteDataItems(items)
    }

    fun refreshCache(streamId: Long) =
        logged("refreshCache(streamId=$streamId)", around = true) {
            performItemsQuery(streamId, null, null, PRELOAD_CACHED_ITEMS, true)
        }

    fun getFirstDataItem(stream: DataStream): DataItem? =
        DataItemStorage.loadFirstDataItem(stream.streamId)?.also { it.stream = stream }

    fun getLastDataItem(stream: DataStream): DataItem {
        val items = getDataItems(stream, null, null, 1)
        return if (items.isEmpty()) DataItem(stream, java.lang.Double.NaN, 0) else items[0]
    }

    fun getDataItems(
        stream: DataStream,
        from: TimeInstant?,
        to: TimeInstant?,
        nRequested: Int,
        conflate: TimePeriod? = null,
        op: ConflateOp = ConflateOp.MAX
    ): List<DataItem> {
        val (nQuery, nReturn) = when {
            stream.mode == DataStreamMode.LAST -> {
                if (to != null) return emptyList()
                1 to 1
            }
            conflate != null -> {
                val q = (conflate.period + TIME_PRECISION - 1) / TIME_PRECISION
                (nRequested * q).toInt() to nRequested
            }
            else -> nRequested to nRequested
        }
        // take all we have from cache
        val cachedResult = cache[stream.streamId]
            ?.getCached(from, to, nQuery)
            ?: CachedResult(mutableListOf(), false)
        val cachedItems = cachedResult.items
        conflate(cachedItems, conflate, op)
        cachedItems.trimFrontToSize(nReturn)
        if (cachedResult.all || cachedItems.size >= nReturn) {
            return fillStream(stream, cachedItems)
        }
        // perform query if not found in cache
        val firstCachedTime = cachedItems.firstOrNull()?.timeMillis
        val queryTo = when {
            firstCachedTime == null -> to
            to == null -> TimeInstant.valueOf(firstCachedTime)
            else -> TimeInstant.valueOf(min(firstCachedTime, to.time()))
        }
        val queriedItems = performItemsQuery(
            stream.streamId, from, queryTo, nQuery - cachedItems.size, false)
        conflate(queriedItems, conflate, op)
        queriedItems.addAll(cachedItems)
        queriedItems.trimFrontToSize(nReturn)
        return fillStream(stream, queriedItems)
    }

    private fun conflate(list: MutableList<DataItem>, conflate: TimePeriod?, op: ConflateOp) {
        if (conflate == null) return
        val period = conflate.period
        var j = 0
        var prev = 0L
        var firstInBucket = true
        for (i in list.indices) {
            val item = list[i]
            val cur = item.timeMillis / period
            list[j] = if (firstInBucket) item else when (op) {
                ConflateOp.LAST -> item
                ConflateOp.MAX -> maxOf(item, list[j], ITEM_BY_VALUE)
                ConflateOp.MIN -> minOf(item, list[j], ITEM_BY_VALUE)
            }
            if (cur != prev) {
                j++
                prev = cur
                firstInBucket = true
            } else {
                firstInBucket = false
            }
        }
        list.subList(j, list.size).clear()
    }

    class CachedResult(val items: MutableList<DataItem>, val all: Boolean)

    private fun CachedItems.getCached(
        from: TimeInstant?,
        to: TimeInstant?,
        n: Int
    ): CachedResult = synchronized(this) {
        val items = items
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
        return CachedResult(items.subList(max(fromIndex, toIndex - n), toIndex).toMutableList(),
            this.fromTime <= fromTime || fromIndex > 0 || toIndex - fromIndex >= n)
    }

    private fun performItemsQuery(
        streamId: Long,
        from: TimeInstant?,
        to: TimeInstant?,
        n: Int,
        forceCacheUpdate: Boolean
    ): MutableList<DataItem> {
        // query both recent items and archive
        val items = ArrayList<DataItem>(DataItemStorage.queryDataItems(streamId, from, to, n))
        if (items.size < n)
            items += DataArchiveStorage.queryItemsFromDataArchives(streamId, from, to, n - items.size)
        items.sortWith(DataItem.BY_TIME)
        // update cache if needed
        val fromTime = from?.time() ?: if (items.size < n) 0 else items[0].timeMillis
        val oldCached = cache[streamId]
        synchronizedIfNeeded(oldCached) {
            if (forceCacheUpdate ||
                (oldCached == null || oldCached.fromTime >= fromTime ||
                    items.isNotEmpty() && items.last().timeMillis >= oldCached.lastTime) &&
                (to == null || oldCached != null && to.time() >= oldCached.fromTime)
            ) {
                val cachedItems = if (oldCached == null || to == null)
                    ArrayList(items)
                else
                    merge(items, to.time(), oldCached.items)
                val cached: CachedItems = if (cachedItems.size > MAX_CACHED_ITEMS) {
                    cachedItems.trimFrontToSize(MAX_CACHED_ITEMS)
                    CachedItems(cachedItems.first().timeMillis, cachedItems)
                } else {
                    CachedItems(fromTime, cachedItems)
                }
                cache[streamId] = cached
                log.info("performItemsQuery(streamId=$streamId) $cached")
            }
        }
        // remove extra items beyond requested
        items.trimFrontToSize(n)
        return items
    }

    private fun MutableList<DataItem>.trimFrontToSize(n: Int) {
        if (size > n) subList(0, size - n).clear()
    }

    private fun merge(newItems: List<DataItem>, time: Long, oldItems: List<DataItem>): ArrayList<DataItem> {
        val result = ArrayList<DataItem>()
        newItems.filterTo(result) { it.timeMillis < time }
        oldItems.filterTo(result) { it.timeMillis >= time }
        return result
    }

    private inline fun synchronizedIfNeeded(lock: Any?, block: () -> Unit) {
        if (lock != null) synchronized(lock, block) else block()
    }

    private fun fillStream(stream: DataStream, items: List<DataItem>): List<DataItem> =
        items.onEach { it.stream = stream }

    fun updateStreamId(fromId: Long, toId: Long) {
        cache.remove(fromId)
        cache.remove(toId)
        DataItemStorage.updateStreamId(fromId, toId)
        DataArchiveStorage.updateStreamId(fromId, toId)
    }

    fun removeAllDataItems(stream: DataStream) {
        cache.remove(stream.streamId)
        DataItemStorage.removeAllByStreamId(stream.streamId)
        DataArchiveStorage.removeAllByStreamId(stream.streamId)
    }

    private class CachedItems(
        val fromTime: Long,
        val items: MutableList<DataItem>
    ) {
        val lastTime: Long
            get() = items.lastOrNull()?.timeMillis ?: fromTime
        
        override fun toString() =
            "Cached fromTime=${formatDateTime(fromTime)} nItems=${items.size}"
    }
}
