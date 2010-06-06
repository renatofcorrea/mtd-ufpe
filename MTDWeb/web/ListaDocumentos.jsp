<%-- 
    Document   : ListaDocumentos
    Created on : 21/05/2010, 16:10:50
    Author     : Bruno
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<jsp:useBean id="testeSessao" scope="application" class="mtd.handler.FachadaHandler" />
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
   <%
   int id = Integer.parseInt(request.getParameter("id"));
     out.println(testeSessao.getListaDocumentos(id));
%>

        <h1>Hello World!</h1>
    </body>
</html>
