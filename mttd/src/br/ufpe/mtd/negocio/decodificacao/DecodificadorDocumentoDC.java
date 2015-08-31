package br.ufpe.mtd.negocio.decodificacao;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tika.language.LanguageIdentifier;

import javafx.scene.text.Text;
import net.sf.jColtrane.annotations.methods.ContainAttribute;
import net.sf.jColtrane.annotations.methods.EndElement;
import net.sf.jColtrane.annotations.methods.InsideElement;
import net.sf.jColtrane.annotations.methods.StartElement;
import net.sf.jColtrane.handler.ContextVariables;
import br.ufpe.mtd.dados.drive.OAIPMHDriver;
import br.ufpe.mtd.util.MTDFactory;
import br.ufpe.mtd.util.MTDUtil;
import br.ufpe.mtd.util.StringConverter;
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
	
	private String identifyLanguage(String text) {
	    LanguageIdentifier identifier = new LanguageIdentifier(text);
	    return identifier.getLanguage();
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
	
	
	@InsideElement(tag = "header")
	@EndElement(tag = "setSpec")
	public void pegarProgramGrauName(ContextVariables contextVariables) {
		String setSpec = contextVariables.getBody();
		String nset = OAIPMHDriver.getInstance().getProgramBySet(setSpec);
		if(nset!=null && !nset.isEmpty()){
			getDoc().setPrograma(tratarCaracteres(nset.replaceFirst("Pós[ ]+Graduação", "Pós-Graduação")));
			setarAreaCNPQPorPrograma(getDoc().getPrograma());
		}
		nset = OAIPMHDriver.getInstance().getGrauBySet(setSpec);
		if(nset!=null && !nset.isEmpty()){
			getDoc().setGrau(tratarCaracteres(nset));
		}
		return;
	}
	
	

	@EndElement(tag = "dc:title")
	public void pegarTitulo(ContextVariables contextVariables) {
		getDoc().setTitulo(tratarCaracteres(contextVariables.getBody()));

	}
	
	//oai_dc
	@EndElement(tag = "dc:description")
	public void pegarResumo(ContextVariables contextVariables) {
		String text = tratarCaracteres(contextVariables.getBody());
		String lang = identifyLanguage(text);
		if(lang.equals("PT"))
		getDoc().setResumo(text);
		else{
			Log log = MTDFactory.getInstancia().getLog();
			String msg = "Resumo não escrito em português, mas em "+lang;
			log.salvarDadosLog(msg);
		}
			

	}
	
	//qdc
	@EndElement(tag = "dcterms:abstract")
	public void pegarResumoQDC(ContextVariables contextVariables) {
			//getDoc().setResumo(tratarCaracteres(contextVariables.getBody()));
		String text = tratarCaracteres(contextVariables.getBody());
		String lang = identifyLanguage(text);
		if(lang.equals("pt")||lang.equals("gl"))
		getDoc().setResumo(text);
		else{//en e preenchimento errado como: vazio, xxxx
			Log log = MTDFactory.getInstancia().getLog();
			String msg = "DecodificadorDocumentoDC.pegarResumoQDC() Documento Id "+getDoc().getId() +" Erro: Resumo não escrito em português, mas em "+lang +", conteúdo: "+text ;
			log.salvarDadosLog(msg);
		}
	}

	@EndElement(tag = "dc:subject")
	public void pegarAssunto(ContextVariables contextVariables) {
		getDoc().adicionarPalavraChave(tratarCaracteres(contextVariables.getBody()).replaceAll("[,|.|;]$", ""));

	}

	@EndElement(tag = "dc:creator")
	public void pegarAutor(ContextVariables contextVariables) {
		//pode ter casos onde a tag tenha mais de uma ocorrencia de Nome
		//mesmo sem ser o autor. por isso esta sendo considerado so a primeira ocorrencia.
		String texto = contextVariables.getBody();
		String temp = texto.toLowerCase();
		if(temp.contains("(") && temp.contains("orientador") && !temp.contains("(co")){//seta orientador
			String orientador = texto.substring(0, temp.indexOf("(")).trim();
			if(orientador.contains(",")){
				String [] p =orientador.split(",");
				orientador = p[1].trim()+" "+p[0].trim();
			}
			if(!getDoc().contemOrientador())
			getDoc().setOrientador(tratarCaracteres(orientador));
		}
		else if(!getDoc().contemAutor()){//seta autor
			setarAutor(texto);
		}else{
			if(!getDoc().contemOrientador() && !temp.contains("(co") && !getDoc().getAutor().contains(texto.split(",")[0])){
				String autor = texto;//autor orientador
				if(autor.contains(",")){
					String [] p =autor.split(",");
					autor = p[1].trim()+" "+p[0].trim();
				}
				autor = autor.replaceAll("\\([A-Za-zÀ-ú_.]*\\)", "").replaceAll("[O|o]rientador[a]*\\)", "").replaceAll("\\([O|o]rientador[a]*", "");
				getDoc().setOrientador(tratarCaracteres(autor));
			}
		}
	}

	/**
	 * Seta Autor do documento
	 * @param texto
	 */
	private void setarAutor(String texto) {
		String autor = texto;
				if(autor.contains(",")){
					String [] p =autor.split(",");
					autor = p[1].trim()+" "+p[0].trim();
				}
		getDoc().setAutor(tratarCaracteres(autor));
	}
	
	
	@EndElement(tag = "dc:contributor")
	public void pegarOrientador(ContextVariables contextVariables) {
		String orientador = contextVariables.getBody();
		String temp = orientador.toLowerCase();
		if(temp.contains("(") && temp.contains("orientador"))//seta orientador
			orientador = orientador.substring(0, orientador.indexOf(" (")).trim();
		if(orientador.contains(",")){
			String [] p =orientador.split(",");
			orientador = p[1].trim()+" "+p[0].trim();
		}
		if(!getDoc().contemOrientador())
		getDoc().setOrientador(tratarCaracteres(orientador));
		//getDoc().setOrientador(orientador);
	}
	
	@EndElement(tag = "dc:publisher")
	public void pegarNomeBiblioteca(ContextVariables contextVariables) {
		if(!getDoc().contemNomeInstituicao()){
			getDoc().setNomeInstituicao(tratarCaracteres(contextVariables.getBody()));
		}
	}
	
	@EndElement(tag = "dc:type")
	public void pegarGrau(ContextVariables contextVariables) {
		if(!getDoc().contemGrau()){
			String grau = tratarCaracteres(contextVariables.getBody().toLowerCase());
			
				if(grau.contains("dissertação") || grau.contains("dissertacao") || grau.contains("dissertation")){
					getDoc().setGrau("mestre");
					
				}else if (grau.contains("tese") || grau.contains("thesis")){
					getDoc().setGrau("doutor");
				}else{
					getDoc().setGrau(grau);
				}
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
				if(!getDoc().contemAutor())
					setarAutor(partes[0].split("\\;")[0]);
				setarPrograma(partes);
				setarAreaCNPQPorPrograma(getDoc().getPrograma());
				setarDataDefesa(partes[2]);
				setarGrau(partes);
			}
		}
	}
	
	private void setarPrograma(String[] partes){
		String programa = null;
		String temp;
			for(int i=2; i < partes.length; i++){
				temp = partes[i].toLowerCase();
				if(temp.contains("programa de ")){
					programa = partes[i].substring(temp.indexOf("programa de "));
					getDoc().setPrograma(tratarCaracteres(programa.split(",")[0].trim()));
					break;
				}
				else if(temp.contains("pós-graduação em")){
					programa = partes[i].substring(temp.indexOf("pós-graduação em"));
					getDoc().setPrograma("Programa de "+tratarCaracteres(programa.split(",")[0].trim()));
					break;
				}
			}
			if(!getDoc().contemPrograma()){
				//getDoc().setPrograma("NAO_INFORMADO");
				MTDFactory.getInstancia().getLog().salvarDadosLog("DecodificadorDocumentoDC.setarPrograma() Documento Id " +getDoc().getId()+" Error: campo requerido Programa não informado.");
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
					log.salvarDadosLog("DecodificadorDocumentoDC.setarGrau() Id "+getDoc().getId()+" Error: grau desconhecido: "+partes[3]);
					//log.salvarDadosLog(new Exception("DecodificadorDocumentoDC.setarGrau() Error: grau desconhecido: "+partes[3]));
					getDoc().setGrau("NAO_INFORMADO");
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
			MTDFactory.getInstancia().getLog().salvarDadosLog("DecodificadorDocumentoDC.Documento.setarAreaCNPQPorPrograma() Id "+getDoc().getId()+" Error:  programa desconhecido: "+programa);
		}
		
		if(!getDoc().contemAreaPrograma() && area != null){
			getDoc().setAreaPrograma(area);
		}		
	}
	
	private void setarDataDefesa(String data){
		if(!getDoc().contemDataDefesa()){
			getDoc().setDataDeDefesa(MTDUtil.recuperarDataFormatosSuportados(data));
		}
	}
	
	//setado pela função acima caso oai_dc, verificar.
	@EndElement(tag = "dcterms:issued")//Data de defesa
	public void pegarDataDefesa(ContextVariables contextVariables) {
		String data = contextVariables.getBody().trim();
		setarDataDefesa(data);
	}	
	
	/**
	 * Retirar os caracteres html que foram colocados dentro do texto.
	 * 
	 * @param entrada
	 * @return
	 */
	private String tratarCaracteres(String entrada){
		entrada = getHtmlToAscii(entrada);
		//entrada = entrada.replace("/", ","); //substitui /
	    //entrada = entrada.replace("\"", " ");//substitui aspas
		// entrada = entrada.replaceAll("[,|.|;]$", "");//ponto ou virgula no final
	    entrada = entrada.replace("\r", " "); //substitui retorno
	    entrada = entrada.replace("\n", " ");//substitui novalinha
	    entrada = entrada.replace("\t", " ");//substitui tabulação
	    entrada = entrada.replaceAll("[ ]{2,}", " ");//excesso de espaço em branco
	    entrada = entrada.trim();
	    return entrada;
	}
	/**
     * Transforma todas as acentuações e caracteres especiais do html no padrao ascii.
     * 
     * @return
     */
    private String getHtmlToAscii( String texto ) {
    	//TODO: fazer a substituição correta das entidades html para caracteres iso8859-1
//        texto = texto.replaceAll( "\\&aacute;", "á" ).replaceAll( "\\&eacute;", "é" ).replaceAll( "\\&iacute;", "í" ).replaceAll( "\\&oacute;", "ó" ).replaceAll( "\\&uacute;", "ú" )
//        		.replaceAll( "\\&Aacute;", "Á" ).replaceAll( "\\&Eacute;", "É" ).replaceAll( "\\&Iacute;", "Í" ).replaceAll( "\\&Oacute;", "Ó" ).replaceAll( "\\&Uacute;", "Ú" )
//                .replaceAll( "\\&acirc;", "â" ).replaceAll( "\\&ecirc;", "ê" ).replaceAll( "\\&ocirc;", "ô" )
//                .replaceAll( "\\&Acirc;", "Â" ).replaceAll( "\\&Ecirc;", "Ê" ).replaceAll( "\\&ocirc;", "Ô" )
//                .replaceAll( "\\&atilde;", "ã" ).replaceAll( "\\&otilde;", "õ" )
//                .replaceAll( "\\&Atilde;", "Ã" ).replaceAll( "\\&Otilde;", "Õ" )
//                .replaceAll( "\\&agrave;", "à" ).replaceAll( "\\&Agrave;", "À" )
//                .replaceAll( "\\&uuml;", "u" )
//                .replaceAll( "\\&ccedil;", "ç" ).replaceAll( "\\&Ccedil;", "Ç" )
//                .replaceAll( "&nbsp;", " " ).replaceAll( "&#32;", " " ).replaceAll("&#13;", "\r");//html code;
    	
    	texto = StringConverter.fromHtmlNotation(texto);
    	texto = StringConverter.corrigeAcentos(texto);//corrigindo problemas na acentuação devido a conversão de caracteres
    	texto = StringConverter.converteCaracteresEspeciais(texto);
    	Matcher m = Pattern.compile("&(#[0-9]*|[A-Za-z]{2,6});").matcher(texto);
    	Log log = MTDFactory.getInstancia().getLog();
    	String msg = "";
    	while ( m.find() )
    		msg += "DecodificadorDocumentoDC.getHtmlToAscii() Documento Id "+getDoc().getId() +" Erro: caracter não convertido: "+m.group()+"\n";
    	if(!msg.isEmpty())
    	log.salvarDadosLog(msg);
        texto = texto.replaceAll("&(#[0-9]*|[A-Za-z]{2,6});", " ");//html code
        return texto;
    }
	
	
}
