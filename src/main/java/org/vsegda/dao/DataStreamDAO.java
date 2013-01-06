package org.vsegda.dao;

import com.google.appengine.api.datastore.Key;
import org.vsegda.data.DataItem;
import org.vsegda.data.DataStream;
import org.vsegda.factory.PM;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.Query;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author Roman Elizarov
 */
public class DataStreamDAO {
    private static final Logger log = Logger.getLogger(DataStreamDAO.class.getName());
    private static final int MAX_FETCH_SIZE = 10000;

    private DataStreamDAO() {}

    public static List<DataStream> listDataStreams() {
        return listDataStreams(0, Integer.MAX_VALUE);
    }

    @SuppressWarnings({"unchecked"})
    public static List<DataStream> listDataStreams(int first, int last) {
        Query query = PM.instance().newQuery(DataStream.class);
        query.setOrdering("streamId asc");
        if (last < Integer.MAX_VALUE)
            query.setRange(first, first + last);
        query.getFetchPlan().setFetchSize(Math.min(MAX_FETCH_SIZE, last));
        return new ArrayList<DataStream>((Collection<DataStream>) query.execute());
    }

    public static DataStream resolveDataStreamByCode(String code) {
        int i = code.lastIndexOf('@');
        String id;
        if (i >= 0)
            id = code.substring(i + 1);
        else
            id = code;
        try {
            return resolveDataStreamById(Long.parseLong(id));
        } catch (NumberFormatException e) {
            // ignore and try to find by tag
        }
        return resolveDataStreamByTag(code);
    }

    @SuppressWarnings({"unchecked"})
    public static DataStream resolveDataStreamByTag(String tag) {
        Query query = PM.instance().newQuery(DataStream.class);
        query.setOrdering("streamId asc");
        query.declareParameters("String code");
        query.setFilter("tag == code");
        Collection<DataStream> streams = (Collection<DataStream>) query.execute(tag);
        DataStream stream;
        if (streams.isEmpty()) {
            log.info("Creating new data stream with tag=" + tag);
            // find last stream
            query = PM.instance().newQuery(DataStream.class);
            query.setOrdering("streamId desc");
            query.setRange(0, 1);
            streams = (Collection<DataStream>) query.execute();
            long id = streams.isEmpty() ? 100 : streams.iterator().next().getStreamId() + 1;
            stream = new DataStream(id);
            stream.setTag(tag);
            PM.instance().makePersistent(stream);
        } else
            stream = streams.iterator().next();
        return stream;
    }

    public static DataStream resolveDataStreamById(long id) {
        try {
            return PM.instance().getObjectById(DataStream.class, id);
        } catch (JDOObjectNotFoundException e) {
            log.info("Creating new data stream with id=" + id);
            DataStream stream = new DataStream(id);
            PM.instance().makePersistent(stream);
            return stream;
        }
    }

    public static DataStream resolveDataStream(DataStream stream) {
        return stream.getStreamId() != null ?
                resolveDataStreamById(stream.getStreamId()) :
                resolveDataStreamByCode(stream.getTag());
    }

    @SuppressWarnings({"unchecked"})
    public static DataItem findFistItem(long streamId) {
        Query query = PM.instance().newQuery(DataItem.class);
        query.setFilter("streamId == id");
        query.setOrdering("timeMillis asc");
        query.declareParameters("long id");
        query.setRange(0, 1);
        Collection<DataItem> items = (Collection<DataItem>) query.execute(streamId);
        if (items.isEmpty())
            return null;
        return items.iterator().next();
    }

    public static DataItem getOrFindFirstItem(DataStream stream) {
        Key key = stream.getFirstItemKey();
        if (key != null)
            try {
                return PM.instance().getObjectById(DataItem.class, key);
            } catch (JDOObjectNotFoundException e) {
                log.warning("First item for streamId=" + stream.getStreamId() + " is not found with key=" + key);
            }
        DataItem firstItem = findFistItem(stream.getStreamId());
        if (firstItem == null)
            return null;
        stream.setFirstItemKey(firstItem.getKey());
        return firstItem;
    }
}
