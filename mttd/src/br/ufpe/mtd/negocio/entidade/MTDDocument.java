package br.ufpe.mtd.negocio.entidade;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.FieldInfo.IndexOptions;

import br.ufpe.mtd.util.MTDFactory;
import br.ufpe.mtd.util.MTDIterator;
import br.ufpe.mtd.util.analizers.SNAnalyser;
import br.ufpe.mtd.util.enumerado.AreaCNPQEnum;
import br.ufpe.mtd.util.enumerado.MTDArquivoEnum;


/**
 * Classe que representa um documento
 * do projeto MTD.
 * A construcao de Objetos desta clase foi delegada a classe
 * BuilderDocumentoMTD.
 * 
 * @see MTDDocumentBuilder
 * 
 * @author djalma
 *
 */
public class MTDDocument implements Comparable<MTDDocument>, Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static final String[] campos = {"titulo", "resumo","data_defesa","autor","programa", "orientador", "area_cnpq","id", "area_programa","repositorio","url","grau", "keyword","instituicao","sintagma_nominal"};
	public static final String VAZIO = "";
	public static final String TITULO = campos[0];
	public static final String RESUMO = campos[1];
	public static final String DATA_DEFESA = campos[2];
	public static final String AUTOR = campos[3];
	public static final String PROGRAMA = campos[4];
	public static final String ORIENTADOR = campos[5];
	public static final String AREA_CNPQ = campos[6];
	public static final String ID = campos[7];
	public static final String AREA_PROGRAMA = campos[8];
	public static final String REPOSITORIO = campos[9];
	public static final String URL = campos[10];
	public static final String GRAU = campos[11];
	public static final String KEY_WORD = campos[12];
	public static final String INSTITUICAO = campos[13];

	//campo gerado a partir de outros. 
	//por isso desconsidera no processamento na hora do parser
	public static final String SINTAGMA_NOMINAL = campos[14];
	
	private int docId;
	private String titulo;
	private String resumo;
	private List<String> keywords;
	private List<String> sintagmas;
	private Date dataDeDefesa;
	private String autor;
	private String programa;
	private String orientador;
	private String areaCNPQ;
	private String id;
	private String areaPrograma;
	private String anoDefesa;
	private String grau;
	private String url;
	private Nodo nodo;
	private String urlNodo;
	private String stringNodo;
	private String repositorio;
	private String nomeInstituicao;
	
	/**
	 * Devolve uma representacao do objeto corrente como um 
	 * 
	 * LuceneDocument (org.apache.lucene.document.Document)
	 *  
	 * @return
	 */
	public Document toDocument(){
		//configuracao de campo indexado
		FieldType fieldTypeIndexadoTV = FieldFactory.fieldIndexado();
	    FieldType fieldTypeIndexadoSimples = FieldFactory.fieldIndexadoSimples();
	    FieldType fieldTypeNaoIndex = FieldFactory.fieldNaoIndexado();
	    
		Document document = new Document();
		
		for(String key: keywords){
			document.add(new Field(KEY_WORD, key ,fieldTypeIndexadoTV));
		}
		
		document.add(new Field(GRAU, this.grau!= null ?  this.grau : VAZIO, fieldTypeIndexadoSimples));
		document.add(new Field(DATA_DEFESA, this.anoDefesa != null ? this.anoDefesa : VAZIO, fieldTypeIndexadoSimples));
		document.add(new Field(URL, this.url!= null ?  this.url : VAZIO, fieldTypeNaoIndex));
		document.add(new Field(TITULO, this.titulo != null ? this.titulo : VAZIO, fieldTypeIndexadoTV));
		
		document.add(new Field(RESUMO, this.resumo != null ? this.resumo: VAZIO, fieldTypeIndexadoTV));
		
		document.add(new Field(AUTOR, this.autor!= null ? this.autor : VAZIO, fieldTypeIndexadoTV));

		document.add(new Field(PROGRAMA, this.programa!= null ? this.programa : VAZIO, fieldTypeIndexadoTV));
		document.add(new Field(AREA_CNPQ, this.areaCNPQ != null ? this.areaCNPQ : VAZIO, fieldTypeIndexadoTV));
		document.add(new Field(ORIENTADOR, this.orientador!= null ? this.orientador : VAZIO, fieldTypeIndexadoTV));
		document.add(new Field(ID, this.id!= null ? this.id : VAZIO, fieldTypeNaoIndex));
		document.add(new Field(AREA_PROGRAMA, this.areaPrograma != null ? this.areaPrograma : VAZIO, fieldTypeIndexadoTV));
		document.add(new Field(REPOSITORIO, this.repositorio != null ? this.repositorio : VAZIO, fieldTypeIndexadoSimples));
		document.add(new Field(INSTITUICAO, this.nomeInstituicao != null ? this.nomeInstituicao : VAZIO, fieldTypeIndexadoSimples));
		
		return document;
	}
	
	public Document toDocumentComSintagmas(HashSet<String> hs){
		Document  document = toDocument();
		try {
			
			//TODO: investigar futuramente o ganho ao extrair sintagmas do título+resumo
			List<String> sintagmas = SNAnalyser.extrairSintagmasNominais(new SNAnalyser(hs),titulo+" . "+resumo);
			for(String key: sintagmas){
				document.add(new Field(SINTAGMA_NOMINAL, key ,FieldFactory.fieldIndexado()));
			}
		} catch (Exception e) {
			MTDFactory.getInstancia().getLog().salvarDadosLog(e);
		}
		return document;
	}
	
	public String getStringNodo() {
		return stringNodo;
	}

	public void setStringNodo(String stringNodo) {
		this.stringNodo = stringNodo;
	}

	public String getUrlNodo() {
		return urlNodo;
	}

	public void setUrlNodo(String urlNodo) {
		this.urlNodo = urlNodo;
	}

	public void adicionarPalavraChave(String key) {
		this.keywords.add(key);
	}

	public String getGrau() {
		return grau;
	}

	public void setGrau(String grau) {
		this.grau = grau;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getTitulo() {
		return titulo;
	}

	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}

	public String getAnoDefesa() {
		return anoDefesa;
	}

	public void setAnoDefesa(String anoDefesa) {
		this.anoDefesa = anoDefesa;
	}

	public String getResumo() {
		return resumo;
	}

	public void setResumo(String resumo) {
		resumo.replace('\n',' ');
		resumo.replace('\r',' ');
		this.resumo = resumo;
	}

	public List<String> getKeywords() {
		return keywords;
	}
	
	public String toStringKeyWord(){
		String str = new String("");
		
		if(keywords!= null){
			str+="[";
			for(String aux: keywords){
				str+= aux+" , ";
			}
			
			str+= getAreaCNPQ();
			
			str+="]";
		}
		
		return str;
	}

	public void setKeywords(List<String> keywords) {
		this.keywords = keywords;
	}
	
	public List<String> getSintagmas() {
		return sintagmas;
	}
	
	public void setSintagmas(List<String> sintagmas) {
		this.sintagmas = sintagmas;
	}

	public String getAutor() {
		return autor;
	}
	
	public void setAutor(String autor) {
		this.autor = autor;
	}

	public String getPrograma() {
		return programa;
	}

	public void setPrograma(String programa) {
		this.programa = programa;

	}

	public String getAreaCNPQ() {
		return areaCNPQ;
	}

	public void setAreaCNPQ(String areaCNPQ) {
		this.areaCNPQ = areaCNPQ;
	}

	public String getOrientador() {
		return orientador;
	}

	public void setOrientador(String orientador) {
		this.orientador = orientador;
	}

	public Date getDataDeDefesa() {
		return dataDeDefesa;
	}

	public void setDataDeDefesa(Date dataDeDefesa) {
		this.dataDeDefesa = dataDeDefesa;
		setAnoDefesa(" "+DateTools.dateToString(this.dataDeDefesa,DateTools.Resolution.YEAR));
	}

	public String getId() {
		return id;
	}
	
	public int getIdHasint(){
		int i = id.lastIndexOf('/');
		i = (i>0)?i:0;
		String s = id.substring(i+1);
		return Integer.parseInt(s);
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getAreaPrograma() {
		return areaPrograma;
	}

	public void setAreaPrograma(String areaPrograma) {
		this.areaPrograma = areaPrograma;
	}
	
	public String getRepositorio() {
		return repositorio;
	}
	
	public void setRepositorio(String repositorio) {
		this.repositorio = repositorio;
	}
	
	@Override
	public boolean equals(Object obj) {
		boolean igual = false;
		MTDDocument outro = (MTDDocument)obj;
		
		igual = (docId == outro.docId);
		
		if(igual && id != null){
			igual = id.equals(outro.id);
		}
		
		if(igual && url != null){
			igual = url.equals(outro.url);
		}
		
		if(igual && repositorio != null){
			igual = repositorio.equals(outro.repositorio);
		}
		
		return igual;
	}

	@Override
	public int compareTo(MTDDocument outro) {
		
		int comparacao = id.compareTo(outro.id);
		
		if(comparacao == 0){
			if(url != null){
				comparacao = url.compareTo(outro.url);
			}
		}
		
		if(comparacao == 0){
			if(repositorio != null){
				comparacao = repositorio.compareTo(outro.repositorio);
			}
		}
		
		return comparacao;
	}
	
	public boolean contemAutor(){
		return autor != null;
	}
	
	public boolean contemOrientador(){
		return orientador != null;
	}
	
	public boolean contemDataDefesa(){
		return dataDeDefesa != null;
	}
	
	public boolean contemPrograma(){
		return programa != null;
	}
	
	public boolean contemTitulo(){
		return titulo != null;
	}
	
	public boolean contemResumo(){
		return resumo != null;
	}
	
	public boolean contemPalavrasChaves(){
		return keywords != null && keywords.size() > 0;
	}
	
	public boolean contemRepositorio(){
		return repositorio != null;
	}
	
	public boolean contemNomeInstituicao(){
		return nomeInstituicao != null;
	}

	public boolean contemAreaPrograma(){
		return areaPrograma != null;
	}	
	
	public boolean contemGrau() {
		return grau != null;
	}
	
	public boolean contemCamposRequeridos(){
		return contemAutor()  && contemPrograma() && contemDataDefesa() && contemTitulo() && contemResumo() && contemPalavrasChaves();
	}
	
	public String faltandoCamposRequeridos(){
		String s = "";
		if(!contemAutor()) s+= " Autor,";
		if(!contemPrograma()) s+= " Programa,";
		if(!contemDataDefesa())s+= " Data,";
		if(!contemTitulo())s+= " Titulo,";
		if(!contemResumo())s+= " Resumo,";
		if(!contemPalavrasChaves())s+= " Keywords,";
	
	return s;
	}
	
	public void setDocId(int docId) {
		this.docId = docId;
	}
	
	public int getDocId() {
		return docId;
	}


	public Nodo getNodo() {
		return nodo;
	}


	public void setNodo(Nodo nodo) {
		this.nodo = nodo;
	}


	public String getNomeInstituicao() {
		return nomeInstituicao;
	}


	public void setNomeInstituicao(String nomeInstituicao) {
		this.nomeInstituicao = nomeInstituicao;
	}
	
	public AreaCNPQEnum getGrandeArea() {
		AreaCNPQEnum area = AreaCNPQEnum.getAreaCNPQPorSubArea(getAreaCNPQ());
		
		if(area.equals(AreaCNPQEnum.NAO_ENCONTRADO)){
			area = AreaCNPQEnum.getGrandeAreaCNPQPorPrograma(programa);
		}
		
		return area;
	}
	
	
	public static class FieldFactory{
		
		public static FieldType fieldIndexado(){
			FieldType fieldTypeIndexadoTV = new FieldType();
			fieldTypeIndexadoTV.setStoreTermVectors(true);
			fieldTypeIndexadoTV.setStoreTermVectorPositions(true);
			fieldTypeIndexadoTV.setIndexed(true);
			fieldTypeIndexadoTV.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
			fieldTypeIndexadoTV.setStored(true);
			return fieldTypeIndexadoTV;
		}
		
		public static FieldType fieldIndexadoSimples(){
			FieldType fieldTypeIndexadoSimples = new FieldType();
			fieldTypeIndexadoSimples.setIndexed(true);
			fieldTypeIndexadoSimples.setStored(true);
			return fieldTypeIndexadoSimples;
		}
		
		public static FieldType fieldNaoIndexado(){
			FieldType fieldTypeNaoIndex = new FieldType();
			fieldTypeNaoIndex.setStored(true);
			return fieldTypeNaoIndex;
		}
	}


	
}