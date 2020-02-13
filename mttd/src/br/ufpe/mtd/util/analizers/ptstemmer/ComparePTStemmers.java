package br.ufpe.mtd.util.analizers.ptstemmer;
import java.util.Scanner;

import br.ufpe.mtd.util.analizers.ptstemmer.*;
import br.ufpe.mtd.util.analizers.ptstemmer.Stemmer.StemmerType;
import br.ufpe.mtd.util.analizers.ptstemmer.exceptions.PTStemmerException;
import br.ufpe.mtd.util.analizers.ptstemmer.implementations.*;
import br.ufpe.mtd.util.analizers.ptstemmer.support.PTStemmerUtilities;

public class ComparePTStemmers {

	public static void example1(){
		//Simple
		Stemmer stemmer;
		try {
			stemmer = new OrengoStemmer();
			System.out.println(stemmer.getWordStem("sintagmas"));
		} catch (PTStemmerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static void example2() throws PTStemmerException {
		/*Melhores stemmers para RI: Savoy, TRUNC5, Porter, RSLP-S, RSLP
		 * ORENGO e RSLP são o mesmo
		 * Orengo Stemmer as defined in:
V. Orengo and C. Huyck, "A stemming algorithm for the portuguese language," String Processing and Information Retrieval, 2001. SPIRE 2001. Proceedings.Eighth International Symposium on, 2001, pp. 186-193.
Added extra stemming rules and exceptions found in:
http://www.inf.ufrgs.br/~arcoelho/rslp/integrando_rslp.html
		 * Porter Stemmer as defined in:
http://snowball.tartarus.org/algorithms/portuguese/stemmer.html
		 * Savoy Stemmer as defined in:
J. Savoy, "Light stemming approaches for the French, Portuguese, German and Hungarian languages," Proceedings of the 2006 ACM symposium on Applied computing, Dijon, France: ACM, 2006, pp. 1031-1035
Implementation based on:
http://members.unine.ch/jacques.savoy/clef/index.html
		 * Stemmer.StemmerType.ORENGO 
		 * Stemmer.StemmerType.PORTER
		 * Stemmer.StemmerType SAVOY
		 * */
		Stemmer stemmer = Stemmer.StemmerFactory(Stemmer.StemmerType.PORTER);
		System.out.println("=====PORTER======");
		//stemmer.enableCaching(1000);
		stemmer.ignore("a","e");
		System.out.println(stemmer.getWordStem("nominais"));
		String stem = stemmer.getWordStem("ciências");
		System.out.println(stem);
		System.out.println(PTStemmerUtilities.removeDiacritics(stem));
		stem = stemmer.getWordStem("canções"); //errado cançõ
		System.out.println(stem);
		stem = stemmer.getWordStem("canção"); //errado cançã
		System.out.println(stem);
		System.out.println(PTStemmerUtilities.removeDiacritics(stem));
		System.out.println(stemmer.getWordStem("da"));
		System.out.println(stemmer.getWordStem("naturais"));
		System.out.println(stemmer.getWordStem("natural"));
		System.out.println(stemmer.getWordStem("natureza"));
		System.out.println(stemmer.getWordStem("papel"));
		System.out.println(stemmer.getWordStem("papéis"));//ERRADO pap
		System.out.println(stemmer.getWordStem("mãe"));
		System.out.println(stemmer.getWordStem("mães"));//ERRADO mao
		System.out.println(stemmer.getWordStem("paizinhos"));
		System.out.println(stemmer.getWordStem("paizinho"));
		System.out.println(stemmer.getWordStem("pai"));//ERRADO pa
		System.out.println(stemmer.getWordStem("pais"));//ERRADO pa
		System.out.println(stemmer.getWordStem("país"));
		System.out.println(stemmer.getWordStem("países"));
		System.out.println(stemmer.getWordStem("quartil"));
		System.out.println(stemmer.getWordStem("quartis"));
		System.out.println(stemmer.getWordStem("filhinhos"));
		System.out.println(stemmer.getWordStem("filhos"));
		System.out.println(stemmer.getWordStem("engenharias"));
		System.out.println(stemmer.getWordStem("engenho"));
		System.out.println(stemmer.getWordStem("engenheiro"));
		System.out.println(stemmer.getWordStem("engenheiros"));

	}
	
	public static void example3() throws PTStemmerException{
		//Complete
		System.out.println("===============savoy=====================");
		//Stemmer stemmer = Stemmer.StemmerFactory(StemmerType.ORENGO);
		Stemmer stemmer = Stemmer.StemmerFactory(StemmerType.SAVOY);
		//Stemmer stemmer2 = Stemmer.StemmerFactory(StemmerType.PORTER);
		stemmer.enableCaching(1000);
		//stemmer.ignore(PTStemmerUtilities.fileToSet("data/stopwords.txt"));
		//stemmer.ignore(PTStemmerUtilities.fileToSet("data/namedEntities.txt"));         
		String stem = stemmer.getWordStem("ciências");
		System.out.println(stem);
		System.out.println(PTStemmerUtilities.removeDiacritics(stem));
		stem = stemmer.getWordStem("canções"); //errado cançã
		System.out.println(stem);
		stem = stemmer.getWordStem("canção"); //errado cançã
		System.out.println(stem);
		System.out.println(PTStemmerUtilities.removeDiacritics(stem));
		System.out.println(stemmer.getWordStem("da"));
		System.out.println(stemmer.getWordStem("naturais"));
		System.out.println(stemmer.getWordStem("natural"));
		System.out.println(stemmer.getWordStem("natureza"));
		System.out.println(stemmer.getWordStem("papel"));
		System.out.println(stemmer.getWordStem("papéis"));
		System.out.println(stemmer.getWordStem("mãe"));
		System.out.println(stemmer.getWordStem("mães"));//ERRADO mao
		System.out.println(stemmer.getWordStem("paizinhos"));
		System.out.println(stemmer.getWordStem("paizinho"));
		System.out.println(stemmer.getWordStem("pai"));
		System.out.println(stemmer.getWordStem("pais"));
		System.out.println(stemmer.getWordStem("país"));
		System.out.println(stemmer.getWordStem("países"));
		System.out.println(stemmer.getWordStem("quartil"));
		System.out.println(stemmer.getWordStem("quartis"));
		System.out.println(stemmer.getWordStem("filhinhos"));
		System.out.println(stemmer.getWordStem("filhos"));
		System.out.println(stemmer.getWordStem("engenharias"));
		System.out.println(stemmer.getWordStem("engenho"));
		System.out.println(stemmer.getWordStem("engenheiro"));
		System.out.println(stemmer.getWordStem("engenheiros"));

	}
	public static boolean matchstem(Stemmer stemmer, String singular, String plural){
		
		return (stemmer.getWordStem(singular).equalsIgnoreCase(stemmer.getWordStem(plural)));
	}
	
	public static boolean matchstemform(Stemmer stemmer, String singular, String plural){
		String stems = PTStemmerUtilities.removeDiacritics(stemmer.getWordStem(singular));
		String stemp = PTStemmerUtilities.removeDiacritics(stemmer.getWordStem(plural));
		return (stems.equalsIgnoreCase(stemp));
	}
	
    public static int matchstemerrors(Stemmer stemmer, String[] singular, String []plural){
		int errors = 0;
    	for(int i=0; i < singular.length; i++){
			if(!matchstemform(stemmer,singular[i],plural[i])){
				System.out.println("Stem Error: "+singular[i]+" "+plural[i]);
				errors++;
			}
		}
		return errors;
	}
    
    public static int teststemmer(Stemmer stemmer){
    	String[] singular = {"álcool","aprendiz","biblioteca","bibliotecário","canção","cão","campus","cartel","ciência","cientista","cupom","da","informação","gestor","natural","natureza","mãe","mão","oficial","país","papel","pesquisa","pesquisador","paizinho","pai","publicação","público","quartil","filho","filhinho","engenharia","engenho","engenheiro","réptil","réptil","social","têxtil","troféu","vilão","vilão"};
    	String[] plural = {"álcoois","aprendizes","bibliotecas","bibliotecários","canções","cães","campi","cartéis","ciências","cientistas","cupons","das","informações","gestores","naturais","naturezas","mães","mãos","oficiais","países","papéis","pesquisas","pesquisadores","paizinhos","pais","publicações","públicos","quartis","filhos","filhinhos","engenharias","engenhos","engenheiros","répteis","réptis","sociais","têxteis","troféus","vilãos","vilões"};

    	return matchstemerrors(stemmer,singular,plural);
    	
    }
    public static void teste() throws PTStemmerException{
    	Stemmer stemmer;
    	System.out.println("=========RSLP-S==========");
		stemmer = new RSLPSStemmer();
		System.out.println("Erros: "+teststemmer(stemmer));
		System.out.println("=========orengo ou RSLP==========");
		stemmer = new OrengoStemmer();
		System.out.println("Erros: "+teststemmer(stemmer));
		System.out.println("=========Savoy==========");
		stemmer = new SavoyStemmer();
		System.out.println("Erros: "+teststemmer(stemmer));
		System.out.println("=========TRUNC 5==========");
		stemmer = new TruncStemmer(5);
		System.out.println("Erros: "+teststemmer(stemmer));
		System.out.println("=========Porter==========");
		stemmer = new PorterStemmer();
		System.out.println("Erros: "+teststemmer(stemmer));
		
		
    }
	
	public static void main(String[] args) {
		
		example1();
		Stemmer stemmer;
		try {
			teste();
			example2();
			example3();
			System.out.println("=========orengo==========");
			stemmer = new OrengoStemmer();
			stemmer.enableCaching(1000);   //Optional
			//stemmer.ignore(PTStemmerUtilities.fileToSet("data/namedEntities.txt"));  //Optional
			String stem = stemmer.getWordStem("ciências");
			System.out.println(stem);
			System.out.println(PTStemmerUtilities.removeDiacritics(stem));
			stem = stemmer.getWordStem("canções"); //errado cançõ
			System.out.println(stem);
			stem = stemmer.getWordStem("canção"); //errado cançã
			System.out.println(stem);
			System.out.println(PTStemmerUtilities.removeDiacritics(stem));
			System.out.println(stemmer.getWordStem("da"));
			System.out.println(stemmer.getWordStem("naturais"));
			System.out.println(stemmer.getWordStem("natural"));
			System.out.println(stemmer.getWordStem("natureza"));
			System.out.println(stemmer.getWordStem("papel"));
			System.out.println(stemmer.getWordStem("papéis"));
			System.out.println(stemmer.getWordStem("mãe"));
			System.out.println(stemmer.getWordStem("mães"));
			System.out.println(stemmer.getWordStem("paizinhos"));
			System.out.println(stemmer.getWordStem("paizinho"));
			System.out.println(stemmer.getWordStem("pai"));
			System.out.println(stemmer.getWordStem("pais"));//errado -> pal
			System.out.println(stemmer.getWordStem("país"));//errado -> paí
			System.out.println(stemmer.getWordStem("países"));
			System.out.println(stemmer.getWordStem("quartil"));
			System.out.println(stemmer.getWordStem("quartis"));
			System.out.println(stemmer.getWordStem("filhinhos"));
			System.out.println(stemmer.getWordStem("filhos"));
			System.out.println(stemmer.getWordStem("engenharias"));
			System.out.println(stemmer.getWordStem("engenho"));
			System.out.println(stemmer.getWordStem("engenheiro"));
			System.out.println(stemmer.getWordStem("engenheiros"));
		} catch (PTStemmerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}

}
