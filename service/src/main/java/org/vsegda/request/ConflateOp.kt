package org.vsegda.request

import org.apache.commons.beanutils.*

enum class ConflateOp(private val str: String) {
    MAX("Max"),
    MIN("Min"),
    LAST("Last");

    override fun toString(): String = str

    class Cnv : Converter {
        @Suppress("UNCHECKED_CAST")
        override fun <T : Any?> convert(type: Class<T>?, value: Any?): T {
            check(type === ConflateOp::class.java)
            return convert(value) as T
        }

        private fun convert(o: Any?): ConflateOp? {
            return ConflateOp.valueOf(o?.toString()?.uppercase() ?: return null)
        }
    }
}
