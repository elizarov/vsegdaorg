package org.vsegda.dao;

import javax.jdo.PersistenceManager;

/**
 * @author Roman Elizarov
 */
public class MessageQueueDAO {
    private MessageQueueDAO() {}

    public static long resolveQueueCode(PersistenceManager pm, String code) {
        // todo: support tagged message queues in the future
        return Long.parseLong(code);
    }
}
