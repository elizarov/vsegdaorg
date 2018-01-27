package org.vsegda.util;

/**
 * @author Roman Elizarov
 */
public enum TimePeriodUnit {
    SECOND('s', TimeUtil.SECOND),
    MINUTE('m', TimeUtil.MINUTE),
    HOUR('h', TimeUtil.HOUR),
    DAY('d', TimeUtil.DAY),
    WEEK('w', TimeUtil.WEEK);

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
