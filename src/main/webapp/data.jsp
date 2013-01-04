<%@taglib uri="/WEB-INF/tlds/c.tld" prefix="c" %>
<%@taglib uri="/WEB-INF/tlds/v.tld" prefix="v" %>
<!DOCTYPE html>
<html>
<head>
  <title>vsegda.org | Data items</title>
  <link rel="stylesheet" href="style.css" type="text/css">
</head>
<body>
<p>
  Data Items
  <c:if test="${param.id != null}">
    <a href="dataGraph.jsp?${pageContext.request.queryString}">Graph</a>
  </c:if>
</p>
<table>
  <tr>
    <th>id</th>
    <th>value</th>
    <th>time</th>
    <th>ago</th>
  </tr>
  <v:dataItems>
    <tr class="${item.formatClass}">
      <c:choose>
        <c:when test="${param.id == null}">
          <td><a href="?id=${item.streamCode}"><c:out value="${item.streamCode}"/></a></td>
        </c:when>
        <c:otherwise>
          <td><c:out value="${item.streamCode}"/></td>
        </c:otherwise>
      </c:choose>
      <td><c:out value="${item.value}"/></td>
      <td><c:out value="${item.time}"/></td>
      <td><c:out value="${item.ago}"/></td>
    </tr>
  </v:dataItems>
</table>
</body>
</html>
