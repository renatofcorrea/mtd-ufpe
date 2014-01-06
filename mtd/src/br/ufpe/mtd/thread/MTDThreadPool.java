package br.ufpe.mtd.thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import br.ufpe.mtd.util.MTDUtil;

public class MTDThreadPool {

	private ExecutorService pool;
	private int qtdThreads ;

	public MTDThreadPool(int qtdThreads) {
		this.qtdThreads = qtdThreads;
		initPool();		
	}
	
	private synchronized void initPool(){
		if(qtdThreads == 1){
			pool = Executors.newSingleThreadExecutor();
		}else{
			pool = Executors.newFixedThreadPool(this.qtdThreads);
		}
	}
	
	/**
	 * Devolve um pool com uma fila de treads ativa para
	 * agendamento de novas threads. caso o pool tenha sido 
	 * fechado sera aberta uma nova fila em outro pool.
	 * 
	 * @param runnable
	 */
	public  synchronized void executar(Runnable runnable){
		if(pool.isShutdown()){
			initPool();
		}
		pool.execute(runnable);
	}
	
	/**
	 * Fecha a fila de Treads atual para 
	 * execucao de novas treads e abre uma nova fila vazia.
	 * 
	 */
	public synchronized void resetPool(){
		fecharPool();
		initPool();
	}
	
	/**
	 * Ao chamar este metodo o pool de Treads sera 
	 * resetado e a tread que chamou o metodo ficara em espera ate
	 * que todas as Threads na fila de processamento seram encerradas.
	 * 
	 * Nesta chamada o pool e automaticamente resetado.
	 */
	public void aguardarFimDoPool(){
		ExecutorService poolAntigo = null;
		synchronized (pool) {
			fecharPool();
			poolAntigo = pool;
		}
		
		while(!poolAntigo.isTerminated()){
			try {
				MTDUtil.imprimirConsole(Thread.currentThread().getName()+" - Esperando fim do pool de thread...");
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Encerra a fila de execucao do pool 
	 * para novas threads. Quando todas as threads
	 * acabarem o pool sera encerrado.
	 */
	public synchronized void fecharPool(){
		pool.shutdown();
	}
}