package br.ufpe.mtd.thread;

public class Monitor {

	boolean lock;
	
	public synchronized void getLock(){
		try {
			while(lock){
				wait();
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		lock = true;
	}
	
	public synchronized void liberarLock(){
		lock = false;
		notify();
	}
	
}
