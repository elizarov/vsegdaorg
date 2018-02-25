package org.vsegda.request

import org.vsegda.data.*
import org.vsegda.service.*
import org.vsegda.util.*
import java.util.*
import javax.servlet.http.*
import kotlin.math.*

class DataRequest(req: HttpServletRequest? = null) : AbstractRequest() {
    // request props

    var id: IdList? = null
        set(id) {
            updateQueryString("id", id?.toString())
            field = id
        }

    var to: TimeInstant? = null
        set(to) {
            updateQueryString("to", to?.toString())
            field = to
        }

    var span: TimePeriod? = TimePeriod.valueOf(1, TimePeriodUnit.WEEK) // one week by default
        set(span) {
            updateQueryString("span", span?.toString())
            field = span
        }

    var n = 2500 // enough data item for a week of data assuming reading every 5 mins
        set(n) {
            updateQueryString("n", if (n == 0) null else n.toString())
            field = n
        }

    var filter = 5.0 // 5 sigmas by default

    // derived props

    val from: TimeInstant?
        get() = span?.let { (to ?: TimeInstant.now()) - it }

    @get:JvmName("hasNavigation")
    val hasNavigation: Boolean
        get() = id != null && span != null
    
    init {
        init(req)
    }
    
    fun queryList(): List<DataItem> {
        val result = queryMap().values.flatMap { it }
        return if (id == null) result else {
            // reorder descending by time (it is stable, for order by ids is kept for same time)
            result.sortedWith(Collections.reverseOrder(DataItem.BY_TIME))
        }
    }

    fun queryMap(): Map<DataStream, List<DataItem>> =
        logged("Data query $this", around = true, result = { "${it.size} items" }) {
            val id = this.id
            if (id == null) {
                DataStreamService.dataStreams
                    .associate { it to listOf(DataItemService.getLastDataItem(it)) }
            } else {
                id.asSequence()
                    .mapNotNull { DataStreamService.resolveDataStreamByCode(it) }
                    .associate { it to DataItemService.getDataItems(it, from, to, n).filter() }
            }
        }

    private fun List<DataItem>.filter(): List<DataItem> {
        if (filter <= 0) return this
        val items = toMutableList()
        for (repeat in 0..19) {
            val n = items.size - 1
            if (n < 2) return items
            var sum = 0.0
            var sum2 = 0.0
            for (i in 0 until n) {
                val d = diff(items, i)
                sum += d
                sum2 += d * d
            }
            val m = sum / n
            val s2 = sum2 / (n - 1) - sum * sum / (n * n - n)
            if (s2 <= 0) return items
            val limit = Math.sqrt(s2) * filter
            var j = 0
            for (i in items.indices) {
                if (absError(items, i, m) <= limit || absError(items, i - 1, m) <= limit) {
                    items[j++] = items[i]
                }
            }
            if (j == items.size) return items
            items.subList(j, items.size).clear()
        }
        return items
    }

    private fun absError(items: List<DataItem>, i: Int, m: Double) =
        if (i < 0 || i >= items.size - 1) Double.POSITIVE_INFINITY else
            abs(m - abs(diff(items, i)))
    
    private fun diff(items: List<DataItem>, i: Int): Double = items[i].value - items[i + 1].value
}
