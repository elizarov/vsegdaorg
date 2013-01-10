package org.vsegda.tags;

import org.vsegda.request.DataRequest;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;

/**
 * @author Roman Elizarov
 */
public class DataNavigationTag extends SimpleTagSupport {
    @SuppressWarnings({"unchecked"})
    @Override
    public void doTag() throws JspException, IOException {
        PageContext ctx = (PageContext) getJspContext();
        DataRequest dataRequest = (DataRequest) ctx.getAttribute("dataRequest");
        int first = dataRequest.getFirst();
        dataRequest.setSince(null); // reset since for navigation
        if (dataRequest.hasMore()) {
            ctx.getOut().println("[<a href=\"?" + dataRequest.getQueryString() + "\">More</a>]");
        } else if (dataRequest.hasNext()) {
            dataRequest.setFirst(first + dataRequest.getLast());
            ctx.getOut().println("[<a href=\"?" + dataRequest.getQueryString() + "\">Next</a>]");
        }
        if (first > 0) {
            dataRequest.setFirst(Math.max(0, first - dataRequest.getLast()));
            ctx.getOut().println("[<a href=\"?" + dataRequest.getQueryString() + "\">Prev</a>]");
        }
    }
}
