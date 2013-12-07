package br.ufpe.mtd.thread;

import br.ufpe.mtd.excecao.MTDException;
import br.ufpe.mtd.util.MTDFactory;

/**
 * Tread basica do sistema que pode ter sua acao cancelada atraves de uma 
 * outra Thread desde que tenha uma referencia para ela.
 * Tambem pode ser feito pegando a referencia a partir de sua instancia pelo pool
 * de Threads.
 * 
 * Foi pensada para que seja executada no pool da aplicacao para isso se utilize do 
 * metodo executarNoPool. ao inves do start();
 * 
 * TODO: Analisar se precisa criar ids para as threads e solicitacoes de execucao no pool.
 * 
 * 
 * @author djalma
 *
 */
public class BaseThread extends Thread{

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
		// TODO Auto-generated method stub
		super.start();
	}
	
	public void checarCancelamento() throws MTDException{
		if(isCancelado()){
			throw new MTDException("Ação foi cancelada !!!");
		}
	}
	
	/**
	 * Dispara a execucao desta thread diretamente no pool 
	 * de Threads da aplicacao.
	 */
	public void executarNoPool(){
		MTDFactory.getInstancia().getPoolThread().execute(this);
	}
	
}
