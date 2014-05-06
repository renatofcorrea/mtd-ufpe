package br.ufpe.mtd.teste;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.misc.HighFreqTerms;
import org.apache.lucene.misc.TermStats;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.solr.client.solrj.SolrServerException;

import br.ufpe.mtd.dados.IRepositorioIndice;
import br.ufpe.mtd.dados.RepositorioIndiceLucene;
import br.ufpe.mtd.entidade.MTDDocumentBuilder;
import br.ufpe.mtd.entidade.MTDDocument;
import br.ufpe.mtd.entidade.EstatisticaPalavra;
import br.ufpe.mtd.negocio.ControleIndice;
import br.ufpe.mtd.util.MTDFactory;
import br.ufpe.mtd.util.MTDParametros;

public class TesteTreinarRedeNeural {

	public static void main(String[] args) {
		try {
			
			//teste1();
			
			//carregarStopWords();
			
			//inserirDocsParaAvaliacao();
			
			//gerarRNComRepostorio();
			
			treinarRedeNeuralComControleIndice();
			
		} catch (Exception e) {
			e.printStackTrace();	
		}
	}
	
	static void treinarRedeNeuralComControleIndice() throws IOException{
		ControleIndice controleIndice = MTDFactory.getInstancia().newControleIndice();
		controleIndice.treinarRedeNeural();
	}
	
	
	/**
	 * Para mudar o diretorio de teste altere o endereco no arquivo
	 * mtd_properties
	 * @throws IOException
	 * @throws SolrServerException 
	 */
	static void inserirDocsParaAvaliacao() throws IOException, SolrServerException{
		File diretorio = MTDParametros.getExternalStorageDirectory();
		String indiceDir = MTDParametros.getMTDProperties().getProperty("indice_dir");
		File pastaIndice = new File(diretorio, indiceDir);
		IRepositorioIndice rep = MTDFactory.getInstancia().getSingleRepositorioIndice();
		
		List<Document> listaDocs = new ArrayList<Document>();
		
		List<String> lista = new ArrayList<String>();
		lista.add("bla");
		lista.add("bla");
		lista.add("bla");
		
		
		MTDDocument doc = new MTDDocumentBuilder().buildDocument("computadores","gerador de lero lero", lista, new Date(),
				"MIT", "Programa de Tecnologia", "Dr. Fulano", "Instituto de Tecnologia", "id1", "Ciências Exatas");
		listaDocs.add(doc.toDocument());
		
		doc = new MTDDocumentBuilder().buildDocument("Psicologia do lero lero","Os motivos do bla bla bla", 
				lista, new Date(), "DCH", "programa de humanas", "Dr. Ciclano", "Instituto de Humanas", "id2", "Ciências Humanas");
		listaDocs.add(doc.toDocument());
		
		rep.inserirDocumento(listaDocs);
		
	}
	
