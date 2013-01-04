package org.vsegda.tags;

import org.vsegda.util.MessageRequest;
import org.vsegda.data.MessageItem;

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
        for (MessageItem item : new MessageRequest(ctx.getRequest(), false).query()) {
            ctx.setAttribute("item", item);
            getJspBody().invoke(null);
        }
    }
}
