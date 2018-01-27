package org.vsegda.data;

/**
 * @author Roman Elizarov
 */
class DeltaUtil {
    static final int MAX_VALUE_PRECISION = 6;
    static final long[] POWER = new long[MAX_VALUE_PRECISION + 1];
    static final long MAX_VALUE_POWER;

    static {
        POWER[0] = 1;
        for (int i = 1; i <= MAX_VALUE_PRECISION; i++)
            POWER[i] = 10 * POWER[i - 1];
        MAX_VALUE_POWER = POWER[MAX_VALUE_PRECISION];
    }

    static int getPrecision(double value) {
        for (int i = 0; i < MAX_VALUE_PRECISION; i++) {
            double m = value * POWER[i];
            if (Math.abs(m - Math.floor(m + 0.5)) < 1.0 / POWER[MAX_VALUE_PRECISION - i])
                return i;
        }
        return MAX_VALUE_PRECISION;
    }

    static long roundTime(long timeMillis) {
        return timeMillis / DataArchive.TIME_PRECISION * DataArchive.TIME_PRECISION;
    }
}
