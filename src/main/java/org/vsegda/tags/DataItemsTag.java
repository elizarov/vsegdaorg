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
public class DataItemsTag extends SimpleTagSupport {
    @SuppressWarnings({"unchecked"})
    @Override
    public void doTag() throws JspException, IOException {
        PageContext ctx = (PageContext) getJspContext();
        Map<DataStream, List<DataItem>> streamItemsMap = (Map<DataStream, List<DataItem>>) ctx.getAttribute("streamItemsMap");
        DataStream stream = (DataStream) ctx.getAttribute("stream");
        List<DataItem> list;
        if (streamItemsMap != null && stream != null)
            list = streamItemsMap.get(stream);
        else
            list = new DataRequest(ctx.getRequest()).queryList();
        for (DataItem item : list) {
            ctx.setAttribute("item", item);
            getJspBody().invoke(null);
        }
    }
}
