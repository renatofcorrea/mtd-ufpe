package br.ufpe.mtd.negocio.controle;

import java.util.ArrayList;
import java.util.List;

import br.ufpe.mtd.dados.drive.OAIPMHDriver;
import br.ufpe.mtd.dados.indice.IRepositorioIndice;
import br.ufpe.mtd.negocio.entidade.Identificador;
import br.ufpe.mtd.negocio.thread.BuscaMetadadosThread;
import br.ufpe.mtd.util.MTDFactory;

public class IndiceControle {
	private MTDFactory f;
	private IRepositorioIndice repositorio;
	private int contador;

	public IndiceControle(IRepositorioIndice repositorio, MTDFactory f) {
		this.repositorio = repositorio;
		this.f = f;
	}
	
	public void indexar(String urlBase, String metaDataPrefix, String set) throws Exception {
		OAIPMHDriver driver = OAIPMHDriver.getInstance(urlBase, metaDataPrefix);
		driver.setSet(set);
		indexar(driver);
	}
	
	/**
	 * Realiza a primeira parte da indexacao que é a busca dos identificadores.
	 * Esta parte e feita de forma sincrona.
	 * Chama a tarefa concorrente de baixar os docs para cada identificador.
	 * Esta tarefa como e mais pesada , sera feita em Threads chamadas pera baixarDocsESalvar.
	 * @param driver
	 * @throws Exception
	 */
	public void indexar(OAIPMHDriver driver) throws Exception {
		
		List<Identificador> dadosRecebidos = new ArrayList<Identificador>();
		List<Identificador> identificadores = null;
		List<Identificador> deletados = new ArrayList<Identificador>();
		
		try {
			while(driver.hasNext()){
				
				identificadores = driver.getNextIdentifiers();
					
				//tratamento pois dados de identificadores podem vir repetidos 
				//verifiquei que o token para nova busca fornecido pelo proprio repositorio, 
				//resulta em busca que tras registros ja enviados em buscas anteriores. 
				if(identificadores.size() > 0 && !dadosRecebidos.contains(identificadores.get(0))){
					dadosRecebidos.addAll(identificadores);
					for (Identificador identificador : identificadores) {
						if(identificador.isDeletado()){
							deletados.add(identificador);
						}
					}
					
					for (Identificador identificador : deletados) {
						MTDFactory.getInstancia().getLog().salvarDadosLog("IndiceControle.indexar() Deletado id : "+identificador.getId());
					}
					
					identificadores.removeAll(deletados);
					deletados.clear();
					contador += identificadores.size();
					
					baixarDocsEsalvar(repositorio, identificadores, driver.getUrlBase(), driver.getMetaDataPrefix());
				}
			}
			
		}catch(Exception e){
			f.getLog().salvarDadosLog(e);
		}finally {
			f.getPoolThread().aguardarFimDoPool();
			repositorio.fecharRepositorio();
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
		//f.getLog().salvarDadosLog("IndiceControle.baixarDocsEsalvar() Quantidade de ids enviados para baixar: "+contador);
		BuscaMetadadosThread t = new BuscaMetadadosThread(f.getLog(),repositorio , identificadores,urlBase,metaDataPrefix);
		t.executarNoPool();
	}
	

}