package br.ufpe.mtd.util.enumerado;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;

import br.ufpe.mtd.util.MTDFactory;


public enum AreaCNPQEnum implements Serializable{
	
	CBS(MTDArquivoEnum.CBS_ARQUIVO,"Ciências Biológicas e da Saúde".toUpperCase()),
	CHLA(MTDArquivoEnum.CHLA_ARQUIVO,"Ciências Humanas, Letras e Artes".toUpperCase()), 
	TCEN(MTDArquivoEnum.TCEN_ARQUIVO,"Tecnologia, Ciências Exatas e da Natureza".toUpperCase()), 
	OUTROS(MTDArquivoEnum.OUTROS_ARQUIVO,"Multidisciplinar".toUpperCase()), 
	NAO_ENCONTRADO("Sem classificação".toUpperCase());
	
	private String texto = "";
	private String descricao;
	private static String bdtdAreas;
	
	private AreaCNPQEnum(String descricao) {
		this.descricao = descricao;
		texto = "NAOENCONTRADO";
	}
	
	private AreaCNPQEnum(MTDArquivoEnum arquivoEnum, String descricao) {
		this.descricao = descricao;
		File arquivo = arquivoEnum.getArquivo();
		try {
			texto = carregarTexto(arquivo);
		} catch (Exception e) {
			MTDFactory.getInstancia().getLog().salvarDadosLog(e);
		}
	}
	
	private static String carregarTextoOriginal(File arquivo) throws IOException{
		StringBuffer str = new StringBuffer();
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(arquivo);
			int byteLido = fis.read();
			while(byteLido != -1){
				str.append((char)byteLido);
				byteLido = fis.read();
			}
			
		} finally {
			if(fis != null){
				try {
					fis.close();
				} catch (Exception e) {
				}
			}
		}
		
