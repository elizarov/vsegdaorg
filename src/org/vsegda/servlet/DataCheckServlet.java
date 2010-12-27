package org.vsegda.servlet;

import org.vsegda.data.DataItem;
import org.vsegda.data.DataStream;
import org.vsegda.util.Alert;
import org.vsegda.util.Factory;

import javax.jdo.PersistenceManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;

/**
 * @author Roman Elizarov
 */
public class DataCheckServlet extends HttpServlet {
    @SuppressWarnings({"unchecked"})
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        long now = System.currentTimeMillis();
        PersistenceManager pm = Factory.getPersistenceManager();
        try {
            Collection<DataStream> streams = (Collection<DataStream>)pm.newQuery(DataStream.class).execute();
            for (DataStream stream : streams) {
                if (stream.getAlertTimeout() != null && stream.getLastItemKey() != null) {
                    DataItem item = (DataItem)pm.getObjectById(stream.getLastItemKey());
                    if (now - item.getTimeMillis() > stream.getAlertTimeout())
                        Alert.sendAlertEmail("" + stream.getStreamId(), "Data update timeout");
                }
            }
        } finally {
            pm.close();
        }
    }
}
