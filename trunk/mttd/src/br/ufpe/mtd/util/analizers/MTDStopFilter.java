package br.ufpe.mtd.util.analizers;

import java.io.IOException;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.FilteringTokenFilter;
/**
 * Baseado em 
 * 
 * http://grepcode.com/file/repo1.maven.org/maven2/org.apache.lucene/lucene-analyzers-common/4.9.0/org/apache/lucene/analysis/core/StopFilter.java/
 * 
 * Classe que realiza a filtragem eliminado os termos
 * que sao apenas numericos ou tamanho menor que 3 caracteres.
 * 
 * @author djalma
 * 
 */
public class MTDStopFilter extends FilteringTokenFilter {
	
	private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
	
	public MTDStopFilter(TokenStream in) {
		super(in);
	}

	@Override
	protected boolean accept() throws IOException {
		
		String aux = termAtt.toString().trim();
    	if (aux.length() < 3 || (aux.matches("[0-9]+[.|,]?[0-9]*[a-z°ºª]{0,3}?")&& !aux.matches("[0-9]{4}"))) {//("^[0-9]+$")
    		return false;
    	}
    	
		return true;
	}
}
