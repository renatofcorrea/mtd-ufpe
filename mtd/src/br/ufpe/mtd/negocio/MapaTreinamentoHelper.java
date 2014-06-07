package br.ufpe.mtd.negocio;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.StringTokenizer;
import java.util.Vector;

import br.ufpe.mtd.entidade.Mapa;
import br.ufpe.mtd.entidade.PalavrasNodo;
import br.ufpe.mtd.enumerado.AreaCNPQEnum;
import br.ufpe.mtd.enumerado.MTDArquivoEnum;
import br.ufpe.mtd.util.MTDUtil;

/**
 * Servlet que fornece acesso a estruturas de dados uteis na criacao do mapa de
 * documentos e na atribuicao do nodo onde se encontra cada documento
 * 
 * @author Bruno
 */
public class MapaTreinamentoHelper {

	private HashMap<Integer, Vector<String>> nodoDocumento = null;
	private HashMap<Integer, PalavrasNodo> hashNodoPalavras = null;
	private Vector<String> palavras = new Vector<String>();
	private HashMap<String, Integer> documentoNodo = null;
	private Vector<String> areas = new Vector<String>();
	private HashMap<Integer, String> mapeamentoIdDocSiglaGrandeArea = new HashMap<Integer, String>();
	private LinkedHashMap<Integer, String> hashPalavras = new LinkedHashMap<Integer, String>();
	private ArrayList<Integer> nodosComDocumentos = new ArrayList<Integer>();
	private LinkedHashMap<Integer, String> hashNodoSigla = new LinkedHashMap<Integer, String>();
	private LinkedHashMap<Integer, ArrayList<String>> hashNodoDocumentos = new LinkedHashMap<Integer, ArrayList<String>>();// nodoDocumento
	private LinkedHashMap<Integer, ArrayList<Double>> hashNodoPesos = new LinkedHashMap<Integer, ArrayList<Double>>();
	private Mapa mapa;

	public void treinarMapa() throws IOException, ClassNotFoundException {
		mapa = new Mapa();
		File fileMapa = MTDArquivoEnum.TREINO_MAPA_DATA.getArquivo();
		if (fileMapa.length() == 0) {
			FileOutputStream fos = new FileOutputStream(fileMapa);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(mapa);
			oos.close();
			fos.close();
		}

		FileInputStream fis;
		Object obj = null;
		fis = new FileInputStream(fileMapa);
		ObjectInputStream obj_in = new ObjectInputStream(fis);
		obj = obj_in.readObject();
		mapa = (Mapa) obj;

		gerarHashNodoDocumentos();
		imprimirDocFNodeDoc();
		lerArquivoDocFNodeDoc();
		gerarHashPalavras();
		imprimirArquivoFWord();
		lerArquivoFword();
		gerarMapeamentoIdDocSiglaGrandeArea();
		gerarHashNodoAreaPredominante();
		avaliarAcuraciaTreinamento();
		imprimirArquivoNodoGrandeArea();
		carregarAreasDosNodos();
		lerArquivoDePesos();
		imprimirArquivoDocFCodeBook();
		lerDocFCodeBook();

		FileOutputStream f_out = new FileOutputStream(MTDArquivoEnum.TREINO_MAPA_DATA.getArquivo());
		ObjectOutputStream obj_out = new ObjectOutputStream(f_out);
		obj_out.writeObject(mapa);
	}

	private void gerarHashNodoDocumentos() throws IOException {

		File arquivoCompactado = MTDArquivoEnum.TREINO_UNIT.getArquivo();
		File arquivoDescompactado = new File(arquivoCompactado.getAbsolutePath().replace(".gz", ""));
		MTDUtil.descompactarGZFile(arquivoCompactado, arquivoDescompactado);

		String arquivoJavaSomUnit = arquivoDescompactado.getAbsolutePath();
		File fileUnit = new File(arquivoJavaSomUnit);
		FileReader readerUnit;
		readerUnit = new FileReader(fileUnit);
		BufferedReader bufferUnit = new BufferedReader(readerUnit);

		int x = 0;
		int y = 0;
		int nodo = 1;
		while (bufferUnit.ready()) {
			String[] linha = bufferUnit.readLine().split(" ");
			ArrayList<String> docsMapeados = new ArrayList<String>();

			if (linha[0].equals("$POS_X")) {
				x = Integer.parseInt(linha[1]);
				linha = bufferUnit.readLine().split(" ");

				if (linha[0].equals("$POS_Y")) {
					y = Integer.parseInt(linha[1]);
				}
				int qtdDocsMapeados = 0;
				while (bufferUnit.ready()) {
					linha = bufferUnit.readLine().split(" ");
					if (linha[0].equals("$NR_VEC_MAPPED")) {
						qtdDocsMapeados = Integer.parseInt(linha[1]);
						break;
					}
				}
				if (qtdDocsMapeados != 0) {
					bufferUnit.readLine(); // le a linha $MAPPED_VECS
					while (bufferUnit.ready()) {
						// Le a linha comecando com os codigos dos documentos mapeados
						linha = bufferUnit.readLine().split(" ");
						if (linha[0].equals("$MAPPED_VECS_DIST")) { // fim dos documentos mapeados
							break;
						} else {
							docsMapeados.add(linha[0]);
						}
					}
					hashNodoDocumentos.put(nodo, docsMapeados);
					nodosComDocumentos.add(nodo);
				}
				nodo++;
			}
		}
		bufferUnit.close();
	}

