package br.ufpe.mtd.thread;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import br.ufpe.mtd.dados.IRepositorioIndice;
import br.ufpe.mtd.dados.RepositorioIndiceLucene;
import br.ufpe.mtd.entidade.DocumentMTD;
import br.ufpe.mtd.enumerado.MTDArquivoEnum;
import br.ufpe.mtd.util.MTDFactory;

public class TreinamentoThread extends BaseThread{
	
	@Override
	public void run() {
		super.run();
		try {
			System.out.println(" ---- iniciando treinamento da rede neural-----");
			gerarArquivosEntradaRN();
			System.out.println(" ---- fim do treinamento da rede neural-----");
		} catch (IOException e) {
			MTDFactory.getInstancia().getLog().salvarDadosLog(e);
		}
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
	 * 
	 * @throws IOException
	 */
	public void gerarArquivosEntradaRN() throws IOException{
		MTDFactory mtdFabrica = MTDFactory.getInstancia();
		IRepositorioIndice rep = mtdFabrica.getSingleRepositorioIndice();
		
		if(rep instanceof RepositorioIndiceLucene){
			RepositorioIndiceLucene repLucene = (RepositorioIndiceLucene)rep;
			TreeMap<String, TreeMap<Integer, Integer>> mapaPalavraDocFreq = repLucene.getMapaPalavraDocFreq(new String[] {DocumentMTD.TITULO, DocumentMTD.RESUMO, DocumentMTD.AREA_CNPQ, DocumentMTD.KEY_WORD});
			
			TreeSet<Integer> mapaDocId = gerarMapaPalavraEPalavraDoc(mapaPalavraDocFreq);
		    
			List<DocumentMTD> listaDocumentos = repLucene.getDocumentos(mapaDocId);
		    gerarMapaDocumentos(listaDocumentos);
		}
	}
	
	/**
	 * Gera os aquivos de mapa de palavra e mapa palavra documento
	 * 
	 * Retorna um conjunto contendo os ids dos documentos. 
	 * 
	 * @param mapaPalavraDocFreq
	 * @return
	 * @throws IOException
	 */
	private TreeSet<Integer> gerarMapaPalavraEPalavraDoc(TreeMap<String, TreeMap<Integer, Integer>> mapaPalavraDocFreq) throws IOException{
		
		TreeSet<Integer> mapaDocId = new TreeSet<Integer>();
		
		MTDFactory mtdFabrica = MTDFactory.getInstancia();
		FileOutputStream fosPalavras = mtdFabrica.getTreinamentoStream(MTDArquivoEnum.WORD_TABLE);
		FileOutputStream fosPalavraDoc = mtdFabrica.getTreinamentoStream(MTDArquivoEnum.WORD_DOC_TABLE);
	    
		int contador = 0;
		Iterator<String> iterator = mapaPalavraDocFreq.keySet().iterator();
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
	    	
	    	TreeMap<Integer, Integer> mapaDocFreq = mapaPalavraDocFreq.get(palavra);
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
		
	    fosPalavraDoc.close();
	    fosPalavras.close();
	    
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
	private void gerarMapaDocumentos(List<DocumentMTD> listaDocumentos) throws IOException{
		
	    MTDFactory mtdFabrica = MTDFactory.getInstancia();
		FileOutputStream fosDocs = mtdFabrica.getTreinamentoStream(MTDArquivoEnum.DOC_TABLE);
	    
	    for(int i = 0; i <listaDocumentos.size() ; i++){
	    	DocumentMTD doc = listaDocumentos.get(i);
    		//========= escrever docTable ===========
    		String dadosDoc = doc.getDocId() +" "+doc.getId()+";"+doc.getAreaCNPQ()+";"+doc.getTitulo()+";"+doc.getAreaPrograma();
    		if(i != listaDocumentos.size() - 1){
    			dadosDoc+="\n";
    		}
    		fosDocs.write(dadosDoc.getBytes());
    		fosDocs.flush();
    		//========= escrever docTable ===========
	    }
	    
	    fosDocs.close();
	}
}