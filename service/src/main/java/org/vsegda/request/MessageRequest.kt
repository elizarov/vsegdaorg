package org.vsegda.request

import io.ktor.request.*
import org.vsegda.data.*
import org.vsegda.service.*
import org.vsegda.util.*
import javax.servlet.http.*

class MessageRequest() : AbstractRequest() {
    var id: IdList? = null
    var isTake: Boolean = false
    var index: Long = 0
    var last = 100 // last 100 items by default
    var first: Int = 0

    constructor(req: HttpServletRequest, post: Boolean) : this() {
        init(req)
        validate(post)
    }

    constructor(req: ApplicationRequest, post: Boolean = false) : this() {
        init(req)
        validate(post)
    }

    private fun validate(post: Boolean) {
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

    fun query(): List<MessageItem> =
        logged("Message request $this", around = true, result = { "${it.size} items" }) {
            val id = this.id
            if (id == null) {
                MessageQueueService.messageQueues
                    .map {
                        MessageItemService.getMessageItem(it.queueId, it.lastPostIndex) ?:
                            MessageItem(it.queueId, it.lastPostIndex)
                    }
            } else {
                id.asSequence()
                    .mapNotNull { MessageQueueService.resolveMessageQueueByCode(it) }
                    .flatMap { queue ->
                        val list = if (isTake) {
                            index = Math.max(index, queue.lastGetIndex)
                            queue.lastGetIndex = index
                            MessageQueueService.updateMessageQueue(queue)
                            MessageItemService.getNewMessageItems(queue.queueId, index, first, last)
                        } else {
                            MessageItemService.getLastMessagesItems(queue.queueId, first, last)
                        }
                        list.asSequence()
                    }
                    .toList()
            }
        }
}