	// Bloco responsavel por imprimir o arquivo docfnodedoc.csv
	private void imprimirDocFNodeDoc() throws IOException {

		String arquivoTuplasNodoDocumento = "docfnodedoc.csv";
		File outFileNodoDoc = new File(MTDArquivoEnum.PASTA_TREINO.getArquivo(), arquivoTuplasNodoDocumento);
		String linhaDocfnodedoc;
		FileWriter fwNodoDoc = new FileWriter(outFileNodoDoc);
		PrintWriter pwNodoDoc = new PrintWriter(fwNodoDoc);
		for (Integer nodo : hashNodoDocumentos.keySet()) {
			ArrayList<String> docs = hashNodoDocumentos.get(nodo);
			for (int i = 0; i < docs.size(); i++) {
				linhaDocfnodedoc = "";
				linhaDocfnodedoc = "" + nodo + " " + docs.get(i);
				pwNodoDoc.println(linhaDocfnodedoc);
			}
		}
		System.out.println("O arquivo: \"" + arquivoTuplasNodoDocumento + "\" foi gerado com sucesso!");
		fwNodoDoc.close();
	}

	private void lerArquivoDocFNodeDoc() throws IOException {
		FileReader fileReaderDocfnodedoc;
		File arquivo = new File(MTDArquivoEnum.PASTA_TREINO.getArquivo(), "docfnodedoc.csv");
		fileReaderDocfnodedoc = new FileReader(arquivo);
		BufferedReader leitorDocfnodedoc = new BufferedReader(fileReaderDocfnodedoc);

		documentoNodo = new HashMap<String, Integer>();
		nodoDocumento = new HashMap<Integer, Vector<String>>();
		Vector<String> vetor;
		String[] valoresLidos;
		String idDocumento;
		String leituraLinha;
		String linhaSemEspacos = null;
		do {

			leituraLinha = leitorDocfnodedoc.readLine();
			if (leituraLinha != null) {
				linhaSemEspacos = leituraLinha.trim();
				valoresLidos = linhaSemEspacos.split(" ");
				int idNodo = Integer.parseInt(valoresLidos[0]);
				vetor = nodoDocumento.get(idNodo);
				if (vetor == null) {
					vetor = new Vector<String>();
				}
				idDocumento = valoresLidos[valoresLidos.length - 1];
				vetor.add(idDocumento);
				documentoNodo.put(idDocumento, idNodo);
				nodoDocumento.put(idNodo, vetor);
			} else {
				linhaSemEspacos = leituraLinha;
			}

		} while (linhaSemEspacos != null);

		mapa.setDocumentoNodo(documentoNodo);
		mapa.setNodoDocumento(nodoDocumento);
	}

	// Bloco responsavel por ler o arquivo que contem todas as palavras
	// selecionadas e gerar o hashPalavras
	private void gerarHashPalavras() throws IOException {
		File fileWordTable = MTDArquivoEnum.WORD_TABLE.getArquivo();
		FileReader readerWordTable;

		readerWordTable = new FileReader(fileWordTable);
		BufferedReader bufferWordTable = new BufferedReader(readerWordTable);
		while (bufferWordTable.ready()) {
			String[] linha = bufferWordTable.readLine().split(" ");
			hashPalavras.put(Integer.parseInt(linha[0]), linha[1]);
		}
		bufferWordTable.close();
	}

