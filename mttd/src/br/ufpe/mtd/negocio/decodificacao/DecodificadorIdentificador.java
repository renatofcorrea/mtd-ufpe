package br.ufpe.mtd.negocio.decodificacao;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import net.sf.jColtrane.annotations.methods.ContainAttribute;
import net.sf.jColtrane.annotations.methods.EndElement;
import net.sf.jColtrane.handler.ContextVariables;
import net.sf.jColtrane.handler.JColtraneXMLHandler;

import org.xml.sax.SAXException;

import br.ufpe.mtd.negocio.entidade.Identificador;
import br.ufpe.mtd.util.MTDException;
import br.ufpe.mtd.util.MTDFactory;

/**
 * Realiza a decodificacao de dados de identificadores que foram 
 * trazidos atraves do formato devolvido de acordo com o protocolo
 * de busca OAIPMH
 * 
 * Ao final estara com alista de todos os identificadores retornados como validos
 * e de todos os que foram marcados como deletados.
 * 
 * 
 * @author djalma
 *
 */
public class DecodificadorIdentificador {
	private static SAXParserFactory factory = SAXParserFactory.newInstance();
	private String resumption;
	private List<Identificador> identificadores;
	private int completeListSize;
	private String repositoryName;
	
	
	public DecodificadorIdentificador() {
		identificadores = new ArrayList<Identificador>();
		this.repositoryName = null;
	}
	
	public DecodificadorIdentificador(String repositoryName) {
		identificadores = new ArrayList<Identificador>();
		this.repositoryName = repositoryName;
	}
	
	@EndElement(tag="resumptionToken")
	public void pegarResuption(ContextVariables contextVariables){
		if(this.resumption==null)
			this.completeListSize = Integer.parseInt((String) contextVariables.getLastEvent().getAtributesHolder().getValue("completeListSize"));
		this.resumption = contextVariables.getBody().trim();	
		
	}
	@EndElement(tag="identifier", priority = 2)
	public void pegarIdentificador(ContextVariables contextVariables){
		String conteudo = contextVariables.getBody().trim();
		Identificador identificador = new Identificador();
		
		//tratamento bug provocado pela mudanca dos dados na bdtd.
		if(conteudo.contains("localhost") && repositoryName != null){
			conteudo = conteudo.replace("localhost", repositoryName);
		}
		
		identificador.setId(conteudo);
		if(!identificadores.contains(identificador)){
			identificadores.add(identificador);
		}
	}
	
	
	/*
	 * Guarda a lista dos deletados para validar futuramente  
	 */
	@EndElement(tag="header", attributes = @ContainAttribute(value = "deleted"), priority = 1)
	public void pegarHeader(ContextVariables contextVariables){
		int ultimo = identificadores.size() - 1;
		if(ultimo >= 0){
			Identificador identificadorRemovido = identificadores.get(ultimo);
			identificadorRemovido.setDeletado(true);			
		}
	}
	
	public String getResumption() {
		return resumption;
	}
	
	/**
	 * Retorna os identificadores baixados 
	 * limpado-os da lista interna do decodificador.
	 * e decodificados. 
	 * @return
	 */
	public List<Identificador> getIdentificadores() {
		ArrayList<Identificador> listaRetorno = new ArrayList<Identificador>();
		listaRetorno.addAll(identificadores);
		
		identificadores.clear();
		return listaRetorno;
	}
	
	public boolean isIniciado(){
		return resumption!= null;
	}
	
	public int getTotalIdentificadores(){
		if (resumption!= null)
			return completeListSize;
		else
			return -1;
	}
	
	/**
	 * Verifica se existem novos registros a serem trazidos do repositorio
	 * externo. Este metodo so dara verdadeiro se pelo menos uma consulta
	 * tiver sido realizada ao repositorio externo.
	 * 
	 * Auxilia a realizar buscas de dados que sao trazidos em lotes.
	 * 
	 * @return
	 */
	public boolean hasNext(){
		boolean contem = isIniciado() && !resumption.equals("");
		return contem;
	}
	
	
	public static List<Identificador> parse(DecodificadorIdentificador decodificador ,InputStream is)
			throws ParserConfigurationException, SAXException, IOException, MTDException{
		SAXParser parser = factory.newSAXParser();
		try{
			//TODO: algumas vezes está dando uma exceção no JColtrane ao obter os metadados nesta linha
			//parece que é necessário resetar o decodificador para aquele que obtem os metadados
        	parser.parse(is, new JColtraneXMLHandler(decodificador));
        	
        }catch(Exception e){
        	MTDFactory.getInstancia().getLog().salvarDadosLog(e);
        	
        }finally{
        	parser.reset();
        	if(is != null){
        		is.close();
        	}
        }
		
        
        return decodificador.getIdentificadores();
	}
}