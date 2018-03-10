package org.vsegda.app

import io.ktor.application.*
import io.ktor.html.*
import io.ktor.pipeline.*
import kotlinx.html.*

suspend fun PipelineContext<Unit, ApplicationCall>.renderAdmin() {
    call.respondHtml {
        page("Admin", header = {
            script(src = "/admin/admin.nocache.js") {}
        }) {
            div(classes = "par") { id = "actions" }
            div(classes = "par") { id = "table" }
            div(classes = "par") { id = "status" }
            // OPTIONAL: include this if you want history support
            iframe {
                id = "__gwt_historyFrame"
                src = "javascript:''"
                tabIndex = "-1"
                style = "position:absolute;width:0;height:0;border:0"
            }
        }
    }
}
