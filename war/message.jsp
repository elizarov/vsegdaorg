<%@taglib uri="/WEB-INF/tlds/c.tld" prefix="c" %>
<%@taglib uri="/WEB-INF/tlds/v.tld" prefix="v" %>
<!DOCTYPE html>
<html>
<head>
  <title>vsegda.org | Message items</title>
  <link rel="stylesheet" href="style.css" type="text/css">
</head>
<body>
<table>
  <tr>
    <th>id</th>
    <th>text</th>
    <th>time</th>
    <th>ago</th>
    <th>index</th>
  </tr>
  <v:messageItems>
    <tr>
      <c:choose>
        <c:when test="${req.id == null}">
          <td><a href="?id=${item.queueId}&all"><c:out value="${item.queueId}"/></a></td>
        </c:when>
        <c:otherwise>
          <td><c:out value="${item.queueId}"/></td>
        </c:otherwise>
      </c:choose>
      <td><c:out value="${item.text}"/></td>
      <td><c:out value="${item.time}"/></td>
      <td><c:out value="${item.ago}"/></td>
      <td><c:out value="${item.messageIndex}"/></td>
    </tr>
  </v:messageItems>
</table>
</body>
</html>
