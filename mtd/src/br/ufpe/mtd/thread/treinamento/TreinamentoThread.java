package br.ufpe.mtd.thread.treinamento;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.TreeMap;
import java.util.TreeSet;

import at.tuwien.ifs.somtoolbox.models.GrowingSOM;
import at.tuwien.ifs.somtoolbox.util.StringUtils;
import br.ufpe.mtd.dados.IRepositorioIndice;
import br.ufpe.mtd.dados.RepositorioIndiceLucene;
import br.ufpe.mtd.entidade.EstatisticaPalavra;
import br.ufpe.mtd.entidade.MTDDocument;
import br.ufpe.mtd.enumerado.AreaCNPQEnum;
import br.ufpe.mtd.enumerado.MTDArquivoEnum;
import br.ufpe.mtd.thread.BaseThread;
import br.ufpe.mtd.util.MTDFactory;

public class TreinamentoThread extends BaseThread{
	
	private final Integer NUM_CICLOS = 30;
	
	@Override
	public void run() {
		super.run();
		try {
			long inicio = System.currentTimeMillis();
			System.out.println(" ---- iniciando treinamento da rede neural-----");
			
			System.out.println("Processando termos...");
			TreeMap<String, EstatisticaPalavra> mapaEstatisticaPalavra = getMapaEstatisticaPalavra();
			
			System.out.println("Recuperando documentos...");
			List<MTDDocument> listaDocumentos = getListaDocumentos(mapaEstatisticaPalavra);
			
			gerarArquivosEntradaRN(listaDocumentos,mapaEstatisticaPalavra);
		    
			realizarTreinamento(mapaEstatisticaPalavra, listaDocumentos);
			
			System.out.println(" ---- fim do treinamento da rede neural-----");
			
			System.out.println("Tempo "+ (System.currentTimeMillis() - inicio));
		} catch (Exception e) {
			MTDFactory.getInstancia().getLog().salvarDadosLog(e);
		} 
	}
	
	private TreeMap<String, EstatisticaPalavra> getMapaEstatisticaPalavra() throws Exception{
		
		MTDFactory mtdFabrica = MTDFactory.getInstancia();
		IRepositorioIndice rep = mtdFabrica.getSingleRepositorioIndice();
		
		String[] campos = new String[] {MTDDocument.TITULO, MTDDocument.RESUMO, MTDDocument.AREA_CNPQ, MTDDocument.KEY_WORD};
		RepositorioIndiceLucene repLucene = (RepositorioIndiceLucene)rep;
					
		int docFreqMax = (int)Math.round((repLucene.getQuantidadeDocumentosNoIndice() * 80.0) / 100.0); //80%
		int docFreqMin = 5;
		int numMaxDoc = 10000;
		
		List<EstatisticaPalavra> filtroPalavrasRelevantes = repLucene.getListaPalavrasFiltrado(campos, numMaxDoc, docFreqMin, docFreqMax);
		TreeMap<String, EstatisticaPalavra> mapaEstatisticaPalavra = repLucene.getMapaPalavraDocFreq(campos, filtroPalavrasRelevantes);
		
		return mapaEstatisticaPalavra;
	}
	
	private List<MTDDocument> getListaDocumentos(TreeMap<String, EstatisticaPalavra> mapaEstatisticaPalavra) throws IOException{
		TreeSet<Integer> mapaDocId = getDocIdSet(mapaEstatisticaPalavra);
		MTDFactory mtdFabrica = MTDFactory.getInstancia();
		IRepositorioIndice rep = mtdFabrica.getSingleRepositorioIndice();
		return ((RepositorioIndiceLucene)rep).getDocumentos(mapaDocId);
	}
	
	@Override
	public void executarNoPool() {
		MTDFactory.getInstancia().getTreinamentoPoolThread().executar(this);
	}
	
