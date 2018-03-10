package org.vsegda.app

import io.ktor.application.*
import io.ktor.routing.*
import kotlinx.html.*

fun Application.main() {
    routing {
        get("/data") {
            renderData()
        }
        get("/dataPlot") {
            renderDataPlot()               
        }
        get("/message") {
            renderMessage()
        }
        get("/admin") {
            renderAdmin()
        }
    }
}

fun HTML.page(title: String, header: HEAD.() -> Unit = {}, topNav: DIV.() -> Unit = {}, block: BODY.() -> Unit) {
    head {
        title("$title @ vsegda.org")
        styleLink("/style.css")
        header()
    }
    body {
       div(classes="par") {
           span(classes = "hdr") { +title }
           topNav()
       }
       block()
    }
}

fun HtmlBlockTag.navigate(title: String, link: String?) {
   if (link == "") {
       span(classes = "nav cur") { +title }
   } else if (link != null) {
       a(href = link, classes = "nav link") { +title }
   }
}