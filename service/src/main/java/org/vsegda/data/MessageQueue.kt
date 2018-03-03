package org.vsegda.data

class MessageQueue(
    var queueId: Long = 0
) {
    var name = ""
    var lastGetIndex: Long = 0
    var lastPostIndex: Long = 0
    var lastSessionId: Long = 0

    override fun toString(): String = "$queueId,$name,$lastGetIndex,$lastPostIndex,$lastSessionId"
}
