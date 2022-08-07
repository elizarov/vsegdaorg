package org.vsegda.util

import java.util.logging.*
import kotlin.system.*

interface Logged // marker interface

private const val TRIM_COMPANION = "\$Companion"

private fun String.toCategory(): String =
    if (this.endsWith(TRIM_COMPANION)) substring(0, length - TRIM_COMPANION.length) else this

private val logger = object : ClassValue<Logger>() {
    override fun computeValue(type: Class<*>) = Logger.getLogger(type.name.toCategory())
}

val Logged.log: Logger get() = logger.get(this::class.java)

inline fun <T> Logged.logged(
    msg: String,
    around: Boolean = false,
    noinline result: ((T) -> String)? = null, /* inliner fail with inline lambda */
    body: () -> T): T
{
    if (around) log.info("START $msg")
    val hdr = if (around) "FINISH $msg" else msg
    return logged(
        msg = {
            if (result == null) hdr else "$hdr -> ${result(it)}"
        },
        body = {
            try { body() }
            catch (e: Throwable) {
                log.log(Level.SEVERE, "FAILED $msg: $e")
                throw e
            }
        })
}

@Suppress("UNCHECKED_CAST")
inline fun <T> Logged.logged(msg: (T) -> String, body: () -> T): T {
    var result: T?
    val time = measureTimeMillis {
        result = body()
    }
    log.info("${msg(result as T)} in $time ms")
    return result as T
}
