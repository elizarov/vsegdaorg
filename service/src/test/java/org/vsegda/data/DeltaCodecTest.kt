package org.vsegda.data

import junit.framework.Assert.*
import org.junit.*

class DeltaCodecTest {
    @Test
    fun testBits() {
        val bits = intArrayOf(0, 0, 1, 1, 1, 0, 1, 0, 0, 1, 0, 1, 1, 0, 1)
        val encoder = DeltaEncoder(0.0, 0)
        for (bit in bits) {
            encoder.writeBit(bit)
        }
        val decoder = DeltaDecoder(0.0, 0, encoder.toByteArray(encoder.size()))
        for (bit in bits) {
            assertEquals(bit, decoder.readBit())
        }
    }

    @Test
    fun testPositive() {
        val xs = intArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18)
        val encoder = DeltaEncoder(0.0, 0)
        for (x in xs) {
            encoder.writePositive(x.toLong())
        }
        val decoder = DeltaDecoder(0.0, 0, encoder.toByteArray(encoder.size()))
        for (x in xs) {
            assertEquals(x.toLong(), decoder.readPositive())
        }
    }

    @Test
    fun testNonZero() {
        val xs = intArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, -1, -2, -3, -4, -5, -6, -7, -8, -9)
        val encoder = DeltaEncoder(0.0, 0)
        for (x in xs) {
            encoder.writeNonZero(x.toLong())
        }
        val decoder = DeltaDecoder(0.0, 0, encoder.toByteArray(encoder.size()))
        for (x in xs) {
            assertEquals(x.toLong(), decoder.readNonZero())
        }
    }

    @Test
    fun testValues() {
        val xs = doubleArrayOf(1.0, 2.0, 3.0, 4.0, 5.0, 5.0, 5.0, 5.1, 5.2, 5.3, 5.3, 5.2, 5.1, 5.01, 5.001, 5.0001)
        val encoder = DeltaEncoder(0.0, 0)
        for (x in xs) {
            encoder.writeValue(x)
        }
        val decoder = DeltaDecoder(0.0, 0, encoder.toByteArray(encoder.size()))
        for (x in xs) {
            assertEquals(x, decoder.readValue())
        }
    }
}
