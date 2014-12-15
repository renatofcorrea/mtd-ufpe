package br.ufpe.mtd.util;

/**
 * Representa uma excecao customizada do sistema.
 * 
 * @author djalma
 *
 */
public class MTDException extends Exception{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private boolean paraColocarLog;
	private Object extraData;
	
	/**
	 * Imprime a mensagem no console. coloca no log se a
	 * opcao estiver true.
	 * @param paraColocarLog
	 * @param mensagem
	 */
	public MTDException(boolean paraColocarLog, String mensagem) {
		this(mensagem);
		this.paraColocarLog = paraColocarLog;
	}

	public MTDException(Exception e , String mensagem) {
		super(mensagem+ " - Causa: "+e.getCause()+" Mensagem "+ e.getLocalizedMessage());
		setStackTrace(e.getStackTrace());
		for(Throwable supressed:e.getSuppressed()){
			addSuppressed(supressed);
		}
	}
	
	public MTDException(String mensagem) {
		super(mensagem);
		paraColocarLog = true;
	}
	
	public Object getExtraData() {
		return extraData;
	}
	
	public void setExtraData(Object extraData) {
		this.extraData = extraData;
	}

	public boolean isParaColocarLog() {
		return paraColocarLog;
	}

	public void setParaColocarLog(boolean paraColocarLog) {
		this.paraColocarLog = paraColocarLog;
	}

}
