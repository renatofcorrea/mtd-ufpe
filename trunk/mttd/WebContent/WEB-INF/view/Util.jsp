<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@page import="br.ufpe.mtd.view.JSPHelper"%>
<%@page errorPage="ErrorPage.jsp" %>
<%
	if(request.getParameter("acao") != null && request.getParameter("acao").equals("limpar")){
		JSPHelper.limparSessao(session);
	}
%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<link href="css/style-mtd.css" rel="stylesheet" />
<script type="text/javascript" src="js/script-mtd.js"></script>
<title>MTTD-UFPE</title>
</head>
<form id="formGlobal" accept-charset="ISO-8859-1">
</form>