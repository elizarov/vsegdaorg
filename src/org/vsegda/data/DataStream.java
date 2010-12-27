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
    private Key lastItemKey;

    @Persistent
    private Long alertTimeout;

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

    @Override
    public String toString() {
        return streamId + "," + name;
    }
}
