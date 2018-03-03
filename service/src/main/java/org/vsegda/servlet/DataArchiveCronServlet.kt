package org.vsegda.servlet

import org.vsegda.data.*
import org.vsegda.service.*
import org.vsegda.shared.*
import org.vsegda.util.*
import javax.servlet.http.*

class DataArchiveCronServlet : HttpServlet(), Logged {
    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) =
        logged("Checking data items for archival needs") {
            for (stream in DataStreamService.dataStreams) {
                // check for archive or purge (find non-recent items)
                val firstItem = DataItemService.getFirstDataItem(stream)
                val lastItem = DataItemService.getLastDataItem(stream)
                log.fine("First item is $firstItem; last item is $lastItem")
                val threshold = if (stream.mode == DataStreamMode.LAST)
                    lastItem.timeMillis
                else
                    System.currentTimeMillis() - RECENT_TIME_INTERVAL - ARCHIVE_INTERVAL
                if (firstItem != null && firstItem.timeMillis < threshold && firstItem.key != lastItem.key) {
                    // need to archive
                    DataArchiveTaskServlet.enqueueTask(stream.streamId)
                }
            }
        }
}
