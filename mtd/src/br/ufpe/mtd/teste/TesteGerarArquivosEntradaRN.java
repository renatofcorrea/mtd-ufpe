package br.ufpe.mtd.teste;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import br.ufpe.mtd.dados.IRepositorioIndice;
import br.ufpe.mtd.dados.MTDIterator;
import br.ufpe.mtd.entidade.DocumentMTD;
import br.ufpe.mtd.util.MTDFactory;
import br.ufpe.mtd.util.MTDParametros;

public class TesteGerarArquivosEntradaRN {

	static TreeMap<String,Integer> arvoreTermos;
	
	public static void main(String[] args) {
		try {
//			String urlString = "http://localhost:8080/solr/mtd";
//			SolrServer solr = new HttpSolrServer(urlString);
			
			File diretorio = MTDParametros.getExternalStorageDirectory();
			String indiceDir = MTDParametros.getMTDProperties().getProperty("indice_dir");
			
			File pastaTabelas = new File(diretorio, "tabelas");
			File pastaIndice = new File(diretorio, indiceDir);
			pastaTabelas.mkdirs();
			
			File tabelaPalavra = new File(pastaTabelas, "word_table.txt");
			File tabelaDocumento = new File(pastaTabelas, "doc_table.txt");
			File tabelaPalavraDocumento = new File(pastaTabelas, "word_doc_table.txt");
			
			System.out.println("Iniciando word table");
			TreeSet<String> arvoreTermos = carregarTermos(pastaIndice);
			gravarDadosArquivo(tabelaPalavra, getWordTable(arvoreTermos));
			System.out.println("Concluido word table");
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		
	}


	//gera o arquivo fazendo as chamadas ao repositorio solr
	private static void escreverTabelaDocumentos(PrintWriter writer) throws Exception{
		//---------------------------- Tabela de palavras --------------------------------
		
		IRepositorioIndice rep = MTDFactory.getInstancia().getSingleRepositorioIndice();
		
		MTDIterator<DocumentMTD> resultSet = rep.iterator();
		long contador = 0;
		while(resultSet.hasNext()){
			contador ++;
			DocumentMTD documento = resultSet.next();
				
			String id = documento.getId();
			System.out.println("Contador : "+contador +" "+id);
			writer.println(contador+";"+id+";" + documento.getAreaCNPQ() +";" + documento.getPrograma()+";" + documento.getAreaPrograma());  
		}
		
		writer.close();
	}
	
	//gera o arquivo fazendo as chamadas diretamente ao solr
	private static void escreverTabelaDocumentos(SolrServer solr, PrintWriter writer) throws SolrServerException{
		//---------------------------- Tabela de palavras --------------------------------
		
		SolrQuery parameters = new SolrQuery();
		parameters.set("q", DocumentMTD.ID+" :[0 TO *]");
		QueryResponse resposta = solr.query(parameters);
		parameters.addSort( DocumentMTD.ID, SolrQuery.ORDER.asc );
		
		SolrDocumentList list = resposta.getResults();
		long encontrados = list.getNumFound();
		long contador = 0;
		String id = "";
		
		while(contador < encontrados){
			
			Iterator<SolrDocument> it = list.iterator();
			
			while(it.hasNext()){
				contador++;
				SolrDocument documento = it.next();
				
				id = documento.getFieldValue(DocumentMTD.ID).toString();
				System.out.println(id);
				writer.println(contador+";"+id+";" + documento.getFieldValue(DocumentMTD.AREA_CNPQ) +";" + documento.getFieldValue(DocumentMTD.PROGRAMA)+";" + documento.getFieldValue(DocumentMTD.AREA_PROGRAMA));
			}
			
			
			parameters.set("q", DocumentMTD.ID+" :["+id+" TO *]");
			resposta = solr.query(parameters);
			list = resposta.getResults();
			list.remove(0);
		}
		
		writer.close();

	}
	
	public static TreeSet<String> carregarTermos(File pastaDoIndice) throws IOException{
		TreeSet<String> arvoreTermos = new TreeSet<String>();
		
		Directory directory = FSDirectory.open(pastaDoIndice);  	    
		DirectoryReader reader = DirectoryReader.open(directory);
		String[] campos = new String[]{DocumentMTD.TITULO, DocumentMTD.RESUMO, DocumentMTD.AREA_PROGRAMA};
		for(String campo : campos){
			
			Terms termos = MultiFields.getTerms(reader, campo);
			TermsEnum termsEnum = null;
			if(termos != null){
				
				termsEnum = termos.iterator(termsEnum);
				BytesRef bytesRef = termsEnum.next();
				
				while(bytesRef != null){
					
					String palavra = termsEnum.term().utf8ToString();
					arvoreTermos.add(palavra);
					bytesRef = termsEnum.next();
				}
			}
			
		}
		
		reader.close();
		
		return arvoreTermos;
	}
	public static TreeMap<String,Integer> carregarArvoreTermos(File pastaDoIndice) throws IOException{
		TreeMap<String,Integer> arvoreTermos = new TreeMap<String,Integer>();
		
		Directory directory = FSDirectory.open(pastaDoIndice);  	    
	    DirectoryReader reader = DirectoryReader.open(directory);
	    String[] campos = new String[]{DocumentMTD.TITULO, DocumentMTD.RESUMO, DocumentMTD.AREA_PROGRAMA};
	    for(String campo : campos){
	    	
		    Terms termos = MultiFields.getTerms(reader, campo);
		    TermsEnum termsEnum = null;
		    if(termos != null){
		    	
		    	termsEnum = termos.iterator(termsEnum);
		    	BytesRef bytesRef = termsEnum.next();
		    	
		    	while(bytesRef != null){
		    		
		    		String palavra = termsEnum.term().utf8ToString();
		    		Integer freq = arvoreTermos.get(termsEnum.term().utf8ToString());
		    		if(freq == null){
		    			freq = termsEnum.docFreq();
		    		}else{
		    			freq += termsEnum.docFreq();
		    			
		    		}
		    		arvoreTermos.put(palavra, freq);
		    		bytesRef = termsEnum.next();
		    	}
		    }
	    	
	    }
	    
	    reader.close();
		
		return arvoreTermos;
	}
	
	public static TreeMap<String,Long> freqMaxTermos(File pastaDoIndice) throws IOException{
		TreeMap<String,Long> arvoreTermos = new TreeMap<String,Long>();
		
		Directory directory = FSDirectory.open(pastaDoIndice);  	    
		DirectoryReader reader = DirectoryReader.open(directory);
		String[] campos = new String[]{DocumentMTD.TITULO, DocumentMTD.RESUMO, DocumentMTD.AREA_PROGRAMA};
		for(String campo : campos){
			
			Terms termos = MultiFields.getTerms(reader, campo);
			TermsEnum termsEnum = null;
			if(termos != null){
				
				termsEnum = termos.iterator(termsEnum);
				BytesRef bytesRef = termsEnum.next();
				
				while(bytesRef != null){
					String palavra = termsEnum.term().utf8ToString();
					Long freq = arvoreTermos.get(termsEnum.term().utf8ToString());
					if(freq == null){
						freq = termsEnum.totalTermFreq();
						arvoreTermos.put(palavra, freq);
					}
					bytesRef = termsEnum.next();
				}
			}
		}
		
		reader.close();
		
		return arvoreTermos;
	}
	
	public static String getWordTable(TreeSet<String> arvoreTermos) throws IOException{
	    int contador = 0;
		StringBuffer strBuffer = new StringBuffer();
	    for(String id: arvoreTermos){
	    	strBuffer.append((contador++)+" "+id+"\n");
	    }
	    
	    return strBuffer.toString();
	}
	
	public static void gravarDadosArquivo(File arquivo, String dado) throws IOException{
		FileOutputStream fos = new FileOutputStream(arquivo);
		fos.write(dado.getBytes());
		fos.close();
	}
}
