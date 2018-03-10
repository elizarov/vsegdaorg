package org.vsegda.app

import io.ktor.application.*
import io.ktor.html.*
import io.ktor.pipeline.*
import kotlinx.html.*
import org.vsegda.request.*

suspend fun PipelineContext<Unit, ApplicationCall>.renderDataPlot() {
    val req = DataRequest(call.request)
    val streams = req.queryMap()
    call.respondHtml {
        page("Data", header = HEAD::dataPlotHeader, topNav = dataNav(req, DataPage.PLOT)) {
            div(classes = "flt") {
                id = "plot"
            }
            div(classes = "flt") {
                id = "legend"
                span { id = "time" }
            }
            div(classes = "clear") {
                id = "overview"
            }
            script {
                +"data = ["
                for ((stream, items) in streams) {
                    +"{label:'${stream.nameOrCode}', "
                    +"data: ["
                    for (item in items) {
                        +"[${item.timeMillis},${item.value}],"
                    }
                    +"]},"
                }
                +"];"
            }
            dataNavigation(req)
        }
    }
}

private fun HEAD.dataPlotHeader() {
    script(src = "/js/jquery.js") {}
    script(src = "/js/jquery.flot.js") {}
    script(src = "/js/jquery.flot.time.js") {}
    script(src = "/js/jquery.flot.selection.js") {}
    script(src = "/js/jquery.flot.crosshair.js") {}
    script(src = "/js/jquery.flot.crosshair.js") {}
    script(src = "/js/dataPlot.js") {}
}