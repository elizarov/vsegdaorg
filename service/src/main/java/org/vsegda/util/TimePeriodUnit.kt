package org.vsegda.util

enum class TimePeriodUnit(
    @get:JvmName("code") val code: Char,
    @get:JvmName("period") val period: Long
) {
    SECOND('s', TimeUtil.SECOND),
    MINUTE('m', TimeUtil.MINUTE),
    HOUR('h', TimeUtil.HOUR),
    DAY('d', TimeUtil.DAY),
    WEEK('w', TimeUtil.WEEK);

    override fun toString(): String = "1" + code
}
