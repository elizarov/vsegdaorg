package org.vsegda.request

import org.apache.commons.beanutils.*
import org.vsegda.util.*

enum class ConflateOp(private val str: String) {
    MAX("Max"),
    MIN("Min"),
    LAST("Last");

    override fun toString(): String = str

    class Cnv : Converter {
        @Suppress("UNCHECKED_CAST")
        override fun <T : Any?> convert(type: Class<T>?, value: Any?): T {
            check(type === ConflateOp::class.java)
            return convert(value) as T
        }

        private fun convert(o: Any?): ConflateOp? {
            return ConflateOp.valueOf(o?.toString()?.toUpperCase() ?: return null)
        }
    }
}

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
    MONTH6("6M", 6 *  TimeUnit.MONTH, 3 * TimeUnit.HOUR),
    YEAR("1Y", 1 *  TimeUnit.YEAR, 6 * TimeUnit.HOUR),
    YEAR5("5Y", 5 *  TimeUnit.YEAR, 1 * TimeUnit.DAY);
}

fun conflationForSpan(span: TimePeriod): TimePeriod? =
    DataSpan.values()
        .lastOrNull { span >= it.span }
        ?.conflate

