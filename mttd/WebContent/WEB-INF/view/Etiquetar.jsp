<%@page import="br.ufpe.mtd.util.analizers.JTreeTagger"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Etiquetador Web Application</title>
  </head>
  <body>
    <h1>Marca palavras e sintagmas nominais de um texto</h1>
    <form action="Etiquetar" method="POST" >
      <p> Digite ou cole o texto abaixo:<br/>
          
   <TEXTAREA name="texto"  rows="20" cols="80" maxlength="2000"></TEXTAREA>
            
          </p>
      <p> 
        <input type="submit" name="Submeter" value="Submeter texto" />
        <input type="reset" name="Resetar" value="Resetar" />
      </p>
    </form>
    
    <br/><br/>

    <%
    String texto   = request.getParameter("texto");
    
    if (texto == null || texto.trim().length() == 0) {
    %>
      Você não digitou nenhum texto!<br><br><br>
    <%
    } else {
    	texto = JTreeTagger.getInstance().etiquetar(texto).getTextoEtiquetado();
    %>
      Texto: <%=texto%><br><br><br> 
    <%
    }
    %>
    <a href="Etiquetar">Tentar novamente?</a>
  </body>
</html>