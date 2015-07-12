package br.ufpe.mtd.dados.indice;



import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.misc.HighFreqTerms;
import org.apache.lucene.misc.TermStats;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.IOContext;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;

import br.ufpe.mtd.negocio.decodificacao.MTDAnalyzer;
import br.ufpe.mtd.negocio.entidade.EstatisticaPalavra;
import br.ufpe.mtd.negocio.entidade.MTDDocument;
import br.ufpe.mtd.negocio.entidade.MTDDocumentBuilder;
import br.ufpe.mtd.util.MTDIterator;
import br.ufpe.mtd.util.enumerado.MTDArquivoEnum;

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
public class RepositorioIndiceLucene implements IRepositorioIndice{

	private Version version;
	private Set<String> stopWords;
	private RAMDirectory diretorioEmMemoria;
	private Directory diretorioEmDisco;
	private TreeSet<String> dicionario;
	
	private File pastaDoIndice;
	
	public RepositorioIndiceLucene(File diretorioIndice, Version version) throws Exception {
		this.version = version;
		this.pastaDoIndice = diretorioIndice;
		if(!existeIndice()){
			criarIndice();
		}
	}

	/*
	  Cria o analyzador
	 */
	private synchronized Analyzer getAnalizerPadrao() throws Exception{
		CharArraySet set = new CharArraySet(getStopWords(), false);
//		return new ArabicAnalyzer(set);
		return new MTDAnalyzer(set);
	}
	
	/*
	 * Representa o diretorio dentro do lucene
	 */
	private synchronized Directory getDirectoryEmDisco() throws IOException{
		if(diretorioEmDisco == null){
			diretorioEmDisco = FSDirectory.open(pastaDoIndice);
		}
		
		return diretorioEmDisco;
	}
	
	/*
	 * Representa uma copia em memoria RAM do diretorio 
	 * do indice.
	 * 
	 * Deve ser analizado se o diretorio crescer demais 
	 * para o servidor se esta opcao deve continuar.
	 * 
	 * Para mudar basta trocar a chamada deste metodo ao
	 * getDiretorioEmDisco.
	 * 
	 */
	private synchronized RAMDirectory getCopiaDiretorioMemoria() throws IOException{
		if(diretorioEmMemoria == null){
			diretorioEmMemoria = new RAMDirectory(getDirectoryEmDisco(), IOContext.DEFAULT);
		}
		return diretorioEmMemoria;
	}
	
	/*
	 * 
	 */
	private synchronized IndexWriter getWriterPadrao(boolean criar) throws Exception{
		IndexWriterConfig config = new IndexWriterConfig(version, getAnalizerPadrao());
		IndexWriter indexWriter = new IndexWriter(getDirectoryEmDisco(), config);
		
		return indexWriter;
	}
	
	/*
	 * 10.000.000 de registros lidos 
	 * - do disco levaram 221 segundos com multiplas instancias de Directory 
	 * - do disco levaram 207 segundos com unica instancia de Directory 
	 * - da memoria levaram 237 segundos com multiplas instancia de RamDirectory
	 * - da memoria levaram 160 segundos com unica instancia de RamDirectory
	 * 
	 * 22/11/2013 UFPE CAC 16:51
	 * Windows 7 Enterprise
	 * Service Pack 1
	 * Processador Intel(R) Dual CPU T3400 @2.16GHz 2.17GHz
	 * Memória RAM 2GB
	 * Sistema Operacional 32 Bits
	 * 
	 */
	public synchronized ArrayList<MTDDocument> consultar(String termo, String[] campos, int maxResultado)
			throws Exception {
		
		ScoreDoc[] hits = getHits(termo, campos, maxResultado);
		ArrayList<MTDDocument> listaRetorno = parseHitsToDocumentArray(hits); 
		
		return listaRetorno;
	}
	
