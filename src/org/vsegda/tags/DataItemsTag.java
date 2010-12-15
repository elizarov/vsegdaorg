package org.vsegda.tags;

import org.vsegda.DataRequest;
import org.vsegda.Factory;
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
        DataRequest req = new DataRequest(ctx.getRequest());
        ctx.setAttribute("req", req);
        PersistenceManager pm = Factory.getPersistenceManager();
        try {
            for (DataItem item : req.query(pm)) {
                ctx.setAttribute("item", item);
                getJspBody().invoke(null);
            }
        } finally {
            pm.close();
        }
    }
}
