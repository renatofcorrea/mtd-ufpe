package br.ufpe.mtd.negocio;

import java.util.ArrayList;
import java.util.List;

import br.ufpe.mtd.consulta.OAIPMHDriver;
import br.ufpe.mtd.dados.RepositorioIndice;
import br.ufpe.mtd.entidade.Identificador;
import br.ufpe.mtd.excecao.MTDException;
import br.ufpe.mtd.thread.BaseThread;
import br.ufpe.mtd.thread.ThreadBuscaMetadados;
import br.ufpe.mtd.util.MTDFactory;
import br.ufpe.mtd.util.MTDUtil;

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
		try {
			while(driver.hasNext()){
				try{
					tentativas++;
					identificadores = driver.getNextIdentifiers();
					tentativas = 0;
				}catch(Exception e){
					if((e instanceof java.net.SocketTimeoutException  || e instanceof java.net.SocketException) &&
							tentativas > QTD_MAX_TENTATIVAS){
						 
						MTDFactory.getInstancia().getLog().salvarDadosLog(new MTDException(e, "Exceção apos Tentativas ("+tentativas+")"));
						throw e;
					}else{
						throw e;
					}
				}
					
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
			}
			
		} finally {
			MTDFactory.getInstancia().getPoolThread().aguardarFimDoPool();
			//se ocorrer excecao sobe para metodo chamador, porem sempre otimiza o indice antes.
			otimizarIndice(repositorio);
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
	 * @throws InterruptedException 
	 */
	private void baixarDocsEsalvar(RepositorioIndice repositorio, List<Identificador> identificadores,String urlBase,String metaDataPrefix) throws InterruptedException{
		ThreadBuscaMetadados t = new ThreadBuscaMetadados(repositorio , identificadores,urlBase,metaDataPrefix);
		t.executarNoPool();
	}
	
	/*
	 * Netodo auxiliar que 
	 * Agenda a otimizacao do indice.
	 * Deve ser chamado apos todos os documentos
	 * estarem agendados para serem inseridos no indice.
	 */
	private void otimizarIndice(final RepositorioIndice repositorio){
		BaseThread t = new BaseThread(){
			public void run() {
				try {
					MTDUtil.imprimirConsole("Iniciando otimização do indice....");
					repositorio.otimizarIndice();
					MTDUtil.imprimirConsole("...Concluida otimização do indice");					
				} catch (Exception e) {
					MTDFactory.getInstancia().getLog().salvarDadosLog(e);
				}
			};
		};
		
		t.executarNoPool();
	}
}