package org.vsegda.servlet

import com.google.appengine.api.taskqueue.*
import org.vsegda.service.*
import org.vsegda.util.*
import javax.servlet.http.*

class DataCacheRefreshTaskServlet : HttpServlet() {
    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
        DataItemService.refreshCache(req.getParameter("id").toLong())
    }

    companion object : Logged {
        fun enqueueTask(streamId: Long) {
            log.info("Enqueueing data cache refresh task for id=$streamId")
            val queue = QueueFactory.getQueue("dataCacheRefreshTaskQueue")
            queue.add(
                TaskOptions.Builder
                    .withUrl("/task/dataCacheRefresh")
                    .param("id", streamId.toString())
            )
        }
    }
}

