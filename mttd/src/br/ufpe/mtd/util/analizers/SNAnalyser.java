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
			}
		}
		
		
		return palavras;
	}
	
	@Override
	protected TokenStreamComponents createComponents(String arg0, Reader arg1) {
		// TODO Auto-generated method stub
		tokenizador = new SNTokenizerWithAtributes(arg1,arg0);
			return new TokenStreamComponents(tokenizador);
		
	}
	
	public static void main(String[] args) throws Exception {
		String sent = "O novo cálculo das aposentadorias resulta em valores menores do que os atuais para quem perde o benefício com menos tempo de contribuição e idade.";
		sent = "Os resultados mostraram uma prevalência de 49,18% da infecção.";
		sent = "Esta dissertação tem por objeto o processo de contratação de pequenas empresas de consultoria, instrumentos são considerados secundários para a identificação de espécies, refletido num amplo espectro de biótipos florais, as espécies de pilosocereus aqui estudadas, 04 filos, sintetizada pela categoria êmica corpo forte.";
		sent= "Este trabalho pretende contribuir para a compreensão da legitimidade ministerial na defesa dos direitos individuais homogêneos, propondo uma concepção ampliada a todas as situações em que a defesa mediata dos direitos individuais homogêneos represente a defesa imediata do Estado democrático de direito, papel institucional precípuo do Ministério Público, vez que somente pela efetivação dos direitos fundamentais alcançar-se-á a democracia substancial. Contextualiza-se o problema no tema do acesso à Justiça; define-se o papel do Ministério Público na Constituição brasileira de 1988; perfila-se a defesa coletiva dos individuais homogêneos, tangenciando aspectos da racionalidade do processo coletivo, do Código Modelo de Processo Coletivo Iberoamericano, do histórico do processo coletivo no Brasil. Estuda-se a legitimação ordinária, a extraordinária e a substituição processual. Investiga-se a natureza jurídica da legitimação do Órgão-Agente sob as perspectivas genérica e específica, perpassando-se pela conceituação corrente dos individuais homogêneos e pela proposta de compreensão deste ensaio, à luz da bidimensionalidade dos direitos fundamentais. Verifica-se a ação civil pública com instrumento para a tutela coletiva dos individuais homogêneos. Busca-se a superação da seara consumerista e das demais áreas de interpolação legislativa, norte na relevância social como critério de sindicabilidade. Exemplifica-se as estirpes tributária, previdenciária e das omissões administrativas como zonas de plena densidade da dignidade da pessoa humana e, por isso, suscetíveis da atuação ministerial legítima. Explicita-se a ratio essendi da legitimação na seara consumerista como fonte de reverberação. Adentra-se no controle jurisdicional da legitimidade ativa do Ministério Público para a defesa mediata dos individuais homogêneos. Aborda-se tópicos de direitos comparado. Ingressa-se numa leitura filosófica da legitimação, tendo por referencial a escola hegeliana.";
		sent="O De regno é um opúsculo, inacabado, que versa sobre o tema da monarquia. A importância desse opúsculo é devida ao fato da entrada da Política de Aristóteles no meio intelectual do Ocidente latino. Por isso, nele, o pensamento político de Tomás de Aquino segue a via segura das teorias políticas de Aristóteles. No entanto, o objetivo do Aquinate, ao retomar as teorias do Estagirita, é solucionar os seus próprios problemas filosófico-políticos. A partir disso, apresenta-se o problema da autoridade no De regno, a qual, no governo de uma cidade ou reino, tem a finalidade de conservar a paz e agir de acordo com o bem comum. Apresenta-se, em seguida, a retomada da divisão clássica dos regimes de governo. Mesmo que a monarquia, dentre as várias formas de governo, seja o melhor regime, Tomás manifesta a sua preferência pelo regime misto, para evitar o abuso de poder. Por fim, expõe-se o problema da tirania. Acerca desse problema, a argumentação de Tomás ocupa a maior parte do escrito. O seu intento é comparar as desvantagens da tirania com os benefícios da realeza. Em suma, mesmo retomando a via segura da Política de Aristóteles, não há como negar que o pensamento político de Tomás, no De regno, é próprio de um teólogo e cristão do século XIII.";
		String stopFile = MTDArquivoEnum.PASTA_ARQUIVOS_AUXILIARES.getArquivo().getAbsolutePath()+"\\JOgma\\sn_stoplist.txt";
		SNAnalyser contextAnalyzer = new SNAnalyser(stopFile);
		
		
		//set o Tagger a ser usado por Tokenizer
		//SNTokenizer.setTagger("Cogroo");
		SNTokenizer.setTagger("Treetagger");
		
		String [] res = new String[7];
		//ids dos resumos 0 5 1 3 4 2 6
        res[0] = sent;
        res[1] = new String("Relato da experiência do Impa na informatização de sua biblioteca, utilizando o software Horizon, e na construção de um servidor de preprints (dissertações de mestrado, teses de doutorado e artigos ainda não publicados) através da participação no projeto internacional Math-Net.");
        res[2]= new String("Apresenta de forma introdutória questões e conceitos fundamentais sobre metadados e a estruturação da descrição padronizada de documentos eletrônicos. Discorre sobre os elementos propostos no Dublin Core e comenta os projetos de catalogação dos recursos da Internet, CATRIONA, InterCat e CALCO.");
        res[3] = new String("Apresenta a implantação de recursos multimídia e interface Web no banco de dados desenvolvido para a coleção de vídeos da Videoteca Multimeios, pertencente ao Departamento de Multimeios do Instituto de Artes da UNICAMP. Localiza a discussão conceitual no universo das bibliotecas digitais e propõe alterações na configuração atual de seu banco de dados.");
        res[4] = new String("Este artigo aborda a necessidade de adoção de padrões de descrição de recursos de informação eletrônica, particularmente, no âmbito da Embrapa Informática Agropecuária. O Rural Mídia foi desenvolvido utilizando o modelo Dublin Core (DC) para descrição de seu acervo, acrescido de pequenas adaptações introduzidas diante da necessidade de adequar-se a especificidades meramente institucionais. Este modelo de metadados baseado no Dublin Core, adaptado para o Banco de Imagem, possui características que endossam a sua adoção, como a simplicidade na descrição dos recursos, entendimento semântico universal (dos elementos), escopo internacional e extensibilidade (o que permite sua adaptação as necessidades adicionais de descrição).");
        res[5] = new String("Bibliografia internacional seletiva e anotada sobre bibliotecas digitais. Aborda os seguintes aspectos: a) Visionários, principais autores que escreveram sobre a biblioteca do futuro, no período de 1945-1985; b) conceituação de biblioteca digital; c) projetos em andamento na Alemanha, Austrália, Brasil, Canadá, Dinamarca, Espanha, Estados Unidos, Franca, Holanda, Japão, Nova Zelândia, Reino Unido, Suécia e Vaticano; d) aspectos técnicos relativos a construção de uma biblioteca digital: arquitetura do sistema, conversão de dados e escaneamento, marcação de textos, desenvolvimento de coleções, catalogação, classificação/indexação, metadados, referencia, recuperação da informação, direitos autorais e preservação da informação digital; e) principais fontes de reuniões técnicas especificas, lista de discussão, grupos e centros de estudos, cursos e treinamento.");
		res[6] = new String("Descreve as opções tecnológicas e metodológicas para atingir a interoperabilidade no acesso a recursos informacionais eletrônicos, disponíveis na Internet, no âmbito do projeto da Biblioteca Digital Brasileira em Ciência e Tecnologia, desenvolvido pelo Instituto Brasileiro de Informação em Ciência e Tecnologia(IBCT). Destaca o impacto da Internet sobre as formas de publicação e comunicação em C&T e sobre os sistemas de informação e bibliotecas. São explicitados os objetivos do projeto da BDB de fomentar mecanismos de publicação pela comunidade brasileira de C&T, de textos completos diretamente na Internet, sob a forma de teses, artigos de periódicos, trabalhos em congressos, literatura \"cinzenta\",ampliando sua visibilidade e acessibilidade nacional e internacional, e também de possibilitar a interoperabilidade entre estes recursos informacionais brasileiros em C&T, heterogêneos e distribuídos, através de acesso unificado via um portal, sem a necessidade de o usuário navegar e consultar cada recurso individualmente.");
		for(int i=0; i < 1;i++){//res.length
//			StringReader sr = new StringReader(res[i]);
//			displayTokensWithFullDetails(new SNAnalyser(stopFile),res[i]);
			
			List<String> palavras = extrairSintagmasNominais(new SNAnalyser(stopFile),res[i]);
			System.out.println(palavras);
			
		}
		
		contextAnalyzer.close();
		
		//Faz quase o mesmo da função acima, imprimindo somente as tokens
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
