package org.vsegda.util;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author Roman Elizarov
 */
public class TimeUtil {
    public static final TimeZone TIMEZONE = TimeZone.getTimeZone("Europe/Moscow");

    public static final long SECOND = 1000L;
    public static final long MINUTE = 60 * SECOND;
    public static final long HOUR = 60 * MINUTE;
    public static final long DAY = 24 * HOUR;

    private static SimpleDateFormat getDateTimeFormat() {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd'T'HHmmss.SSS");
        format.setTimeZone(TIMEZONE);
        return format;
    }

    public static String formatDateTime(long timeMillis) {
        return getDateTimeFormat().format(new Date(timeMillis));
    }

   public static String formatDateTimeDifference(long timeMillis, long now) {
        StringBuilder sb = new StringBuilder();
        long diff = now - timeMillis;
        if (diff < 0) {
            sb.append("-");
            diff = -diff;
        }
        if (diff < HOUR)
            sb.append(String.format("%dm%02ds", diff / MINUTE, (diff % MINUTE) / SECOND));
        else if (diff < DAY)
            sb.append(String.format("%dh%02dm", diff / HOUR, (diff % HOUR) / MINUTE));
        else
            sb.append(String.format("%dd%02dh", diff / DAY, (diff % DAY) / HOUR));
        return sb.toString();
    }

    public static long parseTime(String s, long now) {
        if (s.isEmpty())
            return now;
        ParsePosition pos = new ParsePosition(0);
        Date date = getDateTimeFormat().parse(s, pos);
        if (date != null && pos.getIndex() == s.length())
            return date.getTime();
        try {
            return now + Long.parseLong(s);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid time format: " + s, e);
        }
    }

    public static String getFormatClass(long timeMillis, long now) {
        // Check if older than 15 mins ago
        return timeMillis < now - 15 * 60000L ? "old" : "recent";
    }

    public static long getArchiveLimit(long firstTime) {
        Calendar cal = Calendar.getInstance(TIMEZONE);
        cal.setTimeInMillis(firstTime);
        cal.set(Calendar.HOUR_OF_DAY, cal.getMaximum(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.MINUTE, cal.getMaximum(Calendar.MINUTE));
        cal.set(Calendar.SECOND, cal.getMaximum(Calendar.SECOND));
        cal.set(Calendar.MILLISECOND, cal.getMaximum(Calendar.MILLISECOND));
        return cal.getTimeInMillis();
    }
}
