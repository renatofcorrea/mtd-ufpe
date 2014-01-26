package br.ufpe.mtd.excecao;


public class MTDException extends Exception{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public MTDException(Exception e , String mensagem) {
		super(mensagem+ " - Causa: "+e.getCause()+" Mensagem "+ e.getLocalizedMessage());
		
		setStackTrace(e.getStackTrace());
		
		for(Throwable supressed:e.getSuppressed()){
			addSuppressed(supressed);
		}
	}
	
	public MTDException(String mensagem) {
		super(mensagem);
	}
}
