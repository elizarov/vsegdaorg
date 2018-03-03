package org.vsegda.servlet

import org.vsegda.service.*
import org.vsegda.util.*
import javax.servlet.http.*

class DataCheckTimeoutCronServlet : HttpServlet(), Logged {
    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) =
        logged("Checking data items for timeouts", around = true) {
            for (stream in DataStreamService.dataStreams) {
                // check for data update timeout
                if (stream.alertTimeout != 0L) {
                    val item = DataItemService.getLastDataItem(stream)
                    if (System.currentTimeMillis() - item.timeMillis > stream.alertTimeout)
                        AlertService.sendAlertEmail(stream.code, "Data update timeout")
                }
            }
        }
}
