package br.ufpe.mtd.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class MTDParametros {
	
	private final static int NUM_MAX_THREADS = 10;
	private final static int NUM_MAX_RETENTATIVAS = 5;
	public final static String STOP_WORDS = "bdtdstopwords.txt";
	private static Properties properties;
	private static TipoAmbiente tipoAmbiente;
	
	public static TipoAmbiente getTipoAmbiente() {
		if(tipoAmbiente == null){
			tipoAmbiente = TipoAmbiente.DESENVOLVIMENTO;
		}
		return tipoAmbiente;
	}
	
	/**
	 * Diretorio onde serao colocados os dados
	 * externos da aplicacao.
	 * 
	 * Cria o diretorio ou diretorios caso nao exista.
	 * 
	 * esta sendo consultado de arquivo de propriedades 
	 * 
	 * @return
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public static File getExternalStorageDirectory() {
		File diretorio = new File(getMTDProperties().getProperty("dir_raiz"));
		
		if(!diretorio.exists()){
			diretorio.mkdirs();
		}
		
		return diretorio;
	}
	
	public static Properties getMTDProperties(){
		if(properties == null){
			try {
				
				File arquivoProperties = new File(System.getProperty("user.dir")+File.separator+"mtd_properties.properties");
				
				properties = new Properties();
				properties.load(new FileInputStream(arquivoProperties));
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return properties;
	}
	
	/**
	 * Retorna uma arquivo interno do projeto
	 * localizado na pasta local
	 * @param nome
	 * @return
	 */
	public static File getLocalFile(String nome){
		File file = new File(System.getProperty("user.dir")+File.separator+nome);
		return file;
	}
	
	/**
	 * Retorna o numero maximo de retentaivas dentro do sistema.
	 * 
	 * Retorna valor configurado no arquivo de propoerties pela
	 * chave max_retentativas ou valor padrao  definido pela constante NUM_MAX_RETENTATIVAS
	 * se o properties não tiver a chave configurada.
	 * O numero de retentativas não podera ser superior a 10 e a quantidade minima sera 0.
	 * @return
	 */
	public static int getNumMaxRetentativas(){
		String strMaxRetentaivas = getMTDProperties().getProperty("max_retentativas");
		if(strMaxRetentaivas!= null){
			try {
				Integer qtd = Integer.parseInt(strMaxRetentaivas);
				if(qtd.intValue() >= 0 && qtd.intValue() <= 10){
					return qtd;					
				}
			} catch (Exception e) {
				//sera retornado o valor padrao do metodo
			}
		}
		return NUM_MAX_RETENTATIVAS;
	}
	
	/**
	 * Retorna o numero maximo de threads dentro de um pool do sistema.
	 * 
	 * Retorna valor configurado no arquivo de propoerties pela
	 * chave max_concorrencia ou valor padrao  definido pela constante NUM_MAX_THREADS
	 * se o properties não tiver a chave configurada.
	 * 
	 * A quantidade maxima de Threads sera 100 e a minima 1
	 * @return
	 */
	public static int getNumMaxThreads(){
		String strMaxThreads = getMTDProperties().getProperty("max_concorrencia");
		if(strMaxThreads!= null){
			try {
				Integer qtd = Integer.parseInt(strMaxThreads);
				
				if(qtd.intValue() > 0 && qtd.intValue() <=100){
					return qtd;
				}				
			} catch (Exception e) {
				//sera retornado o valor padrao do metodo
			}
		}
		return NUM_MAX_THREADS;
	}
	
	/**
	 * Retorna a url para o solr ou null se o sistema nao estive configurado para usar 
	 * o Solr. 
	 * 
	 * A configuracao para uso do Solr é feita configurando no arquivo de propoerties do sistema 
	 * as chaves  solr_usar=true
	 * e 
	 * solr_url com a url vaida do solr.
	 * 
	 * @return
	 */
	public static String getSolrUrl(){
		String strSolrUrl = null;
		try{
			Properties propriedades = getMTDProperties();
			String strUsarSoler = propriedades.getProperty("solr_usar");
			
			if(strUsarSoler != null && Boolean.parseBoolean(strUsarSoler) == true){
				strSolrUrl = propriedades.getProperty("solr_url");
			}	
		}catch(Exception e){
			
		}
		
		return strSolrUrl;
	}	
}