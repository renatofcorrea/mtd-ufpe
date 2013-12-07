package br.ufpe.mtd.util;

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
