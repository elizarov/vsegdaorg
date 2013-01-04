<%
    String id = request.getParameter("id");
    if (id == null)
        throw new ServletException("id parameter required");
%>
<!DOCTYPE html>
<html>
<head>
<title>vsegda.org | Graph <%= id %></title>
<script language="JavaScript" src="http://www.google.com/jsapi"></script>
<script language="JavaScript" src="http://apps.pachube.com/google_viz/viz.js"></script>
</head>
<body>
<script language="JavaScript">createViz(13164,<%= id %>,800,400,"FF0066");</script>
</body>
</html>
