package org.vsegda.servlet;

import org.vsegda.dao.DataStreamDAO;
import org.vsegda.data.DataStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * @author Roman Elizarov
 */
public class DataCacheRefreshCronServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(DataCacheRefreshCronServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        log.info("Scheduling cache updates");
        long startTimeMillis = System.currentTimeMillis();
        for (DataStream stream : DataStreamDAO.getAllDataStreams()) {
            // enqueue cache update task
            DataCacheRefreshTaskServlet.enqueueTask(stream.getStreamId());
        }
        log.info("Done in " + (System.currentTimeMillis() - startTimeMillis) + " ms");
    }

}
