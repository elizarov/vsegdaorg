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
    <c:if test="${param.id == null}">
      <th>name</th>
      <th>mode</th>
    </c:if>
  </tr>
  <v:dataItems>
    <tr class="${item.formatClass}">
      <c:choose>
        <c:when test="${param.id == null}">
          <td><a href="?id=${item.stream.code}"><c:out value="${item.stream.code}"/></a></td>
        </c:when>
        <c:otherwise>
          <td><c:out value="${item.stream.code}"/></td>
        </c:otherwise>
      </c:choose>
      <td><c:out value="${item.value}"/></td>
      <td><c:out value="${item.time}"/></td>
      <td><c:out value="${item.ago}"/></td>
      <c:if test="${param.id == null}">
        <td><c:out value="${item.stream.name}"/></td>
        <td><c:out value="${item.stream.mode}"/></td>
      </c:if>
    </tr>
  </v:dataItems>
</table>
</body>
</html>
