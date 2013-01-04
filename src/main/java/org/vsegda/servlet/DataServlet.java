package org.vsegda.servlet;

import org.vsegda.dao.DataStreamDAO;
import org.vsegda.data.DataItem;
import org.vsegda.data.DataStream;
import org.vsegda.util.DataRequest;
import org.vsegda.util.Factory;

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
import java.util.Map;

/**
 * @author Roman Elizarov
 */
public class DataServlet extends HttpServlet {
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
            pm.makePersistentAll(items);
            for (DataItem item : items) {
                DataStream stream = getOrCreateDataStream(pm, item.getStreamId());
                // update last item key
                if (stream.getLastItemKey() != null) {
                    DataItem lastItem = pm.getObjectById(DataItem.class, stream.getLastItemKey());
                    if (item.getTimeMillis() >= lastItem.getTimeMillis())
                        stream.setLastItemKey(item.getKey());
                } else
                    stream.setLastItemKey(item.getKey());
                // update first item key
                if (stream.getFirstItemKey() != null) {
                    DataItem firstItem = pm.getObjectById(DataItem.class, stream.getFirstItemKey());
                    if (item.getTimeMillis() < firstItem.getTimeMillis())
                        stream.setFirstItemKey(item.getKey());
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
