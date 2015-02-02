package br.ufpe.mtd.util.enumerado;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

import br.ufpe.mtd.util.MTDFactory;
import br.ufpe.mtd.util.MTDIterator;
import br.ufpe.mtd.util.MTDParametros;
import br.ufpe.mtd.util.analizers.WordList;

/**
 * Enumerdo que representra todos os arquivos que precisarao ser manipulados pelo
 * sistema. Incluido pastas. 
 * 
 * Prove ferramentas para trabalhar com esses arquivos como Streams para leitura e escrita
 * alem de iterators para navegar entre os bytes ou linhas dos arquivos.
 * Possui ainda metodos que permitem recuperar informacoes sobre os arquivos.
 * 
 * @author djalma
 *
 */
public enum MTDArquivoEnum {

	INDICE_DIR(MTDParametros.getExternalStorageDirectory(),MTDParametros.indiceDir(), true),
	INDICE_SINTAGMA_DIR(MTDParametros.getExternalStorageDirectory(),MTDParametros.indiceDir()+"_sn", true),
	PASTA_TABELAS(MTDParametros.getExternalStorageDirectory(),"tabelas", true),
	PASTA_TREINO(MTDParametros.getExternalStorageDirectory(),"treino", true),
	PASTA_ARQUIVOS_AUXILIARES(MTDParametros.getLocalFolder(),"aux_files", true),
	PASTA_J_OGMA(PASTA_ARQUIVOS_AUXILIARES.getArquivo(),"JOgma", true),
	J_OGMA_STOP_LIST(PASTA_J_OGMA.getArquivo(),"sn_stoplist.txt", false),
	J_OGMA_GRAMATICA(PASTA_J_OGMA.getArquivo(),"Ogma-GRAMATICA-sort.csv", false),
	J_OGMA_NOMES(PASTA_J_OGMA.getArquivo(),"Ogma-NOMES-sort.csv", false),
	J_OGMA_VERBOS(PASTA_J_OGMA.getArquivo(),"Ogma-VERBOS-sort.csv", false),
	PASTA_TAGGER(PASTA_ARQUIVOS_AUXILIARES.getArquivo(),"Tagger", true),
	PASTA_TREE_TAGGER(PASTA_TAGGER.getArquivo(),"TreeTagger", true),
	TREINO(PASTA_TREINO.getArquivo(),"Treino.gz", false),
	TREINO_WGT_GZ(PASTA_TREINO.getArquivo(),"Treino.wgt.gz", false),
	TREINO_WGT(PASTA_TREINO.getArquivo(),"Treino.wgt", false),
	TREINO_MAP(PASTA_TREINO.getArquivo(),"Treino.map", false),
	TREINO_UNIT(PASTA_TREINO.getArquivo(),"Treino.unit.gz", false),
	TREINO_DWM(PASTA_TREINO.getArquivo(),"Treino.dwm.gz", false),
	TREINO_MAPA_DATA(PASTA_TREINO.getArquivo(),"mapa.data", false),
	TREINO_MEDIDA_QUALIDADE(PASTA_TREINO.getArquivo(),"medida_qualidade.data", false),
	TREINO_STATUS_SISTEMA(PASTA_TREINO.getArquivo(),"status_sistema.data", false),
	DOC_TABLE(PASTA_TABELAS.getArquivo(),"doc_table.txt", false), 
	WORD_TABLE(PASTA_TABELAS.getArquivo(),"word_table.txt", false), 
	WORD_DOC_TABLE(PASTA_TABELAS.getArquivo(),"word_doc_table.txt", false),
	TEMPLATE_TREINAMENTO(PASTA_TREINO.getArquivo(),"treinamento_template_vetor.tv", false),
	TEMPLATE_TREINAMENTO_NORM(PASTA_TREINO.getArquivo(),"treinamento_template_vetor_norm.tv", false),
	VECTOR_TREINAMENTO(PASTA_TREINO.getArquivo(),"treinamento_vetor.vec", false),
	VECTOR_TREINAMENTO_NORM(PASTA_TREINO.getArquivo(),"treinamento_vetor_norm.vec", false),
	CLS_AREA_PROGRAMA(PASTA_TREINO.getArquivo(),"area_programa.cls", false),
	CLS_AREA_CNPQ(PASTA_TREINO.getArquivo(),"area_cnpq.cls", false),
	CLS_GRANDE_AREA(PASTA_TREINO.getArquivo(),"grande_area.cls", false),
	CLS_PROGRAMA(PASTA_TREINO.getArquivo(),"programa.cls", false),
	PROPERTIES_TREINAMENTO(PASTA_TREINO.getArquivo(),"treinamento.prop", false),
	STOP_WORDS(PASTA_ARQUIVOS_AUXILIARES.getArquivo(),"bdtdstopwords.txt", false),
	TCEN_ARQUIVO(PASTA_ARQUIVOS_AUXILIARES.getArquivo(),"TCEN.txt", false),
	OUTROS_ARQUIVO(PASTA_ARQUIVOS_AUXILIARES.getArquivo(),"OUTROS.txt",false),
	CHLA_ARQUIVO(PASTA_ARQUIVOS_AUXILIARES.getArquivo(),"CHLA.txt",false),
	CBS_ARQUIVO(PASTA_ARQUIVOS_AUXILIARES.getArquivo(),"CBS.txt",false),
	BDTD_DOC_TABLE(PASTA_ARQUIVOS_AUXILIARES.getArquivo(),"bdtddoctable.txt",false),
	BDTD_AREAS(PASTA_ARQUIVOS_AUXILIARES.getArquivo(),"bdtdareas.txt",false)
	;
	
