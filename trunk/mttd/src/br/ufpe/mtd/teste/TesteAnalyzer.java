package br.ufpe.mtd.teste;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.solr.client.solrj.SolrServerException;

import br.ufpe.mtd.dados.indice.IRepositorioIndice;
import br.ufpe.mtd.util.MTDFactory;
import br.ufpe.mtd.util.MTDParametros;

public class TesteAnalyzer {

	
	public static void main(String[] args) throws IOException {
		
		try {
			inserirDocsParaAvaliacao();
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	  }
	
	
	/**
	 * Para mudar o diretorio de teste altere o endereco no arquivo
	 * mtd_properties
	 * @throws IOException
	 * @throws SolrServerException 
	 */
	static void inserirDocsParaAvaliacao() throws IOException, SolrServerException{
		File diretorio = MTDParametros.getExternalStorageDirectory();
		String indiceDir = MTDParametros.indiceDir();
		File pastaIndice = new File(diretorio, indiceDir);
		IRepositorioIndice rep = MTDFactory.getInstancia().getSingleRepositorioIndice();
		
		List<Document> listaDocs = new ArrayList<Document>();
		
		List<String> lista = new ArrayList<String>();
		lista.add("h2so4");
		lista.add("NHCL");
		lista.add("H2O");
		
//		(String titulo, String resumo,List<String> keywords, Date dataDefesa, String autor,
//				String programa, String orientador, String areaCNPQ, String id,String areaPrograma)
		
		
//		MTDDocument doc = new MTDDocumentBuilder().buildDocument("o analisador para h2so4","quimica e fisica", 
//				lista, new Date(), "Djalma", "QUIMICA", "Norte", "cnpq cnpq", "id1", "Exatas");
//		listaDocs.add(doc.toDocument());
//		
//		doc = new MTDDocumentBuilder().buildDocument("Sobre o Analisador de H2SO4","formula da �gua", lista, new Date(), 
//				"djalma", "FISICA", "B�sula", "cnpq cnpq", "id2", "Ci�ncia da Natureza");
//		listaDocs.add(doc.toDocument());
		
//		rep.inserirDocumento(listaDocs);
		
//		MTDFactory.getInstancia().newControleIndice().treinarRedeNeural();
		
	}
}
