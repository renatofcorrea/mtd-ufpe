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
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;

import br.ufpe.mtd.entidade.DocumentMTD;
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
		
		UpdateResponse response = solrServer.add(listaInserir);
		
		solrServer.commit();
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