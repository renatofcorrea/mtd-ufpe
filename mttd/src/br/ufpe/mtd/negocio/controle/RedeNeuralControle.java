package br.ufpe.mtd.negocio.controle;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Logger;

import at.tuwien.ifs.somtoolbox.data.InputData;
import at.tuwien.ifs.somtoolbox.data.InputDataFactory;
import at.tuwien.ifs.somtoolbox.input.SOMLibDataWinnerMapping;
import at.tuwien.ifs.somtoolbox.input.SOMLibFileFormatException;
import at.tuwien.ifs.somtoolbox.input.SOMLibFormatInputReader;
import at.tuwien.ifs.somtoolbox.layers.GrowingLayer;
import at.tuwien.ifs.somtoolbox.layers.quality.QualityMeasure;
import at.tuwien.ifs.somtoolbox.models.GrowingSOM;
import at.tuwien.ifs.somtoolbox.util.StringUtils;
import br.ufpe.mtd.dados.indice.IRepositorioIndice;
import br.ufpe.mtd.dados.indice.RepositorioIndiceLucene;
import br.ufpe.mtd.negocio.entidade.EstatisticaPalavra;
import br.ufpe.mtd.negocio.entidade.MTDDocument;
import br.ufpe.mtd.negocio.entidade.MedidasQualidade;
import br.ufpe.mtd.util.MTDFactory;
import br.ufpe.mtd.util.MTDIterator;
import br.ufpe.mtd.util.MTDParametros;
import br.ufpe.mtd.util.enumerado.AreaCNPQEnum;
import br.ufpe.mtd.util.enumerado.MTDArquivoEnum;
import br.ufpe.mtd.util.log.Log;

/**
 * Representa a regra de treinamento da rede neural.
 * 
 * Os dois principais metodos sao treinarRedeNeiral e retreinarRedeNeural
 * os quais representam o objetivo da classe. Sendo os outros metodos auxiliares
 * destes.
 * 
 * @author djalma
 *
 */
public class RedeNeuralControle {

	public static final int MAPA_X_SIZE = 12;
	public static final int MAPA_Y_SIZE = 10;
	public static final double SIGMA = 9.0;
	public static final double TAU = 1.0;
	public static final double TAXA_APRENDIZADO = 0.7;
	private final int NUM_CICLOS = 10;
	
	private Log log;

	public RedeNeuralControle(Log log) {
		this.log = log;
	}

	
	public void treinarRedeNeural() {
		try {
			long inicio = System.currentTimeMillis();
			log.salvarDadosLog(" RedeNeuralControle.treinarRedeNeural() ---- iniciando treinamento da rede neural-----");

			deletarTreinoAnteiror();

			log.salvarDadosLog("Processando termos...");
			TreeMap<String, EstatisticaPalavra> mapaEstatisticaPalavra = getMapaEstatisticaPalavra();

			log.salvarDadosLog("Recuperando documentos...");
			List<MTDDocument> listaDocumentos = getListaDocumentos(mapaEstatisticaPalavra);

			gerarArquivosEntradaRN(listaDocumentos, mapaEstatisticaPalavra);

			realizarTreinamento(mapaEstatisticaPalavra, listaDocumentos);

			
			log.salvarDadosLog(" ---- fim do treinamento da rede neural---- Tempo decorrido" + (System.currentTimeMillis() - inicio));

		} catch (Exception e) {
			log.salvarDadosLog(e);
		}
	}

	public void retreinarRedeNeural() {
		try {
			long inicio = System.currentTimeMillis();
			log.salvarDadosLog(" RedeNeuralControle.retreinarRedeNeural() ---- iniciando retreinamento da rede neural-----");
			log.salvarDadosLog(" ---- Error: faltando implementar em RedeNeuralControle.retreinarRedeNeural() -----");
			
			//gerar arquivo retreino properties
			//mapDescriptionFile
			//weightVectorFile
			
			log.salvarDadosLog(" ---- fim do retreinamento da rede neural----- Tempo decorrido" + (System.currentTimeMillis() - inicio));

		} catch (Exception e) {
			log.salvarDadosLog(e);
		}
	}

