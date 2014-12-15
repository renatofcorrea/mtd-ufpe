<%@include file="Util.jsp"%>
<%@page import="br.ufpe.mtd.view.JSPHelper.NodosAnalisados"%>
<%@page import="br.ufpe.mtd.view.JSPHelper.NodoHolder"%>
<%@page import="br.ufpe.mtd.util.MTDParametros"%>
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
	//exibicao dos labels
	boolean exibirCbs = false;
	boolean exibirChla = false;
	boolean exibirTcen = false;
	boolean exibirOutros = false;
	boolean exibirNEncontrado = false;
	
	//exibir 1 ou 3 palavras chaves por celula
	boolean exibirUnicaKeyWord = JSPHelper.exibirUnicaKeyWord();
	
	//realiza a busca se solicitada no request
	List<MTDDocument> documentosPesquisa = JSPHelper.realizarBusca(request, session);
	if(documentosPesquisa == null){
		documentosPesquisa = JSPHelper.resultadoBuscaSalvo(session);
	}
	if(documentosPesquisa != null){
		JSPHelper.salvarDocsBusca(documentosPesquisa, session);
	}
	
	//qtd de colunas para gerar o mapa
	int qtdColunasMapa = JSPHelper.qtdColunasMapa();
	
	Mapa mapa = JSPHelper.recuperarMapa();
	
