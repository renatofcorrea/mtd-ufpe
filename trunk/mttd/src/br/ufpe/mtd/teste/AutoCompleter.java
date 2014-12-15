
package br.ufpe.mtd.teste;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.ngram.EdgeNGramTokenFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.SortField.Type;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.spell.LuceneDictionary;
import org.apache.lucene.search.suggest.Lookup.LookupResult;
import org.apache.lucene.search.suggest.analyzing.FreeTextSuggester;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import br.ufpe.mtd.negocio.entidade.MTDDocument;
import br.ufpe.mtd.util.enumerado.MTDArquivoEnum;

/**
 * Search term auto-completer, works for single terms (so use on the last term
 * of the query).
 * <p>
 * Returns more popular terms first.
 * 
 * @author Mat Mannion, M.Mannion@warwick.ac.uk
 */
public final class AutoCompleter {

	private static final String GRAMMED_WORDS_FIELD = "words";

    private static final String SOURCE_WORD_FIELD = "sourceWord";

    private static final String COUNT_FIELD = "count";

    private static Version version;
    
    private static final String[] ENGLISH_STOP_WORDS = {
    "a", "an", "and", "are", "as", "at", "be", "but", "by",
    "for", "i", "if", "in", "into", "is",
    "no", "not", "of", "on", "or", "s", "such",
    "t", "that", "the", "their", "then", "there", "these",
    "they", "this", "to", "was", "will", "with"
    };

    private final Directory autoCompleteDirectory;

    private IndexReader autoCompleteReader;

    private IndexSearcher autoCompleteSearcher;

    public AutoCompleter(File dir, Version version) throws IOException {
    	autoCompleteDirectory = FSDirectory.open(dir);
    	autoCompleteReader = DirectoryReader.open(autoCompleteDirectory);
    	autoCompleteSearcher = new IndexSearcher(autoCompleteReader);
    	AutoCompleter.version = version;
    }

    public List<String> suggestTermsFor(String term) throws IOException {
    	// get the top 5 terms for query
    	Query query = new TermQuery(new Term(MTDDocument.KEY_WORD, term));
    	SortField sf = new SortField(MTDDocument.KEY_WORD, Type.STRING);
    	Sort sort = new Sort(sf);
    	
    	TopDocs docs = autoCompleteSearcher.search(query, 5, sort);
    	List<String> suggestions = new ArrayList<String>();
    	for (ScoreDoc doc : docs.scoreDocs) {
    		suggestions.add(autoCompleteReader.document(doc.doc).get(MTDDocument.KEY_WORD));
    	}

    	return suggestions;
    }
    
    static Analyzer analiser = new Analyzer() {

		@Override
		protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
			Collection<String> c = new ArrayList<String>();
			
			CharArraySet stopWords = new CharArraySet(version, c, true);
			StandardTokenizer token = new StandardTokenizer(version, reader);

			TokenStream result = new StandardFilter(version,token);
			result = new LowerCaseFilter(version,result);
			//result = new ISOLatin1AccentFilter(Version.LUCENE_46,result);
			result = new StopFilter(version, result,stopWords);
			result = new EdgeNGramTokenFilter(version, result, 1, 20);

			return new TokenStreamComponents(token, result);
		}
	};


    @SuppressWarnings("unchecked")
    public void reIndex(Directory sourceDirectory, String fieldToAutocomplete)
    		throws CorruptIndexException, IOException {
    	// build a dictionary (from the spell package)
    	IndexReader sourceReader = IndexReader.open(sourceDirectory);
    	LuceneDictionary dict = new LuceneDictionary(sourceReader,fieldToAutocomplete);
    	    	
    	// use a custom analyzer so we can do EdgeNGramFiltering
    	FSDirectory dir = FSDirectory.open(MTDArquivoEnum.INDICE_DIR.getArquivo());
		IndexWriterConfig config = new IndexWriterConfig(version, analiser);
		IndexWriter indexWriter = new IndexWriter(dir, config);


    	// go through every word, storing the original word (incl. n-grams) 
    	// and the number of times it occurs
    	Map<String, Integer> wordsMap = new HashMap<String, Integer>();

    	Iterator<String> iter = (Iterator<String>) dict.getEntryIterator();
    	while (iter.hasNext()) {
    		String word = iter.next();

    		int len = word.length();
    		if (len < 3) {
    			continue; // too short we bail but "too long" is fine...
    		}

    		if (wordsMap.containsKey(word)) {
    			throw new IllegalStateException(
    					"This should never happen in Lucene 2.3.2");
    			// wordsMap.put(word, wordsMap.get(word) + 1);
    		} else {
    			// use the number of documents this word appears in
    			wordsMap.put(word, sourceReader.docFreq(new Term(
    					fieldToAutocomplete, word)));
    		}
    	}

    	for (String word : wordsMap.keySet()) {
    		// ok index the word
    		Document doc = new Document();
    		doc.add(new Field(SOURCE_WORD_FIELD, word , MTDDocument.FieldFactory.fieldIndexado())); // orig term
    		doc.add(new Field(GRAMMED_WORDS_FIELD, word, MTDDocument.FieldFactory.fieldIndexado())); // grammed
    		doc.add(new Field(COUNT_FIELD,Integer.toString(wordsMap.get(word)), MTDDocument.FieldFactory.fieldNaoIndexado())); // count

    		indexWriter.addDocument(doc);
    	}

    	sourceReader.close();

    	// close writer
    	indexWriter.close();

    	// re-open our reader
//    	reOpenReader();
    }

    private void reOpenReader() throws CorruptIndexException, IOException {
    	if (autoCompleteReader == null) {
    		autoCompleteReader = IndexReader.open(autoCompleteDirectory);
    	} else {
//    		autoCompleteReader.reopen();
    	}

    	autoCompleteSearcher = new IndexSearcher(autoCompleteReader);
    }

    public static void main(String[] args) throws Exception {
    	AutoCompleter autocomplete = new AutoCompleter(MTDArquivoEnum.INDICE_DIR.getArquivo(), Version.LUCENE_48);
    	String term = "futebol";
    	//System.out.println(autocomplete.suggestTermsFor(term));
    	
    	FreeTextSuggester fts = new FreeTextSuggester(analiser, analiser , 2, (byte) 0x20);
    	List<LookupResult> lista = fts.lookup(term.subSequence(0, term.length()), 5);
    	for (LookupResult lookupResult : lista) {
			System.out.println(lookupResult.toString());
		}
    }
}

