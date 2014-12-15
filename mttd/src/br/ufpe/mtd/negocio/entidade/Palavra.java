package br.ufpe.mtd.negocio.entidade;

import java.io.Serializable;


public class Palavra implements Serializable{

	private static final long serialVersionUID = 1L;
	private int palavraId;	
	private String strPalavra;
	
	public Palavra(int palavraId, String strPalavra) {
		super();
		this.palavraId = palavraId;
		this.strPalavra = strPalavra;
	}

	public int getPalavraId() {
		return palavraId;
	}

	public void setPalavraId(int palavraId) {
		this.palavraId = palavraId;
	}

	public String getStrPalavra() {
		return strPalavra;
	}

	public void setStrPalavra(String strPalavra) {
		this.strPalavra = strPalavra;
	}
	
	

}
