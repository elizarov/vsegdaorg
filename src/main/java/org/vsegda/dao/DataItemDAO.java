package org.vsegda.dao;

import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheException;
import net.sf.jsr107cache.CacheFactory;
import net.sf.jsr107cache.CacheManager;
import org.vsegda.data.DataItem;
import org.vsegda.data.DataStream;
import org.vsegda.factory.PM;
import org.vsegda.shared.DataStreamMode;
import org.vsegda.util.TimeInstant;

import javax.jdo.Query;
import java.io.Serializable;
import java.util.*;
import java.util.logging.Logger;

/**
 * @author Roman Elizarov
 */
public class DataItemDAO {
    private static final Logger log = Logger.getLogger(DataItemDAO.class.getName());

    private static final int INITIAL_LIST_CACHE_SIZE = 2000;
    private static final int MAX_LIST_SIZE = (int)(1.5 * INITIAL_LIST_CACHE_SIZE);

    private static final Cache LIST_CACHE;

    static {
        try {
            CacheFactory cf = CacheManager.getInstance().getCacheFactory();
            LIST_CACHE = cf.createCache(Collections.emptyMap());
        } catch (CacheException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private DataItemDAO() {} // do not create

    public static void persistDataItem(DataItem dataItem) {
        PM.instance().makePersistent(dataItem);
        ListEntry entry = (ListEntry) LIST_CACHE.get(dataItem.getStreamId());
        if (entry != null) {
            entry.items.add(dataItem);
            int size = entry.items.size();
            if (size >= 2 && DataItem.ORDER_BY_TIME.compare(entry.items.get(size - 1), entry.items.get(size - 2)) < 0)
                Collections.sort(entry.items, DataItem.ORDER_BY_TIME);
            if (size > MAX_LIST_SIZE)
                entry.items.subList(0, size - INITIAL_LIST_CACHE_SIZE).clear();
            LIST_CACHE.put(dataItem.getStreamId(), entry);
        }
    }

    public static void persistDataItems(List<DataItem> items) {
        for (DataItem item : items)
            persistDataItem(item);
    }

    public static DataItem getLastDataItem(DataStream stream) {
        List<DataItem> items = listDataItems(stream, null, 1);
        return items.isEmpty() ? new DataItem(stream, Double.NaN, 0) : items.get(0);
    }

    /**
     * Returns a list of last data items in ASCENDING order.
     */
    public static List<DataItem> listDataItems(DataStream stream, TimeInstant since, int n) {
        if (stream.getMode() == DataStreamMode.LAST)
            n = 1;
        ListEntry entry = (ListEntry) LIST_CACHE.get(stream.getStreamId());
        if (entry != null) {
            int start = 0;
            int size = entry.items.size();
            if (since != null) {
                while (start < size && entry.items.get(start).getTimeMillis() < since.time())
                    start++;
            }
            if (entry.complete || start > 0 || size >= n)
                // return from cache
                return fillStream(stream,
                        entry.items.subList(Math.max(start, size - n), size));
        }
        // perform query
        return fillStream(stream, performItemsQuery(stream.getStreamId(), since, n));
    }

    @SuppressWarnings({"unchecked"})
    private static List<DataItem> performItemsQuery(long streamId, TimeInstant since, int n) {
        Query query = PM.instance().newQuery(DataItem.class);
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
        query.setRange(0, n);
        query.getFetchPlan().setFetchSize(n);
        List<DataItem> items = new ArrayList<DataItem>((Collection<DataItem>)query.executeWithMap(queryArgs));
        Collections.reverse(items); // turn descending into ascending order
        LIST_CACHE.put(streamId, new ListEntry(items, items.size() < n));
        return items;
    }

    private static List<DataItem> fillStream(DataStream stream, List<DataItem> items) {
        for (DataItem item : items)
            item.setStream(stream);
        return items;
    }

    public static void refreshCache(long streamId) {
        performItemsQuery(streamId, null, INITIAL_LIST_CACHE_SIZE);
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
