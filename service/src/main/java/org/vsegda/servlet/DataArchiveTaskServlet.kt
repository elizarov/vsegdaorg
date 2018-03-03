package org.vsegda.servlet

import com.google.appengine.api.taskqueue.*
import org.vsegda.data.*
import org.vsegda.service.*
import org.vsegda.shared.*
import org.vsegda.storage.*
import org.vsegda.util.*
import org.vsegda.util.TimeUtil
import javax.servlet.http.*

private const val FLUSH_DELAY_MS = 10_000L // 10 seconds

class DataArchiveTaskServlet : HttpServlet(), Logged {
    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
        val streamId = req.getParameter("id").toLong()
        val stream = DataStreamStorage.loadDataStreamById(streamId) ?: error("Stream id=$streamId is not found")
        log.info("Archiving data for streamId=$streamId, mode=${stream.mode}")
        val firstItem = DataItemService.getFirstDataItem(stream)
        if (firstItem == null || stream.mode != DataStreamMode.LAST && firstItem.isRecent) {
            log.info("First stream item is recent or missing: $firstItem")
            return
        }
        val lastItem = DataItemService.getLastDataItem(stream)
        log.info("First item is $firstItem; last item is $lastItem")
        // Archive up to next midnight (or up to last item for LAST mode)
        val limit = if (stream.mode == DataStreamMode.LAST)
            lastItem.timeMillis
        else
            TimeUtil.getArchiveLimit(firstItem.timeMillis)

        val items = DataItemStorage.queryFirstDataItems(streamId, limit, MAX_ARCHIVE_ITEMS).toMutableList()

        // remove archives when needed
        if (stream.mode == DataStreamMode.LAST || stream.mode == DataStreamMode.RECENT) {
            log.info("Removing archives in ${stream.mode} mode")
            DataArchiveStorage.removeAllByStreamId(stream.streamId)
        }

        // never remove or archive the last item !!!
        if (!items.isEmpty() && items[items.size - 1].itemId == lastItem.itemId)
            items.removeAt(items.size - 1)
        if (items.isEmpty()) {
            log.warning("No items to archive")
            return
        }

        when (stream.mode) {
            DataStreamMode.LAST -> {
                log.info("Only last item is kept, removing other ones")
                DataItemService.removeDataItems(stream, items)
            }
            DataStreamMode.RECENT -> {
                log.info("Only recent items are kept, removing old ones")
                DataItemService.removeDataItems(stream, items)
            }
            DataStreamMode.ARCHIVE -> {
                // Actually archive
                val archive = DataArchive(streamId)
                archive.encodeItems(items)
                items.subList(archive.count, items.size).clear() // remove all that wasn't encoded
                log.info("Creating archive " + archive)
                DataArchiveStorage.storeDataArchive(archive)
                DataItemService.removeDataItems(stream, items)
            }
            else -> log.warning("Unsupported ${stream.mode} mode")
        }
        // create next task to check this stream again after some delay (to flush previous operation)
        enqueueTask(streamId, FLUSH_DELAY_MS)
    }

    companion object : Logged {
        fun enqueueTask(streamId: Long, waitMillis: Long = 0L) {
            log.info("Enqueueing data archive task for id=$streamId")
            val queue = QueueFactory.getQueue("archiveTaskQueue")
            queue.add(
                TaskOptions.Builder
                    .withUrl("/task/dataArchive")
                    .param("id", streamId.toString())
                    .countdownMillis(waitMillis)
            )
        }
    }
}
