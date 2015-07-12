package br.ufpe.mtd.negocio.decodificacao;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import net.sf.jColtrane.handler.JColtraneXMLHandler;
import br.ufpe.mtd.negocio.entidade.Identificador;
import br.ufpe.mtd.negocio.entidade.MTDDocument;
import br.ufpe.mtd.negocio.entidade.MTDDocumentBuilder;
import br.ufpe.mtd.util.MTDException;
import br.ufpe.mtd.util.MTDFactory;
import br.ufpe.mtd.util.log.Log;

/**
 * Classe que vai fazer a decodificacao dos 
 * documentos e ao final do processo tera uma lista de 
 * documentos decodificados.
 * 
 * Auxilia classes como JColtraneXMLHandler na realizacao de parser. 
 * 
 * @author djalma
 * 
 * 
 */
public class DecodificadorDocumento {
	
	private static SAXParserFactory factory = SAXParserFactory.newInstance();
	private MTDDocument doc;
	private List<MTDDocument> documentos;

	public DecodificadorDocumento() {
		documentos = new ArrayList<MTDDocument>();
		
	}

	public void criarDocumento() {
		setDoc(new MTDDocumentBuilder().buildDocument().build());
	}

	/**
	 * Metodo chamado na hora de guardar um novo registro na lista
	 * de registros decodificados.
	 */
	public void salvarDocumento() {
		if(getDoc().contemCamposRequeridos())
		getDocumentos().add(getDoc());
		else
			MTDFactory.getInstancia().getLog().salvarDadosLog("Documento Id "+ getDoc().getId()+" n�o indexado pois n�o cont�m os campos requeridos:"+getDoc().faltandoCamposRequeridos());
	}	

	public Iterator<MTDDocument> getDocIterator() {
		return this.documentos.iterator();
	}
	
	public List<MTDDocument> getDocumentos() {
		return documentos;
	}
	
	public MTDDocument getDoc() {
		if(doc == null)
			criarDocumento();
		return doc;
	}
	
	public void setDoc(MTDDocument doc) {
		this.doc = doc;
	}
	
	/**
	 * Tenta fazer o parse usando a codifica��o padr�o e retenta codifica��o alternativa em caso de exce��o.
	 * 
	 * @param xml
	 * @param decodificador
	 * @param identificador
	 * @throws Exception
	 */
	public static void parse(InputStream is, DecodificadorDocumento decodificador, Identificador identificador) throws Exception {
		
		SAXParser parser = factory.newSAXParser();
		
		Log log = MTDFactory.getInstancia().getLog();
		
		try {
			parser.parse(is, new JColtraneXMLHandler(decodificador));
		
		}catch (Exception e){
			MTDException excecao = new MTDException(e,Thread.currentThread().getName()+"- Erro durante parse : "+identificador.getId()); 
			
			log.salvarDadosLog(Thread.currentThread().getName()+"- Erro de parse - procurar no log de Excecao por: "+identificador.getId());
			log.salvarDadosLog(excecao);
			
			excecao.setExtraData(identificador);
			throw excecao;
			
		}finally{
			if(is != null){
				is.close();
			}
		}
	}
}