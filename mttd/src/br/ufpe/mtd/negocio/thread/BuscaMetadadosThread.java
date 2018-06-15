package br.ufpe.mtd.negocio.thread;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import org.apache.lucene.document.Document;

import br.ufpe.mtd.dados.drive.OAIPMHDriver;
import br.ufpe.mtd.dados.indice.IRepositorioIndice;
import br.ufpe.mtd.dados.indice.RepositorioIndiceLucene;
import br.ufpe.mtd.negocio.decodificacao.DecodificadorDocumento;
import br.ufpe.mtd.negocio.decodificacao.MTDDecodificadorDocumentoBuilder;
import br.ufpe.mtd.negocio.entidade.Identificador;
import br.ufpe.mtd.negocio.entidade.MTDDocument;
import br.ufpe.mtd.util.MTDException;
import br.ufpe.mtd.util.MTDFactory;
import br.ufpe.mtd.util.MTDParametros;
import br.ufpe.mtd.util.log.Log;

/**
 * Thread responsavel de trazer dados de registros
 * existentes nos repositorios externos a aplicacao.
 * 
 * Como se trata de uma acao custosa sera executada em paralelo
 * e colocado em uma fila de dados a serem salvos.
 * 
 * @author djalma
 *
 */
public class BuscaMetadadosThread extends BaseThread{
	
	private static Monitor monitor = new Monitor();
	private IRepositorioIndice repositorio;
	private List<Identificador> identificadores;
	private String urlBase;
	private String metaDataPrefix;
	private int threadId;
	private Log log;
	
	/**
	 * Estes objetos não devem ser criados de forma concorrente,
	 * mas em sequencia para que a ordem de salvamento dos dados seja garantida.
	 * 
	 * @param repositorio
	 * @param identificadores
	 * @param urlBase
	 * @param metaDataPrefix
	 */
	public BuscaMetadadosThread(Log log, IRepositorioIndice repositorio, List<Identificador> identificadores, String urlBase, String metaDataPrefix) {
		this.threadId = monitor.getFilaId();
		monitor.setLog(log);
		this.log = log;
		this.identificadores = identificadores;
		this.repositorio = repositorio;
		this.urlBase = urlBase;
		this.metaDataPrefix = metaDataPrefix;
	}
	
	/**
	 * Execucao da busca de metadados.
	 * Busca a partir de uma lista de identificadores, os dados
	 * Disponiveis em um repositorio online referente as metainformacoes associadas a aqueles
	 * identificadores. Esses dados sao colocados em forma de MTDDocument e 
	 * entao salvos no indice. 
	 * 
	 * verifica no indice se ja existem documentos salvos para os identificadores informados,
	 * nao baixa os dados em caso afirmativo para os identificadores repetidos.
	 * 
	 */
	@Override
	public void execucao() {
		String idsToString = recuperarComoString(identificadores);
		try {			
			identificadores = removerExistentes(identificadores);
			List<Document> docs = colherMetadadosOnline(identificadores, urlBase, metaDataPrefix);
			inserirDocsOrdenados(docs);
			
			//formatar mensagem
			String ids = "";
			for(Document aux : docs){
				ids+= aux.get(MTDDocument.ID)+",";
			}
			ids = ids.isEmpty() ? "" : ids.substring(0, ids.lastIndexOf(","));
			if(docs.size() > 0)
			log.salvarDadosLog("BuscaMetadadosThread.execucao() Inseridos no índice "+docs.size()+ " documentos: {"+ids+"}");
		
		} catch (Exception e) {
			MTDException excecao = new MTDException(e, "Problemas ao tentar inserir "+idsToString);
			log.salvarDadosLog(excecao);
		}finally{
			
		}
	}
	
	/*
	 * Salva no log os ids que foram inseridos apos a coleta dos metadados.
	 */
	private String recuperarComoString(List<Identificador> identificadores){
		
		StringBuffer strIds = new StringBuffer("ids:{");
		Identificador identificador = null;
		for (int i= 0 ; i < identificadores.size(); i++) {
			
			identificador = identificadores.get(i);
			strIds.append(identificador.getId());
			
			if(i != identificadores.size() - 1){
				strIds.append(",");
			}
		}
		
		strIds.append("}");
		
		
		return strIds.toString();
	}
	
	/*
	 * Remoção de dados que já estão salvos no indice.
	 * Pode ser que a acao de indexar em operacoes anteriores tenha
	 * falhado por algum motivo.
	 * 
	 */
	private List<Identificador>  removerExistentes(List<Identificador> docs) throws Exception{
		if(repositorio instanceof RepositorioIndiceLucene){
			List<String> todosDosIds = ((RepositorioIndiceLucene)repositorio).getIdsTodosDocumentos();
			
			List<Identificador> docsRemover = new ArrayList<Identificador>();
			
			for(Identificador doc: docs){
				for(String id: todosDosIds){
					if(id.equals(doc.getId())){
						docsRemover.add(doc);
						break;
					}
				}
			}
			docs.removeAll(docsRemover);
			
//			if(!docsRemover.isEmpty()){
//				String ids = recuperarComoString(docsRemover);
//				MTDFactory.getInstancia().getLog().salvarDadosLog("BuscaMetadadosThread.removerExistentes() Documentos ja indexados e não inseridos: "+ids);
//			}
		}
		
		return docs;
	}
	
