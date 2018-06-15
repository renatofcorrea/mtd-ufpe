package br.ufpe.mtd.negocio.controle;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import br.ufpe.mtd.dados.arquivo.RepositorioMapa;
import br.ufpe.mtd.dados.indice.RepositorioIndiceLucene;
import br.ufpe.mtd.negocio.entidade.MTDDocument;
import br.ufpe.mtd.negocio.entidade.MTDDocumentBuilder;
import br.ufpe.mtd.negocio.entidade.Mapa;
import br.ufpe.mtd.negocio.entidade.Nodo;
import br.ufpe.mtd.negocio.entidade.Palavra;
import br.ufpe.mtd.negocio.entidade.PesoPalavra;
import br.ufpe.mtd.util.MTDFactory;
import br.ufpe.mtd.util.MTDIterator;
import br.ufpe.mtd.util.MTDUtil;
import br.ufpe.mtd.util.enumerado.AreaCNPQEnum;
import br.ufpe.mtd.util.enumerado.MTDArquivoEnum;
import br.ufpe.mtd.util.log.Log;

public class MapaControle {
	
	private Log log;
	RepositorioMapa rep;
	
	
	public MapaControle(MTDFactory fabrica) throws Exception {
		this.rep = fabrica.getSingleRepositorioMapa();
		this.log = fabrica.getLog();
	}
	
	public void treinarMapa() throws Exception{
		Mapa mapa = rep.getMapa();
		carregarPalavras(mapa);
		List<Nodo> nodos = carregarNodos();
		mapa.setNodos(nodos);
		mapa.carregarDocumentos();
		carregarAreas(mapa);
		atualizarPesos(mapa);
		salvaMapa(mapa);
	}
	
	public synchronized Mapa getMapa() throws Exception{
		Mapa mapa = rep.getMapa();
		return mapa;
	}
	
	
	private void salvaMapa(Mapa mapa) throws IOException{
		rep.salvaMapa(mapa);
	}
	
	public boolean isMapaTreinado() throws Exception{
		Mapa mapa = getMapa();
		if(mapa == null || mapa.getNodos() == null || mapa.getDocumentos().size() < (RedeNeuralControle.MAPA_X_SIZE * RedeNeuralControle.MAPA_Y_SIZE)|| mapa.numDocumentos() < MTDFactory.getInstancia().getSingleRepositorioIndice().getQuantidadeDocumentosNoIndice()){
			return false;
		}
		return true;
	}
		
	private void carregarPalavras(Mapa mapa) throws Exception {
		MTDArquivoEnum enumerado = MTDArquivoEnum.TEMPLATE_TREINAMENTO_NORM;
		MTDIterator<String> it = enumerado.lineIterator();
		boolean podeLer = false;
		while (it.hasNext()) {
			String[] linha = it.next().split(" ");
			if(linha[0].equals("$VEC_DIM")){
				podeLer = true;
				continue;
			}
			if(podeLer){
				Palavra palavra = new Palavra(Integer.parseInt(linha[0]), linha[1]);
				mapa.addPalavra(palavra);
			}
		}
		it.close();
	}
	
	private void carregarAreas(Mapa mapa) throws Exception {
		MTDArquivoEnum enumerado = MTDArquivoEnum.CLS_GRANDE_AREA;
		MTDIterator<String> it = enumerado.lineIterator();
		while (it.hasNext()) {
			String[] linha = it.next().split("\t");
			int docId = Integer.parseInt(linha[0]);
			MTDDocument doc = mapa.getDocumento(docId);
			AreaCNPQEnum area = AreaCNPQEnum.getGrandeAreaCNPQPorNome(linha[1]);
			doc.setAreaCNPQ(area.name());
		}
		
		System.out.println("Areas carregadas ");
		it.close();
	}

