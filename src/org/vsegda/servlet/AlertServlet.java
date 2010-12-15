package org.vsegda.servlet;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Properties;

/**
 * @author Roman Elizarov
 */
public class AlertServlet extends HttpServlet {
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String text = req.getParameter("text");
		if (text == null)
			throw new ServletException("text parameter required");
		String id = req.getParameter("id");
		sendAlertEmail(id, text);
		
	}

	private void sendAlertEmail(String id, String text) throws IOException {
		Session session = Session.getDefaultInstance(new Properties(), null);
		String subject = "ALERT: " + text;
		String body = text;
		if (id != null) {
			subject += " (" + id + ")";
			body += "\nhttp://apps.vsegda.org/graph.jsp?id=" + id;
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
