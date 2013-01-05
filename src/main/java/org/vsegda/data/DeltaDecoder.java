package org.vsegda.data;

import java.io.ByteArrayInputStream;

/**
 * @author Roman Elizarov
 */
class DeltaDecoder {
    private final ByteArrayInputStream in;
    private double lastValue;
    private int lastPrecision;
    private long lastTimeMillis;
    private int bits;
    private int bitCount;

    public DeltaDecoder(double firstValue, long firstTimeMillis, byte[] bytes) {
        in = new ByteArrayInputStream(bytes);
        lastValue = firstValue;
        lastPrecision = DeltaUtil.getPrecision(firstValue);
        lastTimeMillis = DeltaUtil.roundTime(firstTimeMillis);
    }

    public double readValue() {
        int flag = readBit();
        if (flag == 0)
            return lastValue;
        flag = readBit();
        if (flag == 1)
            lastPrecision += readNonZero();
        lastValue += (double)readNonZero() / DeltaUtil.POWER[lastPrecision];
        // round to max precision
        lastValue = Math.floor(lastValue * DeltaUtil.MAX_VALUE_POWER + 0.5) / DeltaUtil.MAX_VALUE_POWER;
        return lastValue;
    }

    public long readTime() {
        long flag = readBit();
        if (flag == 0)
            return lastTimeMillis;
        lastTimeMillis += DeltaUtil.TIME_PRECISION * readNonZero();
        return lastTimeMillis;
    }

    long readNonZero() {
        int sign = readBit();
        long value = readPositive();
        return sign == 0 ? value : -value;
    }

    long readPositive() {
        long result = 1L << 63;
        int shift = 63;
        while (readBit() == 1) {
            shift--;
            result |= ((long)readBit()) << shift;
        }
        return result >>> shift;
    }

    int readBit() {
        if (bitCount == 0) {
            bits = in.read();
            if (bits == -1)
                throw new IllegalStateException();
            bitCount = 8;
        }
        bitCount--;
        return (bits >> bitCount) & 1;
    }

}