	private void imprimirArquivoFWord() throws IOException {

		String arquivoMapeamentoIdPalavraDescPalavra = "fword.csv";
		File outFileFword = new File(MTDArquivoEnum.PASTA_TREINO.getArquivo(), arquivoMapeamentoIdPalavraDescPalavra);
		String linha1;
		FileWriter fwFword = new FileWriter(outFileFword);
		PrintWriter pwFword = new PrintWriter(fwFword);

		for (Integer idPalavra : hashPalavras.keySet()) {
			String descricaoPalavra = hashPalavras.get(idPalavra);
			int novoIndice = idPalavra + 1;
			linha1 = "" + novoIndice + " " + descricaoPalavra;
			pwFword.println(linha1);
		}
		fwFword.close();
	}

	// Bloco responsavel por ler o arquivo "fword.csv"
	private void lerArquivoFword() throws IOException {
		FileReader fileReaderFword;
		File arquivo = new File(MTDArquivoEnum.PASTA_TREINO.getArquivo(), "fword.csv");
		fileReaderFword = new FileReader(arquivo);
		BufferedReader leitorFword = new BufferedReader(fileReaderFword);
		String[] valores;
		String linhaLida;
		String linhaComTrim = null;
		int t = 0;
		do {
			palavras.add(linhaComTrim);
			linhaLida = leitorFword.readLine();
			if (linhaLida != null) {
				linhaComTrim = linhaLida.trim();
				valores = linhaComTrim.split(" ");
				linhaComTrim = valores[valores.length - 1];
			} else {
				linhaComTrim = linhaLida;
			}
		} while (linhaComTrim != null);

		mapa.setPalavras(palavras);
	}

	private void gerarMapeamentoIdDocSiglaGrandeArea() throws IOException {
		File fileDocTable = MTDArquivoEnum.DOC_TABLE.getArquivo();
		FileReader readerDocTable;
		readerDocTable = new FileReader(fileDocTable);
		BufferedReader bufferDocTable = new BufferedReader(readerDocTable);

		while (bufferDocTable.ready()) {
			String[] linha = bufferDocTable.readLine().split(";");
			if (linha.length > 2) {
				mapeamentoIdDocSiglaGrandeArea.put(Integer.parseInt(linha[0]), linha[2].trim());
			}
		}
		bufferDocTable.close();
	}

	private void gerarHashNodoAreaPredominante() {

		for (int i = 0; i < nodosComDocumentos.size(); i++) {
			int nrNodo = nodosComDocumentos.get(i);
			ArrayList<String> documentosMapeados = new ArrayList<String>();
			documentosMapeados = hashNodoDocumentos.get(nrNodo);
			int arrayContagemSiglas[] = new int[4];

			for (int j = 0; j < documentosMapeados.size(); j++) {
				int idDoc = Integer.parseInt(documentosMapeados.get(j));
				String siglaDoc = mapeamentoIdDocSiglaGrandeArea.get(idDoc);

				// CHLA, CBS, TCEN, SEM_SIGLA
				if (siglaDoc.equals(AreaCNPQEnum.CHLA.name())) {
					arrayContagemSiglas[0] = arrayContagemSiglas[0] + 1;
				} else if (siglaDoc.equals(AreaCNPQEnum.CBS.name())) {
					arrayContagemSiglas[1] = arrayContagemSiglas[1] + 1;
				} else if (siglaDoc.equals(AreaCNPQEnum.TCEN.name())) {
					arrayContagemSiglas[2] = arrayContagemSiglas[2] + 1;
				} else {
					arrayContagemSiglas[3] = arrayContagemSiglas[3] + 1;
				}
			}
			int maiorQuantidade = 0;
			int indexSigla = 0;
			for (int k = 0; k < arrayContagemSiglas.length; k++) {
				if (arrayContagemSiglas[k] > maiorQuantidade) {
					maiorQuantidade = arrayContagemSiglas[k];
					indexSigla = k;
				}
			}
			if (indexSigla == 0) {
				hashNodoSigla.put(nrNodo, AreaCNPQEnum.CHLA.name());
			} else if (indexSigla == 1) {
				hashNodoSigla.put(nrNodo, AreaCNPQEnum.CBS.name());
			} else if (indexSigla == 2) {
				hashNodoSigla.put(nrNodo, AreaCNPQEnum.TCEN.name());
			} else {
				hashNodoSigla.put(nrNodo, AreaCNPQEnum.NAO_ENCONTRADO.name());
			}
		}
	}

