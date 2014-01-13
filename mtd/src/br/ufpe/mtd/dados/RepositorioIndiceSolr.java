package br.ufpe.mtd.dados;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.ParseException;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;

import br.ufpe.mtd.entidade.DocumentMTD;
import br.ufpe.mtd.excecao.MTDException;
import br.ufpe.mtd.util.Log;
import br.ufpe.mtd.util.MTDFactory;
import br.ufpe.mtd.util.MTDUtil;

/**
 * Classe que fara o controle de acesso ao indice do 
 * lucene.
 * 
 * Deve ser instanciado atraves da MTDFactory para otimizacao
 * de performance.
 * 
 * Permite criacao de indice, leitura, adicao de novos registros.
 * 
 * @author djalma
 *
 */
public class RepositorioIndiceSolr implements IRepositorioIndice{

	private SolrServer solrServer;
	
	public RepositorioIndiceSolr(String solrUrl) {
		solrServer = new HttpSolrServer(solrUrl);
	}
	
	//TODO: termos devem ter local central (classe ou arquivo de confguracao
	public synchronized ArrayList<DocumentMTD> consultar(String termo, int maxResultado)
			throws ParseException, CorruptIndexException, IOException, SolrServerException {
		
		ArrayList<DocumentMTD> retorno = new ArrayList<DocumentMTD>();

		SolrQuery parameters = new SolrQuery();
		parameters.set("q", "id:552199");
		QueryResponse resposta = solrServer.query(parameters);
		
		
		SolrDocumentList list = resposta.getResults();
		for (SolrDocument document : list) {
			
			
			DocumentMTD docAtual = new DocumentMTD(document.get(DocumentMTD.TITULO).toString(), 
					document.get(DocumentMTD.RESUMO).toString(), 
					null,
					MTDUtil.recuperarDataFormatosSuportados(document.get(DocumentMTD.DATA_DEFESA).toString().trim()), 
					document.get(DocumentMTD.AUTOR).toString(), 
					document.get(DocumentMTD.PROGRAMA).toString(), 
					document.get(DocumentMTD.ORIENTADOR).toString(), 
					document.get(DocumentMTD.AREA_CNPQ).toString(), 
					document.get(DocumentMTD.ID).toString(), 
					document.get(DocumentMTD.AREA_PROGRAMA).toString());
			
			docAtual.setRepositorio(document.get(DocumentMTD.REPOSITORIO).toString());
			docAtual.setUrl(document.get(DocumentMTD.URL).toString());
			docAtual.setGrau(document.get(DocumentMTD.GRAU).toString());
			
			// docAtual.setNodo(documentoNodo.get(docAtual.getId()+""));
			retorno.add(docAtual);
		}

		return retorno;
	}



	/**
	 * Permite adicionar uma colecao de documentos
	 * deve ser usado preferencialmente mesmo que em alguns
	 * casos seja passado apenas 1 doc.
	 * 
	 * @param dosc
	 * @throws CorruptIndexException
	 * @throws IOException
	 * @throws SolrServerException 
	 */
	public synchronized void inserirDocumento(List<Document> dosc) throws SolrServerException, IOException {
		
		List<SolrInputDocument> listaInserir = new ArrayList<SolrInputDocument>();
		
		for (Document document : dosc) {
			List campos = document.getFields();
			SolrInputDocument iDocument = new SolrInputDocument();
			
			for (Object campo : campos) {
				Field field = (Field)campo;
				iDocument.addField(field.name(), field.stringValue());
			}
			
			listaInserir.add(iDocument);
		}
		
		//tentar inserir todos de uma vez
		try {
			solrServer.add(listaInserir);
			solrServer.commit();
			
		} catch (Exception e) {
			Log log = MTDFactory.getInstancia().getLog();
			
			String strIds = "";
			for (SolrInputDocument solrInputDocument : listaInserir) {
				strIds+= solrInputDocument.getFieldValue(DocumentMTD.ID)+",";
			}
			
			log.salvarDadosLog(new MTDException(e, "Falha ao tentar inserir lista : {"+strIds+"}\nIniciando inserção de documentos individualmente..."));
			
			inserirDocumentoIndividualmente(listaInserir);
		}
		
	}
	
	/*
	 * Metodo auxiliar para tratar casos onde ocorra excecao na insercao em massa de 
	 * documentos no Solr.
	 * 
	 * Para não dar erro em toda a lista tentaremos inserir indivialmente cada doc
	 * ficando apenas para aqueles documentos com problema de fora do indice.
	 * 
	 * @param listaInserir
	 */
	private void inserirDocumentoIndividualmente(List<SolrInputDocument> listaInserir){
		Log log = MTDFactory.getInstancia().getLog();
		
		for (SolrInputDocument iDocument : listaInserir) {
			try {
				log.salvarDadosLog("Tentando inserir documento individualmente: "+iDocument.getFieldValue(DocumentMTD.ID));
				solrServer.add(iDocument);
				solrServer.commit();
				
			} catch (Exception e) {
				log.salvarDadosLog(new MTDException(e, "Falha ao tentar inserir : "+iDocument.getFieldValue(DocumentMTD.ID)));
			}
		}
	}
	
	/**
	 * Depois de fazer todas as inclusoes chame 
	 * o metodo de otimizacao.
	 * 
	 * Segundo pesquisa a onclusao em um indice ainda nao otimizado fica
	 * mais rapida. Assim primeiro inserimos tudo depois otimizamos. 
	 * 
	 * @throws CorruptIndexException
	 * @throws IOException
	 * @throws SolrServerException 
	 */
	public synchronized void otimizarIndice() throws SolrServerException, IOException {

		solrServer.optimize();
		
	}
	
	
	/**
	 * Fecha o repositorio liberando os recursos 
	 * de arquivo e memoria que ficaram abertos 
	 * assim como os streams que estejam associados
	 * a estes por conta do lucene.
	 * 
	 */
	public synchronized void fecharRepositorio(){
		solrServer.shutdown();
	}
}