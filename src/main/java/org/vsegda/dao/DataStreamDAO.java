package org.vsegda.dao;

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
        return listDataStreams(Integer.MAX_VALUE, 0, null);
    }

    @SuppressWarnings({"unchecked"})
    public static List<DataStream> listDataStreams(int last, int first, HasNext hasNext) {
        Query query = PM.instance().newQuery(DataStream.class);
        query.setOrdering("streamId asc");
        if (last < Integer.MAX_VALUE)
            query.setRange(first, first + last);
        query.getFetchPlan().setFetchSize(Math.min(MAX_FETCH_SIZE, last));
        ArrayList<DataStream> list = new ArrayList<DataStream>((Collection<DataStream>) query.execute());
        if (list.size() >= last && hasNext != null)
            hasNext.set();
        return list;
    }

    public static DataStream resolveDataStreamByCode(String code, boolean createIfAbsent) {
        int i = code.lastIndexOf(DataStream.TAG_ID_SEPARATOR);
        String id;
        if (i >= 0)
            id = code.substring(i + DataStream.TAG_ID_SEPARATOR.length());
        else
            id = code;
        try {
            return resolveDataStreamById(Long.parseLong(id), createIfAbsent);
        } catch (NumberFormatException e) {
            // ignore and try to find by tag
        }
        return resolveDataStreamByTag(code, createIfAbsent);
    }

    @SuppressWarnings({"unchecked"})
    public static DataStream resolveDataStreamByTag(String tag, boolean createIfAbsent) {
        Query query = PM.instance().newQuery(DataStream.class);
        query.setOrdering("streamId asc");
        query.declareParameters("String code");
        query.setFilter("tag == code");
        Collection<DataStream> streams = (Collection<DataStream>) query.execute(tag);
        DataStream stream;
        if (streams.isEmpty()) {
            if (!createIfAbsent)
                return null;
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

    public static DataStream resolveDataStreamById(long id, boolean createIfAbsent) {
        try {
            return PM.instance().getObjectById(DataStream.class, id);
        } catch (JDOObjectNotFoundException e) {
            if (!createIfAbsent)
                return null;
            log.info("Creating new data stream with id=" + id);
            DataStream stream = new DataStream(id);
            PM.instance().makePersistent(stream);
            return stream;
        }
    }

    public static DataStream resolveDataStream(DataStream stream) {
        return stream.getStreamId() != null ?
                resolveDataStreamById(stream.getStreamId(), true) :
                resolveDataStreamByCode(stream.getTag(), true);
    }
}
