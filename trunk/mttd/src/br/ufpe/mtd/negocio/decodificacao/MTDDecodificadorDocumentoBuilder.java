package br.ufpe.mtd.negocio.decodificacao;
public class MTDDecodificadorDocumentoBuilder{
	
	private DecodificadorDocumento decodificador;
	
	public MTDDecodificadorDocumentoBuilder() {
		
	}
	
	public MTDDecodificadorDocumentoBuilder buildDecodificador(String metadataPrefix){
		criarDecodificador(MTDDocumentProtocolo.getInstancia(metadataPrefix));
		return this;
	}
	
	private void criarDecodificador(MTDDocumentProtocolo protocolo){
				
		if(MTDDocumentProtocolo.QDC.equals(protocolo)){
			decodificador =  new DecodificadorDocumentoDC();
		}else if(MTDDocumentProtocolo.MTD2BR.equals(protocolo)){
			decodificador = new DecodificadorDocumentoMTD2BR();
		}
		
	}
	
	public DecodificadorDocumento build(){
		return decodificador;
	}
	
	public static enum MTDDocumentProtocolo{
		MTD2BR, QDC;
		
		static MTDDocumentProtocolo getInstancia(String protocolo){
			if(protocolo == null){
				return null;
			}
			if(protocolo.equalsIgnoreCase("mtd2-br")){
				return MTD2BR;
			}else if(protocolo.equalsIgnoreCase("qdc")){
				return QDC;
			}else{
				return null;
			}
		}
	}
}