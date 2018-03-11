package org.vsegda.request

import org.vsegda.util.*

enum class DataSpan(
    val text: String,
    val span: TimePeriod,
    val conflate: TimePeriod? = null
) {
    DAY("1D", 1 * TimeUnit.DAY),
    DAY3("3D", 3 * TimeUnit.DAY),
    WEEK("1W", 1 * TimeUnit.WEEK),
    WEEK2("2W", 2 * TimeUnit.WEEK, 10 * TimeUnit.MINUTE),
    MONTH("1M", 1 *  TimeUnit.MONTH, 30 * TimeUnit.MINUTE),
    MONTH3("3M", 3 *  TimeUnit.MONTH, 1 * TimeUnit.HOUR),
    MONTH6("6M", 6 *  TimeUnit.MONTH, 3 * TimeUnit.HOUR);
}

fun conflationForSpan(span: TimePeriod): TimePeriod? =
    DataSpan.values()
        .lastOrNull { span >= it.span }
        ?.conflate

