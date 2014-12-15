package br.ufpe.mtd.negocio.decodificacao;

import net.sf.jColtrane.annotations.methods.ContainAttribute;
import net.sf.jColtrane.annotations.methods.EndElement;
import net.sf.jColtrane.annotations.methods.InsideElement;
import net.sf.jColtrane.annotations.methods.StartElement;
import net.sf.jColtrane.handler.ContextVariables;
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
public class DecodificadorDocumentoMTD2BR extends DecodificadorDocumento{

	public DecodificadorDocumentoMTD2BR() {
		super();
	}
	
	@StartElement(tag = "record")
	public void criarDocumento() {
		super.criarDocumento();
	}

	/**
	 * Metodo chamado na hora de guardar um novo registro na lista
	 * de registros decodificados.
	 */
	@EndElement(tag = "record")
	public void salvarDocumento() {
		super.salvarDocumento();
	}
	

	@EndElement(tag = "mtd2-br:Titulo", attributes = @ContainAttribute(value = "pt"))
	public void pegarTitulo(ContextVariables contextVariables) {
		getDoc().setTitulo(contextVariables.getBody());

	}
	
	@EndElement(tag = "mtd2-br:Resumo", attributes = @ContainAttribute(value = "pt"))
	public void pegarResumo(ContextVariables contextVariables) {
		getDoc().setResumo(contextVariables.getBody());

	}

	@EndElement(tag = "mtd2-br:Assunto", attributes = {
			@ContainAttribute(value = "pt"),
			@ContainAttribute(value = "Palavra-chave") })
	public void pegarAssunto(ContextVariables contextVariables) {
		getDoc().adicionarPalavraChave(contextVariables.getBody());

	}

	@EndElement(tag = "mtd2-br:Assunto", attributes = {
			@ContainAttribute(value = "pt"),
			@ContainAttribute(value = "Tabela CNPQ") })
	public void pegarAreaCNPQ(ContextVariables contextVariables) {
		getDoc().setAreaCNPQ(contextVariables.getBody());

	}

	@InsideElement(tag = "mtd2-br:Autor")
	@EndElement(tag = "mtd2-br:Nome")
	public void pegarAutor(ContextVariables contextVariables) {
		//pode ter casos onde a tag tenha mais de uma ocorrencia de Nome
		//mesmo sem ser o autor. por isso esta sendo considerado so a primeira ocorrencia.
		if(!getDoc().contemAutor()){
			getDoc().setAutor(contextVariables.getBody());
		}
	}
	
	@InsideElement(tag = "mtd2-br:BibliotecaDigital")
	@EndElement(tag = "mtd2-br:Sigla")
	public void pegarRepositorio(ContextVariables contextVariables) {
		//pode ter casos onde a tag tenha mais de uma ocorrencia de Sigla
		//mesmo sem ser o autor. por isso esta sendo considerado so a primeira ocorrencia.
		if(!getDoc().contemRepositorio()){
			getDoc().setRepositorio(contextVariables.getBody());
		}
	}
	
	@InsideElement(tag = "mtd2-br:InstituicaoDefesa")
	@EndElement(tag = "mtd2-br:Nome")
	public void pegarNomeBiblioteca(ContextVariables contextVariables) {
		if(!getDoc().contemNomeInstituicao()){
			getDoc().setNomeInstituicao(contextVariables.getBody());
		}
	}
	
	@InsideElement(tag = "mtd2-br:Arquivo")
	@EndElement(tag = "mtd2-br:URL")
	public void pegarUrlArquivo(ContextVariables contextVariables) {
		//pode ter casos onde a tag tenha mais de uma ocorrencia de Sigla
		//mesmo sem ser o autor. por isso esta sendo considerado so a primeira ocorrencia.
		getDoc().setUrl(contextVariables.getBody());
	}

	@InsideElement(tag = "mtd2-br:InstituicaoDefesa")
	@EndElement(tag = "mtd2-br:Area")
	public void pegarAreaPrograma(ContextVariables contextVariables) {
		getDoc().setAreaPrograma(contextVariables.getBody());
		
	}

	@InsideElement(tag = "mtd2-br:Programa")
	@EndElement(tag = "mtd2-br:Nome")
	public void pegarPrograma(ContextVariables contextVariables) {
		getDoc().setPrograma(contextVariables.getBody());

	}

	@InsideElement(tag = "mtd2-br:Contribuidor", attributes = @ContainAttribute(value = "Orientador"))
	@EndElement(tag = "mtd2-br:Nome")
	public void pegarOrientador(ContextVariables contextVariables) {
		getDoc().setOrientador(contextVariables.getBody());

	}

	@EndElement(tag = "mtd2-br:DataDefesa")
	public void pegarDataDefesa(ContextVariables contextVariables) {
		String data = contextVariables.getBody();
		getDoc().setDataDeDefesa(MTDUtil.recuperarDataFormatosSuportados(data.trim()));
	}
	
	@EndElement(tag = "mtd2-br:Grau")
	public void pegarGrau(ContextVariables contextVariables) {
		String data = contextVariables.getBody();
		getDoc().setGrau(data);
	}

	@EndElement(tag = "identifier")
	public void pegarId(ContextVariables contextVariables) {
		String identificador = contextVariables.getBody();
		getDoc().setId(identificador);
	}
}
