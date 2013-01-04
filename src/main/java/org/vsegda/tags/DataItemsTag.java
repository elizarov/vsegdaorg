package org.vsegda.tags;

import org.vsegda.util.DataRequest;
import org.vsegda.util.Factory;
import org.vsegda.data.DataItem;

import javax.jdo.PersistenceManager;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;

/**
 * @author Roman Elizarov
 */
public class DataItemsTag extends SimpleTagSupport {
    @SuppressWarnings({"unchecked"})
    @Override
    public void doTag() throws JspException, IOException {
        PageContext ctx = (PageContext)getJspContext();
        for (DataItem item : new DataRequest(ctx.getRequest()).query()) {
            ctx.setAttribute("item", item);
            getJspBody().invoke(null);
        }
    }
}