		if(str.length()==0)
			return null;
		else
		return str.toString();
	}
	
	private static String carregarTexto(File arquivo) throws Exception{
		return substituirCaracteresEspeciais(carregarTextoOriginal(arquivo)).toUpperCase();
	}
	
	
	
	private boolean contains(String subArea){
		subArea = substituirCaracteresEspeciais(subArea);
		String [] linhas = texto.split("\n");
		for(String linha : linhas){
			if(linha.equals(subArea)){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Retira caracteres de string e substitue por outros caracteres
	 * padrao para a busca de dados
	 * 
	 * @param strEntrada
	 * @return
	 */
	private static String substituirCaracteresEspeciais(String strEntrada){
		
		String[] caracteresInic = new String[]{"á","à","ã","â","ä","Á","À","Â","Ã","Ä","é","è","ê","ë","É","È","Ê","Ë","í","ì","î","ï","Í","Ì","Î","Ï","ó","ò","õ","ô",
												"ö","Ó","Ò","Õ","Ô","Ö","ú","ù","û","ü","Ú","Ù","Û","Ü","ç","Ç","\t",",",".","?","&",":","/","!",";","º","ª","%","‘","’","(",")","\"","”","“",
												"0","1","2","3","4","5","6","7","8","9","-","\r"};
		
		String[] caracteresSubs = new String[]{"A","A","A","A","A","A","A","A","A","A","E","E","E","E","E","E","E","E","I","I","I","I","I","I","I","I","O","O","O","O",
												"O","O","O","O","O","O","U","U","U","U","U","U","U","U","C","C", " " , "", "", "" , "", "", "", "", "" ,"" ,"", "", "" ,"", "", "" , "" , "" , "",
												"" , "", "", "", "", "", "", "", "", "", "", " "};	
		
		for(int i = 0; i < caracteresInic.length ; i++){
			strEntrada = strEntrada.replace(caracteresInic[i], caracteresSubs[i]);
		}
		
    	strEntrada = strEntrada.replace(" ", "").trim();
		
		return strEntrada.toUpperCase();
	}
	
	public static AreaCNPQEnum getAreaCNPQPorSubArea(String nomeSubArea){
		
		for(AreaCNPQEnum area : AreaCNPQEnum.values()){
			if(area.contains(nomeSubArea)){
				return area;
			}
		}
		
		return AreaCNPQEnum.NAO_ENCONTRADO;
	}

	public static AreaCNPQEnum getGrandeAreaCNPQPorPrograma(String nomePrograma){
		try {
			if(bdtdAreas == null){
				bdtdAreas = carregarTextoOriginal(MTDArquivoEnum.BDTD_AREAS.getArquivo());
			}
		
			String [] linhas = bdtdAreas.split("\n");
			
			for(String linha : linhas){
				String programa = linha.split(";")[0];
				String area = linha.split(";")[1];
				
				programa = substituirCaracteresEspeciais(programa);
				String aux = substituirCaracteresEspeciais(nomePrograma).replace("PROGRAMADE", "");
				if(programa.equals(aux)){
					return getAreaCNPQPorSubArea(area);
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Não encontrado em bdtdareas.txt programa: "+nomePrograma);
		return AreaCNPQEnum.NAO_ENCONTRADO;
	}
	
	private static synchronized void initAreas() throws IOException{
		if(bdtdAreas == null){
			bdtdAreas = carregarTextoOriginal(MTDArquivoEnum.BDTD_AREAS.getArquivo());
		}
	}
	
	public static String getAreaCNPQPorPrograma(String nomePrograma){
		try {
			initAreas();
			String [] linhas = bdtdAreas.split("\n");
			
			for(String linha : linhas){
				String programa = linha.split(";")[0];
				String area = linha.split(";")[1];
				
				programa = substituirCaracteresEspeciais(programa);
				String aux = substituirCaracteresEspeciais(nomePrograma).replace("PROGRAMADE", "");
				if(programa.equals(aux)){
					return area;
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null; //AreaCNPQEnum.NAO_ENCONTRADO;
	}	
	
	public static AreaCNPQEnum getGrandeAreaCNPQPorNome(String nomeArea){
		
		for(AreaCNPQEnum area : AreaCNPQEnum.values()){
			if(area.name().equalsIgnoreCase(nomeArea)){
				return area;
			}
		}
		
		return AreaCNPQEnum.NAO_ENCONTRADO;
	}
	
	public String getDescricao() {
		return descricao;
	}
	
	public static void main(String[] args) {
		AreaCNPQEnum area = AreaCNPQEnum.getAreaCNPQPorSubArea("CIENCIAS SOCIAIS APLICADAS");
		System.out.println(area);
		
		area = AreaCNPQEnum.getGrandeAreaCNPQPorPrograma("Programa de Pós-Graduação em Ciência da Computação");
		System.out.println(area);
		
		
		area = AreaCNPQEnum.getGrandeAreaCNPQPorPrograma("Programa de Pós-Graduação em Administração");
		System.out.println(area);
		
		
		area = AreaCNPQEnum.getGrandeAreaCNPQPorPrograma("Pós-Graduação em Ciência da Computação");
		System.out.println(area);
		
		String aux = AreaCNPQEnum.getAreaCNPQPorPrograma("Pós-Graduação em Ciência da Computação");
		System.out.println(aux);
		
		aux = AreaCNPQEnum.getAreaCNPQPorPrograma("Programa de Pós-Graduação em Gestão e Pública p/ o Desenvolvimento do Nordeste");
		System.out.println(aux);
		
		String[] array = new String[] {"Programa de Pós-Graduação em Engenharia e Tecnologia de Materiais",
				"Programa de Programa de Pós-Graduação em Biologia Aplicada à Saúde",
				"Programa de Programa de Pós-Graduação em Biotecnologia Industrial",
				"Programa de Pós-Graduação em Ciência da Informação",
				"Programa de Pós-Graduação em Educação Matemática e Tecnológica",
				"Programa de Pós-Graduação em Enfermagem",
				"Programa de Pós-Graduação em Patologia",
				"Programa de Pós-Graduação em Saúde Humana e Meio Ambiente",
				"Programa de Pós-Graduação em Fisioterapia",
				"Programa de Pós-Graduação em Saúde Humana e Meio Ambiente",
				"Programa de Pós-Graduação em Inovação Terapêutica",
				"Programa de Pós-Graduação em Serviço Social",
				"Programa de Pós-Graduação em Enfermagem",
				"Programa de Pós-Graduação em Saúde Coletiva"};
		
		for(String str : array){
			aux = AreaCNPQEnum.getAreaCNPQPorPrograma(str);
			String strArea = AreaCNPQEnum.getAreaCNPQPorSubArea(aux).name();
			System.out.println(str+" : "+aux+" : "+strArea);
		}
		
		System.out.println("==============================");
		
		teste();
	}
	
	
	private static void teste(){
		try {
			String	bdtdAreas = carregarTextoOriginal(MTDArquivoEnum.BDTD_AREAS.getArquivo());
		
			String [] linhas = bdtdAreas.split("\n");
			
			for(String linha : linhas){
				String programa = linha.split(";")[0];
				String area = linha.split(";")[1];
				
				System.out.println(programa+"  "+area+" "+getAreaCNPQPorSubArea(area));
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