	public static void main(String[] args) {
		try {
			List<double[]> lista = new RedeNeuralControle(MTDFactory.getInstancia().getLog()).lerPesosIniciais();
			
			for (double[] ds : lista) {
				for (double d : ds) {
					System.out.print(d+" ");
				}
				System.out.println("");
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private List<double[]> lerPesosIniciais() throws Exception {
		List<double[]> lista = new ArrayList<double[]>();
		MTDIterator<String> iterator = MTDArquivoEnum.TREINO_WGT.lineIterator();
		boolean lerPesos = false;
		while(iterator.hasNext()){
			String linha = iterator.next();
			String[] dados = linha.split(" ");
			if(lerPesos){
				double[] pesos = new double[dados.length -1];
				int posicao = 0;
				for(String dado: dados){
					pesos[posicao] = Double.parseDouble(dado);
					posicao++;
					if(posicao >= pesos.length){
						break;
					}
				}
				lista.add(pesos);
			}else if(dados[0].equals("$VEC_DIM")){
				lerPesos = true;
			}
		}
		iterator.close();
		return lista;
	}

	/**
	 * Vai deletar os arquivos do treinamento anterior.
	 * 
	 */
	public void deletarTreinoAnteiror() {
		for(File arquivo: MTDArquivoEnum.PASTA_TREINO.getArquivo().listFiles()){
			arquivo.delete();
			try {
				arquivo.createNewFile();
			} catch (IOException e) {
				log.salvarDadosLog(e);
			}
		}
	}

	public static double capturarErroMedioQuantizacao() {
		double erro = 0.0;
		MTDIterator<Integer> it = null;
		try {
			it = MTDArquivoEnum.TREINO_MAP.iterator();
			StringBuffer sb = new StringBuffer();
			int byteLido = 0;
			while(it.hasNext()){
				byteLido = it.next();
				sb.append((char)byteLido);
				String linha = sb.toString();
				if(linha.contains("\n")){
					if(linha.contains("$QUANTERROR_VEC")){
						String strErro = linha.split(" ")[1];
						erro = Double.parseDouble(strErro);
						break;
					}else{
						sb.delete(0, sb.length());
					}
				}
			}
		} catch (Exception e) {
			
		}finally{
			if(it != null){
				try {
					it.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		return erro;
	}

	private TreeMap<String, EstatisticaPalavra> getMapaEstatisticaPalavra() throws Exception {

		MTDFactory mtdFabrica = MTDFactory.getInstancia();
		IRepositorioIndice rep = mtdFabrica.getSingleRepositorioIndice();

		String[] campos = new String[] { MTDDocument.TITULO, MTDDocument.RESUMO, MTDDocument.KEY_WORD, MTDDocument.PROGRAMA };// MTDDocument.AREA_CNPQ,
		RepositorioIndiceLucene repLucene = (RepositorioIndiceLucene) rep;

		//TODO: colocar como parâmetros do sistema
		int docFreqMax = (int) Math.round((repLucene.getQuantidadeDocumentosNoIndice() * 50.0) / 100.0); // 50%
		int docFreqMin = 10;
		int numMaxDoc = 5000;

		//TODO: analisar radicalizador utilizado na função RepositorioIndiceLucene.getListaPalavrasFiltrado
		List<EstatisticaPalavra> filtroPalavrasRelevantes = repLucene.getListaPalavrasFiltrado(campos, numMaxDoc, docFreqMin, docFreqMax);
		TreeMap<String, EstatisticaPalavra> mapaEstatisticaPalavra = repLucene.getMapaPalavraDocFreq(campos, filtroPalavrasRelevantes);

		return mapaEstatisticaPalavra;
	}

	private List<MTDDocument> getListaDocumentos(TreeMap<String, EstatisticaPalavra> mapaEstatisticaPalavra) throws IOException {
		TreeSet<Integer> mapaDocId = getDocIdSet(mapaEstatisticaPalavra);
		MTDFactory mtdFabrica = MTDFactory.getInstancia();
		IRepositorioIndice rep = mtdFabrica.getSingleRepositorioIndice();
		return ((RepositorioIndiceLucene) rep).getDocumentos(mapaDocId);
	}

	/**
	 * Gera os arquivos de entrada para o treinamento da rede neural do MTD. 3
	 * arquivos sao gerados word_table - que contem uma tabela textual com as
	 * informacoes das palvras no formato (id_palavra palavra) sendo espaÃ§o o
	 * separador das colunas. doc_table - que contem uma tabela textual com as
	 * informacoes das dos documentos no formato (id_documento
	 * doc_identifier;area_cnpq;titulo;area_programa) sendo espaco o separador
	 * das colunas. word_doc_table - que contem uma tabela textual com as
	 * informacoes das palvras-documentos-frequencia da palavra no doc, no
	 * formato (idpalavra id_documento frequencia) sendo espaÃ§o o separador das
	 * colunas.
	 * 
	 * @throws Exception
	 */
	public void gerarArquivosEntradaRN(List<MTDDocument> listaDocumentos, TreeMap<String, EstatisticaPalavra> mapaEstatisticaPalavra) throws Exception {
		gerarMapaPalavraEPalavraDoc(mapaEstatisticaPalavra);
		gerarMapaDocumentos(listaDocumentos);
		TreeMap<Integer, Double> mapaNormas = getMapaDocsNormalizado(mapaEstatisticaPalavra);
		gerarArquivoTemplateVetor(mapaEstatisticaPalavra, listaDocumentos, mapaNormas);
		gerarArquivoVecCls(mapaEstatisticaPalavra, listaDocumentos, mapaNormas);
	}

	/**
	 * Gera os arquivos especificos para entrada da rede SOM e chama o
	 * GrowingSom que realizara o treinamento da rede propriamente dito
	 * 
	 * @param mapaEstatisticaPalavra
	 * @param listaDocumentos
	 * @throws IOException
	 */
	public void realizarTreinamento(TreeMap<String, EstatisticaPalavra> mapaEstatisticaPalavra, List<MTDDocument> listaDocumentos) throws IOException {
		File arquivoProp = MTDArquivoEnum.PROPERTIES_TREINAMENTO.getArquivo();

		// TODO:este comando irá mudar para sugerir pesos iniciais para o mapa
		// opção
		// --w Treino.wgt.gz (caminho para web-inf\aux_files\Treino.wgt.gz)
		// uma condição para isto é que o número de palavras deve ser fixo, e
		// não mais variável
		// $VEC_DIM 12869 que é o caso do arquivo que está em
		// web-inf\aux_files\Treino.wgt.gz
		// após a indexação, na geração dos arquivos para treinamento a dimensão
		// deve ser respeitada, selecionando os 13067 primeiras palavras com
		// maior document frequency
		// http://www.ifs.tuwien.ac.at/dm/somtoolbox/somtoolbox-reference.html#GrowingSOM
		// Usage: java at.tuwien.ifs.somtoolbox.models.GrowingSOM [-h] [-l
		// <labeling>] [-n <numberLabels>] [-w <weightVectorFile>] [-m
		// <mapDescriptionFile>] [--skipDWM] [--numberWinners <numberWinners>]
		// <properties> [--cpus <cpus>]
		//---->uma opção para resolver o problema da dimensionalidade entre mapas
		//é gerar a incialização dos peso com base na média (centróide)
		//dos novos vetores documentos dos documentos mapeados em cada nodo no mapa anterior
		String[] cmdLine = new String[] { "--numberWinners", new Integer(listaDocumentos.size()).toString(), "--cpus", MTDParametros.getNumMaxThreads().toString(), arquivoProp.getAbsolutePath() };
		gerarArquivoProperties(cmdLine, arquivoProp, listaDocumentos.size());

		GrowingSOM.main(cmdLine);
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
	private void gerarMapaPalavraEPalavraDoc(TreeMap<String, EstatisticaPalavra> mapaEstatisticaPalavra) throws IOException {
		log.salvarDadosLog("RedeNeuralControle.gerarMapaPalavraEPalavraDoc() Gerando mapa de palavras e mapa de palavra_documento...");
		FileOutputStream fosPalavras = MTDArquivoEnum.WORD_TABLE.getFileOutputStream(false);
		FileOutputStream fosPalavraDoc = MTDArquivoEnum.WORD_DOC_TABLE.getFileOutputStream(false);

		int contador = 0;
		Iterator<String> iterator = mapaEstatisticaPalavra.keySet().iterator();

		while (iterator.hasNext()) {

			String palavra = iterator.next();
			// ========= escrever wordtable =======
			EstatisticaPalavra auxEstat = mapaEstatisticaPalavra.get(palavra);
			// contadoPalavra, palavra, docFreq, freqMin, freqMax, freqMedia,
			// frqMediaGlobal
			String aux = (++contador) + " " + palavra + " " + auxEstat.getDocFreq() + " " + auxEstat.getFreqMin() + " " + auxEstat.getFreqMax() + " " + auxEstat.getFreqMediaLocal() + " " + auxEstat.getFreqMediaGlobal();
			if (iterator.hasNext()) {
				aux += "\n";
			}
			fosPalavras.write(aux.getBytes());
			fosPalavras.flush();
			// ========= escrever wordtable =======

			EstatisticaPalavra estatisticaPalavra = mapaEstatisticaPalavra.get(palavra);
			TreeMap<Integer, Integer> mapaDocFreq = estatisticaPalavra.getMapaDocFreq();
			Iterator<Integer> iteratorDocFreq = mapaDocFreq.keySet().iterator();

			while (iteratorDocFreq.hasNext()) {
				Integer docId = iteratorDocFreq.next();
				// ============= escrever wordDocTable =================
				aux = contador + " " + docId + " " + mapaDocFreq.get(docId);
				if (iterator.hasNext() | iteratorDocFreq.hasNext()) {
					aux += "\n";
				}
				fosPalavraDoc.write(aux.getBytes());
				fosPalavraDoc.flush();
				// ============= escrever wordDocTable =================
			}
		}

		fosPalavraDoc.close();
		fosPalavras.close();
	}

	/**
	 * Recupera todos os ids de documentos Representa o corpus
	 * 
	 * @param mapaEstatisticaPalavra
	 * @return
	 * @throws IOException
	 */
	private TreeSet<Integer> getDocIdSet(TreeMap<String, EstatisticaPalavra> mapaEstatisticaPalavra) throws IOException {

		TreeSet<Integer> mapaDocId = new TreeSet<Integer>();
		Iterator<String> iterator = mapaEstatisticaPalavra.keySet().iterator();
		while (iterator.hasNext()) {
			String palavra = iterator.next();

			EstatisticaPalavra estatisticaPalavra = mapaEstatisticaPalavra.get(palavra);
			TreeMap<Integer, Integer> mapaDocFreq = estatisticaPalavra.getMapaDocFreq();

			Iterator<Integer> iteratorDocFreq = mapaDocFreq.keySet().iterator();

			while (iteratorDocFreq.hasNext()) {
				Integer docId = iteratorDocFreq.next();
				mapaDocId.add(docId);
			}
		}

		return mapaDocId;
	}

	/**
	 * Recupera todos os ids de documentos Representa o corpus
	 * 
	 * @param mapaEstatisticaPalavra
	 * @return
	 * @throws IOException
	 */
	private TreeMap<Integer, Double> getMapaDocsNormalizado(TreeMap<String, EstatisticaPalavra> mapaEstatisticaPalavra) throws IOException {

		TreeMap<Integer, Double> mapaDocFreqNorm = new TreeMap<Integer, Double>();

		TreeMap<Integer, TreeMap<String, Integer>> mapaDocs = new TreeMap<Integer, TreeMap<String, Integer>>();

		Iterator<String> iterator = mapaEstatisticaPalavra.keySet().iterator();

		while (iterator.hasNext()) {
			String palavra = iterator.next();

			EstatisticaPalavra estatisticaPalavra = mapaEstatisticaPalavra.get(palavra);
			TreeMap<Integer, Integer> mapaDocFreq = estatisticaPalavra.getMapaDocFreq();

			Iterator<Integer> iteratorDocFreq = mapaDocFreq.keySet().iterator();

			while (iteratorDocFreq.hasNext()) {
				Integer docId = iteratorDocFreq.next();
				// cria o mapa na primeira vez
				if (!mapaDocs.containsKey(docId)) {
					mapaDocs.put(docId, new TreeMap<String, Integer>());
				}

				TreeMap<String, Integer> mapa = mapaDocs.get(docId);

				if (!mapa.containsKey(palavra)) {
					mapa.put(palavra, mapaDocFreq.get(docId) * mapaDocFreq.get(docId));

				} else {
					Integer freqAcum = mapa.get(docId) + mapaDocFreq.get(docId * mapaDocFreq.get(docId));
					mapa.put(palavra, freqAcum);

				}
			}
		}

		for (Integer docId : mapaDocs.keySet()) {
			Integer norma = 0;
			TreeMap<String, Integer> mapa = mapaDocs.get(docId);

			for (String palavra : mapa.keySet()) {
				norma += mapa.get(palavra);
			}

			mapaDocFreqNorm.put(docId, Math.sqrt(norma));
		}

		return mapaDocFreqNorm;
	}

	/**
	 * Escreve no arquivo os dados relativos ao mapa de documentos que Ã©
	 * representado pelo id do doc e dados adicionais formados por seus
	 * atributos
	 * 
	 * onde cada linha do arquivo segue a formatacao a seguir (id
	 * atributo;atributo;atributo)
	 * 
	 * @param listaDocumentos
	 * @throws IOException
	 */
	private void gerarMapaDocumentos(List<MTDDocument> listaDocumentos) throws IOException {
		log.salvarDadosLog("RedeNeuralControle.gerarMapaDocumentos() Gerando mapa de documentos...");

		FileOutputStream fosDocs = MTDArquivoEnum.DOC_TABLE.getFileOutputStream(false);

		for (int i = 0; i < listaDocumentos.size(); i++) {
			MTDDocument doc = listaDocumentos.get(i);
			// ========= escrever docTable ===========

			AreaCNPQEnum areaCnpq = doc.getGrandeArea();

			// como o separador de colunas  e ; substituiremos por virgula(,)
			// qualquer ocorrencia dentro dos textos.
			String dadosDoc = doc.getDocId() + ";" + doc.getId() + ";" + areaCnpq + ";" + doc.getAreaPrograma() + ";" + doc.getPrograma() + ";" + doc.getTitulo().replace(";", ",");
			if (i != listaDocumentos.size() - 1) {
				dadosDoc += "\n";
			}
			fosDocs.write(dadosDoc.getBytes());
			fosDocs.flush();

			// ========= escrever docTable ===========
		}

		fosDocs.close();
	}

	public void gerarArquivoTemplateVetor(TreeMap<String, EstatisticaPalavra> mapaEstatisticaPalavra, List<MTDDocument> listaDocumentos, TreeMap<Integer, Double> mapaNormas) throws IOException {
		log.salvarDadosLog("RedeNeuralControle.gerarArquivoTemplateVetor() Gerando template vector...");

		FileOutputStream fos = MTDArquivoEnum.TEMPLATE_TREINAMENTO.getFileOutputStream(false);
		FileOutputStream fosNorm = MTDArquivoEnum.TEMPLATE_TREINAMENTO_NORM.getFileOutputStream(false);

		StringBuilder strBuilder = new StringBuilder();
		StringBuilder strBuilderNorm = new StringBuilder();

		strBuilder.append("$TYPE template\n");
		strBuilder.append("$XDIM 7\n");
		strBuilder.append("$YDIM " + listaDocumentos.size() + "\n");
		strBuilder.append("$VEC_DIM " + mapaEstatisticaPalavra.size() + "\n");

		strBuilderNorm.append(strBuilder.toString());

		Iterator<String> it = mapaEstatisticaPalavra.keySet().iterator();
		int i = 0;
		while (it.hasNext()) {
			String palavra = it.next();

			EstatisticaPalavra estatisticaPalavra = mapaEstatisticaPalavra.get(palavra);
			estatisticaPalavra.normalizarMapa(mapaNormas);
			estatisticaPalavra.gerarEstatisticaNorm();

			String linha = i + " " + palavra + " " + estatisticaPalavra.getDocFreq() + " " + estatisticaPalavra.getTotalDocFreq() + " " + estatisticaPalavra.getFreqMin() + " " + estatisticaPalavra.getFreqMax() + " " + estatisticaPalavra.getFreqMediaGlobal();

			String linhaNorm = i + " " + palavra + " " + estatisticaPalavra.getDocFreq() + " " + estatisticaPalavra.getTotalDocFreq() + " " + estatisticaPalavra.getFreqMinNorm().longValue() + " " + Double.valueOf(Math.ceil(estatisticaPalavra.getFreqMaxNorm())).longValue() + " " + estatisticaPalavra.getFreqMediaGlobalNorm();

			strBuilder.append(linha);
			strBuilderNorm.append(linhaNorm);

			if (it.hasNext()) {
				strBuilder.append("\n");
				strBuilderNorm.append("\n");
			}
			i++;
		}

		fos.write(strBuilder.toString().getBytes());
		fos.flush();

		fosNorm.write(strBuilderNorm.toString().getBytes());
		fosNorm.flush();

		fos.close();
		fosNorm.close();
	}

	/**
	 * Gera o arquivo de vetor de entradas .vec E os arquivos de visualizacao
	 * .cls
	 * 
	 * Sao abertas streams para todos os arquivos onde a cada linha gerada os
	 * dados sao flusheados para os arquivos creespondentes.
	 * 
	 * 
	 * @param mapaEstatisticaPalavra
	 * @param listaDocumentos
	 * @throws IOException
	 */
	public void gerarArquivoVecCls(TreeMap<String, EstatisticaPalavra> mapaEstatisticaPalavra, List<MTDDocument> listaDocumentos, TreeMap<Integer, Double> mapaNormas) throws IOException {
		log.salvarDadosLog("RedeNeuralControle.gerarArquivoVecCls() Gerando vector de entradas e arquivos cls...");

		FileOutputStream fosVec = MTDArquivoEnum.VECTOR_TREINAMENTO.getFileOutputStream(false);
		FileOutputStream fosVecNorm = MTDArquivoEnum.VECTOR_TREINAMENTO_NORM.getFileOutputStream(false);
		FileOutputStream fosClsAreaP = MTDArquivoEnum.CLS_AREA_PROGRAMA.getFileOutputStream(false);
		FileOutputStream fosClsProg = MTDArquivoEnum.CLS_PROGRAMA.getFileOutputStream(false);
		FileOutputStream fosClsAreaCnpq = MTDArquivoEnum.CLS_AREA_CNPQ.getFileOutputStream(false);
		FileOutputStream fosClsGranArea = MTDArquivoEnum.CLS_GRANDE_AREA.getFileOutputStream(false);

		try {
			// ======================= cabecalho =======================
			StringBuilder strBuilder = new StringBuilder();

			strBuilder.append("$TYPE vector\n");
			strBuilder.append("$XDIM " + listaDocumentos.size() + "\n");
			strBuilder.append("$YDIM 1\n");
			strBuilder.append("$VEC_DIM " + mapaEstatisticaPalavra.size() + "\n");

			fosVec.write(strBuilder.toString().getBytes());
			fosVec.flush();

			fosVecNorm.write(strBuilder.toString().getBytes());
			fosVecNorm.flush();
			// ======================= cabecalho =======================

			for (MTDDocument doc : listaDocumentos) {
				StringBuilder strLinha = new StringBuilder();
				StringBuilder strLinhaNorm = new StringBuilder();

				// formatar linha arquivos vec
				Double norma = mapaNormas.get(doc.getDocId());
				Iterator<String> it = mapaEstatisticaPalavra.keySet().iterator();

				while (it.hasNext()) {
					String string = (String) it.next();
					TreeMap<Integer, Integer> mapaDocIdFreq = mapaEstatisticaPalavra.get(string).getMapaDocFreq();

					if (mapaDocIdFreq.containsKey(doc.getDocId())) {
						strLinha.append(mapaDocIdFreq.get(doc.getDocId()));
						
						//valor dividido pela norma
						strLinhaNorm.append(mapaDocIdFreq.get(doc.getDocId()) / norma);
					} else {
						// a palavra nao aparece no doc freq 0
						strLinha.append("0");
						strLinhaNorm.append("0");
					}
					strLinha.append(" ");
					strLinhaNorm.append(" ");
				}

				//  formatar linha arquivos vec
				strLinha.append(" " + doc.getDocId());
				strLinhaNorm.append(" " + doc.getDocId());

				// =============== enviando os dados para os arquivos
				// ================
				fosVec.write(strLinha.toString().getBytes());
				fosVec.write("\n".getBytes());
				fosVec.flush();

				fosVecNorm.write(strLinhaNorm.toString().getBytes());
				fosVecNorm.write("\n".getBytes());
				fosVecNorm.flush();

				fosClsAreaCnpq.write((doc.getDocId() + "\t" + doc.getAreaCNPQ() + "\n").getBytes());
				fosClsAreaCnpq.flush();

				fosClsAreaP.write((doc.getDocId() + "\t" + doc.getAreaPrograma() + "\n").getBytes());
				fosClsAreaP.flush();

				fosClsProg.write((doc.getDocId() + "\t" + doc.getPrograma() + "\n").getBytes());
				fosClsProg.flush();

				fosClsGranArea.write((doc.getDocId() + "\t" + doc.getGrandeArea() + "\n").getBytes());
				fosClsGranArea.flush();
				// =============== enviando os dados para os arquivos
				// ================
			}

		} catch (IOException e) {
			throw e;

		} finally {
			// tenta fechar todas as Streams
			FileOutputStream[] streams = new FileOutputStream[] { fosVec, fosVecNorm, fosClsAreaCnpq, fosClsAreaP, fosClsProg, fosClsGranArea };
			for (FileOutputStream fileOutputStream : streams) {
				try {
					if (fileOutputStream != null) {
						fileOutputStream.close();
					}
				} catch (Exception e2) {
					log.salvarDadosLog(e2);
				}
			}
		}
	}

	/**
	 * O javaSomToolBox usa um arquivo de properties para treinar a rede assim
	 * vamos criar esse arquivo com os dados necesários para o treinamento.
	 * 
	 * @param cmdLine
	 * @param arquivoDestino
	 * @param tamListaDoc
	 * @throws IOException
	 */
	public void gerarArquivoProperties(String[] cmdLine, File arquivoDestino, int tamListaDoc) throws IOException {
		log.salvarDadosLog("RedeNeuralControle.gerarArquivoProperties() Gerando arquivo de properties...");
		Integer randoSeed = NUM_CICLOS * tamListaDoc;

		Properties properties = new Properties();

		properties.setProperty("outputDirectory", MTDArquivoEnum.PASTA_TREINO.getArquivo().getAbsolutePath());
		properties.setProperty("workingDirectory", MTDArquivoEnum.PASTA_TREINO.getArquivo().getAbsolutePath());

		properties.setProperty("tau", Double.toString(TAU));
		
		//todos arquivos terao o mesmo prefixo no nome
		properties.setProperty("namePrefix", MTDArquivoEnum.TREINO.getNomeArquivo());
																					
		properties.setProperty("vectorFileName", MTDArquivoEnum.VECTOR_TREINAMENTO_NORM.getArquivo().getAbsolutePath());
		properties.setProperty("sparseData", "yes");
		properties.setProperty("isNormalized", "yes");// falta normalizar

		properties.setProperty("xSize", Integer.toString(MAPA_X_SIZE));
		properties.setProperty("ySize", Integer.toString(MAPA_Y_SIZE));
		properties.setProperty("learnrate", Double.toString(TAXA_APRENDIZADO));
		properties.setProperty("sigma", Double.toString(SIGMA));

		properties.setProperty("templateFileName", MTDArquivoEnum.TEMPLATE_TREINAMENTO_NORM.getArquivo().getAbsolutePath());
		properties.setProperty("randomSeed", randoSeed.toString());

		properties.setProperty("numCycles", Integer.toString(NUM_CICLOS));

		properties.setProperty("metricName", "at.tuwien.ifs.somtoolbox.layers.metrics.L2Metric");
		properties.setProperty("growthQualityMeasureName", "");

		final String header = arquivoDestino.getName() + " prop file\n# somtoolbox " + Arrays.deepToString(cmdLine);
		FileWriter writer = new FileWriter(arquivoDestino);
		properties.store(writer, StringUtils.wrap(header, 80, "#   "));
		writer.close();
	}
	
	public static boolean isMedidasQualidadeAdequadas() throws ClassNotFoundException, IOException{
		File arquivo = MTDArquivoEnum.TREINO_MEDIDA_QUALIDADE.getArquivo();
		if(arquivo.length() == 0){
			return false;
		}
		
		MedidasQualidade mq = (MedidasQualidade)new ObjectInputStream(MTDArquivoEnum.TREINO_MEDIDA_QUALIDADE.getFileInputStream()).readObject(); 
		
		if(mq == null){
			return false;
		}
		
		if(mq.getTeMap() < 0.35d || mq.getErroQuantizacao() > 0.22d){
			return false;
		}
		
		return true;
	}

	/**
	 * Verifica as medidas de qualidade de um mapa treinado.
	 * 
	 * @throws SOMLibFileFormatException
	 * @throws Exception 
	 * @throws ClassNotFoundException 
	 */
	public void gerarMedidasQualidadeRedeNeural() throws SOMLibFileFormatException, ClassNotFoundException, Exception {
		if(isMedidasQualidadeAdequadas()){
			log.salvarDadosLog("RedeNeuralControle.gerarMedidasQualidadeRedeNeural() Medidas de qualidade já estão adequadas. Encerrando...");
			return;
		}
		log.salvarDadosLog("Inicio da geração de medidas de qualidade da rede neural...");
		log.salvarDadosLog("Calculando dados de qualidade da rede neural...");
		GrowingSOM gsom = null;
		InputData data = null;
		MedidasQualidade mq = new MedidasQualidade();

		String inputVectorFileName = MTDArquivoEnum.VECTOR_TREINAMENTO_NORM.getArquivo().getAbsolutePath();
		String mapDescFileName = MTDArquivoEnum.TREINO_MAP.getArquivo().getAbsolutePath();
		String dataWinnerMappingFile = MTDArquivoEnum.TREINO_DWM.getArquivo().getAbsolutePath();
		String weightFileName = MTDArquivoEnum.TREINO_WGT_GZ.getPathSemExtensao();
		String unitDescFileName = MTDArquivoEnum.TREINO_UNIT.getPathSemExtensao();

		SOMLibDataWinnerMapping dataWinnerMapping = new SOMLibDataWinnerMapping(dataWinnerMappingFile);
		data = InputDataFactory.open(inputVectorFileName);

		try {
			gsom = new GrowingSOM(new SOMLibFormatInputReader(weightFileName, unitDescFileName, mapDescFileName));
		} catch (Exception e) {
			Logger.getLogger("at.tuwien.ifs.somtoolbox").severe(e.getMessage() + " Aborting.");
		}

		GrowingLayer layer = gsom.getLayer();
		QualityMeasure medQuaTopografica = new at.tuwien.ifs.somtoolbox.layers.quality.TopographicError(layer, data);
		QualityMeasure medQuaQuantizacao = new at.tuwien.ifs.somtoolbox.layers.quality.QuantizationError(layer, data);
		QualityMeasure medQuaIntrinsicDistance = new at.tuwien.ifs.somtoolbox.layers.quality.IntrinsicDistance(layer, data);
		QualityMeasure medQuaTopgProduto = new at.tuwien.ifs.somtoolbox.layers.quality.TopographicProduct(layer, data);
		QualityMeasure medQuaDistorcao = new at.tuwien.ifs.somtoolbox.layers.quality.SOMDistortion(layer, data);
		QualityMeasure medQuaVisinhaca = new at.tuwien.ifs.somtoolbox.layers.quality.Trustworthiness_NeighborhoodPreservation(layer, data);

		// Erro topologico
		try {
			double medida = medQuaTopografica.getMapQuality("TE_Map");
			mq.setTeMap(medida);
			log.salvarDadosLog("TE_Map " + medida);
		} catch (Exception ex) {
			log.salvarDadosLog(ex);
		}
		try {
			double medida = medQuaTopografica.getMapQuality("TE8_Map");
			mq.setTe8Map(medida);
			log.salvarDadosLog("TE8_Map " + medida);
		} catch (Exception ex) {
			log.salvarDadosLog(ex);
			
		}
		// Erro de quantizacao
		try {
			double medida = medQuaQuantizacao.getMapQuality("mqe");
			mq.setMqe(medida);
			log.salvarDadosLog("QE_Map " + medida);
		} catch (Exception ex) {
			log.salvarDadosLog(ex);
		}
		try {
			double medida = medQuaQuantizacao.getMapQuality("mmqe");
			mq.setMmqe(medida);
			log.salvarDadosLog("MQE_Map " + medida);
		} catch (Exception ex) {
			log.salvarDadosLog(ex);
		}
		// Intrisic Distance
		try {
			double medida = medQuaIntrinsicDistance.getMapQuality("ID_Map");
			mq.setIdMap(medida);
			log.salvarDadosLog("ID_Map " + medida);
		} catch (Exception ex) {
			log.salvarDadosLog(ex);
		}
//		try {
//        	log.salvarDadosLog("TP_Map "+medQuaTopgProduto.getMapQuality("TP_Map"));
//		} catch (Exception ex) {
//			log.salvarDadosLog(ex);
//		}
//		try {
//			 log.salvarDadosLog("TW_Map "+medQuaVisinhaca.getMapQuality("TW_Map|5"));
//		} catch (Exception ex) {
//			log.salvarDadosLog(ex);
//		}
//		try {
//        	log.salvarDadosLog("MQE_Map "+medQuaDistorcao.getMapQuality("Dist_Map"));
//		} catch (Exception ex) {
//			log.salvarDadosLog(ex);
//		}
		
		mq.setErroQuantizacao(capturarErroMedioQuantizacao());

		ObjectOutputStream oos = null;
		try {
			FileOutputStream fos = MTDArquivoEnum.TREINO_MEDIDA_QUALIDADE.getFileOutputStream(false);
			oos = new ObjectOutputStream(fos);
			oos.writeObject(mq);
			oos.flush();
		} catch (Exception e) {
			log.salvarDadosLog(e);
		} finally {
			if (oos != null) {
				try {
					oos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		log.salvarDadosLog("Fim da geração de medidas de qualidade da rede neural...");
	}
}