package br.ufpe.mtd.dados;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.ParseException;
import org.apache.solr.client.solrj.SolrServerException;

import br.ufpe.mtd.entidade.DocumentMTD;

public interface IRepositorioIndice {

	public ArrayList<DocumentMTD> consultar(String termo, int maxResultado) throws ParseException, CorruptIndexException, IOException, SolrServerException;

	public void inserirDocumento(List<Document> dosc) throws CorruptIndexException, IOException, SolrServerException;

	public void otimizarIndice() throws CorruptIndexException, IOException, SolrServerException;
	
	public void fecharRepositorio();
	
}
