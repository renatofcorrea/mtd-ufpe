package br.ufpe.mtd.negocio.decodificacao;

import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.ar.ArabicAnalyzer;
import org.apache.lucene.analysis.ar.ArabicLetterTokenizer;
import org.apache.lucene.analysis.ar.ArabicNormalizationFilter;
import org.apache.lucene.analysis.ar.ArabicStemFilter;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.miscellaneous.SetKeywordMarkerFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.StopwordAnalyzerBase;
import org.apache.lucene.util.Version;

/**
 * Representa o analisador de texto do sistema. Baseia-se em:
 * http://grepcode.com/file/repo1.maven.org/maven2/org.apache.lucene/lucene-analyzers/3.6.2/org/apache/lucene/analysis/ar/ArabicAnalyzer.java/
 * 
 * @author djalma
 *
 */
@SuppressWarnings("deprecation")
public class MTDAnalyzer extends StopwordAnalyzerBase {

	public final static String DEFAULT_STOPWORD_FILE = "stopwords.txt";

	public static CharArraySet getDefaultStopSet() {
		return DefaultSetHolder.DEFAULT_STOP_SET;
	}

	private static class DefaultSetHolder {
		static final CharArraySet DEFAULT_STOP_SET;

		static {
			try {
				DEFAULT_STOP_SET = loadStopwordSet(false, ArabicAnalyzer.class, DEFAULT_STOPWORD_FILE, "#");
			} catch (IOException ex) {
				// default set should always be present as it is part of the
				// distribution (JAR)
				throw new RuntimeException("Unable to load default stopword set");
			}
		}
	}

	private final CharArraySet stemExclusionSet;

	public MTDAnalyzer() {
		this(DefaultSetHolder.DEFAULT_STOP_SET);
	}

	public MTDAnalyzer(CharArraySet stopwords) {
		this(stopwords, CharArraySet.EMPTY_SET);
	}

	public MTDAnalyzer(CharArraySet stopwords, CharArraySet stemExclusionSet) {
		super(stopwords);
		this.stemExclusionSet = CharArraySet.unmodifiableSet(CharArraySet.copy(stemExclusionSet));
	}

	@Override
	protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
		final Tokenizer source = getVersion().onOrAfter(Version.LUCENE_31) ? new StandardTokenizer(reader) : new ArabicLetterTokenizer(getVersion(), reader);
		
		TokenStream result = new LowerCaseFilter(source);
		
		//filtrar os tokens de acordo com necessidade do projeto.
		result = new MTDStopFilter(result);
		
		// the order here is important: the stopword list is not normalized!
		result = new StopFilter(result, stopwords);
		// TODO maybe we should make ArabicNormalization filter also
		// KeywordAttribute aware?!
		result = new ArabicNormalizationFilter(result);
		if (!stemExclusionSet.isEmpty()) {
			result = new SetKeywordMarkerFilter(result, stemExclusionSet);
		}
		return new TokenStreamComponents(source, new ArabicStemFilter(result));
	}
}