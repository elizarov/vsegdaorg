package org.vsegda.request

import io.ktor.request.*
import org.apache.commons.beanutils.*
import org.vsegda.util.*
import java.util.*
import javax.servlet.http.*

abstract class AbstractRequest : Logged {
    var queryString: QueryString = QueryString()
        private set

    override fun toString(): String =
        if (queryString.isEmpty()) "<all>" else queryString.params

    // must be invoked at most once
    protected fun init(req: HttpServletRequest) {
        populate(req.parameterMap.mapValues { it.value.toList() }.entries)
        queryString = QueryString(req.queryString ?: "")
    }

    protected fun init(req: ApplicationRequest) {
        populate(req.queryParameters.entries())
        queryString = QueryString(req.local.uri.substringAfter('?', ""))
    }

    private fun populate(params: Set<Map.Entry<String, List<String>>>) {
        val populate = LinkedHashMap<String, String>()
        for ((key, values) in params) {
            if (values.isNotEmpty()) populate[key] = values[0]
        }
        try {
            BeanUtils.populate(this, populate)
        } catch (e: Exception) {
            throw IllegalArgumentException(e)
        }
    }

    companion object {
        init {
            ConvertUtils.register(IdList.Cnv(), IdList::class.java)
            ConvertUtils.register(TimeInstant.Cnv(), TimeInstant::class.java)
            ConvertUtils.register(TimePeriod.Cnv(), TimePeriod::class.java)
            ConvertUtils.register(ConflateOp.Cnv(), ConflateOp::class.java)
        }
    }
}
