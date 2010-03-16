package main;

import java.util.Set;
import java.util.TreeSet;

import net.sf.jColtrane.annotations.methods.EndElement;
import net.sf.jColtrane.handler.ContextVariables;

public class Leitor {

	public String resumption = null;
	public Set<String> vetor = new TreeSet<String>();
	
	@EndElement(tag="resumptionToken")
	public void pegarResuption(ContextVariables contextVariables){
		this.resumption = contextVariables.getBody();
		
	}
	
	@EndElement(tag="identifier")
	public void pegarIdentificador(ContextVariables contextVariables){
			String conteudo = contextVariables.getBody();
			vetor.add(conteudo);
	}
	
	
	
	
}
