package org.vsegda.servlet;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import org.vsegda.service.DataItemService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * @author Roman Elizarov
 */
public class DataCacheRefreshTaskServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(DataCacheRefreshTaskServlet.class.getName());

    public static void enqueueTask(long streamId) {
        log.info("Enqueueing data cache refresh task for id=" + streamId);
        Queue queue = QueueFactory.getQueue("dataCacheRefreshTaskQueue");
        queue.add(TaskOptions.Builder
                .withUrl("/task/dataCacheRefresh")
                .param("id", String.valueOf(streamId)));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long streamId = Long.parseLong(req.getParameter("id"));
        log.info("Refreshing cache for streamId=" + streamId);
        long startTimeMillis = System.currentTimeMillis();
        DataItemService.INSTANCE.refreshCache(streamId);
        log.info("Completed cache refresh in " + (System.currentTimeMillis() - startTimeMillis) + " ms");

    }
}
