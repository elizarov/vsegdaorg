<%@taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>
<%@taglib uri="/WEB-INF/tld/v.tld" prefix="v" %>
<!DOCTYPE html>
<html>
<head>
  <title>vsegda.org | Message items</title>
  <link rel="stylesheet" href="/style.css" type="text/css">
</head>
<body>
<div class="par hdr">
  Message items
</div>
<table class="par data">
  <tr>
    <th>id</th>
    <th>text</th>
    <th>time</th>
    <th>ago</th>
    <th>index</th>
  </tr>
  <v:messageItems>
    <tr class="${item.formatClass}">
      <c:choose>
        <c:when test="${param.id == null}">
          <td><a href="?id=${item.queueId}"><c:out value="${item.queueId}"/></a></td>
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
