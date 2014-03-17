package br.ufpe.mtd.enumerado;

import java.io.File;
import java.io.IOException;

import br.ufpe.mtd.util.MTDFactory;
import br.ufpe.mtd.util.MTDParametros;

public enum MTDArquivoEnum {

	INDICE_DIR(MTDParametros.getExternalStorageDirectory(),MTDParametros.getMTDProperties().getProperty("indice_dir")),
	PASTA_TABELAS(MTDParametros.getExternalStorageDirectory(),"tabelas"),
	DOC_TABLE(PASTA_TABELAS.getArquivo(),"doc_table.txt"), 
	WORD_TABLE(PASTA_TABELAS.getArquivo(),"word_table.txt"), 
	WORD_DOC_TABLE(PASTA_TABELAS.getArquivo(),"word_doc_table.txt");
	
	private File arquivo;

	private MTDArquivoEnum(File pastaRaiz,String nomeArquivo){
		
		if(!pastaRaiz.exists()){
			pastaRaiz.mkdirs();
		}
		
		arquivo = new File(pastaRaiz, nomeArquivo);
		
		if(arquivo.isDirectory() && !arquivo.exists()){
			arquivo.mkdirs();
		}else if(arquivo.isFile() && !arquivo.exists()){
			try {
				arquivo.createNewFile();
			} catch (IOException e) {
				MTDFactory.getInstancia().getLog().salvarDadosLog(e);
			}
		}
		
	}
	
	private MTDArquivoEnum(String nomeArquivo) {
		File diretorio = MTDParametros.getExternalStorageDirectory();
		String indiceDir = MTDParametros.getMTDProperties().getProperty("indice_dir");
		File pastaIndice = new File(diretorio, indiceDir);
		
		File pastaTabelas = new File(pastaIndice.getParentFile(), "tabelas");
		pastaTabelas.mkdirs();
	}

	public File getArquivo() {
		return arquivo;
	}
}