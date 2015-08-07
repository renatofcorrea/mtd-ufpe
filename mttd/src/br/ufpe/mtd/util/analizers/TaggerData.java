package br.ufpe.mtd.util.analizers;

public class TaggerData {
	private String textoEtiquetado;
	private String textoLemas;
	private String textoTokens;
	private String textoTags;

	public TaggerData(String textoEtiquetado, String textoLemas,
			String textoTokens, String textoTags) {
		this.textoEtiquetado = textoEtiquetado;
		this.textoLemas = textoLemas;
		this.textoTokens = textoTokens;
		this.textoTags = textoTags;
	}

	public TaggerData() {
		this.textoEtiquetado = new String();
		this.textoLemas = new String();
		this.textoTokens = new String();
		this.textoTags = new String();
	}

	public String getTextoEtiquetado() {
		return textoEtiquetado;
	}

	public void setTextoEtiquetado(String textoEtiquetado) {
		this.textoEtiquetado = textoEtiquetado;
	}

	public String getTextoLemas() {
		return textoLemas;
	}

	public void setTextoLemas(String textoLemas) {
		this.textoLemas = textoLemas;
	}

	public String getTextoTokens() {
		return textoTokens;
	}

	public void setTextoTokens(String textoTokens) {
		this.textoTokens = textoTokens;
	}

	public String getTextoTags() {
		return textoTags;
	}

	public void setTextoTags(String textoTags) {
		this.textoTags = textoTags;
	}
	
	public String toString(){
		String delim = "<>";
		return this.getTextoEtiquetado()+delim+this.getTextoTokens()+delim+this.getTextoLemas()+delim+this.getTextoTags();
	}

	public static TaggerData parse(String string) {
		String [] temp = string.split("\\<>");
		if(temp.length == 4)
		return new TaggerData(temp[0], temp[1],temp[2], temp[3]);
		else
			return null;
	}
}

