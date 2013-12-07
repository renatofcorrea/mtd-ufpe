package br.ufpe.mtd.consulta;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import net.sf.jColtrane.handler.JColtraneXMLHandler;

import org.xml.sax.SAXException;

import br.ufpe.mtd.entidade.Identificador;
import br.ufpe.mtd.excecao.MTDException;
import br.ufpe.mtd.util.MTDFactory;
import br.ufpe.mtd.util.MimeTypeEnum;

/**
 * Classe que realiza a comunicacao com o servidor
 * de dados externos atraves do protocolo OAIPMH.
 * @author djalma
 *
 */
public class OAIPMHDriver {

	private DecodificadorIdentificador decodificador;
	private String strUrlBase;
	private String metaDataPrefix;
	
	public OAIPMHDriver() {
		decodificador = new DecodificadorIdentificador();
	}
	
	public OAIPMHDriver(String strUrl) {
		decodificador = new DecodificadorIdentificador();
		this.strUrlBase = strUrl;
	}
	
	public OAIPMHDriver(String strUrl, String metaDataPrefix) {
		this.strUrlBase = strUrl;
		this.decodificador = new DecodificadorIdentificador();
		this.metaDataPrefix = metaDataPrefix;
	}
	
	public void setURLBase(String strUrl) {
		strUrlBase = strUrl;
	}
	

	private String getResponse(String metaInf) throws IOException {

		String urlstr = strUrlBase + metaInf;
		String str = null;
		URL urlbase = new URL(urlstr);
		HttpURLConnection urlConn = (HttpURLConnection) urlbase
				.openConnection();
		if (urlConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
			MTDFactory fabrica = MTDFactory.getInstancia();
			str = (String)fabrica.createContentHandler(MimeTypeEnum.XML.getCodigo()).getContent(urlConn);
		}
		return str;
	}

	public String getIdentify() throws IOException {
		String metainf = "?verb=Identify";
		String str = getResponse(metainf);
		return str;
	}

	public String getListMetadataFormats() throws IOException {
		String metainf = "?verb=ListMetadataFormats";
		String str = getResponse(metainf);
		return str;
	}

	public String getListSets() throws IOException {
		String metainf = "?verb=ListSets";
		String str = getResponse(metainf);
		return str;
	}

	public String getListIdentifiers(String metaDataPrefix) throws IOException {
		String metainf = "?verb=ListIdentifiers&metadataPrefix="
				+ metaDataPrefix;
		String str = getResponse(metainf);
		return str;
	}
	
	/**
	 * baixa a lista de identificadores de um repositorio de forma sequencial
	 * em lotes , chame este método ate que nao tenham mais identificadores.
	 * use em conjunto com o metodo hasNext.
	 * @return
	 * @throws IOException
	 * @throws MTDException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	public List<Identificador> getNextIdentifiers() throws IOException, ParserConfigurationException, SAXException, MTDException{
		if(!decodificador.isIniciado()){
			//Retorna os identificadores iniciais do repositorio externo
			return decodificarIdentificadores(getListIdentifiers(metaDataPrefix));
		}else{
			return decodificarIdentificadores(getListIdentifiersResumptionToken(decodificador.getResumption()));
		}
	}
	
	public String getListIdentifiersResumptionToken(String resumptionToken) throws IOException {
		String metainf = "?verb=ListIdentifiers&resumptionToken="
				+ resumptionToken;
		String str = getResponse(metainf);
		return str;
	}

	public String listRecords() throws IOException {
		String metainf = "?verb=ListRecords&metadataPrefix=mtd-br&from==2008-01-01T20:52:32Z";
		String str = getResponse(metainf);
		return str;
	}

	public String getRecordsApartirDe() throws IOException {
		String metainf = "?verb=ListRecords&from=2009-07-29T15:52:32Z&until=2009-07-29T20:52:32Z";
		String str = getResponse(metainf);
		return str;
	}

	public String getRecord(String metaDataPrefix, int identifier) throws IOException {
		String metainf = "?verb=GetRecord&metadataPrefix=" + metaDataPrefix
				+ "&identifier=oai:bdtd.ufpe.br:" + identifier;
		String str = getResponse(metainf);
		return str;
	}

	public String getRecord(String metaDataPrefix, String identifier) throws IOException {
		String metainf = "?verb=GetRecord&metadataPrefix=" + metaDataPrefix
				+ "&identifier=" + identifier;
		String str = getResponse(metainf);
		return str;
	}

	public boolean hasNext(){
		boolean contem = !decodificador.isIniciado() || decodificador.hasNext();
		return contem;
	}
	
	public DecodificadorIdentificador getDecodificador(){
		return decodificador;
	}
	
	public List<Identificador> decodificarIdentificadores(String str)
			throws ParserConfigurationException, SAXException, IOException, MTDException{
		SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
        
		if (str != null) {
            if (!str.startsWith("<?xml")) {
                String[] split = str.split("<?xml");
                str = "<?xml" + split[1] + "xml" + split[2] + "xml" + split[3]
                        + "xml" + split[4];
            }
            
            //TODO: Recuperar o charset para criar o array de bytes
            ByteArrayInputStream bais = new ByteArrayInputStream(str.getBytes());
           
            if (parser != null) {
                parser.parse(bais, new JColtraneXMLHandler(decodificador));
            }

            bais.reset();
        } else {
            throw new MTDException("Erro na Colheita dos Identificadores");
        }
        
        return decodificador.getIdentificadores();
	}
}
