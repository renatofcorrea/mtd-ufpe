package br.ufpe.mtd.util.analizers;
public interface TaggerInterface {
	
	public abstract TaggerData etiquetar(String texto);

	public abstract String[] getLemmas(TaggerData d);
	
	public abstract String[] getTags(TaggerData d);
	
	public abstract String[] getTokens(TaggerData d);
	
	public abstract String getName();

}