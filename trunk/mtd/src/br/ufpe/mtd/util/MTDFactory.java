package br.ufpe.mtd.util;

import java.io.File;
import java.io.IOException;
import java.net.ContentHandler;
import java.net.ContentHandlerFactory;
import java.util.Properties;

import br.ufpe.mtd.dados.IRepositorioIndice;
import br.ufpe.mtd.dados.RepositorioIndiceLucene;
import br.ufpe.mtd.dados.RepositorioIndiceSolr;
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
	private int qtdMaxThread = 1;
	
	private MTDFactory(){
		try {
			Properties propriedades = MTDParametros.getMTDProperties();
			String strUsarSoler = propriedades.getProperty("solr_usar");
			
			if(strUsarSoler != null && Boolean.parseBoolean(strUsarSoler) == true){
				String strSolrUrl = propriedades.getProperty("solr_url");
				repositorioIndice = new RepositorioIndiceSolr(strSolrUrl);
				
			}else{
				repositorioIndice = new RepositorioIndiceLucene(new File(MTDParametros.getExternalStorageDirectory(),MTDParametros.getMTDProperties().getProperty("indice_dir")));
			}
			
			
			poolThread = new MTDThreadPool(qtdMaxThread);
			logPoolThread = new MTDThreadPool(1);
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
	
	/**
	 * Fa�a o log dos seus dados e exceptions atraves deste objeto.
	 * 
	 * @return
	 */
	public Log getLog(){
		return log;
	}
	
	
}