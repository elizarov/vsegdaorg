package org.vsegda.request;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;
import org.vsegda.data.MessageItem;
import org.vsegda.factory.DS;
import org.vsegda.storage.MessageStorage;
import org.vsegda.util.IdList;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author Roman Elizarov
 */
public class MessageRequest extends AbstractRequest {
    private static final Logger log = Logger.getLogger(MessageRequest.class.getName());

	private IdList id;
    private boolean take;
	private long index;
    private int last = 100; // last 100 items by default
	private int first;

    public MessageRequest(HttpServletRequest req, boolean post) {
        init(req);
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

    public List<MessageItem> query() {
        log.info("Performing message query " + this);
        long startTimeMillis = System.currentTimeMillis();
        List<MessageItem> items = new ArrayList<MessageItem>();
        if (id == null) {
            Query query = new Query("MessageQueue");
            query.addSort("__key__", Query.SortDirection.ASCENDING);
            Iterable<Entity> entities = DS.prepare(query).asIterable(
                    FetchOptions.Builder.withOffset(first).limit(last).chunkSize(last));
            for (Entity entity : entities) {
                items.add(MessageStorage.toMessageItem(entity));
            }
        } else {
            throw new UnsupportedOperationException(); // todo
//            for (String code : this.id) {
//                long id = MessageQueueService.resolveQueueCode(code);
//                Query query = DS.instance().newQuery(MessageItem.class);
//                if (take) {
//                    MessageQueue queue = DS.instance().getObjectById(MessageQueue.class, MessageQueue.createKey(id));
//                    index = Math.max(index, queue.getLastGetIndex());
//                    queue.setLastGetIndex(index);
//                    query.setFilter("queueId == id && messageIndex > index");
//                    query.declareParameters("long id, long index");
//                    query.setOrdering("messageIndex asc");
//                    query.setRange(first, first + last);
//                    items.addAll((Collection<MessageItem>)query.execute(id, index));
//                } else {
//                    query.setFilter("queueId == id");
//                    query.declareParameters("long id");
//                    query.setOrdering("messageIndex desc");
//                    query.setRange(first, first + last);
//                    items.addAll((Collection<MessageItem>)query.execute(id));
//                }
//            }
        }
        log.info("Completed message query in " + (System.currentTimeMillis() - startTimeMillis) + " ms");
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