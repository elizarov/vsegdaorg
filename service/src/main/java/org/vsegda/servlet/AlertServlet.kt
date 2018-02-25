package org.vsegda.servlet

import org.vsegda.service.*
import java.io.*
import javax.servlet.*
import javax.servlet.http.*

class AlertServlet : HttpServlet() {
    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
        val text = req.getParameter("text") ?: throw ServletException("text parameter required")
        val id = req.getParameter("id")
        AlertService.sendAlertEmail(id, text)
    }
}
