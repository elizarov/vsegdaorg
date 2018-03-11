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

enum class NavStyle(val cls: String? = null) {
    CUR("cur"),
    LINK("link"),
    DISABLED;
}

fun HtmlBlockTag.navigate(title: String, link: String, style: NavStyle = NavStyle.LINK) {
   a(href = if (style == NavStyle.LINK) link else "#") {
       style.cls?.let { classes += it }
       +title
   }
}

fun HtmlBlockTag.navigate(title: String, link: String, cur: Boolean) {
    navigate(title, link, if (cur) NavStyle.CUR else NavStyle.LINK)
}

fun HtmlBlockTag.navigate(title: String, link: String?) {
    navigate(title, link ?: "", if (link == null) NavStyle.DISABLED else NavStyle.LINK)
}