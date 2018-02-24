package org.vsegda.service;

import org.vsegda.data.DataStream;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.Properties;

/**
 * @author Roman Elizarov
 */
public class AlertService {
    public static void sendAlertEmail(String code, String text) throws IOException {
        Session session = Session.getDefaultInstance(new Properties(), null);
        String subject = "ALERT: " + text;
        String body = text;
        if (code != null) {
            DataStream stream = DataStreamService.INSTANCE.resolveDataStreamByCode(code);
            subject += ": " + (stream != null ? stream.getNameOrCode() : code);
            body += "\nhttp://apps.vsegda.org/dataPlot?id=" + code;
        }
        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress("vsdacha@gmail.com", "Dacha"));
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress("elizarov@gmail.com", "Roman Elizarov"));
            msg.setSubject(subject);
            msg.setText(body);
            Transport.send(msg);
        } catch (MessagingException e) {
            throw new IOException(e);
        }
    }
}
