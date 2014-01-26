package main;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.queryParser.ParseException;
import org.xml.sax.SAXException;

public class Principal {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		Fachada fachada;
		try {
			String caminhoIndice = "C:\\Documents and Settings\\Bruno\\Desktop\\indice MTD";
			fachada = new Fachada("http://www.bdtd.ufpe.br/tedeSimplificado/tde_oai/oai3.php", true, "bdtdstopwords.txt", caminhoIndice);
			fachada.setMetadataprefix("mtd2-br");
			String a = fachada.colherIdentificadores();
			System.out.println(a);
			a = fachada.colherMetadadosOnline();
			System.out.println(a);
			a = fachada.indexar();
			System.out.println(a);
			
//			String a = fachada.colherMetadadosCache("C:\\Users\\Bruno\\Desktop\\bdtd.xml");
//			fachada.colherMetadadosPorId(1);
			
			IndexReader iR = IndexReader.open(caminhoIndice);
			
			FileWriter docTable = new FileWriter("C:\\Documents and Settings\\Bruno\\Desktop\\MatrizesMTD\\docTable.txt", true);
			FileWriter wordTable = new FileWriter("C:\\Documents and Settings\\Bruno\\Desktop\\MatrizesMTD\\wordTable.txt", true);
			FileWriter wordDocTable = new FileWriter("C:\\Documents and Settings\\Bruno\\Desktop\\MatrizesMTD\\wordDocTable.txt", true);
			
						
			PrintWriter writer = new PrintWriter(docTable, true);  
		      
		    for (int i = 0; i < iR.numDocs(); i++) { 
		    	writer.println(i+";" + iR.document(i).get("areaCNPQ")+";" + iR.document(i).get("programa")+";" + iR.document(i).get("title"));  
		    }  		      
		    
		    writer.close();  
			
		    writer = new PrintWriter(wordTable, true); 
		    
		    PrintWriter writerWordDocTable = new PrintWriter(wordDocTable, true);
		    
			TermEnum enumTerm =  iR.terms();
									
			Set<String> termos = new TreeSet<String>();
			
			for(int i=0; enumTerm.next(); i++){									
				Term termoAtual = enumTerm.term();	
				
				if(!termos.add(termoAtual.text())){
					i--;
				}else{
					writer.println(i + " " +termoAtual.text());
				}
			}
			
			
			TermDocs termDoc = iR.termDocs();
			Iterator<String> iteratorTermos = termos.iterator();
			
			for(int i=0; iteratorTermos.hasNext(); i++){			
				HashMap<Integer, Integer> mapaDocFreq = new HashMap<Integer, Integer>();
				
				String termoAtual = iteratorTermos.next();
								
				Term termoResumo = new Term("resumo", termoAtual);
				
				termDoc.seek(termoResumo);
								
				while(termDoc.next()){
					int doc = termDoc.doc();
					int freq = termDoc.freq();
					
					if(mapaDocFreq.containsKey(doc)){
						int freqAtual = mapaDocFreq.get(doc);
						mapaDocFreq.remove(doc);
						freq = freqAtual + freq;
						mapaDocFreq.put(doc, freq);
					}else{
						mapaDocFreq.put(doc, freq);
					}
					
//					writerWordDocTable.println(i +" "+ doc +" "+ freq);
				}
				
				Term termoTitulo = new Term("title", termoAtual);
				
				termDoc.seek(termoTitulo);
				
				while(termDoc.next()){
					int doc = termDoc.doc();
					int freq = termDoc.freq();
					
					if(mapaDocFreq.containsKey(doc)){
						int freqAtual = mapaDocFreq.get(doc);
						mapaDocFreq.remove(doc);
						freq = freqAtual + freq;
						mapaDocFreq.put(doc, freq);
					}else{
						mapaDocFreq.put(doc, freq);
					}
					
//					writerWordDocTable.println(i +" "+ doc +" "+ freq);
				}
				
				Term termoKeyWord = new Term("keyword", termoAtual);
				
				termDoc.seek(termoKeyWord);

				while(termDoc.next()){
					int doc = termDoc.doc();
					int freq = termDoc.freq();
					
					if(mapaDocFreq.containsKey(doc)){
						int freqAtual = mapaDocFreq.get(doc);
						mapaDocFreq.remove(doc);
						freq = freqAtual + freq;
						mapaDocFreq.put(doc, freq);
					}else{
						mapaDocFreq.put(doc, freq);
					}
//					writerWordDocTable.println(i +" "+ doc +" "+ freq);
				}
				
				Iterator<Integer> iterator = mapaDocFreq.keySet().iterator();
				
				while(iterator.hasNext()){
					int doc = iterator.next();
					int freq = mapaDocFreq.get(doc);
					writerWordDocTable.println(i +" "+ doc +" "+ freq);
					System.out.println(i +" "+ doc +" "+ freq);
				}
								
			}
			
			writerWordDocTable.close();
			writer.close();
						
			fachada.consultar("Futebol", 50);
			
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		
	}

	

}
