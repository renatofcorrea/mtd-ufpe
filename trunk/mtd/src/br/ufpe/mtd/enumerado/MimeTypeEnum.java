package br.ufpe.mtd.enumerado;

public enum MimeTypeEnum {
	
	HTML("text/html"), XML("text/xml");
	
	private String codigo;
	
	private MimeTypeEnum(String codigo) {
		this.codigo = codigo;
	}

	public String getCodigo() {
		return codigo;
	}
}
