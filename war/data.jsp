<%@taglib uri="/WEB-INF/tlds/c.tld" prefix="c" %>
<%@taglib uri="/WEB-INF/tlds/v.tld" prefix="v" %>
<!DOCTYPE html>
<html>
<head>
  <title>vsegda.org | Data items</title>
  <link rel="stylesheet" href="style.css" type="text/css">
</head>
<body>
<table>
  <tr>
    <th>id</th>
    <th>value</th>
    <th>time</th>
    <th>ago</th>
  </tr>
  <v:dataItems>
    <tr>
      <c:choose>
        <c:when test="${req.id == null}">
          <td><a href="?id=${item.streamId}"><c:out value="${item.streamId}"/></a></td>
        </c:when>
        <c:otherwise>
          <td><c:out value="${item.streamId}"/></td>
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
