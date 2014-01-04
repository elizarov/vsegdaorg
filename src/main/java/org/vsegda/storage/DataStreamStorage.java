package org.vsegda.storage;

import org.vsegda.data.DataStream;
import org.vsegda.factory.PM;

import javax.jdo.FetchPlan;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.Query;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author Roman Elizarov
 */
public class DataStreamStorage {
    private static final Logger log = Logger.getLogger(DataStreamStorage.class.getName());

    private DataStreamStorage() {} // do not create

    public static void storeDataStream(DataStream stream) {
        PM.instance().makePersistent(stream);
    }

    @SuppressWarnings({"unchecked"})
    public static List<DataStream> queryDataStreams() {
        Query query = PM.instance().newQuery(DataStream.class);
        query.setOrdering("streamId asc");
        query.getFetchPlan().setFetchSize(FetchPlan.FETCH_SIZE_GREEDY);
        long queryStartTime = System.currentTimeMillis();
        ArrayList<DataStream> streams = new ArrayList<DataStream>((Collection<DataStream>) query.execute());
        log.info(String.format("retrieved %d data streams from storage in %d ms",
                streams.size(), System.currentTimeMillis() - queryStartTime));
        return streams;
    }

    @SuppressWarnings({"unchecked"})
    public static DataStream loadLastDataStream() {
        Query query = PM.instance().newQuery(DataStream.class);
        query.setOrdering("streamId desc");
        query.setRange(0, 1);
        long queryStartTime = System.currentTimeMillis();
        Collection<DataStream> streams = (Collection<DataStream>) query.execute();
        log.info(String.format("retrieved %d data streams from storage in %d ms",
                streams.size(), System.currentTimeMillis() - queryStartTime));
        return streams.isEmpty() ? null : streams.iterator().next();
    }

    @SuppressWarnings({"unchecked"})
    public static DataStream loadDataStreamByTag(String tag) {
        Query query = PM.instance().newQuery(DataStream.class);
        query.setOrdering("streamId asc");
        query.declareParameters("String _tag");
        query.setFilter("tag == _tag");
        long queryStartTime = System.currentTimeMillis();
        Collection<DataStream> streams = (Collection<DataStream>) query.execute(tag);
        log.info(String.format("(tag=%s) retrieved %d data streams from storage in %d ms",
                tag, streams.size(), System.currentTimeMillis() - queryStartTime));
        return streams.isEmpty() ? null : streams.iterator().next();
    }

    public static DataStream loadDataStreamById(long id) {
        long queryStartTime = System.currentTimeMillis();
        try {
            DataStream result = PM.instance().getObjectById(DataStream.class, id);
            log.info(String.format("(id=%d) retrieved data stream from storage in %d ms",
                    id, System.currentTimeMillis() - queryStartTime));
            return result;
        } catch (JDOObjectNotFoundException e) {
            log.info(String.format("(id=%d) not found data stream in storage in %d ms",
                    id, System.currentTimeMillis() - queryStartTime));
            return null;
        }
    }

    public static void removeDataStream(DataStream stream) {
        PM.instance().deletePersistent(stream);
    }
}