	static void gerarRNComRepostorio() throws Exception{
		
		File diretorio = MTDParametros.getExternalStorageDirectory();
		String indiceDir = MTDParametros.getMTDProperties().getProperty("indice_dir");
		File pastaIndice = new File(diretorio, indiceDir);
		IRepositorioIndice rep = MTDFactory.getInstancia().getSingleRepositorioIndice();
		
		if(rep instanceof RepositorioIndiceLucene){
			String[] campos = new String[] {MTDDocument.TITULO, MTDDocument.RESUMO, MTDDocument.AREA_CNPQ};
			RepositorioIndiceLucene repLucene = (RepositorioIndiceLucene)rep;
			
			int frequenciaMax = ((RepositorioIndiceLucene) rep).getQuantidadeDocumentosNoIndice() * 80 /100; 
			
			List<EstatisticaPalavra> lista = repLucene.getListaPalavrasFiltrado(campos, 5000, 0, frequenciaMax);
			TreeMap<String, EstatisticaPalavra> mapa = repLucene.getMapaPalavraDocFreq(campos,lista);
			
			TreeSet<Integer> mapaDocId = new TreeSet<Integer>();
			File pastaTabelas = new File(pastaIndice.getParentFile(), "tabelas");
			pastaTabelas.mkdirs();
			
			File tabelaPalavra = new File(pastaTabelas, "word_table.txt");
			File tabelaDocumento = new File(pastaTabelas, "doc_table.txt");
			File tabelaPalavraDocumento = new File(pastaTabelas, "word_doc_table.txt");
			
			FileOutputStream fosPalavras = new FileOutputStream(tabelaPalavra);
			FileOutputStream fosDocs = new FileOutputStream(tabelaDocumento);
			FileOutputStream fosPalavraDoc = new FileOutputStream(tabelaPalavraDocumento);

			System.out.println("========= Iniciando geração de mapa palavra e mapa palavraDoc ==================");
		    
			int contador = 0;
			Iterator<String> iterator = mapa.keySet().iterator();
			while(iterator.hasNext()){
				String palavra = iterator.next();
				//========= escrever wordtable =======
		    	String aux = (++contador)+" "+palavra;
		    	if(iterator.hasNext()){
		    		aux += "\n";
		    	}
		    	fosPalavras.write(aux.getBytes());
		    	fosPalavras.flush();
		    	//========= escrever wordtable =======
		    	
		    	TreeMap<Integer, Integer> mapaDocFreq = mapa.get(palavra).getMapaDocFreq();
		    	Iterator<Integer> iteratorDocFreq = mapaDocFreq.keySet().iterator();
		    	
		    	while(iteratorDocFreq.hasNext()){
		    		Integer docId = iteratorDocFreq.next();
		    		mapaDocId.add(docId);
		    		//============= escrever wordDocTable =================
		    		aux = contador+" "+docId+" "+mapaDocFreq.get(docId);
		    		if(iterator.hasNext() | iteratorDocFreq.hasNext()){
		    			aux += "\n";
		    		}
		    		fosPalavraDoc.write(aux.getBytes());
		    		fosPalavraDoc.flush();
		    		//============= escrever wordDocTable =================
		    	}
		    }
		    
		    System.out.println("========= Fim geração de mapa palavra e mapa palavraDoc ==================");
		    
		    System.out.println("========= Iniciando geração de mapa docs ==================");
		    
		    List<MTDDocument> listaDocumentos = repLucene.getDocumentos(mapaDocId);
		    
		    for(int i = 0; i <listaDocumentos.size() ; i++){
		    	MTDDocument doc = listaDocumentos.get(i);
	    		//========= escrever docTable ===========
	    		String dadosDoc = doc.getDocId() +" "+doc.getId()+";"+doc.getAreaCNPQ()+";"+doc.getTitulo()+";"+doc.getAreaPrograma();
	    		if(i != listaDocumentos.size() - 1){
	    			dadosDoc+="\n";
	    		}
	    		fosDocs.write(dadosDoc.getBytes());
	    		fosDocs.flush();
	    		//========= escrever docTable ===========
		    }
		    
		    System.out.println("========= Fim geração de mapa docs ==================");

		    fosDocs.close();
		    fosPalavraDoc.close();
		    fosPalavras.close();
		}
	}
	
	static void gerarRNSemRepositorio() throws IOException{
		File diretorio = MTDParametros.getExternalStorageDirectory();
		String indiceDir = MTDParametros.getMTDProperties().getProperty("indice_dir");
		File pastaIndice = new File(diretorio, indiceDir);
		TreeMap<String, TreeMap<Integer, Integer>> mapaPorPalavra = getMapaPorPalavra(pastaIndice);
		gerarArquivosRN(mapaPorPalavra, pastaIndice);
	}
	
	static void teste1() throws Exception{
		
		File diretorio = MTDParametros.getExternalStorageDirectory();
		String indiceDir = MTDParametros.getMTDProperties().getProperty("indice_dir");
		File pastaIndice = new File(diretorio, indiceDir);
		
		//Directory indexDirectory = FSDirectory.open(pastaDoIndice);
		SimpleFSDirectory[] directory = new SimpleFSDirectory[1];
		DirectoryReader[] directoryReader = new DirectoryReader[1];

		directory[0] = new SimpleFSDirectory(pastaIndice);
		directoryReader[0] = DirectoryReader.open(directory[0]);

		MultiReader mr = new MultiReader(directoryReader);

		TermStats[] stats = null;
		stats = HighFreqTerms.getHighFreqTerms(mr, 100, MTDDocument.KEY_WORD, new HighFreqTerms.DocFreqComparator());

		for (TermStats termstat : stats) {
			System.out.println("Palavra: "
					+ termstat.termtext.utf8ToString() + ", docFrequency: "
					+ termstat.docFreq+" campo "+termstat.field);
		}
		
	}
	
