<%@page import="br.ufpe.mtd.view.JSPHelper"%>
<body>
	<div id="mascara_site" onclick="ocultar('ajuda');ocultar('contato');ocultar('mascara_site');">
	</div>
	<div class="poppup ajuda-contato-popup invisivel" id="ajuda">
		<div onclick="ocultar('ajuda');ocultar('contato');ocultar('mascara_site');" style="border-bottom-style: solid;position: relative;float: right;" >Fechar X</div>
		<%@ include file="Ajuda.jsp" %>
	</div>
	<div class="poppup ajuda-contato-popup invisivel" id="contato">
		<div onclick="ocultar('ajuda');ocultar('contato');ocultar('mascara_site');" style="border-bottom-style: solid;position: relative;float: right;" >Fechar X</div>
		<%@ include file="Contato.jsp" %>
	</div>
	<div class="quebra-linha"></div>
	<div id="cabecalho" >
		<div id="menu-cabecalho-titulo">
			<h1>MTTD-UFPE</h1>
		</div>
		<div id="menu-cabecalho-form">
			<div id="div-form-busca" class="item-menu-cabecalho">
				<form id="formBusca"  action="./" method="POST">
					<div>
						<input type="text" name="termo_busca" autocomplete="off" id="input-termo-busca" size="50" title="Digite o que deseja pesquisar" 
							onkeydown="limparSugestao(event,get('input-termo-busca').value, get('sugestao'), get('input-termo-busca'));"  
							onkeyup="suggest(event,get('sugestao'),this.value,getTipoSugestao());"
							/>
						<div id="sugestao"></div>
						<input id="btn-pesquisar" type="submit" value="Pesquisar" title="Realizar pesquisa" class="botao"/>
					</div>
				</form>
			</div>
			<div id="div-btn-limpar" class="item-menu-cabecalho">
				<form action="./" method="POST">
					<input  type="hidden" name="acao" value="limpar"/>
					<input id="btn-limpar" type="submit" value="Limpar" title="Limpar pesquisa" class="botao"/>
				</form>
			</div>
			<div class="item-menu-cabecalho" style="width:100%; margin-top: -2px; color: #8B4726; text-align: center;">
			
					Sugestão por :
					<%
				String tipoSugestao = JSPHelper.getTipoSugestao(session);
			%>
					
					<input type="radio" name="tipo_sugestao" value="<%=JSPHelper.sugestaoPalavraChave()%>"  onchange="setTipoSugestao(this.value);" 
						<%=tipoSugestao != null && tipoSugestao.equals(JSPHelper.sugestaoPalavraChave()) ? "checked" : ""%>/><b>Palavras-chave</b>
						
					<input type="radio" name="tipo_sugestao" value="<%=JSPHelper.sugestaoSintagma()%>" onchange="setTipoSugestao(this.value);"
						<%=tipoSugestao != null && tipoSugestao.equals(JSPHelper.sugestaoSintagma()) ? "checked" : ""%>	/><b>Sintagmas Nominais</b>
			</div>
		</div>
		<div id="menu-cabecalho-informacoes">
			<div class="item-menu-cabecalho">
					<img src="imagens/interrogacao.png" title="AJUDA" onclick="exibir('mascara_site');exibir('ajuda');"/>
			</div>
			<div class="item-menu-cabecalho">
					<img src="imagens/contato.png" title="CONTATO" onclick="exibir('mascara_site');exibir('contato');"/>
			</div>
		</div>
		<div id="menu-cabecalho-abas" >
				<ul >
					<li><a href="./" id="aba_mapa" class="menu-cabecalho-link"><br/>Mapa</a></li>
					<% if(JSPHelper.hasBuscaSalva(session)){%>
						<li><a href="./resultado" id="aba_resultado_busca" class="menu-cabecalho-link"><br/>Retornados</a></li>
					<%} %>
					<%if(JSPHelper.hasNodoConsultado(session)){%>	
						<li><a href="./docs" id="aba_docs_nodo" class="menu-cabecalho-link"><br/>Célula</a></li>
					<%} %>
				</ul>
		</div>
	</div>
	<div class="quebra-linha"></div>
	<div id="linha-inferior-cabecalho"></div>
	
	<script type="text/javascript">
		//seleciona e formatar a largura da caixa de sugestao
		var busca = get('input-termo-busca');
		busca.focus();
		
		<%String strUltimoTermo = JSPHelper.recuperarTermoUltimaBusca(session);%>
		busca.value = '<%= strUltimoTermo != null ? strUltimoTermo : "" %>';
		
		var largura = busca.offsetWidth;
		var sugestao = get('sugestao');
		sugestao.style.width = largura+"px";
		
		
		
		//formatar a largura do menu-cabecalho-form
		get('menu-cabecalho-form').style.width = (get('div-form-busca').offsetWidth + get('div-btn-limpar').offsetWidth + 15)+"px";
		
		var tipoSugestao = '<%=JSPHelper.getTipoSugestao(session)%>';
		function setTipoSugestao(valor){
			tipoSugestao = valor;
		}
		
		function getTipoSugestao(){
			return tipoSugestao;
		}
		
	</script>
