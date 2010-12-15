package org.vsegda.util;

import java.text.SimpleDateFormat;
import java.text.ParsePosition;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author Roman Elizarov
 */
public class TimeUtil {
    public static final long SECOND = 1000L;
    public static final long MINUTE = 60 * SECOND;
    public static final long HOUR = 60 * MINUTE;
    public static final long DAY = 24 * HOUR;

    private static SimpleDateFormat getDateTimeFormat() {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd'T'HHmmss.SSS");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
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
}
