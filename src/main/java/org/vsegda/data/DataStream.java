package org.vsegda.data;

import com.google.appengine.api.datastore.Key;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * @author Roman Elizarov
 */
@PersistenceCapable
public class DataStream {
    @PrimaryKey
    @Persistent
    private Long streamId;

    @Persistent
    private String name = "";

    @Persistent
    private Key firstItemKey;

    @Persistent
    private Key lastItemKey;

    @Persistent
    private Long alertTimeout;

    @Persistent
    private DataStreamMode mode = DataStreamMode.LAST;

    public DataStream(Long streamId) {
        this.streamId = streamId;
    }

    public Long getStreamId() {
        return streamId;
    }

    public void setStreamId(Long streamId) {
        this.streamId = streamId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Key getFirstItemKey() {
        return firstItemKey;
    }

    public void setFirstItemKey(Key firstItemKey) {
        this.firstItemKey = firstItemKey;
    }

    public Key getLastItemKey() {
        return lastItemKey;
    }

    public void setLastItemKey(Key lastItemKey) {
        this.lastItemKey = lastItemKey;
    }

    public Long getAlertTimeout() {
        return alertTimeout;
    }

    public void setAlertTimeout(Long alertTimeout) {
        this.alertTimeout = alertTimeout;
    }

    public DataStreamMode getMode() {
        // all legacy data stream are assumed to have ARCHIVE mode
        return mode == null ? DataStreamMode.ARCHIVE : mode;
    }

    public void setMode(DataStreamMode mode) {
        this.mode = mode;
    }

    @Override
    public String toString() {
        return streamId + "," + name;
    }
}