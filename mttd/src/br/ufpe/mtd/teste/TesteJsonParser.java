package br.ufpe.mtd.teste;

import java.util.Iterator;

import org.json.JSONObject;

import br.ufpe.mtd.util.MTDParametros;


public class TesteJsonParser {

	
	public static void main(String[] args) {
		try {
			String acesso = MTDParametros.acessoRepositorio();
			JSONObject j = new JSONObject(acesso);
			Iterator it = j.keys();
			while(it.hasNext()){
				JSONObject aux = j.getJSONObject(it.next().toString());
				System.out.println(aux.get("url"));
				System.out.println(aux.get("protocolo"));
				System.out.println(aux.get("set"));
			}
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
