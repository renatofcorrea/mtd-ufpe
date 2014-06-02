package br.ufpe.mtd.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Classe utilitaria do sistema.
 * @author djalma
 *
 */
public class MTDUtil {
	
	/**
	 * Recupera data a partir de string 
	 * deve ter padrao de string mapeado
	 * suportte inicial ao padrao yyyy-MM-dd
	 *   
	 * @param data
	 * @return
	 */
	public static Date recuperarDataFormatosSuportados(String data){
		Date dataConvertida = null;
		String[] formatos = {"yyyy-MM-dd","yyyy"};
		
		//tenta todos os formatos ate achar um que tenha suporte
		for(String formato : formatos){
			try{
				dataConvertida = recuperarDataParaFormato(data, formato);
				break;//formatou com sucesso encerra.
			} catch (ParseException e) {
				//nao tem suporte ao formato
			}
		}
		
		return dataConvertida;
	}
	
	/**
	 * Recupera data para a partir de uma string representando uma data 
	 * em um formato especifico de padrao textual de data.
	 *  
	 * @param data
	 * @param formato
	 * @return
	 * @throws ParseException
	 */
	public static Date recuperarDataParaFormato(String data, String formato) throws ParseException{
		DateFormat formatador; 
		Date dataConvertida = null;
		formatador = new SimpleDateFormat(formato);
		data = data.trim();
		dataConvertida = formatador.parse(data);
		return dataConvertida;
	}
	
	/**
	 * Imprime dados no console se o ambiente estiver setado como
	 * desenvolvimento.
	 * Evita carga excessiva de dados enviados para stream de dados 
	 * que diminuiriam a performance em ambiente de producao.
	 * @param str
	 */
	public synchronized static void imprimirConsole(String str){
		if (MTDParametros.getTipoAmbiente().isDesenvovimento()) {
			System.out.println(str);
		}
	}
	
	public synchronized static void imprimirConsole(Exception e){
		if (MTDParametros.getTipoAmbiente().isDesenvovimento()) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Retira caracteres de string e substitue por outros caracteres
	 * padrao para a busca de dados
	 * 
	 * @param strEntrada
	 * @return
	 */
	public static String substituirCaracteresEspeciais(String strEntrada){
		
		String[] caracteresInic = new String[]{"�","�","�","�","�","�","�","�","�","�","�","�","�","�","�","�","�","�","�","�","�","�","�","�","�","�","�","�","�","�",
												"�","�","�","�","�","�","�","�","�","�","�","�","�","�","�","�","\t",",",".","?","&",":","/","!",";","�","�","%","�","�","(",")","\"","�","�"};
		
		String[] caracteresSubs = new String[]{"A","A","A","A","A","A","A","A","A","A","E","E","E","E","E","E","E","E","I","I","I","I","I","I","I","I","O","O","O","O",
												"O","O","O","O","O","O","U","U","U","U","U","U","U","U","C","C", "" ,"-","-","-","-","-","-","-","-","-","-","-","-","-","-","-","-" ,"-","-"};	
		
		for(int i = 0; i < caracteresInic.length ; i++){
			strEntrada = strEntrada.replace(caracteresInic[i], caracteresSubs[i]);
		}
		
	    while(strEntrada.contains("  ")){
	    	strEntrada = strEntrada.replace("  ", " ").trim();
	    }
		
		return strEntrada;
	}
	
	public static void main(String[] args) {
		File file;
		try {
			file = new File("C:\\Temp\\Treino.unit.gz");
			File saida = new File(file.getAbsolutePath().replace(".gz", ""));
			descompactarGZFile(file, saida);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Descompacta arquivos zip
	 * @param zipFile
	 * @throws IOException
	 */
	@SuppressWarnings("rawtypes")
	public static final void descompactarGZFile(File zipFile, File saida) throws IOException {
		if(!saida.exists()){
			saida.createNewFile();
		}
		FileInputStream fis = new FileInputStream(zipFile);			
		GZIPInputStream is = new GZIPInputStream(fis);
		FileOutputStream fos = new FileOutputStream(saida);		
		copyInputStream(is, fos);
	}
	  
	public static final void copyInputStream(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int len;
		while ((len = in.read(buffer)) >= 0) {
			out.write(buffer, 0, len);
		}
		in.close();
		out.close();
	}
}