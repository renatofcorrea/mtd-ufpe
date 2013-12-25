package br.ufpe.mtd.negocio;

import java.util.ArrayList;
import java.util.List;
import br.ufpe.mtd.consulta.OAIPMHDriver;
import br.ufpe.mtd.dados.RepositorioIndice;
import br.ufpe.mtd.entidade.Identificador;
import br.ufpe.mtd.thread.BaseThread;
import br.ufpe.mtd.thread.ThreadBuscaMetadados;
import br.ufpe.mtd.util.MTDFactory;

public class ControleIndice {
	private final  int QTD_MAX_TENTATIVAS = 2;
	private RepositorioIndice repositorio;

	public ControleIndice(RepositorioIndice repositorio) {
		this.repositorio = repositorio;
	}
	
	public void indexar(String urlBase, String metaDataPrefix) throws Exception {
		int tentativas = 0;
		List<Identificador> dadosRecebidos = new ArrayList<Identificador>();
		List<Identificador> identificadores = null;
		List<Identificador> deletados = new ArrayList<Identificador>();
		OAIPMHDriver driver = new  OAIPMHDriver(urlBase, metaDataPrefix);
		
		while(driver.hasNext()){
			tentativas++;
			
			try{
				identificadores = driver.getNextIdentifiers();
				
				//tratamento pois dados de identificadores podem vir repetidos 
				//TODO: verificar se este comportamento de repticao pode ser evitado.
				if(identificadores.size() > 0 && !dadosRecebidos.contains(identificadores.get(0))){
					
					dadosRecebidos.addAll(identificadores);
					
					for (Identificador identificador : identificadores) {
						if(identificador.isDeletado()){
							deletados.add(identificador);
						}
					}
					
					for (Identificador identificador : deletados) {
						MTDFactory.getInstancia().getLog().salvarDadosLog("Deletado id : "+identificador.getId());
					}
					
					identificadores.removeAll(deletados);
					deletados.clear();
					baixarDocsEsalvar(repositorio, identificadores, urlBase, metaDataPrefix);
				}
				
				tentativas = 0;
				
			}catch(Exception e){
				
				if((e instanceof java.net.SocketTimeoutException  || e instanceof java.net.SocketException) &&
						tentativas > QTD_MAX_TENTATIVAS){
					MTDFactory.getInstancia().getLog().salvarDadosLog(e);
					throw e;
				}else{
					throw e;
				}
				
			}finally{
				otimizarIndice(repositorio);
			}
		}
	}
	
	/**
	 * Realiza a coleta registros de forma online
	 * em paralelo para agilizar a busca de informações.
	 * esta coleta sera enviada para o poll de Threads da aplicacao.
	 * @param repositorio
	 * @param identificadores
	 * @param urlBase
	 * @param metaDataPrefix
	 */
	private void baixarDocsEsalvar(RepositorioIndice repositorio, List<Identificador> identificadores,String urlBase,String metaDataPrefix){
		ThreadBuscaMetadados t = new ThreadBuscaMetadados(repositorio , identificadores,urlBase,metaDataPrefix);
		t.setPriority(Thread.MAX_PRIORITY);
		t.executarNoPool();
	}
	
	/*
	 * Netodo auxiliar que 
	 * Agenda a otimizacao do indice.
	 * Deve ser chamado apos todos os documentos
	 * estarem agendados para serem inseridos no indice.
	 * Nao e prioritario que seja a ultima thread  a ser executada na fila
	 * por isso nao foi preciso um controle rigido nesse sentido.
	 * Apenas baixou-se a prioridade desta execucao em relacao as demais.
	 */
	private void otimizarIndice(final RepositorioIndice repositorio){
		BaseThread t = new BaseThread(){
			public void run() {
				try {
					repositorio.otimizarIndice();
					
				} catch (Exception e) {
					MTDFactory.getInstancia().getLog().salvarDadosLog(e);
				}
			};
		};
		
		t.setPriority(Thread.MIN_PRIORITY);
		t.executarNoPool();
	}
}