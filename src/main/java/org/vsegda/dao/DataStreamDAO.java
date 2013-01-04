package org.vsegda.dao;

import com.google.appengine.api.datastore.Key;
import org.vsegda.data.DataItem;
import org.vsegda.data.DataStream;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import java.util.Collection;
import java.util.logging.Logger;

/**
 * @author Roman Elizarov
 */
public class DataStreamDAO {
    private static final Logger log = Logger.getLogger(DataStreamDAO.class.getName());

    private DataStreamDAO() {}

    @SuppressWarnings({"unchecked"})
    public static long resolveStreamCode(PersistenceManager pm, String code) {
        try {
            return Long.parseLong(code);
        } catch (NumberFormatException e) {
            // ignore and try to find by tag
        }
        Query query = pm.newQuery(DataStream.class);
        query.setOrdering("streamId asc");
        query.declareParameters("String code");
        query.setFilter("tag == code");
        Collection<DataStream> streams = (Collection<DataStream>) query.execute(code);
        if (streams.isEmpty()) {
            log.info("Creating new data stream with tag=" + code);
            // find last stream
            query = pm.newQuery(DataStream.class);
            query.setOrdering("streamId desc");
            query.setRange(0, 1);
            streams = (Collection<DataStream>) query.execute();
            long id = streams.isEmpty() ? 100 : streams.iterator().next().getStreamId() + 1;
            DataStream stream = new DataStream(id);
            stream.setTag(code);
            pm.makePersistent(stream);
            return stream.getStreamId();
        }
        return streams.iterator().next().getStreamId();
    }

    @SuppressWarnings({"unchecked"})
    public static boolean ensureFirstItemKey(PersistenceManager pm, DataStream stream) {
        if (stream.getFirstItemKey() != null)
            return true;
        log.info("Determining first item key for streamId=" + stream.getStreamId());
        Key key = findFistItemKey(pm, stream.getStreamId());
        stream.setFirstItemKey(key);
        return key != null;
    }

    public static Key findFistItemKey(PersistenceManager pm, long streamId) {
        DataItem fistItem = findFistItem(pm, streamId);
        return fistItem == null ? null : fistItem.getKey();
    }

    @SuppressWarnings({"unchecked"})
    public static DataItem findFistItem(PersistenceManager pm, long streamId) {
        Query query = pm.newQuery(DataItem.class);
        query.setFilter("streamId == id");
        query.setOrdering("timeMillis asc");
        query.declareParameters("long id");
        query.setRange(0, 1);
        Collection<DataItem> items = (Collection<DataItem>) query.execute(streamId);
        if (items.isEmpty())
            return null;
        return items.iterator().next();
    }
}
