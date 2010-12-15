package org.vsegda.data;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * @author Roman Elizarov
 */
@PersistenceCapable
public class MessageQueue {
    @PrimaryKey
    @Persistent
    private Key key;

    @Persistent
    private String name = "";

    @Persistent
    private long lastGetIndex;

    @Persistent
    private long lastPostIndex;

    @Persistent
    private long lastSessionId;

    public static Key createKey(long queueId) {
        return KeyFactory.createKey(MessageQueue.class.getSimpleName(), queueId);
    }

    public MessageQueue() {}

    public MessageQueue(Key key) {
        this.key = key;
    }

    public Key getKey() {
        return key;
    }

    public void setKey(Key key) {
        this.key = key;
    }

    public long getQueueId() {
        return key.getId();
    }

    public void setQueueId(long queueId) {
        this.key = createKey(queueId);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getLastGetIndex() {
        return lastGetIndex;
    }

    public void setLastGetIndex(long lastGetIndex) {
        this.lastGetIndex = lastGetIndex;
    }

    public long getLastPostIndex() {
        return lastPostIndex;
    }

    public void setLastPostIndex(long lastPostIndex) {
        this.lastPostIndex = lastPostIndex;
    }

    public long getLastSessionId() {
        return lastSessionId;
    }

    public void setLastSessionId(long lastSessionId) {
        this.lastSessionId = lastSessionId;
    }

    @Override
    public String toString() {
        return getQueueId() + "," + name + "," + lastGetIndex + "," + lastPostIndex + "," + lastSessionId;
    }
}
