package org.vsegda.request

data class QueryString(private val str: String = "") {
    fun isEmpty(): Boolean = str.isEmpty()
    override fun toString(): String = if (str.isEmpty()) "" else "?$str"

    fun update(key: String, value: Any?): QueryString {
        val keyValues = str.split('&').filter { !it.isEmpty() }.toMutableList()
        val eq = key + "="
        var found = false
        var updated = keyValues.mapNotNull { keyValue ->
            if (keyValue.startsWith(eq)) {
                if (value == null || found) {
                    null
                } else {
                    found = true
                    eq + value
                }
            } else
                keyValue
        }
        if (value != null && !found) updated += eq + value
        return QueryString(updated.joinToString("&"))
    }
}