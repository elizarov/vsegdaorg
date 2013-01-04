package org.vsegda.servlet;

import org.vsegda.dao.DataRequest;
import org.vsegda.dao.DataStreamDAO;
import org.vsegda.dao.Factory;
import org.vsegda.data.DataItem;
import org.vsegda.data.DataStream;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author Roman Elizarov
 */
public class DataServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(DataServlet.class.getName());

    @SuppressWarnings({"unchecked"})
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        DataRequest dataRequest = new DataRequest(req);
        resp.setContentType("text/csv");
        resp.setCharacterEncoding("UTF-8");
        ServletOutputStream out = resp.getOutputStream();
        PersistenceManager pm = Factory.getPersistenceManager();
        try {
            for (DataItem item : dataRequest.query(pm)) {
                out.println(item.toString());
            }
        } finally {
            pm.close();
        }
    }

    @SuppressWarnings({"unchecked"})
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<DataItem> items = parseDataItems(req.getReader());
        PersistenceManager pm = Factory.getPersistenceManager();
        try {
            // resolve all stream tags
            for (DataItem item : items) {
                if (item.getStreamId() == 0 && item.getStreamTag() != null)
                    item.setStreamId(DataStreamDAO.resolveStreamCode(pm, item.getStreamTag()));
            }
            // persist all items
            pm.makePersistentAll(items);
            // update all streams
            for (DataItem item : items) {
                DataStream stream = getOrCreateDataStream(pm, item.getStreamId());
                // update last item key
                if (stream.getLastItemKey() != null) {
                    try {
                        DataItem lastItem = pm.getObjectById(DataItem.class, stream.getLastItemKey());
                        if (item.getTimeMillis() >= lastItem.getTimeMillis())
                            stream.setLastItemKey(item.getKey());
                    } catch (JDOObjectNotFoundException e) {
                        log.warning("Cannot find last DataItem from stream with key=" + stream.getLastItemKey());
                        // something wrong -- fallback to set
                        stream.setLastItemKey(item.getKey());
                    }
                } else
                    stream.setLastItemKey(item.getKey());
                // update first item key
                if (stream.getFirstItemKey() != null) {
                    try {
                        DataItem firstItem = pm.getObjectById(DataItem.class, stream.getFirstItemKey());
                        if (item.getTimeMillis() < firstItem.getTimeMillis())
                            stream.setFirstItemKey(item.getKey());
                    } catch (JDOObjectNotFoundException e) {
                        log.warning("Cannot find first DataItem from stream with key=" + stream.getFirstItemKey());
                        // something wrong -- fallback to set
                        stream.setFirstItemKey(DataStreamDAO.findFistItemKey(pm, stream.getStreamId()));
                    }
                } else
                    DataStreamDAO.ensureFirstItemKey(pm, stream);
            }
        } finally {
            pm.close();
        }
    }

    private DataStream getOrCreateDataStream(PersistenceManager pm, Long id) {
        try {
            return pm.getObjectById(DataStream.class, id);
        } catch (JDOObjectNotFoundException e) {
            return pm.makePersistent(new DataStream(id));
        }
    }

    private List<DataItem> parseDataItems(BufferedReader in) throws IOException {
        String line;
        long now = System.currentTimeMillis();
        List<DataItem> items = new ArrayList<DataItem>();
        while ((line = in.readLine()) != null)
            items.add(new DataItem(line, now));
        return items;
    }
}
