package br.ufpe.mtd.negocio.decodificacao;

import net.sf.jColtrane.annotations.methods.ContainAttribute;
import net.sf.jColtrane.annotations.methods.EndElement;
import net.sf.jColtrane.annotations.methods.InsideElement;
import net.sf.jColtrane.annotations.methods.StartElement;
import net.sf.jColtrane.handler.ContextVariables;
import br.ufpe.mtd.util.MTDFactory;
import br.ufpe.mtd.util.MTDUtil;
import br.ufpe.mtd.util.enumerado.AreaCNPQEnum;
import br.ufpe.mtd.util.log.Log;

/**
 * Classe que vai fazer a decodificacao dos 
 * documentos e ao final do processo tera uma lista de 
 * documentos decodificados.
 * 
 * Auxilia classes como JColtraneXMLHandler na realizacao de parser. 
 * Usa o padrão de metadados Dublin Core
 * Util para harvesting de repositórios institucionais
 * @author renato
 * 
 * 
 */

//URLs de acesso as teses e dissertações no repositorio da UFPE
//http://www.repositorio.ufpe.br/oai/request?verb=ListIdentifiers&metadataPrefix=oai_dc&set=com_123456789_50
//http://www.repositorio.ufpe.br/oai/request?verb=GetRecord&metadataPrefix=qdc&identifier=oai:repositorio.ufpe.br:123456789/387

public class DecodificadorDocumentoDC extends DecodificadorDocumento{


	public DecodificadorDocumentoDC() {
		super();
	}

	@StartElement(tag = "request")
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
	
	@EndElement(tag = "request",attributes=@ContainAttribute(name="identifier"))
	public void pegarIdFromRequest(ContextVariables contextVariables) {
		String identificador = contextVariables.getLastEvent().getAtributesHolder().getValue("identifier");
		getDoc().setId(identificador);
		getDoc().setRepositorio(identificador.split(":")[1]);//seta também repositório
	}
	
	@InsideElement(tag = "header")
	@EndElement(tag = "identifier")
	public void pegarId(ContextVariables contextVariables) {
		String identificador = contextVariables.getBody();
		if(!identificador.contains("localhost")){
		getDoc().setId(identificador);
		getDoc().setRepositorio(identificador.split(":")[1]);//seta também repositório
		}
	}
	
	
	

	@EndElement(tag = "dc:title")
	public void pegarTitulo(ContextVariables contextVariables) {
		getDoc().setTitulo(tratarCaracteres(contextVariables.getBody()));

	}
	
	//oai_dc
	@EndElement(tag = "dc:description")
	public void pegarResumo(ContextVariables contextVariables) {
		getDoc().setResumo(tratarCaracteres(contextVariables.getBody()));

	}
	
	//qdc
	@EndElement(tag = "dcterms:abstract")
	public void pegarResumoQDC(ContextVariables contextVariables) {
			getDoc().setResumo(tratarCaracteres(contextVariables.getBody()));
	}

	@EndElement(tag = "dc:subject")
	public void pegarAssunto(ContextVariables contextVariables) {
		getDoc().adicionarPalavraChave(tratarCaracteres(contextVariables.getBody()));

	}

	@EndElement(tag = "dc:creator")
	public void pegarAutor(ContextVariables contextVariables) {
		//pode ter casos onde a tag tenha mais de uma ocorrencia de Nome
		//mesmo sem ser o autor. por isso esta sendo considerado so a primeira ocorrencia.
		String temp = contextVariables.getBody();
		if(temp.contains("(Orientador)")){//seta orientador
			String orientador = temp.substring(0, temp.indexOf(" (Orientador)")).trim();
			if(orientador.contains(",")){
				String [] p =orientador.split(",");
				orientador = p[1].trim()+" "+p[0].trim();
			}
			getDoc().setOrientador(tratarCaracteres(orientador));
		}
		else if(!getDoc().contemAutor()){//seta autor
			String autor = contextVariables.getBody();
					if(autor.contains(",")){
						String [] p =autor.split(",");
						autor = p[1].trim()+" "+p[0].trim();
					}
			getDoc().setAutor(tratarCaracteres(autor));
		}
	}
	
	@EndElement(tag = "dc:publisher")
	public void pegarNomeBiblioteca(ContextVariables contextVariables) {
		if(!getDoc().contemNomeInstituicao()){
			getDoc().setNomeInstituicao(tratarCaracteres(contextVariables.getBody()));
		}
	}
	
