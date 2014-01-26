package br.ufpe.mtd.thread;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import org.apache.lucene.document.Document;

import br.ufpe.mtd.dados.IRepositorioIndice;
import br.ufpe.mtd.dados.OAIPMHDriver;
import br.ufpe.mtd.entidade.DocumentMTD;
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
	private IRepositorioIndice repositorio;
	private List<Identificador> identificadores;
	private String urlBase;
	private String metaDataPrefix;
	
	public ThreadBuscaMetadados(IRepositorioIndice repositorio, List<Identificador> identificadores, String urlBase, String metaDataPrefix) {
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
			
			//TODO: Ajustar para saber quem realmente foi inserido. esta dando falso positivo.
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
	 * Realiza a busca de metadados dos documentos no repositorio mtd2-br
	 * corrente a partir da lista de identificadores passada como parametro.
	 * 
	 * @param identificadores
	 * @param urlBase
	 * @param metaDataPrefix
	 * @return
	 * @throws Exception 
	 */
	public List<Document> colherMetadadosOnline(List<Identificador> identificadores,String urlBase,String metaDataPrefix)
			throws Exception {
		OAIPMHDriver driver = new  OAIPMHDriver(urlBase);
		DecodificadorDocumento decodificador = new DecodificadorDocumento();
		String url = null;
		Log log = MTDFactory.getInstancia().getLog();

		long qtd = 0; 
		long tamanho = identificadores.size();
		
		
		for (Identificador identificador : identificadores) {
			log.salvarDadosLog(Thread.currentThread().getName()+" Buscando documento para identificador: ("+identificador.getId()+"),  registro "+(++qtd) + " De "+tamanho);
			
			url = driver.getRecord(metaDataPrefix, identificador.getId());//busca os dados online
			
			InputStream is = driver.getResponse(url);
			
			DecodificadorDocumento.parse(is, decodificador, identificador);
		}
		
		ArrayList<Document> docs = new ArrayList<Document>();
		//tirar os repetidos caso existam
		TreeSet<DocumentMTD> treeSetDocs = new TreeSet<DocumentMTD>(decodificador.getDocumentos());
		
		for (DocumentMTD documento : treeSetDocs) {
			docs.add(documento.toDocument());
		}
		
		return docs;
	}
}