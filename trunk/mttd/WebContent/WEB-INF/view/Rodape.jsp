<%@page import="java.util.*, java.text.*" %>
<div id="linha-superior-rodape"></div>
<% int dia = Integer.parseInt((new SimpleDateFormat("dd")).format(new Date()));
int mes = Integer.parseInt((new SimpleDateFormat("M")).format(new Date()));
int ano = Integer.parseInt((new SimpleDateFormat("yyyy")).format(new Date()));
%>


<div id="msg-copyrigth" align="center">Mapeador Tem�tico de Teses e Disserta��es - �2010-<%out.println(ano);%> UFPE (Universidade Federal de Pernambuco)</div>
<br/>
</body>
</html>