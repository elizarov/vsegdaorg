package org.vsegda.storage

import com.google.appengine.api.datastore.*
import org.vsegda.data.*
import org.vsegda.storage.MessageQueueStorage.toKey
import org.vsegda.util.*

private var Entity.queueId by Prop.long
private var Entity.sessionId by Prop.long
private var Entity.creationTimeMillis by Prop.long
private var Entity.lastPostIndex by Prop.long

object MessageSessionStorage : BaseStorage<MessageSession>() {
    override val kind = "MessageSession"

    private fun keyOf(queueId: Long, sessionId: Long): Key = KeyFactory.createKey(
        MessageQueueStorage.keyOf(queueId), kind, sessionId)

    override fun MessageSession.toKey() = keyOf(queueId, sessionId)
        
    override fun MessageSession.toEntity()= Entity(toKey()).also { e ->
        e.queueId = queueId
        e.sessionId = sessionId
        e.creationTimeMillis = creationTimeMillis
        e.lastPostIndex = lastPostIndex
    }

    override fun Entity.toObject() = MessageSession().apply {
        val e = this@toObject
        queueId = e.queueId ?: e.key.parent?.id ?: 0L
        sessionId = e.sessionId ?: e.key.id
        creationTimeMillis = e.creationTimeMillis ?: 0L
        lastPostIndex = e.lastPostIndex ?: 0L
    }

    fun loadMessageSession(queueId: Long, sessionId: Long): MessageSession? =
        logged({ "loadMessageSession(queueId=$queueId, sessionId=$sessionId) -> $it" }) {
            load(keyOf(queueId, sessionId))
        }

    fun loadLastMessageSession(queueId: Long): MessageSession? =
        logged({ "loadLastMessageSession(queueId=$queueId) -> $it" }){
            query {
                filterEq(Entity::queueId, queueId)
                sortDescBy(Entity::sessionId)
            }.asList(1).singleOrNull()
        }

    fun Transaction.storeMessageSession(session: MessageSession) =
        logged({ "storeMessageSession($session) -> $it" }) {
            store(session)
        }
}