	/**
	 * Transforma dados recuperados a partir de uma consulta em 
	 * Objetos do tipo MTDDocument
	 * 
	 * @param hits
	 * @return
	 * @throws Exception 
	 */
	public synchronized ArrayList<MTDDocument> parseHitsToDocumentArray(ScoreDoc[] hits) throws Exception{
		ArrayList<MTDDocument> retorno = new ArrayList<MTDDocument>();
		
		Directory indexDirectory = getCopiaDiretorioMemoria();		
		Analyzer analisador = getAnalizerPadrao();
		IndexReader reader = DirectoryReader.open(indexDirectory);
		// Cria o acesso ao indice
		IndexSearcher searcher = new IndexSearcher(reader);

		for (int i = 0; i < hits.length; ++i) {
			int docId = hits[i].doc;
			Document d = searcher.doc(docId);

			MTDDocument docAtual = new MTDDocumentBuilder().buildDocument(d).build();
			docAtual.setDocId(docId);
			retorno.add(docAtual);
		}
		
		reader.close();
		analisador.close();

		return retorno;
	}

	/**
	 * Cria o indice do lucene no local
	 * indicado na construcao do
	 * Repositorio. Depois libera os recursos
	 * abertos.
	 * @throws Exception 
	 */
	private synchronized void criarIndice() throws Exception {
		IndexWriter indexWriter = getWriterPadrao(true);
		try {
			indexWriter.getAnalyzer().close();
			indexWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Verifica se existe o indice do lucene no diretorio
	 * setado no reposotorio.
	 * 
	 * @return
	 */
	private synchronized boolean existeIndice() {
		boolean indiceExiste = new File(pastaDoIndice, "segments.gen")
				.exists();

		return indiceExiste;
	}

	/**
	 * Permite adicionar uma colecao de documentos
	 * deve ser usado preferencialmente mesmo que em alguns
	 * casos seja passado apenas 1 doc.
	 * 
	 * @param dosc
	 * @throws Exception 
	 */
	public synchronized void inserirDocumento(List<Document> dosc) throws Exception {
		IndexWriter indexWriter = getWriterPadrao(false);
		for (Document document : dosc) {
			indexWriter.addDocument(document);
			
		}
		indexWriter.close();
	}
	
	/*
	 * A atualização de documentos siginifica deletar e depois salvar o doc.
	 * Alem de não estar funcionando a deleção em primeiro instante, por algum problema
	 * a ser analisado, consegui realiza a deleçaõ com o Luke e vi que o indice fica com o 
	 * registro da delecao.
	 * Entao para o processo de geracao de sintagmas nominais nao parece ser o mais adequado.
	 * O esforco de reescrever todos dados seria melhor gerar um nov indice com os sintagmas.
	 * 
	 * @param identificador
	 * @param doc
	 * @throws CorruptIndexException
	 * @throws IOException
	 */
	public synchronized void atualizarDocumento(Term identificador, Document doc) throws Exception {
		IndexWriter indexWriter = getWriterPadrao(false);
		
//		indexWriter.deleteDocuments(identificador);
//		indexWriter.commit();
//		indexWriter.addDocument(doc);
//		indexWriter.commit();
		
		indexWriter.updateDocument(identificador, doc);
		indexWriter.close();
	}
	
	/**
	 * Depois de fazer todas as inclusoes chame 
	 * o metodo de otimizacao.
	 * 
	 * Segundo pesquisa a onclusao em um indice ainda nao otimizado fica
	 * mais rapida. Assim primeiro inserimos tudo depois otimizamos. 
	 * @throws Exception 
	 */
	@Deprecated
	public synchronized void otimizarIndice() throws Exception {
		IndexWriter indexWriter = getWriterPadrao(false);
		indexWriter.forceMerge(50);
		indexWriter.close();
	}
	
	
	/**
	 * Fecha o repositorio liberando os recursos 
	 * de arquivo e memoria que ficaram abertos 
	 * assim como os streams que estejam associados
	 * a estes por conta do lucene.
	 * 
	 */
	public synchronized void fecharRepositorio(){
		try {
			
			if( diretorioEmMemoria != null){
				diretorioEmMemoria.close();
				diretorioEmMemoria = null;
			}
			
			if( diretorioEmDisco != null){
				diretorioEmDisco.close();
				diretorioEmDisco = null;
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	/**
	 * Retorna uma mapa contendo todos os termos do indice (Cada termos é chave no mapa)
	 * e cada valor é um objeto do tipo EstatisticaPalavra
	 * 
	 * Recebe como filtro as palavras consideradas relevantes 

	 * @param campos
	 * @param filtro
	 * @return
	 * @throws IOException
	 */
	public synchronized TreeMap<String, EstatisticaPalavra> getMapaPalavraDocFreq(String[] campos, List<EstatisticaPalavra> filtro) throws IOException{
		
		//mapa de palavras com mapa de doc freq
		TreeMap<String, EstatisticaPalavra> mapaPalavraEstatistica = new TreeMap<String, EstatisticaPalavra>();
	    DirectoryReader reader = DirectoryReader.open(getCopiaDiretorioMemoria());
	    
	    
	    for(String campo : campos){
	    	
		    Terms termos = MultiFields.getTerms(reader, campo);
		    TermsEnum termsEnum = null;
		    if(termos != null){
		    	
		    	termsEnum = termos.iterator(termsEnum);
		    	BytesRef bytesRef = termsEnum.next();
		    	
		    	while(bytesRef != null){
		    		String palavra = termsEnum.term().utf8ToString();
		    		EstatisticaPalavra estatPalavraAux = new EstatisticaPalavra(palavra);
		    		
		    		//filtrar apenas as palavras que foram consideradas relevantes passadas no filtro
		    		if(filtro.contains(estatPalavraAux)){
		    			
		    			if(!mapaPalavraEstatistica.containsKey(palavra)){
		    				int indicePalavra = filtro.indexOf(estatPalavraAux);
		    				mapaPalavraEstatistica.put(palavra, filtro.get(indicePalavra));
		    			}
		    			
		    			EstatisticaPalavra estatisticaPalavra = mapaPalavraEstatistica.get(palavra);
		    			ArrayList<int[]> lista = getListaDocFreq(reader, campo, palavra);
		    			estatisticaPalavra.atualizarMapa(lista);
		    			estatisticaPalavra.gerarEstatistica();
		    		}
		    		
		    		bytesRef = termsEnum.next();
		    	}
		    }
	    }
	    
	    reader.close();
	    
	    return mapaPalavraEstatistica;
	}
	
	public synchronized int getQuantidadeDocumentosNoIndice() throws IOException{
		DirectoryReader reader = DirectoryReader.open(getCopiaDiretorioMemoria());
		int qtdDocs = reader.numDocs();
	    reader.close();
	    return qtdDocs;
	}
	
	/**
	 * Recupera todos os identificadores guardados no indice.
	 * Cada identificador é um dado que identificam o documento na 
	 * bibliiotecade origem
	 * Ex: oai:repositorio.ufpe.br:123456789/7985
	 * 
	 * @throws Exception 
	 */
	public synchronized List<String> getIdsTodosDocumentos() throws Exception{
		List<String> lista = new ArrayList<String>();
		MTDIterator<MTDDocument> iterator = iterator();
		while(iterator.hasNext()){
			MTDDocument doc = iterator.next();
			lista.add(doc.getId());
		}
		iterator.close();
		
		return lista;
	}
	
	/**
	 * Lista todos os ids gerados pelo Proprio Indice no salvamento de cada
	 * registro.
	 * @return
	 * @throws Exception
	 */
	public synchronized List<Integer> getDocIdsTodosDocumentos() throws Exception{
		List<Integer> lista = new ArrayList<Integer>();
		
		MTDIterator<MTDDocument> iterator = iterator();
		while(iterator.hasNext()){
			MTDDocument doc = iterator.next();
			lista.add(doc.getDocId());
		}
		iterator.close();
		
		return lista;
	}
	
	/**
	 * Recupera lista contendo todos os documentos e frequencia 
	 * que contenham em determinado campo ocorrencia de determinado termo.
	 * 
	 * Cada linha da lista contem um array de duas posicoes de acordo com descricao abaixo 
	 *  
	 * [0] = docID
	 * [1] = freq
	 * 
	 * @param reader
	 * @param campo
	 * @param termo
	 * @return
	 * @throws IOException
	 */
	private synchronized ArrayList<int[]> getListaDocFreq(DirectoryReader reader, String campo, String termo) throws IOException{
		ArrayList<int[]> lista = new ArrayList<int[]>();
	    DocsEnum docsEnum = MultiFields.getTermDocsEnum(reader, MultiFields.getLiveDocs(reader), campo, new BytesRef(termo));
	    if(docsEnum != null){
	    	while(docsEnum.nextDoc() != DocsEnum.NO_MORE_DOCS) {
	    		lista.add(new int[]{docsEnum.docID(), docsEnum.freq()});
	    	}
	    }
	    return lista;
	}
	
	/**
	 * Recebe uma lista de Ids e realiza a busca no indice pelos documentos
	 * correspondentes.
	 * @param ids
	 * @return
	 * @throws IOException
	 */
	public synchronized List<MTDDocument> getDocumentos(Collection<Integer> ids){
		ArrayList<MTDDocument> listaRetorno = new ArrayList<MTDDocument>();
		DirectoryReader reader;
		try {
			reader = DirectoryReader.open(getCopiaDiretorioMemoria());
		IndexSearcher searcher = new IndexSearcher(reader);		
	    for(Integer docId: ids){
    		Document documento = searcher.doc(docId.intValue());
    		if(documento != null){
    		MTDDocument doc = new MTDDocumentBuilder().buildDocument(documento).build();
    		doc.setDocId(docId.intValue());
    		listaRetorno.add(doc);
    		}else
    		{System.out.println("Document with docId "+docId.intValue()+" not found.");}
	    }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return listaRetorno;
	}
	
	/**
	 * Pegar a frequencia para cada campo
	 * 
	 * Colocar um filtro para tirar as palavras que aaprecem pouco e as que aparecem demais.
	 * 
	 * O Ranking ja tira as palavras que aparecem pouco porem faz para um unico campo.
	 * 
	 * Juntar todas as palavras que aparecem em todos os campo , colocar numa lista so então aplicar o criterio de eliminacao.
	 * 
	 * DocFreq - Em quantos documentos a palavra apareceu 
	 * TotalTermFreq - Quantidade de ococrrencia da palavra nos documentos.
	 * 
	 * A acumulacao de doc freq não leva em conta as interesecções de docFreq entre campos distintos.
	 * 
	 * @param campos
	 * @throws Exception
	 */
	public synchronized List<EstatisticaPalavra> getListaPalavrasFiltrado(String[] campos, int maxPalavrasPorCampo, long minDocFreq, long maxDocFreq) throws Exception{
		
		EstatisticaPalavra.setTamCorpus(getQuantidadeDocumentosNoIndice());
		
		List<EstatisticaPalavra> listaRetorno = new ArrayList<EstatisticaPalavra>();
		
		TreeMap<String,long[]> conjuntoPalavras = new TreeMap<String,long[]>();
		
		IndexReader reader = DirectoryReader.open(getCopiaDiretorioMemoria());
		MultiReader mr = new MultiReader(reader);
		
		//guarda todas as palavras em um mapa e soma os valores de docfreq e total freq encontrado por campo
		for(String campo: campos){
			TermStats[] stats = HighFreqTerms.getHighFreqTerms(mr, maxPalavrasPorCampo, campo, new HighFreqTerms.DocFreqComparator());
			
			for (TermStats termstat : stats) {
				String palavra = termstat.termtext.utf8ToString();
				
				if(!conjuntoPalavras.containsKey(palavra)){
					
					int docFreq = getHits(palavra, campos, EstatisticaPalavra.getTamCorpus()).length;
					conjuntoPalavras.put(palavra, new long[]{docFreq,termstat.totalTermFreq});
					
				}else{
					
					long[] freqs = conjuntoPalavras.get(palavra);
					freqs[1] += termstat.totalTermFreq;
				}
			}
		}		
		
		//adiciona na lista de retorno apenas as palavra que atendem ao criterio de filtro
		for(String palavra: conjuntoPalavras.keySet()){
			long docFreq = conjuntoPalavras.get(palavra)[0];
			long totalFreq = conjuntoPalavras.get(palavra)[1];
			if(docFreq > minDocFreq && totalFreq < maxDocFreq){
				EstatisticaPalavra estatisticaPalavra = new EstatisticaPalavra(palavra);
				estatisticaPalavra.setDocFreq((int)docFreq);
				estatisticaPalavra.setTotalDocFreq(totalFreq);
				
				listaRetorno.add(estatisticaPalavra);
			}
		}
		
		
		return listaRetorno;
	}
	
	/**
	 * Realiza a busca no indice, de docuemntos que possuam determinado termo,
	 * nos campos especificados. A quantidade maxima de documentos desejada deve ser informada.
	 * 
	 * @param termo
	 * @param campos
	 * @param maxResultado
	 * @return
	 * @throws Exception 
	 */
	private synchronized ScoreDoc[] getHits(String termo, String[] campos, int maxResultado) throws Exception{
		
		Directory indexDirectory = getCopiaDiretorioMemoria();		
		IndexReader reader = DirectoryReader.open(indexDirectory);
		Analyzer analisador = getAnalizerPadrao();
		// Cria o acesso ao indice
		IndexSearcher searcher = new IndexSearcher(reader);
		
		MultiFieldQueryParser mfqp = new MultiFieldQueryParser(campos, analisador);
		mfqp.setPhraseSlop(2);
		Query q = mfqp.parse(termo);

		// Prepara a colecao de resultado
		TopScoreDocCollector collector = TopScoreDocCollector.create(maxResultado, true);
		
		// Faz a pesquisa
		searcher.search(q, collector);
		// Separa os itens mais relevantes para a consulta.
		
		ScoreDoc[] hits = collector.topDocs().scoreDocs;
		
		reader.close();
		analisador.close();
		
		return hits;
	}
	
	/**
	 * 
	 * E uma lista de palavra que esta salva em um arquivo 
	 * e sera usada para filtrar as palavras sem relevancia
	 * para uma busca ou indexacao.
	 * 
	 * @return
	 * @throws Exception 
	 * 
	 */
	public synchronized Set<String> getStopWords() throws Exception {
		
		if(stopWords == null){
			MTDArquivoEnum enumerado = MTDArquivoEnum.STOP_WORDS;
			stopWords = new HashSet<String>();
			MTDIterator<String> it = enumerado.lineIterator();
			while (it.hasNext()) {
				stopWords.add(it.next());
			}
			it.close();
		}
        
        return stopWords;
    }
	
	public synchronized TreeSet<String> carregarDicionario(String[] campos) throws IOException {
		
		if(dicionario == null){
			Directory indexDirectory = getCopiaDiretorioMemoria();		
			IndexReader reader = DirectoryReader.open(indexDirectory);
			dicionario = new TreeSet<String>();
			for (String campo : campos) {
				
				Terms termos = MultiFields.getTerms(reader, campo);
				TermsEnum termsEnum = null;
				if (termos != null) {
					
					termsEnum = termos.iterator(termsEnum);
					BytesRef bytesRef = termsEnum.next();
					
					while (bytesRef != null) {
						String palavra = termsEnum.term().utf8ToString();
						bytesRef = termsEnum.next();
						dicionario.add(palavra);
					}
				}
			}
			
			reader.close();
			
		}
		return dicionario;
	}
	
	@Override
	public MTDIterator<MTDDocument> iterator() throws Exception {
		
		return new MTDIterator<MTDDocument>() {			
			DirectoryReader reader;
			int indice;
			
			@Override
			public MTDDocument next() throws Exception {
				
				if(hasNext()){
					Document doc = reader.document(indice);
					indice ++;
					return new MTDDocumentBuilder().buildDocument(doc).build();
				}

				return null;
			}
			
			@Override
			public void init() throws Exception {
				reader = DirectoryReader.open(getCopiaDiretorioMemoria());
				indice = 0;
			}
			
			@Override
			public boolean hasNext() throws Exception {
				
				return indice < reader.maxDoc();
			}
			
			@Override
			public void close(){
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
	}
}