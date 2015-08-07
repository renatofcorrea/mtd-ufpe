package br.ufpe.mtd.teste;

import java.util.HashMap;

import br.ufpe.mtd.dados.drive.OAIPMHDriver;

public class TesteColetaSets {

		public static void main(String[] args){
			String urlBase="http://www.repositorio.ufpe.br/oai/request";//"http://repositorio.pucrs.br/oai/request";
			//String metaDataPrefix ="qdc";
			//String set="com_123456789_50";//"col_10923_338";
			OAIPMHDriver driver = OAIPMHDriver.getInstance(urlBase);
			//driver.setSet(set);
			String progr="com_123456789_151";
			try {
				HashMap<String,String> hs = driver.getSets("Programa[A-Za-zÀ-ú -/]+");//"Programa[A-Za-zÀ-ú -/]+");
				System.out.println(hs.size());
				System.out.println(hs.toString());
				System.out.println("Id Programa " + progr+ " Nome: "+hs.get(progr));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	}

}
