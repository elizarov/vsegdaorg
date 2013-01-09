package org.vsegda.data;

import com.google.appengine.api.datastore.Key;
import org.vsegda.shared.DataStreamMode;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import java.io.Serializable;

/**
 * @author Roman Elizarov
 */
@PersistenceCapable
public class DataStream implements Serializable {
    private static final long serialVersionUID = -5570677468800411934L;

    public static final String TAG_ID_SEPARATOR = ":";

    @PrimaryKey
    @Persistent
    private Long streamId;

    @Persistent
    private String tag;

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

    public DataStream() {}

    public DataStream(Long streamId) {
        this.streamId = streamId;
    }

    public Long getStreamId() {
        return streamId;
    }

    public void setStreamId(Long streamId) {
        this.streamId = streamId;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        if (tag == null)
            this.tag = null;
        else {
            tag = tag.trim();
            this.tag = tag.isEmpty() ? null : tag;
        }
    }

    /**
     * Returns stream id or tag if defined.
     */
    public String getCode() {
        return tag != null ?
                tag + (streamId == null ? "" : TAG_ID_SEPARATOR + streamId) :
                String.valueOf(streamId);
    }

    public String getNameOrCode() {
        return name.isEmpty() ? getCode() : name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? "" : name.trim();
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
        return getCode() + "," + name;
    }
}
