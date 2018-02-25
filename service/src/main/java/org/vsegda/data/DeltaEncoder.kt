package org.vsegda.data

import java.io.*
import java.util.*
import kotlin.math.*

internal class DeltaEncoder(firstValue: Double, firstTimeMillis: Long) {
    private val out = ByteArrayOutputStream()
    var lastValue: Double = firstValue
        private set
    private var lastPrecision: Int = computePrecision(firstValue)
    var lastTimeMillis: Long = roundTime(firstTimeMillis)
        private set
    private var bits: Int = 0
    private var bitIndex = 8

    init {
        require(firstValue.isFinite())
    }

    fun size(): Int = out.size() + if (bitIndex < 8) 1 else 0

    fun writeValue(value: Double) {
        require(value.isFinite())
        val delta = value - lastValue
        lastValue = value
        if (delta == 0.0)
            writeBit(0)
        else {
            writeBit(1)
            val precision = computePrecision(delta)
            if (precision != lastPrecision) {
                writeBit(1)
                writeNonZero((precision - lastPrecision).toLong())
                lastPrecision = precision
            } else
                writeBit(0)
            writeNonZero((delta * POWER[precision]).roundToLong())
        }
    }

    fun writeTime(timeMillis: Long) {
        val rounded = roundTime(timeMillis)
        val delta = (rounded - lastTimeMillis) / TIME_PRECISION
        require(delta >= 0) { "Should be ordered by time" }
        lastTimeMillis = rounded
        if (delta == 0L)
            writeBit(0)
        else {
            writeBit(1)
            writeNonZero(delta)
        }
    }

    fun toByteArray(size: Int): ByteArray {
        if (bitIndex < 8) flushBits()
        val bytes = out.toByteArray()
        return if (bytes.size == size) bytes else bytes.copyOf(size)
    }

    fun writeNonZero(value: Long) {
        var x = value
        if (x < 0) {
            writeBit(1)
            x = -x
        } else
            writeBit(0)
        writePositive(x)
    }

    fun writePositive(x: Long) {
        assert(x > 0)
        val count = 63 - java.lang.Long.numberOfLeadingZeros(x)
        var shift = count
        while (--shift >= 0) {
            writeBit(1)
            writeBit((x shr shift).toInt() and 1)
        }
        writeBit(0)
    }

    fun writeBit(bit: Int) {
        bitIndex--
        bits = bits or (bit shl bitIndex)
        if (bitIndex == 0) flushBits()
    }

    private fun flushBits() {
        out.write(bits)
        bits = 0
        bitIndex = 8
    }
}
