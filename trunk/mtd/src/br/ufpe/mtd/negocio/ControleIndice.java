package br.ufpe.mtd.negocio;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import net.sf.jColtrane.handler.JColtraneXMLHandler;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.xml.sax.SAXException;

import br.ufpe.mtd.consulta.OAIPMHDriver;
import br.ufpe.mtd.dados.RepositorioIndice;
import br.ufpe.mtd.entidade.DocumentWrapper;
import br.ufpe.mtd.entidade.Identificador;
import br.ufpe.mtd.excecao.MTDException;
import br.ufpe.mtd.thread.BaseThread;
import br.ufpe.mtd.util.Log;
import br.ufpe.mtd.util.MTDFactory;
import br.ufpe.mtd.xml.DecodificadorDocumento;

public class ControleIndice {
	
	private RepositorioIndice repositorio;

	public ControleIndice(RepositorioIndice repositorio) {
		this.repositorio = repositorio;
	}
	
	public void indexar(String urlBase, String metaDataPrefix) throws SAXException, IOException, MTDException, ParserConfigurationException{
		List<Identificador> dadosRecebidos = new ArrayList<Identificador>();
		
		List<Identificador> identificadores = null;
		List<Identificador> deletados = new ArrayList<Identificador>();
		
		OAIPMHDriver driver = new  OAIPMHDriver(urlBase, metaDataPrefix);
		
		while(driver.hasNext()){
			identificadores = driver.getNextIdentifiers();
			
			//tratamento pois dados de identificadores podem vir repetidos
			if(identificadores.size() > 0 && !dadosRecebidos.contains(identificadores.get(0))){
				
				dadosRecebidos.addAll(identificadores);
				
				System.out.println("----------- ids ---------");
				for (Identificador identificador : identificadores) {
					System.out.println(identificador.getId()+ "  deletado: "+identificador.isDeletado());
					
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
		t.executarNoPool();
	}
	
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
	private class ThreadBuscaMetadados extends BaseThread{
		private RepositorioIndice repositorio;
		private List<Identificador> identificadores;
		private String urlBase;
		private String metaDataPrefix;
		
		public ThreadBuscaMetadados(RepositorioIndice repositorio, List<Identificador> identificadores, String urlBase, String metaDataPrefix) {
			this.identificadores = identificadores;
			this.repositorio = repositorio;
			this.urlBase = urlBase;
			this.metaDataPrefix = metaDataPrefix;
		}
		
		@Override
		public void run() {
			super.run();
			try {
				
				List<Document> docs = colherMetadadosOnline(identificadores, urlBase, metaDataPrefix);
				repositorio.inserirDocumento(docs);
			} catch (CorruptIndexException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		/**
		 * 
		 * @param identificadores
		 * @param urlBase
		 * @param metaDataPrefix
		 * @return
		 * @throws IOException
		 * @throws ParserConfigurationException
		 * @throws SAXException
		 */
		public List<Document> colherMetadadosOnline(List<Identificador> identificadores,String urlBase,String metaDataPrefix)
				throws IOException, ParserConfigurationException, SAXException {
			OAIPMHDriver driver = new  OAIPMHDriver(urlBase);
			SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
			DecodificadorDocumento decodificador = new DecodificadorDocumento();
			String str = null;
			Log log = MTDFactory.getInstancia().getLog();

			long qtd = 0; 
			long tamanho = identificadores.size();
			
			for (Identificador identificador : identificadores) {
				
				log.salvarDadosLog(Thread.currentThread().getName()+" Processando "+(++qtd) + " De "+tamanho+" documentos...");
				
				str = driver.getRecord(metaDataPrefix, identificador.getId());
				
				ByteArrayInputStream bais = new ByteArrayInputStream(str.getBytes("ISO-8859-1"));
				
				
				if (parser != null) {
					try {
						parser.parse(bais, new JColtraneXMLHandler(decodificador));
						
					} catch (Exception e) {
						log.salvarDadosLog(Thread.currentThread().getName()+"- dados : "+str);
					}
				}
				bais.reset();
			}
			
			ArrayList<Document> docs = new ArrayList<Document>();
			
			//tirar os repetidos caso existam
			TreeSet<DocumentWrapper> treeSetDocs = new TreeSet<DocumentWrapper>(decodificador.getDocumentos());
			
			for (DocumentWrapper documento : treeSetDocs) {
				docs.add(documento.getDocument());
			}
			
			return docs;
		}
	}
}
