package org.vsegda.app

import io.ktor.application.*
import io.ktor.html.*
import io.ktor.pipeline.*
import kotlinx.html.*
import org.vsegda.data.*
import org.vsegda.request.*
import kotlin.math.*

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
                    +"{label:'${stream.nameOrCode}'"
                    +",times:"
                    compressTimes(items)
                    +",values:"
                    compressValues(items)
                    +"},"
                }
                +"];"
            }
            dataSelector(req)
        }
    }
}

fun SCRIPT.compressTimes(items: List<DataItem>) {
    +"['*',$TIME_PRECISION"
    var cur = 0L
    for (item in items) {
        val delta = (item.timeMillis - cur) / TIME_PRECISION
        +",$delta"
        cur += delta * TIME_PRECISION
    }
    +"]"
}

fun SCRIPT.compressValues(items: List<DataItem>) {
    var precision = 0
    for (item in items) {
        precision = max(precision, computePrecision(item.value))
    }
    val pow = POWER[precision]
    +"['/',$pow"
    var cur = 0.0
    for (item in items) {
        val delta = ((item.value - cur) * pow).roundToLong()
        +",$delta"
        cur += delta.toDouble() / pow
    }
    +"]"
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