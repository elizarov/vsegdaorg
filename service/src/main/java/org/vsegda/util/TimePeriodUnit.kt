package org.vsegda.util

enum class TimePeriodUnit(
    @get:JvmName("code") val code: Char,
    @get:JvmName("period") val period: Long
) {
    SECOND('s', org.vsegda.util.SECOND),
    MINUTE('m', org.vsegda.util.MINUTE),
    HOUR('h', org.vsegda.util.HOUR),
    DAY('d', org.vsegda.util.DAY),
    WEEK('w', org.vsegda.util.WEEK);

    override fun toString(): String = "1" + code
}
