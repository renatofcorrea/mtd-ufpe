package br.ufpe.mtd.teste;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.ParseException;
import org.xml.sax.SAXException;

import br.ufpe.mtd.dados.RepositorioIndice;
import br.ufpe.mtd.entidade.DocumentWrapper;
import br.ufpe.mtd.excecao.MTDException;
import br.ufpe.mtd.negocio.ControleIndice;
import br.ufpe.mtd.util.MTDFactory;
import br.ufpe.mtd.util.MTDUtil;

/*
	http://www.ibm.com/developerworks/br/java/library/os-apache-lucenesearch/

	http://www.lucenetutorial.com/lucene-in-5-minutes.html
	
	http://faladede.opportunitas.com.br/lucene-facil-indexando-conteudo-forma-simples-direta/
	
	http://vitorpamplona.com/wiki/Introdu%C3%A7%C3%A3o%20ao%20Apache%20Lucene
	
 */
public class TesteRepositorioIndice {

	
	public static void main(String [] args){
		
		//testeCarga();
		indexar();
	}
	
	static void consultar(String termo){
		
	}
	
	static void testeCarga(){
		try {
			long inicio = System.currentTimeMillis();
			RepositorioIndice rep = MTDFactory.getInstancia().getSingleRepositorioIndice();
			ExecutorService poll = Executors.newFixedThreadPool(50);
			
			for(int i = 0; i < 1000; i ++){
				ThreadTestaRepositorioIndice t = new ThreadTestaRepositorioIndice(rep);
				poll.execute(t);
			}
			
			poll.shutdown();
			
			while(!poll.isTerminated()){
				try {
					ThreadTestaRepositorioIndice.sleep(1000);					
				} catch (InterruptedException e) {
					MTDFactory.getInstancia().getLog().salvarDadosLog(e);
				}
			}
			
			rep.fecharRepositorio();
			MTDUtil.imprimirConsole("Tempo total : "+ (System.currentTimeMillis() - inicio));
			
			//enviar meail de alert para equipe tecnica se servico der excecao
		} catch (IOException e) {
			MTDFactory.getInstancia().getLog().salvarDadosLog(e);
		}
	}

	static void indexar(){
		try {
			long inicio = System.currentTimeMillis();
			RepositorioIndice rep = MTDFactory.getInstancia().getSingleRepositorioIndice();
			ControleIndice controle = new ControleIndice(rep);
			
			controle.indexar("http://tede.pucrs.br/tde_oai/oai3.php", "mtd2-br");
			
			ExecutorService pool = MTDFactory.getInstancia().getPoolThread();
			
			while(!pool.isTerminated()){
				Thread.sleep(1000);
			}
			
			rep.fecharRepositorio();
			
			MTDUtil.imprimirConsole("Tempo total : "+ (System.currentTimeMillis() - inicio));
			
			//enviar meail de alert para equipe tecnica se servico der excecao
		} catch (Exception e) {
			MTDFactory.getInstancia().getLog().salvarDadosLog(e);
		} 
	}
	
	
	static class ThreadTestaRepositorioIndice extends Thread{
		
		RepositorioIndice rep;
		public ThreadTestaRepositorioIndice(RepositorioIndice rep) {
			this.rep = rep;
		}
		
		@Override
		public void run() {
			super.run();
			
		//	inserir();
			
			ler();
		}
		
		void ler(){
			try {
				long leitura ;
				long inicio = System.currentTimeMillis();
				ArrayList<DocumentWrapper> docs;
				docs = rep.consultar("futebol", 10000);
				
				//================consulta===================
	//			long i = 0;
	//			for (Documento documento : docs) {
	//				System.out.println((++i)+" " + documento.getTitulo()+" - "+documento.getAutor());
	//			}
				leitura = (System.currentTimeMillis() - inicio );
				
				MTDUtil.imprimirConsole("Registros: "+docs.size()+" Tempo Leitura "+leitura);
				
			} catch (Exception e) {
				MTDFactory.getInstancia().getLog().salvarDadosLog(e);
			}			
		}
		
		void inserir(){
			try {
				System.out.println(Thread.currentThread().getName()+ " inserindo ");
				long insercao;
				ArrayList<String> keywords = new ArrayList<String>();
				
				keywords.add("keyword"+System.currentTimeMillis());
				keywords.add("keyword"+System.currentTimeMillis());
				keywords.add("keyword"+System.currentTimeMillis());
				keywords.add("keyword"+System.currentTimeMillis());
				
	
				
				//================insercao===================
				long inicio = System.currentTimeMillis();
				ArrayList<Document> docs = new ArrayList<Document>();
				for(int i = 0; i < 10; i++){
					long instante = System.currentTimeMillis();
					Document doc = new Document();
					doc.add(new Field("titulo ",Thread.currentThread().getName()+instante, Field.Store.YES,Field.Index.ANALYZED));
					doc.add(new Field("resumo",Thread.currentThread().getName()+instante, Field.Store.YES,Field.Index.ANALYZED));

					for (int j = 0; j < keywords.size() && keywords.get(j) != null; i++) {
						doc.add(new Field("keyword", keywords.get(j), Field.Store.YES,
								Field.Index.ANALYZED));
					}
					docs.add(doc);
				}
				
				rep.inserirDocumento(docs);
				
				insercao = (System.currentTimeMillis() - inicio );
				System.out.println("Insercao "+insercao);
				System.out.println(Thread.currentThread().getName()+ " concluido ");
			} catch (Exception e) {
				MTDFactory.getInstancia().getLog().salvarDadosLog(e);
			}
		}
	}
}
