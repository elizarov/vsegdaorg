package org.vsegda.service;

/**
 * @author Roman Elizarov
 */
public class MessageQueueService {
    private MessageQueueService() {}

    public static long resolveQueueCode(String code) {
        // todo: support tagged message queues in the future
        return Long.parseLong(code);
    }
}
