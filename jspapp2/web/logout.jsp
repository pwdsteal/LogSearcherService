<%--
  Created by IntelliJ IDEA.
  User: opushkarev
  Date: 19.12.2016
  Time: 15:23
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>logout</title>
</head>
<body>
<%@ page session="true"%>

User '<%=request.getRemoteUser()%>' has been logged out.

<%

    util.EventLogger.getInstance().logEvent(request, "logout");

    session.invalidate();
    request.logout();

    response.sendRedirect("mylogin.jsp");

%>


</body>
</html>
