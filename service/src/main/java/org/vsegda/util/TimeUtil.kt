package org.vsegda.util

import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

object TimeUtil {
    private val TIMEZONE = TimeZone.getTimeZone("Europe/Moscow")
    private const val NA_STRING = "N/A"

    const val SECOND = 1000L
    const val MINUTE = 60 * SECOND
    const val HOUR = 60 * MINUTE
    const val DAY = 24 * HOUR
    const val WEEK = 7 * DAY

    private val dateTimeFormat: SimpleDateFormat
        get() = SimpleDateFormat("yyyyMMdd'T'HHmmss").apply {
            timeZone = TIMEZONE
        }

    fun formatDateTime(timeMillis: Long): String =
        if (timeMillis == 0L) NA_STRING else dateTimeFormat.format(Date(timeMillis))

    fun formatDateTimeDifference(timeMillis: Long, now: Long): String {
        if (timeMillis == 0L)
            return NA_STRING
        val sb = StringBuilder()
        var diff = now - timeMillis
        if (diff < 0) {
            sb.append("-")
            diff = -diff
        }
        when {
            diff < HOUR -> sb.append(String.format("%dm%02ds", diff / MINUTE, diff % MINUTE / SECOND))
            diff < DAY -> sb.append(String.format("%dh%02dm", diff / HOUR, diff % HOUR / MINUTE))
            else -> sb.append(String.format("%dd%02dh", diff / DAY, diff % DAY / HOUR))
        }
        return sb.toString()
    }

    fun parseTime(s: String): Long {
        if (s.equals(NA_STRING, ignoreCase = true)) return 0
        val pos = ParsePosition(0)
        val date = dateTimeFormat.parse(s, pos) ?: throw IllegalArgumentException("Invalid time format: " + s)
        if (pos.index == s.length) return date.time
        if (s[pos.index] != '.') throw IllegalArgumentException("Invalid time format: " + s)
        // optional millis
        try {
            val mf = java.lang.Double.parseDouble("0" + s.substring(pos.index))
            return date.time + (1000 * mf).toInt()
        } catch (e: NumberFormatException) {
            throw IllegalArgumentException("Invalid time format: " + s, e)
        }
    }

    fun parseTime(s: String, now: Long): Long {
        if (s.isEmpty())
            return now
        try {
            return parseTime(s)
        } catch (e: IllegalArgumentException) {
            // ignore & fallback to parse as time period offset
        }
        try {
            return now + TimePeriod.valueOf(s).period()
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Invalid time format: " + s, e)
        }
    }

    fun getFormatClass(timeMillis: Long, now: Long): String =
        if (timeMillis < now - 15 * 60000L) "old" else "recent" // Check if older than 15 mins ago

    fun getArchiveLimit(firstTime: Long): Long {
        val cal = Calendar.getInstance(TIMEZONE)
        cal.timeInMillis = firstTime
        cal.add(Calendar.DATE, 1)
        cal.set(Calendar.HOUR_OF_DAY, cal.getMinimum(Calendar.HOUR_OF_DAY))
        cal.set(Calendar.MINUTE, cal.getMinimum(Calendar.MINUTE))
        cal.set(Calendar.SECOND, cal.getMinimum(Calendar.SECOND))
        cal.set(Calendar.MILLISECOND, cal.getMinimum(Calendar.MILLISECOND))
        return cal.timeInMillis
    }
}
