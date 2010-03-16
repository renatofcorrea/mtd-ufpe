package main;

import java.util.Date;
import java.util.Vector;

public class Documento {

	private String titulo;
	private String descricao;
	private Vector<String> keywords;
	private Date dataDeDefesa;
	private String autor;
	private String programa;
	private String orientador;
	private String areaCNPQ;
	private Long id;

	public Documento() {
		this.titulo = null;
		this.descricao =null;
		this.keywords = new Vector<String>();
		this.autor = null;
		this.programa = null;
		this.orientador = null;
		this.areaCNPQ = null;
		this.dataDeDefesa = null;
		this.id = null;
	}
	
	public void adicionarPalavraChave(String key){
		this.keywords.add(key);
	}

	public String getTitulo() {
		return titulo;
	}

	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public Vector<String> getKeywords() {
		return keywords;
	}

	public void setKeywords(Vector<String> keywords) {
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
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

}
