package org.vsegda.util

import org.apache.commons.beanutils.Converter

class TimeInstant private constructor(
    private val time: Long,
    private val period: TimePeriod?
) {
    val isNowOrFuture: Boolean
        get() = if (period == null) time >= System.currentTimeMillis() else period.period >= 0

    fun time(): Long = if (period == null) time else System.currentTimeMillis() + period.period

    operator fun plus(other: TimePeriod): TimeInstant = if (period == null)
        valueOf(time + other.period)
    else
        valueOf(period.minus(other))

    operator fun minus(other: TimePeriod): TimeInstant = if (period == null)
        valueOf(time - other.period)
    else
        valueOf(period.plus(other))

    override fun toString(): String = if (period == null)
        TimeUtil.formatDateTime(time)
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
        fun now(): TimeInstant = TimeInstant(0, TimePeriod.ZERO)
        fun valueOf(time: Long): TimeInstant = TimeInstant(time, null)
        fun valueOf(period: TimePeriod): TimeInstant = TimeInstant(0, period)

        fun valueOf(s: String): TimeInstant? {
            if (s.isEmpty()) return null
            return if (s.startsWith("+") || s.startsWith("-"))
                TimeInstant(0, TimePeriod.valueOf(s))
            else
                TimeInstant(TimeUtil.parseTime(s), null)
        }
    }
}
