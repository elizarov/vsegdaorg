package org.vsegda.service;

import org.vsegda.data.DataStream;
import org.vsegda.storage.DataStreamStorage;

import java.util.List;
import java.util.logging.Logger;

/**
 * @author Roman Elizarov
 */
public class DataStreamService {
    private static final Logger log = Logger.getLogger(DataStreamService.class.getName());

    private DataStreamService() {}

    @SuppressWarnings({"unchecked"})
    public static List<DataStream> getDataStreams() {
        return DataStreamStorage.queryDataStreams();
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
        DataStream stream = DataStreamStorage.loadDataStreamByTag(tag);
        if (stream == null && createIfAbsent) {
            log.info("Creating new data stream with tag=" + tag);
            // find last stream
            stream = DataStreamStorage.loadLastDataStream();
            stream = new DataStream(stream == null ? 100 : stream.getStreamId() + 1);
            stream.setTag(tag);
            DataStreamStorage.storeDataStream(stream);
        }
        return stream;
    }

    public static DataStream resolveDataStreamById(long id, boolean createIfAbsent) {
        DataStream stream = DataStreamStorage.loadDataStreamById(id);
        if (stream == null && createIfAbsent) {
            log.info("Creating new data stream with id=" + id);
            stream = new DataStream(id);
            DataStreamStorage.storeDataStream(stream);
        }
        return stream;
    }

    public static DataStream resolveDataStream(DataStream stream) {
        return stream.getStreamId() != null ?
                resolveDataStreamById(stream.getStreamId(), true) :
                resolveDataStreamByCode(stream.getTag(), true);
    }
}
