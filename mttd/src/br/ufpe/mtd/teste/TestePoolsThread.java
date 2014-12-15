package br.ufpe.mtd.teste;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;


public class TestePoolsThread {

	
	public static void main(String[] args) {
		
		FutureTask<Integer> t1 = new FutureTask<Integer>(new Chamavel());
		FutureTask<Integer> t2 = new FutureTask<Integer>(new Chamavel());
		
		ExecutorService executor = Executors.newFixedThreadPool(2);
        executor.execute(t1);
        executor.execute(t2);
        
        while(!t1.isDone() && !t2.isDone()){
        	try {
        			System.out.println(Thread.currentThread().getName()+" Chamando");
        			System.out.println("Percentual "+t1.get());
        			
        			System.out.println("Percentual "+t1.get()); 
        			
        			Thread.sleep(1000);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	
        	try {
        			System.out.println(Thread.currentThread().getName()+" Chamando");
        			System.out.println("Percentual "+t2.get(10, TimeUnit.SECONDS));
        			Thread.sleep(1000);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
        }
        
        executor.shutdown();
        
	}

		
	static class Chamavel implements Callable<Integer>{
		int percent = 0;
		static int numero = 0;
		int meuNumero ;
		
		public Chamavel() {
			numero ++;
			meuNumero = numero;
		}
		
		@Override
		public Integer call() throws Exception {
			
			while(percent < 100){
				System.out.println(Thread.currentThread().getName()+" Executando valor = "+percent);
				percent++;
				Thread.sleep(100);
				
				if(meuNumero % 2 == 0 && percent == 50 ){
//					return percent;
					throw new Exception("Fim forçado");
				}
			}
			
			return percent;
		}
		
	}
}
