package br.ufpe.mtd.negocio.thread;

import br.ufpe.mtd.util.MTDException;
import br.ufpe.mtd.util.MTDFactory;
import br.ufpe.mtd.util.MTDThreadPool;

/**
 * Tread basica do sistema que pode ter sua acao cancelada atraves de uma 
 * outra Thread desde que tenha uma referencia para ela.
 * Tambem pode ser feito pegando a referencia a partir de sua instancia pelo pool
 * de Threads.
 * 
 * Foi pensada para que seja executada no pool da aplicacao para isso se utilize do 
 * metodo executarNoPool. ao inves do start();
 * 
 * @author djalma
 *
 */
public abstract class BaseThread extends Thread{

	private MTDThreadPool pool;
	private boolean cancelado;
	
	public boolean isCancelado() {
		return cancelado;
	}
	
	public void cancelar(){
		cancelado = true;
	}
	
	/**
	 * Use o metodo executarNoPool 
	 * para ter o comportamento planejado para 
	 * a aplicacao.
	 */
	@Deprecated
	@Override
	public final synchronized void start() {
		System.out.println("Metodo deprecado. Use o metodo executar no pool");
	}
	
	public void checarCancelamento() throws MTDException{
		if(isCancelado()){
			throw new MTDException("Ação foi cancelada !!!");
		}
	}
	
	/**
	 * As classes filhas de BaseThread devem implementar este
	 * metodo que sera a acao a ser executada. Sera o metodo
	 * que substitue o tradicional run.
	 */
	public abstract void execucao();
	
	/**
	 * Executa uma chamada ao metodo execucao e 
	 * depois de concluido retira a Thread do pool 
	 * de Threads.
	 */
	@Override
	public final void run() {
		try {
			super.run();
			execucao();
		} catch (Exception e) {
			MTDFactory.getInstancia().getLog().salvarDadosLog(e);
		}finally{
			pool.sairDoPool(this);
		}
	}
	
	public void executarNoPool(MTDThreadPool pool){
		this.pool = pool;
		pool.executar(this);
	}
	
	/**
	 * Dispara a execucao desta thread diretamente no pool 
	 * de Threads da aplicacao.
	 */
	public final void executarNoPool(){
		if(pool != null){
			throw new RuntimeException("Já existe pool setado para esta Thread.");
		}
		pool = MTDFactory.getInstancia().getPoolThread();
		pool.executar(this);
	}	
}