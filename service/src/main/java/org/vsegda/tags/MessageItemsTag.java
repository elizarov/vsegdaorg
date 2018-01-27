package org.vsegda.tags;

import org.vsegda.data.MessageItem;
import org.vsegda.request.MessageRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;

/**
 * @author Roman Elizarov
 */
public class MessageItemsTag extends SimpleTagSupport {
    @SuppressWarnings({"unchecked"})
    @Override
    public void doTag() throws JspException, IOException {
        PageContext ctx = (PageContext)getJspContext();
        for (MessageItem item : new MessageRequest((HttpServletRequest) ctx.getRequest(), false).query()) {
            ctx.setAttribute("item", item);
            getJspBody().invoke(null);
        }
    }
}
