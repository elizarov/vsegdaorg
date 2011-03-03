package org.vsegda.util;

/**
 * @author Roman Elizarov
 */
public enum TimePeriodUnit {
    SECOND('s', 1000),
    MINUTE('m', SECOND.period * 60),
    HOUR('h', MINUTE.period * 60),
    DAY('d', HOUR.period * 24),
    WEEK('w', DAY.period * 7);

    private final char code;
    private final long period;

    TimePeriodUnit(char code, long period) {
        this.code = code;
        this.period = period;
    }

    public char code() {
        return code;
    }

    public long period() {
        return period;
    }

  @Override
    public String toString() {
        return "1" + code;
    }
}
