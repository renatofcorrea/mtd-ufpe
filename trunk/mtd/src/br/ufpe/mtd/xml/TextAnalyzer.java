package br.ufpe.mtd.xml;

import java.io.Reader;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseTokenizer;
import org.apache.lucene.analysis.PorterStemFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;

public class TextAnalyzer extends Analyzer {  
    private final Set<String> stopWords;  
    private final boolean usePorterStemming;  
  
    public TextAnalyzer(Set<String> stopWords, boolean usePorterStemming) {  
        this.stopWords = stopWords;  
        this.usePorterStemming = usePorterStemming;  
    }  
  
    public TokenStream tokenStream(String fieldName, Reader reader) {  
        TokenStream result = new LowerCaseTokenizer(reader);  
        if (!stopWords.isEmpty()) {  
            result = new StopFilter(result, stopWords, true);  
        }  
        if (usePorterStemming) {  
            result = new PorterStemFilter(result);  
        }  
        return result;  
    }

	  
}
