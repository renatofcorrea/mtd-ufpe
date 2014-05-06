package br.ufpe.mtd.dados;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.solr.client.solrj.SolrServerException;

import br.ufpe.mtd.entidade.MTDDocument;

public interface IRepositorioIndice {

	public ArrayList<MTDDocument> consultar(String termo, String[] campos, int maxResultado) throws ParseException, CorruptIndexException, IOException, SolrServerException;

	public void inserirDocumento(List<Document> dosc) throws CorruptIndexException, IOException, SolrServerException;

	/**
	 * A otimizacao do indice e um processo que
	 * evoluiu de forma a ser realizado internamente
	 * e desaconselha-se fazer de forma manual
	 *  
	 * @throws CorruptIndexException
	 * @throws IOException
	 * @throws SolrServerException
	 */
	@Deprecated
	public void otimizarIndice() throws CorruptIndexException, IOException, SolrServerException;
	
	public void fecharRepositorio();
	
	public MTDIterator<MTDDocument> iterator() throws Exception;
	
}
