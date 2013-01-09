package org.vsegda.data;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

/**
 * @author Roman Elizarov
 */
class DeltaEncoder {
    private static final int INITIAL_BIT_INDEX = 8;

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private double lastValue;
    private int lastPrecision;
    private long lastTimeMillis;
    private int bits;
    private int bitIndex = INITIAL_BIT_INDEX;

    public DeltaEncoder(double firstValue, long firstTimeMillis) {
        checkValue(firstValue);
        lastValue = firstValue;
        lastPrecision = DeltaUtil.getPrecision(firstValue);
        lastTimeMillis = DeltaUtil.roundTime(firstTimeMillis);
    }

    public int size() {
        return out.size() + (bitIndex < INITIAL_BIT_INDEX ? 1 : 0);
    }

    public double getLastValue() {
        return lastValue;
    }

    public long getLastTimeMillis() {
        return lastTimeMillis;
    }

    private static void checkValue(double x) {
        if (Double.isNaN(x) || Double.isInfinite(x))
            throw new IllegalArgumentException("value: " + x);
    }

    public void writeValue(double value) {
        checkValue(value);
        double delta = value - lastValue;
        lastValue = value;
        if (delta == 0)
            writeBit(0);
        else {
            writeBit(1);
            int precision = DeltaUtil.getPrecision(delta);
            if (precision != lastPrecision) {
                writeBit(1);
                writeNonZero(precision - lastPrecision);
                lastPrecision = precision;
            } else
                writeBit(0);
            writeNonZero(Math.round(delta * DeltaUtil.POWER[precision]));
        }
    }

    public void writeTime(long timeMillis) {
        long rounded = DeltaUtil.roundTime(timeMillis);
        long delta = (rounded - lastTimeMillis) / DeltaUtil.TIME_PRECISION;
        if (delta < 0)
            throw new IllegalArgumentException("Should be ordered by time");
        lastTimeMillis = rounded;
        if (delta == 0)
            writeBit(0);
        else {
            writeBit(1);
            writeNonZero(delta);
        }
    }

    public byte[] toByteArray(int size) {
        if (bitIndex < INITIAL_BIT_INDEX)
            flushBits();
        byte[] bytes = out.toByteArray();
        return bytes.length == size ? bytes : Arrays.copyOf(bytes, size);
    }

    void writeNonZero(long x) {
        if (x < 0) {
            writeBit(1);
            x = -x;
        } else
            writeBit(0);
        writePositive(x);
    }

    void writePositive(long x) {
        assert x > 0;
        Integer count = 63 - Long.numberOfLeadingZeros(x);
        for (int shift = count; --shift >= 0;) {
            writeBit(1);
            writeBit(((int)(x >> shift)) & 1);
        }
        writeBit(0);
    }

    void writeBit(int bit) {
        bitIndex--;
        bits |= bit << bitIndex;
        if (bitIndex == 0)
            flushBits();
    }

    private void flushBits() {
        out.write(bits);
        bits = 0;
        bitIndex = INITIAL_BIT_INDEX;
    }
}
