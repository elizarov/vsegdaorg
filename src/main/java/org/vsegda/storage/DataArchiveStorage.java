package org.vsegda.storage;

import com.google.appengine.api.datastore.Cursor;
import org.datanucleus.store.appengine.query.JDOCursorHelper;
import org.vsegda.data.DataArchive;
import org.vsegda.data.DataItem;
import org.vsegda.factory.PM;
import org.vsegda.util.TimeInstant;

import javax.jdo.Query;
import java.util.*;
import java.util.logging.Logger;

/**
 * @author Roman Elizarov
 */
public class DataArchiveStorage {
    private static final Logger log = Logger.getLogger(DataArchiveStorage.class.getName());
    private static final int BATCH_FETCH_SIZE = 10000;

    private DataArchiveStorage() {} // do not create

    public static void storeDataArchive(DataArchive archive) {
        PM.instance().makePersistent(archive);
    }

    @SuppressWarnings({"unchecked"})
    public static List<DataItem> queryItemsFromDataArchives(long streamId, TimeInstant from, TimeInstant to, int nItems) {
        Query query = PM.instance().newQuery(DataArchive.class);
        String queryFilter = "streamId == _id";
        String queryParams = "long _id";
        Map<String, Object> queryArgs = new HashMap<String, Object>();
        queryArgs.put("_id", streamId);
        if (from != null) {
            queryFilter += " && firstTimeMillis >= _from";
            queryParams += ", long _from";
            queryArgs.put("_from", from.time());
        }
        if (to != null) {
            queryFilter += " && firstTimeMillis < _to";
            queryParams += ", long _to";
            queryArgs.put("_to", to.time());
        }
        query.setFilter(queryFilter);
        query.declareParameters(queryParams);
        query.setOrdering("firstTimeMillis desc");
        int range = 1 + nItems / DataArchive.COUNT_ESTIMATE; // estimate number of archives
        query.setRange(0, range);

        List<DataItem> items = new ArrayList<DataItem>();
        long queryStartTime = System.currentTimeMillis();
        int totalArchives = 0;

    loadLoop:
        while (true) {
            List<DataArchive> archives = new ArrayList<DataArchive>((Collection<DataArchive>)query.executeWithMap(queryArgs));
            Cursor cursor = JDOCursorHelper.getCursor(archives);
            totalArchives += archives.size();

            for (DataArchive archive : archives) {
                int prevSize = items.size();
                items.addAll(archive.getItems());
                Collections.reverse(items.subList(prevSize, items.size())); // turn ascending items into descending
                if (items.size() >= nItems) {
                    items.subList(nItems, items.size()).clear();
                    break loadLoop;
                }
            }
            if (archives.size() < range)
                break; // returns less data, means no stuff -- done

            // load next batch using JDO cursor
            Map<String, Object> extensionMap = new HashMap<String, Object>();
            extensionMap.put(JDOCursorHelper.CURSOR_EXTENSION, cursor);
            query.setExtensions(extensionMap);
        }

        log.info(String.format("(streamId=%d, from=%s, to=%s, nItems=%d) retrieved %d data archives from storage with %d items in %d ms",
                streamId, from, to, nItems, totalArchives, items.size(), System.currentTimeMillis() - queryStartTime));
        Collections.reverse(items); // turn descending into ascending order
        return items;
    }

    @SuppressWarnings("unchecked")
    public static void updateStreamId(long fromId, long toId) {
        Query query = PM.instance().newQuery(DataArchive.class);
        query.setFilter("streamId == _id");
        query.declareParameters("long _id");
        query.getFetchPlan().setFetchSize(BATCH_FETCH_SIZE);
        Collection<DataArchive> items = (Collection<DataArchive>) query.execute(fromId);
        for (DataArchive item : items)
            item.setStreamId(toId);
    }

    @SuppressWarnings("unchecked")
    public static void removeAllByStreamId(long streamId) {
        Query query = PM.instance().newQuery(DataArchive.class);
        query.setFilter("streamId == _id");
        query.declareParameters("long _id");
        query.getFetchPlan().setFetchSize(BATCH_FETCH_SIZE);
        Collection<DataArchive> items = (Collection<DataArchive>) query.execute(streamId);
        for (DataArchive item : items)
            PM.instance().deletePersistent(item);
    }
}
