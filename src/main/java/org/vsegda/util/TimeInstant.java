package org.vsegda.util;

import org.apache.commons.beanutils.Converter;

/**
 * @author Roman Elizarov
 */
public class TimeInstant {
    private final long time;
    private final TimePeriod period;

    public TimeInstant(long time, TimePeriod period) {
        this.time = time;
        this.period = period;
    }

    public static TimeInstant valueOf(long time) {
        return new TimeInstant(time, null);
    }

    public static TimeInstant valueOf(TimePeriod period) {
        return new TimeInstant(0, period);
    }

    public static TimeInstant valueOf(String s) {
        if (s.startsWith("+") || s.startsWith("-"))
            return new TimeInstant(0, TimePeriod.valueOf(s));
        else
            return new TimeInstant(TimeUtil.parseTime(s), null);
    }

    public long time() {
        return period == null ? time : System.currentTimeMillis() + period.period();
    }

    @Override
    public String toString() {
        return period == null ? TimeUtil.formatDateTime(time) :
                ((period.period() >= 0 ? "+" : "") + period);
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
