package org.vsegda.util;

import org.apache.commons.beanutils.Converter;

/**
 * @author Roman Elizarov
 */
public class TimeInstant {
    private final long time;
    private final TimePeriod period;
    private boolean nowOrFuture;

    private TimeInstant(long time, TimePeriod period) {
        this.time = time;
        this.period = period;
    }

    public static TimeInstant now() {
        return new TimeInstant(0, TimePeriod.ZERO);
    }

    public static TimeInstant valueOf(long time) {
        return new TimeInstant(time, null);
    }

    public static TimeInstant valueOf(TimePeriod period) {
        return new TimeInstant(0, period);
    }

    public static TimeInstant valueOf(String s) {
        if (s.isEmpty())
            return null;
        if (s.startsWith("+") || s.startsWith("-"))
            return new TimeInstant(0, TimePeriod.valueOf(s));
        else
            return new TimeInstant(TimeUtil.INSTANCE.parseTime(s), null);
    }

    public long time() {
        return period == null ? time : System.currentTimeMillis() + period.period();
    }

    public boolean isNowOrFuture() {
        return period == null ? time >= System.currentTimeMillis() : period.period() >= 0;
    }

    public TimeInstant subtract(TimePeriod other) {
        return period == null ?
                valueOf(time - other.period()) :
                valueOf(period.subtract(other));
    }

    public TimeInstant add(TimePeriod other) {
        return period == null ?
                valueOf(time + other.period()) :
                valueOf(period.add(other));
    }

    @Override
    public String toString() {
        return period == null ? TimeUtil.INSTANCE.formatDateTime(time) :
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
