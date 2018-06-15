package br.ufpe.mtd.util.analizers;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
//import org.apache.lucene.analysis.tokenattributes.TermAttribute; //lucene 2.9.0
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

import br.ufpe.mtd.util.enumerado.MTDArquivoEnum;

/**
 * 
 * @author djalma
 *
 */
public final class SNAnalyser extends Analyzer {
	private static HashSet<String> stopWords = null;
	Tokenizer tokenizador;
	
	public void setReader(Reader input) throws IOException{
		tokenizador.setReader(input);
	}
	
	public SNAnalyser(String stopFile) throws IOException{
		init(stopFile);
	}
	
	public SNAnalyser(HashSet stopWords) throws IOException{
		carregarStopWords(stopWords);
	}
	


	private static synchronized void carregarStopWords(HashSet stopWords){
		if(SNAnalyser.stopWords == null){
			SNAnalyser.stopWords = stopWords;
			//include puctuations as stopwords
			String punc = "-'`.!?;/\\\"[]<>{}&()";//remain , : ()
			for(int i=0;i<punc.length();i++){
				SNAnalyser.stopWords.add(punc.substring(i,i+1));
			}
		}
	}

	private static synchronized void init(String stopFile) throws IOException{
		//Load stopwords
		if(stopWords == null){
			stopWords = new HashSet<String>();
			BufferedReader input = new BufferedReader(new InputStreamReader(new FileInputStream(stopFile)));
			String line = input.readLine();
			while(line!=null){
				stopWords.add(line);
				line = input.readLine();
			}
			input.close();
			//include puctuations as stopwords
			String punc = "-'`.!?;/\\\"[]<>{}&()=+%";//remain , : ()
			for(int i=0;i<punc.length();i++){
				stopWords.add(punc.substring(i,i+1));
			}
		}
	}
	
	public static boolean isStopword(String s){
		s=s.toLowerCase();
		if(s.length()<3)//siglas com menos de 3 caracteres
			return true;
		if(s.split("( |-)").length < 2)//palavras isoladas
			return true;
		if(stopWords != null){//sn stopwords
			return stopWords.contains(s);
			
		}else{
			System.out.println("Warnning SNAnalyser.isStopword(): stopwords not loaded.");
			return false;
		}
			
		
	}
	
//	@Override  //lucene 2.9.0
//	public TokenStream tokenStream(String fieldName, Reader reader) {
//		TokenStream stream = null;
//		stream = new SNTokenizerWithAtributes(reader, fieldName);//***
//		return stream;
//	}
	
	
//	@Override  //lucene 2.9.0
//	public TokenStream reusableTokenStream(String fieldName, Reader reader)
//			throws IOException {
//		Tokenizer tokenizer = (Tokenizer) getPreviousTokenStream();
//		if (tokenizer == null) {
//			tokenizer = new SNTokenizerWithAtributes(reader,"context");//***
//			setPreviousTokenStream(tokenizer);
//		} else
//			tokenizer.reset(reader);
//		return tokenizer;
//	}
	
	public static void displayTokens(Analyzer analyzer,
			String text) throws IOException {
			displayTokens(analyzer.tokenStream("contents", new StringReader(text)));
			}
	
	public static void displayTokens(TokenStream stream)
			throws IOException {
			//TermAttribute term = (TermAttribute) stream.addAttribute(TermAttribute.class); //lucene 2.9.0
		CharTermAttribute term = (CharTermAttribute) stream.addAttribute(CharTermAttribute.class);	
		while(stream.incrementToken()) {
			//System.out.print("[" + term.term() + "] "); //lucene 2.9.0
			System.out.println("[" + term.toString() + "] ");
			}
	}
	
