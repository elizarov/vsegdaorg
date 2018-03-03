package org.vsegda.service

import org.vsegda.data.*
import org.vsegda.storage.*

object MessageQueueService {
    val messageQueues: List<MessageQueue>
        get() = MessageQueueStorage.queryMessageQueues()

    // only literal queueId is supported
    fun resolveMessageQueueByCode(code: String): MessageQueue? =
        MessageQueueStorage.loadMessageQueueById(code.toLong())

    private fun Transaction.resolveMessageQueueByIdOrCreate(queueId: Long): MessageQueue =
        MessageQueueStorage.loadMessageQueueById(queueId) ?:
            MessageQueue(queueId).also {
                with(MessageQueueStorage) { storeMessageQueue(it) }
            }

    private fun Transaction.resolveMessageSessionByIdOrCreate(queue: MessageQueue, sessionId: Long?, now: Long): MessageSession =
            sessionId?.let { id -> MessageSessionStorage.loadMessageSession(queue.queueId, id) } ?: run {
                val id = MessageSessionStorage.loadLastMessageSession(queue.queueId)?.sessionId
                    ?.let { last -> last + 1 } ?: 1L
                MessageSession(queue.queueId, id, now).also {
                    with(MessageSessionStorage) { storeMessageSession(it) }
                }
            }

    fun postToQueue(queueId: Long, sessionId: Long?, items: List<MessageItem>?, now: Long): Long =
        MessageQueueStorage.transaction {
            val queue = resolveMessageQueueByIdOrCreate(queueId)
            val session = resolveMessageSessionByIdOrCreate(queue, sessionId, now)
            var maxSessionPostIndex = session.lastPostIndex
            items?.forEach { item ->
                if (item.messageIndex <= session.lastPostIndex)
                    return@forEach // was already posted
                maxSessionPostIndex = Math.max(maxSessionPostIndex, session.lastPostIndex)
                val index = queue.lastPostIndex + 1
                queue.lastPostIndex = index
                item.messageIndex = index
            }
            items?.let { with(MessageItemStorage) { storeMessageItems(it) } }
            session.lastPostIndex = maxSessionPostIndex
            with(MessageQueueStorage) { storeMessageQueue(queue) }
            with(MessageSessionStorage) { storeMessageSession(session) }
            session.sessionId
        }

    fun updateMessageQueue(queue: MessageQueue) {
        MessageQueueStorage.transaction {
            with(MessageQueueStorage) { storeMessageQueue(queue) }
        }
    }
}
