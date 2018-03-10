package org.vsegda.util

import org.apache.commons.beanutils.Converter

class TimePeriod private constructor(
    @get:JvmName("period") val period: Long
) {
    operator fun plus(other: TimePeriod): TimePeriod = valueOf(period - other.period)
    operator fun minus(other: TimePeriod): TimePeriod = valueOf(period + other.period)

    override fun equals(other: Any?): Boolean =
        other is TimePeriod && other.period == period

    override fun hashCode(): Int = period.hashCode()

    override fun toString(): String {
        val sb = StringBuilder()
        var r = period
        if (r < 0) {
            sb.append('-')
            r = -r
        }
        val units = TimePeriodUnit.values()
        var cnt = 0
        var i = units.size
        while (--i >= 0) {
            val unit = units[i]
            val `val` = r / unit.period
            r %= unit.period
            val ms = unit === TimePeriodUnit.SECOND && r > 0
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

        fun valueOf(period: Long): TimePeriod = TimePeriod(period)
        fun valueOf(n: Long, unit: TimePeriodUnit): TimePeriod = TimePeriod(n * unit.period)

        fun valueOf(string: String?): TimePeriod {
            var s: String = string ?: return TimePeriod(0)
            s = s.trim { it <= ' ' }
            if (s.isEmpty()) return TimePeriod(0)
            var period: Long = 0
            var mul: Long = 1
            var i = 0
            if (s.startsWith("+")) {
                i++
            } else if (s.startsWith("-")) {
                i++
                mul = -1
            }
            val units = TimePeriodUnit.values()
            var k = units.size - 1
            while (i < s.length) {
                var j = i
                while (j < s.length && numChar(s[j]))
                    j++
                val part = java.lang.Double.parseDouble(s.substring(i, j))
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
