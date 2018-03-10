package org.vsegda.app

import io.ktor.application.*
import io.ktor.html.*
import io.ktor.pipeline.*
import kotlinx.html.*
import org.vsegda.request.*

suspend fun PipelineContext<Unit, ApplicationCall>.renderMessage() {
    val req = MessageRequest(call.request)
    val items = req.query()
    val isOverview = req.id == null
    call.respondHtml {
        page("Message", topNav = {
            navigate("Overview", if (isOverview) "" else "/message${req.queryString.update("id", null)}")
        }) {
            table(classes = "par data") {
                thead {
                    tr {
                        th { +"id" }
                        th { +"text" }
                        th { +"time" }
                        th { +"ago" }
                        th { +"index" }
                    }
                }
                tbody {
                    for (item in items) {
                        tr(classes = item.formatClass) {
                            td {
                                if (isOverview) {
                                    a(href="/message${req.queryString.update("id", item.queueId)}") {
                                        +item.queueId.toString()
                                    }
                                } else {
                                    +item.queueId.toString()
                                }
                            }
                            td { +item.text }
                            td { +item.time }
                            td { +item.ago }
                            td { +item.messageIndex.toString() }
                        }
                    }
                }
            }
        }
    }
}