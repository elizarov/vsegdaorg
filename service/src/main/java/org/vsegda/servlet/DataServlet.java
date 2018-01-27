package org.vsegda.servlet;

import org.vsegda.data.DataItem;
import org.vsegda.request.DataRequest;
import org.vsegda.service.DataItemService;
import org.vsegda.service.DataStreamService;

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
        for (DataItem item : dataRequest.queryList())
            out.println(item.toString());
    }

    @SuppressWarnings({"unchecked"})
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<DataItem> items = parseDataItems(req.getReader());
        // resolve all stream tags
        for (DataItem item : items)
            item.setStream(DataStreamService.resolveDataStream(item.getStream()));
        // persist all items
        DataItemService.INSTANCE.addDataItems(items);
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
