package br.ufpe.mtd.util.log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import br.ufpe.mtd.util.MTDException;
import br.ufpe.mtd.util.MTDParametros;
import br.ufpe.mtd.util.MTDUtil;

/**
 * Representa o log da aplicacao.
 * 
 * Chamar atraves de MTDFActory para uso adequado.
 * 
 * @author djalma
 *
 */
public class Log {

	private File logAplicDir;
	private File logAplicDados;
	private File logAplicExcecao;

	/**
	 * Controla o log da aplicacao.
	 * 
	 * salva dados de excecao e dados de negocio que sejam importantes para 
	 * a aplicacao.
	 * 
	 * log_dados para dados e log_excecao para as exceptions ocorridas na aplicacao.
	 * 
	 * Recupere objetos de log a partir da classe MTDFActory.
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public Log() throws FileNotFoundException, IOException {
		logAplicDir = new File(MTDParametros.getExternalStorageDirectory(),
				MTDParametros.logDir());
		logAplicDados = new File(logAplicDir, MTDParametros.nomeLogDados());
		logAplicExcecao = new File(logAplicDir, MTDParametros.nomeLogExcecao());
		logAplicDir.mkdirs();
		if (logAplicDir.exists()) {
			logAplicExcecao.createNewFile();
			logAplicDados.createNewFile();
		}
	}

	/**
	 * Salva as excecoes no log da aplicacao.
	 * 
	 * Para salvar dados que nao sejam excecao use a sobrecarga do metodo.
	 * 
	 * Os dados serao colocados na fila de escrita no log.
	 * @param excecao
	 */
	public void salvarDadosLog(final Exception excecao) {
		if(excecao instanceof MTDException && !((MTDException)excecao).isParaColocarLog()){
			return;
		}
		try {
			if (logAplicExcecao.exists()) {
				String cabecalho = "---------- Excecao ---------: \n"+ new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss").format(new Date());
				LogExceptionRunnable runnable = new LogExceptionRunnable(cabecalho, excecao, logAplicExcecao);
				runnable.executarNoPool();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Salva dados importantes no log da aplicacao.
	 * 
	 * Para salvar exceptions use a sobrecarga do metodo.
	 * 
	 * Os dados serao colocados na fila de escrita no log.
	 * 
	 * @param excecao
	 */
	public void salvarDadosLog(final String dado) {
		try {
			if (logAplicDados.exists()) {
				final String cabecalho = "---------- Dado ---------: "+ new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss").format(new Date());
				LogDadosRunnable runnable = new LogDadosRunnable(cabecalho, dado, logAplicDados);
				runnable.executarNoPool();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
	
	private class LogExceptionRunnable extends LogRunnable {
		private Exception excecao;
		private String cabecalho;
		private File arquivo;
		
		public LogExceptionRunnable(String cabecalho, Exception excecao, File arquivo) {
			this.excecao = excecao;
			this.cabecalho = cabecalho;
			this.arquivo = arquivo;
		}
		
		@Override
		public void run() {
			FileOutputStream fos = recuperarStreamParaArquivo(arquivo);
			PrintStream printStream = null;
			try {
				if(fos == null){
					return;
				}
				printStream = new PrintStream(fos);
				fos.write(cabecalho.getBytes());
				fos.write('\n');
				excecao.printStackTrace(printStream);
				
				MTDUtil.imprimirConsole(cabecalho);
				MTDUtil.imprimirConsole(excecao);
				
			} catch (IOException e) {
				e.printStackTrace();
			} finally {// trata a excecao fora mas fecha a stream sempre
				try {
					if(printStream!= null){
						printStream.flush();
						printStream.close();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				try {
					if(fos != null){
						fos.flush();
						fos.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	
	private class LogDadosRunnable extends LogRunnable{
		private String cabecalho;
		private String dado;
		private File arquivo;
		public LogDadosRunnable(String cabecalho, String dado, File arquivo) {
			this.cabecalho = cabecalho;
			this.dado = dado;
			this.arquivo = arquivo;
		}
		
		@Override
		public void run() {
			FileOutputStream fos = recuperarStreamParaArquivo(arquivo);
			
			if(fos == null){
				return;
			}
			
			try {
				fos.write(cabecalho.getBytes());
				fos.write('\n');
				fos.write(dado.getBytes());
				fos.write('\n');

				MTDUtil.imprimirConsole(cabecalho);
				MTDUtil.imprimirConsole(dado);
				
			} catch (IOException e) {
				e.printStackTrace();
			} finally {// trata a excecao fora mas fecha a stream sempre
				if (fos != null) {
					try {
						fos.flush();
						fos.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}	
}
