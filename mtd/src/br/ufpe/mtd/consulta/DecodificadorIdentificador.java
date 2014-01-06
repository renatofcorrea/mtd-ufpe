package br.ufpe.mtd.consulta;

import java.util.ArrayList;
import java.util.List;

import net.sf.jColtrane.annotations.methods.ContainAttribute;
import net.sf.jColtrane.annotations.methods.EndElement;
import net.sf.jColtrane.handler.ContextVariables;
import br.ufpe.mtd.entidade.Identificador;

/**
 * Realiza a decodificacao de dados de identificadores que foram 
 * trazidos atraves do formato devolvido de acordo com o protocolo
 * de busca OAIPMH
 * 
 * Ao final estara com alista de todos os identificadores retornados como validos
 * e de todos os que foram marcados como deletados.
 * 
 * De TODO: indentificadores
 * @author djalma
 *
 */
public class DecodificadorIdentificador {

	private String resumption;
	private List<Identificador> identificadores;
	
	
	public DecodificadorIdentificador() {
		identificadores = new ArrayList<Identificador>();
	}
	
	@EndElement(tag="resumptionToken")
	public void pegarResuption(ContextVariables contextVariables){
		this.resumption = contextVariables.getBody().trim();			
	}
	
	@EndElement(tag="identifier", priority = 2)
	public void pegarIdentificador(ContextVariables contextVariables){
		String conteudo = contextVariables.getBody().trim();
		Identificador identificador = new Identificador();
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
}