	public static void displayTokensWithFullDetails(Analyzer analyzer,String text)
					throws IOException {
		
		StringReader rt= new StringReader(text);
		TokenStream stream = analyzer.tokenStream("contents",rt);
		stream.reset();
		
//		TermAttribute term = (TermAttribute) stream.addAttribute(TermAttribute.class);
		CharTermAttribute term = (CharTermAttribute) stream.addAttribute(CharTermAttribute.class);
		PositionIncrementAttribute posIncr = (PositionIncrementAttribute) stream.addAttribute(PositionIncrementAttribute.class);
		OffsetAttribute offset = (OffsetAttribute) stream.addAttribute(OffsetAttribute.class);
		TypeAttribute type = (TypeAttribute) stream.addAttribute(TypeAttribute.class);
		int position = 0;
		while(stream.incrementToken()) {
			int increment = posIncr.getPositionIncrement();
			if (increment > 0) {
				position = position + increment;
				System.out.println();
				System.out.print(position + ": ");
			}
			System.out.print("[" +
					//term.term() + ":" +
					term.toString() + ":" +
					offset.startOffset() + "->" +
					offset.endOffset() + ":" +
					type.type() + "] ");
		}
		System.out.println();
		stream.end();
		stream.close();
		
	}
	
	public static List<String> extrairSintagmasNominais(Analyzer analyzer,String text)
			throws Exception {
		//SNTokenizer.setTagger("Cogroo");
		SNTokenizer.setTagger("TreeTagger");
		List<String> palavras = new ArrayList<String>();
		StringReader rt= new StringReader(text);
		TokenStream stream = ((SNAnalyser)analyzer).tokenStream("contents",rt);
		try {
			stream.reset();
			CharTermAttribute term = (CharTermAttribute) stream.addAttribute(CharTermAttribute.class);
			while(stream.incrementToken()) {
				palavras.add(term.toString());
			}
			
		} catch (Exception e) {
			throw e;
		}finally{
			
			if(stream!= null){
				stream.end();
				stream.close();
				rt.close();
			}
		}
		
		
		return palavras;
	}
	
	@Override
	protected TokenStreamComponents createComponents(String arg0, Reader arg1) {
		// TODO Auto-generated method stub
		tokenizador = new SNTokenizerWithAtributes(arg1,arg0);//SNTokenizer(arg1,arg0);//n�o elimina sns em stoplist
			return new TokenStreamComponents(tokenizador);
		
	}
	
