package br.ufpe.mtd.util.enumerado;

public enum TipoAmbiente {

	DESENVOLVIMENTO, HOMOLOGACAO, PRODUCAO;
	
	public boolean isDesenvovimento() {
		
		return this.equals(TipoAmbiente.DESENVOLVIMENTO);
	}

	public boolean isHomologacao() {
		return this.equals(TipoAmbiente.HOMOLOGACAO);
	}

	public boolean isProducao() {
		return this.equals(TipoAmbiente.PRODUCAO);
	}

}
