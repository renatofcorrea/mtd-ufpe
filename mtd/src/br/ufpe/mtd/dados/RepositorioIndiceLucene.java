package br.ufpe.mtd.dados;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
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
import org.apache.lucene.util.Version;

import br.ufpe.mtd.entidade.BuilderDocumentMTD;
import br.ufpe.mtd.entidade.DocumentMTD;

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

	private RAMDirectory diretorioEmMemoria;
	private Directory diretorioEmDisco;
	
	private File pastaDoIndice;
	
	public RepositorioIndiceLucene(File diretorioIndice) throws IOException {
		this.pastaDoIndice = diretorioIndice;
		if(!existeIndice()){
			criarIndice();
		}
	}

	/*
	  Cria o analyzador
	  TODO: deve pegar o analizador de acordo com a lingua
	  considerar o uso do BrazilianAnalizer
	 */
	private StandardAnalyzer getAnalizerPadrao(){
		Version matchVersion = Version.LUCENE_46;
		return new StandardAnalyzer(matchVersion);
	}
	
	/*
	 * Representa o diretorio dentro do lucene
	 */
	private Directory getDirectoryEmDisco() throws IOException{
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
	private RAMDirectory getCopiaDiretorioMemoria() throws IOException{
		if(diretorioEmMemoria == null){
			diretorioEmMemoria = new RAMDirectory(getDirectoryEmDisco(), IOContext.DEFAULT);
		}
		return diretorioEmMemoria;
	}
	
	/*
	 * 
	 */
	private IndexWriter getWriterPadrao(boolean criar) throws CorruptIndexException, LockObtainFailedException, IOException{
		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_46, getAnalizerPadrao());
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
	public synchronized ArrayList<DocumentMTD> consultar(String termo, int maxResultado)
			throws CorruptIndexException, IOException, ParseException {

		Directory indexDirectory = getCopiaDiretorioMemoria();		
		StandardAnalyzer analisador = getAnalizerPadrao();
		IndexReader reader = DirectoryReader.open(indexDirectory);
		// Cria o acesso ao indice
		IndexSearcher searcher = new IndexSearcher(reader);
		
		MultiFieldQueryParser mfqp = new MultiFieldQueryParser(Version.LUCENE_46, DocumentMTD.campos, analisador);
		mfqp.setPhraseSlop(2);
		Query q = mfqp.parse(termo);

		// Prepara a colecao de resultado
		TopScoreDocCollector collector = TopScoreDocCollector.create(maxResultado, true);
		
		// Faz a pesquisa
		searcher.search(q, collector);
		// Separa os itens mais relevantes para a consulta.
		
		ScoreDoc[] hits = collector.topDocs().scoreDocs;
		ArrayList<DocumentMTD> retorno = new ArrayList<DocumentMTD>();
		
		// TODO
		// HashMap<String, Integer> documentoNodo = LeitorMapa.getMapa().getDocumentoNodo();

		for (int i = 0; i < hits.length; ++i) {
			int docId = hits[i].doc;
			Document d = searcher.doc(docId);

			DocumentMTD docAtual = new BuilderDocumentMTD().buildDocument(d);//TODO: colocar os dados que nao estao no construtor atraves dos sets
//			// docAtual.setNodo(documentoNodo.get(docAtual.getId()+""));
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
	 * @throws IOException
	 */
	private synchronized void criarIndice() throws IOException {
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
	 * @throws CorruptIndexException
	 * @throws IOException
	 */
	public synchronized void inserirDocumento(List<Document> dosc) throws CorruptIndexException, IOException {
		IndexWriter indexWriter = getWriterPadrao(false);
		for (Document document : dosc) {
			indexWriter.addDocument(document);
			
		}
		indexWriter.close();
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
	 */
	@Deprecated
	public synchronized void otimizarIndice() throws CorruptIndexException, IOException {
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
			}
			
			if( diretorioEmDisco != null){
				diretorioEmDisco.close();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public MTDIterator<DocumentMTD> iterator() throws Exception {
		
		return new MTDIterator<DocumentMTD>() {
			
			@Override
			public DocumentMTD next() throws Exception {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public void init() throws Exception {
				
				
			}
			
			@Override
			public boolean hasNext() throws Exception {
				// TODO Auto-generated method stub
				return false;
			}
		};
	}
}