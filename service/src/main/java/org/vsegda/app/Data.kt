package org.vsegda.app

import io.ktor.application.*
import io.ktor.html.*
import io.ktor.pipeline.*
import kotlinx.html.*
import org.vsegda.data.*
import org.vsegda.request.*
import org.vsegda.util.*
import org.vsegda.util.TimePeriod.Companion.valueOf

enum class DataPage(val title: String, val link: DataRequest.() -> String?) {
    OVERVIEW("Overview", { "/data${queryString.update("id", null)}" }),
    TABLE("Table", { "/data$queryString".takeIf { id != null } }),
    PLOT("Plot", { "/dataPlot$queryString".takeIf { id != null } })
}

fun dataNav(req: DataRequest, cur: DataPage): DIV.() -> Unit = {
    for (page in DataPage.values()) {
        navigate(page.title, if (page == cur) "" else page.link(req))
    }
}

suspend fun PipelineContext<Unit, ApplicationCall>.renderData() {
    val req = DataRequest(call.request)
    val items = req.queryList()
    val isOverview = req.id == null
    val curPage = if (isOverview) DataPage.OVERVIEW else DataPage.TABLE
    call.respondHtml {
        page("Data", topNav = dataNav(req, curPage)) {
            table(classes = "par data") {
                thead {
                    tr {
                        th { +"tag${TAG_ID_SEPARATOR}id" }
                        th { +"value" }
                        if (isOverview) {
                            th { +"name" }
                        }
                        th { +"time" }
                        th { +"ago" }
                    }
                }
                tbody {
                    for (item in items) {
                        tr(classes = item.formatClass) {
                            td {
                                if (isOverview) {
                                    a(href="/dataPlot${req.queryString.update("id", item.streamCode)}") {
                                        +item.streamCode
                                    }
                                } else {
                                    +item.streamCode
                                }
                            }
                            td { +item.value.toString() }
                            if (isOverview) {
                                td { +item.stream.name }
                            }
                            td { +item.time }
                            td { +item.ago }
                        }
                    }
                }
            }
            dataNavigation(req)
        }
    }
}

enum class DataSpan(val text: String, val span: TimePeriod) {
    DAY("1 Day", valueOf(1, TimePeriodUnit.DAY)),
    DAY3("3 Days", valueOf(3, TimePeriodUnit.DAY)),
    WEEK("1 Week", valueOf(1, TimePeriodUnit.WEEK)),
    WEEK2("2 Weeks", valueOf(2, TimePeriodUnit.WEEK)),
    MONTH("4 Weeks", valueOf(4, TimePeriodUnit.WEEK))
}

fun BODY.dataNavigation(req: DataRequest) {
    if (req.id == null) return
    val query = req.queryString
    val last = req.to
    val prevQuery = query.update("to", (last ?: TimeInstant.now()).minus(req.span))
    val nextQuery = last?.let {
        val nextLast = it.plus(req.span)
        query.update("to", if (nextLast.isNowOrFuture) null else nextLast)
    }
    navigate("Prev", "$prevQuery")
    navigate("Next", nextQuery?.toString() ?: "")
    span(classes = "spacer")
    for (ds in DataSpan.values()) {
        navigate(ds.text, if (req.span == ds.span) "" else "${query.update("span", ds.span)}")
    }
}