	private void avaliarAcuraciaTreinamento() {
		double qtdTotaldeDocsNosNodos = 0;
		double qtdDocumentosAssociadosCorretamente = 0;
		for (int i = 0; i < nodosComDocumentos.size(); i++) {
			int nrNodo = nodosComDocumentos.get(i);
			ArrayList<String> documentosMapeados = new ArrayList<String>();
			documentosMapeados = hashNodoDocumentos.get(nrNodo);
			qtdTotaldeDocsNosNodos = qtdTotaldeDocsNosNodos + documentosMapeados.size();

			int arrayContagemSiglas[] = new int[4];

			for (int j = 0; j < documentosMapeados.size(); j++) {
				int idDoc = Integer.parseInt(documentosMapeados.get(j));
				String siglaDoc = mapeamentoIdDocSiglaGrandeArea.get(idDoc);

				// CHLA, CBS, TCEN, SEM_SIGLA
				if (siglaDoc.equals(AreaCNPQEnum.CHLA.name())) {
					arrayContagemSiglas[0] = arrayContagemSiglas[0] + 1;
				} else if (siglaDoc.equals(AreaCNPQEnum.CBS.name())) {
					arrayContagemSiglas[1] = arrayContagemSiglas[1] + 1;
				} else if (siglaDoc.equals(AreaCNPQEnum.TCEN.name())) {
					arrayContagemSiglas[2] = arrayContagemSiglas[2] + 1;
				} else {
					arrayContagemSiglas[3] = arrayContagemSiglas[3] + 1;
				}
			}
			int maiorQuantidade = 0;
			int indexSigla = 0;
			for (int k = 0; k < arrayContagemSiglas.length; k++) {
				if (arrayContagemSiglas[k] > maiorQuantidade) {
					maiorQuantidade = arrayContagemSiglas[k];
					indexSigla = k;
				}
			}
			if (indexSigla == 0) {
				hashNodoSigla.put(nrNodo, AreaCNPQEnum.CHLA.name());
			} else if (indexSigla == 1) {
				hashNodoSigla.put(nrNodo, AreaCNPQEnum.CBS.name());
			} else if (indexSigla == 2) {
				hashNodoSigla.put(nrNodo, AreaCNPQEnum.TCEN.name());
			} else {
				hashNodoSigla.put(nrNodo, AreaCNPQEnum.NAO_ENCONTRADO.name());
			}

			qtdDocumentosAssociadosCorretamente = qtdDocumentosAssociadosCorretamente + maiorQuantidade;
		}

		System.out.println("Docs Mapeados Corretamente / Total Docs: " + qtdDocumentosAssociadosCorretamente / qtdTotaldeDocsNosNodos);
	}

	private void imprimirArquivoNodoGrandeArea() throws IOException {
		String arquivoNodega = "nodega.txt";
		File outFileNodega = new File(MTDArquivoEnum.PASTA_TREINO.getArquivo(), arquivoNodega);
		FileWriter fwNodega = new FileWriter(outFileNodega);
		PrintWriter pwNodega = new PrintWriter(fwNodega);
		for (Integer nodo : hashNodoSigla.keySet()) {
			String sigla = hashNodoSigla.get(nodo);
			pwNodega.println(sigla);
		}
		System.out.println("O arquivo: \"" + arquivoNodega + "\" foi gerado com sucesso!");
		fwNodega.close();
	}

	// Faz a leitura do arquivo nodega.txt e carrega a variavel "areas".
	private void carregarAreasDosNodos() throws IOException {
		Vector<String> gar = new Vector<String>(120);
		FileReader readerNodega;
		File arquivo = new File(MTDArquivoEnum.PASTA_TREINO.getArquivo(), "nodega.txt");
		readerNodega = new FileReader(arquivo);
		BufferedReader bufferNodega = new BufferedReader(readerNodega);
		int i = 0;
		String a = null;
		do {
			a = bufferNodega.readLine();
			if (a != null) {
				a = a.trim();
				gar.add(a);
			}
			i++;
		} while (a != null);
		areas = gar;
		mapa.setAreas(areas);
	}

	// Bloco responsavel por ler o arquivo .wgt gerado pelo JavaSOM
	private void lerArquivoDePesos() throws IOException {

		File arquivoCompactado = MTDArquivoEnum.TREINO_WGT.getArquivo();
		File arquivoDescompactado = new File(arquivoCompactado.getAbsolutePath().replace(".gz", ""));
		MTDUtil.descompactarGZFile(arquivoCompactado, arquivoDescompactado);

		FileReader readerWgt;
		readerWgt = new FileReader(arquivoDescompactado);
		BufferedReader bufferWgt = new BufferedReader(readerWgt);
		int nodo = 1;

		while (bufferWgt.ready()) {
			String[] linha = bufferWgt.readLine().split(" ");
			if (linha[0].equals("$VEC_DIM")) {
				while (bufferWgt.ready()) {
					ArrayList<Double> pesosDoNodo = new ArrayList<Double>();					
					// Le linha comecando com os codigos dos documentos mapeados
					linha = bufferWgt.readLine().split(" ");
					for (int k = 0; k < linha.length - 1; k++) {
						pesosDoNodo.add(Double.parseDouble(linha[k]));
					}
					hashNodoPesos.put(nodo, pesosDoNodo);
					nodo++;
				}
			}
		}
		bufferWgt.close();
	}

