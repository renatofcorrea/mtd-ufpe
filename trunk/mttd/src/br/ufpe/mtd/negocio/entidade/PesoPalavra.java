package br.ufpe.mtd.negocio.entidade;

import java.io.Serializable;

/**
 * Classe que repsenta uma palavra
 * que sera organizada segundo determinadas
 * caracteristicas que precisam ser guardadas.
 * 
 * @author djalma
 *
 */
public class PesoPalavra implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int ordemPesoPalavra;
	private Double peso;
	private int idPalavra;
	private Palavra palavra;

	public PesoPalavra(Double peso, int posicao) {
		this.peso = peso;
		this.idPalavra = posicao;
	}

	@Override
	public String toString() {

		return "{posicao:" + idPalavra + ", peso:" + peso+ ", palavra:" + palavra.getStrPalavra()+ ", palavraId:" + palavra.getPalavraId() + "}";
	}

	public Double getPeso() {
		return peso;
	}

	public void setPeso(Double peso) {
		this.peso = peso;
	}

	public int getIdPalavra() {
		return idPalavra;
	}

	public void setPosicao(int posicao) {
		this.idPalavra = posicao;
	}

	public Palavra getPalavra() {
		return palavra;
	}
	
	public void setPalavra(Palavra palavra) {
		this.palavra = palavra;
	}
	
	public int getOrdemPesoPalavra() {
		return ordemPesoPalavra;
	}
	
	public void setOrdemPesoPalavra(int ordemPesoPalavra) {
		this.ordemPesoPalavra = ordemPesoPalavra;
	}
}