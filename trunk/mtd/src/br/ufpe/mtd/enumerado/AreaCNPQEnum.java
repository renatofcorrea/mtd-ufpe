package br.ufpe.mtd.enumerado;

import java.io.File;
import java.io.FileInputStream;

import br.ufpe.mtd.util.MTDFactory;
import br.ufpe.mtd.util.MTDUtil;


public enum AreaCNPQEnum {
	
	CBS(MTDArquivoEnum.CBS_ARQUIVO), CHLA(MTDArquivoEnum.CHLA_ARQUIVO), TCEN(MTDArquivoEnum.TCEN_ARQUIVO), OUTROS(MTDArquivoEnum.OUTROS_ARQUIVO), NAO_ENCONTRADO;
	
	private String texto = "";
	
	
	private AreaCNPQEnum() {
		texto = "NAOENCONTRADO";
	}
	private AreaCNPQEnum(MTDArquivoEnum arquivoEnum) {
		File arquivo = arquivoEnum.getArquivo();
		try {
			texto = carregarTexto(arquivo);
		} catch (Exception e) {
			MTDFactory.getInstancia().getLog().salvarDadosLog(e);
		}
	}
	
	private String carregarTexto(File arquivo) throws Exception{
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
		
		return MTDUtil.substituirCaracteresEspeciais(str.toString()).toUpperCase();
	}
	
	public boolean contains(String subArea){
		subArea = MTDUtil.substituirCaracteresEspeciais(subArea);
		return texto.contains(subArea.toUpperCase());
	}
}
