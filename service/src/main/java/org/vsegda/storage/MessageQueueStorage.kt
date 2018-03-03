package org.vsegda.storage

import com.google.appengine.api.datastore.*
import org.vsegda.data.*
import org.vsegda.util.*

private var Entity.name by Prop.string
private var Entity.lastGetIndex by Prop.long
private var Entity.lastPostIndex by Prop.long
private var Entity.lastSessionId by Prop.long

object MessageQueueStorage : BaseStorage<MessageQueue>() {
    override val kind = "MessageQueue"

    fun keyOf(queueId: Long): Key = KeyFactory.createKey(kind, queueId)

    override fun MessageQueue.toKey() =
        if (queueId == 0L) null else keyOf(queueId)

    override fun MessageQueue.toEntity() = newEntity { e ->
        e.name = name
        e.lastGetIndex = lastGetIndex
        e.lastPostIndex = lastPostIndex
        e.lastSessionId = lastSessionId
    }

    override fun Entity.toObject() = MessageQueue().apply {
        val e = this@toObject
        queueId = e.key?.id ?: 0L
        name = e.name ?: ""
        lastGetIndex = e.lastGetIndex ?: 0L
        lastPostIndex = e.lastPostIndex ?: 0L
        lastSessionId = e.lastSessionId ?: 0L
    }

    fun queryMessageQueues(): List<MessageQueue> =
        logged({ "queryMessageQueues() -> ${it.size} queues" }) {
            query { sortAscByKey() }.asList()
        }

    fun loadMessageQueueById(queueId: Long): MessageQueue? =
        logged({ "loadMessageQueueById($queueId) -> $it" }) {
            load(keyOf(queueId))
        }

    fun Transaction.storeMessageQueue(queue: MessageQueue) =
        logged({ "storeMessageQueue($queue) -> $it" }) {
            store(queue)
        }
}