package main;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class DataProvider {

	private String strurlbase;
	private HTTPContentHandler httpch;

	public DataProvider() {
		strurlbase = null;
	}

	public boolean setURLBase(String strUrl) {
		// Constrói a URL.
		strurlbase = strUrl;
		httpch = new HTTPContentHandler();
		URLConnection.setContentHandlerFactory(httpch);
		return true;
	}

	private String getResponse(String metainf) {

		String urlstr = strurlbase + metainf;
		String str = null;
		URL urlbase = null;
		try {
			urlbase = new URL(urlstr);
		} catch (MalformedURLException e) {
			return null;
		}

		try {
			HttpURLConnection urlConn = (HttpURLConnection) urlbase
					.openConnection();
			// Se o documento está acessível...
			if (urlConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
				// Recupera o conteúdo da URL.
				str = ((String) urlConn.getContent());

				
			}
		} catch (Exception e) {
			return str;
		}
		return str;
	}

	public String getIdentify() {
		String metainf = "?verb=Identify";
		String str = getResponse(metainf);
		return str;
	}

	public String getListMetadataFormats() {
		String metainf = "?verb=ListMetadataFormats";
		String str = getResponse(metainf);
		return str;
	}

	public String getListSets() {
		String metainf = "?verb=ListSets";
		String str = getResponse(metainf);
		return str;
	}

	public String getListIdentifiers(String metadataprefix) {
		String metainf = "?verb=ListIdentifiers&metadataPrefix="
				+ metadataprefix;
		String str = getResponse(metainf);
		return str;
	}

	public String getListIdentifiersResumptionToken(String resumptionToken) {
		String metainf = "?verb=ListIdentifiers&resumptionToken="
				+ resumptionToken;
		String str = getResponse(metainf);
		return str;
	}

	public String listRecords() {
		String metainf = "?verb=ListRecords&metadataPrefix=mtd-br&from==2008-01-01T20:52:32Z";
		String str = getResponse(metainf);
		return str;
	}

	public String getRecordsApartirDe() {
		String metainf = "?verb=ListRecords&from=2009-07-29T15:52:32Z&until=2009-07-29T20:52:32Z";
		String str = getResponse(metainf);
		return str;
	}

	public String getRecord(String metadataprefix, int identifier) {
		String metainf = "?verb=GetRecord&metadataPrefix=" + metadataprefix
				+ "&identifier=oai:bdtd.ufpe.br:" + identifier;
		String str = getResponse(metainf);
		return str;
	}

	public String getRecord(String metadataprefix, String identifier) {
		String metainf = "?verb=GetRecord&metadataPrefix=" + metadataprefix
				+ "&identifier=" + identifier;
		String str = getResponse(metainf);
		return str;
	}

}
