package br.ufpe.mtd.enumerado;

import java.io.File;

import br.ufpe.mtd.util.MTDFactory;
import br.ufpe.mtd.util.MTDParametros;

public enum MTDArquivoEnum {

	INDICE_DIR(MTDParametros.getExternalStorageDirectory(),MTDParametros.getMTDProperties().getProperty("indice_dir")),
	PASTA_TABELAS(MTDParametros.getExternalStorageDirectory(),"tabelas"),
	PASTA_TREINO(MTDParametros.getExternalStorageDirectory(),"treino"),
	TREINO_WGT(PASTA_TREINO.getArquivo(),"Treino.wgt.gz"),
	TREINO_MAP(PASTA_TREINO.getArquivo(),"Treino.map"),
	TREINO_UNIT(PASTA_TREINO.getArquivo(),"Treino.unit.gz"),
	TREINO_DWM(PASTA_TREINO.getArquivo(),"Treino.dwm.gz"),
	TREINO_MAPA_DATA(PASTA_TREINO.getArquivo(),"mapa.data"),
	DOC_TABLE(PASTA_TABELAS.getArquivo(),"doc_table.txt"), 
	WORD_TABLE(PASTA_TABELAS.getArquivo(),"word_table.txt"), 
	WORD_DOC_TABLE(PASTA_TABELAS.getArquivo(),"word_doc_table.txt"),
	TEMPLATE_TREINAMENTO(PASTA_TREINO.getArquivo(),"treinamento_template_vetor.tv"),
	TEMPLATE_TREINAMENTO_NORM(PASTA_TREINO.getArquivo(),"treinamento_template_vetor_norm.tv"),
	VECTOR_TREINAMENTO(PASTA_TREINO.getArquivo(),"treinamento_vetor.vec"),
	VECTOR_TREINAMENTO_NORM(PASTA_TREINO.getArquivo(),"treinamento_vetor_norm.vec"),
	CLS_AREA_PROGRAMA(PASTA_TREINO.getArquivo(),"area_programa.cls"),
	CLS_AREA_CNPQ(PASTA_TREINO.getArquivo(),"area_cnpq.cls"),
	CLS_GRANDE_AREA(PASTA_TREINO.getArquivo(),"grande_area.cls"),
	CLS_PROGRAMA(PASTA_TREINO.getArquivo(),"programa.cls"),
	PROPERTIES_TREINAMENTO(PASTA_TREINO.getArquivo(),"treinamento.prop"),
	TCEN_ARQUIVO(MTDParametros.getLocalFile("TCEN.txt")),
	OUTROS_ARQUIVO(MTDParametros.getLocalFile("OUTROS.txt")),
	CHLA_ARQUIVO(MTDParametros.getLocalFile("CHLA.txt")),
	CBS_ARQUIVO(MTDParametros.getLocalFile("CBS.txt"))
	;
	
	private File arquivo;

	private MTDArquivoEnum(File pastaRaiz,String nomeArquivo){
		
		if(!pastaRaiz.exists()){
			pastaRaiz.mkdirs();
		}
		
		arquivo = new File(pastaRaiz, nomeArquivo);
		
		if(arquivo.isDirectory() && !arquivo.exists()){
			arquivo.mkdirs();
		}else if(!arquivo.isDirectory() && !arquivo.exists()){
			try {
				arquivo.createNewFile();
			} catch (Exception e) {
				MTDFactory.getInstancia().getLog().salvarDadosLog(e);
			}
		}
	}
	
	private MTDArquivoEnum(File arquivoLocal){
		arquivo = arquivoLocal;
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
	
	public String getNomeArquivo(){
		return arquivo.getName().substring(0, arquivo.getName().lastIndexOf("."));
	}
}