package org.vsegda.dao;

import org.vsegda.data.MessageItem;
import org.vsegda.data.MessageQueue;
import org.vsegda.factory.Factory;
import org.vsegda.util.IdList;
import org.vsegda.util.RequestUtil;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.Query;
import javax.servlet.ServletRequest;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Roman Elizarov
 */
public class MessageRequest {
	private IdList id;
    private boolean take;
	private long index;
	private int first;
	private int last = 100; // last 100 items by default

    public MessageRequest(ServletRequest req, boolean post) {
        RequestUtil.populate(this, req);
        if (id != null && id.isSingleton() && post)
            take = true; // force take on POST request if "id" is set
		if (index != 0 && (id == null || !id.isSingleton()))
            throw new IllegalArgumentException("cannot specify index without a singleton id");
        if (index != 0 && !take)
            throw new IllegalArgumentException("cannot specify index without take");
        if (take && (id == null || !id.isSingleton()))
            throw new IllegalArgumentException("cannot specify take without a singleton id");
        if (take && first != 0)
            throw new IllegalArgumentException("cannot specify take with first");
	}

    @SuppressWarnings({"unchecked"})
    public Collection<MessageItem> query() {
        Collection<MessageItem> items = new ArrayList<MessageItem>();
        if (id == null) {
            Query query = Factory.getPM().newQuery(MessageQueue.class);
            query.setOrdering("key asc");
            query.setRange(first, first + last);
            for (MessageQueue queue : (Collection<MessageQueue>)query.execute())
                try {
                    items.add(Factory.getPM().getObjectById(MessageItem.class, MessageItem.createKey(queue.getQueueId(), queue.getLastPostIndex())));
                } catch (JDOObjectNotFoundException e) {
                    // just ignore
                }
        } else {
            for (String code : this.id) {
                long id = MessageQueueDAO.resolveQueueCode(code);
                Query query = Factory.getPM().newQuery(MessageItem.class);
                if (take) {
                    MessageQueue queue = Factory.getPM().getObjectById(MessageQueue.class, MessageQueue.createKey(id));
                    index = Math.max(index, queue.getLastGetIndex());
                    queue.setLastGetIndex(index);
                    query.setFilter("queueId == id && messageIndex > index");
                    query.declareParameters("long id, long index");
                    query.setOrdering("messageIndex asc");
                    query.setRange(first, first + last);
                    items.addAll((Collection<MessageItem>)query.execute(id, index));
                } else {
                    query.setFilter("queueId == id");
                    query.declareParameters("long id");
                    query.setOrdering("messageIndex desc");
                    query.setRange(first, first + last);
                    items.addAll((Collection<MessageItem>)query.execute(id));
                }
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

    public boolean isTake() {
        return take;
    }

    public void setTake(boolean take) {
        this.take = take;
    }

    public long getIndex() {
        return index;
    }

    public void setIndex(long index) {
        this.index = index;
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