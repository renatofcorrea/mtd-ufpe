package br.ufpe.mtd.xml;


import java.io.Reader;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseTokenizer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.miscellaneous.LengthFilter;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.util.Version;

public class TextAnalyzer extends Analyzer {  
    private final Set<String> stopWords;  
    private final boolean usePorterStemming;  
  
    public TextAnalyzer(Set<String> stopWords, boolean usePorterStemming) {  
        this.stopWords = stopWords;  
        this.usePorterStemming = usePorterStemming;  
    }  

    //TODO: Procurar analisador que coloque para caixa baixa e que nao retire os numeros em palavras que contem numero
	@Override
	protected TokenStreamComponents createComponents(String arg0, Reader reader) {
		Version versao = Version.LUCENE_46;
		final Tokenizer source = new LowerCaseTokenizer(versao,reader);
        
		TokenStream result = new LengthFilter(versao,source, 3, Integer.MAX_VALUE); 
		if (!stopWords.isEmpty()) {
        	
        	CharArraySet charArraySet = new CharArraySet(versao, stopWords, true);
            result = new StopFilter(versao,result, charArraySet);  
        }  
        if (usePorterStemming) {  
            result = new PorterStemFilter(result);  
        }  
        
        return new TokenStreamComponents(source, result);
	}
}
