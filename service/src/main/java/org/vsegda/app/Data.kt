package org.vsegda.app

import io.ktor.application.*
import io.ktor.html.*
import io.ktor.util.pipeline.*
import kotlinx.html.*
import org.vsegda.data.*
import org.vsegda.request.*
import org.vsegda.util.*

enum class DataPage(val title: String, val link: DataRequest.() -> String?) {
    OVERVIEW("Overview", { "/data${queryString.update("id", null)}" }),
    TABLE("Table", { "/data$queryString".takeIf { id != null } }),
    PLOT("Plot", { "/dataPlot$queryString".takeIf { id != null } })
}

fun dataNav(req: DataRequest, cur: DataPage): DIV.() -> Unit = {
    nav {
        for (page in DataPage.values()) {
            val link = page.link(req)
            if (link != null) navigate(page.title, link, page == cur)
        }
    }
    val resetQuery = req.queryString
        .update("to", null)
        .update("span", null)
        .update("op", null)
    if (resetQuery != req.queryString) {
        nav {
            navigate("Reset", resetQuery.toString())
        }
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
            div(classes = "par") {
                +"${items.size} items"
            }
            dataSelector(req)
        }
    }
}

fun BODY.dataSelector(req: DataRequest) {
    if (req.id == null) return
    val query = req.queryString
    val last = req.to
    val prevQuery = query.update("to", (last ?: TimeInstant.now()).minus(req.span))
    val nextQuery = last?.let {
        val nextLast = it.plus(req.span)
        query.update("to", if (nextLast.isNowOrFuture) null else nextLast)
    }
    nav {
        navigate("Prev", "$prevQuery")
        navigate("Next", nextQuery?.toString())
    }
    nav {
        for (ds in DataSpan.values()) {
            navigate(ds.text, "${query.update("span", ds.span)}", req.span == ds.span)
        }
    }
    if (req.conflate != null) {
        nav {
            for (op in ConflateOp.values()) {
                navigate(op.toString(), "${query.update("op", op)}", req.op == op)
            }
        }
    }
}