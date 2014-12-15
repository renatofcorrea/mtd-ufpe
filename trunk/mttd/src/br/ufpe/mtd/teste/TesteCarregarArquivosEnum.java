package br.ufpe.mtd.teste;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TesteCarregarArquivosEnum {

	
	public static void main(String[] args) {
		
		String padrao = "\\s{2,}";
	    Pattern regPat = Pattern.compile(padrao);
	    String frase = "   Esta    frase   contém    espaços   "; 
	    Matcher matcher = regPat.matcher(frase);
	    String res = matcher.replaceAll(" ").trim();
	    
	    System.out.println(res);
	    while(frase.contains("  ")){
	    	frase = frase.replace("  ", " ").trim();
	    	System.out.println(frase);
	    }
		
//		File arquivo = MTDArquivoEnum.CBS.getArquivo();
//		
//		try {
//			FileInputStream fis = new FileInputStream(arquivo);
//			
//			int byteLido = fis.read();
//			StringBuffer str = new StringBuffer();
//			while(byteLido != -1){
//				str.append((char)byteLido);
//				byteLido = fis.read();
//			}
//			
//			
//			String strMinuscula = MTDUtil.substituirCaracteresEspeciais(str.toString());
//			strMinuscula = strMinuscula.toUpperCase();
//
//			System.out.println(strMinuscula);
//			
//			
//			System.out.println("=====================================Busca de termos ===================================================");
//			String termoProcurado = "CIENCIA DA COMPUTACAO";
//			
//			System.out.println("Comtem o termo "+termoProcurado+" = "+strMinuscula.contains(MTDUtil.substituirCaracteresEspeciais(termoProcurado)));
//			
//			termoProcurado = "BIOLOGIA GERAL";
//			System.out.println("Comtem o termo "+termoProcurado+" = "+strMinuscula.contains(MTDUtil.substituirCaracteresEspeciais(termoProcurado)));
//			
//			String[] caracteresInic = new String[]{"á","à","ã","â","ä","Á","À","Â","Ã","Ä","é","è","ê","ë","É","È","Ê","Ë","í","ì","î","ï","Í","Ì","Î","Ï","ó","ò","õ","ô","ö","Ó","Ò","Õ","Ô","Ö","ú","ù","û","ü","Ú","Ù","Û","Ü","ç","Ç", " ","\t",",",".","?","&",":","/","!",";","º","ª","%","‘","’","(",")","\"","”","“"};
//			String teste = "";
//			for(int i = 0; i < caracteresInic.length ; i++){
//				teste+= caracteresInic[i];
//			}
//			
//			String retorno = MTDUtil.substituirCaracteresEspeciais(teste);
//			
//			System.out.println("Tam teste "+teste.length()+" Tam retorno "+retorno.length());
//			System.out.println("Teste   "+teste);
//			System.out.println("Retorno "+retorno);
//			
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
}