package org.vsegda.util

import java.util.logging.*
import kotlin.system.*

val Any.log: Logger get() = Logger.getLogger(this::class.java.name)

inline fun <T> Any.logged(msg: String, start: Boolean = false, body: () -> T): T {
    if (start) log.info("START $msg")
    return logged({ msg }, body)
}

@Suppress("UNCHECKED_CAST")
inline fun <T> Any.logged(msg: (T) -> String, body: () -> T): T {
    var result: T? = null
    val time = measureTimeMillis {
        result = body()
    }
    log.info("FINISH ${msg(result as T)} in $time ms")
    return result as T
}
