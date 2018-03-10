var data = []; // will be set by the JSP

$(function () {
    function decompressArray(a) {
        var op = a[0];
        var m = a[1];
        var cur = 0;
        var b = [];
        for (var i = 2; i < a.length; ++i) {
            cur += a[i];
            switch (op) {
                case '*': b.push(cur * m); break;
                case '/': b.push(cur / m); break;
            }
        }
        return b;
    }

    function decompressSeries(series) {
        var times = decompressArray(series.times);
        var values = decompressArray(series.values);
        var data = [];
        for (var i = 0; i < times.length; ++i) {
            data.push([times[i], values[i]]);
        }
        series.data = data;
    }

    for (var i = 0; i < data.length; ++i) {
        decompressSeries(data[i]);
    }

    var marginLeft = 20;
    var marginRight = 40;

    var plotOptions = {
        xaxis: { mode: "time", timezone: "browser" },
        yaxes: [
            { labelWidth: marginRight, reserveSpace: true, position: "right" },
            { labelWidth: marginLeft, reserveSpace: true }
        ],
        legend: { container: "#legend" },
        selection: { mode: "x" },
        crosshair: { mode: "x" },
        grid: { hoverable: true, autoHighlight: false }
    };

    var overviewOptions = {
        series: {
            lines: { show: true, lineWidth: 1 },
            shadowSize: 0
        },
        xaxis: { mode: "time", ticks: [] },
        yaxes: [
            { labelWidth: marginRight, reserveSpace: true, ticks: [], position: "right" },
            { labelWidth: marginLeft, reserveSpace: true, ticks: [] }
        ],
        legend: { show: false },
        selection: { mode: "x" },
        crosshair: { mode: "x" },
        grid: { hoverable: true, autoHighlight: false }
    };

    var plot = $.plot($("#plot"), data, plotOptions);
    var overview = null; // show overview on first zoom

    var updateLegendTimeout = null;
    var lastPos = null;

    function fmtTime(time) {
        var d = new Date(time);
        return d.getFullYear() + "-" + ("0" + (d.getMonth() + 1)).slice(-2) + "-" + ("0" + d.getDate()).slice(-2) + " " +
                d.getHours() + ":" + ("0" + d.getMinutes()).slice(-2) + ":" + ("0" + d.getSeconds()).slice(-2);

    }

    function updateLegend() {
        updateLegendTimeout = null;
        var axes = plot.getAxes();
        var legend = $("#legend .legendLabel");
        var time = axes.xaxis.max;
        for (var i = 0; i < data.length; ++i) {
            var series = data[i];
            var lastData = series.data[series.data.length - 1];
            var y = lastData[1];
            if (lastPos !== null) {
                if (lastPos.x < axes.xaxis.min) {
                    time = axes.xaxis.min;
                    y = series.data[0][1];
                } else if (lastPos.x < axes.xaxis.max) {
                    time = lastPos.x;
                    for (var j = 0; j < series.data.length; ++j)
                        if (series.data[j][0] > lastPos.x) {
                            y = series.data[j][1];
                            break;
                        }
                }
            }
            var node = legend.eq(i);
            node.html("<span class='legendValue'></span><span class='legendText'></span>");
            node.children(".legendValue").text(y);
            node.children(".legendText").text(series.label);
        }
        $("#time").text("time: " + fmtTime(time))
    }

    function scheduleUpdateLegend(pos) {
        lastPos = pos;
        if (!updateLegendTimeout)
            updateLegendTimeout = setTimeout(updateLegend, 50);
    }

    function rebuildPlot(ranges) {
        plot = $.plot($("#plot"), data,
                $.extend(true, {}, plotOptions, {
                    xaxis: { min: ranges.xaxis.from, max: ranges.xaxis.to }
                }));
        if (overview === null)
            overview = $.plot($("#overview"), [data[0]], overviewOptions);
        overview.setSelection(ranges, true);
        scheduleUpdateLegend(null);
    }

    updateLegend();

    $("#plot").bind("plotselected", function (event, ranges) {
        rebuildPlot(ranges);
    });

    $("#overview").bind("plotselected", function (event, ranges) {
        plot.setSelection(ranges);
    });

    $("#plot").bind("plothover", function (event, pos, item) {
        scheduleUpdateLegend(pos);
    });
});
