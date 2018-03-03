package org.vsegda.service

import org.vsegda.data.*
import org.vsegda.storage.*
import org.vsegda.util.*
import java.util.concurrent.*
import kotlin.math.*

object DataStreamService : Logged {
    private const val FIRST_STREAM_ID = 1000L
    private const val CACHE_TIMEOUT = 30_000L // cache all streams for 30 sec

    private val idCache = ConcurrentHashMap<Long, CachedStream>()
    private val tagCache = ConcurrentHashMap<String, CachedStream>()

    @Volatile
    private var allCache: CachedStreams? = null

    val dataStreams: List<DataStream>
        get() = allCache.takeIf { it.isUpToDate() }?.streams ?:
            DataStreamStorage.queryDataStreams().apply {
                forEach { cache(it) }
            }.also {
                allCache = CachedStreams(System.currentTimeMillis(), it)
            }

    fun resolveDataStreamByCode(code: String): DataStream? = code.toCode().let {
        when (it) {
            is Tag -> resolveDataStreamByTag(it.tag)
            is Id -> resolveDataStreamById(it.id)
        }
    }

    private fun resolveDataStreamByCodeOrCreate(code: String): DataStream = code.toCode().let {
        when (it) {
            is Tag -> resolveDataStreamByTagOrCreate(it.tag)
            is Id -> resolveDataStreamByIdOrCreate(it.id)
        }
    }

    private fun resolveDataStreamByTag(tag: String): DataStream? =
        tagCache[tag]?.stream?.takeIf { it.tag == tag } ?:
            DataStreamStorage.loadDataStreamByTag(tag)?.also { cache(it) }

    fun resolveDataStreamByTagOrCreate(tag: String): DataStream =
        resolveDataStreamByTag(tag) ?: run {
            log.info("Creating new data stream with tag=$tag")
            // find last stream
            val lastStream = DataStreamStorage.loadLastDataStream()
            DataStream(max(FIRST_STREAM_ID, (lastStream?.streamId ?: 0) + 1), tag).also {
                DataStreamStorage.storeDataStream(it)
                clearCache()
            }
        }

    fun resolveDataStreamById(id: Long): DataStream? =
        idCache[id]?.stream?.takeIf { it.streamId == id } ?:
            DataStreamStorage.loadDataStreamById(id)?.also { cache(it) }

    fun resolveDataStreamByIdOrCreate(id: Long, failWhenExists: Boolean = false): DataStream {
        val exists = resolveDataStreamById(id)
        if (failWhenExists && exists != null)
            throw IllegalArgumentException("Stream with id=$id already exists")
        return exists ?: run {
            log.info("Creating new data stream with id=$id")
            DataStream(id).also {
                DataStreamStorage.storeDataStream(it)
                clearCache()
            }
        }
    }

    fun resolveDataStream(stream: DataStream): DataStream = if (stream.streamId != 0L)
        resolveDataStreamByIdOrCreate(stream.streamId)
    else
        resolveDataStreamByCodeOrCreate(stream.tag!!)

    fun removeDataStream(stream: DataStream) {
        DataStreamStorage.deleteDataStream(stream)
        clearCache()
    }

    fun updateDataStream(stream: DataStream) {
        DataStreamStorage.storeDataStream(stream)
        clearCache()
    }

    private fun cache(stream: DataStream) {
        val cached = CachedStream(stream.streamId, stream.tag, stream)
        idCache[cached.id] = cached
        if (cached.tag != null) tagCache[cached.tag] = cached
    }

    private fun clearCache() {
        allCache = null 
        idCache.clear()
        tagCache.clear()
    }

    class CachedStream(
        val id: Long,
        val tag: String?,
        val stream: DataStream
    )

    class CachedStreams(
        val time: Long,
        val streams: List<DataStream>
    )

    private fun CachedStreams?.isUpToDate() =
        this != null && System.currentTimeMillis() < time + CACHE_TIMEOUT
}