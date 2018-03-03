package org.vsegda.request

import org.apache.commons.beanutils.*
import org.vsegda.util.*
import java.util.*
import javax.servlet.http.*

abstract class AbstractRequest : Logged {
    private lateinit var keyValues: MutableList<String>

    val queryString: String
        get() = keyValues.joinToString(separator = "&")

    override fun toString(): String = if (keyValues.isEmpty()) "<all>" else keyValues.toString()

    // must be invoked at most once
    protected fun init(req: HttpServletRequest? = null) {
        check(!this::keyValues.isInitialized)
        if (req == null) {
            keyValues = mutableListOf()
            return
        }
        // populate first
        try {
            val params = req.parameterMap
            val populate = LinkedHashMap<String, String>()
            for ((key, value) in params) {
                if (value.isNotEmpty()) populate[key] = value[0]
            }
            BeanUtils.populate(this, populate)
        } catch (e: Exception) {
            throw IllegalArgumentException(e)
        }
        // then keep keyValues as simple strings in order
        keyValues = (req.queryString?.split("&") ?: emptyList()).toMutableList()
    }

    protected fun updateQueryString(key: String, value: String?) {
        if (!this::keyValues.isInitialized) return // ignore updates from inside constructor
        val eq = key + "="
        for (i in keyValues.indices) {
            val keyValue = keyValues[i]
            if (keyValue.startsWith(eq)) {
                if (value == null)
                    keyValues.removeAt(i)
                else
                    keyValues[i] = eq + value
                return
            }
        }
        keyValues.add(eq + value!!)
    }

    companion object {
        init {
            ConvertUtils.register(IdList.Cnv(), IdList::class.java)
            ConvertUtils.register(TimeInstant.Cnv(), TimeInstant::class.java)
            ConvertUtils.register(TimePeriod.Cnv(), TimePeriod::class.java)
        }
    }
}
