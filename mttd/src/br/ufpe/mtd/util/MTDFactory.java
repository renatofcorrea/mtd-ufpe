package br.ufpe.mtd.util;

import java.io.IOException;
import java.net.ContentHandler;
import java.net.ContentHandlerFactory;
import java.util.Timer;

import br.ufpe.mtd.dados.arquivo.RepositorioMapa;
import br.ufpe.mtd.dados.indice.IRepositorioIndice;
import br.ufpe.mtd.dados.indice.RepositorioIndiceLucene;
import br.ufpe.mtd.dados.indice.RepositorioIndiceSolr;
import br.ufpe.mtd.negocio.IndiceControle;
import br.ufpe.mtd.negocio.MapaControle;
import br.ufpe.mtd.negocio.RedeNeuralControle;
import br.ufpe.mtd.negocio.thread.MTDTask;
import br.ufpe.mtd.util.enumerado.MTDArquivoEnum;
import br.ufpe.mtd.util.enumerado.MimeTypeEnum;
import br.ufpe.mtd.util.log.Log;

/**
 * Fabrica de objetos da aplicacao cria os objetos necessarios
 * a serem usados pelas outras partes do sistema
 * configurando estes objetos de forma adequada.
 * 
 * Ficam na fabrica objetos mais complexos ou que precisem ter seu cicclo de vida gerenciado.
 * 
 * @author djalma
 *
 */
public class MTDFactory implements ContentHandlerFactory{
	
	public static final long PERIODO_REPETICAO = 1000l * 60l * 60l * 24l * 1l;//1 dia
	private static Log log;
	private static MTDFactory instancia;
	private IRepositorioIndice repositorioIndice, repositorioSintagmas;
	private MTDThreadPool poolThread;
	private MTDThreadPool logPoolThread;
	private MTDThreadPool treinamentoPoolThread;
	private Timer timer;
	private static RepositorioMapa repMapa;
	
	
	private MTDFactory(){
		try {
			poolThread = new MTDThreadPool(MTDParametros.getNumMaxThreads());
			logPoolThread = new MTDThreadPool(1);
			treinamentoPoolThread = new MTDThreadPool(1);
			log = new Log();
			
			carregarRepositorios();
			
			if(MTDParametros.isAmbienteWeb()){
				agendarTreino();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void carregarRepositorios() throws Exception{
		repMapa  = new RepositorioMapa();
		
		String urlSolr = MTDParametros.getSolrUrl();
		if(urlSolr != null){
			repositorioIndice = new RepositorioIndiceSolr(urlSolr);
			
		}else{
			repositorioIndice = new RepositorioIndiceLucene(MTDArquivoEnum.INDICE_DIR.getArquivo(), MTDParametros.LUCENE_VERSION);
		}
		
		repositorioSintagmas = new RepositorioIndiceLucene(MTDArquivoEnum.INDICE_SINTAGMA_DIR.getArquivo(), MTDParametros.LUCENE_VERSION);
		
	}
	
	public void resetarRepositorios() throws Exception{
		carregarRepositorios();
	}
	
	public boolean agendarTreino(){
		if(timer == null){
			timer = new Timer();
		}else{
			timer.cancel();
			timer = new Timer();
		}
		timer.schedule(new MTDTask(this), 60000, PERIODO_REPETICAO);//depois de 1 minuto começa a tarefa.
		return false;
	}
	
	public synchronized static MTDFactory getInstancia(){
		if(instancia == null){
			instancia =  new MTDFactory();
		}
		return instancia;
	}
	
	public synchronized void fechar(){
		try {
			if(MTDParametros.isAmbienteWeb()){
				timer.cancel();
			}
			poolThread.fecharPoolAgora();
			logPoolThread.fecharPoolAgora();
			treinamentoPoolThread.fecharPoolAgora();
			log = null;
			repositorioIndice = null;
			repositorioSintagmas = null;
			repMapa = null;
			instancia = null;			
		} catch (Exception e) {
			e.printStackTrace();
		}
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
	
	public IRepositorioIndice getSingleRepositorioSintagmas() throws IOException{
		return repositorioSintagmas;
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
	public IndiceControle newControleIndice() throws IOException{
		IndiceControle controle = new IndiceControle(getSingleRepositorioIndice(),this);
		return controle;
	}
	
	public MapaControle newMapaControle() throws Exception{
		MapaControle mControle = new MapaControle(this);
		return mControle;
	}
	
	public RedeNeuralControle newRedeNeuralControle(){
		RedeNeuralControle rnControle = new RedeNeuralControle(log);
		return rnControle;
	}
	
	public RepositorioMapa getSingleRepositorioMapa(){
		return repMapa;
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
	public synchronized Log getLog(){
		return log;
	}
}