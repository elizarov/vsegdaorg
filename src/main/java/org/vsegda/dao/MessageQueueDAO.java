package org.vsegda.dao;

/**
 * @author Roman Elizarov
 */
public class MessageQueueDAO {
    private MessageQueueDAO() {}

    public static long resolveQueueCode(String code) {
        // todo: support tagged message queues in the future
        return Long.parseLong(code);
    }
}
