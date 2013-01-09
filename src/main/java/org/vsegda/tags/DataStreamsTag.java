package org.vsegda.tags;

import org.vsegda.dao.DataRequest;
import org.vsegda.data.DataItem;
import org.vsegda.data.DataStream;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;
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
        Map<DataStream, List<DataItem>> streamItemsMap = new DataRequest(ctx.getRequest()).queryMapAscending();
        ctx.setAttribute("streamItemsMap", streamItemsMap);
        for (DataStream stream : streamItemsMap.keySet()) {
            ctx.setAttribute("stream", stream);
            getJspBody().invoke(null);
        }
    }
}
