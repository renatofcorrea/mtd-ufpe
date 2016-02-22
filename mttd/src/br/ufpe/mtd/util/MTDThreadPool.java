package br.ufpe.mtd.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Classe que serve para controlar a execucao
 * de Threads dentro do sistema. 
 * 
 * Preferencialmente utilize classes Filhas de BaseThread.
 * 
 * @author djalma
 *
 */
public class MTDThreadPool {

	private ExecutorService pool;
	private int qtdThreads;
	List<Runnable> lista;

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
		
		lista = new ArrayList<Runnable>();
	}
	
	/**
	 * Devolve um pool com uma fila de treads ativa para
	 * agendamento de novas threads. caso o pool tenha sido 
	 * fechado sera aberta uma nova fila em outro pool.
	 * 
	 * @param runnable
	 */
	public synchronized void executar(Runnable runnable){
		if(pool.isShutdown()){
			initPool();
		}
		lista.add(runnable);
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
				//TODO:Fica imprimindo direto, sem parar, Thread-0!!!
				//MTDUtil.imprimirConsole(Thread.currentThread().getName()+" - Esperando fim das threads "+lista.toString());
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public synchronized int qtdThreadsAlive(){
		int qtd = 0;
		for(Runnable r : lista){
			Thread t = (Thread)r;
			if(!t.getState().equals(Thread.State.TERMINATED)){
				qtd++;
			}
		}
		return qtd;
	}
	
	public synchronized  void sairDoPool(Runnable r){
		lista.remove(r);
	}
	
	/**
	 * Encerra a fila de execucao do pool 
	 * para novas threads. Quando todas as threads
	 * acabarem o pool sera encerrado.
	 */
	public synchronized void fecharPool(){
		pool.shutdown();
	}
	
	public synchronized void fecharPoolAgora(){
		try {
			pool.shutdownNow();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	
}