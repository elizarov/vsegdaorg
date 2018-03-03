package org.vsegda.util

import org.apache.commons.beanutils.Converter

class TimeInstant private constructor(
    private val time: Long?,
    private val period: TimePeriod?
) {
    init { require(if (period == null) time != null else time == null) }

    private var _now: Long? = null
    private fun now(): Long = _now ?: System.currentTimeMillis().also { _now = it }
    
    val isNowOrFuture: Boolean
        get() = if (period == null) time!! >= now() else period.period >= 0

    fun time(): Long = if (period == null) time!! else now() + period.period

    operator fun plus(other: TimePeriod): TimeInstant = if (period == null)
        valueOf(time!! + other.period)
    else
        valueOf(period.minus(other))

    operator fun minus(other: TimePeriod): TimeInstant = if (period == null)
        valueOf(time!! - other.period)
    else
        valueOf(period.plus(other))

    override fun toString(): String = if (period == null)
        formatDateTime(time!!)
    else
        (if (period.period >= 0) "+" else "") + period

    class Cnv : Converter {
        @Suppress("UNCHECKED_CAST")
        override fun <T : Any?> convert(type: Class<T>, value: Any?): T {
            check(type === TimeInstant::class.java)
            return convert(value) as T
        }

        private fun convert(o: Any?): TimeInstant? {
            return valueOf(o?.toString() ?: return null)
        }
    }

    companion object {
        fun now(): TimeInstant = TimeInstant(null, TimePeriod.ZERO)
        fun valueOf(time: Long): TimeInstant = TimeInstant(time, null)
        fun valueOf(period: TimePeriod): TimeInstant = TimeInstant(null, period)

        fun valueOf(s: String): TimeInstant? {
            if (s.isEmpty()) return null
            return if (s.startsWith("+") || s.startsWith("-"))
                TimeInstant(null, TimePeriod.valueOf(s))
            else
                TimeInstant(parseTime(s), null)
        }
    }
}