	static ArrayList<int[]> getDocuments(DirectoryReader reader, String campo, String termo) throws IOException{
		ArrayList<int[]> lista = new ArrayList<int[]>();
	    DocsEnum de = MultiFields.getTermDocsEnum(reader, MultiFields.getLiveDocs(reader), campo, new BytesRef(termo));
	    if(de != null){
	    	int doc;
	    	while((doc = de.nextDoc()) != DocsEnum.NO_MORE_DOCS) {
	    		//System.out.println("Campo: "+campo+" Termo: "+termo+" Doc: "+de.docID()+" Freq: "+de.freq());
	    		lista.add(new int[]{de.docID(), de.freq()});
	    	}
	    }
	    
	    return lista;
	}
	
	static void gerarArquivosRN(TreeMap<String, TreeMap<Integer, Integer>> mapaPorPalavra, File pastaIndice) throws IOException{
		TreeSet<Integer> mapaDocId = new TreeSet<Integer>();
		File pastaTabelas = new File(pastaIndice.getParentFile(), "tabelas");
		pastaTabelas.mkdirs();
		
		File tabelaPalavra = new File(pastaTabelas, "word_table.txt");
		File tabelaDocumento = new File(pastaTabelas, "doc_table.txt");
		File tabelaPalavraDocumento = new File(pastaTabelas, "word_doc_table.txt");
		
		FileOutputStream fosPalavras = new FileOutputStream(tabelaPalavra);
		FileOutputStream fosDocs = new FileOutputStream(tabelaDocumento);
		FileOutputStream fosPalavraDoc = new FileOutputStream(tabelaPalavraDocumento);
		
		Directory directory = FSDirectory.open(pastaIndice);  	    
	    DirectoryReader reader = DirectoryReader.open(directory);
		IndexSearcher searcher = new IndexSearcher(reader);
	    String[] campos = new String[] {MTDDocument.AREA_CNPQ, MTDDocument.AREA_PROGRAMA, MTDDocument.TITULO};

		int contador = 0;
		System.out.println("========= Iniciando geração de mapa palavra e mapa palavraDoc ==================");
	    for(String palavra: mapaPorPalavra.keySet()){
	    	//========= escrever wordtable =======
	    	String aux = (++contador)+" "+palavra+"\n";
	    	fosPalavras.write(aux.getBytes());
	    	fosPalavras.flush();
	    	//========= escrever wordtable =======
	    	
	    	TreeMap<Integer, Integer> mapaDocFreq = mapaPorPalavra.get(palavra);
	    	for(Integer docId: mapaDocFreq.keySet()){
	    		mapaDocId.add(docId);
	    		//============= escrever wordDocTable =================
	    		aux = contador+" "+docId+" "+mapaDocFreq.get(docId)+"\n";
	    		fosPalavraDoc.write(aux.getBytes());
	    		fosPalavraDoc.flush();
	    		//============= escrever wordDocTable =================
	    	}
	    }
	    System.out.println("========= Fim geração de mapa palavra e mapa palavraDoc ==================");
	    
	    System.out.println("========= Iniciando geração de mapa docs ==================");
	    
	    for(int docId: mapaDocId){
    		Document documento = searcher.doc(docId);
    		//========= escrever docTable ===========
    		String dadosDoc = docId +" "+documento.get(MTDDocument.ID);
    		for(String campo : campos){
    			dadosDoc+= ";"+ documento.get(campo);
    		}
    		
    		dadosDoc+="\n";
    		fosDocs.write(dadosDoc.getBytes());
    		fosDocs.flush();
    		//========= escrever docTable ===========

	    }
	    System.out.println("========= Fim geração de mapa docs ==================");

	    fosDocs.close();
	    fosPalavraDoc.close();
	    fosPalavras.close();
	    directory.close();
	    reader.close();
	}
	
