package br.ufpe.mtd.negocio;

import java.util.ArrayList;
import java.util.List;

import br.ufpe.mtd.consulta.BuscaMetadadosThread;
import br.ufpe.mtd.dados.IRepositorioIndice;
import br.ufpe.mtd.dados.OAIPMHDriver;
import br.ufpe.mtd.entidade.Identificador;
import br.ufpe.mtd.thread.BaseThread;
import br.ufpe.mtd.util.MTDFactory;
import br.ufpe.mtd.util.MTDUtil;

public class ControleIndice {

	private IRepositorioIndice repositorio;

	public ControleIndice(IRepositorioIndice repositorio) {
		this.repositorio = repositorio;
	}
	
	public void treinarRedeNeural(){
		new TreinamentoThread().executarNoPool();
	}
	
	public void indexar(String urlBase, String metaDataPrefix) throws Exception {
		
		List<Identificador> dadosRecebidos = new ArrayList<Identificador>();
		List<Identificador> identificadores = null;
		List<Identificador> deletados = new ArrayList<Identificador>();
		OAIPMHDriver driver = new  OAIPMHDriver(urlBase, metaDataPrefix);
		try {
			while(driver.hasNext()){
				
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
	private void baixarDocsEsalvar(IRepositorioIndice repositorio, List<Identificador> identificadores,String urlBase,String metaDataPrefix) throws InterruptedException{
		BuscaMetadadosThread t = new BuscaMetadadosThread(repositorio , identificadores,urlBase,metaDataPrefix);
		t.executarNoPool();
	}
	
	/*
	 * Netodo auxiliar que 
	 * Agenda a otimizacao do indice.
	 * Deve ser chamado apos todos os documentos
	 * estarem agendados para serem inseridos no indice.
	 */
	private void otimizarIndice(final IRepositorioIndice repositorio){
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