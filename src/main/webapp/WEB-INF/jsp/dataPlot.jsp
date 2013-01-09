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
</head>
<body>
<div class="par">Data Items | Plot</div>
<div id="plot" class="flt" style="width:600px;height:300px;"></div>
<div id="legend" class="flt"></div>

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
$(document).ready(function () {
    $.plot($("#plot"), data, {
      xaxis: { mode: "time", timezone: "browser" },
      legend: { container: "#legend" }
    });
});
</script>
</body>
</html>
