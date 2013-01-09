<%@taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>
<%@taglib uri="/WEB-INF/tld/v.tld" prefix="v" %>
<!DOCTYPE html>
<html>
<head>
  <title>vsegda.org | Data items | Plot</title>
  <link rel="stylesheet" href="/style.css" type="text/css">
  <!--[if lte IE 8]><script language="javascript" type="text/javascript" src="/js/excanvas.min.js"></script><![endif]-->
  <script language="javascript" type="text/javascript" src="/js/jquery.js"></script>
  <script language="javascript" type="text/javascript" src="/js/jquery.flot.js"></script>
  <script language="javascript" type="text/javascript" src="/js/jquery.flot.time.js"></script>
  <script language="javascript" type="text/javascript" src="/js/jquery.flot.selection.js"></script>
</head>
<body>
<div class="par">Data Items | Plot</div>
<div id="plot" class="flt" style="width:700px;height:300px;"></div>
<div id="legend" class="flt"></div>
<div id="overview" class="clear" style="width:700px;height:60px;"></div>

<script>
var data = [
  <v:dataStreams>
  {
    label: "<c:out value="${stream.nameOrCode}"/>",
    data: [
        <v:dataItems>
        [ <c:out value="${item.timeMillis}"/>, <c:out value="${item.value}"/> ],
        </v:dataItems>
    ]
  },
  </v:dataStreams>
];
$(function () {
    var marginLeft = 50;
    var marginRight = 30;

    var options = {
      xaxis: { mode: "time", timezone: "browser" },
      yaxes: [ { labelWidth: marginLeft, reserveSpace: true },
               { labelWidth: marginRight, reserveSpace: true, position: "right" } ],
      legend: { container: "#legend" },
      selection: { mode: "x" }
    };

    var plot = $.plot($("#plot"), data, options);

    var overview = $.plot($("#overview"), [data[0]], {
      series: {
          lines: { show: true, lineWidth: 1 },
          shadowSize: 0
      },
      xaxis: { mode: "time", ticks: [] },
      yaxes: [ { labelWidth: marginLeft, reserveSpace: true, ticks: [] },
               { labelWidth: marginRight, reserveSpace: true, ticks: [], position: "right" } ],
      legend: { show: false },
      selection: { mode: "x" }
    });

    $("#plot").bind("plotselected", function (event, ranges) {
        plot = $.plot($("#plot"), data,
                      $.extend(true, {}, options, {
                          xaxis: { min: ranges.xaxis.from, max: ranges.xaxis.to }
                      }));
        overview.setSelection(ranges, true);
    });

    $("#overview").bind("plotselected", function (event, ranges) {
        plot.setSelection(ranges);
    });
});
</script>
</body>
</html>
