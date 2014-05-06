package br.ufpe.mtd.teste;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;
import java.util.SortedMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import net.sf.jColtrane.handler.JColtraneXMLHandler;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexableField;
import org.xml.sax.SAXException;

import br.ufpe.mtd.entidade.MTDDocument;
import br.ufpe.mtd.xml.DecodificadorDocumento;

public class TesteEncoding {

	
	public static void main(String[] args) {
		
		parserXMLSemString();
		
	
	}
	
	static void parserXMLSemString(){
		/*
		 * 
		    
		 */
		DecodificadorDocumento decodificador = new DecodificadorDocumento();
		
		String listaInseridos = "oai:pucrs.br:2044,oai:pucrs.br:2045,oai:pucrs.br:1289,oai:pucrs.br:3066,oai:pucrs.br:3429,oai:pucrs.br:3354,oai:pucrs.br:6,oai:pucrs.br:5,oai:pucrs.br:4,oai:pucrs.br:7,oai:pucrs.br:8,oai:pucrs.br:9,oai:pucrs.br:10,oai:pucrs.br:11,oai:pucrs.br:13,oai:pucrs.br:14,oai:pucrs.br:15,oai:pucrs.br:16,oai:pucrs.br:17,oai:pucrs.br:18,oai:pucrs.br:19,oai:pucrs.br:20,oai:pucrs.br:21,oai:pucrs.br:22,oai:pucrs.br:23,oai:pucrs.br:24,oai:pucrs.br:25,oai:pucrs.br:26,oai:pucrs.br:27,oai:pucrs.br:28,oai:pucrs.br:29,oai:pucrs.br:30,oai:pucrs.br:31,oai:pucrs.br:32,oai:pucrs.br:33,oai:pucrs.br:34,oai:pucrs.br:35,oai:pucrs.br:36,oai:pucrs.br:37,oai:pucrs.br:38,oai:pucrs.br:39,oai:pucrs.br:40,oai:pucrs.br:41,oai:pucrs.br:42,oai:pucrs.br:43,oai:pucrs.br:44,oai:pucrs.br:45,oai:pucrs.br:46,oai:pucrs.br:47,oai:pucrs.br:48,oai:pucrs.br:49,oai:pucrs.br:50,oai:pucrs.br:51,oai:pucrs.br:52,oai:pucrs.br:53,oai:pucrs.br:54,oai:pucrs.br:55,oai:pucrs.br:56,oai:pucrs.br:57,oai:pucrs.br:58,oai:pucrs.br:59,oai:pucrs.br:60,oai:pucrs.br:61,oai:pucrs.br:62,oai:pucrs.br:63,oai:pucrs.br:64,oai:pucrs.br:65,oai:pucrs.br:66,oai:pucrs.br:67,oai:pucrs.br:68,oai:pucrs.br:69,oai:pucrs.br:70,oai:pucrs.br:71,oai:pucrs.br:12,oai:pucrs.br:72,oai:pucrs.br:73,oai:pucrs.br:74,oai:pucrs.br:75,oai:pucrs.br:76,oai:pucrs.br:77,oai:pucrs.br:78,oai:pucrs.br:79,oai:pucrs.br:80,oai:pucrs.br:81,oai:pucrs.br:83,oai:pucrs.br:82,oai:pucrs.br:84,oai:pucrs.br:85,oai:pucrs.br:86,oai:pucrs.br:87,oai:pucrs.br:89,oai:pucrs.br:90,oai:pucrs.br:91,oai:pucrs.br:92,oai:pucrs.br:93,oai:pucrs.br:94,oai:pucrs.br:95,oai:pucrs.br:96,oai:pucrs.br:97,oai:pucrs.br:98,oai:pucrs.br:99,oai:pucrs.br:100,oai:pucrs.br:101,oai:pucrs.br:102,oai:pucrs.br:103,oai:pucrs.br:104,oai:pucrs.br:105,oai:pucrs.br:106,oai:pucrs.br:108,oai:pucrs.br:109,oai:pucrs.br:110,oai:pucrs.br:111,oai:pucrs.br:112,oai:pucrs.br:113,oai:pucrs.br:114,oai:pucrs.br:115,oai:pucrs.br:116,oai:pucrs.br:117,oai:pucrs.br:118,oai:pucrs.br:119,oai:pucrs.br:120,oai:pucrs.br:121,oai:pucrs.br:122,oai:pucrs.br:123,oai:pucrs.br:124";
		
		String[] registros = listaInseridos.split(",");
		
		for (String idRegistro : registros) {
			
			String repostiorio = "http://tede.pucrs.br/tde_oai/oai3.php?verb=GetRecord&metadataPrefix=mtd2-br&identifier="+idRegistro;
			
			URL urlBase;
			try {
				urlBase = new URL(repostiorio);
				HttpURLConnection urlConn = (HttpURLConnection) urlBase.openConnection();
				
				if (urlConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
					
					String conteudo = ""; // String para o conteudo
					InputStream entrada = urlConn.getInputStream(); // Criamos o arquivo de
					// entrada
					int caracter; // para ler carater/carater
					
					SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
					
					parser.parse(entrada, new JColtraneXMLHandler(decodificador));
					
					
					
				}
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
			
			List<MTDDocument> lista = decodificador.getDocumentos();
			for (MTDDocument documentMTD : lista) {
				Document doc = documentMTD.toDocument();
				
				List<IndexableField> campos = doc.getFields();
				
				System.out.println("------------------------------------------");
				for (IndexableField field : campos) {
					System.out.println(field.stringValue());
				}
			}
		
		
		
		
	}
	
	static void  teste(){
//		String str = "<?xml version=1.0 encoding=UTF-8?><?xml-stylesheet type=text/xsl href=oainav3.php ?><OAI-PMH xmlns=http://www.openarchives.org/OAI/2.0/xmlns:xsi=http://www.w3.org/2001/XMLSchema-instancexsi:schemaLocation=http://www.openarchives.org/OAI/2.0/http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd><responseDate>2014-01-13T14:32:33Z</responseDate><request verb=GetRecord metadataPrefix=mtd2-br identifier=oai:pucrs.br:530>http://tede.pucrs.br/tde_oai/oai2.php</request><GetRecord><record><header><identifier>oai:pucrs.br:530</identifier><datestamp>2007-04-26T09:48:02Z</datestamp></header><metadata><mtd2-br:mtd2br xmlns:mtd2-br=http://oai.ibict.br/mtd2-br/  xmlns:xsi=http://www.w3.org/2001/XMLSchema-instance  xsi:schemaLocation=http://oai.ibict.br/mtd2-br/                             http://oai.ibict.br/mtd2-br/mtd2-br.xsd ><mtd2-br:Controle><mtd2-br:Sigla>PUCRS/Tede</mtd2-br:Sigla><mtd2-br:DataAtualizacao>2007-04-26T09:48:02Z</mtd2-br:DataAtualizacao><mtd2-br:IdentificacaoDocumento>530</mtd2-br:IdentificacaoDocumento><mtd2-br:Tipo>Tese ou Disserta√ß√£o Eletr√¥nica</mtd2-br:Tipo></mtd2-br:Controle><mtd2-br:BibliotecaDigital><mtd2-br:Nome>Biblioteca Digital de Teses e Disserta√ß√µes da PUCRS</mtd2-br:Nome><mtd2-br:Sigla>BDTD-PUCRS</mtd2-br:Sigla><mtd2-br:URL>http://tede.pucrs.br</mtd2-br:URL><mtd2-br:ProvedorServico><mtd2-br:Nome>Pontif√≠cia Universidade Cat√≥lica do Rio Grande do Sul</mtd2-br:Nome><mtd2-br:Sigla>PUCRS</mtd2-br:Sigla><mtd2-br:Pais>BR</mtd2-br:Pais><mtd2-br:UF>RS</mtd2-br:UF><mtd2-br:URL>http://www.pucrs.br/</mtd2-br:URL></mtd2-br:ProvedorServico></mtd2-br:BibliotecaDigital><mtd2-br:BibliotecaDepositaria><mtd2-br:Nome>Biblioteca Central Irm√£o Jos√© Ot√£o</mtd2-br:Nome><mtd2-br:Sigla>BCE/PUCRS</mtd2-br:Sigla><mtd2-br:URL>http://www.pucrs.br/biblioteca/</mtd2-br:URL><mtd2-br:NumeroChamada>T 193 H465Ve</mtd2-br:NumeroChamada></mtd2-br:BibliotecaDepositaria><mtd2-br:Titulo Idioma=pt >A deca√≠da em ser e tempo : explicita√ß√£o de um existencial esquecido</mtd2-br:Titulo><mtd2-br:Arquivo><mtd2-br:URL Formato=PDF >http://tede.pucrs.br/tde_busca/arquivo.php?codArquivo=604</mtd2-br:URL><mtd2-br:Legenda Idioma=pt >Acessar documento</mtd2-br:Legenda><mtd2-br:NivelAcesso>Publico</mtd2-br:NivelAcesso></mtd2-br:Arquivo><mtd2-br:Idioma>pt</mtd2-br:Idioma><mtd2-br:Grau>Doutor</mtd2-br:Grau><mtd2-br:Titulacao>Doutor em Filosofia</mtd2-br:Titulacao><mtd2-br:Resumo Idioma=pt >A presente tese investiga a constitui√ß√£o do ser humano, o Dasein, na forma de um dos seus existenciais: a deca√≠da. A partir de uma leitura de Ser e tempo, onde se destacam os existenciais compreens√£o, sentimento de situa√ß√£o e cuidado, busca-se explicitar a deca√≠da atrav√©s de dois eixos condutores: o cotidiano tematizado e o encobrimento do ser. O cotidiano tematizado assume a forma da cotidianidade que √© tratada em Ser e tempo. O encobrimento do ser √© destacado pela contribui√ß√£o de uma leitura da confer√™ncia O que metaf√≠sica? feita em 1929, juntamente com a sua introdu√ß√£o (1949) e posf√°cio (1943). Desta forma, o encobrimento do ser atinge um grau ampliado, al√©m da dimens√£o da anal√≠tica do Dasein. A partir desta compreens√£o do encobrimento, torna-se poss√≠vel especificar o eixo condutor pela tem√°tica do conhecimento, utilizando-se alguns constructos das teorias do conhecimento. A tese trabalha com os existenciais compreens√£o e sentimento de situa√ß√£o e tamb√©m na maneira como se insere o recurso metodol√≥gico de Heidegger sob a forma dos ind√≠cios-formais. A utiliza√ß√£o dos ind√≠ciosformais marca uma diferen√ßa entre Heidegger e a tradi√ß√£o e determina a possibilidade de manter a vida f√°tica sob o foco de investiga√ß√£o. A tematiza√ß√£o do cotidiano percorre toda a an√°lise, principalmente quando esta se remete √† pr√≥pria obra Ser e tempo. Assim, o elemento da tematiza√ß√£o do cotidiano se mostra como fator discriminador entre uma interpreta√ß√£o baseada na anal√≠tica existencial e uma interpreta√ß√£o encobridora baseada na metaf√≠sica. A tese analisa os existenciais e as principais condi√ß√µes do Dasein entre elas o ser-em e o ser-no-mundo, mostrando atrav√©s deles o cotidiano tematizado e o encobrimento, que podem revelar um alcance na compreens√£o do existencial deca√≠da. A tese demonstra que os elementos contidos na deca√≠da podem ser utilizados para realizar uma an√°lise da tradi√ß√£o filos√≥fica, na medida em que esta se mostra encobridora do ser. A tradi√ß√£o filos√≥fica, tomada na forma de constructos da teoria do conhecimento, √© confrontada com o trabalho dos existenciais, principalmente, dos existenciais compreens√£o e sentimento de situa√ß√£o. O trabalho de an√°lise investiga a forma do ser-a√≠ fugir diante do seu estar-arrojado ao mundo. Esta fuga, tematizada pela deca√≠da, determina um encobrimento do car√°ter de ser deste ente. Este encobrimento pode ser mostrado atrav√©s do modo de ser do conhecer. O modo de ser do conhecer √© um modo do ser-em, esta situa√ß√£o do conhecer √© obtida atrav√©s de uma leitura da obra Proleg√¥menos para a hist√≥ria do conceito de tempo. Nesta obra, Heidegger afirma que o conhecer √© um modo de ser do ser-em. Este modo de ser do ser-em n√£o √©, um modo de ser fundamental do ser-no-mundo. Este elemento do ser-em abre a possibilidade da pesquisa do conhecimento ser inserida dentro da leitura de Ser e tempo, na forma do existencial deca√≠da. Assim se alcan√ßa uma explicita√ß√£o da deca√≠da, mostrando o quanto este existencial ainda pode ser explorado</mtd2-br:Resumo><mtd2-br:Assunto Idioma=pt  Esquema=Palavra-chave >FILOSOFIA ALEM√?</mtd2-br:Assunto><mtd2-br:Assunto Idioma=pt  Esquema=Palavra-chave > SER E TEMPO - CR√?TICA E INTERPRETA√?√?O</mtd2-br:Assunto><mtd2-br:Assunto Idioma=pt  Esquema=Palavra-chave > ONTOLOGIA</mtd2-br:Assunto><mtd2-br:Assunto Idioma=pt  Esquema=Palavra-chave > HEIDEGGER, MARTIN - CR√?TICA E INTERPRETA√?√?O</mtd2-br:Assunto><mtd2-br:Assunto Idioma=pt  Esquema=Palavra-chave > METAF√?SICA</mtd2-br:Assunto><mtd2-br:Assunto Idioma=pt  Esquema=Tabela CNPQ >FILOSOFIA</mtd2-br:Assunto><mtd2-br:LocalDefesa><mtd2-br:Cidade>Porto Alegre</mtd2-br:Cidade><mtd2-br:UF>RS</mtd2-br:UF><mtd2-br:Pais>BR</mtd2-br:Pais></mtd2-br:LocalDefesa><mtd2-br:DataDefesa>2007-03-27</mtd2-br:DataDefesa><mtd2-br:Autor><mtd2-br:Nome>Itamar Soares Veiga</mtd2-br:Nome><mtd2-br:Lattes>http://buscatextual.cnpq.br/buscatextual/visualizacv.jsp?id=K4776191T1</mtd2-br:Lattes></mtd2-br:Autor><mtd2-br:Contribuidor Papel=Orientador ><mtd2-br:Nome>Ernildo Jacob Stein</mtd2-br:Nome><mtd2-br:Lattes>http://buscatextual.cnpq.br/buscatextual/visualizacv.jsp?id=K4783928Z7</mtd2-br:Lattes></mtd2-br:Contribuidor><mtd2-br:InstituicaoDefesa><mtd2-br:Nome>Pontif√≠cia Universidade Cat√≥lica do Rio Grande do Sul</mtd2-br:Nome><mtd2-br:Sigla>PUCRS</mtd2-br:Sigla><mtd2-br:Pais>BR</mtd2-br:Pais><mtd2-br:UF>RS</mtd2-br:UF><mtd2-br:URL>http://www.pucrs.br/</mtd2-br:URL><mtd2-br:Programa><mtd2-br:Nome>Programa de P√≥s-Gradua√ß√£o em Filosofia</mtd2-br:Nome><mtd2-br:Area>FILOSOFIA</mtd2-br:Area></mtd2-br:Programa></mtd2-br:InstituicaoDefesa><mtd2-br:Direitos Idioma=pt >Liberar o conte√∫do dos arquivos para acesso p√∫blico</mtd2-br:Direitos></mtd2-br:mtd2br></metadata></record></GetRecord></OAI-PMH>";
//		String str = "DistribuiÁ„o temporal, atividade reprodutiva e vocalizaÁıes em uma assembleia de anfÌbios anuros de uma floresta ombrÛfila mista em Santa Catarina, sul do Brasil";
		String str = "No perÌodo de abril de 2005 a novembro de 2006 foram estudadas a distribuiÁ„o temporal, a partilha do habitat, a reproduÁ„o e a atividade vocal em uma assembleia de anfÌbios anuros na Fazenda Serra da EsperanÁa, municÌpio de Lebon RÈgis, Estado de Santa Catarina. Os objetivos do trabalho foram verificar a import‚ncia da pluviosidade e da temperatura na distribuiÁ„o temporal das espÈcies na assembleia, analisar a ocupaÁ„o do habitat, realizar a an·lise ac˙stica do repertÛrio vocal das espÈcies e testar a influÍncia da temperatura do ar e do tamanho e massa dos machos vocalizantes sobre os par‚metros ac˙sticos. Foram encontradas 32 espÈcies na ·rea de estudo, a maior riqueza de anfÌbios registrada para o Estado. A taxonomia de pelo menos sete dessas espÈcies È incerta, podendo tratar-se de t·xons ainda n„o descritos na literatura. A temperatura apresentou uma forte influÍncia na distribuiÁ„o temporal das espÈcies. O n˙mero de espÈcies em atividade de vocalizaÁ„o e reproduÁ„o foi relacionado ‡s variaÁıes da temperatura mensal mÈdia, mÌnima e m·xima, significando que nos meses mais quentes foram encontradas mais espÈcies em atividade de vocalizaÁ„o e reproduÁ„o. Foi documentada atividade reprodutiva em 14 espÈcies e um total de nove modos reprodutivos na assembleia. A comparaÁ„o das vocalizaÁıes de 23 espÈcies da assembleia com descriÁıes de vocalizaÁıes de outras assembleias indicou diferenÁas que sugerem a existÍncia de espÈcies ainda n„o descritas na ·rea de estudo. TambÈm foram documentadas variaÁıes intraespecÌficas nos cantos em decorrÍncia do tamanho e massa dos machos cantores e em funÁ„o da temperatura do ar. Encontraram-se influÍncias da massa e tamanho do macho cantor na frequÍncia dominante do canto de an˙ncio, e tambÈm da temperatura do ar na duraÁ„o das notas. A riqueza de espÈcies da assembleia apresentou forte semelhanÁa biogeogr·fica com ·reas de Floresta OmbrÛfila Mista dos Estados de Santa Catarina, Paran· e Rio Grande do Sul. A presenÁa de possÌveis novas espÈcies e da espÈcie Pleurodema bibroni, classificada na categoria quase ameaÁada, salienta a import‚ncia da conservaÁ„o deste bioma altamente degradado e demonstra a nossa carÍncia de conhecimento acerca da anurofauna catarinense.";
		
		SortedMap<String, Charset> mapa = Charset.availableCharsets();
		
		Collection<Charset> lista = mapa.values();
		
		for (Charset charset : lista) {
//			if(charset.toString().equals("UTF-8")){
			ByteBuffer bb = ByteBuffer.wrap(str.getBytes());
			System.out.println(charset+" - "+charset.decode(bb));
//			}
			
		}
		
	}
	
}