	@EndElement(tag = "dc:identifier")
	public void pegarUrlArquivo(ContextVariables contextVariables) {
		String temp = contextVariables.getBody();
		if(temp.contains("http")){
			if(getDoc().getId().contains("repositorio.ufpe.br")){
				//Endereço do handle errado em repositorio.ufpe.br
				//Informado: http://hdl.handle.net/123456789/9400
				//Correto: http://www.repositorio.ufpe.br/handle/123456789/9400
				temp = temp.replace("http://hdl.handle.net/", "http://www.repositorio.ufpe.br/handle/");
			}
			getDoc().setUrl(temp);
		
		}else{
			
			String [] partes = temp.split("\\. ");
			// 0 - autores separados por ;
			// 1 - título
			// 2 - ano
			// 3 - Tipo: Dissertação (Mestrado) / Tese (Doutorado)
			// 4 - programa, instituição, cidade, ano
			if(partes.length >=5){
				setarPrograma(partes);
				setarAreaCNPQPorPrograma(getDoc().getPrograma());
				setarDataDefesa(partes[2]);
				setarGrau(partes);
			}
		}
	}
	
	private void setarPrograma(String[] partes){
		String programa = partes[4].split(",")[0];
		if(programa.toLowerCase().contains("programa de "))
			getDoc().setPrograma(programa.trim());
		else{
			for(int i=5; i < partes.length; i++){
				if(partes[i].toLowerCase().contains("programa de ")){
					getDoc().setPrograma(tratarCaracteres(partes[i].split(",")[0].trim()));
					break;
				}
			}
		}
	}
	
	private void setarGrau(String[] partes){
		for(int i=3; i < partes.length; i++){
			if(partes[i].toLowerCase().contains("dissertação") || partes[i].toLowerCase().contains("tese")){
				if(partes[i].toLowerCase().contains("dissertação")){
					getDoc().setGrau("mestre");
					
				}else if (partes[i].toLowerCase().contains("tese")){
					getDoc().setGrau("doutor");
				}else{
					Log log = MTDFactory.getInstancia().getLog();
					log.salvarDadosLog("DecodificadorDocumentoDC.pegarURLArquivo, grau desconhecido: "+partes[3]);
					log.salvarDadosLog(new Exception("DecodificadorDocumentoDC.pegarURLArquivo, grau desconhecido: "+partes[3]));
				}
				break;
			}
		}
	}
	
	private void setarAreaCNPQPorPrograma(String programa){
		String area = null;
		if(programa != null){
			area = AreaCNPQEnum.getAreaCNPQPorPrograma(programa);
		}
		
		if(area != null){
			getDoc().setAreaCNPQ(area);
		}else{
			getDoc().setAreaCNPQ(AreaCNPQEnum.NAO_ENCONTRADO.name());
		}
		
		if(!getDoc().contemAreaPrograma() && area != null){
			getDoc().setAreaPrograma(area);
		}		
	}
	
	private void setarDataDefesa(String data){
		if(!getDoc().contemDataDefesa()){
			getDoc().setDataDeDefesa(MTDUtil.recuperarDataFormatosSuportados(data.trim()));
		}
	}
	
	//setado pela função acima caso oai_dc, verificar.
	@EndElement(tag = "dcterms:issued")//Data de defesa
	public void pegarDataDefesa(ContextVariables contextVariables) {
		String data = contextVariables.getBody();
		getDoc().setDataDeDefesa(MTDUtil.recuperarDataFormatosSuportados(data.trim()));
	}	
	
	/**
	 * Retirar os caracteres html que foram colocados dentro do texto.
	 * 
	 * @param entrada
	 * @return
	 */
	private String tratarCaracteres(String entrada){
		//TODO: fazer a substituição correta das entidades html para caracteres iso8859-1
		entrada = entrada.replaceAll("&#[0-9]*;", " ");//html code
		entrada = entrada.replace("&#13;", "\r");//html code
		entrada = entrada.replace("/", ","); //substitui /
	    entrada = entrada.replace("\"", " ");//substitui aspas
	    entrada = entrada.replace("\r", " "); //substitui retorno
	    entrada = entrada.replace("\n", " ");//substitui novalinha
	    entrada = entrada.replace("\t", " ");//substitui tabulação
	    entrada = entrada.replaceAll("[ ]{2,}", " ");//excesso de espaço em branco
	    return entrada;
	}
}
