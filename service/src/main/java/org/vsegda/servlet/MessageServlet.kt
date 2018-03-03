package org.vsegda.servlet

import org.vsegda.data.*
import org.vsegda.request.*
import org.vsegda.service.*
import java.io.*
import java.util.*
import javax.servlet.http.*

private const val SESSION = "session"
private const val NEW_SESSION = "newsession"

class MessageServlet : HttpServlet() {
    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        writeGetResponse(resp, MessageRequest(req, false))
    }

    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
        val messageRequest = MessageRequest(req, true)
        val now = System.currentTimeMillis()
        val sessions = parseSessions(req)
        val items = parseMessageItems(req.reader, now)
        val queues = sessions.keys.toSet() + items.keys.toSet()
        val resolvedSessions = queues.associate { queue ->
            queue to MessageQueueService.postToQueue(queue, sessions[queue], items[queue], now)
        }
        addSessionCookies(resp, resolvedSessions)
        // combine poll for messages with POST
        if (messageRequest.isTake)
            writeGetResponse(resp, messageRequest)
    }

    private fun writeGetResponse(resp: HttpServletResponse, messageRequest: MessageRequest) {
        resp.contentType = "text/csv"
        resp.characterEncoding = "UTF-8"
        val out = resp.outputStream
        for (item in messageRequest.query())
            out.println(item.toString())
    }

    private fun parseSessions(req: HttpServletRequest): Map<Long, Long?> {
        val sessions = HashMap<Long, Long?>()
        // get sessions from cookies
        val cookies = req.cookies
        if (cookies != null)
            for (cookie in cookies) {
                val name = cookie.name
                if (name.startsWith(SESSION)) {
                    val queueId = name.substring(SESSION.length).toLong()
                    val sessionId = cookie.value.toLong()
                    sessions[queueId] = sessionId
                }
            }
        // get sessions from parameters
        for ((name, value) in req.parameterMap) {
            if (name.startsWith(SESSION)) {
                val queueId = name.substring(SESSION.length).toLong()
                val sessionId = value[0].toLong()
                sessions[queueId] = sessionId
            }
            if (name.startsWith(NEW_SESSION)) {
                val queueId = name.substring(NEW_SESSION.length).toLong()
                sessions[queueId] = null
            }
        }
        return sessions
    }

    private fun addSessionCookies(resp: HttpServletResponse, sessions: Map<Long, Long>) {
        for ((key, value) in sessions) {
            resp.addCookie(Cookie(SESSION + key, "" + value))
        }
    }

    private fun parseMessageItems(input: BufferedReader, now: Long) =
        generateSequence { input.readLine() }
            .map { MessageItem(it, now) }
            .groupBy { it.queueId }
}
