<%@include file="Util.jsp"%>
<%@page import="br.ufpe.mtd.negocio.entidade.Nodo"%>
<%@page import="br.ufpe.mtd.negocio.entidade.MTDDocument"%>
<%@page import="br.ufpe.mtd.util.enumerado.AreaCNPQEnum"%>
<%@page import="br.ufpe.mtd.negocio.entidade.PalavrasNodo"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.Collections"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%@page import="br.ufpe.mtd.util.MTDFactory"%>
<%@page import="br.ufpe.mtd.negocio.entidade.Mapa"%>
<link href="css/style-tabela-sort.css" rel="stylesheet" />
<%
	List<MTDDocument> documentosPesquisa = JSPHelper.resultadoBuscaSalvo(session);
	documentosPesquisa = JSPHelper.setarNodosDocs(documentosPesquisa);
%>
<%@include file="Cabecalho.jsp"%>
<script type="text/javascript">
<!--
	ativarAba('aba_resultado_busca');
//-->
</script>
<div class="corpo"><!-- Corpo inicio -->
		<div >
		<div id="qs">
			<form action="">
				<p><%=JSPHelper.hasBuscaSalva(session)? "<b>Ultima Busca:</b> "+JSPHelper.recuperarTermoUltimaBusca(session)+"<br/>&nbsp;" : "" %></p>
				<p><b>Filtro:</b> 
					<input type="text" name="qsfield" id="qsfield" autocomplete="off" size="50" title="Digite palavras ou expressões regulares" />
					<input type="button" onclick="clearQS()" value="Limpar" title="Limpar Filtro" class="botao"/>
					&nbsp; <b>Número de documentos recuperados:</b> <span id="stat">0</span>.</p>
				<div id="qssettings">
					<p onclick="toggleQSettingsDialog()">Opções de Busca</p>
					<ul></ul>
				</div>
			</form>
		</div>
		
		<table class="sortable invisivel" id="qstable">
			<thead>
				<tr >
					<th style="width: 6%;">Ordem</th>
					<th style="width: 10%;">Autor</th>
					<th style="width: 35%;">Titulo</th>
					<th style="width: 25%;">Programa</th>
					<th style="width: 5%;">Ano</th>
					<th style="width: 5%;">Grau</th>
					<th style="width: 2%;">URL</th>
					<th style="width: 2%;">Célula</th>
				</tr>				
			</thead>
			<tbody>
			
			<%if(documentosPesquisa!= null){%>
				<% int i = 0;%>
				<% for(Object docPesquisado: documentosPesquisa){%>
					<% i++;%>
					<% MTDDocument doc = (MTDDocument)docPesquisado;%>
					<% String docId = "p_"+doc.getDocId();%>
					
					<tr class="entry" style="background-color: #FFF68F">
						<td align="center" ><%= i < 10 ? "0"+i : i%></td>
						<td ><%=doc.getAutor() %></td>
						
						<td> <%=doc.getTitulo()%>
							<br/>
							<div style="position: relative;float:left; left: 0px;" onclick="exibir('<%= docId+"_Resumo" %>');">[Resumo]</div>
							<div style="position: relative;float:left;left: 0px;" onclick="exibir('<%= docId+"_BibTex" %>');">[BibTex]</div>
							<div class="poppup entry-popup" 
									id="<%=docId+"_Resumo"%>" >
									<div onclick="ocultar('<%=docId+"_Resumo"%>');" style="border-bottom-style: solid;position: relative;float: right;" >Fechar X</div>
									<br/>
									<div>
										<%=doc.getTitulo()%>
									</div>
									<br/>
									<div>
										<%=doc.getResumo()%><br/>
										Palavras-chave: <%=doc.toStringKeyWord() %>
									</div>
									
							</div>
							<div class="poppup entry-popup" id="<%=docId+"_BibTex"%>" >
									<div onclick="ocultar('<%=docId+"_BibTex"%>');" style="border-bottom-style: solid;position: relative;float: right;" >Fechar X</div>
									<br/>
									<div>
										BibTex:<br/>
										<%=doc.getGrau().equalsIgnoreCase("mestre")? "@MASTERTHESIS" : "@PHDTHESIS"%>
										<%="{"+doc.getId()%>,<br/>
										&nbsp;<%="author = {"+doc.getAutor()+"}"%>,<br/>
										&nbsp;<%="title = {"+doc.getTitulo()+"}"%>,<br/>
										&nbsp;<%="school = {"+doc.getNomeInstituicao()+"}"%>,<br/>
										&nbsp;<%="year = {"+doc.getAnoDefesa()+"}"%>,<br/>
										&nbsp;<%="type = {"+(doc.getGrau().equalsIgnoreCase("mestre")?"Dissertação de mestrado":"Tese de Doutorado")+"}"%>,<br/>
										&nbsp;<%="address = {"+doc.getPrograma()+"}"%>,<br/>
										&nbsp;<%="note = {"+doc.getOrientador()+"}"%>,<br/>
										&nbsp;<%="url = {"+doc.getUrl()+"}"%><br/>
										<%="}"%><br/>
									</div>
							</div>							
						</td>
						
						<td><%=doc.getPrograma() %></td>
						<td align="center"><%=doc.getAnoDefesa() %></td>
						<td align="center"><%=doc.getGrau() %></td>
						<td align="center"><a target="<%=doc.getDocId()%>" href="<%=doc.getUrl() %>">abrir</a></td>
						
						<% 
							Nodo nodo = doc.getNodo();
						%>
						<td align="center" onclick="executarForm('formGlobal','./docs','<%=nodo != null ? "id_linha"+i+"_id_nodo_"+nodo.getId() : ""%>');" style="cursor: pointer;" >
							<%if(nodo != null && nodo.getId()!= null){ %>
								<%
									String prefixo = nodo.getId().intValue() < 10 ? "00" : nodo.getId().intValue() < 100 ? "0" :"";
								%>
								<%=prefixo + nodo.getId()%>
								<input type="hidden" id="<%="id_linha"+i+"_id_nodo_"+nodo.getId()%>" name="nodo_id" value="<%=nodo.getId()%>">
							<%}%>
						</td>
					</tr>
				<% }%>
			<% }%>
			</tbody>
		</table>
		</div>
</div ><!-- Corpo Fim -->
<div class="quebra-linha"></div>
<div class="fim-tabela"></div>
<script type="text/javascript">
<!--
	exibir('qstable');
//-->
</script>
<%@include file="Rodape.jsp"%>