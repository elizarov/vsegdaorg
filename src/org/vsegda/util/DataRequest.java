package org.vsegda.util;

import org.vsegda.data.DataItem;
import org.vsegda.data.DataStream;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.ServletRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Roman Elizarov
 */
public class DataRequest {
	private Long id;
	private int first;
	private int last = 100; // last 100 items by default

	public DataRequest(ServletRequest req) {
		String idString = req.getParameter("id");
		if (idString != null)
            try {
                id = Long.parseLong(idString);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("id must be integer", e);
            }
		String firstString = req.getParameter("first");
		if (firstString != null)
			try {
				first = Integer.parseInt(firstString);
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("first must be integer", e);
			}
		String lastString = req.getParameter("last");
		if (lastString != null)
			try {
				last = Integer.parseInt(lastString);
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("last must be integer", e);
			}
	}

    public List<DataItem> query() {
        PersistenceManager pm = Factory.getPersistenceManager();
        try {
            return new ArrayList<DataItem>(query(pm));
        } finally {
            pm.close();
        }
    }

    @SuppressWarnings({"unchecked"})
    public Collection<DataItem> query(PersistenceManager pm) {
        if (id == null) {
            Query query = pm.newQuery(DataStream.class);
            query.setOrdering("streamId asc");
            query.setRange(first, first + last);
            Collection<DataItem> items = new ArrayList<DataItem>();
            for (DataStream stream : (Collection<DataStream>)query.execute())
                items.add(pm.getObjectById(DataItem.class, stream.getLastItemKey()));
            return items;
        } else {
            Query query = pm.newQuery(DataItem.class);
            query.setFilter("streamId == id");
            query.declareParameters("int id");
            query.setOrdering("timeMillis desc");
            query.setRange(first, first + last);
            return (Collection<DataItem>)query.execute(id);
        }
    }

    public Long getId() {
        return id;
    }
}
