package br.ufpe.mtd.teste;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import net.sf.jColtrane.handler.JColtraneXMLHandler;
import br.ufpe.mtd.dados.drive.OAIPMHDriver;
import br.ufpe.mtd.dados.indice.RepositorioIndiceLucene;
import br.ufpe.mtd.negocio.decodificacao.DecodificadorDocumentoDC;
import br.ufpe.mtd.negocio.entidade.Identificador;
import br.ufpe.mtd.negocio.entidade.MTDDocument;
import br.ufpe.mtd.util.MTDException;
import br.ufpe.mtd.util.MTDFactory;
import br.ufpe.mtd.util.analizers.SNAnalyser;
import br.ufpe.mtd.util.analizers.SNTokenizer;
import br.ufpe.mtd.util.enumerado.AreaCNPQEnum;
import br.ufpe.mtd.util.enumerado.MTDArquivoEnum;

public class TesteColetaMetadados {
	public static void main(String[] args){
		String urlBase="https://repositorio.ufpe.br/oai/request";//"http://repositorio.pucrs.br/oai/request";
		String metaDataPrefix ="qdc";
		String set="com_123456789_50";//"col_10923_338";
		OAIPMHDriver driver = OAIPMHDriver.getInstance(urlBase, metaDataPrefix);
		driver.setSet(set);

		List<Identificador> dadosRecebidos = new ArrayList<Identificador>();
		List<Identificador> identificadores = null;
		List<Identificador> deletados = new ArrayList<Identificador>();

		try {
			while(driver.hasNext()){
	
				
					identificadores = driver.getNextIdentifiers();
	
	
					//tratamento pois dados de identificadores podem vir repetidos 
					//verifiquei que o token para nova busca fornecido pelo proprio repositorio, 
					//resulta em busca que tras registros ja enviados em buscas anteriores. 
					if(identificadores.size() > 0 && !dadosRecebidos.contains(identificadores.get(0))){
						dadosRecebidos.addAll(identificadores);
						for (Identificador identificador : identificadores) {
							if(identificador.isDeletado()){
								deletados.add(identificador);
							}
						}
						System.out.println("Identificadores coletados: " +dadosRecebidos.size()+ " de "+driver.getTotalIdentificadores());
	
						for (Identificador identificador : deletados) {
							System.out.println("Deletado id : "+identificador.getId());
						}
	
						identificadores.removeAll(deletados);
						deletados.clear();
						//baixarDocsEsalvar(repositorio, identificadores, driver.getUrlBase(), driver.getMetaDataPrefix());
					}
	
			}//end while
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		System.out.println("Identificadores coletados: " +dadosRecebidos.size());
		DecodificadorDocumentoDC decodificador = new DecodificadorDocumentoDC();
		String url = null;
		InputStream is = null;
		int index= 0;
		//String stopFile = "WebContent/WEB-INF/aux_files/JOgma/Ogma_stoplist.txt";
		String snstopFile = MTDArquivoEnum.PASTA_ARQUIVOS_AUXILIARES.getArquivo().getAbsolutePath()+"\\JOgma\\sn_stoplist.txt";
		//SNAnalyser contextAnalyzer = new SNAnalyser(stopFile);
		
		
		//set o Tagger a ser usado por Tokenizer, listados em ordem de melhor desempenho
		SNTokenizer.setTagger("TreeTagger");//macmorpho 
		
		List<Identificador> listaRetentativa = new ArrayList<Identificador>();//verificar se tem instabilidadee no jColtraine
		try( PrintWriter out = new PrintWriter("./bdtd-doc.csv.txt")){
		    //out.println( text );
		
		out.println("id;url;documento;programa;area");
		for (Identificador identificador : dadosRecebidos) {
			//System.out.println(identificador.getId());
			//Coletar documento
			try{
				url = driver.getRecord(metaDataPrefix, identificador.getId());//busca os dados online			
				is = driver.getResponse(url);
				//DecodificadorDocumentoDC.parse(is, decodificador, identificador);
				SAXParser parser = SAXParserFactory.newInstance().newSAXParser();

				parser.parse(is, new JColtraneXMLHandler(decodificador));
				
				if(decodificador.getDocumentos().size() > index){//documentos sem campos requeridos n�o s�o adicionados
					MTDDocument d = decodificador.getDocumentos().get(index);
					//if(d.getAreaCNPQ()== null || d.getAreaCNPQ().isEmpty()|| d.getAreaCNPQ().equals("NAO_INFORMADO")){
					//System.out.println(d.getTitulo()); 
					System.out.println(" ->"+d.getUrl());
					String texto = d.getUrl()+";\""+d.getTitulo().replace(';', ',').replace('"', '\'').replaceAll("[ \n\t\r]+", " ")+" . "+d.getResumo().replace(';', ',').replace('"', '\'').replaceAll("[ \n\t\r]+", " ")+" . "+String.join(", ", d.getKeywords())+" . \"";
					out.print(index+";"+texto+";" + d.getPrograma()+";");
					out.println(AreaCNPQEnum.getGrandeAreaCNPQPorPrograma(d.getPrograma()));
					
					//SNAnalyser.displayTokensWithFullDetails(new SNAnalyser(snstopFile),d.getResumo());
				    //}
					index++;

					if(false){//verifica duplicatas
					RepositorioIndiceLucene rep = (RepositorioIndiceLucene) MTDFactory.getInstancia().getSingleRepositorioIndice();
					//Adicionando condi��o para encontrar duplicatas
					ArrayList<MTDDocument> doc = rep.getDocFirstInserted(d);
					if(doc !=null && !doc.isEmpty()){
						//System.out.println(d.getId() + " Duplicado "+doc.size()+" vezes.");
						//System.out.println(d.getTitulo());
						//System.out.println(" ->"+d.getUrl());
						//System.out.println(" ->"+d.getPrograma());
						//System.out.println("==============================================");
					}
					}

				}
				
			}catch(MTDException e ){
				Object o = e.getExtraData();
				if(o instanceof Identificador){
					listaRetentativa.add((Identificador)o);
				}
			}catch(Exception e ){
				e.printStackTrace();
			}finally{
				if(is != null){
					try {
						is.close();
					} catch (IOException e) {

						e.printStackTrace();
					}
				}
			}
		}//end for
		out.close();
		}//end try PrinterWriter
 catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println("Fim da execu��o.");
		return;
	}
	

}
