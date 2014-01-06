package br.ufpe.mtd.xml;

import java.io.ByteArrayInputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import net.sf.jColtrane.handler.JColtraneXMLHandler;

public class MTDParser {

	public synchronized void parse(String xml, Object decodificador) throws Exception {
		SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
		ByteArrayInputStream bais = new ByteArrayInputStream(xml.getBytes("ISO-8859-1"));
		
		try {
			parser.parse(bais, new JColtraneXMLHandler(decodificador));
			
		} catch (IllegalArgumentException e) {
			
			try {
				if(bais != null){
					bais.close();
				}
			} catch (Exception e2) {
				
			}
			
			bais = new ByteArrayInputStream(ajustarCaracteresParaParse(xml));
			parser.parse(bais, new JColtraneXMLHandler(decodificador));
		}
		
		bais.close();
	} 
	
	public byte[] ajustarCaracteresParaParse(String xml){
		
		String retorno = xml;
		
		return retorno.getBytes();
	}
}