	/**
	 * Gera os arquivos de entrada para o treinamento da rede neural
	 * do MTD.
	 * 3 arquivos sao gerados
	 * word_table - que contem uma tabela textual com as informacoes das palvras no formato (id_palavra palavra) sendo espaço o separador das colunas.
	 * doc_table - que contem uma tabela textual com as informacoes das dos documentos no formato (id_documento doc_identifier;area_cnpq;titulo;area_programa) sendo espaço o separador das colunas.
	 * word_doc_table - que contem uma tabela textual com as informacoes das palvras-documentos-frequencia da palavra no doc,  no formato (idpalavra id_documento frequencia) sendo espaço o separador das colunas.
	 * @throws Exception 
	 */
	public void gerarArquivosEntradaRN(List<MTDDocument> listaDocumentos, TreeMap<String, EstatisticaPalavra> mapaEstatisticaPalavra) throws Exception{
		gerarMapaPalavraEPalavraDoc(mapaEstatisticaPalavra);
		gerarMapaDocumentos(listaDocumentos);
	}
	
	/**
	 * Gera os arquivos especificos para entrada da rede SOM
	 * e chama o GrowingSom que realizara o treinamento da rede propriamente dito
	 *  
	 * @param mapaEstatisticaPalavra
	 * @param listaDocumentos
	 * @throws IOException 
	 */
	public void realizarTreinamento(TreeMap<String, EstatisticaPalavra> mapaEstatisticaPalavra,List<MTDDocument> listaDocumentos) throws IOException{
		
		File arquivoProp = MTDArquivoEnum.PROPERTIES_TREINAMENTO.getArquivo(); 
		String[] cmdLine = new String[]{"--numberWinners",""+(NUM_CICLOS * listaDocumentos.size()),"--cpus",""+Runtime.getRuntime().availableProcessors(), arquivoProp.getAbsolutePath()};
		gerarArquivoProperties(cmdLine,arquivoProp, listaDocumentos.size());
		
		gerarArquivoTemplateVetor(mapaEstatisticaPalavra,listaDocumentos);
		gerarArquivoVetorEntradas(mapaEstatisticaPalavra,listaDocumentos);
		
		GrowingSOM.main(cmdLine);
		
	}
	
	public void iniciarTreinamento() {
        
    }
	