	private File arquivo;

	private MTDArquivoEnum(File pastaRaiz,String nomeArquivo, boolean isDir){
		
		if(!pastaRaiz.exists()){
			pastaRaiz.mkdirs();
		}
		
		arquivo = new File(pastaRaiz, nomeArquivo);
		
		if(isDir && !arquivo.exists()){
			arquivo.mkdirs();
		}else if(!isDir && !arquivo.exists()){
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
		String indiceDir = MTDParametros.indiceDir();
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
	
	public String getPathSemExtensao(){
		return arquivo.getParent()+File.separator+arquivo.getName().substring(0, arquivo.getName().lastIndexOf("."));
	}
	
	public FileInputStream getFileInputStream(){
		try {
			return new FileInputStream(getArquivo());
		} catch (FileNotFoundException e) {
			return null;
		}
	}
	
	public FileOutputStream getFileOutputStream(boolean escreverNoFimDoArquivo){
		try {
			return new FileOutputStream(getArquivo(), escreverNoFimDoArquivo);
		} catch (FileNotFoundException e) {
			return null;
		}
	}
	
	public void escreverNoArquivo(byte[] dados, boolean escreverNoFimDoArquivo) throws IOException{
		FileOutputStream fos = getFileOutputStream(escreverNoFimDoArquivo);
		fos.write(dados);
		fos.flush();
		fos.close();
	}
	
	/**
	 * Itera sobre o arquivo pegando os dados devolvidos pela stream que aponta 
	 * para o arquivo. Os dados lidos devem ser convertidos para o formato escolhido.
	 * @return
	 * @throws Exception
	 */
	public MTDIterator<Integer> iterator() throws Exception {
		return new MTDIterator<Integer>() {
			FileInputStream fis;
			Integer byteLido;
			
			@Override
			public Integer next() throws Exception {
				int retorno = this.byteLido;
				
				this.byteLido = fis.read();
				
				return retorno;
			}
			
			@Override
			public void init() throws Exception {
				fis = getFileInputStream();
				byteLido = fis.read();
			}
			
			@Override
			public boolean hasNext() throws Exception {
				if(fis == null){
					return false;
				}
				return byteLido != null && byteLido != -1; 
			}
			
			@Override
			public void close(){
				try {
					if(fis != null){
						fis.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
	}
	
	public MTDIterator<String> lineIterator() throws Exception {
		return new MTDIterator<String>() {
			BufferedReader buffer;
			String linhaLida ;
			
			@Override
			public String next() throws Exception {
				String retorno = this.linhaLida;
				
				linhaLida = buffer.readLine();
				
				return retorno;
			}
			
			@Override
			public void init() throws Exception {
	            FileReader fr = new FileReader(arquivo);
	            buffer = new BufferedReader(fr);
	            linhaLida = buffer.readLine();
			}
			
			@Override
			public boolean hasNext() throws Exception {
				if(buffer == null){
					return false;
				}
				return linhaLida != null; 
			}
			
			@Override
			public void close(){
				try {
					if(buffer != null){
						buffer.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
	}
}