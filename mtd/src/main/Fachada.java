package main;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringBufferInputStream;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import net.sf.jColtrane.handler.JColtraneXMLHandler;

import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.NumberTools;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class Fachada {

	private SAXParser parser;
	private DataProvider dp;
	private Leitor analisador;
	private String str;
	private StringBufferInputStream sb;
	private HashSet<String> stopwords;
	private TextAnalyzer analyzer;
	private Directory indexDirectory;
	private LeitorDocumentos analisadorMetadados;
	private String metadataprefix;

	public Fachada(String url, boolean stemming, String arquivoStopwords, String caminhoIndice)
			throws ParserConfigurationException, SAXException, IOException {
		this.dp = new DataProvider();
		this.dp.setURLBase(url);
		this.str = null;
		this.sb = null;
		this.parser = SAXParserFactory.newInstance().newSAXParser();

		this.analisador = new Leitor();
		this.analisadorMetadados = new LeitorDocumentos();

		this.carregarStopWords(arquivoStopwords);

		this.analyzer = new TextAnalyzer(stopwords, stemming);

		String fsIndexDir = System.getProperty("java.io.tmpdir", "tmp")
				+ System.getProperty("file.separator") + "fs-index";

		// this.indexDirectory = new RAMDirectory();
		this.indexDirectory = FSDirectory.getDirectory(caminhoIndice,false);
	}

	private void carregarStopWords(String caminho) {

		this.stopwords = new HashSet<String>();

		File file = new File(caminho);
		FileInputStream fileInputStream = null;
		BufferedInputStream bufferedInputStream = null;
		DataInputStream dataInputStream = null;

		try {
			fileInputStream = new FileInputStream(file);

			bufferedInputStream = new BufferedInputStream(fileInputStream);
			dataInputStream = new DataInputStream(bufferedInputStream);

			while (dataInputStream.available() != 0) {
				this.stopwords.add(dataInputStream.readLine());
			}

			fileInputStream.close();
			bufferedInputStream.close();
			dataInputStream.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String colherIdentificadores() throws SAXException, IOException {

		do {
			if (str == null) {
				str = dp.getListIdentifiers(this.metadataprefix);
			} else {
				str = null;
				str = dp
						.getListIdentifiersResumptionToken(analisador.resumption);
			}

			if (!str.startsWith("<?xml")) {
				String[] split = str.split("<?xml");
				str = "<?xml" + split[1] + "xml" + split[2] + "xml" + split[3]
						+ "xml" + split[4];
			}

			this.sb = new StringBufferInputStream(str);

			if (parser != null) {
				parser.parse(sb, new JColtraneXMLHandler(analisador));
			}

			this.sb.reset();
		} while (!analisador.resumption.equals(""));

		return "Fim Colheita Identificadores";
	}

	public String colherMetadadosOnline() {
		String a;
		Iterator<String> iterator = this.analisador.vetor.iterator();

		while (iterator.hasNext()) {
			a = iterator.next();
			System.out.println(a);

			this.str = dp.getRecord(this.metadataprefix, a);

			this.sb = new StringBufferInputStream(str);

			if (parser != null) {
				try {
					parser.parse(sb, new JColtraneXMLHandler(
							analisadorMetadados));
				} catch (SAXException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return "Fim Colheita Metadados Online";
	}

	public String colherMetadadosPorId(int id) {

		this.str = dp.getRecord(this.metadataprefix, id);

		this.sb = new StringBufferInputStream(str);

		if (parser != null) {
			try {
				parser.parse(sb, new JColtraneXMLHandler(analisadorMetadados));
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return "Fim Colheita Metadados Online";
	}

	public String colherMetadadosCache(String caminho) {

		File file = new File(caminho);

		if (parser != null) {
			InputSource input = new InputSource(file.getAbsolutePath());
			try {
				parser.parse(input,
						new JColtraneXMLHandler(analisadorMetadados));
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return "Fim Colheita Metadados Cache";
	}

	public String indexar() throws CorruptIndexException,
			LockObtainFailedException, IOException {

		IndexWriter w = new IndexWriter(indexDirectory, analyzer, true,
				IndexWriter.MaxFieldLength.UNLIMITED);

		Iterator<Documento> iterator = this.analisadorMetadados.documentos
				.iterator();

		int i = 0;
		while (iterator.hasNext()) {
			Documento d = iterator.next();

			if (d.getDescricao() == null)
				d.setDescricao("");
			if (d.getTitulo() == null)
				d.setTitulo("");
			if (d.getAreaCNPQ() == null)
				d.setAreaCNPQ("");
			if (d.getAreaPrograma() == null)
				d.setAreaPrograma("");
			if (d.getAutor() == null)
				d.setAutor("");
			if (d.getOrientador() == null)
				d.setOrientador("");
			if (d.getPrograma() == null)
				d.setPrograma("");

			this.addDoc(w, d.getTitulo(), d.getDescricao(), d.getKeywords(), d
					.getDataDeDefesa(), d.getAutor(), d.getPrograma(), d
					.getOrientador(), d.getAreaCNPQ(), d.getId(), d
					.getAreaPrograma());

			System.out.println(i);
			i++;
		}

		// Fecha o arquivo.
		w.close();

		return "Fim Indexação";
	}

	public void consultar(String termo, int maxResultado)
			throws ParseException, CorruptIndexException, IOException {

		String[] campos = { "title", "resumo", "keyword", "autor", "programa",
				"orientador", "areaCNPQ" };
		Query q = new MultiFieldQueryParser(campos, analyzer).parse(termo);

		// Cria o acesso ao índice
		IndexSearcher searcher = new IndexSearcher(indexDirectory);

		// Prepara a coleção de resultado
		TopDocCollector collector = new TopDocCollector(maxResultado);

		// Faz a pesquisa
		System.out.println("Pesquisar");
		searcher.search(q, collector);

		// Separa os itens mais relevantes para a consulta.
		ScoreDoc[] hits = collector.topDocs().scoreDocs;

		System.out.println("Found " + hits.length + " hits.");
		for (int i = 0; i < hits.length; ++i) {
			int docId = hits[i].doc;
			Document d = searcher.doc(docId);

			System.out.println((i + 1) + ". " + d.get("title"));
		}

	}

	private void addDoc(IndexWriter w, String text, String resumo,
			Vector<String> keywords, Date dataDefesa, String autor,
			String programa, String orientador, String areaCNPQ, long id,
			String areaPrograma) throws CorruptIndexException, IOException {
		Document doc = new Document();

		doc
				.add(new Field("title", text, Field.Store.YES,
						Field.Index.ANALYZED));
		doc.add(new Field("resumo", resumo, Field.Store.YES,
				Field.Index.ANALYZED));

		if (dataDefesa != null) {
			doc.add(new Field("dataDefesa", DateTools.dateToString(dataDefesa,
					DateTools.Resolution.YEAR), Field.Store.YES,
					Field.Index.ANALYZED));
		} else {
			System.out.println("data nula");
		}

		doc
				.add(new Field("autor", autor, Field.Store.YES,
						Field.Index.ANALYZED));
		doc.add(new Field("programa", programa, Field.Store.YES,
				Field.Index.ANALYZED));
		doc.add(new Field("orientador", orientador, Field.Store.YES,
				Field.Index.ANALYZED));
		doc.add(new Field("areaCNPQ", areaCNPQ, Field.Store.YES,
				Field.Index.ANALYZED));
		String longToString = NumberTools.longToString(id);
		doc.add(new Field("id", longToString, Field.Store.YES, Field.Index.NO));
		doc.add(new Field("areaPrograma", areaPrograma, Field.Store.YES,
				Field.Index.ANALYZED));

		for (int i = 0; i < keywords.size() && keywords.elementAt(i) != null; i++) {
			doc.add(new Field("keyword", keywords.elementAt(i),
					Field.Store.YES, Field.Index.ANALYZED));
		}

		w.addDocument(doc);

	}

	public String getMetadataprefix() {
		return metadataprefix;
	}

	public void setMetadataprefix(String metadataprefix) {
		this.metadataprefix = metadataprefix;
	}

}