	private List<Nodo> carregarNodos() throws IOException {
		List<Nodo> nodos = new ArrayList<>();
		File arquivoCompactado = MTDArquivoEnum.TREINO_UNIT.getArquivo();
		System.out.println("Open RNA file: "+arquivoCompactado.getAbsolutePath());
		File arquivoDescompactado = new File(arquivoCompactado.getAbsolutePath().replace(".gz", ""));
		MTDUtil.descompactarGZFile(arquivoCompactado, arquivoDescompactado);
		FileReader readerUnit = new FileReader(arquivoDescompactado);
		BufferedReader bufferUnit = new BufferedReader(readerUnit);

		int nodoId = 1;
		while (bufferUnit.ready()) {
			String[] linha = bufferUnit.readLine().split(" ");

			if (linha[0].equals("$POS_X")) {
				linha = bufferUnit.readLine().split(" ");
				int qtdDocsMapeados = 0;
				while (bufferUnit.ready()) {
					linha = bufferUnit.readLine().split(" ");
					if (linha[0].equals("$NR_VEC_MAPPED")) {
						qtdDocsMapeados = Integer.parseInt(linha[1]);
						break;
					}
				}
				
				Nodo nodo = new Nodo();
				nodo.setId(nodoId);
				nodos.add(nodo);
				nodoId++;
				
				if (qtdDocsMapeados != 0) {
					bufferUnit.readLine(); // le a linha $MAPPED_VECS
					while (bufferUnit.ready()) {
						// Le a linha comecando com os codigos dos documentos mapeados
						linha = bufferUnit.readLine().split(" ");
						if (linha[0].equals("$MAPPED_VECS_DIST")) { // fim dos documentos mapeados
							break;
						} else {
							MTDDocument doc = new MTDDocumentBuilder().buildDocument().build();
							doc.setDocId(Integer.parseInt(linha[0]));
							nodo.addDocumento(doc);
							doc.setNodo(nodo);
						}
					}
				}
			}
		}
		
		bufferUnit.close();
		
		return nodos;
	}
	
	private void atualizarPesos(Mapa mapa) throws IOException {

		File arquivoCompactado = MTDArquivoEnum.TREINO_WGT_GZ.getArquivo();
		File arquivoDescompactado = new File(arquivoCompactado.getAbsolutePath().replace(".gz", ""));
		MTDUtil.descompactarGZFile(arquivoCompactado, arquivoDescompactado);

		FileReader readerWgt;
		readerWgt = new FileReader(arquivoDescompactado);
		BufferedReader bufferWgt = new BufferedReader(readerWgt);
		
		int nodo = 1;
		boolean podeLerPesos = false;
		
		while (bufferWgt.ready()) {
			String[] linha = bufferWgt.readLine().split(" ");
			if (linha[0].equals("$VEC_DIM")) {
				podeLerPesos = true;
				continue;//pula para a proxima linha
			}
			if(podeLerPesos){
				ArrayList<PesoPalavra> pesosDoNodo = new ArrayList<>();
				for (int k = 0; k < linha.length - 1; k++) {
					PesoPalavra pp = new PesoPalavra(Double.parseDouble(linha[k]), k);
					Palavra aux = mapa.getPalavra(pp.getIdPalavra());
					pp.setPalavra(aux);
					pesosDoNodo = atualizarMaioresPesos(pesosDoNodo, pp);
				}
				Nodo aux = mapa.getNodo(nodo);
				if(aux != null){
					mapa.getNodo(nodo).setMaioresPesos(pesosDoNodo);
				}else{
					log.salvarDadosLog(new Exception("Nodo de id: "+nodo+" Não existe..."));
				}
				nodo++;
			}
		}
		
		bufferWgt.close();
	}
	
	private ArrayList<PesoPalavra> atualizarMaioresPesos(ArrayList<PesoPalavra> pesos, PesoPalavra novoItem){
		//adiciona item
		pesos.add(novoItem);

		//ordena
		Collections.sort(pesos, new Comparator<PesoPalavra>() {
			@Override
			public int compare(PesoPalavra o1, PesoPalavra o2) {
				return (-1)*(o1.getPeso().compareTo(o2.getPeso()));//ordem descendente
			}
		});
		
		//retira excedentes
		if(pesos.size() > 3){
			for(int i = 3; i < pesos.size(); i++){
				pesos.remove(i);
			}
		}
		
		//seta novo indice para cada posicao
		int i = 0;
		for(PesoPalavra pp : pesos){
			pp.setOrdemPesoPalavra(++i);
		}
		return pesos;
	}
}