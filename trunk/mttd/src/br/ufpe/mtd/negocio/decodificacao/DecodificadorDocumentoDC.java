package br.ufpe.mtd.negocio.decodificacao;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.scene.text.Text;
import net.sf.jColtrane.annotations.methods.ContainAttribute;
import net.sf.jColtrane.annotations.methods.EndElement;
import net.sf.jColtrane.annotations.methods.InsideElement;
import net.sf.jColtrane.annotations.methods.StartElement;
import net.sf.jColtrane.handler.ContextVariables;
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
 * Usa o padr�o de metadados Dublin Core
 * Util para harvesting de reposit�rios institucionais
 * @author renato
 * 
 * 
 */

//URLs de acesso as teses e disserta��es no repositorio da UFPE
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
		getDoc().setRepositorio(identificador.split(":")[1]);//seta tamb�m reposit�rio
	}
	
	@InsideElement(tag = "header")
	@EndElement(tag = "identifier")
	public void pegarId(ContextVariables contextVariables) {
		String identificador = contextVariables.getBody();
		if(!identificador.contains("localhost")){
		getDoc().setId(identificador);
		getDoc().setRepositorio(identificador.split(":")[1]);//seta tamb�m reposit�rio
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
				autor = autor.replaceAll("\\([A-Za-z�-�_.]*\\)", "").replaceAll("[O|o]rientador[a]*\\)", "").replaceAll("\\([O|o]rientador[a]*", "");
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
			
				if(grau.contains("disserta��o") || grau.contains("dissertacao") || grau.contains("dissertation")){
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
				//Endere�o do handle errado em repositorio.ufpe.br
				//Informado: http://hdl.handle.net/123456789/9400
				//Correto: http://www.repositorio.ufpe.br/handle/123456789/9400
				temp = temp.replace("http://hdl.handle.net/", "http://www.repositorio.ufpe.br/handle/");
			}
			getDoc().setUrl(temp);
		
		}else{
			
			String [] partes = temp.split("\\. ");
			// 0 - autores separados por ;
			// 1 - t�tulo
			// 2 - ano
			// 3 - Tipo: Disserta��o (Mestrado) / Tese (Doutorado)
			// 4 - programa, institui��o, cidade, ano
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
				else if(temp.contains("p�s-gradua��o em")){
					programa = partes[i].substring(temp.indexOf("p�s-gradua��o em"));
					getDoc().setPrograma("Programa de "+tratarCaracteres(programa.split(",")[0].trim()));
					break;
				}
			}
			if(!getDoc().contemPrograma()){
				//getDoc().setPrograma("nao_informado");
				MTDFactory.getInstancia().getLog().salvarDadosLog("Documento Id " +getDoc().getId()+" campo requerido Programa n�o informado.");
			}
	}
	
	private void setarGrau(String[] partes){
		for(int i=3; i < partes.length; i++){
			if(partes[i].toLowerCase().contains("disserta��o") || partes[i].toLowerCase().contains("tese")){
				if(partes[i].toLowerCase().contains("disserta��o")){
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
			MTDFactory.getInstancia().getLog().salvarDadosLog("Documento Id "+getDoc().getId()+" programa desconhecido: "+programa);
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
	
	//setado pela fun��o acima caso oai_dc, verificar.
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
		entrada = entrada.replace("/", ","); //substitui /
	    entrada = entrada.replace("\"", " ");//substitui aspas
	    entrada = entrada.replace("\r", " "); //substitui retorno
	    entrada = entrada.replace("\n", " ");//substitui novalinha
	    entrada = entrada.replace("\t", " ");//substitui tabula��o
	    entrada = entrada.replaceAll("[ ]{2,}", " ");//excesso de espa�o em branco
	    entrada = entrada.replaceAll("[,|.|;]$", "");//ponto ou virgula no final
	    entrada = entrada.trim();
	    return entrada;
	}
	/**
     * Transforma todas as acentua��es e caracteres especiais do html no padrao ascii.
     * 
     * @return
     */
    private String getHtmlToAscii( String texto ) {
    	//TODO: fazer a substitui��o correta das entidades html para caracteres iso8859-1
//        texto = texto.replaceAll( "\\&aacute;", "�" ).replaceAll( "\\&eacute;", "�" ).replaceAll( "\\&iacute;", "�" ).replaceAll( "\\&oacute;", "�" ).replaceAll( "\\&uacute;", "�" )
//        		.replaceAll( "\\&Aacute;", "�" ).replaceAll( "\\&Eacute;", "�" ).replaceAll( "\\&Iacute;", "�" ).replaceAll( "\\&Oacute;", "�" ).replaceAll( "\\&Uacute;", "�" )
//                .replaceAll( "\\&acirc;", "�" ).replaceAll( "\\&ecirc;", "�" ).replaceAll( "\\&ocirc;", "�" )
//                .replaceAll( "\\&Acirc;", "�" ).replaceAll( "\\&Ecirc;", "�" ).replaceAll( "\\&ocirc;", "�" )
//                .replaceAll( "\\&atilde;", "�" ).replaceAll( "\\&otilde;", "�" )
//                .replaceAll( "\\&Atilde;", "�" ).replaceAll( "\\&Otilde;", "�" )
//                .replaceAll( "\\&agrave;", "�" ).replaceAll( "\\&Agrave;", "�" )
//                .replaceAll( "\\&uuml;", "u" )
//                .replaceAll( "\\&ccedil;", "�" ).replaceAll( "\\&Ccedil;", "�" )
//                .replaceAll( "&nbsp;", " " ).replaceAll( "&#32;", " " ).replaceAll("&#13;", "\r");//html code;
    	
    	texto = StringConverter.fromHtmlNotation(texto);
    	texto = StringConverter.converteCaracteresEspeciais(texto);
    	Matcher m = Pattern.compile("&(#[0-9]*|[A-Za-z]{2,6});").matcher(texto);
    	Log log = MTDFactory.getInstancia().getLog();
    	while ( m.find() )
    		log.salvarDadosLog("Documento Id "+getDoc().getDocId() +" DecodificadorDocumentoDC.getHtmlAscii, not converted: "+m.group());
    	
        texto = texto.replaceAll("&(#[0-9]*|[A-Za-z]{2,6});", " ");//html code
        return texto;
    }
	
	
}
