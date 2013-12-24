package br.ufpe.mtd.thread;

import java.io.File;
import java.io.FileOutputStream;

import br.ufpe.mtd.util.MTDFactory;

/**
 * Classe que representa um objeto a ser agendado para 
 * escrita de dados no arquivi de log da aplicacao.
 * 
 * Classes que estendam esta devem impleletar o metodo run
 * 
 * Para que execute dentro do comportamento previsto para a 
 * aplicacao deve ser chamado o metodo executarNoPool.
 * 
 * @author djalma
 *
 */
public abstract class LogRunnable implements Runnable{

	/**
	 * Tenta abrir stream para escrever no arquivo
	 * trata numero de tentativas para sistemas de
	 * arquivos mais lentos que 
	 * demoram a liberar o lock do arquivo apos 
	 * o uso.
	 * 
	 * 
	 * @param arquivo
	 * @return
	 */
	public FileOutputStream recuperarStreamParaArquivo(File arquivo){
		FileOutputStream fos = null;
		int tentativas = 0;	
		while(fos == null && tentativas < 10){
			try {
				tentativas ++;
				fos = new FileOutputStream(arquivo, true);
				
			} catch (Exception e) {
				System.out.println(Thread.currentThread().getName()+"Tentado criar stream arquivo tentativa "+ tentativas);
				try {
					Thread.sleep(100);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		
		}
		
		if(fos == null){
			System.out.println("Nao conseguiu colocar dados no log apos ("+tentativas+") tentativas");
		}
		return fos;
	}
	
	/**
	 * Executa a Runnable atual no pool da aplicacao.
	 */
	public void executarNoPool(){
		MTDFactory.getInstancia().getLogPoolThread().execute(this);
	}
}
