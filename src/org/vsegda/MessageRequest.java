package org.vsegda;

import org.vsegda.data.MessageItem;
import org.vsegda.data.MessageQueue;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.ServletRequest;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Roman Elizarov
 */
public class MessageRequest {
	private Long id;
    private boolean all;
	private long index;
	private int first;
	private int last = 100; // last 100 items by default

    public MessageRequest(ServletRequest req) {
        String idString = req.getParameter("id");
		if (idString != null)
            try {
                id = Long.parseLong(idString);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("id must be integer", e);
            }
        all = req.getParameter("all") != null;
        if (all && id == null)
            throw new IllegalArgumentException("cannot specify all without id");
        String indexString = req.getParameter("index");
		if (indexString != null) {
            if (id == null)
                throw new IllegalArgumentException("cannot specify index without id");
            if (all)
                throw new IllegalArgumentException("cannot specify index with all");
			try {
				index = Long.parseLong(indexString);
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("index must be integer", e);
			}
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


    @SuppressWarnings({"unchecked"})
    public Collection<MessageItem> query(PersistenceManager pm) {
        if (id == null) {
            Query query = pm.newQuery(MessageQueue.class);
            query.setOrdering("key asc");
            query.setRange(first, first + last);
            Collection<MessageItem> items = new ArrayList<MessageItem>();
            for (MessageQueue queue : (Collection<MessageQueue>)query.execute())
                try {
                    items.add(pm.getObjectById(MessageItem.class, MessageItem.createKey(queue.getQueueId(), queue.getLastPostIndex())));
                } catch (JDOObjectNotFoundException e) {
                    // just ignore
                }
            return items;
        } else {
            Query query = pm.newQuery(MessageItem.class);
            if (all) {
                query.setFilter("queueId == id");
                query.declareParameters("long id");
                query.setOrdering("messageIndex desc");
                query.setRange(first, first + last);
                return (Collection<MessageItem>)query.execute(id);
            } else {
                MessageQueue queue = pm.getObjectById(MessageQueue.class, MessageQueue.createKey(id));
                index = Math.max(index, queue.getLastGetIndex());
                queue.setLastGetIndex(index);
                query.setFilter("queueId == id && messageIndex > index");
                query.declareParameters("long id, long index");
                query.setOrdering("messageIndex asc");
                query.setRange(first, first + last);
                return (Collection<MessageItem>)query.execute(id, index);
            }
        }
    }

    public boolean isPollRequest() {
        return id != null && !all && first == 0;
    }

    public Long getId() {
        return id;
    }

}