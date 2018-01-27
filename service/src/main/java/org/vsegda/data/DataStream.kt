package org.vsegda.data

import org.vsegda.shared.*

/**
 * @author Roman Elizarov
 */
class DataStream {
    var streamId: Long = 0L
    var tag: String? = null
        set(tag) {
            field = tag?.trim { it <= ' ' }?.takeUnless { it.isEmpty() }
        }
    var name: String = ""
        set(name) {
            field = name.trim { it <= ' ' }
        }
    var alertTimeout: Long = 0L
    var mode: DataStreamMode = DataStreamMode.LAST

    /**
     * Returns stream id or tag if defined.
     */
    val code: String
        get() = tag?.let { it + (if (streamId == 0L) "" else TAG_ID_SEPARATOR + streamId) } ?: streamId.toString()

    val nameOrCode: String
        get() = if (this.name.isEmpty()) code else this.name

    constructor()

    constructor(streamId: Long?) {
        this.streamId = streamId!!
    }

    override fun toString(): String = "$code,$name"

    companion object {
        const val TAG_ID_SEPARATOR = ":"
    }
}