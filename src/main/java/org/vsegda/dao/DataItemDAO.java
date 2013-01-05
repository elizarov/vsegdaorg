package org.vsegda.dao;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.memcache.jsr107cache.GCacheFactory;
import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheException;
import net.sf.jsr107cache.CacheFactory;
import net.sf.jsr107cache.CacheManager;
import org.vsegda.data.DataItem;
import org.vsegda.data.DataStream;
import org.vsegda.factory.Factory;
import org.vsegda.shared.DataStreamMode;
import org.vsegda.util.TimeInstant;
import org.vsegda.util.TimeUtil;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.Query;
import java.io.Serializable;
import java.util.*;
import java.util.logging.Logger;

/**
 * @author Roman Elizarov
 */
public class DataItemDAO {
    private static final Logger log = Logger.getLogger(DataItemDAO.class.getName());

    private static final int LIST_CACHE_EXPIRATION_MILLIS = (int)(15 * TimeUtil.MINUTE);
    private static final int MAX_LIST_SIZE = 4000;
    private static final int TRIM_LIST_SIZE = 3000;

    private static final Comparator<DataItem> ORDER_BY_TIME = new Comparator<DataItem>() {
        @Override
        public int compare(DataItem o1, DataItem o2) {
            return o1.getTimeMillis() < o2.getTimeMillis() ? -1 : o1.getTimeMillis() > o2.getTimeMillis() ? 1 : 0;
        }
    };

    private static final Cache ITEM_BY_KEY_CACHE;
    private static final Cache LIST_CACHE;

    static {
        try {
            CacheFactory cf = CacheManager.getInstance().getCacheFactory();
            ITEM_BY_KEY_CACHE = cf.createCache(Collections.emptyMap());
            Map<String, Object> props = new HashMap<String, Object>();
            props.put(GCacheFactory.EXPIRATION_DELTA_MILLIS, LIST_CACHE_EXPIRATION_MILLIS);
            LIST_CACHE = cf.createCache(props);
        } catch (CacheException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private DataItemDAO() {} // do not create

    public static void persistDataItem(DataItem dataItem) {
        Factory.getPM().makePersistent(dataItem);
        ITEM_BY_KEY_CACHE.put(dataItem.getKey(), dataItem);
        ListEntry entry = (ListEntry) LIST_CACHE.get(dataItem.getStreamId());
        if (entry != null) {
            entry.items.add(dataItem);
            int size = entry.items.size();
            if (size >= 2 && ORDER_BY_TIME.compare(entry.items.get(size - 1), entry.items.get(size - 2)) < 0)
                Collections.sort(entry.items, ORDER_BY_TIME);
            if (size > MAX_LIST_SIZE)
                entry.items.subList(0, size - TRIM_LIST_SIZE).clear();
            LIST_CACHE.put(dataItem.getStreamId(), entry);
        }
    }

    public static void deleteDataItem(DataItem dataItem) {
        ITEM_BY_KEY_CACHE.remove(dataItem.getKey());
        if (!((javax.jdo.spi.PersistenceCapable)dataItem).jdoIsPersistent()) {
            // data item came from cache -- reload
            dataItem = performGetDataItemByKey(dataItem.getKey());
            if (dataItem == null)
                return; // already removed
        }
        Factory.getPM().deletePersistent(dataItem);
    }

    public static void persistDataItems(List<DataItem> items) {
        for (DataItem item : items)
            persistDataItem(item);
    }

    public static DataItem getDataItemByKey(Key key) {
        if (key == null)
            return null;
        DataItem dataItem = (DataItem) ITEM_BY_KEY_CACHE.get(key);
        if (dataItem != null)
            return dataItem;
        dataItem = performGetDataItemByKey(key);
        if (dataItem != null)
            ITEM_BY_KEY_CACHE.put(key, dataItem);
        return dataItem;
    }

    private static DataItem performGetDataItemByKey(Key key) {
        try {
            return Factory.getPM().getObjectById(DataItem.class, key);
        } catch (JDOObjectNotFoundException e) {
            log.info("Data item is not found by key=" + key);
            return null;
        }
    }

    /**
     * Returns a list of last data items in ASCENDING order.
     */
    public static List<DataItem> listDataItems(DataStream stream, TimeInstant since, int first, int last) {
        if (stream.getMode() == DataStreamMode.LAST) {
            DataItem theOne = getDataItemByKey(stream.getLastItemKey());
            return theOne != null ?
                    Collections.<DataItem>singletonList(theOne) :
                    Collections.<DataItem>emptyList();
        }
        ListEntry entry = (ListEntry) LIST_CACHE.get(stream.getStreamId());
        if (entry != null) {
            int start = 0;
            int size = entry.items.size();
            if (since != null) {
                while (start < size && entry.items.get(start).getTimeMillis() < since.time())
                    start++;
            }
            if (entry.complete || start > 0 || size >= first + last)
                // return from cache
                return fillStream(stream,
                        entry.items.subList(Math.max(start, size - first - last), Math.max(start, size - first)));
        }
        // perform query
        return fillStream(stream, performItemsQuery(stream.getStreamId(), since, first, last));
    }

    @SuppressWarnings({"unchecked"})
    private static List<DataItem> performItemsQuery(long streamId, TimeInstant since, int first, int last) {
        Query query = Factory.getPM().newQuery(DataItem.class);
        String queryFilter = "streamId == id";
        String queryParams = "long id";
        Map<String, Object> queryArgs = new HashMap<String, Object>();
        queryArgs.put("id", streamId);
        if (since != null) {
            queryFilter += " && timeMillis >= since";
            queryParams += ", long since";
            queryArgs.put("since", since.time());
        }
        query.setFilter(queryFilter);
        query.declareParameters(queryParams);
        query.setOrdering("timeMillis desc");
        query.setRange(first, first + last);
        query.getFetchPlan().setFetchSize(last);
        List<DataItem> items = new ArrayList<DataItem>((Collection<DataItem>)query.executeWithMap(queryArgs));
        Collections.reverse(items); // turn descending into ascending order
        if (first == 0)
            LIST_CACHE.put(streamId, new ListEntry(items, items.size() < last));
        return items;
    }

    private static List<DataItem> fillStream(DataStream stream, List<DataItem> items) {
        for (DataItem item : items)
            item.setStream(stream);
        return items;
    }

    public static void refreshCache(long streamId) {
        performItemsQuery(streamId, null, 0, MAX_LIST_SIZE);
    }

    private static class ListEntry implements Serializable {
        List<DataItem> items;
        boolean complete;

        ListEntry(List<DataItem> items, boolean complete) {
            this.items = items;
            this.complete = complete;
        }
    }
}
