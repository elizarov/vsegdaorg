package org.vsegda.tags;

import org.vsegda.request.DataRequest;
import org.vsegda.util.TimeInstant;

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
        if (dataRequest.hasNavigation()) {
            TimeInstant last = dataRequest.getTo();
            dataRequest.setTo((last == null ? TimeInstant.Companion.now() : last).minus(dataRequest.getSpan()));
            ctx.getOut().println("[<a href=\"?" + dataRequest.getQueryString() + "\">Prev</a>]");
            if (last != null) {
                TimeInstant nextLast = last.plus(dataRequest.getSpan());
                dataRequest.setTo(nextLast.isNowOrFuture() ? null : nextLast);
                ctx.getOut().println("[<a href=\"?" + dataRequest.getQueryString() + "\">Next</a>]");
            }
        }
    }
}
