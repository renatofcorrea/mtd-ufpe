package br.ufpe.mtd.view;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.solr.client.solrj.SolrServerException;

import br.ufpe.mtd.dados.indice.RepositorioIndiceLucene;
import br.ufpe.mtd.negocio.MTDFacede;
import br.ufpe.mtd.negocio.entidade.MTDDocument;
import br.ufpe.mtd.negocio.entidade.Mapa;
import br.ufpe.mtd.negocio.entidade.Nodo;
import br.ufpe.mtd.util.MTDException;
import br.ufpe.mtd.util.MTDFactory;
import br.ufpe.mtd.util.MTDParametros;

/**
 * Classe que vai auxiliar as JSPs do sistema a realizar determinada atividades
 * sem ter que inchar o codigo das mesmas.
 * 
 * Caso o sistema venha a aumentar de tamanho pode-se colocar uma classe com esse objetivo para
 * cada pagina.
 * 
 * @author djalma
 *
 */
public class JSPHelper {

	private static Integer media;
	private static MTDFactory f = MTDFactory.getInstancia();

	public static void limparSessao(HttpSession sessao) {
		Enumeration<String> atributos = sessao.getAttributeNames();
		while (atributos.hasMoreElements()) {
			sessao.removeAttribute(atributos.nextElement());
		}
	}

	public synchronized static int mediaDocs() {
		try {
			if (media == null) {
				RepositorioIndiceLucene rep = (RepositorioIndiceLucene) MTDFactory.getInstancia().getSingleRepositorioIndice();
				media = rep.getQuantidadeDocumentosNoIndice() / recuperarMapa().getNodos().size();
			}
		} catch (Exception e) {
			media = 100;
		}
		return media;
	}
	
	public static NodosAnalisados  nodosAnalisados(List<MTDDocument> documentosPesquisa, List<Nodo> nodos){
		NodosAnalisados na = new NodosAnalisados();
		List<NodoHolder> lista = new ArrayList<JSPHelper.NodoHolder>();
		NodoHolder maior = null;
		
		for(Nodo aux : nodos){
			NodoHolder nh = new NodoHolder();
			nh.setNodo(aux);
			
			//Quantidade de documentos retornados em um nodo para a busca
			int qtdDocsNodo = 0;
			
			//obs: de acordo com a quantidade maxima de docs retornados na busca, alguns nodos podem nao ser marcados.
			//mesmo que tenham docs que casam com a busca, no caso da busca ser muito generica.
			if(documentosPesquisa!= null){
				for(Object docPesquisado: documentosPesquisa){
					MTDDocument doc = (MTDDocument)docPesquisado;
					for(MTDDocument docAux : nh.getNodo().getDocumentos()){
						if(docAux.getDocId() == doc.getDocId()){
							nh.setContemDadosBusca(true);
							qtdDocsNodo++;
							break;
						}
					}
				}
			}
			
			nh.setRetornados(qtdDocsNodo);
			
			if(maior == null){
				maior = nh;
				maior.setMaior(true);
			}else if(maior.getRetornados().intValue() < nh.getRetornados().intValue()){
				maior.setMaior(false);
				maior = nh;
				maior.setMaior(true);
			}
			
			lista.add(nh);
		}
		
		na.setMaior(maior);
		na.setLista(lista);
		return na;
	}
	
	public static class NodosAnalisados{
		
		private NodoHolder maior;
		private List<NodoHolder> lista;
		
		public NodoHolder getMaior() {
			return maior;
		}
		
		public void setMaior(NodoHolder maior) {
			this.maior = maior;
		}
		
		public List<NodoHolder> getLista() {
			return lista;
		}
		
		public void setLista(List<NodoHolder> lista) {
			this.lista = lista;
		}
	}
	
	public static class NodoHolder{
		private boolean maior;
		private Nodo nodo;
		private Integer retornados;
		private boolean contemDadosBusca;
		
		public boolean contemDadosBusca() {
			return contemDadosBusca;
		}
		
		public void setContemDadosBusca(boolean contemDadosBusca) {
			this.contemDadosBusca = contemDadosBusca;
		}

		public Nodo getNodo() {
			return nodo;
		}

		public void setNodo(Nodo nodo) {
			this.nodo = nodo;
		}

		public Integer getRetornados() {
			return retornados;
		}

		public void setRetornados(Integer retornados) {
			this.retornados = retornados;
		}

		public boolean isMaior() {
			return maior;
		}

		public void setMaior(boolean maior) {
			this.maior = maior;
		}
		
	}

	public static boolean exibirUnicaKeyWord() {
		boolean valorDefault = false;
		try {
			// exibir palavras chaves dentro das celulas do mapa
			String strExibir = MTDParametros.exibirUnicaKeyWord();
			return strExibir == null ? valorDefault : Boolean.parseBoolean(strExibir);

		} catch (Exception e) {
			// TODO: handle exception
		}
		return valorDefault;
	}

	public static int qtdColunasMapa() {
		int valorDefault = 12;
		try {
			// quantidade de coluas a serem exibidas no mapa
			String strColunas = MTDParametros.qtdColunasMapa();
			return strColunas == null ? valorDefault : Integer.parseInt(strColunas);

		} catch (Exception e) {
			// TODO: handle exception
		}
		return valorDefault;
	}

	public static List<MTDDocument> setarNodosDocs(List<MTDDocument> documentosPesquisa) throws Exception {

		List<Nodo> nodos = recuperarMapa().getNodos();

		// setar os nodos nos docs
		if (nodos != null && documentosPesquisa != null) {
			for (MTDDocument doc : documentosPesquisa) {
				for1: for (Nodo nodo : nodos) {
					for (MTDDocument docAux : nodo.getDocumentos()) {
						if (docAux.getDocId() == doc.getDocId()) {
							doc.setNodo(nodo);
							break for1;
						}
					}
				}
			}
		}

		return documentosPesquisa;
	}

