package br.ufpe.mtd.dados;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import br.ufpe.mtd.entidade.Identificador;
import br.ufpe.mtd.xml.DecodificadorIdentificador;

/**
 * Classe que realiza a comunicacao com o servidor
 * de dados externos atraves do protocolo OAIPMH.
 * @author djalma
 *
 */
public class OAIPMHDriver {

	private final  int QTD_MAX_TENTATIVAS = 5;
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
	

	/*
	 * Tenta realizar a solicitacao por uma quantidade maxima 
	 * de vezes ate conseguir a resposta ou devolve excecao.
	 */
	public InputStream getResponse(String metaInf) throws Exception {
		InputStream dados = null;
		int tentativas = 0;
		String urlstr = strUrlBase + metaInf;
		URL urlbase = new URL(urlstr);
		
		while(dados == null && tentativas < QTD_MAX_TENTATIVAS){
			try {
				tentativas++;
				
				HttpURLConnection urlConn = (HttpURLConnection) urlbase
						.openConnection();
				
				dados = urlConn.getInputStream();
				
			} catch (Exception e) {
				
				if(tentativas == QTD_MAX_TENTATIVAS){
					throw e;
				}else{
					Thread.sleep(1000);
				
				}
			}
		}
		return dados;
	}

	public String getIdentify() throws Exception {
		String metainf = "?verb=Identify";
		return metainf;
	}

	public String getListMetadataFormats() throws Exception {
		String metainf = "?verb=ListMetadataFormats";
		return metainf;
	}

	public String getListSets() throws Exception {
		String metainf = "?verb=ListSets";
		return metainf;
	}

	public String getListIdentifiers(String metaDataPrefix) throws Exception {
		String metainf = "?verb=ListIdentifiers&metadataPrefix="
				+ metaDataPrefix;
		return metainf;
	}
	
	
	
	public String getListIdentifiersResumptionToken(String resumptionToken) throws Exception {
		String metainf = "?verb=ListIdentifiers&resumptionToken="
				+ resumptionToken;
		return metainf;
	}

	public String listRecords() throws Exception {
		String metainf = "?verb=ListRecords&metadataPrefix=mtd-br&from==2008-01-01T20:52:32Z";
		return metainf;
	}

	public String getRecordsApartirDe() throws Exception {
		String metainf = "?verb=ListRecords&from=2009-07-29T15:52:32Z&until=2009-07-29T20:52:32Z";
		return metainf;
	}

	public String getRecord(String metaDataPrefix, int identifier) throws Exception {
		String metainf = "?verb=GetRecord&metadataPrefix=" + metaDataPrefix
				+ "&identifier=oai:bdtd.ufpe.br:" + identifier;
		return metainf;
	}

	public String getRecord(String metaDataPrefix, String identifier) throws Exception {
		String metainf = "?verb=GetRecord&metadataPrefix=" + metaDataPrefix
				+ "&identifier=" + identifier;
		return metainf;
	}

	public boolean hasNext(){
		boolean contem = !decodificador.isIniciado() || decodificador.hasNext();
		return contem;
	}
	
	public DecodificadorIdentificador getDecodificador(){
		return decodificador;
	}
	
	/**
	 * baixa a lista de identificadores de um repositorio de forma sequencial
	 * em lotes , chame este m�todo ate que nao tenham mais identificadores.
	 * use em conjunto com o metodo hasNext.
	 * @return
	 * @throws Exception 
	 */
	public List<Identificador> getNextIdentifiers() throws Exception{
		if(!decodificador.isIniciado()){
			//Retorna os identificadores iniciais do repositorio externo
			return DecodificadorIdentificador.parse(decodificador, getResponse(getListIdentifiers(metaDataPrefix)));
		}else{
			return DecodificadorIdentificador.parse(decodificador, getResponse(getListIdentifiersResumptionToken(decodificador.getResumption())));
		}
	}
}
