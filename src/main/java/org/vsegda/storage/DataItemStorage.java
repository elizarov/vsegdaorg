package org.vsegda.storage;

import org.vsegda.data.DataItem;
import org.vsegda.factory.PM;
import org.vsegda.util.TimeInstant;

import javax.jdo.Query;
import java.util.*;
import java.util.logging.Logger;

/**
 * @author Roman Elizarov
 */
public class DataItemStorage {
    private static final Logger log = Logger.getLogger(DataItemStorage.class.getName());

    private DataItemStorage() {} // do not create

    public static void storeDataItem(DataItem dataItem) {
        PM.instance().makePersistent(dataItem);
    }

    public static void deleteDataItem(DataItem dataItem) {
        PM.instance().deletePersistent(dataItem);
    }

    @SuppressWarnings({"unchecked"})
    public static List<DataItem> queryDataItems(long streamId, TimeInstant from, TimeInstant to, int n) {
        Query query = PM.instance().newQuery(DataItem.class);
        String queryFilter = "streamId == _id";
        String queryParams = "long _id";
        Map<String, Object> queryArgs = new HashMap<String, Object>();
        queryArgs.put("_id", streamId);
        if (from != null) {
            queryFilter += " && timeMillis >= _from";
            queryParams += ", long _from";
            queryArgs.put("_from", from.time());
        }
        if (to != null) {
            queryFilter += " && timeMillis < _to";
            queryParams += ", long _to";
            queryArgs.put("_to", to.time());
        }
        query.setFilter(queryFilter);
        query.declareParameters(queryParams);
        query.setOrdering("timeMillis desc");
        query.setRange(0, n);
        query.getFetchPlan().setFetchSize(n);
        long queryStartTime = System.currentTimeMillis();
        List<DataItem> items = new ArrayList<DataItem>((Collection<DataItem>)query.executeWithMap(queryArgs));
        log.info(String.format("(streamId=%d, from=%s, to=%s, n=%d) retrieved %d data items from storage in %d ms",
                streamId, from, to, n, items.size(), System.currentTimeMillis() - queryStartTime));
        Collections.reverse(items); // turn descending into ascending order
        return items;
    }

    @SuppressWarnings({"unchecked"})
    public static DataItem loadFirstDataItem(long streamId) {
        Query query = PM.instance().newQuery(DataItem.class);
        query.setFilter("streamId == _id");
        query.setOrdering("timeMillis asc");
        query.declareParameters("long _id");
        query.setRange(0, 1);
        long queryStartTime = System.currentTimeMillis();
        Collection<DataItem> items = (Collection<DataItem>) query.execute(streamId);
        log.info(String.format("(streamId=%d) retrieved %d data items from storage in %d ms",
                streamId, items.size(), System.currentTimeMillis() - queryStartTime));
        return items.isEmpty() ? null : items.iterator().next();
    }

    @SuppressWarnings({"unchecked"})
    public static List<DataItem> queryFirstDataItems(long streamId, long timeLimit, int n) {
        Query query = PM.instance().newQuery(DataItem.class);
        query.setOrdering("timeMillis asc");
        query.declareParameters("long _id, long _limit");
        query.setFilter("streamId == _id && timeMillis < _limit");
        query.setRange(0, n);
        query.getFetchPlan().setFetchSize(n);
        return new ArrayList<DataItem>((Collection<DataItem>) query.execute(streamId, timeLimit));
    }
}
