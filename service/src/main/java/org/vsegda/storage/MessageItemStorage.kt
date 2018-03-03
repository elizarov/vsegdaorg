package org.vsegda.storage

import com.google.appengine.api.datastore.*
import org.vsegda.data.*
import org.vsegda.util.*

private var Entity.queueId by Prop.long
private var Entity.messageIndex by Prop.long
private var Entity.text by Prop.string
private var Entity.timeMillis by Prop.long

object MessageItemStorage : BaseStorage<MessageItem>() {
    override val kind = "MessageItem"

    private fun keyOf(queueId: Long, messageIndex: Long): Key = KeyFactory.createKey(
        MessageQueueStorage.keyOf(queueId), kind, messageIndex)

    override fun MessageItem.toKey() =
        if (messageIndex == 0L) null else keyOf(queueId, messageIndex)

    override fun MessageItem.toEntity()= newEntity { e ->
        e.queueId = queueId
        e.messageIndex = messageIndex
        e.text = text
        e.timeMillis = timeMillis
    }

    override fun Entity.toObject() = MessageItem().apply {
        val e = this@toObject
        queueId = e.queueId ?: e.key?.parent?.id ?: 0L
        messageIndex = e.messageIndex ?: e.key?.id ?: 0L
        text = e.text ?: ""
        timeMillis = e.timeMillis ?: 0L
    }

    fun loadMessageItem(queueId: Long, messageIndex: Long): MessageItem? =
        logged({ "loadMessageItem(queueId=$queueId, messageIndex=$messageIndex) -> $it" }) {
            load(keyOf(queueId, messageIndex))
        }

    fun loadNewMessageItems(queueId: Long, index: Long, offset: Int, n: Int): List<MessageItem> =
        logged({ "loadNewMessageItems(queueId=$queueId, index=$index, offset=$offset, n=$n) -> ${it.size}" }) {
            query {
                filterEq(Entity::queueId, queueId)
                filterGreater(Entity::messageIndex, index)
                sortAscBy(Entity::messageIndex)
            }.asList(n, offset)
        }

    fun loadLastMessagesItems(queueId: Long, offset: Int, n: Int): List<MessageItem> =
        logged({ "loadLastMessagesItems(queueId=$queueId, offset=$offset, n=$n) -> ${it.size}" }) {
            query {
                filterEq(Entity::queueId, queueId)
                sortDescBy(Entity::messageIndex)
            }.asList(n, offset)
        }

    fun Transaction.storeMessageItems(items: List<MessageItem>) =
        logged("storeMessageItems($items)") {
            storeList(items)
        }
}