	public static void main(String[] args) throws Exception {
		String sent = "O novo c�lculo das aposentadorias resulta em valores menores do que os atuais para quem perde o benef�cio com menos tempo de contribui��o e idade.";
		sent = "Os resultados mostraram uma preval�ncia de 49,18% da infec��o.";
		sent = "Esta disserta��o tem por objeto o processo de contrata��o de pequenas empresas de consultoria, instrumentos s�o considerados secund�rios para a identifica��o de esp�cies, refletido num amplo espectro de bi�tipos florais, as esp�cies de pilosocereus aqui estudadas, 04 filos, sintetizada pela categoria �mica corpo forte.";
		sent= "Este trabalho pretende contribuir para a compreens�o da legitimidade ministerial na defesa dos direitos individuais homog�neos, propondo uma concep��o ampliada a todas as situa��es em que a defesa mediata dos direitos individuais homog�neos represente a defesa imediata do Estado democr�tico de direito, papel institucional prec�puo do Minist�rio P�blico, vez que somente pela efetiva��o dos direitos fundamentais alcan�ar-se-� a democracia substancial. Contextualiza-se o problema no tema do acesso � Justi�a; define-se o papel do Minist�rio P�blico na Constitui��o brasileira de 1988; perfila-se a defesa coletiva dos individuais homog�neos, tangenciando aspectos da racionalidade do processo coletivo, do C�digo Modelo de Processo Coletivo Iberoamericano, do hist�rico do processo coletivo no Brasil. Estuda-se a legitima��o ordin�ria, a extraordin�ria e a substitui��o processual. Investiga-se a natureza jur�dica da legitima��o do �rg�o-Agente sob as perspectivas gen�rica e espec�fica, perpassando-se pela conceitua��o corrente dos individuais homog�neos e pela proposta de compreens�o deste ensaio, � luz da bidimensionalidade dos direitos fundamentais. Verifica-se a a��o civil p�blica com instrumento para a tutela coletiva dos individuais homog�neos. Busca-se a supera��o da seara consumerista e das demais �reas de interpola��o legislativa, norte na relev�ncia social como crit�rio de sindicabilidade. Exemplifica-se as estirpes tribut�ria, previdenci�ria e das omiss�es administrativas como zonas de plena densidade da dignidade da pessoa humana e, por isso, suscet�veis da atua��o ministerial leg�tima. Explicita-se a ratio essendi da legitima��o na seara consumerista como fonte de reverbera��o. Adentra-se no controle jurisdicional da legitimidade ativa do Minist�rio P�blico para a defesa mediata dos individuais homog�neos. Aborda-se t�picos de direitos comparado. Ingressa-se numa leitura filos�fica da legitima��o, tendo por referencial a escola hegeliana.";
		sent="O De regno � um op�sculo, inacabado, que versa sobre o tema da monarquia. A import�ncia desse op�sculo � devida ao fato da entrada da Pol�tica de Arist�teles no meio intelectual do Ocidente latino. Por isso, nele, o pensamento pol�tico de Tom�s de Aquino segue a via segura das teorias pol�ticas de Arist�teles. No entanto, o objetivo do Aquinate, ao retomar as teorias do Estagirita, � solucionar os seus pr�prios problemas filos�fico-pol�ticos. A partir disso, apresenta-se o problema da autoridade no De regno, a qual, no governo de uma cidade ou reino, tem a finalidade de conservar a paz e agir de acordo com o bem comum. Apresenta-se, em seguida, a retomada da divis�o cl�ssica dos regimes de governo. Mesmo que a monarquia, dentre as v�rias formas de governo, seja o melhor regime, Tom�s manifesta a sua prefer�ncia pelo regime misto, para evitar o abuso de poder. Por fim, exp�e-se o problema da tirania. Acerca desse problema, a argumenta��o de Tom�s ocupa a maior parte do escrito. O seu intento � comparar as desvantagens da tirania com os benef�cios da realeza. Em suma, mesmo retomando a via segura da Pol�tica de Arist�teles, n�o h� como negar que o pensamento pol�tico de Tom�s, no De regno, � pr�prio de um te�logo e crist�o do s�culo XIII.";
		String stopFile = MTDArquivoEnum.PASTA_ARQUIVOS_AUXILIARES.getArquivo().getAbsolutePath()+"\\JOgma\\sn_stoplist.txt";
		SNAnalyser contextAnalyzer = new SNAnalyser(stopFile);
		
		
		//set o Tagger a ser usado por Tokenizer
		//SNTokenizer.setTagger("Cogroo");
		SNTokenizer.setTagger("Treetagger");
		
		String [] res = new String[7];
		//ids dos resumos 0 5 1 3 4 2 6
        res[0] = sent;
        res[1] = new String("Relato da experi�ncia do Impa na informatiza��o de sua biblioteca, utilizando o software Horizon, e na constru��o de um servidor de preprints (disserta��es de mestrado, teses de doutorado e artigos ainda n�o publicados) atrav�s da participa��o no projeto internacional Math-Net.");
        res[2]= new String("Apresenta de forma introdut�ria quest�es e conceitos fundamentais sobre metadados e a estrutura��o da descri��o padronizada de documentos eletr�nicos. Discorre sobre os elementos propostos no Dublin Core e comenta os projetos de cataloga��o dos recursos da Internet, CATRIONA, InterCat e CALCO.");
        res[3] = new String("Apresenta a implanta��o de recursos multim�dia e interface Web no banco de dados desenvolvido para a cole��o de v�deos da Videoteca Multimeios, pertencente ao Departamento de Multimeios do Instituto de Artes da UNICAMP. Localiza a discuss�o conceitual no universo das bibliotecas digitais e prop�e altera��es na configura��o atual de seu banco de dados.");
        res[4] = new String("Este artigo aborda a necessidade de ado��o de padr�es de descri��o de recursos de informa��o eletr�nica, particularmente, no �mbito da Embrapa Inform�tica Agropecu�ria. O Rural M�dia foi desenvolvido utilizando o modelo Dublin Core (DC) para descri��o de seu acervo, acrescido de pequenas adapta��es introduzidas diante da necessidade de adequar-se a especificidades meramente institucionais. Este modelo de metadados baseado no Dublin Core, adaptado para o Banco de Imagem, possui caracter�sticas que endossam a sua ado��o, como a simplicidade na descri��o dos recursos, entendimento sem�ntico universal (dos elementos), escopo internacional e extensibilidade (o que permite sua adapta��o as necessidades adicionais de descri��o).");
        res[5] = new String("Bibliografia internacional seletiva e anotada sobre bibliotecas digitais. Aborda os seguintes aspectos: a) Vision�rios, principais autores que escreveram sobre a biblioteca do futuro, no per�odo de 1945-1985; b) conceitua��o de biblioteca digital; c) projetos em andamento na Alemanha, Austr�lia, Brasil, Canad�, Dinamarca, Espanha, Estados Unidos, Franca, Holanda, Jap�o, Nova Zel�ndia, Reino Unido, Su�cia e Vaticano; d) aspectos t�cnicos relativos a constru��o de uma biblioteca digital: arquitetura do sistema, convers�o de dados e escaneamento, marca��o de textos, desenvolvimento de cole��es, cataloga��o, classifica��o/indexa��o, metadados, referencia, recupera��o da informa��o, direitos autorais e preserva��o da informa��o digital; e) principais fontes de reuni�es t�cnicas especificas, lista de discuss�o, grupos e centros de estudos, cursos e treinamento.");
		res[6] = new String("Descreve as op��es tecnol�gicas e metodol�gicas para atingir a interoperabilidade no acesso a recursos informacionais eletr�nicos, dispon�veis na Internet, no �mbito do projeto da Biblioteca Digital Brasileira em Ci�ncia e Tecnologia, desenvolvido pelo Instituto Brasileiro de Informa��o em Ci�ncia e Tecnologia(IBCT). Destaca o impacto da Internet sobre as formas de publica��o e comunica��o em C&T e sobre os sistemas de informa��o e bibliotecas. S�o explicitados os objetivos do projeto da BDB de fomentar mecanismos de publica��o pela comunidade brasileira de C&T, de textos completos diretamente na Internet, sob a forma de teses, artigos de peri�dicos, trabalhos em congressos, literatura \"cinzenta\",ampliando sua visibilidade e acessibilidade nacional e internacional, e tamb�m de possibilitar a interoperabilidade entre estes recursos informacionais brasileiros em C&T, heterog�neos e distribu�dos, atrav�s de acesso unificado via um portal, sem a necessidade de o usu�rio navegar e consultar cada recurso individualmente.");
		for(int i=0; i < res.length;i++){//res.length
//			StringReader sr = new StringReader(res[i]);
//			displayTokensWithFullDetails(new SNAnalyser(stopFile),res[i]);
			
			List<String> palavras = extrairSintagmasNominais(new SNAnalyser(stopFile),res[i]);
			System.out.println(palavras);
			
		}
		
		contextAnalyzer.close();
		
		//Faz quase o mesmo da fun��o acima, imprimindo somente as tokens
//		TokenStream tokens = contextAnalyzer.tokenStream("context",reader);
////		TermAttribute termAtt = (TermAttribute) tokens.addAttribute(TermAttribute.class);
//		CharTermAttribute termAtt = (CharTermAttribute) tokens.addAttribute(CharTermAttribute.class);
//		tokens.reset();
//		// print all tokens until stream is exhausted
//		while (tokens.incrementToken()) {
////			System.out.println("token:"+termAtt.term());
//			System.out.println("token:"+termAtt.toString());
//		}
//		tokens.end();
//		tokens.close();

//				
	}
}
