<%@taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>
<%@taglib uri="/WEB-INF/tld/v.tld" prefix="v" %>
<!DOCTYPE html>
<html>
<head>
  <title>Data plot @ vsegda.org</title>
  <link rel="stylesheet" href="/style.css" type="text/css">
  <!--[if lte IE 8]>
  <script language="javascript" type="text/javascript" src="/js/excanvas.min.js"></script><![endif]-->
  <script language="javascript" type="text/javascript" src="/js/jquery.js"></script>
  <script language="javascript" type="text/javascript" src="/js/jquery.flot.js"></script>
  <script language="javascript" type="text/javascript" src="/js/jquery.flot.time.js"></script>
  <script language="javascript" type="text/javascript" src="/js/jquery.flot.selection.js"></script>
  <script language="javascript" type="text/javascript" src="/js/jquery.flot.crosshair.js"></script>
  <script language="javascript" type="text/javascript" src="/js/dataPlot.js"></script>
</head>
<body>
<div class="par">
  <span class="hdr">Data Items<span>
  | <span class="hdr">Plot</span>
  [<a href="data?${pageContext.request.queryString}">Table</a>]
</div>
<div id="plot" class="flt" style="width:700px;height:300px;"></div>
<div class="flt">
  <div id="legend"></div>
  <span id="time" style="font-size:smaller"></span>
</div>
<div id="overview" class="clear" style="width:700px;height:60px;"></div>

<script>
  data = [
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
</script>
</body>
</html>
