package org.vsegda.servlet;

import org.vsegda.dao.DataItemDAO;
import org.vsegda.dao.DataRequest;
import org.vsegda.dao.DataStreamDAO;
import org.vsegda.data.DataItem;
import org.vsegda.data.DataStream;
import org.vsegda.shared.DataStreamMode;

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
        for (DataItem item : dataRequest.queryListDescending())
            out.println(item.toString());
    }

    @SuppressWarnings({"unchecked"})
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<DataItem> items = parseDataItems(req.getReader());
        // resolve all stream tags
        for (DataItem item : items)
            item.setStream(DataStreamDAO.resolveDataStream(item.getStream()));
        // persist all items
        DataItemDAO.persistDataItems(items);
        // update all streams
        for (DataItem item : items) {
            DataStream stream = item.getStream();
            // update last item key
            if (stream.getLastItemKey() != null) {
                DataItem lastItem = DataItemDAO.getDataItemByKey(stream.getLastItemKey());
                if (lastItem == null)
                    stream.setLastItemKey(item.getKey()); // something wrong -- update to cur item
                else {
                    // remove last item in LAST mode
                    if (stream.getMode() == DataStreamMode.LAST) {
                        stream.setLastItemKey(null);
                        if (stream.getFirstItemKey() == lastItem.getKey())
                            stream.setFirstItemKey(null);
                        DataItemDAO.deleteDataItem(lastItem);
                        stream.setLastItemKey(item.getKey());
                    } else if (item.getTimeMillis() >= lastItem.getTimeMillis())
                        stream.setLastItemKey(item.getKey());
                }
            } else
                stream.setLastItemKey(item.getKey());
            // update first item key
            if (stream.getFirstItemKey() != null) {
                DataItem firstItem = DataItemDAO.getDataItemByKey(stream.getFirstItemKey());
                if (firstItem == null)
                    stream.setFirstItemKey(null); // something wrong -- clear it
                else if (item.getTimeMillis() < firstItem.getTimeMillis())
                    stream.setFirstItemKey(item.getKey());
            }
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
