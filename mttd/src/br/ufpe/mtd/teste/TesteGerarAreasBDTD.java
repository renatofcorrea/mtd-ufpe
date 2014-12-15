package br.ufpe.mtd.teste;

import java.util.HashMap;

import br.ufpe.mtd.util.MTDIterator;
import br.ufpe.mtd.util.enumerado.MTDArquivoEnum;

public class TesteGerarAreasBDTD {

	public static void main(String[] args) {
		try {
			HashMap<String,Integer> progAreas = null;
			HashMap<String,HashMap<String,Integer>> mapa = new HashMap<String, HashMap<String,Integer>>();
			MTDArquivoEnum docTable = MTDArquivoEnum.BDTD_DOC_TABLE;
			MTDArquivoEnum areas = MTDArquivoEnum.BDTD_AREAS;
			
			MTDIterator<Integer> it = docTable.iterator();
			StringBuffer str = new StringBuffer();
			while(it.hasNext()){
				str.append((char)it.next().intValue());
			}
			
			//carregar dados em memoria.
			String[] strs = str.toString().split("\n");
			for (String string : strs) {
				String[] campos = string.split(";");
				String programa = campos[2];
				if(!mapa.containsKey(programa)){
					mapa.put(programa, new HashMap<String, Integer>());
				}
				
				progAreas = mapa.get(programa);
				
				String keyGAreaArea = campos[1]+";"+campos[3]+";"+ (campos.length == 5 ? campos[4] : "NÃO ENCOTRADO");
				if(!progAreas.containsKey(keyGAreaArea)){
					progAreas.put(keyGAreaArea, 0);
				}
				
				progAreas.put(keyGAreaArea, progAreas.get(keyGAreaArea) + 1);
				
			}
			
			//procurar area que tem maior frequencia para programa e salvar dados em arquivo
			StringBuffer saida = new StringBuffer();
			for(String prog : mapa.keySet()){
				int maiorFreq = 0;
				String aux = null;
				
				
				for(String area: mapa.get(prog).keySet()){
					int freq = mapa.get(prog).get(area);
					if(freq > maiorFreq){
						maiorFreq = freq;
						aux = area+";"+maiorFreq;
					}
				}
				
				aux = aux.replace("\n", "").replace("\r", "");
				saida.append(prog+";");
				saida.append(aux+"\n");
				
				System.out.println(prog);
				System.out.println("\t"+aux);
			}
			
			it.close();
			
			areas.escreverNoArquivo(saida.toString().getBytes(), false);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
