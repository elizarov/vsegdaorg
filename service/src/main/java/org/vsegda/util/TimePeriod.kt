package org.vsegda.util

import org.apache.commons.beanutils.*

@Suppress("DataClassPrivateConstructor")
data class TimePeriod private constructor(
    @get:JvmName("period") val period: Long
): Comparable<TimePeriod> {
    operator fun plus(other: TimePeriod): TimePeriod = valueOf(period - other.period)
    operator fun minus(other: TimePeriod): TimePeriod = valueOf(period + other.period)
    override fun compareTo(other: TimePeriod): Int = period.compareTo(other.period)

    override fun toString(): String {
        val sb = StringBuilder()
        var r = period
        if (r < 0) {
            sb.append('-')
            r = -r
        }
        val units = TimeUnit.values()
        var cnt = 0
        var i = units.size
        while (--i >= 0) {
            val unit = units[i]
            val `val` = r / unit.period
            r %= unit.period
            val ms = unit === TimeUnit.SECOND && r > 0
            if (`val` > 0 || ms) {
                cnt++
                sb.append(`val`)
                if (ms)
                    sb.append('.')
                        .append(r / 100)
                        .append(r / 10 % 10)
                        .append(r % 10)
                sb.append(unit.code)
            }
        }
        if (cnt == 0)
            sb.append(r)
        return sb.toString()
    }

    class Cnv : Converter {
        @Suppress("UNCHECKED_CAST")
        override fun <T : Any?> convert(type: Class<T>?, value: Any?): T {
            check(type === TimePeriod::class.java)
            return convert(value) as T
        }

        private fun convert(o: Any?): TimePeriod? {
            return valueOf(o?.toString() ?: return null)
        }
    }

    companion object {
        val ZERO = TimePeriod(0)

        fun valueOf(period: Long): TimePeriod =
            if (period == 0L) ZERO else TimePeriod(period)

        fun valueOf(string: String?): TimePeriod {
            var s: String = string ?: return ZERO
            s = s.trim { it <= ' ' }
            if (s.isEmpty()) return ZERO
            var period: Long = 0
            var mul: Long = 1
            var i = 0
            if (s.startsWith("+")) {
                i++
            } else if (s.startsWith("-")) {
                i++
                mul = -1
            }
            val units = TimeUnit.values()
            var k = units.size - 1
            while (i < s.length) {
                var j = i
                while (j < s.length && numChar(s[j]))
                    j++
                val part = s.substring(i, j).toDouble()
                if (j >= s.length) {
                    period += part.toLong()
                    break
                }
                while (k >= 0 && s[j] != units[k].code)
                    k--
                if (k < 0)
                    throw IllegalArgumentException("Invalid time period code: " + s[j])
                period += (part * units[k].period).toLong()
                k--
                i = j + 1
            }
            return valueOf(mul * period)
        }

        private fun numChar(c: Char): Boolean = c in '0'..'9' || c == '.'
    }
}

operator fun Int.times(unit: TimeUnit) = TimePeriod.valueOf(this * unit.period)

