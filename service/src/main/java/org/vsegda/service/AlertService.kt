package org.vsegda.service

import org.vsegda.util.*
import java.io.*
import java.util.*
import javax.mail.*
import javax.mail.internet.*

object AlertService {
    fun sendAlertEmail(code: String?, text: String) {
        val session = Session.getDefaultInstance(Properties(), null)
        var subject = "ALERT: " + text
        var body = text
        if (code != null) {
            val stream = DataStreamService.resolveDataStreamByCode(code)
            subject += ": " + (stream?.nameOrCode ?: code)
            body += "\nhttp://apps.vsegda.org/dataPlot?id=" + code
        }
        val from = InternetAddress("vsdacha@gmail.com", "Dacha")
        val to = InternetAddress("elizarov@gmail.com", "Roman Elizarov")
        log.info("Sending email from=$from to=$to subject=$subject")
        Transport.send(MimeMessage(session).apply {
            setFrom(from)
            addRecipient(Message.RecipientType.TO, to)
            setSubject(subject)
            setText(body)
        })
    }
}

