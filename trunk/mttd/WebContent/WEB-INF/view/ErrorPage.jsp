<%@page import="br.ufpe.mtd.util.MTDFactory"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@page import="br.ufpe.mtd.view.JSPHelper"%>
<%@page isErrorPage="true" %>
<%
	JSPHelper.limparSessao(session);
	MTDFactory.getInstancia().getLog().salvarDadosLog(new Exception(exception));
%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<link href="css/style-mtd.css" rel="stylesheet" />
<script type="text/javascript" src="js/script-mtd.js"></script>
<title>MTTD-UFPE</title>
</head>
<body>
	<div class="quebra-linha"></div>
	<div id="cabecalho" >
		<div id="menu-cabecalho-titulo">
			<h1>MTTD-UFPE</h1>
		</div>
	</div>
	<div class="quebra-linha"></div>
	
	<div class="poppup ajuda-contato-popup visivel" style="width: 600px; height: 200px;">
		<h2 class="title">Opa!</h2>
		<p>	Desculpe o transtorno.<br/>
			O sistema pode estar passando por manutenção ou instabilidade. Verifique a mensagem abaixo.	
		</p>
		
		<p><%= exception.getMessage() %></p>
		
		<form action="./" >
			<input type="submit" value="Voltar" title="Voltar ao inicio" class="botao"/>
		</form>
	</div>
<div class="quebra-linha"></div>
<div id="linha-inferior-cabecalho"></div>
</body>
</html>