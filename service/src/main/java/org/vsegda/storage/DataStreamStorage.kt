package org.vsegda.storage

import com.google.appengine.api.datastore.*
import org.vsegda.data.*
import org.vsegda.shared.*
import org.vsegda.util.*

private var Entity.tag by Prop.string
private var Entity.name by Prop.string
private var Entity.mode by Prop.string
private var Entity.alertTimeout by Prop.long

object DataStreamStorage : BaseStorage<DataStream>() {
    override val kind = "DataStream"

    private fun keyOf(streamId: Long): Key = KeyFactory.createKey(kind, streamId)

    override fun DataStream.toKey() =
        if (streamId == 0L) null else keyOf(streamId)

    override fun DataStream.toEntity() = newEntity { e ->
        e.tag = tag
        e.name = name
        e.mode = mode.toString()
        if (alertTimeout != 0L) e.alertTimeout = alertTimeout
    }

    override fun Entity.toObject() = DataStream().apply {
        val e = this@toObject
        streamId = e.key?.id ?: 0L
        tag = e.tag
        name = e.name ?: ""
        mode = e.mode?.let { DataStreamMode.valueOf(it) } ?: DataStreamMode.ARCHIVE // legacy items are archived
        alertTimeout = e.alertTimeout ?: 0L
    }

    fun storeDataStream(stream: DataStream): Key =
        logged({ "storeDataStream($stream) -> $it" }) {
            store(stream)
        }

    fun deleteDataStream(stream: DataStream) =
        logged("deleteDataStream(streamId=${stream.streamId})") {
            delete(stream)
        }

    fun queryDataStreams(): List<DataStream> =
        logged({ "queryDataStreams() -> ${it.size} streams" }) {
            query { sortAscByKey() }.asList()
        }

    fun loadLastDataStream(): DataStream? =
        logged({ "loadLastDataStream() -> $it" }) {
            query { sortDescByKey() }.asList(1).singleOrNull()
        }

    fun loadDataStreamByTag(tag: String): DataStream? =
        logged({ "loadDataStreamByTag($tag) -> $it" }) {
            query {
                sortAscByKey()
                filterEq(Entity::tag, tag)
            }.asList(1).singleOrNull()
        }

    fun loadDataStreamById(id: Long): DataStream? =
        logged({ "loadDataStreamById($id) -> $it" }) {
            load(keyOf(id))
        }
}
