package org.vsegda.servlet

import com.google.appengine.api.taskqueue.*
import org.vsegda.service.*
import org.vsegda.service.DataArchiveService.Result.*
import org.vsegda.util.*
import javax.servlet.http.*

private const val TASK_DELAY_MS = 1000L // 1 second

class DataArchiveTaskServlet : HttpServlet(), Logged {
    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
        val streamId = req.getParameter("id")?.toLongOrNull() ?:
            error("Required 'id' parameter")
        val lastTime = req.getParameter("lastTime")?.toLongOrNull() ?: 0L
        val delay = req.getParameter("delay")?.toLongOrNull() ?: 0L
        val result = DataArchiveService.createDataArchive(streamId, lastTime)
        when (result) {
            is Done -> return
            is RetryLater -> enqueueTask(streamId, lastTime, (delay * 2).coerceAtLeast(TASK_DELAY_MS))
            is DoAgain -> enqueueTask(streamId, result.lastTime, TASK_DELAY_MS)
        }
    }

    companion object : Logged {
        fun enqueueTask(streamId: Long, lastTime: Long = 0L, delay: Long = 0L) {
            log.info("Enqueueing data archive task for id=$streamId")
            val queue = QueueFactory.getQueue("archiveTaskQueue")
            queue.add(
                TaskOptions.Builder
                    .withUrl("/task/dataArchive")
                    .param("id", streamId.toString())
                    .param("lastTime", lastTime.toString())
                    .param("delay", delay.toString())
                    .countdownMillis(delay)
            )
        }
    }
}
