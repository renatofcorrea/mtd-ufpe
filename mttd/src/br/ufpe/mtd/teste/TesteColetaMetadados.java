package br.ufpe.mtd.teste;


import java.io.IOException;
import java.io.InputStream;
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
import br.ufpe.mtd.util.enumerado.MTDArquivoEnum;

public class TesteColetaMetadados {
	public static void main(String[] args){
		String urlBase="http://www.repositorio.ufpe.br/oai/request";//"http://repositorio.pucrs.br/oai/request";
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
		for (Identificador identificador : dadosRecebidos) {
			//System.out.println(identificador.getId());
			//Coletar documento
			try{
				url = driver.getRecord(metaDataPrefix, identificador.getId());//busca os dados online			
				is = driver.getResponse(url);
				//DecodificadorDocumentoDC.parse(is, decodificador, identificador);
				SAXParser parser = SAXParserFactory.newInstance().newSAXParser();

				parser.parse(is, new JColtraneXMLHandler(decodificador));
				if(decodificador.getDocumentos().size() > index){//documentos sem campos requeridos não são adicionados
					MTDDocument d = decodificador.getDocumentos().get(index);
					//System.out.println("==============================================");
					//System.out.println(d.getTitulo()); 
					//System.out.println(" ->"+d.getUrl());
					//System.out.println(" ->"+d.getPrograma());
					//System.out.println(" ->"+d.getResumo());
					//SNAnalyser.displayTokensWithFullDetails(new SNAnalyser(snstopFile),d.getResumo());

					index++;

					RepositorioIndiceLucene rep = (RepositorioIndiceLucene) MTDFactory.getInstancia().getSingleRepositorioIndice();
					//Adicionando condição para encontrar duplicatas
					ArrayList<MTDDocument> doc = rep.getDocFirstInserted(d);
					if(doc !=null && !doc.isEmpty()){
						//System.out.println(d.getId() + " Duplicado "+doc.size()+" vezes.");
						//System.out.println(d.getTitulo());
						//System.out.println(" ->"+d.getUrl());
						//System.out.println(" ->"+d.getPrograma());
						//System.out.println("==============================================");
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
		System.out.println("Fim da execução.");
		return;
	}
	

}