	/**
	 * Realiza a busca de metadados dos documentos no repositorio mtd2-br
	 * corrente a partir da lista de identificadores passada como parametro.
	 * 
	 * @param identificadores
	 * @param urlBase
	 * @param metaDataPrefix
	 * @return
	 * @throws Exception 
	 */
	public List<Document> colherMetadadosOnline(List<Identificador> identificadores,String urlBase,String metaDataPrefix) throws Exception {
		
		List<Identificador> listaRetentativa = new ArrayList<Identificador>();//verificar se tem instabilidadee no jCoollTraine
		OAIPMHDriver driver = OAIPMHDriver.getInstance(urlBase,metaDataPrefix);
		DecodificadorDocumento decodificador = new MTDDecodificadorDocumentoBuilder().buildDecodificador(metaDataPrefix).build();
			
		String url = null;
		Log log = MTDFactory.getInstancia().getLog();

		long qtd = 0; 
		long tamanho = identificadores.size();
		
		//baixar dados normalmente.
		for (Identificador identificador : identificadores) {
			
			//log.salvarDadosLog("BuscaMetadadosThread.colherMetadadosOnline() "+ Thread.currentThread().getName()+" Buscando documento para identificador: ("+identificador.getId()+"),  registro "+(++qtd) + " De "+tamanho);			
			url = driver.getRecord(metaDataPrefix, identificador.getId());//busca os dados online			
			InputStream is = driver.getResponse(url);
			
			try{
				DecodificadorDocumento.parse(is, decodificador, identificador);
				
			}catch(MTDException e ){
				Object o = e.getExtraData();
				if(o instanceof Identificador){
					listaRetentativa.add((Identificador)o);
				}
			}finally{
				if(is != null){
					is.close();
				}
			}
		}
		
		//Retentar para os casos onde teve falha.
		for (Identificador identificador : listaRetentativa) {
			int tentativas = 0;
			while(tentativas < MTDParametros.getNumMaxRetentativas()){
				InputStream is = null;
				try{
				
					log.salvarDadosLog(Thread.currentThread().getName()+" Retentando documento para identificador: ("+identificador.getId()+")");
					url = driver.getRecord(metaDataPrefix, identificador.getId());//busca os dados online
					is = driver.getResponse(url);
					DecodificadorDocumento.parse(is, decodificador, identificador);
					break;
					
				}catch(MTDException e ){
					tentativas++;
					Thread.sleep(1000);
				}finally{
					if(is != null){
						is.close();
					}
				}
			}
		}
		
		//tirar os repetidos caso existam. Usar set para isso.
		ArrayList<Document> docs = new ArrayList<Document>();
		TreeSet<MTDDocument> treeSetDocs = new TreeSet<MTDDocument>(decodificador.getDocumentos());
		
		for (MTDDocument documento : treeSetDocs) {
			docs.add(documento.toDocument());
		}
		
		return docs;
	}
	
	/**
	 * Executara a insercao dos docs respeitando a ordem de criacao das threads.
	 * 
	 * @param docs
	 * @param t
	 * @throws Exception 
	 */
	private void inserirDocsOrdenados(List<Document> docs) throws Exception{
		monitor.tentar(threadId);
		try {
			repositorio.inserirDocumento(docs);
			//log.salvarDadosLog("BuscaMetadadosThread.inserirDocsOrdenados() Concluida Thread de insercao numero de ordem : "+threadId);
		} catch (Exception e) {
			throw e;
		}finally{
			monitor.notificar();
		}
	}	
	
	
	//Controle de fila de insercao de dados no contexto concorrente.
	//Objetivo e garantir a mesma ordem sequncia dos documentos que existe no repositorio original.
	
	
	static class Monitor{
		private int filaId = 0;
		private int filaIdProcessado = 0;
		private Log log;
		
		private synchronized  void setLog(Log log) {
			if(this.log == null){
				this.log = log;
			}
		}
		
		//inicio controle concorrencia
		private synchronized  int getFilaId(){
			return ++filaId;
		}
		
		private synchronized boolean podeExecutar(int threadId){
			return threadId == filaIdProcessado + 1;
		}
		
		private synchronized void updateProcessados(){
			filaIdProcessado++;
		}
		
		/**
		 * Uma Thread de um determinado id tentara executar acao
		 * E so podera executa-la se for a sua vez. Basta chamar este metodo que
		 * a execucao sera controlada.
		 * 
		 * @param threadId
		 */
		private synchronized void tentar(int threadId){
			while(!podeExecutar(threadId)){
				try {
					//log.salvarDadosLog("BuscaMetadadosThread.tentar() Aguardando minha vez para inserir "+Thread.currentThread().getName()+" Ordem na fila "+threadId+" Processados: "+filaIdProcessado);
					wait();
				} catch (Exception e) {
					log.salvarDadosLog(e);
				}
			}	
		}
		
		private synchronized void notificar(){
			updateProcessados();
			notifyAll();
		}
	}
}