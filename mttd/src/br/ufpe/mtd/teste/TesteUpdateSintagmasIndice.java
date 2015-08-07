package br.ufpe.mtd.teste;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.lucene.document.Document;

import br.ufpe.mtd.dados.indice.RepositorioIndiceLucene;
import br.ufpe.mtd.negocio.entidade.MTDDocument;
import br.ufpe.mtd.negocio.entidade.MTDDocumentBuilder;
import br.ufpe.mtd.util.MTDFactory;
import br.ufpe.mtd.util.MTDIterator;
import br.ufpe.mtd.util.MTDParametros;
import br.ufpe.mtd.util.enumerado.MTDArquivoEnum;

public class TesteUpdateSintagmasIndice {

	
	public static void main(String[] args) {
//		updateSintagmasIndice();
		gerarSintagmas();
	}
	
	public static void gerarSintagmas(){
		HashSet<String> hs;
		try {
			MTDIterator<String> it = MTDArquivoEnum.J_OGMA_STOP_LIST.lineIterator();
			hs = new HashSet<String>();
			while(it.hasNext()){
				hs.add(it.next());
			}
			it.close();
			MTDDocument doc = new MTDDocumentBuilder().buildDocument().build();
			doc.setTitulo("Brasil");
			doc.setResumo("O brasil é um pais com uma área territorial de cerca de 8 milhões de km². Este vasto pais é habitado desde antes da chegada dos portugueses em 1500...");
			
			Document d = doc.toDocumentComSintagmas(hs);
			
			System.out.println(d.toString());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static void updateSintagmasIndice(){
		try {
			RepositorioIndiceLucene repSintagmas = new RepositorioIndiceLucene(MTDArquivoEnum.INDICE_SINTAGMA_DIR.getArquivo(), MTDParametros.LUCENE_VERSION);
			RepositorioIndiceLucene rep = (RepositorioIndiceLucene)MTDFactory.getInstancia().getSingleRepositorioIndice();
			int contador = 0;
			MTDIterator<MTDDocument> iterator = rep.iterator();
			ArrayList<Document> docs = new ArrayList<Document>();
			while(iterator.hasNext()){
				MTDDocument doc = iterator.next();
				System.out.println(doc.getId()+" Qtd : "+contador);
				
				List<MTDDocument> lista = repSintagmas.consultar(doc.getId(), new String[]{MTDDocument.ID}, 1);
//				List<MTDDocument> lista = repSintagmas.consultar(doc.getAutor(), new String[]{MTDDocument.AUTOR}, 1);
				
				if(lista.isEmpty()){
					
					//docs.add( doc.toDocumentComSintagmas());
					//repSintagmas.inserirDocumento(docs);
					//docs.clear();
					
				}else{
					System.out.println(doc.getSintagmas());
				}
				
				contador++;
			}
			//oai:pucrs.br:3700 Qtd : 3798
			iterator.close();
			
			System.out.println("Concluido!!!"+contador);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
