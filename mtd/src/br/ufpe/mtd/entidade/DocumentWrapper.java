package br.ufpe.mtd.entidade;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import br.ufpe.mtd.util.MTDUtil;


/**
 * Ao criar um documentWrapper o mesmo ja carrega dentro de si
 * um document vazio pronto para colocar os dados
 * 
 * @author djalma
 *
 */
public class DocumentWrapper implements Comparable<DocumentWrapper>{

	/**
	 * 
	 */
	private Document document;
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
	
	
	public DocumentWrapper() {
		this.keywords = new ArrayList<String>();
		document = new Document();
	}
	
	public DocumentWrapper(Document document) {
		this(document.get("titulo"), 
				document.get("resumo"), 
				null,
				MTDUtil.recuperarDataFormatosSuportados(document.get("dataDefesa").trim()), 
				document.get("autor"), 
				document.get("programa"), 
				document.get("orientador"), 
				document.get("areaCNPQ"), 
				document.get("id"), 
				document.get("areaPrograma"));
				setRepositorio(document.get("repositorio"));
				setUrl(document.get("url"));
				setGrau(document.get("Grau"));
	}



	public DocumentWrapper(String titulo, String resumo,
			List<String> keywords, Date dataDefesa, String autor,
			String programa, String orientador, String areaCNPQ, String id,
			String areaPrograma) {
		this();
		setTitulo(titulo);
		setResumo(resumo);
		setKeywords(keywords);
		setDataDeDefesa(dataDefesa);
		setAutor(autor);
		setPrograma(programa);
		setOrientador(orientador);
		setAreaCNPQ(areaCNPQ);
		setId(id);
		setAreaPrograma(areaPrograma);
	}
	
	public Document getDocument(){
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
		getDocument().add(new Field("keyword", key,Field.Store.YES, Field.Index.ANALYZED));
	}

	public String getGrau() {
		return grau;
	}

	public void setGrau(String grau) {
		this.grau = grau;
		getDocument().add(new Field("grau", this.grau!= null ?  this.grau : "", Field.Store.YES, Field.Index.ANALYZED));
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
		getDocument().add(new Field("url", this.url!= null ?  this.url : "", Field.Store.YES, Field.Index.NO));
	}

	public String getTitulo() {
		return titulo;
	}

	public void setTitulo(String titulo) {
		this.titulo = titulo;
		getDocument().add(new Field("title", this.titulo != null ? this.titulo : "", Field.Store.YES,
				Field.Index.ANALYZED));
	}

	public String getAnoDefesa() {
		return anoDefesa;
	}

	public void setAnoDefesa(String anoDefesa) {
		this.anoDefesa = anoDefesa;
		if (this.anoDefesa != null) {
			getDocument().add(new Field("dataDefesa", this.anoDefesa , Field.Store.YES,
					Field.Index.ANALYZED));
		}// data nula
	}

	public String getResumo() {
		return resumo;
	}

	public void setResumo(String resumo) {
		this.resumo = resumo;
		getDocument().add(new Field("resumo", this.resumo != null ? this.resumo: "", Field.Store.YES,
				Field.Index.ANALYZED));
	}

	public List<String> getKeywords() {
		return keywords;
	}

	public void setKeywords(List<String> keywords) {
		this.keywords = keywords;
		for (int i = 0; i < this.keywords.size() && this.keywords.get(i) != null; i++) {
			getDocument().add(new Field("keyword", this.keywords.get(i), Field.Store.YES,
					Field.Index.ANALYZED));
		}
	}

	public String getAutor() {
		return autor;
	}

	public boolean contemAutor(){
		return getDocument().get("autor") != null;
	}
	
	public boolean contemRepositorio(){
		return getDocument().get("repositorio") != null;
	}
	
	public void setAutor(String autor) {
		this.autor = autor;
		getDocument().add(new Field("autor", this.autor!= null ? this.autor : "", Field.Store.YES, Field.Index.ANALYZED));
	}

	public String getPrograma() {
		return programa;
	}

	public void setPrograma(String programa) {
		this.programa = programa;
		getDocument().add(new Field("programa", this.programa!= null ? this.programa : "", Field.Store.YES,Field.Index.ANALYZED));

	}

	public String getAreaCNPQ() {
		return areaCNPQ;
	}

	public void setAreaCNPQ(String areaCNPQ) {
		this.areaCNPQ = areaCNPQ;
		getDocument().add(new Field("areaCNPQ", this.areaCNPQ != null ? this.areaCNPQ : "", Field.Store.YES,Field.Index.ANALYZED));
	}

	public String getOrientador() {
		return orientador;
	}

	public void setOrientador(String orientador) {
		this.orientador = orientador;
		getDocument().add(new Field("orientador", this.orientador!= null ? this.orientador : "", Field.Store.YES,Field.Index.ANALYZED));
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
		getDocument().add(new Field("id", this.id!= null ? this.id : "", Field.Store.YES, Field.Index.NO));
	}

	public String getAreaPrograma() {
		return areaPrograma;
	}

	public void setAreaPrograma(String areaPrograma) {
		this.areaPrograma = areaPrograma;
		getDocument().add(new Field("areaPrograma", this.areaPrograma != null ? this.areaPrograma : "", Field.Store.YES,Field.Index.ANALYZED));
	}
	
	public String getRepositorio() {
		return repositorio;
	}
	
	public void setRepositorio(String repositorio) {
		this.repositorio = repositorio;
		getDocument().add(new Field("repositorio", this.repositorio != null ? this.repositorio : "", Field.Store.YES,Field.Index.ANALYZED));
	}
	
	@Override
	public boolean equals(Object obj) {
		boolean igual = false;
		DocumentWrapper outro = (DocumentWrapper)obj;
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
	public int compareTo(DocumentWrapper outro) {
		
		int comparacao = id.compareTo(outro.id);
		
		if(comparacao == 0){
			comparacao = url.compareTo(outro.url);
		}
		
		if(comparacao == 0){
			comparacao = repositorio.compareTo(outro.repositorio);
		}
		
		return comparacao;
	}
	
	
}
