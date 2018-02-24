package org.vsegda.data

const val TAG_ID_SEPARATOR = ":"

sealed class Code
data class Tag(val tag: String) : Code()
data class Id(val id: Long) : Code()

fun String.toCode(): Code {
    val i = lastIndexOf(TAG_ID_SEPARATOR)
    val id: String
    if (i >= 0)
        id = substring(i + TAG_ID_SEPARATOR.length)
    else
        id = this
    return id.toLongOrNull()?.let { Id(it) } ?: Tag(this)
}
