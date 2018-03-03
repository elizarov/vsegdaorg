package org.vsegda.data

import org.vsegda.util.*

class MessageSession(
    var queueId: Long = 0,
    var sessionId: Long = 0,
    var creationTimeMillis: Long = 0
) {
    var lastPostIndex: Long = -1

    override fun toString(): String =
        "$queueId,$sessionId,${TimeUtil.formatDateTime(creationTimeMillis)},$lastPostIndex"
}
