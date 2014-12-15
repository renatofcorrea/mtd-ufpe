<div id="cabecalho">
	<div id="menu-cabecalho-titulo">
		<h1>MTTD-UFPE</h1>
	</div>
</div>
<div class="quebra-linha"></div>
<div id="linha-inferior-cabecalho"></div>
<h2 class="title">Ajuda</h2>
<ol>
	<li>
		<p align="left">
			<b> Visão geral </b>- O MTTD-UFPE é um Sistema de Recuperação de Informação (SRI) que: 
		</p>
		<ul>
			<li>
				Faz uso de uma Rede Neural Artificial (RNA) para auxiliar na busca dos usuários por documentos em uma biblioteca digital. 
			</li>
			<li>
				Através da RNA é gerado um mapa onde cada célula agrupa documentos por similaridade de conteúdo, e células próximas possuem documentos semelhantes. 
			</li>
			<li>
				As células são agrupadas e coloridas de acordo com a área de conhecimento predominante.
			</li>
			<li>
				Coleta novos documentos e treina a RNA periodicamente, o que permite que o mesmo esteja atualizado.
			</li>			
		</ul>
	</li>
	<li>
		<p align="left">
			<b> Conteúdo atual </b>- Atualmente o MTTD-UFPE está provendo acesso aos dados da Biblioteca Digital de Testes e Dissertações da UFPE (BDTD-UFPE).
		</p>
	</li>
	<li>
		<p align="left">
			<b>Mecanismos de pesquisa </b> - Através do MTTD-UFPE é possível realizar pesquisas por <b> recuperação </b> e por <b> navegação </b>.
			</p> 
	<li>
		<p align="left">
			<b>Recuperação </b> -  É possível realizar buscas digitando palavras ou frase na <b>caixa de pesquisa</b> e clicando no botão Pesquisar.
			 Frases são especificadas envolvendo as palavras por aspas duplas.
			 Serão retornados em ordem de relevância os documentos cujo resumo contenha alguma das palavras da busca ou a frase especificada.
			 Os documentos serão listados na aba <b>Retornados</b>. As células do <b> mapa </b> contendo documentos retornados recebem uma marcação indicando a quantidade de documentos retornados que ela contém.
			 Ao digitar palavras na caixa de pesquisa, o MTTD-UFPE realizará <b>sugestão de busca</b> exibindo termos existentes no sistema relacionados ao que o usuário está digitando.
			Funciona com Sintagmas Nominais ou Palavras-chave, dependendo da opção de sugestão selecionada:
		</p>
		<ul>
			<li>
				<p align="left">
					<b>Sugestão por Palavras-chave </b> - Palavras-chaves dos documentos no sistema são sugeridas enquanto o usuário digita. Selecionando expressões de
					busca contendo Palavras-chave na caixa de texto e clicando o botão
					Pesquisar a lista de documentos resultante da busca aparecerá 
					na aba Retornados. As células contendo documentos retornados recebem uma marcação indicando a quantidade de documentos nela encontrados que casa com a busca.
				</p>
			</li>
			<li>
				<p align="left">
					<b>Sugestão por Sintagma nominal </b> - Sintagmas nominais extraídos dos resumos dos documentos são sugeridos enquanto o usuário digita. Selecionando expressões de
					busca contendo Sintagma nominal na caixa de texto e clicando o botão
					Pesquisar a lista de documentos resultante da busca aparecerá 
					na aba Retornados. As células contendo documentos retornados recebem uma marcação indicando a quantidade de documentos nela encontrados que casa com a busca.
				</p>
			</li>			
		</ul>
	</li>
	<li>
		<p align="left">
			<b> Navegação </b> - Documentos podem ser encontrados através da navegação pelo mapa de documentos. Pousando o mouse por sobre uma célula será exibidas informações descritivas daquela célula. 
			Clicando em uma célula do
			mapa será aberta uma nova aba com o tílulo <b>Célula</b>. Nesta aba será
			exibida a lista de todos os documentos daquela célula. A lista de documentos pode ser ordenada por qualquer um dos campos da tabela exibida. Os documentos podem ser filtrados localmente através do campo <b>Filtro</b>.
			
		</p>
	</li>
	<li>
		<p align="left">
			<b> Interface</b>- As abas são exibidas de acordo com as informações disponíveis.<br/>
		</p>
		<ul>
			<li>
				<b>Mapa</b>- É a aba inicial que contém o mapa resultante do ultimo treinamento da rede neural<br/>
			</li>
			<li>
				<b>Retornados</b>- É a aba que contem os documentos retornados da ultima busca realizada.<br/>
			</li>
			<li>
				<b>Célula</b>- É a aba que contém todos os documentos da última uma célula clicada.<br/>
			</li>
		</ul>
	</li>	
</ol>

<div class="quebra-linha"></div>
