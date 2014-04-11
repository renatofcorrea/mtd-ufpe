package br.ufpe.mtd.xml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import net.sf.jColtrane.annotations.methods.ContainAttribute;
import net.sf.jColtrane.annotations.methods.EndElement;
import net.sf.jColtrane.annotations.methods.InsideElement;
import net.sf.jColtrane.annotations.methods.StartElement;
import net.sf.jColtrane.handler.ContextVariables;
import net.sf.jColtrane.handler.JColtraneXMLHandler;
import br.ufpe.mtd.entidade.BuilderDocumentMTD;
import br.ufpe.mtd.entidade.DocumentMTD;
import br.ufpe.mtd.entidade.Identificador;
import br.ufpe.mtd.excecao.MTDException;
import br.ufpe.mtd.util.Log;
import br.ufpe.mtd.util.MTDFactory;
import br.ufpe.mtd.util.MTDUtil;

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

	private DocumentMTD doc;
	private List<DocumentMTD> documentos;

	public DecodificadorDocumento() {
		documentos = new ArrayList<DocumentMTD>();
	}

	@EndElement(tag = "mtd2-br:Titulo", attributes = @ContainAttribute(value = "pt"))
	public void pegarTitulo(ContextVariables contextVariables) {
		this.doc.setTitulo(contextVariables.getBody());

	}
	
	@EndElement(tag = "mtd2-br:Resumo", attributes = @ContainAttribute(value = "pt"))
	public void pegarResumo(ContextVariables contextVariables) {
		this.doc.setResumo(contextVariables.getBody());

	}

	@EndElement(tag = "mtd2-br:Assunto", attributes = {
			@ContainAttribute(value = "pt"),
			@ContainAttribute(value = "Palavra-chave") })
	public void pegarAssunto(ContextVariables contextVariables) {
		this.doc.adicionarPalavraChave(contextVariables.getBody());

	}

	@EndElement(tag = "mtd2-br:Assunto", attributes = {
			@ContainAttribute(value = "pt"),
			@ContainAttribute(value = "Tabela CNPQ") })
	public void pegarAreaCNPQ(ContextVariables contextVariables) {
		this.doc.setAreaCNPQ(contextVariables.getBody());

	}

	@InsideElement(tag = "mtd2-br:Autor")
	@EndElement(tag = "mtd2-br:Nome")
	public void pegarAutor(ContextVariables contextVariables) {
		//pode ter casos onde a tag tenha mais de uma ocorrencia de Nome
		//mesmo sem ser o autor. por isso esta sendo considerado so a primeira ocorrencia.
		if(!this.doc.contemAutor()){
			this.doc.setAutor(contextVariables.getBody());
		}
	}
	
	@InsideElement(tag = "mtd2-br:BibliotecaDigital")
	@EndElement(tag = "mtd2-br:Sigla")
	public void pegarRepositorio(ContextVariables contextVariables) {
		//pode ter casos onde a tag tenha mais de uma ocorrencia de Sigla
		//mesmo sem ser o autor. por isso esta sendo considerado so a primeira ocorrencia.
		if(!this.doc.contemRepositorio()){
			this.doc.setRepositorio(contextVariables.getBody());
		}
	}
	
	@InsideElement(tag = "mtd2-br:Arquivo")
	@EndElement(tag = "mtd2-br:URL")
	public void pegarUrlArquivo(ContextVariables contextVariables) {
		//pode ter casos onde a tag tenha mais de uma ocorrencia de Sigla
		//mesmo sem ser o autor. por isso esta sendo considerado so a primeira ocorrencia.
		this.doc.setUrl(contextVariables.getBody());
	}

	@InsideElement(tag = "mtd2-br:InstituicaoDefesa")
	@EndElement(tag = "mtd2-br:Area")
	public void pegarAreaPrograma(ContextVariables contextVariables) {
		this.doc.setAreaPrograma(contextVariables.getBody());
		
	}

	@InsideElement(tag = "mtd2-br:Programa")
	@EndElement(tag = "mtd2-br:Nome")
	public void pegarPrograma(ContextVariables contextVariables) {
		this.doc.setPrograma(contextVariables.getBody());

	}

	@InsideElement(tag = "mtd2-br:Contribuidor", attributes = @ContainAttribute(value = "Orientador"))
	@EndElement(tag = "mtd2-br:Nome")
	public void pegarOrientador(ContextVariables contextVariables) {
		this.doc.setOrientador(contextVariables.getBody());

	}

	@EndElement(tag = "mtd2-br:DataDefesa")
	public void pegarDataDefesa(ContextVariables contextVariables) {
		String data = contextVariables.getBody();
		this.doc.setDataDeDefesa(MTDUtil.recuperarDataFormatosSuportados(data.trim()));
	}
	
	@EndElement(tag = "mtd2-br:Grau")
	public void pegarGrau(ContextVariables contextVariables) {
		String data = contextVariables.getBody();
		this.doc.setGrau(data);
	}

	@EndElement(tag = "identifier")
	public void pegarId(ContextVariables contextVariables) {
		String identificador = contextVariables.getBody();
		this.doc.setId(identificador);
	}

	@StartElement(tag = "record")
	public void criarDocumento() {
		this.doc = new BuilderDocumentMTD().buildDocument();
	}

	/**
	 * Metodo chamado na hora de guardar um novo registro na lista
	 * de registros decodificados.
	 */
	@EndElement(tag = "record")
	public void salvarDocumento() {
		this.documentos.add(this.doc);
	}

	public Iterator<DocumentMTD> getDocIterator() {
		return this.documentos.iterator();
	}
	
	public List<DocumentMTD> getDocumentos() {
		return documentos;
	}
	
	
	/**
	 * Tenta fazer o parse usando a codificação padrão e retenta codificação alternativa em caso de exceção.
	 * 
	 * @param xml
	 * @param decodificador
	 * @param identificador
	 * @throws Exception
	 */
	public static void parse(InputStream is, DecodificadorDocumento decodificador, Identificador identificador) throws Exception {
		SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
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