	static TreeMap<String, TreeMap<Integer, Integer>> getMapaPorPalavra(File pastaDoIndice) throws IOException{
		
		//mapa de palavras com mapa de doc freq
		TreeMap<String, TreeMap<Integer, Integer>> mapaPorPalavra = new TreeMap<String, TreeMap<Integer, Integer>>();
		Directory directory = FSDirectory.open(pastaDoIndice);  	    
	    DirectoryReader reader = DirectoryReader.open(directory);
	    String[] campos = new String[] {MTDDocument.TITULO, MTDDocument.RESUMO, MTDDocument.AREA_CNPQ};
	    
	    for(String campo : campos){
	    	
		    Terms termos = MultiFields.getTerms(reader, campo);
		    TermsEnum termsEnum = null;
		    if(termos != null){
		    	
		    	termsEnum = termos.iterator(termsEnum);
		    	BytesRef bytesRef = termsEnum.next();
		    	
		    	while(bytesRef != null){
		    		String palavra = termsEnum.term().utf8ToString();
		    		
		    		if(!mapaPorPalavra.containsKey(palavra)){
		    			mapaPorPalavra.put(palavra, new TreeMap<Integer, Integer>());
		    		}
		    		
		    		TreeMap<Integer, Integer> mapaDocFreq = mapaPorPalavra.get(palavra);
		    		ArrayList<int[]> lista = getDocuments(reader, campo, palavra);
		    		for (int[] is : lista) {
		    			int docId = is[0];
		    			if(mapaDocFreq.containsKey(docId)){
		    				int freq = mapaDocFreq.get(docId) + is[1];
		    				mapaDocFreq.put(docId, freq);
		    			}else{
		    				mapaDocFreq.put(docId, is[1]);
		    			}
					}
		    		
		    		bytesRef = termsEnum.next();
		    	}
		    }
	    }
	    
	    directory.close();
	    reader.close();
	    
	    return mapaPorPalavra;
	}
	
	
	
	static void teste2(File pastaDoIndice) throws IOException{
		System.out.println("Inicio teste 2");
		TreeMap<String,Long> arvoreTermos = new TreeMap<String,Long>();
		
		Directory directory = FSDirectory.open(pastaDoIndice);  	    
	    DirectoryReader reader = DirectoryReader.open(directory);
	    
	    for(String campo : MTDDocument.campos){
	    	
		    Terms termos = MultiFields.getTerms(reader, campo);
		    TermsEnum termsEnum = null;
		    if(termos != null){
		    	
		    	termsEnum = termos.iterator(termsEnum);
		    	BytesRef bytesRef = termsEnum.next();
		    	
		    	while(bytesRef != null){
		    		
		    		String palavra = termsEnum.term().utf8ToString();
		    		System.out.println(palavra+" freq "+termsEnum.docFreq()+"Total "+termsEnum.totalTermFreq());
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
	    
	    StringBuffer strBuffer = new StringBuffer();
	    TreeSet<String> docIds =  new TreeSet<String>() ;
	    docIds.addAll(arvoreTermos.keySet());
	    
	    
	    for(String id: docIds){
	    	strBuffer.append(id+" "+arvoreTermos.get(id)+"\n");
	    }
	    System.out.println(strBuffer);
	    System.out.println("Fim teste 2");
	}
	
	
	public static HashSet<String> carregarStopWords() throws IOException {
		File arquivo = MTDParametros.getLocalFile(MTDParametros.STOP_WORDS);
		HashSet<String> stopwords = new HashSet<String>();
    	FileInputStream fileInputStream = new FileInputStream(arquivo);
    	
    	BufferedReader buffer = new BufferedReader(new InputStreamReader(fileInputStream));

    	String palavra = buffer.readLine();
        while (palavra != null) {
            stopwords.add(palavra);
            palavra = buffer.readLine();
        }

        fileInputStream.close();
        buffer.close();
        
        
        for(String stopWord: stopwords){
        	System.out.println(stopWord);
        }
        
        return stopwords;
    }
}