	private void imprimirArquivoDocFCodeBook() throws IOException {

		String arquivoDocfcodebook = "docfcodebook.csv";
		File outFileDocfcodebook = new File(MTDArquivoEnum.PASTA_TREINO.getArquivo(), arquivoDocfcodebook);
		String linhaDocfcodebook;
		linhaDocfcodebook = "";
		FileWriter fw = new FileWriter(outFileDocfcodebook);
		PrintWriter pw = new PrintWriter(fw);
		for (Integer nodo : hashNodoPesos.keySet()) {
			ArrayList<Double> pesos = hashNodoPesos.get(nodo);
			for (int i = 1; i <= pesos.size(); i++) {
				linhaDocfcodebook = "" + nodo + " " + i + " " + pesos.get(i - 1);
				pw.println(linhaDocfcodebook);
			}
		}
		System.out.println("O arquivo: \"" + arquivoDocfcodebook + "\" foi gerado com sucesso!");
		fw.close();
	}

	private void lerDocFCodeBook() throws IOException {
		FileReader fileReaderDocfcodebook;
		File arquivo = new File(MTDArquivoEnum.PASTA_TREINO.getArquivo(), "docfcodebook.csv");
		fileReaderDocfcodebook = new FileReader(arquivo);
		BufferedReader leitorDocfcodebook = new BufferedReader(fileReaderDocfcodebook);

		hashNodoPalavras = new HashMap<Integer, PalavrasNodo>();
		StringTokenizer stringT;
		int idNodo;
		int idVirtualPalavra;
		float peso;
		PalavrasNodo palavrasNodo;
		int segundo;
		int terceiro;
		float pesoSegundo;
		float pesoTerceiro;
		String conteudoLinhaLida;
		String linhaAposTrim = null;

		do {
			conteudoLinhaLida = leitorDocfcodebook.readLine();
			System.out.println();
			if (conteudoLinhaLida != null) {
				linhaAposTrim = conteudoLinhaLida.trim();
				stringT = new StringTokenizer(linhaAposTrim, " ");

				idNodo = Integer.parseInt(stringT.nextToken());
				idVirtualPalavra = Integer.parseInt(stringT.nextToken());
				peso = Float.parseFloat(stringT.nextToken());

				palavrasNodo = hashNodoPalavras.get(idNodo);
				if (palavrasNodo == null) {
					palavrasNodo = new PalavrasNodo();
				}
				if (peso >= palavrasNodo.getPesoMaior()) {
					segundo = palavrasNodo.getMaior();
					terceiro = palavrasNodo.getMeio();
					pesoSegundo = palavrasNodo.getPesoMaior();
					pesoTerceiro = palavrasNodo.getPesoMedio();
					palavrasNodo.setMaior(idVirtualPalavra);
					palavrasNodo.setMeio(segundo);
					palavrasNodo.setMenor(terceiro);
					palavrasNodo.setPesoMaior(peso);
					palavrasNodo.setPesoMedio(pesoSegundo);
					palavrasNodo.setPesoMenor(pesoTerceiro);
				} else if (peso >= palavrasNodo.getPesoMedio()) {
					terceiro = palavrasNodo.getMeio();
					pesoTerceiro = palavrasNodo.getPesoMedio();
					palavrasNodo.setMeio(idVirtualPalavra);
					palavrasNodo.setPesoMedio(peso);
					palavrasNodo.setMenor(terceiro);
					palavrasNodo.setPesoMenor(pesoTerceiro);
				} else if (peso >= palavrasNodo.getPesoMenor()) {
					palavrasNodo.setMenor(idVirtualPalavra);
					palavrasNodo.setPesoMenor(peso);
				}
				hashNodoPalavras.put(idNodo, palavrasNodo);
			} else {
				linhaAposTrim = conteudoLinhaLida;
			}

		} while (linhaAposTrim != null);

		mapa.setHashNodoPalavras(hashNodoPalavras);
	}
}