	/**
	 * MOstra o ultimo resultado de busca salovo na sessao.
	 * 
	 * @return
	 */
	public static List<MTDDocument> resultadoBuscaSalvo(HttpSession sessao) {
		return (List<MTDDocument>) sessao.getAttribute("resultado_busca");
	}

	public static Mapa recuperarMapa() throws Exception {
		return MTDFacede.recuperarMapa();
	}

	/**
	 * Recupera os docs de um nodo atraves de requisicao feita pelo usuario na
	 * tela do mapa.
	 * 
	 * @param request
	 * @return
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	public static List<MTDDocument> recuperarDocumentosNodo(HttpServletRequest request, HttpSession sessao) throws Exception {

		boolean pesquisar = false;

		String nodoId = (String) request.getParameter("nodo_id");
		if (nodoId == null) {
			nodoId = (String) sessao.getAttribute("nodo_id");
		} else {
			pesquisar = true;
		}

		Nodo nodo = null;
		if (nodoId != null) {
			nodo = recuperarMapa().getNodo(Integer.parseInt(nodoId));
			sessao.setAttribute("nodo_id", nodoId);
		}

		List<MTDDocument> documentosNodo = nodo != null ? nodo.getDocumentos() : null;

		if (documentosNodo != null) {
			if (pesquisar) {
				List<Integer> ids = new ArrayList<Integer>();
				for (int i = 0; i < documentosNodo.size(); i++) {
					MTDDocument doc = (MTDDocument) documentosNodo.get(i);
					ids.add(doc.getDocId());
				}
				RepositorioIndiceLucene ri = (RepositorioIndiceLucene) MTDFactory.getInstancia().getSingleRepositorioIndice();
				documentosNodo = ri.getDocumentos(ids);
				sessao.setAttribute("docs", documentosNodo);
			} else {
				documentosNodo = (List<MTDDocument>) sessao.getAttribute("docs");
			}
		}

		return documentosNodo;
	}

	public static Nodo recuperarNodo(HttpServletRequest request, HttpSession sessao) throws IOException, NumberFormatException, ClassNotFoundException {
		Nodo nodo = null;

		try {
			Mapa mapa = recuperarMapa();
			String nodoId = request.getParameter("nodo_id");
			if (nodoId == null) {
				nodoId = (String) sessao.getAttribute("nodo_id");
			}

			nodo = mapa.getNodo(Integer.parseInt(nodoId));
		} catch (Exception e) {
			f.getLog().salvarDadosLog(e);
		}

		return nodo;
	}
	
	public static void salvarTipoSugestao(HttpServletRequest request, String tipoSugestao){
		if(tipoSugestao == null){
			tipoSugestao = MTDFacede.sugestaoPadrao();
		}
		try {
			request.getSession().setAttribute("tipo_sugestao", tipoSugestao);
		} catch (Exception e) {
			//
		}
	}
	
	public static String sugestaoPalavraChave(){
		return MTDFacede.sugestaoPadrao();
	}
	
	public static String sugestaoSintagma(){
		return MTDFacede.sugestaoSintagma();
	}
	
	public static String getTipoSugestao(HttpSession sessao){
		String tipoSugestao = MTDFacede.sugestaoPadrao();
		try{
			String aux  = (String) sessao.getAttribute("tipo_sugestao");
			tipoSugestao = MTDFacede.getTipoSugestao(aux);
		}catch(Exception e){
			//
		}
		return tipoSugestao;
	}

	public static List<MTDDocument> realizarBusca(HttpServletRequest request, HttpSession sessao) throws CorruptIndexException, ParseException, IOException, SolrServerException {
		
		List<MTDDocument> documentosPesquisa = null;
		try {
			String strTermoBusca = (String) request.getParameter("termo_busca");
			validarTermoBusca(strTermoBusca);
			
			if (strTermoBusca != null && !strTermoBusca.trim().isEmpty()) {
				documentosPesquisa = f.getSingleRepositorioIndice().consultar(strTermoBusca, new String[] { MTDDocument.TITULO, MTDDocument.RESUMO, MTDDocument.KEY_WORD, MTDDocument.AUTOR, MTDDocument.ORIENTADOR, MTDDocument.PROGRAMA, MTDDocument.AREA_CNPQ }, 100);
				
				sessao.setAttribute("termo_busca", strTermoBusca);
			}
		} catch (Exception e) {
			f.getLog().salvarDadosLog(e);
		}
		return documentosPesquisa;
	}

	public static void salvarDocsBusca(List documentosPesquisa, HttpSession sessao) {
		sessao.setAttribute("resultado_busca", documentosPesquisa);
	}

	public static String recuperarTermoUltimaBusca(HttpSession sessao) {
		String termo = (String) sessao.getAttribute("termo_busca");
		
		if(termo != null){
			termo = termo.replace("'", "\\'");
		}
		
		return termo;
	}

	public static boolean hasNodoConsultado(HttpSession sessao) {
		String str = (String) sessao.getAttribute("nodo_id");
		return str != null;
	}

	public static boolean hasBuscaSalva(HttpSession sessao) {
		return recuperarTermoUltimaBusca(sessao) != null;
	}
	
	public static void validarTermoBusca(String termo ) throws MTDException{
		if(termo == null){
			termo = "";
		}
		
		int qtdAspas = 0;
		for(byte caractere : termo.getBytes()){
			if(((char)caractere) == '"'){
				qtdAspas++;
			}
		}
		
		if(qtdAspas > 0 && qtdAspas % 2 != 0){
			throw new MTDException("Termo de busca em formato incorreto. possui aspas não fechadas.");
		}
	}
}
