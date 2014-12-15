package br.ufpe.mtd.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.lucene.util.Version;

import br.ufpe.mtd.util.enumerado.TipoAmbiente;

/**
 * Classe que vai gerenciar e permitir recuperar informacoes 
 * sobre os parametros do sistema.
 * @author djalma
 *
 */
public class MTDParametros {
	public static final Version LUCENE_VERSION = Version.LUCENE_4_10_1;
	private final static int NUM_MAX_RETENTATIVAS = 5;
	private static String pastaWeb = null;
	private static Properties properties;
	private static TipoAmbiente tipoAmbiente;
	
	public static TipoAmbiente getTipoAmbiente() {
		String ambiente = getMTDProperties().getProperty("ambiente");
		if(tipoAmbiente == null && ambiente != null){
			try {
				tipoAmbiente = TipoAmbiente.valueOf(ambiente);
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		
		if(tipoAmbiente == null){
			tipoAmbiente = TipoAmbiente.DESENVOLVIMENTO;
		}
		
		return tipoAmbiente;
	}
	
	public static String getPastaWeb(){
		return pastaWeb;
	}
	
	public static String setPastaWeb(String pasta){
		return pastaWeb = pasta;
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
		File diretorio =  null;
		if(isAmbienteWeb()){
			diretorio = getLocalFile(getMTDProperties().getProperty("web_dir_raiz"));
		}else{
			diretorio = new File(getMTDProperties().getProperty("dir_raiz"));
		}
		
		if(!diretorio.exists()){
			diretorio.mkdirs();
		}
		
		return diretorio;
	}
	
	public static String getSuggestType() {
		return getMTDProperties().getProperty("tipo_suggester");
	}
	
	public static String getMaxSuggestResults(){
		return getMTDProperties().getProperty("max_result_suggester");
	}
	
	private static Properties getMTDProperties(){
		if(properties == null){
			try {
				File arquivoProperties = getLocalFile("mtd_properties.properties");
				properties = new Properties();
				properties.load(new FileInputStream(arquivoProperties));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
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
		File file = new File(getLocalFolder(),nome);
		return file;
	}
	
	public static boolean isAmbienteWeb(){
		return pastaWeb != null;
	}
	
	public static File getLocalFolder(){
	
		String pasta = isAmbienteWeb() ? pastaWeb : System.getProperty("user.dir") + File.separator + "WebContent" + File.separator + "WEB-INF";
		File file = new File(pasta);
		
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
	 * Que é calculado baseado na quantidade de processadores disponiveis no
	 * computador onde o sistema esta executando.
	 * 
	 * Threads = 2 *  Qtd_CPUS
	 * 
	 * @return
	 */
	public static Integer getNumMaxThreads(){
		return new Integer(Runtime.getRuntime().availableProcessors() * 2);
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
	
	public static String exibirUnicaKeyWord(){
		return MTDParametros.getMTDProperties().getProperty("exibir_unica_key_word");
	}
	
	public static String indiceDir(){
		return getMTDProperties().getProperty("indice_dir");
	}
	
	public static String acessoRepositorio(){
		return getMTDProperties().getProperty("acesso_repositorio");
	}
	
	public static String logDir(){
		return getMTDProperties().getProperty("log_dir");
	}
	
	public static String nomeLogDados(){
		return getMTDProperties().getProperty("log_dados");
	}
	
	public static String nomeLogExcecao(){
		return getMTDProperties().getProperty("log_excecao");
	}
	
	public static String qtdColunasMapa(){
		return getMTDProperties().getProperty("qtd_colunas_mapa");
	}
	
	public static String incluirTodosTermosBusca(){
		return getMTDProperties().getProperty("incluir_todos_termos");
	}
	
	public static String diasTreino(){
		return getMTDProperties().getProperty("dias_treino");
	}
}