	/**
	 * Gera os aquivos de mapa de palavra e mapa palavra documento
	 * 
	 * Retorna um conjunto contendo os ids dos documentos que foram salvos. 
	 * 
	 * @param mapaEstatisticaPalavra
	 * @return
	 * @throws IOException
	 */
	private void gerarMapaPalavraEPalavraDoc(TreeMap<String, EstatisticaPalavra> mapaEstatisticaPalavra) throws IOException{
		System.out.println("Gerando mapa de palavras e mapa de palavra_documento...");
		MTDFactory mtdFabrica = MTDFactory.getInstancia();
		FileOutputStream fosPalavras = mtdFabrica.getTreinamentoStream(MTDArquivoEnum.WORD_TABLE);
		FileOutputStream fosPalavraDoc = mtdFabrica.getTreinamentoStream(MTDArquivoEnum.WORD_DOC_TABLE);
	    
		int contador = 0;
		Iterator<String> iterator = mapaEstatisticaPalavra.keySet().iterator();
		
		while(iterator.hasNext()){
			
			String palavra = iterator.next();
			//========= escrever wordtable =======
			EstatisticaPalavra auxEstat = mapaEstatisticaPalavra.get(palavra);
			//contadoPalavra, palavra, docFreq, freqMin, freqMax, freqMedia, frqMediaGlobal
	    	String aux = (++contador)+" "+palavra+" "+auxEstat.getDocFreq()+" "+auxEstat.getFreqMin()+" "+auxEstat.getFreqMax()+" "+auxEstat.getFreqMediaLocal()+" "+auxEstat.getFreqMediaGlobal();
	    	if(iterator.hasNext()){
	    		aux += "\n";
	    	}
	    	fosPalavras.write(aux.getBytes());
	    	fosPalavras.flush();
	    	//========= escrever wordtable =======
	    	
	    	EstatisticaPalavra estatisticaPalavra = mapaEstatisticaPalavra.get(palavra);
	    	TreeMap<Integer, Integer> mapaDocFreq = estatisticaPalavra.getMapaDocFreq();
	    	Iterator<Integer> iteratorDocFreq = mapaDocFreq.keySet().iterator();
	    	
	    	while(iteratorDocFreq.hasNext()){
	    		Integer docId = iteratorDocFreq.next();
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
		
	    fosPalavraDoc.close();
	    fosPalavras.close();
	}
	
	private TreeSet<Integer> getDocIdSet(TreeMap<String, EstatisticaPalavra> mapaEstatisticaPalavra) throws IOException{
		
		TreeSet<Integer> mapaDocId = new TreeSet<Integer>();
		Iterator<String> iterator = mapaEstatisticaPalavra.keySet().iterator();
		while(iterator.hasNext()){
			String palavra = iterator.next();
			
			EstatisticaPalavra estatisticaPalavra = mapaEstatisticaPalavra.get(palavra);
			TreeMap<Integer, Integer> mapaDocFreq = estatisticaPalavra.getMapaDocFreq();
			
			Iterator<Integer> iteratorDocFreq = mapaDocFreq.keySet().iterator();
			
			while(iteratorDocFreq.hasNext()){
				Integer docId = iteratorDocFreq.next();
				mapaDocId.add(docId);
			}
		}
		
		return mapaDocId;
	}
	
	/**
	 * Escreve no arquivo os dados relativos ao mapa de documentos
	 * que é representado pelo id do doc e dados adicionais formados por seus atributos
	 * 
	 * onde cada linha do arquivo segue a formatacao a seguir (id atributo;atributo;atributo)
	 *  
	 * @param listaDocumentos
	 * @throws IOException
	 */
	private void gerarMapaDocumentos(List<MTDDocument> listaDocumentos) throws IOException{
		System.out.println("Gerando mapa de documentos...");
		
	    MTDFactory mtdFabrica = MTDFactory.getInstancia();
		FileOutputStream fosDocs = mtdFabrica.getTreinamentoStream(MTDArquivoEnum.DOC_TABLE);
	    
	    for(int i = 0; i <listaDocumentos.size() ; i++){
	    	MTDDocument doc = listaDocumentos.get(i);
    		//========= escrever docTable =========== 
	    	
	    	AreaCNPQEnum areaCnpq = mtdFabrica.getAreaCNPQ(doc.getAreaCNPQ());
	    	
	    	//como o separador de colunas é ; substituiremos por virgula(,) qualquer ocorrencia dentro dos textos.
    		String dadosDoc = doc.getDocId() +";"+doc.getId()+";"+areaCnpq+";"+doc.getAreaPrograma()+";"+doc.getPrograma()+";"+doc.getTitulo().replace(";", ",");
    		if(i != listaDocumentos.size() - 1){
    			dadosDoc+="\n";
    		}
    		fosDocs.write(dadosDoc.getBytes());
    		fosDocs.flush();
    		
    		//========= escrever docTable ===========
	    }
	    
	    fosDocs.close();
	}
	
	public void gerarArquivoTemplateVetor(TreeMap<String, EstatisticaPalavra> mapaEstatisticaPalavra, List<MTDDocument> listaDocumentos) throws IOException{
		System.out.println("Gerando template ovector...");
	    
		FileOutputStream fos = MTDFactory.getInstancia().getTreinamentoStream(MTDArquivoEnum.TEMPLATE_TREINAMENTO);
	    StringBuilder strBuilder = new StringBuilder();
	    
	    strBuilder.append("$TYPE template\n");
	    strBuilder.append("$XDIM 7\n");
	    strBuilder.append("$YDIM " + listaDocumentos.size()+"\n");
	    strBuilder.append("$VEC_DIM " + mapaEstatisticaPalavra.size()+"\n");
	    
	    
	    Iterator<String> it = mapaEstatisticaPalavra.keySet().iterator();
	    int i = 0;
	    while(it.hasNext()){
	    	String palavra = it.next();
	    	EstatisticaPalavra estatisticaPalavra = mapaEstatisticaPalavra.get(palavra);
	    	String linha = i +" " + palavra + " " +estatisticaPalavra.getDocFreq() + " " +estatisticaPalavra.getTotalDocFreq() + 
	    			" " +estatisticaPalavra.getFreqMin() + " " + estatisticaPalavra.getFreqMax() + " " +estatisticaPalavra.getFreqMediaGlobal();
	    	
	    	strBuilder.append(linha);
	    	
	    	if(it.hasNext()){
	    		strBuilder.append("\n");
	    	}
	    	i++;
	    }
	    
	    fos.write(strBuilder.toString().getBytes());
	    fos.flush();
	    fos.close();
	}
	
	public void gerarArquivoVetorEntradas(TreeMap<String, EstatisticaPalavra> mapaEstatisticaPalavra, List<MTDDocument> listaDocumentos) throws IOException{
		System.out.println("Gerando vector de entradas...");
		
		FileOutputStream fos = MTDFactory.getInstancia().getTreinamentoStream(MTDArquivoEnum.VECTOR_TREINAMENTO);
		
		try {
			StringBuilder strBuilder = new StringBuilder();
			
			strBuilder.append("$TYPE vector\n");
			strBuilder.append("$XDIM "+ listaDocumentos.size() +"\n");
			strBuilder.append("$YDIM 1\n");
			strBuilder.append("$VEC_DIM " + mapaEstatisticaPalavra.size()+"\n");
			
			fos.write(strBuilder.toString().getBytes());
			fos.flush();
			
			StringBuilder strLinha = new StringBuilder();
			for(MTDDocument doc : listaDocumentos){
				Iterator<String> it = mapaEstatisticaPalavra.keySet().iterator();
				
				while (it.hasNext()) {
					String string = (String) it.next();
					TreeMap<Integer, Integer> mapaDocIdFreq = mapaEstatisticaPalavra.get(string).getMapaDocFreq();
					
					if(mapaDocIdFreq.containsKey(doc.getDocId())){
						strLinha.append(mapaDocIdFreq.get(doc.getDocId()));
					}else{
						strLinha.append("0");
					}
					strLinha.append(" ");
				}
				
				strLinha.append(" "+doc.getDocId());
				
				fos.write(strLinha.toString().getBytes());
				fos.write("\n".getBytes());
				fos.flush();
				strLinha = new StringBuilder();
			}

			
		} catch (IOException e) {
			throw e;
			
		}finally{
			try {
				if(fos != null){
					fos.close();
				}
			} catch (Exception e2) {
				//
			}
		}
	}
	
	public void gerarArquivoProperties(String[] cmdLine, File arquivoDestino, int tamListaDoc) throws IOException{
		System.out.println("Gerando arquivo de properties...");
		Integer randoSeed = NUM_CICLOS * tamListaDoc;
		
		Properties properties = new Properties();

		properties.setProperty("outputDirectory", MTDArquivoEnum.PASTA_TREINO.getArquivo().getAbsolutePath());
		properties.setProperty("workingDirectory",MTDArquivoEnum.PASTA_TREINO.getArquivo().getAbsolutePath());
		
		properties.setProperty("tau", "1.0");
		properties.setProperty("namePrefix", "Treino");
		properties.setProperty("vectorFileName", MTDArquivoEnum.VECTOR_TREINAMENTO.getArquivo().getAbsolutePath());
		properties.setProperty("sparseData", "yes");
		properties.setProperty("isNormalized", "no");//falta normalizar
		
		properties.setProperty("xSize", "10");
		properties.setProperty("ySize", "10");
		properties.setProperty("learnrate", "0.7");
		properties.setProperty("sigma", "1.0");
		
		properties.setProperty("templateFileName", MTDArquivoEnum.TEMPLATE_TREINAMENTO.getArquivo().getAbsolutePath());
		properties.setProperty("randomSeed", randoSeed.toString());

		properties.setProperty("numCycles", NUM_CICLOS.toString());
//		properties.setProperty("numIterations", "32");//esta usando ciclos e nao iteracoes
		
		properties.setProperty("metricName", "at.tuwien.ifs.somtoolbox.layers.metrics.L2Metric");
		properties.setProperty("growthQualityMeasureName", "");
		
        final String header = arquivoDestino.getName() + " prop file\n# somtoolbox " + Arrays.deepToString(cmdLine);
        FileWriter writer = new FileWriter(arquivoDestino); 
        properties.store(writer, StringUtils.wrap(header, 80, "#   "));
        writer.close();
	}
}