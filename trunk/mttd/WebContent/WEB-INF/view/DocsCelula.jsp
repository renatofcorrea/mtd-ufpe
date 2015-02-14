<%@page import="java.util.Comparator"%>
<%@page import="java.util.TreeMap"%>
<%@include file="Util.jsp"%>
<%@page import="br.ufpe.mtd.dados.indice.RepositorioIndiceLucene"%>
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
<%
	List<MTDDocument> documentosUltimaPesquisa = JSPHelper.resultadoBuscaSalvo(session);
	List<MTDDocument> documentosNodo = JSPHelper.recuperarDocumentosNodo(request, session);
	Nodo nodo = JSPHelper.recuperarNodo(request,session);
	
	List<MTDDocument> listaNaPesquisa = new ArrayList<MTDDocument>();
	List<MTDDocument> listaOrdenada = new ArrayList<MTDDocument>();
	
	if(documentosUltimaPesquisa != null){
		for(Object docPesquisado: documentosNodo){
			MTDDocument doc = (MTDDocument)docPesquisado;
			boolean docNaPesquisa = false;
			for(Object aux : documentosUltimaPesquisa){
				MTDDocument docAux = (MTDDocument)aux;
				if(docAux.getDocId() == doc.getDocId()){
					listaNaPesquisa.add(doc);
					break;
				}
			}
		}
	}
	
	documentosNodo.removeAll(listaNaPesquisa);
	listaOrdenada.addAll(listaNaPesquisa);
	listaOrdenada.addAll(documentosNodo);
	
	
%>
<%@include file="Cabecalho.jsp"%>
<link href="css/style-tabela-sort.css" rel="stylesheet" />
<script type="text/javascript">
<!--
	ativarAba('aba_docs_nodo');
//-->
</script>
<div class="corpo"><!-- Corpo inicio -->
		<div >
		<div id="qs">
			<form action="">
				<p>
					<% if(nodo != null){%>
						<%="<b>Celula:</b> "+nodo.getId()+"&nbsp;&nbsp;&nbsp;<b>Palavras chave</b> ["+ nodo.getMaioresPesos().get(0).getPalavra().getStrPalavra()+", "+nodo.getMaioresPesos().get(1).getPalavra().getStrPalavra()
						+", "+nodo.getMaioresPesos().get(2).getPalavra().getStrPalavra()+"] "
						
						%>
					<%}%>
					<%=JSPHelper.hasBuscaSalva(session)? "&nbsp;&nbsp;&nbsp;<b>Ultima Busca:</b> "+JSPHelper.recuperarTermoUltimaBusca(session) : "" %>
				</p>
				<br/>
				<p><b>Filtro:</b>
					<input type="text" name="qsfield" id="qsfield" autocomplete="off" size="50" title="Digite palavras ou expressões regulares" />
					<input type="button" onclick="clearQS()" value="Limpar" title="Limpar Filtro" class="botao"/>
					&nbsp; <b>Número de documentos recuperados:</b><span id="stat">0</span>.</p>
				<div id="qssettings">
					<p onclick="toggleQSettingsDialog()">Opções de Busca</p>
					<ul></ul>
				</div>
			</form>
		</div>
		
		<table class="sortable" id="qstable">
			<thead>
				<tr >
					<th style="width: 8%;" id="th-coincidente">Retornado</th>
					<th style="width: 8%;">Autor</th>
					<th style="width: 34%;">Titulo</th>
					<th style="width: 25%;">Programa</th>
					<th style="width: 5%;">Ano</th>
					<th style="width: 5%;">Grau</th>
					<th style="width: 5%;">URL</th>
				</tr>				
			</thead>	
			<tbody>
			
			<%if(documentosNodo != null){%>
				<% int i = 0;%>
				<% for(Object docPesquisado: listaOrdenada){%>
					<% i++;%>
					<% MTDDocument doc = (MTDDocument)docPesquisado;%>
					<% String docId = "p_"+doc.getDocId();%>
					<%
						boolean docNaPesquisa = false;
						for(Object aux : listaNaPesquisa){
							MTDDocument docAux = (MTDDocument)aux;
							if(docAux.getDocId() == doc.getDocId()){
								docNaPesquisa = true;
								break;
							}
						}
					%>
					<tr class="entry" <%=docNaPesquisa ? "style=\"background-color: #FFF68F;\"": ""%> >
						<td ><%= docNaPesquisa ? "SIM":"NÃO" %> </td>
						<td ><%= doc.getAutor() %> </td>
						<td> <%=doc.getTitulo()%>
							<br/>
							<div style="position: relative;float:left; left: 0px;" onclick="exibir('<%= docId+"_Resumo" %>');">[Resumo]</div>
							<div style="position: relative;float:left;left: 0px;" onclick="exibir('<%= docId+"_BibTex" %>');">[BibTex]</div>
							<div class="poppup entry-popup" 
									id="<%=docId+"_Resumo"%>">
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
							<div class="poppup entry-popup"  
									id="<%=docId+"_BibTex"%>" >
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
						</td>
						
						<td><%=doc.getPrograma() %></td>
						<td align="center"><%=doc.getAnoDefesa() %></td>
						<td align="center"><%=doc.getGrau() %></td>
						<td align="center"><a target="<%=doc.getDocId()%>" href="<%=doc.getUrl() %>">abrir</a></td>
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
	get('th-coincidente').className="sort_asc";
//-->
</script>
<%@include file="Rodape.jsp"%>