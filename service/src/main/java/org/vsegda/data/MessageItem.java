package org.vsegda.data;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import org.vsegda.util.TimeUtil;

/**
 * @author Roman Elizarov
 */
public class MessageItem {
    private Key key;
    private long queueId;
    private long messageIndex;
    private String text;
    private long timeMillis;

    public static Key createKey(long queueId, long messageIndex) {
        return KeyFactory.createKey(MessageQueue.createKey(queueId),
                MessageItem.class.getSimpleName(), messageIndex);
    }

    public MessageItem() {}

    public MessageItem(String line, long now) {
        String[] tokens = line.split(",");
        if (tokens.length < 2 || tokens.length > 4)
            throw new IllegalArgumentException("Invalid line format: " + line);
        try {
            queueId = Long.parseLong(tokens[0]);
            text = tokens[1];
            timeMillis = tokens.length < 3 ? now : Math.min(TimeUtil.INSTANCE.parseTime(tokens[2], now), now);
            messageIndex = tokens.length < 4 ? 0 : Long.parseLong(tokens[3]);
            updateKey();
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid line format: " + line, e);
        }
    }

    private void updateKey() {
        if (queueId != 0 && messageIndex != 0)
            key = createKey(queueId, messageIndex);
    }

    public long getQueueId() {
        return queueId;
    }

    public void setQueueId(long queueId) {
        this.queueId = queueId;
        updateKey();
    }

    public long getMessageIndex() {
        return messageIndex;
    }

    public void setMessageIndex(long messageIndex) {
        this.messageIndex = messageIndex;
        updateKey();
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getTimeMillis() {
        return timeMillis;
    }

    public void setTimeMillis(long timeMillis) {
        this.timeMillis = timeMillis;
    }

    public String getTime() {
        return TimeUtil.INSTANCE.formatDateTime(timeMillis);
    }

    public String getAgo() {
        return TimeUtil.INSTANCE.formatDateTimeDifference(getTimeMillis(), System.currentTimeMillis());
    }

    public String getFormatClass() {
        return TimeUtil.INSTANCE.getFormatClass(getTimeMillis(), System.currentTimeMillis());
    }

    @Override
    public String toString() {
        return queueId + "," + text + "," + getTime() + "," + messageIndex;
    }

}
