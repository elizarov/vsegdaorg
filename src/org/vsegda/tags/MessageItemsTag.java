package org.vsegda.tags;

import org.vsegda.Factory;
import org.vsegda.MessageRequest;
import org.vsegda.data.MessageItem;

import javax.el.ExpressionFactory;
import javax.jdo.PersistenceManager;
import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;
import java.util.Map;

/**
 * @author Roman Elizarov
 */
public class MessageItemsTag extends SimpleTagSupport {
    @SuppressWarnings({"unchecked"})
    @Override
    public void doTag() throws JspException, IOException {
        PageContext ctx = (PageContext)getJspContext();
        MessageRequest req = new MessageRequest(ctx.getRequest());
        ctx.setAttribute("req", req);
        PersistenceManager pm = Factory.getPersistenceManager();
        try {
            for (MessageItem item : req.query(pm)) {
                ctx.setAttribute("item", item);
                getJspBody().invoke(null);
            }
        } finally {
            pm.close();
        }
    }
}
