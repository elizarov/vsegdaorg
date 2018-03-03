package org.vsegda.service

import org.vsegda.data.*
import org.vsegda.storage.*

object MessageItemService {
    fun getMessageItem(queueId: Long, messageIndex: Long): MessageItem? =
        MessageItemStorage.loadMessageItem(queueId, messageIndex)

    fun getNewMessageItems(queueId: Long, index: Long, first: Int, last: Int): List<MessageItem> =
        MessageItemStorage.loadNewMessageItems(queueId, index, first, last)

    fun getLastMessagesItems(queueId: Long, first: Int, last: Int): List<MessageItem> =
        MessageItemStorage.loadLastMessagesItems(queueId, first, last)
}