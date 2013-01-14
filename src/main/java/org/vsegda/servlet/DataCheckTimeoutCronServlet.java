package org.vsegda.servlet;

import org.vsegda.data.DataItem;
import org.vsegda.data.DataStream;
import org.vsegda.service.AlertService;
import org.vsegda.service.DataItemService;
import org.vsegda.service.DataStreamService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * @author Roman Elizarov
 */
public class DataCheckTimeoutCronServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(DataCheckTimeoutCronServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        log.info("Checking data items for timeouts");
        long startTimeMillis = System.currentTimeMillis();
        for (DataStream stream : DataStreamService.getDataStreams()) {
            // check for data update timeout
            if (stream.getAlertTimeout() != null) {
                DataItem item = DataItemService.getLastDataItem(stream);
                if (startTimeMillis - item.getTimeMillis() > stream.getAlertTimeout())
                    AlertService.sendAlertEmail(stream.getCode(), "Data update timeout");
            }
        }
        log.info("Done in " + (System.currentTimeMillis() - startTimeMillis) + " ms");
    }
}
