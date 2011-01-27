<%
  String query = request.getQueryString();
  String source = "dataSource" + (query == null ? "" : "?" + query);
%>
<!DOCTYPE html>
<html>
<head>
  <title>vsegda.org | Data graph</title>
  <script type='text/javascript' src='https://www.google.com/jsapi'></script>
  <script type='text/javascript'>
    google.load('visualization', '1', {'packages':['annotatedtimeline']});
    google.setOnLoadCallback(init);

    function init() {
      var query = new google.visualization.Query('<%= source %>');
      query.send(drawChart);
    }

    function drawChart(response) {
      if (response.isError()) {
        alert('Error in query: ' + response.getMessage() + ' ' + response.getDetailedMessage());
        return;
      }
      var data = response.getDataTable();
      var chart = new google.visualization.AnnotatedTimeLine(document.getElementById('chart_div'));
      char.displayZoomButtons = false;
      chart.draw(data, {displayAnnotations: true});
    }
  </script>
</head>

<body>
<p>
  Data graph
</p>
<div id='chart_div' style='width: 900px; height: 300px;'></div>
</body>
</html>