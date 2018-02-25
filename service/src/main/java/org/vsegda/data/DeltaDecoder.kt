package org.vsegda.data

import java.io.*

internal class DeltaDecoder(private var lastValue: Double, firstTimeMillis: Long, bytes: ByteArray) {
    private val input = ByteArrayInputStream(bytes)
    private var lastPrecision: Int = computePrecision(lastValue)
    private var lastTimeMillis: Long = roundTime(firstTimeMillis)
    private var bits: Int = 0
    private var bitCount: Int = 0

    fun readValue(): Double {
        var flag = readBit()
        if (flag == 0) return lastValue
        flag = readBit()
        if (flag == 1) lastPrecision += readNonZero().toInt()
        lastValue += readNonZero().toDouble() / POWER[lastPrecision]
        // round to max precision
        lastValue = Math.floor(lastValue * MAX_VALUE_POWER + 0.5) / MAX_VALUE_POWER
        return lastValue
    }

    fun readTime(): Long {
        val flag = readBit().toLong()
        if (flag == 0L) return lastTimeMillis
        lastTimeMillis += TIME_PRECISION * readNonZero()
        return lastTimeMillis
    }

    fun readNonZero(): Long {
        val sign = readBit()
        val value = readPositive()
        return if (sign == 0) value else -value
    }

    fun readPositive(): Long {
        var result = 1L shl 63
        var shift = 63
        while (readBit() == 1) {
            shift--
            result = result or (readBit().toLong() shl shift)
        }
        return result ushr shift
    }

    fun readBit(): Int {
        if (bitCount == 0) {
            bits = input.read()
            if (bits == -1) throw EOFException()
            bitCount = 8
        }
        bitCount--
        return bits shr bitCount and 1
    }
}
