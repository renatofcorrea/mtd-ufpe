package br.ufpe.mtd.negocio.controle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.lucene.document.Document;
import org.json.JSONObject;

import br.ufpe.mtd.dados.indice.RepositorioIndiceLucene;
import br.ufpe.mtd.negocio.entidade.MTDDocument;
import br.ufpe.mtd.negocio.entidade.Mapa;
import br.ufpe.mtd.util.MTDException;
import br.ufpe.mtd.util.MTDFactory;
import br.ufpe.mtd.util.MTDIterator;
import br.ufpe.mtd.util.MTDParametros;
import br.ufpe.mtd.util.MTDUtil;
import br.ufpe.mtd.util.enumerado.MTDArquivoEnum;
import br.ufpe.mtd.util.log.Log;

public class MTDFacede {
	
	public static void indexar() throws Exception{
		MTDFactory f = MTDFactory.getInstancia();
		Log log = f.getLog();
		log.salvarDadosLog("MTDFacede.indexar() ---------------Iniciando coleta e indexação-------------");
		
		long inicio = System.currentTimeMillis();
		try {
			IndiceControle controle = f.newControleIndice();
			
			String acesso = MTDParametros.acessoRepositorio();
			JSONObject j = new JSONObject(acesso);
			log.salvarDadosLog("Dados de repositorios para indexação : "+j.toString());
			
			Iterator<?> it = j.keys();
			while(it.hasNext()){
				log.salvarDadosLog("Aguardando para iniciar nova coleta...");
				Thread.sleep(5000);
				
				String repositorio = it.next().toString();
				log.salvarDadosLog("Iniciando coleta para repositorio : "+repositorio);
				
				JSONObject aux = j.getJSONObject(repositorio);
				String url = (String)aux.get("url"); 
				String protocolo = (String)aux.get("protocolo");
				String set = aux.has("set") ? (String)aux.get("set") : null;
				boolean executar = aux.has("executar") ? aux.getBoolean("executar") : false;
				
				if(executar){
					controle.indexar(url, protocolo,set);
				}else{
					log.salvarDadosLog("Repositorio configurado em properties para não executar!!!");
				}
				
				log.salvarDadosLog("Concluida coleta para repositorio : "+repositorio);
			}
			
		} catch (Exception e) {
			throw e;
		} finally{
			f.resetarRepositorios();
			MTDUtil.imprimirConsole("---------Tempo total da Indexação: "+ (System.currentTimeMillis() - inicio));
		}
	}
	
	public static void resetarRepositorios() throws Exception{
		MTDFactory f = MTDFactory.getInstancia();
		f.resetarRepositorios();
	}
	
	/**
	 * Realiza o treinamento ou retreino da rede neural, e logo apos o treino do mapa.
	 * @throws Exception
	 */
	public static void realizarTreinamento() throws Exception{
		MTDFactory f = MTDFactory.getInstancia();
		Log log = f.getLog();
		if(!isSistemaTreinado()){
			MapaControle mControle = f.newMapaControle();
			RedeNeuralControle rnControle = f.newRedeNeuralControle();
			rnControle.treinarRedeNeural();
			mControle.treinarMapa(); 
		}else{
			RedeNeuralControle rnControle = new RedeNeuralControle(log);
			rnControle.retreinarRedeNeural();
		}
	}
	
	
	
	public static void gerarMedidasQualidadeRedeNeural() throws Exception{
		MTDFactory f = MTDFactory.getInstancia();
		RedeNeuralControle rnControle = f.newRedeNeuralControle();
		rnControle.gerarMedidasQualidadeRedeNeural();
	}	
	
	public static boolean isMedidasQualidadeAdequadas() throws Exception{
		return RedeNeuralControle.isMedidasQualidadeAdequadas();
	}
	
