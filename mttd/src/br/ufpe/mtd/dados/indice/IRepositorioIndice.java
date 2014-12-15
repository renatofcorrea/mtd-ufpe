package br.ufpe.mtd.dados.indice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.solr.client.solrj.SolrServerException;

import br.ufpe.mtd.negocio.entidade.MTDDocument;
import br.ufpe.mtd.util.MTDIterator;

public interface IRepositorioIndice {

	public ArrayList<MTDDocument> consultar(String termo, String[] campos, int maxResultado) throws ParseException, CorruptIndexException, IOException, SolrServerException, Exception;

	public void inserirDocumento(List<Document> dosc) throws CorruptIndexException, IOException, SolrServerException, Exception;

	/**
	 * A otimizacao do indice e um processo que
	 * evoluiu de forma a ser realizado internamente
	 * e desaconselha-se fazer de forma manual
	 *  
	 * @throws CorruptIndexException
	 * @throws IOException
	 * @throws SolrServerException
	 * @throws Exception 
	 */
	@Deprecated
	public void otimizarIndice() throws CorruptIndexException, IOException, SolrServerException, Exception;
	
	public void fecharRepositorio();
	
	public MTDIterator<MTDDocument> iterator() throws Exception;
	
}
