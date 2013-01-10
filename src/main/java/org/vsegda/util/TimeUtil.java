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
    public static final String NA_STRING = "N/A";

    public static final long SECOND = 1000L;
    public static final long MINUTE = 60 * SECOND;
    public static final long HOUR = 60 * MINUTE;
    public static final long DAY = 24 * HOUR;
    public static final long WEEK = 7 * DAY;

    private static SimpleDateFormat getDateTimeFormat() {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
        format.setTimeZone(TIMEZONE);
        return format;
    }

    public static String formatDateTime(long timeMillis) {
        return timeMillis == 0 ? NA_STRING : getDateTimeFormat().format(new Date(timeMillis));
    }

    public static String formatDateTimeDifference(long timeMillis, long now) {
        if (timeMillis == 0)
            return NA_STRING;
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

    public static long parseTime(String s) {
        if (s.equalsIgnoreCase(NA_STRING))
            return 0;
        ParsePosition pos = new ParsePosition(0);
        Date date = getDateTimeFormat().parse(s, pos);
        if (date == null)
            throw new IllegalArgumentException("Invalid time format: " + s);
        if (pos.getIndex() == s.length())
            return date.getTime();
        if (s.charAt(pos.getIndex()) != '.')
            throw new IllegalArgumentException("Invalid time format: " + s);
        // optional millis
        try {
            double mf = Double.parseDouble("0" + s.substring(pos.getIndex()));
            return date.getTime() + (int) (1000 * mf);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid time format: " + s, e);
        }
    }

    public static long parseTime(String s, long now) {
        if (s.isEmpty())
            return now;
        try {
            return parseTime(s);
        } catch (IllegalArgumentException e) {
            // ignore & fallback to parse as time period offset
        }
        try {
            return now + TimePeriod.valueOf(s).period();
        } catch (IllegalArgumentException e) {
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
        cal.add(Calendar.DATE, 1);
        cal.set(Calendar.HOUR_OF_DAY, cal.getMinimum(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.MINUTE, cal.getMinimum(Calendar.MINUTE));
        cal.set(Calendar.SECOND, cal.getMinimum(Calendar.SECOND));
        cal.set(Calendar.MILLISECOND, cal.getMinimum(Calendar.MILLISECOND));
        return cal.getTimeInMillis();
    }
}
