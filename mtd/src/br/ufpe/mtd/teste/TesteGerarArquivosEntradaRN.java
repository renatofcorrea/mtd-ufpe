package br.ufpe.mtd.teste;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Iterator;

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

	
public static void main(String[] args) {
		

		try {
			
			String urlString = "http://localhost:8080/solr/mtd";
			SolrServer solr = new HttpSolrServer(urlString);
			
			File diretorio = MTDParametros.getExternalStorageDirectory();
			File pastaTabelas = new File(diretorio, "tabelas");
			pastaTabelas.mkdirs();
			
			File tabelaPalavra = new File(pastaTabelas, "word_table.txt");
			File tabelaDocumento = new File(pastaTabelas, "doc_table.txt");
			File tabelaPalavraDocumento = new File(pastaTabelas, "word_doc_table.txt");
			
			tabelaPalavra.createNewFile();
			tabelaDocumento.createNewFile();
			tabelaPalavraDocumento.createNewFile();
			
			FileWriter wordTable = new FileWriter(tabelaPalavra, false);
			FileWriter wordDocTable = new FileWriter(tabelaPalavraDocumento, false);
			FileWriter docTable = new FileWriter(tabelaDocumento, false);
			
			PrintWriter writer = new PrintWriter(docTable, true);
			
			//acesso direto
			//escreverTabelaDocumentos(solr, writer);
			
			//usando o repositorio solr
			escreverTabelaDocumentos(writer);
			
			
			
			
//		    writer = new PrintWriter(wordTable, true); 
//		    
//		    Ter
//		    
//			TermEnum enumTerm =  iR.terms();
//									
//			Set<String> termos = new TreeSet<String>();
//			
//			for(int i=0; enumTerm.next(); i++){									
//				Term termoAtual = enumTerm.term();	
//				
//				if(!termos.add(termoAtual.text())){
//					i--;
//				}else{
//					writer.println(i + " " +termoAtual.text());
//				}
//			}
			
//			
//		    PrintWriter writerWordDocTable = new PrintWriter(wordDocTable, true);
//			TermDocs termDoc = iR.termDocs();
//			Iterator<String> iteratorTermos = termos.iterator();
//			
//			for(int i=0; iteratorTermos.hasNext(); i++){			
//				HashMap<Integer, Integer> mapaDocFreq = new HashMap<Integer, Integer>();
//				
//				String termoAtual = iteratorTermos.next();
//								
//				Term termoResumo = new Term("resumo", termoAtual);
//				
//				termDoc.seek(termoResumo);
//								
//				while(termDoc.next()){
//					int doc = termDoc.doc();
//					int freq = termDoc.freq();
//					
//					if(mapaDocFreq.containsKey(doc)){
//						int freqAtual = mapaDocFreq.get(doc);
//						mapaDocFreq.remove(doc);
//						freq = freqAtual + freq;
//						mapaDocFreq.put(doc, freq);
//					}else{
//						mapaDocFreq.put(doc, freq);
//					}
//					
////					writerWordDocTable.println(i +" "+ doc +" "+ freq);
//				}
//				
//				Term termoTitulo = new Term("title", termoAtual);
//				
//				termDoc.seek(termoTitulo);
//				
//				while(termDoc.next()){
//					int doc = termDoc.doc();
//					int freq = termDoc.freq();
//					
//					if(mapaDocFreq.containsKey(doc)){
//						int freqAtual = mapaDocFreq.get(doc);
//						mapaDocFreq.remove(doc);
//						freq = freqAtual + freq;
//						mapaDocFreq.put(doc, freq);
//					}else{
//						mapaDocFreq.put(doc, freq);
//					}
//					
////					writerWordDocTable.println(i +" "+ doc +" "+ freq);
//				}
//				
//				Term termoKeyWord = new Term("keyword", termoAtual);
//				
//				termDoc.seek(termoKeyWord);
//
//				while(termDoc.next()){
//					int doc = termDoc.doc();
//					int freq = termDoc.freq();
//					
//					if(mapaDocFreq.containsKey(doc)){
//						int freqAtual = mapaDocFreq.get(doc);
//						mapaDocFreq.remove(doc);
//						freq = freqAtual + freq;
//						mapaDocFreq.put(doc, freq);
//					}else{
//						mapaDocFreq.put(doc, freq);
//					}
////					writerWordDocTable.println(i +" "+ doc +" "+ freq);
//				}
//				
//				Iterator<Integer> iterator = mapaDocFreq.keySet().iterator();
//				
//				while(iterator.hasNext()){
//					int doc = iterator.next();
//					int freq = mapaDocFreq.get(doc);
//					writerWordDocTable.println(i +" "+ doc +" "+ freq);
//					System.out.println(i +" "+ doc +" "+ freq);
//				}
//								
//			}
//			
//			writerWordDocTable.close();
//			writer.close();
//						
			
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
			System.out.println(id);
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

}
