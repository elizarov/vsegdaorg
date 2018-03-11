package org.vsegda.util

enum class TimeUnit(
    @get:JvmName("code") val code: Char,
    @get:JvmName("period") val period: Long
) {
    SECOND('s', org.vsegda.util.SECOND),
    MINUTE('m', org.vsegda.util.MINUTE),
    HOUR('h', org.vsegda.util.HOUR),
    DAY('d', org.vsegda.util.DAY),
    WEEK('w', org.vsegda.util.WEEK),
    MONTH('M', org.vsegda.util.MONTH),
    YEAR('Y', org.vsegda.util.YEAR);

    override fun toString(): String = "1" + code
}
