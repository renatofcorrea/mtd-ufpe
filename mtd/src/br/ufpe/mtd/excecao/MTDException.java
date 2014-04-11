package br.ufpe.mtd.excecao;


public class MTDException extends Exception{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Object extraData;
	
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
	
	public Object getExtraData() {
		return extraData;
	}
	
	public void setExtraData(Object extraData) {
		this.extraData = extraData;
	}

}
