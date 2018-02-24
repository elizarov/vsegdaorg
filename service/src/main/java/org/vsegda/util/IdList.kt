package org.vsegda.util

import org.apache.commons.beanutils.Converter

import java.util.ArrayList
import java.util.StringTokenizer

class IdList(s: String) : ArrayList<String>() {
    val isSingleton: Boolean
        get() = size == 1

    init {
        val st = StringTokenizer(s, ",")
        while (st.hasMoreTokens()) {
            val token = st.nextToken()
            val i = token.indexOf('-', 1)
            val a: Long
            val b: Long
            if (i > 0) {
                a = java.lang.Long.parseLong(token.substring(0, i))
                b = java.lang.Long.parseLong(token.substring(i + 1))
                for (id in a..b)
                    add(id.toString())
            } else {
                add(token)
            }
        }
    }

    override fun toString(): String {
        val sb = StringBuilder()
        var a: Long = 1
        var b: Long = 0
        for (code in this) {
            val id: Long
            try {
                id = java.lang.Long.parseLong(code)
                if (a <= b && id == b + 1)
                    b = id
                else {
                    flushToString(sb, a, b)
                    b = id
                    a = b
                }
            } catch (e: NumberFormatException) {
                // ignore -- just add this code
                flushToString(sb, a, b)
                flushToString(sb, code)
                a = 1
                b = 0
            }

        }
        flushToString(sb, a, b)
        return sb.toString()
    }

    private fun flushToString(sb: StringBuilder, code: String) {
        if (sb.isNotEmpty())
            sb.append(',')
        sb.append(code)
    }

    private fun flushToString(sb: StringBuilder, a: Long, b: Long) {
        if (a < b)
            flushToString(sb, a.toString() + "-" + b)
        else if (a == b)
            flushToString(sb, a.toString())
    }

    class Cnv : Converter {
        @Suppress("UNCHECKED_CAST")
        override fun <T> convert(type: Class<T>?, value: Any?): T {
            check(type === IdList::class.java)
            return convert(value) as T
        }

        private fun convert(o: Any?): IdList? {
            return IdList(o?.toString() ?: return null)
        }
    }
}
