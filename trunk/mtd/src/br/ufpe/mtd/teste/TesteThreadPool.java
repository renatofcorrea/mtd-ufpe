package br.ufpe.mtd.teste;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TesteThreadPool {

	static ExecutorService pool = Executors.newSingleThreadExecutor();
	
	public static void main(String[] args) {
		
		new TesteThreadPool(). new MinhaThread().start();
		pool.execute(new TesteThreadPool(). new MinhaThread());
		
		
	}
	
	class MinhaThread extends Thread{
		
		@Override
		public synchronized void start() {
			System.out.println("Metodo deprecado. Use o metodo executar no pool");
		}
		
		@Override
		public void run() {
			super.run();
			System.out.println("Run iniciado");
		}
		
		public void executarNoPoo(){
			System.out.println("Metodo executar no pool iniciado!!!");
			pool.execute(this);
		}
	}	
	
}
