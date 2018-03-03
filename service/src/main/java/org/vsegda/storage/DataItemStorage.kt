package org.vsegda.storage

import com.google.appengine.api.datastore.*
import org.vsegda.data.*
import org.vsegda.util.*

private var Entity.streamId by Prop.long
private var Entity.timeMillis by Prop.long
private var Entity.value by Prop.double

object DataItemStorage : BaseStorage<DataItem>() {
    override val kind: String = "DataItem"

    private fun keyOf(itemId: Long): Key = KeyFactory.createKey(kind, itemId)

    override fun DataItem.toKey(): Key? =
        if (itemId == 0L) null else keyOf(itemId)

    override fun DataItem.toEntity() = newEntity { e ->
        e.streamId = streamId
        e.timeMillis = timeMillis
        e.value = value
    }

    override fun Entity.toObject() = DataItem().apply {
        val e = this@toObject
        itemId = e.key?.id ?: 0L
        streamId = e.streamId ?: 0L
        value = e.value ?: 0.0
        timeMillis = e.timeMillis ?: 0L
        
    }

    fun storeDataItem(item: DataItem): Key =
        logged({ "storeDataItem($item) -> $it" }) {
            store(item).also { item.itemId = it.id }
        }

    fun deleteDataItems(list: List<DataItem>) =
        logged("deleteDataItems(${list.size} items starting from ${list.firstOrNull()})") {
            deleteList(list)
        }

    fun queryDataItems(streamId: Long, from: TimeInstant?, to: TimeInstant?, n: Int): List<DataItem> =
        logged({ "queryDataItems(streamId=$streamId, from=$from, to=$to, n=$n) -> ${it.size} items" }) {
            query {
                filterEq(Entity::streamId, streamId)
                if (from != null) filterGreaterEq(Entity::timeMillis, from.time())
                if (to != null) filterLess(Entity::timeMillis, to.time())
                sortDescBy(Entity::timeMillis)
            }.asList(n).reversed()
        }

    fun loadFirstDataItem(streamId: Long): DataItem? =
        logged({ "loadFirstDataItem($streamId) -> $it" }) {
            query {
                filterEq(Entity::streamId, streamId)
                sortAscBy(Entity::timeMillis)
            }.asList(1).singleOrNull()
        }

    fun queryFirstDataItems(streamId: Long, timeLimit: Long, n: Int): List<DataItem> =
        logged({ "queryFirstDataItems(streamId=$streamId, timeLimit=${formatDateTime(timeLimit)}, n=$n) -> ${it.size} items" }) {
            query {
                filterEq(Entity::streamId, streamId)
                filterLess(Entity::timeMillis, timeLimit)
                sortAscBy(Entity::timeMillis)
            }.asList(n)
        }

    fun updateStreamId(fromId: Long, toId: Long) =
        logged("updateStreamId(fromId=$fromId, toId=$toId)") {
            queryAll(fromId).onEach { it.streamId = toId }.store()
        }

    fun removeAllByStreamId(streamId: Long) =
        logged("removeAllByStreamId(streamId=$streamId)") {
            queryAll(streamId).map { it.key }.delete()
        }

    private fun queryAll(streamId: Long): Sequence<Entity> =
        query { filterEq(Entity::streamId, streamId) }.asEntitySequence()
}
