package org.vsegda.storage

import com.google.appengine.api.datastore.*
import org.vsegda.data.*
import org.vsegda.util.*
import java.util.*

private var Entity.streamId by Prop.long
private var Entity.count by Prop.int
private var Entity.firstValue by Prop.double
private var Entity.firstTimeMillis by Prop.long
private var Entity.highValue by Prop.double
private var Entity.lowValue by Prop.double
private var Entity.encodedItems by Prop.blob

object DataArchiveStorage : BaseStorage<DataArchive>() {
    override val kind: String = "DataArchive"

    override fun DataArchive.toKey(): Key = key ?: error("Data archive $this is not persistent")

    override fun DataArchive.toEntity(): Entity = Entity(kind).also { e ->
        e.streamId = streamId
        e.count = count
        e.firstValue = firstValue
        e.firstTimeMillis = firstTimeMillis
        e.highValue = highValue
        e.lowValue = lowValue
        e.encodedItems = encodedItems
    }

    override fun Entity.toObject(): DataArchive = DataArchive().apply {
        val e = this@toObject
        streamId = e.streamId ?: 0
        count = e.count ?: 0
        firstValue = e.firstValue ?: Double.NaN
        firstTimeMillis = e.firstTimeMillis ?: 0L
        highValue = e.highValue ?: Double.NaN
        lowValue = e.lowValue ?: Double.NaN
        encodedItems = e.encodedItems
    }

    fun storeDataArchive(archive: DataArchive) =
        logged("storeDataArchive($archive") {
            store(archive).also { archive.key = it }
        }

    fun queryItemsFromDataArchives(streamId: Long, from: TimeInstant?, to: TimeInstant?, nItems: Int): List<DataItem> =
        logged({ "queryItemsFromDataArchives(streamId=$streamId, from=$from, to=$to, nItem=$nItems) -> ${it.size} items" }) {
            val estimatedRange = 1 + nItems / DataArchive.COUNT_ESTIMATE // estimate number of archives
            val items = ArrayList<DataItem>()
            val it = query {
                filterEq(Entity::streamId, streamId)
                if (from != null) filterGreaterEq(Entity::firstTimeMillis, from.time())
                if (to != null) filterLess(Entity::firstTimeMillis, to.time())
                sortDescBy(Entity::firstTimeMillis)
            }.asSequence(estimatedRange)
            for (archive in it) {
                val prevSize = items.size
                items.addAll(archive.items)
                items.subList(prevSize, items.size).reverse() // turn ascending items into descending
                if (items.size >= nItems) {
                    items.subList(nItems, items.size).clear()
                    break
                }
            }
            items.reverse() // turn descending into ascending order
            items
        }

    fun updateStreamId(fromId: Long, toId: Long) =
        logged("updateStreamId(fromId=$fromId, toId=$toId)") {
            queryAll(fromId).onEach { it.streamId = toId }.store()
        }

    fun removeAllByStreamId(streamId: Long) =
        logged("removeAllByStreamId($streamId)") {
            queryAll(streamId).map { it.key }.delete()
        }

    private fun queryAll(streamId: Long): Sequence<Entity> =
        query { filterEq(Entity::streamId, streamId) }.asEntitySequence()
}
