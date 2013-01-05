package org.vsegda.util;

import org.apache.commons.beanutils.Converter;

/**
 * @author Roman Elizarov
 */
public class TimePeriod {
    private final long period;

    public static TimePeriod valueOf(long period) {
        return new TimePeriod(period);
    }

    public static TimePeriod valueOf(long n, TimePeriodUnit unit) {
        return new TimePeriod(n * unit.period());
    }

    public static TimePeriod valueOf(String s) {
        if (s == null)
            return new TimePeriod(0);
        s = s.trim();
        if (s.isEmpty())
            return new TimePeriod(0);
        long period = 0;
        long mul = 1;
        int i = 0;
        if (s.startsWith("+"))
            i++;
        else if (s.startsWith("-")) {
            i++;
            mul = -1;
        }
        TimePeriodUnit[] units = TimePeriodUnit.values();
        int k = units.length - 1;
        while (i < s.length()) {
            int j = i;
            while (j < s.length() && numChar(s.charAt(j)))
                j++;
            double part = Double.parseDouble(s.substring(i, j));
            if (j >= s.length()) {
                period += part;
                break;
            }
            while (k >= 0 && s.charAt(j) != units[k].code())
                k--;
            if (k < 0)
                throw new IllegalArgumentException("Invalid time period code: " + s.charAt(j));
            period += part * units[k].period();
            k--;
            i = j + 1;
        }
        return valueOf(mul * period);
    }

    private static boolean numChar(char c) {
        return c >= '0' && c <= '9' || c == '.';
    }

    private TimePeriod(long period) {
        this.period = period;
    }

    public long period() {
        return period;
    }

    public Long periodOrNull() {
        return period == 0 ? null : period;
    }

    public String format(int limit) {
        StringBuilder sb = new StringBuilder();
        long r = period;
        if (r < 0) {
            sb.append('-');
            r = -r;
        }
        TimePeriodUnit[] units = TimePeriodUnit.values();
        int cnt = 0;
        for (int i = units.length; --i >= 0; ) {
            TimePeriodUnit unit = units[i];
            if (cnt > 0 || r >= unit.period()) {
                cnt++;
                sb.append(r / unit.period());
                r %= unit.period();
                if (unit == TimePeriodUnit.SECOND && r > 0 && cnt < limit)
                    sb.append('.')
                            .append(r / 100)
                            .append((r / 10) % 10)
                            .append(r % 10);
                sb.append(unit.code());
                if (cnt >= limit)
                    break;
            }
        }
        if (cnt == 0)
            sb.append(r);
        return sb.toString();
    }

    @Override
    public String toString() {
        return format(Integer.MAX_VALUE);
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
