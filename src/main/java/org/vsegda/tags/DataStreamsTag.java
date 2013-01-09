package org.vsegda.tags;

import org.vsegda.dao.DataRequest;
import org.vsegda.data.DataItem;
import org.vsegda.data.DataStream;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Roman Elizarov
 */
public class DataStreamsTag extends SimpleTagSupport {
    @SuppressWarnings({"unchecked"})
    @Override
    public void doTag() throws JspException, IOException {
        PageContext ctx = (PageContext)getJspContext();
        List<DataItem> list = new DataRequest(ctx.getRequest()).query();
        Map<DataStream, List<DataItem>> streamItemsMap = new HashMap<DataStream, List<DataItem>>();
        for (DataItem item : list) {
            List<DataItem> items = streamItemsMap.get(item.getStream());
            if (items == null)
                streamItemsMap.put(item.getStream(), items = new ArrayList<DataItem>());
            items.add(item);
        }
        ctx.setAttribute("streamItemsMap", streamItemsMap);
        for (DataStream stream : streamItemsMap.keySet()) {
            ctx.setAttribute("stream", stream);
            getJspBody().invoke(null);
        }
    }
}
