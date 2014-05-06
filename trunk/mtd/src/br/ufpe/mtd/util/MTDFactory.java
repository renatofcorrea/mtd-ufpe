package br.ufpe.mtd.util;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ContentHandler;
import java.net.ContentHandlerFactory;

import br.ufpe.mtd.dados.IRepositorioIndice;
import br.ufpe.mtd.dados.RepositorioIndiceLucene;
import br.ufpe.mtd.dados.RepositorioIndiceSolr;
import br.ufpe.mtd.enumerado.AreaCNPQEnum;
import br.ufpe.mtd.enumerado.MTDArquivoEnum;
import br.ufpe.mtd.enumerado.MimeTypeEnum;
import br.ufpe.mtd.negocio.ControleIndice;
import br.ufpe.mtd.thread.MTDThreadPool;

/**
 * Fabrica de objetos da aplicacao cria os objetos necessarios
 * a serem usados pelas outras partes do sistema
 * configurando estes objetos de forma adequada.
 * 
 * @author djalma
 *
 */
public class MTDFactory implements ContentHandlerFactory{
	
	private static Log log;
	private static MTDFactory instancia;
	private IRepositorioIndice repositorioIndice;
	private MTDThreadPool poolThread;
	private MTDThreadPool logPoolThread;
	private MTDThreadPool treinamentoPoolThread;
	
	private MTDFactory(){
		try {
			String urlSolr = MTDParametros.getSolrUrl();
			if(urlSolr != null){
				repositorioIndice = new RepositorioIndiceSolr(urlSolr);
				
			}else{
				repositorioIndice = new RepositorioIndiceLucene(MTDArquivoEnum.INDICE_DIR.getArquivo());
			}
			
			poolThread = new MTDThreadPool(MTDParametros.getNumMaxThreads());
			logPoolThread = new MTDThreadPool(1);
			treinamentoPoolThread = new MTDThreadPool(1);
			
			log = new Log();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static MTDFactory getInstancia(){
		if(instancia == null){
			instancia =  new MTDFactory();
		}
		return instancia;
	}
	
	/**
	 * Retorna instancia unica do repositorio de indice.
	 * 
	 * @see newControleIndice()
	 *  
	 * @return
	 * @throws IOException
	 */
	public IRepositorioIndice getSingleRepositorioIndice() throws IOException{
		return repositorioIndice;
	}
	
	public  ContentHandler createContentHandler(String mimeType) {
		if (mimeType == null)
			return null;
		if (MimeTypeEnum.HTML.getCodigo().equalsIgnoreCase(mimeType)
				|| MimeTypeEnum.XML.getCodigo().equalsIgnoreCase(mimeType)) {
			return new TextContentHandler();
		} // retornar gerenciador para HTML/XML
		else {
			return new OtherContentHandler();
		} // retornar gerenciador para outros tipos de conteudo.
	}
	
	/**
	 * Metodo que devolve um controle do indice e ja 
	 * utiliza o respostorio adequado para manipular o indice 
	 * e aplicar as regras de negocio adequadas. 
	 * @return
	 * @throws IOException
	 */
	public ControleIndice newControleIndice() throws IOException{
		ControleIndice controle = new ControleIndice(getSingleRepositorioIndice());
		return controle;
	}
	
	public synchronized MTDThreadPool getPoolThread(){
		return poolThread;
	}
	
	public synchronized MTDThreadPool getLogPoolThread() {
		return logPoolThread;
	}
	
	public synchronized MTDThreadPool getTreinamentoPoolThread(){
		return treinamentoPoolThread;
	}
	
	/**
	 * Faça o log dos seus dados e exceptions atraves deste objeto.
	 * 
	 * @return
	 */
	public Log getLog(){
		return log;
	}
	
	public FileOutputStream getTreinamentoStream(MTDArquivoEnum arquivoTreinamento) throws FileNotFoundException{
		return new FileOutputStream(arquivoTreinamento.getArquivo());
	}
	
	/**
	 * Devolve uma grande area cnpq para um determinado termo que representa uma sub area.
	 * 
	 * @param nomeSubArea
	 * @return
	 */
	public AreaCNPQEnum getAreaCNPQ(String nomeSubArea){
		
		for(AreaCNPQEnum area : AreaCNPQEnum.values()){
			if(area.contains(nomeSubArea)){
				return area;
			}
		}
		
		return AreaCNPQEnum.NAO_ENCONTRADO;
	}
}