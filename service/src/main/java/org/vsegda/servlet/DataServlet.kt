package org.vsegda.servlet

import org.vsegda.data.*
import org.vsegda.request.*
import org.vsegda.service.*
import java.io.*
import javax.servlet.http.*

class DataServlet : HttpServlet() {
    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        val dataRequest = DataRequest(req)
        resp.contentType = "text/csv"
        resp.characterEncoding = "UTF-8"
        val out = resp.outputStream
        for (item in dataRequest.queryList())
            out.println(item.toString())
    }

    override fun doPut(req: HttpServletRequest, resp: HttpServletResponse) {
        val items = parseDataItems(req.reader, System.currentTimeMillis())
        // resolve all stream tags
        for (item in items)
            item.stream = DataStreamService.resolveDataStream(item.stream)
        // persist all items
        DataItemService.addDataItems(items)
    }

    private fun parseDataItems(input: BufferedReader, now: Long): List<DataItem> =
        generateSequence { input.readLine() }.map { line -> DataItem(line, now) }.toList()
}
