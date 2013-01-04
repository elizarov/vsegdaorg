package org.vsegda.dao;

import com.google.appengine.api.datastore.Key;
import org.vsegda.data.DataItem;
import org.vsegda.data.DataStream;

import javax.jdo.JDOObjectNotFoundException;
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

    public static DataStream resolveStreamByCode(PersistenceManager pm, String code) {
        try {
            return resolveStreamById(pm, Long.parseLong(code));
        } catch (NumberFormatException e) {
            // ignore and try to find by tag
        }
        return resolveStreamByTag(pm, code);
    }

    @SuppressWarnings({"unchecked"})
    public static DataStream resolveStreamByTag(PersistenceManager pm, String tag) {
        Query query = pm.newQuery(DataStream.class);
        query.setOrdering("streamId asc");
        query.declareParameters("String code");
        query.setFilter("tag == code");
        Collection<DataStream> streams = (Collection<DataStream>) query.execute(tag);
        DataStream stream;
        if (streams.isEmpty()) {
            log.info("Creating new data stream with tag=" + tag);
            // find last stream
            query = pm.newQuery(DataStream.class);
            query.setOrdering("streamId desc");
            query.setRange(0, 1);
            streams = (Collection<DataStream>) query.execute();
            long id = streams.isEmpty() ? 100 : streams.iterator().next().getStreamId() + 1;
            stream = new DataStream(id);
            stream.setTag(tag);
            pm.makePersistent(stream);
        } else
            stream = streams.iterator().next();
        return stream;
    }

    private static DataStream resolveStreamById(PersistenceManager pm, long id) {
        try {
            return pm.getObjectById(DataStream.class, id);
        } catch (JDOObjectNotFoundException e) {
            log.info("Creating new data stream with id=" + id);
            DataStream stream = new DataStream(id);
            pm.makePersistent(stream);
            return stream;
        }
    }

    public static DataStream resolveStream(PersistenceManager pm, DataStream stream) {
        return stream.getStreamId() != null ?
                resolveStreamById(pm, stream.getStreamId()) :
                resolveStreamByTag(pm, stream.getTag());
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

    public static DataItem getOrFindFirstItem(PersistenceManager pm, DataStream stream) {
        Key key = stream.getFirstItemKey();
        if (key != null)
            try {
                return pm.getObjectById(DataItem.class, key);
            } catch (JDOObjectNotFoundException e) {
                log.warning("First item for streamId=" + stream.getStreamId() + " is not found with key=" + key);
            }
        DataItem firstItem = findFistItem(pm, stream.getStreamId());
        if (firstItem == null)
            return null;
        stream.setFirstItemKey(firstItem.getKey());
        return firstItem;
    }

}