%>
<% 	if(mapa != null){%>
<% 
		List<Nodo> nodos = mapa.getNodos();
		
		NodosAnalisados nodosAnalisados = JSPHelper.nodosAnalisados(documentosPesquisa, nodos);
%>
<%@include file="Cabecalho.jsp"%>
<script type="text/javascript">
<!--
	ativarAba('aba_mapa');
//-->
</script>
<div class="corpo" align="center"><!-- Corpo inicio -->
		<div class="areas" id="areas" >
			<div id="label-cbs" title="<%= AreaCNPQEnum.CBS.getDescricao() %>" class="celula-verde celula-afastamento invisivel"><%=AreaCNPQEnum.CBS%>
			</div>
			<div id="label-chla" title="<%= AreaCNPQEnum.CHLA.getDescricao() %>" class="celula-vermelha celula-afastamento invisivel"><%=AreaCNPQEnum.CHLA%>
			</div>
			<div id="label-tcen" title="<%= AreaCNPQEnum.TCEN.getDescricao() %>" class="celula-azul celula-afastamento invisivel"><%=AreaCNPQEnum.TCEN %>
			</div>
			<div id="label-outros" title="<%= AreaCNPQEnum.OUTROS.getDescricao() %>" class="celula-cinza celula-afastamento invisivel" ><%=AreaCNPQEnum.OUTROS%>
			</div>
			<div id="label-nao-encontrado" title="<%= AreaCNPQEnum.NAO_ENCONTRADO.getDescricao() %>" class="celula-sem-classificacao celula-afastamento invisivel"><%=AreaCNPQEnum.NAO_ENCONTRADO.getDescricao()%>
			</div>
		</div>
		<div class="quebra-linha"></div>
		<div id="tabela" class="invisivel">
		<table >
	<%
			int coluna = 1;
			int linha = 1;
			int maiorQtdDocsNodo = 0;
			for(NodoHolder aux : nodosAnalisados.getLista()){
				Nodo nodo = aux.getNodo();
				
				String classe = "";
				String popPupClass = "poppup";
				
				AreaCNPQEnum areaCnpq = nodo.getAreaPredominante();
				
				if(areaCnpq.equals(AreaCNPQEnum.CBS)){
					//classe = "celula-vermelha";
					classe = "celula-verde";
					exibirCbs = true;
				}else if(areaCnpq.equals(AreaCNPQEnum.CHLA)){
					//classe = "celula-verde";
					classe = "celula-vermelha";
					exibirChla = true;
				}else if(areaCnpq.equals(AreaCNPQEnum.TCEN)){
					classe = "celula-azul";
					exibirTcen = true;
				}else if(areaCnpq.equals(AreaCNPQEnum.OUTROS)){
					classe = "celula-cinza";
					exibirOutros = true;
				}else if(areaCnpq.equals(AreaCNPQEnum.NAO_ENCONTRADO)){
					classe = "celula-sem-classificacao";
					exibirNEncontrado = true;
				}
				
				if(coluna > qtdColunasMapa/2){
					if(linha > (nodos.size() / qtdColunasMapa)/2){
						popPupClass += " quadrante4";
					}else{
						popPupClass += " quadrante3";
					}
				}else{
					if(linha > (nodos.size() / qtdColunasMapa)/2){
						popPupClass += " quadrante2";
					}
				}
				
		%>			
				<%if(coluna == 1) {%>
					<tr>
				<%} %>
						<td>
							<div class="<%=classe%>" id="<%=nodo.getId() %>" 
									onclick="executarForm('formGlobal','./docs','<%= "id_nodo_"+nodo.getId()%>');"
									onmouseover="exibir('<%="p_"+nodo.getId() %>')" 
									onmouseout="ocultar('<%="p_"+nodo.getId() %>')">
								<input type="hidden" id="<%="id_nodo_"+nodo.getId()%>" name="nodo_id" value="<%= ""+nodo.getId() %>">
								<div class="<%=popPupClass%>" id="<%="p_"+nodo.getId() %>" style="background-color: inherit;">
									<%="Célula: "+nodo.getId() %><br/>
									<%="Documentos: "+nodo.getDocumentos().size() %><br/>
									<%="Area Predominante: "+nodo.getAreaPredominante()%><br/>
										Palavras-Chave:<br/>
										<%=nodo.getMaioresPesos().size() > 0 && nodo.getMaioresPesos().get(0).getPalavra()!= null? nodo.getMaioresPesos().get(0).getPalavra().getStrPalavra() : "-"%><br/>
										<%=nodo.getMaioresPesos().size()> 1 && nodo.getMaioresPesos().get(1).getPalavra()!= null? nodo.getMaioresPesos().get(1).getPalavra().getStrPalavra() : "-"%><br/>
										<%=nodo.getMaioresPesos().size()> 2 && nodo.getMaioresPesos().get(2).getPalavra()!= null? nodo.getMaioresPesos().get(2).getPalavra().getStrPalavra() : "-"%><br/>
										<%if(aux.contemDadosBusca()) {%>
											<%="Retornados: "+aux.getRetornados()%>
											<div class="marcador amarelo"></div>
									<%}%>
								</div>
								<div style="position:absolute; padding-top:2px;" id="escala-crescente">
									<%if(aux.contemDadosBusca()) {%>
										<div class="marcador amarelo" style="opacity: <%=  ( new Double(aux.getRetornados())/nodosAnalisados.getMaior().getRetornados() - 0.75)/0.25%>;" ></div>
										<div class = "quebra-linha"></div>
										<div class="marcador amarelo" style="opacity: <%=  ( new Double(aux.getRetornados())/nodosAnalisados.getMaior().getRetornados() - 0.50)/0.25%>;" ></div>
										<div class = "quebra-linha"></div>
										<div class="marcador amarelo" style="opacity: <%=  ( new Double(aux.getRetornados())/nodosAnalisados.getMaior().getRetornados() - 0.25)/0.25 %>;" ></div>									
										<div class = "quebra-linha"></div>
										<div class="marcador amarelo" style="opacity: <%= ( new Double(aux.getRetornados())/nodosAnalisados.getMaior().getRetornados())/0.01 %>;" ></div>
									<%}%>
								</div>
								<%if(exibirUnicaKeyWord) {%>
									<div>
										<div>
											<%="&nbsp;"%>
										</div>
										<div class = "quebra-linha"></div>
										<div ><%=nodo.getMaioresPesos().size()> 0 && nodo.getMaioresPesos().get(0).getPalavra()!= null?  nodo.getMaioresPesos().get(0).getPalavra().getStrPalavra() : "-"%>
										</div>
										<div class = "quebra-linha"></div>
										<div ><%="&nbsp;"%>
										</div>
									</div>
								<%}else{%>
									<div>
										<div>
											<%=nodo.getMaioresPesos().size()> 0 && nodo.getMaioresPesos().get(0).getPalavra()!= null?  nodo.getMaioresPesos().get(0).getPalavra().getStrPalavra()  : "-"%>
										</div>
										<div class = "quebra-linha"></div>
										<div ><%=nodo.getMaioresPesos().size()> 1 && nodo.getMaioresPesos().get(1).getPalavra()!= null?  nodo.getMaioresPesos().get(1).getPalavra().getStrPalavra() : "-"%>
										</div>
										<div class = "quebra-linha"></div>
										<div ><%=nodo.getMaioresPesos().size()> 2 && nodo.getMaioresPesos().get(2).getPalavra()!= null?  nodo.getMaioresPesos().get(2).getPalavra().getStrPalavra() : "-"%>
										</div>
									</div>
								<%}%>
							</div>
						</td>
				<%if(coluna % qtdColunasMapa == 0) {%>
					</tr>
					<% coluna = 1;%>
					<% linha++;%>
				<%} else {%>	
					<% coluna++;%>
				<%}%>	
			<%} %>	
	<%} %>
		</table>
		</div>
</div ><!-- Corpo Fim -->	
<div class="quebra-linha"></div>

<script type="text/javascript">
<!--
	var largura = 0;
	var areasAtivas = 0;
	<%= exibirCbs == true ? "exibir('label-cbs'); areasAtivas++; largura += get('label-cbs').offsetWidth;" : "" %>
	<%= exibirTcen == true ? "exibir('label-tcen');areasAtivas++; largura += get('label-tcen').offsetWidth;" : "" %>
	<%= exibirChla == true ? "exibir('label-chla');areasAtivas++; largura += get('label-chla').offsetWidth;" : "" %>
	<%= exibirOutros == true ? "exibir('label-outros');areasAtivas++; largura += get('label-outros').offsetWidth;" : "" %>
	<%= exibirNEncontrado == true ? "exibir('label-nao-encontrado');areasAtivas++; largura += get('label-nao-encontrado').offsetWidth;" : "" %>
	
	var areas = get('areas');
	areas.style.width = (largura + areasAtivas * 20)+"px"; 
	
	exibir('tabela');
//-->
</script>
<%@include file="Rodape.jsp"%>