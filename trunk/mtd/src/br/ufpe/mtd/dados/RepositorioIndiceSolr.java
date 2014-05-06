package br.ufpe.mtd.dados;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;

import br.ufpe.mtd.entidade.MTDDocumentBuilder;
import br.ufpe.mtd.entidade.MTDDocument;
import br.ufpe.mtd.excecao.MTDException;
import br.ufpe.mtd.util.Log;
import br.ufpe.mtd.util.MTDFactory;

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
	public synchronized ArrayList<MTDDocument> consultar(String termo, String[] campos, int maxResultado)
			throws ParseException, CorruptIndexException, IOException, SolrServerException {
		
		ArrayList<MTDDocument> retorno = new ArrayList<MTDDocument>();

		SolrQuery parameters = new SolrQuery();
		parameters.set("q", "id:552199");
		QueryResponse resposta = solrServer.query(parameters);
		
		
		SolrDocumentList list = resposta.getResults();
		for (SolrDocument document : list) {
			
			
			MTDDocument docAtual = new MTDDocumentBuilder().buildDocument(document);
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
				strIds+= solrInputDocument.getFieldValue(MTDDocument.ID)+",";
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
				log.salvarDadosLog("Tentando inserir documento individualmente: "+iDocument.getFieldValue(MTDDocument.ID));
				solrServer.add(iDocument);
				solrServer.commit();
				
			} catch (Exception e) {
				log.salvarDadosLog(new MTDException(e, "Falha ao tentar inserir : "+iDocument.getFieldValue(MTDDocument.ID)));
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
	
	
	
	/**
	 * Retorna um mtdIterator que traz todos os documentos 
	 * existentes no indice do solr. os dados sao trazidos sob demanda
	 * durante a navegacao do iterator.
	 */
	public MTDIterator<MTDDocument> iterator() throws Exception{
		
		return new MTDIterator<MTDDocument>() {
			long contador;
			long encontrados;
			String id = "";
			SolrDocumentList list;
			SolrQuery parameters;
			
			@Override
			public void init() throws Exception {
				
				parameters = new SolrQuery();
				parameters.set("q", MTDDocument.ID+" :[0 TO *]");
				QueryResponse resposta = solrServer.query(parameters);
				parameters.addSort( MTDDocument.ID, SolrQuery.ORDER.asc );
				
				list = resposta.getResults();
				encontrados = list.getNumFound();
			}
			
			@Override
			public MTDDocument next() throws Exception {
				if(contador >= encontrados){
					throw new IndexOutOfBoundsException("Valor="+contador);
				}
				
				SolrDocument retorno = null;
				Iterator<SolrDocument> it = list.iterator();
				
				if(it.hasNext()){
					contador++;
					retorno = it.next();
					it.remove();
				}else{
					
					parameters.set("q", MTDDocument.ID+" :["+id+" TO *]");
					QueryResponse resposta = solrServer.query(parameters);
					list = resposta.getResults();
					
					System.out.println("==============================================");
					List<SolrDocument> listaRemover = new ArrayList<SolrDocument>();
					for(SolrDocument aux: list){
						if(aux.getFieldValue(MTDDocument.ID).equals(id)){
							listaRemover.add(aux);
						}
					}
					list.removeAll(listaRemover);
					
					it = list.iterator();
					if(it.hasNext()){
						contador++;
						retorno = it.next();
						it.remove();
					}
				}
				MTDDocument documento = null;
				if(retorno != null){
					documento = new MTDDocumentBuilder().buildDocument(retorno);
					id = documento.getId();
				}
				
				return documento;	
			}
			
			@Override
			public boolean hasNext() {
				return contador < encontrados;
			}
		};
	}
}