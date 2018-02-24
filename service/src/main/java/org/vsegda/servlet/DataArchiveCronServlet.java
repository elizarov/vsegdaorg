package org.vsegda.servlet;

import org.vsegda.data.DataArchive;
import org.vsegda.data.DataItem;
import org.vsegda.data.DataStream;
import org.vsegda.service.DataItemService;
import org.vsegda.service.DataStreamService;
import org.vsegda.shared.DataStreamMode;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * @author Roman Elizarov
 */
public class DataArchiveCronServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(DataArchiveCronServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        log.info("Checking data items for archival needs");
        long startTimeMillis = System.currentTimeMillis();
        for (DataStream stream : DataStreamService.INSTANCE.getDataStreams()) {
            // check for archive or purge (find non-recent items)
            DataItem firstItem = DataItemService.INSTANCE.getFirstDataItem(stream);
            DataItem lastItem = DataItemService.INSTANCE.getLastDataItem(stream);
            log.fine("First item is " + firstItem + "; last item is " + lastItem);
            long threshold =
                    stream.getMode() == DataStreamMode.LAST ? lastItem.getTimeMillis() :
                    System.currentTimeMillis() - DataArchive.RECENT_TIME_INTERVAL - DataArchive.ARCHIVE_INTERVAL;
            if (firstItem != null && firstItem.getTimeMillis() < threshold &&
                    !firstItem.getKey().equals(lastItem.getKey()))
            {
                // need to archive
                DataArchiveTaskServlet.enqueueTask(stream.getStreamId());
            }
        }
        log.info("Done in " + (System.currentTimeMillis() - startTimeMillis) + " ms");
    }
}
