package org.vsegda.servlet

import org.vsegda.service.*
import org.vsegda.util.*
import javax.servlet.http.*

class DataCacheRefreshCronServlet : HttpServlet(), Logged {
    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) =
        logged("Scheduling cache updates", around = true){
            // enqueue cache update task
            for (stream in DataStreamService.dataStreams) {
                DataCacheRefreshTaskServlet.enqueueTask(stream.streamId)
            }
        }
}
