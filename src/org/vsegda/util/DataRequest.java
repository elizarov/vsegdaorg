package org.vsegda.util;

import org.apache.commons.beanutils.BeanUtils;
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
	private IdList id;
	private int first;
	private int last = 1000; // last 1000 items by default

	public DataRequest(ServletRequest req) {
        RequestUtil.populate(this, req);
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
        Collection<DataItem> items = new ArrayList<DataItem>();
        if (id == null) {
            Query query = pm.newQuery(DataStream.class);
            query.setOrdering("streamId asc");
            query.setRange(first, first + last);
            for (DataStream stream : (Collection<DataStream>)query.execute())
                items.add(pm.getObjectById(DataItem.class, stream.getLastItemKey()));
        } else {
            for (long id : this.id) {
                Query query = pm.newQuery(DataItem.class);
                query.setFilter("streamId == id");
                query.declareParameters("int id");
                query.setOrdering("timeMillis desc");
                query.setRange(first, first + last);
                items.addAll((Collection<DataItem>)query.execute(id));
            }
        }
        return items;
    }

    public IdList getId() {
        return id;
    }

    public void setId(IdList id) {
        this.id = id;
    }

    public int getFirst() {
        return first;
    }

    public void setFirst(int first) {
        this.first = first;
    }

    public int getLast() {
        return last;
    }

    public void setLast(int last) {
        this.last = last;
    }
}
