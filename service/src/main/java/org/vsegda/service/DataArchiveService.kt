package org.vsegda.service

import org.vsegda.data.*
import org.vsegda.service.DataArchiveService.Result.*
import org.vsegda.shared.*
import org.vsegda.storage.*
import org.vsegda.util.*

object DataArchiveService : Logged {
    sealed class Result {
        object Done : Result()
        object RetryLater : Result()
        data class DoAgain(val lastTime: Long) : Result()
    }

    fun createDataArchive(streamId: Long, prevTime: Long): Result =
        logged("createDataArchive(streamId=$streamId)", around = true) {
            val stream = DataStreamStorage.loadDataStreamById(streamId) ?:
                error("Stream id=$streamId is not found")
            val firstItem = DataItemService.getFirstDataItem(stream)
            if (firstItem == null || stream.mode != DataStreamMode.LAST && firstItem.isRecent) {
                log.info("First stream item is recent or missing: $firstItem, do not archive")
                return@logged Done
            }
            val lastItem = DataItemService.getLastDataItem(stream)
            // check if there is the only item (always keep it)
            if (firstItem.itemId == lastItem.itemId) {
                log.info("There is only one item: $firstItem, do not archive")
                return@logged Done
            }
            require(firstItem.timeMillis < lastItem.timeMillis) {
                "Inconsistent time order between firstItem=$firstItem, lastItem=$lastItem"
            }
            // Archive up to next midnight (or up to last item for LAST mode)
            val limit = if (stream.mode == DataStreamMode.LAST)
                lastItem.timeMillis
            else
                TimeUtil.getArchiveLimit(firstItem.timeMillis)
            // log info
            log.info("First item is $firstItem; last item is $lastItem; limit=${TimeUtil.formatDateTime(limit)}")
            require(limit > firstItem.timeMillis) { "Inconsistent limit" }
            if (lastItem.timeMillis < limit) {
                log.info("Last item is below archive limit, do not archive (until more data comes)")
                return@logged Done
            }
            // query items
            val items = DataItemStorage.queryFirstDataItems(streamId, limit, MAX_ARCHIVE_ITEMS)
                .toMutableList()
            // we should retrieve at least the first item
            if (items.isEmpty()) {
                log.warning("Inconsistent read from the storage (items are missing), will retry later")
                return@logged RetryLater
            }
            if (items.first().itemId != firstItem.itemId) {
                log.warning("Inconsistent read from the storage (different first item), will retry later")
                return@logged RetryLater
            }
            if (items.first().timeMillis < prevTime) {
                log.warning("Inconsistent read from the storage (less that previous time of ${TimeUtil.formatDateTime(prevTime)}), will retry later")
                return@logged RetryLater
            }
            // never remove or archive the last item (we've already checked for it)
            require(items.last().itemId != lastItem.itemId) { "Should not retrieve last item: ${items.last()}" }
            when (stream.mode) {
                DataStreamMode.LAST -> {
                    log.info("LAST MODE: Only the last item is kept, removing other ones")
                    DataItemService.removeDataItems(stream, items)
                    DataArchiveStorage.removeAllByStreamId(stream.streamId) // drop all archives
                }
                DataStreamMode.RECENT -> {
                    log.info("RECENT MODE: Only recent items are kept, removing old ones")
                    DataItemService.removeDataItems(stream, items)
                    DataArchiveStorage.removeAllByStreamId(stream.streamId) // drop all archives
                }
                DataStreamMode.ARCHIVE -> {
                    // Actually archive
                    val archive = DataArchive(streamId)
                    archive.encodeItems(items)
                    items.subList(archive.count, items.size).clear() // remove all that wasn't encoded
                    log.info("ARCHIVE MODE: Creating archive " + archive)
                    DataArchiveStorage.storeDataArchive(archive)
                    DataItemService.removeDataItems(stream, items)
                }
                else -> error("Unsupported ${stream.mode} mode")
            }
            return@logged DoAgain(items.last().timeMillis)
        }
}