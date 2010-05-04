<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%@taglib prefix="f" uri="http://java.sun.com/jsf/core"%>
<%@taglib prefix="h" uri="http://java.sun.com/jsf/html"%>
<jsp:useBean id="testeSessao" scope="application" class="mtd.handler.FachadaHandler" />

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<%--
    This file is an entry point for JavaServer Faces application.
--%>
<f:view>
    <html>
        <head>
            <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
            <title>JSP Page</title>
        </head>
        <body>
            <h1><h:outputText value="#{fachada.nome}"/></h1>
           
 <%
out.println(testeSessao.getMapaRandomico());
%>
<h:commandButton actionListener="#{fachada.mudarImagem}">
</h:commandButton>


<h:graphicImage>
    
</h:graphicImage>


        </body>
    </html>
</f:view>
