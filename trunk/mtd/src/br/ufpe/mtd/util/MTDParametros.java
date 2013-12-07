package br.ufpe.mtd.util;

import java.io.File;

public class MTDParametros {

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
	 * Dever� ser consultado de arquivo de propriedades 
	 * ou tabela de parametros em banco de dados.
	 * @return
	 */
	public static File getExternalStorageDirectory(){
		File diretorio = new File("D:"+File.separator+"MTD_Workspace");
		return diretorio;
	}
}