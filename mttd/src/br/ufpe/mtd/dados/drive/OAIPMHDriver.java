package br.ufpe.mtd.dados.drive;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import br.ufpe.mtd.negocio.decodificacao.DecodificadorIdentificador;
import br.ufpe.mtd.negocio.entidade.Identificador;
import br.ufpe.mtd.util.MTDParametros;

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
	private String set;
	private String repositoryname;
	
	public String getRepositoryName(){
		return repositoryname;
	}
	
	public OAIPMHDriver() {
		decodificador = new DecodificadorIdentificador();
	}
	
	public OAIPMHDriver(String strUrl) {
		setURLBase(strUrl);
		decodificador = new DecodificadorIdentificador(this.repositoryname);
	}
	
	public OAIPMHDriver(String strUrl, String metaDataPrefix) {
		setURLBase(strUrl);
		this.decodificador = new DecodificadorIdentificador(this.repositoryname);
		this.metaDataPrefix = metaDataPrefix;
		
	}
	
	public void setURLBase(String strUrl) {
		strUrlBase = strUrl;
		this.repositoryname = strUrl.split("/")[2].replace("www.", "");
	}
	

	/*
	 * Tenta realizar a solicitacao por uma quantidade maxima 
	 * de vezes ate conseguir a resposta ou devolve excecao.
	 */
	public InputStream getResponse(String metaInf) throws Exception {
		InputStream stream = null;
		int tentativas = 0;
		String urlstr = strUrlBase + metaInf;
		URL urlbase = new URL(urlstr);
		boolean isdown = false;
		final int numMaxRetentativas = MTDParametros.getNumMaxRetentativas();
		while(stream == null && tentativas < numMaxRetentativas && !isdown){
			try {
				tentativas++;
				
				HttpURLConnection urlConn = (HttpURLConnection) urlbase.openConnection();
				urlConn.setReadTimeout(10000);
				urlConn.setConnectTimeout(10000);
				stream = urlConn.getInputStream();
				
			} catch (java.net.ConnectException e) {
				isdown = true;
				throw e;
				
			}catch (Exception e) {
				if(tentativas == numMaxRetentativas || isdown){
					throw e;
					
				}else{
					System.out.println("Retentativa numero "+tentativas+" de baixar dados para "+urlstr);
					Thread.sleep(1000);
				}
			}
		}
		return stream;
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

	//Chamada para obtenção da primeira página
	public String getListIdentifiers(String metaDataPrefix) throws Exception {
		String metainf = "?verb=ListIdentifiers&metadataPrefix="
				+ metaDataPrefix;
		return metainf;
	}
	
	//Chamada para obtenção da primeira página
	public String getListIdentifiers(String metaDataPrefix, String set) throws Exception {
		//http://www.repositorio.ufpe.br/oai/request?verb=ListIdentifiers&metadataPrefix=oai_dc&set=com_123456789_50
		String metainf = "?verb=ListIdentifiers&metadataPrefix="
				+ metaDataPrefix +"&set="+set;
		return metainf;
	}
	
	//Chamada para obtenção das demais páginas, se houver.
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
	 * em lotes , chame este método ate que nao tenham mais identificadores.
	 * use em conjunto com o metodo hasNext.
	 * @return
	 * @throws Exception 
	 */
	public List<Identificador> getNextIdentifiers() throws Exception{
		if(!decodificador.isIniciado()){
			//Retorna os identificadores iniciais do repositorio externo
			if(set!=null){
				return DecodificadorIdentificador.parse(decodificador, getResponse(getListIdentifiers(metaDataPrefix,set)));
			}else{
				return DecodificadorIdentificador.parse(decodificador, getResponse(getListIdentifiers(metaDataPrefix)));
			}
		}else{
			return DecodificadorIdentificador.parse(decodificador, getResponse(getListIdentifiersResumptionToken(decodificador.getResumption())));
		}
	}

	public String getSet() {
		return set;
	}
	
	public String setSet(String set) {
		return this.set = set;
	}	

	public String getMetaDataPrefix() {
		
		return metaDataPrefix;
	}

	public String getUrlBase() {
		
		return strUrlBase;
	}

	public int getTotalIdentificadores() {
		
		return decodificador.getTotalIdentificadores();
	}
}
