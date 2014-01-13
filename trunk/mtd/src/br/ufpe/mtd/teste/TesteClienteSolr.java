package br.ufpe.mtd.teste;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;

public class TesteClienteSolr {

	public static void main(String[] args) {

		try {
			String urlString = "http://localhost:8080/solr/collection1";
			SolrServer solr = new HttpSolrServer(urlString);
			
			limparIndice(solr);
			
			System.out.println("Fim");
			System.exit(0);//sair.
			
			SolrQuery parameters = new SolrQuery();
			parameters.set("q", "id:552199");
			QueryResponse resposta = solr.query(parameters);
			SolrDocumentList list = resposta.getResults();
			
			for (SolrDocument solrDocument : list) {
				String dados = "";
				
				dados += " id "+solrDocument.get("id");
				dados += " nome "+solrDocument.get("name");
				dados += " preço "+solrDocument.get("price");
				System.out.println("-------- Dados -----------");
				System.out.println(dados);
			
			}
			
			if(list.isEmpty()){
				SolrInputDocument document = new SolrInputDocument();
				document.addField("id", "552199");
				document.addField("repositorio", "Gouda cheese wheel");
				document.addField("price", "49.99");
				UpdateResponse response = solr.add(document);
				
				// Remember to commit your changes!
				solr.commit();
			}else{
				solr.deleteById("552199");
				solr.commit();
			}
			
			
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	/**
	 * Busca todos os ids no indice e deleta todos.
	 * 
	 * @param solr
	 * @throws SolrServerException
	 * @throws IOException
	 */
	private static void limparIndice(SolrServer solr) throws SolrServerException, IOException{
		SolrQuery parameters = new SolrQuery();
		parameters.set("q", "id:*");
		
		
		QueryResponse resposta = solr.query(parameters);
		SolrDocumentList list = resposta.getResults();
		
		while(list.size() > 0){
			
			String strIds = "";
			List<String> ids = new ArrayList<String>();
			for (SolrDocument solrDocument : list) {
				String id = solrDocument.getFieldValue("id").toString();
				ids.add(id);
				strIds += id+ " , ";
			}
			
			System.out.println("Deletando ids : "+ strIds);
			
			solr.deleteById(ids);
			solr.commit();
			
			resposta = solr.query(parameters);
			list = resposta.getResults();
		}	
	}
}
