package org.vsegda.servlet;

import org.vsegda.dao.DataStreamDAO;
import org.vsegda.dao.Factory;
import org.vsegda.data.DataArchive;
import org.vsegda.data.DataItem;
import org.vsegda.data.DataStream;
import org.vsegda.data.DataStreamMode;
import org.vsegda.util.Alert;

import javax.jdo.PersistenceManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.logging.Logger;

/**
 * @author Roman Elizarov
 */
public class DataCheckCronServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(DataCheckCronServlet.class.getName());

    @SuppressWarnings({"unchecked"})
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        log.info("Checking data items for timeouts and archival needs");
        long now = System.currentTimeMillis();
        PersistenceManager pm = Factory.getPersistenceManager();
        try {
            Collection<DataStream> streams = (Collection<DataStream>)pm.newQuery(DataStream.class).execute();
            for (DataStream stream : streams) {
                // check for data update timeout
                if (stream.getAlertTimeout() != null && stream.getLastItemKey() != null) {
                    DataItem item = (DataItem)pm.getObjectById(stream.getLastItemKey());
                    if (now - item.getTimeMillis() > stream.getAlertTimeout())
                        Alert.sendAlertEmail("" + stream.getStreamId(), "Data update timeout");
                }
                // check for archive or purge (find non-recent items)
                long threshold = System.currentTimeMillis() - DataArchive.RECENT_TIME_INTERVAL - 2 * DataArchive.ARCHIVE_INTERVAL;
                if (stream.getMode() != DataStreamMode.LAST) {
                    DataItem firstItem = DataStreamDAO.getOrFindFirstItem(pm, stream);
                    if (firstItem != null && firstItem.getTimeMillis() < threshold) // need to archive
                        DataArchiveTaskServlet.enqueueDataArchiveTask(stream.getStreamId());
                }
            }
        } finally {
            pm.close();
        }
    }
}
