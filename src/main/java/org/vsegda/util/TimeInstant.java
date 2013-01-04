package org.vsegda.util;

import org.apache.commons.beanutils.Converter;

import java.sql.Time;

/**
 * @author Roman Elizarov
 */
public class TimeInstant {
    private final long time;

    private TimeInstant(long time) {
        this.time = time;
    }

    public static TimeInstant valueOf(long time) {
        return new TimeInstant(time);
    }

    public static TimeInstant valueOf(String s) {
        long now = System.currentTimeMillis();
        long time;
        if (s.startsWith("+") || s.startsWith("-")) {
            time = now + TimePeriod.valueOf(s).period();
        }
        else
            time = TimeUtil.parseTime(s, now);
        return valueOf(time);
    }

    public long time() {
        return time;
    }

    @Override
    public String toString() {
        return TimeUtil.formatDateTime(time);
    }

    public static class Cnv implements Converter {
        public Object convert(Class clazz, Object o) {
            if (o == null)
               return null;
            String s = o.toString();
            if (s == null)
               return null;
            return valueOf(s);
        }
    }

}
