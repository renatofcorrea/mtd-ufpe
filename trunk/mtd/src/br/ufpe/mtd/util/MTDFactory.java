package br.ufpe.mtd.util;

import java.io.File;
import java.io.IOException;
import java.net.ContentHandler;
import java.net.ContentHandlerFactory;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import br.ufpe.mtd.dados.RepositorioIndice;
import br.ufpe.mtd.negocio.ControleIndice;

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
	private RepositorioIndice repositorioIndice;
	private ExecutorService poolThread;
	private ExecutorService logPoolThread;
	private int qtdMaxThread = 10;
	
	private MTDFactory(){
		try {
			repositorioIndice = new RepositorioIndice(new File(MTDParametros.getExternalStorageDirectory(),"indice_MTD"));
			poolThread = Executors.newFixedThreadPool(qtdMaxThread);
			logPoolThread = Executors.newSingleThreadExecutor();
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
	public RepositorioIndice getSingleRepositorioIndice() throws IOException{
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
	
	public ExecutorService getPoolThread(){
		return poolThread;
	}
	
	public ExecutorService getLogPoolThread() {
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