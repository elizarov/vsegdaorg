package org.vsegda.data;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import org.vsegda.util.TimeUtil;

/**
 * @author Roman Elizarov
 */
public class MessageSession {
    private Key key;
    private long queueId;
    private long sessionId;
    private long creationTimeMillis;
    private long lastPostIndex = -1;

    public static Key createKey(long queueId, long sessionId) {
        return KeyFactory.createKey(MessageQueue.createKey(queueId),
                MessageSession.class.getSimpleName(), sessionId);
    }

    public MessageSession() {}

    public MessageSession(long queueId, long sessionId, long now) {
        this.queueId = queueId;
        this.sessionId = sessionId;
        this.creationTimeMillis = now;
        updateKey();
    }

    private void updateKey() {
        if (queueId != 0 && sessionId != 0)
            key = createKey(queueId, sessionId);
    }

    public long getQueueId() {
        return queueId;
    }

    public void setQueueId(long queueId) {
        this.queueId = queueId;
        updateKey();
    }

    public long getSessionId() {
        return sessionId;
    }

    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
        updateKey();
    }

    public long getCreationTimeMillis() {
        return creationTimeMillis;
    }

    public void setCreationTimeMillis(long creationTimeMillis) {
        this.creationTimeMillis = creationTimeMillis;
    }

    public long getLastPostIndex() {
        return lastPostIndex;
    }

    public void setLastPostIndex(long lastPostIndex) {
        this.lastPostIndex = lastPostIndex;
    }

    @Override
    public String toString() {
        return queueId + "," + sessionId + "," + TimeUtil.formatDateTime(creationTimeMillis) + "," + lastPostIndex;
    }
}
