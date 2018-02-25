package org.vsegda.data

import kotlin.math.*

const val MAX_VALUE_PRECISION = 6

val POWER = LongArray(MAX_VALUE_PRECISION + 1)

val MAX_VALUE_POWER: Long = run {
    POWER[0] = 1
    for (i in 1..MAX_VALUE_PRECISION)
        POWER[i] = 10 * POWER[i - 1]
    POWER[MAX_VALUE_PRECISION]
}

fun computePrecision(value: Double): Int {
    for (i in 0 until MAX_VALUE_PRECISION) {
        val m = value * POWER[i]
        if (Math.abs(m - floor(m + 0.5)) < 1.0 / POWER[MAX_VALUE_PRECISION - i])
            return i
    }
    return MAX_VALUE_PRECISION
}

fun roundTime(timeMillis: Long): Long = timeMillis / TIME_PRECISION * TIME_PRECISION

