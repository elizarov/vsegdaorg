package org.vsegda.data;

import junit.framework.TestCase;

import java.io.EOFException;

/**
 * @author Roman Elizarov
 */
public class DeltaCodecTest extends TestCase {
    public void testBits() throws EOFException {
        int[] bits = { 0, 0, 1, 1, 1, 0, 1, 0, 0, 1, 0, 1, 1, 0, 1 };
        DeltaEncoder encoder = new DeltaEncoder(0, 0);
        for (int bit : bits) {
            encoder.writeBit(bit);
        }
        DeltaDecoder decoder = new DeltaDecoder(0, 0, encoder.toByteArray(encoder.size()));
        for (int bit : bits) {
            assertEquals(bit, decoder.readBit());
        }
    }

    public void testPositive() throws EOFException {
        int[] xs = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18 };
        DeltaEncoder encoder = new DeltaEncoder(0, 0);
        for (int x : xs) {
            encoder.writePositive(x);
        }
        DeltaDecoder decoder = new DeltaDecoder(0, 0, encoder.toByteArray(encoder.size()));
        for (int x : xs) {
            assertEquals(x, decoder.readPositive());
        }
    }

    public void testNonZero() throws EOFException {
        int[] xs = { 1, 2, 3, 4, 5, 6, 7, 8, 9, -1, -2, -3, -4, -5, -6, -7, -8, -9 };
        DeltaEncoder encoder = new DeltaEncoder(0, 0);
        for (int x : xs) {
            encoder.writeNonZero(x);
        }
        DeltaDecoder decoder = new DeltaDecoder(0, 0, encoder.toByteArray(encoder.size()));
        for (int x : xs) {
            assertEquals(x, decoder.readNonZero());
        }
    }

    public void testValues() throws EOFException {
        double[] xs = { 1, 2, 3, 4, 5, 5, 5, 5.1, 5.2, 5.3, 5.3, 5.2, 5.1, 5.01, 5.001, 5.0001 };
        DeltaEncoder encoder = new DeltaEncoder(0, 0);
        for (double x : xs) {
            encoder.writeValue(x);
        }
        DeltaDecoder decoder = new DeltaDecoder(0, 0, encoder.toByteArray(encoder.size()));
        for (double x : xs) {
            assertEquals(x, decoder.readValue());
        }
    }
}
