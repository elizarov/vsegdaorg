package org.vsegda.tags;

import org.vsegda.data.DataItem;
import org.vsegda.data.DataStream;
import org.vsegda.request.DataRequest;

import javax.servlet.http.HttpServletRequest;
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
        DataRequest dataRequest = new DataRequest((HttpServletRequest) ctx.getRequest());
        ctx.setAttribute("dataRequest", dataRequest);
        Map<DataStream, List<DataItem>> streamItemsMap = dataRequest.queryMap();
        ctx.setAttribute("streamItemsMap", streamItemsMap);
        for (DataStream stream : streamItemsMap.keySet()) {
            ctx.setAttribute("stream", stream);
            getJspBody().invoke(null);
        }
    }
}
