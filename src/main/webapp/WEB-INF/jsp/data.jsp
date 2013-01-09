<%@ page import="org.vsegda.data.DataStream" %>
<%@taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>
<%@taglib uri="/WEB-INF/tld/v.tld" prefix="v" %>
<!DOCTYPE html>
<html>
<head>
  <title>vsegda.org | Data items</title>
  <link rel="stylesheet" href="/style.css" type="text/css">
</head>
<body>
<div class="par">
  Data Items
  <c:if test="${param.id != null}">
    <a href="dataGraph?${pageContext.request.queryString}">Graph</a>
  </c:if>
</div>
<table class="par data">
  <tr>
    <th>tag<%= DataStream.TAG_ID_SEPARATOR %>id</th>
    <th>value</th>
    <c:if test="${param.id == null}">
      <th>name</th>
    </c:if>
    <th>time</th>
    <th>ago</th>
  </tr>
  <v:dataItems>
    <tr class="${item.formatClass}">
      <c:choose>
        <c:when test="${param.id == null}">
          <td><a href="/dataPlot?id=${item.stream.code}"><c:out value="${item.stream.code}"/></a></td>
        </c:when>
        <c:otherwise>
          <td><c:out value="${item.stream.code}"/></td>
        </c:otherwise>
      </c:choose>
      <td><c:out value="${item.value}"/></td>
      <c:if test="${param.id == null}">
        <td><c:out value="${item.stream.name}"/></td>
      </c:if>
      <td><c:out value="${item.time}"/></td>
      <td><c:out value="${item.ago}"/></td>
    </tr>
  </v:dataItems>
</table>
</body>
</html>