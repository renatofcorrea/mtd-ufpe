package br.ufpe.mtd.thread;

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
import org.xml.sax.SAXException;

import br.ufpe.mtd.consulta.OAIPMHDriver;
import br.ufpe.mtd.dados.RepositorioIndice;
import br.ufpe.mtd.entidade.DocumentWrapper;
import br.ufpe.mtd.entidade.Identificador;
import br.ufpe.mtd.excecao.MTDException;
import br.ufpe.mtd.util.Log;
import br.ufpe.mtd.util.MTDFactory;
import br.ufpe.mtd.xml.DecodificadorDocumento;

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
public class ThreadBuscaMetadados extends BaseThread{
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
		String idsToString = recuperarComoString(identificadores);
		try {
			
			List<Document> docs = colherMetadadosOnline(identificadores, urlBase, metaDataPrefix);
			repositorio.inserirDocumento(docs);
			MTDFactory.getInstancia().getLog().salvarDadosLog("Inseridos - "+idsToString);
		
		} catch (Exception e) {
			
			MTDException excecao = new MTDException(e, "Problemas ao tentar inserir "+idsToString);
			MTDFactory.getInstancia().getLog().salvarDadosLog(excecao);
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
			
			log.salvarDadosLog(Thread.currentThread().getName()+" Buscando documento para identificador: ("+identificador.getId()+"),  registro "+(++qtd) + " De "+tamanho);
			
			str = driver.getRecord(metaDataPrefix, identificador.getId());
			
			ByteArrayInputStream bais = new ByteArrayInputStream(str.getBytes("ISO-8859-1"));
			
			
			if (parser != null) {
				try {
					parser.parse(bais, new JColtraneXMLHandler(decodificador));
					
				} catch (Exception e) {
					MTDException excecao = new MTDException(e,Thread.currentThread().getName()+"- Erro de parse : "+str); 
					log.salvarDadosLog(Thread.currentThread().getName()+"- Erro de parse - procurar no log de Excecao por: "+identificador.getId());
					log.salvarDadosLog(excecao);
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