	public static boolean isSintagmasConcluidos() throws Exception{
		MTDFactory f = MTDFactory.getInstancia();
		
		RepositorioIndiceLucene rep = ((RepositorioIndiceLucene)f.getSingleRepositorioIndice());
		int qtdIndice = rep.getQuantidadeDocumentosNoIndice();
		
		rep = ((RepositorioIndiceLucene)f.getSingleRepositorioSintagmas());
		int qtdIndiceSintagmas = rep.getQuantidadeDocumentosNoIndice();
		
		return (qtdIndice == qtdIndiceSintagmas);
	}
	
	public static boolean isSistemaTreinado() throws Exception{
		MTDFactory f = MTDFactory.getInstancia();
		MapaControle mControle = f.newMapaControle();
		
		boolean mapaTreinado = mControle.isMapaTreinado();
		
		//TODO: Erro de classificacao; numero de docs diferentes das areas dos nodos / total de docs. aceitavel abaixo 40 %
		
		boolean medidasQualidadeAdequadas = RedeNeuralControle.isMedidasQualidadeAdequadas();
		
		return mapaTreinado && medidasQualidadeAdequadas;
	}
	
	public static Mapa recuperarMapa() throws Exception{
		if(!isSistemaTreinado()){
			throw new MTDException(false,"Sistema em manutenção. Rede neural está sendo treinada. Mapa está sendo gerado.");
		}
		MTDFactory f = MTDFactory.getInstancia();
		Mapa mapa = f.newMapaControle().getMapa();
		return mapa;
	}
	
	
	public static void salvarDadosIndiceSintagmas() throws Exception{
		MTDFactory f = MTDFactory.getInstancia();
		//Carregando stoplist de sns
		MTDIterator<String> it = MTDArquivoEnum.J_OGMA_STOP_LIST.lineIterator();
		HashSet<String> hs = new HashSet<String>();
		while(it.hasNext()){
			hs.add(it.next());
		}
		it.close();
		
		MTDIterator<MTDDocument> iterator =null;
		try {
			RepositorioIndiceLucene repSintagmas = (RepositorioIndiceLucene)f.getSingleRepositorioSintagmas();
			List<String> idsJaSalvos = repSintagmas.getIdsTodosDocumentos();
			RepositorioIndiceLucene rep = (RepositorioIndiceLucene)f.getSingleRepositorioIndice();
			int contador = 0;
			int qtdGerados = 0;
			iterator = rep.iterator();
			ArrayList<Document> docs = new ArrayList<Document>();
			while(iterator.hasNext()){
				MTDDocument doc = iterator.next();
				MTDUtil.imprimirConsole(doc.getId()+" Qtd : "+contador);
				
				if(!idsJaSalvos.contains(doc.getId())){
					docs.add( doc.toDocumentComSintagmas(hs));
					repSintagmas.inserirDocumento(docs);
					docs.clear();
					qtdGerados++;
					
					if(qtdGerados == 3000){//resetar processo. tratamento bug sintagmas ficam lentos apos 1000 registros gerados.
						//throw new MTDException(false, "resetando geração de sintagmas para melhorar performance.");
						break;
					}
					
				}else{
					MTDUtil.imprimirConsole("Abortando salvamento, Doc já salvo antes: "+doc.getId());
				}
				
				contador++;
			}
			
			
			MTDUtil.imprimirConsole("Concluido!!!"+contador);
		} catch (Exception e) {
			throw e;
		}finally{
			if(iterator != null){
				iterator.close();
			}
			f.resetarRepositorios();
		}
	}
	
	
	public static String sugestaoPadrao(){
		return SuggesterControle.getSugestaoPadrao();
	}
	
	public static String sugestaoSintagma(){
		return SuggesterControle.getSugestaoSintagma();
	}
	
	public static String getTipoSugestao(String aux){
		return SuggesterControle.getTipoSugestao(aux);
	}
	
	public static Collection<String> buscarSugestoes(String termo, String tipoSugestao) throws Exception{
		SuggesterControle suggester = new SuggesterControle(tipoSugestao, MTDFactory.getInstancia());
		Collection<String> sugestoes = suggester.lookup(termo);
		return sugestoes;
	}
}
