package main;

import java.util.GregorianCalendar;
import java.util.Vector;

import net.sf.jColtrane.annotations.methods.ContainAttribute;
import net.sf.jColtrane.annotations.methods.EndElement;
import net.sf.jColtrane.annotations.methods.InsideElement;
import net.sf.jColtrane.annotations.methods.StartElement;
import net.sf.jColtrane.handler.ContextVariables;

public class LeitorDocumentos {

	Documento doc;
	Vector<Documento> documentos = new Vector<Documento>();;
	
	@EndElement(tag="mtd2-br:Titulo", attributes=@ContainAttribute(value="pt"))
	public void pegarTitulo(ContextVariables contextVariables){
		this.doc.setTitulo(contextVariables.getBody());
		
	}
	
	@EndElement(tag="mtd2-br:Resumo", attributes=@ContainAttribute(value="pt"))
	public void pegarResumo(ContextVariables contextVariables){
		this.doc.setDescricao(contextVariables.getBody());
		
	}
	
	@EndElement(tag="mtd2-br:Assunto", attributes={@ContainAttribute(value="pt"), @ContainAttribute(value="Palavra-chave")})
	public void pegarAssunto(ContextVariables contextVariables){
		this.doc.adicionarPalavraChave(contextVariables.getBody());
		
	}
	
	@EndElement(tag="mtd2-br:Assunto", attributes={@ContainAttribute(value="pt"), @ContainAttribute(value="Tabela CNPQ")})
	public void pegarAreaCNPQ(ContextVariables contextVariables){
		this.doc.setAreaCNPQ(contextVariables.getBody());
		
	}
	
	@InsideElement(tag="mtd2-br:Autor")
	@EndElement(tag="mtd2-br:Citacao")
	public void pegarAutor(ContextVariables contextVariables){
		if(this.doc.getAutor()==null)
			this.doc.setAutor(contextVariables.getBody());
		
	}
	
	@InsideElement(tag="mtd2-br:InstituicaoDefesa")
	@EndElement(tag="mtd2-br:Area")
	public void pegarAreaPrograma(ContextVariables contextVariables){
			this.doc.setAreaPrograma(contextVariables.getBody());
		
	}
	
	@InsideElement(tag="mtd2-br:Programa")
	@EndElement(tag="mtd2-br:Nome")
	public void pegarPrograma(ContextVariables contextVariables){
		this.doc.setPrograma(contextVariables.getBody());
		
	}
	
	@InsideElement(tag="mtd2-br:Contribuidor", attributes=@ContainAttribute(value="Orientador"))
	@EndElement(tag="mtd2-br:Nome")
	public void pegarOrientador(ContextVariables contextVariables){
		this.doc.setOrientador(contextVariables.getBody());
		
	}	
	
	@EndElement(tag="mtd2-br:DataDefesa")
	public void pegarDataDefesa(ContextVariables contextVariables){
		String data = contextVariables.getBody();
		int ano = Integer.parseInt(data.substring(0, 4));
		int mes = Integer.parseInt(data.substring(5, 7));
		int dia = Integer.parseInt(data.substring(8, 10));
		GregorianCalendar date = new GregorianCalendar(ano, mes, dia);
		this.doc.setDataDeDefesa(date.getGregorianChange());
	}
	
	@EndElement(tag="identifier")
	public void pegarId(ContextVariables contextVariables){
		String identificador = contextVariables.getBody();
		String[] split = identificador.split(":");		
		Long id = new Long(split[split.length-1]);
		this.doc.setId(id);
	}
	
	@StartElement(tag="record")
	public void criarDocumento(){		
        this.doc = new Documento();
  }
	
	@EndElement(tag="record")
	public void salvarDocumento(){
        this.documentos.add(this.doc);
  }
	
	
	
}
