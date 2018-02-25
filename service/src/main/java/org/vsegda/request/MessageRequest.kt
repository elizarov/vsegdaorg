package org.vsegda.request

import com.google.appengine.api.datastore.*
import org.vsegda.data.*
import org.vsegda.factory.*
import org.vsegda.storage.*
import org.vsegda.util.*
import javax.servlet.http.*

class MessageRequest(req: HttpServletRequest, post: Boolean) : AbstractRequest() {
    var id: IdList? = null
    var isTake: Boolean = false
    var index: Long = 0
    var last = 100 // last 100 items by default
    var first: Int = 0

    init {
        init(req)
        if (id != null && id!!.isSingleton && post)
            isTake = true // force take on POST request if "id" is set
        if (index != 0L && (id == null || !id!!.isSingleton))
            throw IllegalArgumentException("cannot specify index without a singleton id")
        if (index != 0L && !isTake)
            throw IllegalArgumentException("cannot specify index without take")
        if (isTake && (id == null || !id!!.isSingleton))
            throw IllegalArgumentException("cannot specify take without a singleton id")
        if (isTake && first != 0)
            throw IllegalArgumentException("cannot specify take with first")
    }

    // todo: ???
    fun query(): List<MessageItem> =
        logged("Message request $this", around = true) {
            if (id == null) {
                val query = Query("MessageQueue")
                query.addSort("__key__", Query.SortDirection.ASCENDING)
                val entities = DS.prepare(query).asIterable(
                    FetchOptions.Builder.withOffset(first).limit(last).chunkSize(last)
                )
                entities.map { MessageItemStorage.toMessageItem(it) }
            } else {
                throw TODO("Message query for id list is not supported")
            }
        }
}