package br.ufpe.mtd.negocio.decodificacao;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import net.sf.jColtrane.annotations.methods.ContainAttribute;
import net.sf.jColtrane.annotations.methods.EndElement;
import net.sf.jColtrane.annotations.methods.InsideElement;
import net.sf.jColtrane.handler.ContextVariables;
import net.sf.jColtrane.handler.JColtraneXMLHandler;

import org.xml.sax.SAXException;

import br.ufpe.mtd.negocio.entidade.Identificador;
import br.ufpe.mtd.util.MTDException;
import br.ufpe.mtd.util.MTDFactory;

/**
 * Realiza a decodificacao de dados de sets que foram 
 * trazidos atraves de registro de acordo com o protocolo
 * de busca OAIPMH
 * 
 * Ao final estara com alista de todos os identificadores dos sets retornados como validos
 * e de todos os que foram marcados como deletados por não atender ao critério de filtragem.
 * 
 * 
 * @author djalma
 *
 */
public class DecodificadorSet {
	private static SAXParserFactory factory = SAXParserFactory.newInstance();
	private String resumption;
	String identificador;
	private HashMap<String,String> identificadores;//setspec e setname
	private int completeListSize;
	private String repositoryName;
	
	
	public DecodificadorSet() {
		identificadores = new HashMap<String,String>();
		this.repositoryName = null;
	}
	
	public DecodificadorSet(String repositoryName) {
		identificadores = new HashMap<String,String>();
		this.repositoryName = repositoryName;
	}
	
	@EndElement(tag="resumptionToken")
	public void pegarResuption(ContextVariables contextVariables){
		if(this.resumption==null)
			this.completeListSize = Integer.parseInt((String) contextVariables.getLastEvent().getAtributesHolder().getValue("completeListSize"));
		this.resumption = contextVariables.getBody().trim();	
		
	}
	
	@InsideElement(tag ="set")
	@EndElement(tag="setSpec")
	public void pegarIdentificador(ContextVariables contextVariables){
		String conteudo = contextVariables.getBody().trim();
		identificador = conteudo.trim();

	}
	
	@InsideElement(tag ="set")
	@EndElement(tag="setName")
	public void pegarValor(ContextVariables contextVariables){
		
		String conteudo = contextVariables.getBody().trim();
		String valor = conteudo.trim();
		
		if(!identificadores.containsKey(identificador)){
			identificadores.put(identificador,valor);
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
	public HashMap<String,String> getSets(String regex) {
		
		HashMap<String,String> listaRetorno = new HashMap<String,String>();
		
		if(regex == null || regex.isEmpty()){
			listaRetorno.putAll(identificadores);
			//identificadores.clear();
			return listaRetorno;
		}else{
			for(Map.Entry<String,String> aux: identificadores.entrySet()){
				//System.out.print(aux.getValue());
				if(aux.getValue().matches(regex)){
					//System.out.println("--- casou");
					listaRetorno.put(aux.getKey(), aux.getValue());
				}
				//System.out.println("");
			}
			//identificadores.clear();
			return listaRetorno;
		}
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
	
	
	public static void parse(DecodificadorSet decodificador ,InputStream is)
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
		
        
        return;
	}
}