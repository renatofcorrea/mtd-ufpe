package br.ufpe.mtd.entidade;

public class Identificador {

	private String id;
	private boolean deletado;
	
	public Identificador() {
	}
	
	public Identificador(String id, boolean deletado) {
		super();
		this.id = id;
		this.deletado = deletado;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public boolean isDeletado() {
		return deletado;
	}

	public void setDeletado(boolean deletado) {
		this.deletado = deletado;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		Identificador other = (Identificador) obj;
		return id.equals(other.id);
	}
	
	
}
