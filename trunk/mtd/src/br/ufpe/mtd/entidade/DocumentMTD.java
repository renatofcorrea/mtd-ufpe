package br.ufpe.mtd.entidade;

import java.util.Date;
import java.util.List;

import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.FieldInfo.IndexOptions;


/**
 * Classe que representa um documento
 * do projeto MTD.
 * A construcao de Objetos desta clase foi delegada a classe
 * BuilderDocumentoMTD.
 * 
 * @see BuilderDocumentMTD
 * 
 * @author djalma
 *
 */
public class DocumentMTD implements Comparable<DocumentMTD>{

	public static final String[] campos = {"titulo", "resumo","data_defesa","autor","programa", "orientador", "area_cnpq","id", "area_programa","repositorio","url","grau", "keyword"};
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
	
	private int docId;
	private String titulo;
	private String resumo;
	private List<String> keywords;
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
	private int nodo;
	private String urlNodo;
	private String stringNodo;
	private String repositorio;
	
	/**
	 * Devolve uma representacao do objeto corrente como um 
	 * 
	 * LuceneDocument (org.apache.lucene.document.Document)
	 *  
	 * @return
	 */
	public Document toDocument(){
		
		//configuracao de campo indexado
		FieldType fieldTypeIndexadoTV = new FieldType();
		fieldTypeIndexadoTV.setStoreTermVectors(true);
	    fieldTypeIndexadoTV.setStoreTermVectorPositions(true);
	    fieldTypeIndexadoTV.setIndexed(true);
	    fieldTypeIndexadoTV.setIndexOptions(IndexOptions.DOCS_AND_FREQS);
	    fieldTypeIndexadoTV.setStored(true);
	    
	    FieldType fieldTypeIndexadoSimples = new FieldType();
	    fieldTypeIndexadoSimples.setIndexed(true);
	    fieldTypeIndexadoSimples.setStored(true);
	    
	    
		
	    //configuracao de campo nao indexado
	    FieldType fieldTypeNaoIndex = new FieldType();
	    fieldTypeNaoIndex.setStored(true);
	    
	    
		Document document = new Document();
		
		for(String key: keywords){
			document.add(new Field(KEY_WORD, key ,fieldTypeIndexadoTV));
		}
		
		document.add(new Field(GRAU, this.grau!= null ?  this.grau : VAZIO, fieldTypeIndexadoSimples));
		document.add(new Field(URL, this.url!= null ?  this.url : VAZIO, fieldTypeNaoIndex));
		document.add(new Field(TITULO, this.titulo != null ? this.titulo : VAZIO, fieldTypeIndexadoTV));
		if (this.anoDefesa != null) {
			document.add(new Field(DATA_DEFESA, this.anoDefesa , fieldTypeIndexadoSimples));
		}// data nula
		document.add(new Field(RESUMO, this.resumo != null ? this.resumo: VAZIO, fieldTypeIndexadoTV));
		
		document.add(new Field(AUTOR, this.autor!= null ? this.autor : VAZIO, fieldTypeIndexadoTV));

		document.add(new Field(PROGRAMA, this.programa!= null ? this.programa : VAZIO, fieldTypeIndexadoTV));
		document.add(new Field(AREA_CNPQ, this.areaCNPQ != null ? this.areaCNPQ : VAZIO, fieldTypeIndexadoTV));
		document.add(new Field(ORIENTADOR, this.orientador!= null ? this.orientador : VAZIO, fieldTypeIndexadoTV));
		document.add(new Field(ID, this.id!= null ? this.id : VAZIO, fieldTypeNaoIndex));
		document.add(new Field(AREA_PROGRAMA, this.areaPrograma != null ? this.areaPrograma : VAZIO, fieldTypeIndexadoTV));
		document.add(new Field(REPOSITORIO, this.repositorio != null ? this.repositorio : VAZIO, fieldTypeIndexadoSimples));
		
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

	public int getNodo() {
		return nodo;
	}

	public void setNodo(int nodo) {
		this.nodo = nodo;
		this.setUrlNodo("ListaDocumentos.jsp?id=" + nodo);
		this.setStringNodo("" + nodo);
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
		this.resumo = resumo;
	}

	public List<String> getKeywords() {
		return keywords;
	}

	public void setKeywords(List<String> keywords) {
		this.keywords = keywords;
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
		setAnoDefesa(DateTools.dateToString(this.dataDeDefesa,DateTools.Resolution.YEAR));
	}

	public String getId() {
		return id;
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
		DocumentMTD outro = (DocumentMTD)obj;
		igual = id.equals(outro.id);
		
		if(igual){
			igual = url.equals(outro.url);
		}
		
		if(igual){
			igual = repositorio.equals(outro.repositorio);
		}
		
		return igual;
	}

	@Override
	public int compareTo(DocumentMTD outro) {
		
		int comparacao = id.compareTo(outro.id);
		
		if(comparacao == 0){
			comparacao = url.compareTo(outro.url);
		}
		
		if(comparacao == 0){
			comparacao = repositorio.compareTo(outro.repositorio);
		}
		
		return comparacao;
	}
	
	public boolean contemAutor(){
		return autor != null;
	}
	
	public boolean contemRepositorio(){
		return repositorio != null;
	}
	
	public void setDocId(int docId) {
		this.docId = docId;
	}
	
	public int getDocId() {
		return docId;
	}
}