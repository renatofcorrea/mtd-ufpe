package br.ufpe.mtd.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
		
		String[] caracteresInic = new String[]{"á","à","ã","â","ä","Á","À","Â","Ã","Ä","é","è","ê","ë","É","È","Ê","Ë","í","ì","î","ï","Í","Ì","Î","Ï","ó","ò","õ","ô",
												"ö","Ó","Ò","Õ","Ô","Ö","ú","ù","û","ü","Ú","Ù","Û","Ü","ç","Ç","\t",",",".","?","&",":","/","!",";","º","ª","%","‘","’","(",")","\"","”","“"};
		
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
}