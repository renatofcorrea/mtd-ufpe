package br.ufpe.mtd.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class MTDParametros {

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
}