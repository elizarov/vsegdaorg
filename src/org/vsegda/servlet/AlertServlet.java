package org.vsegda.servlet;

import org.vsegda.util.Alert;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

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
		Alert.sendAlertEmail(id, text);
		
	}
}
