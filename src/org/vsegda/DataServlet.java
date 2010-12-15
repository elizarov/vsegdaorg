package org.vsegda;

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
import java.util.*;

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
        Map<Long, DataItem> items = parseDataItems(req.getReader());
        PersistenceManager pm = Factory.getPersistenceManager();
        try {
            pm.makePersistentAll(items.values());
            for (Long id : items.keySet()) {
                DataStream stream = getOrCreateDataStream(pm, id);
                stream.setLastItemKey(items.get(id).getKey());
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

    private Map<Long, DataItem> parseDataItems(BufferedReader in) throws IOException {
        String line;
        Map<Long, DataItem> items = new HashMap<Long, DataItem>();
        long now = System.currentTimeMillis();
        while ((line = in.readLine()) != null) {
            DataItem item = new DataItem(line, now);
            items.put(item.getStreamId(), item);
        }
        return items;
    }
}
