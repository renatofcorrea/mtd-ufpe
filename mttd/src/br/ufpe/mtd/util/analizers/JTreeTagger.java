package br.ufpe.mtd.util.analizers;

import java.io.IOException;
import java.util.Arrays;

import org.annolab.tt4j.TokenHandler;
import org.annolab.tt4j.TreeTaggerException;
import org.annolab.tt4j.TreeTaggerWrapper;

import br.ufpe.mtd.util.enumerado.MTDArquivoEnum;

public class JTreeTagger implements TaggerInterface {

	private static String myModel = null;
	private static String[] models = {"pt.par:iso8859-1",//gamallo
			 "Trained80:iso8859-1"};//nilc mac-morpho 80%TRN 20%TST --- default
	private TreeTaggerWrapper<String> tt = null;
	private String ttHome = MTDArquivoEnum.PASTA_TREE_TAGGER.getArquivo().getAbsolutePath();
	
	private static JTreeTagger myInstance = getInstance();
	
	//private TaggerData data = null;//deve ser �nico para cada thread

	public static JTreeTagger getInstance() {  
		int indexModel = 1; //default model mac-morpho nilc
	      if (myInstance == null) {  
	    	  myModel = models[indexModel];
	    	  myInstance = new JTreeTagger(myModel);   
	      }   
	      return myInstance;   
	   }
	
	public static JTreeTagger getInstance(String model){
    	if (myInstance == null) {   
	    	  myInstance = new JTreeTagger(model);
	    	  myModel = model;
	    	}else if(!myModel.equals(model)) {
	    	  myInstance = new JTreeTagger(model);
	    	  myModel = model;
	    	}
	      return myInstance; 
    }
	
	private JTreeTagger(String model) {
		if(ttHome == null)
			ttHome = "WEB-INF\\aux_files\\Tagger\\TreeTagger";//"WebContent\\WEB-INF\\aux_files\\Tagger\\TreeTagger";
		String isdone = System.setProperty("treetagger.home", ttHome);//aqui.....
		if(isdone == null){
			System.out.println("Fail setting treetagger.home property with JTreeTagger path");
			model = ttHome + "\\models\\"+model;
		}
		tt = new TreeTaggerWrapper<String>();
		//data = null;
		try {
		     //tt.setModel("english.par:iso8859-1");
			 //tt.setModel("pt.par:iso8859-1");
			//tt.setModel("Trained80:iso8859-1");
			tt.setModel(model);
		    String sexe = tt.getExecutableProvider().getExecutable();
			System.out.println("JTreeTagger.etiquetar() executable: "+sexe);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());
			e.printStackTrace();
		}catch (NullPointerException e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());
			e.printStackTrace();
		}   finally {
		     //tt.destroy();
		}
	}
	
	 public static String[] getModels() {
			return models;
		}

	
	public String[] getTokens(TaggerData d) {
		return d.getTextoTokens().split("/");
	}
	
	public String[] getTags(TaggerData d) {
		return d.getTextoTags().split("/");
	}

	public static String getOgmaFormat(String token, String pos, String lemma){
		String temp = JOgmaEtiquetador.getInstance().buscaPalavra(token.toLowerCase(), "verbos", null);
		String temp2 = JOgmaEtiquetador.getInstance().buscaPalavra(token.toLowerCase(), "nomes", null);
		boolean isverb = temp.contains("VB") && temp2.isEmpty();
		boolean isvp = (temp.contains("VP")|| pos.contains("PCP")) && token.toLowerCase().matches(".*(i|a)(d|t)(o|a)[s]?") && !temp2.contains("SU") && (temp2.isEmpty() || temp2.contains("AJ"));
		boolean isnom = (temp2.contains("SU")) && temp.isEmpty();
		boolean isadj = temp2.contains("AJ") && temp.isEmpty() && !isnom;
		int len = token.trim().length();
		
		if(len == 1){
			if("=.,:;!?()[]{}<>'\"".contains(pos)||"=.,:;!?()[]{}<>'\"".contains(token))
				return token+"/PN";
			else if(pos.equals("-"))//hifen solto
				return token+"/PN";
			else if(token.equals("�"))
				return token+"/VB";	
		}
		if(len >=1 && len <= 2 ){
			if("� �s".contains(token.toLowerCase())){
				if(token.toLowerCase().equals("�"))
					return "a/PR "+"a/AD";
				else
					return "a/PR "+"as/AD";
			}
			if("o a os as".contains(token.toLowerCase())){
					return token+"/AD";
			}
		}
		if(len >=2 && len <= 3 ){
			if (token.equalsIgnoreCase("em"))
				return token.toLowerCase()+"/PR";
			else if (token.equalsIgnoreCase("se"))
				return token.toLowerCase()+"/ct";
			else if(token.toLowerCase().startsWith("n")&& "no na nos nas".contains(token.toLowerCase())){
				token = token.toLowerCase();
				int i = token.indexOf("o");
				int j = token.indexOf("a");
				if(i >=0)
					return "em/PR "+token.substring(i)+"/AD";	
				else if(j>=0)
					return "em/PR "+token.substring(j)+"/AD";
			}else if(token.toLowerCase().startsWith("d") && "de do da dos das".contains(token.toLowerCase())){
				token = token.toLowerCase();
				int i = token.indexOf("o");
				int j = token.indexOf("a");
				if(i >=0)
					return "de/PR "+token.substring(i)+"/AD";	
				else if(j>=0)
					return "de/PR "+token.substring(j)+"/AD";
				else if (token.indexOf("e") >=0)
					return token+"/PR";
				else
					return token+"/ct";
			}else if(token.toLowerCase().startsWith("a") && "ao aos".contains(token.toLowerCase())){
				int i = token.indexOf("o");
				if(i>=0)
					return "a/PR "+token.substring(i)+"/AD";
				else
					return token+"/AD";
			}
		}
	
		if(len >= 3 && len <=5){
				if("lhe lhes".contains(token.toLowerCase()))
				return token + "/ct";
				else if(token.toLowerCase().startsWith("qu") && "que qual quais".contains(token.toLowerCase()))
					return token + "/PL";
				else if(token.toLowerCase().startsWith("nu") && "num numa nuns numas".contains(token.toLowerCase())){
					int i = token.indexOf("u");
					if(i >=0)
					return "em/PR "+token.substring(i)+"/AI";
					else
					return token+"/ct";
				}
		}
		if(len >= 4 && len <= 5){
				if(token.toLowerCase().startsWith("de") && "dele dela deles delas".contains(token.toLowerCase()))
					return token + "/ct";
				else  if(token.toLowerCase().startsWith("pel") && "pelo pela pelos pelas".contains(token.toLowerCase()))
				return token+"/PR";
				else if(token.equalsIgnoreCase("para"))
					return token+"/PR";
		}
		if(len >= 5 && len <= 6){
				if("nesse nisso nessa neste nisto nesta nesses nessas nestes nestas deste disto desta desse disso dessa dessas desses destes destas".contains(token.toLowerCase()))
				return token + "/PD";
		}
		if(len >= 6 && len <= 8){
			if("�quele �quela �queles �quelas aquele aquela aqueles aquelas aquilo daquilo daquele daquela daqueles daquelas".contains(token.toLowerCase()))
			return token + "/PD";
		}
			
		if(token.matches("[-+]?(?:[0-9]+(?:[.,][0-9]*)?|[.,][0-9]+)(?:[eE][-+]?[0-9]+)?[%]?"))
				return token + "/NC";
		
		if (token.split("-").length > 1){ 
			if("lhe lhes lo la los las o a os as se nos vos".contains(token.split("-")[1]))
			return token.toLowerCase()+"/VB";
		else if ("o a os as se nos vos".contains(token.split("-")[1]))
			return token.toLowerCase()+"/VB";
		}
			
		if(myModel.equals(models[1])){//nilc mac-morpho http://www.nilc.icmc.usp.br/macmorpho/macmorpho-manual.pdf
				if(isverb && (pos.startsWith("V")||pos.startsWith("VAUX")))
					return token+"/VB";
				else if(isverb && token.toLowerCase().matches(".*(i|a|e|o)(r)$"))
					return token+"/VB";
				else if(isverb && pos.equals("N") && !token.toLowerCase().matches(".*(t|r|m)(a)(s)$"))
					return token+"/VB";
				else  if(!isverb &&token.equals("deste"))
					return token+"/PD";
				else if(isadj && (pos.equals("ADJ")))
					return token+"/AJ";
				else if(isadj && (pos.equals("V")||pos.equals("PCP")))
					return token+"/AJ";
				else if((pos.equals("N")|| pos.equals("NPROP")|| pos.equals("V")||pos.equals("ADJ"))&& token.matches("[A-Z�-�a-z�-�]+(a|o|i|e)nte(s)*") && (JOgmaEtiquetador.getInstance().buscaPalavra(token.toLowerCase().replaceFirst("nte(s)*", ""), "verbos", null).contains("VB")||token.toLowerCase().replaceFirst("nte(s)*", "").equalsIgnoreCase("provenie")))
					return token+"/AJ";//n�o casa com proveniente
				else if(isnom && (pos.equals("N")))
					//JOgmaEtiquetador.getInstance().buscaPalavra(token.toLowerCase(), "Nomes", null).isEmpty() &&
					return token+"/SU";
				else if(isnom && (pos.equals("NUM")))
					return token+"/SU";
				else if( pos.equalsIgnoreCase("PCP") && isvp)
					//JOgmaEtiquetador.getInstance().buscaPalavra(token.toLowerCase(), "Nomes", null).isEmpty() &&
					return token+"/VP";
				else if(isvp)
					//JOgmaEtiquetador.getInstance().buscaPalavra(token.toLowerCase(), "Nomes", null).isEmpty() &&
					return token+"/VP";
				else if( pos.equalsIgnoreCase("PCP") && !isvp && !isverb && !isnom)	
					//JOgmaEtiquetador.getInstance().buscaPalavra(token.toLowerCase(), "Nomes", null).isEmpty() &&
					return token+"/VB";
				else if(lemma.contains("@card@")||pos.equalsIgnoreCase("CARD")||pos.equalsIgnoreCase("NUM")||pos.equalsIgnoreCase("SENT")||pos.equalsIgnoreCase("CUR"))
					return token+"/NC";
				else if(pos.contains("@card@")||token.matches("[-+]?(?:[0-9]+(?:[.,][0-9]*)?|[.,][0-9]+)(?:[eE][-+]?[0-9]+)?[%]?"))
					return token + "/NC";
				else if(pos.equalsIgnoreCase("N")||pos.startsWith("N|")|| pos.contains("NPROP")|| pos.equalsIgnoreCase("PROP")){
					//    			return token+"/NP";
					//			    return token+"/NP";
					if(pos.equals("N"))
						return token + "/SU";
					else
						return token+"/NP";
				}else if(pos.startsWith("V")||pos.startsWith("VAUX")){
					 if(isnom)
						return token+"/NP";
					else if(temp2.equals("PD"))
						return token+"/PD";
					else
						return token+"/VB";
				}else if(pos.equalsIgnoreCase("PCP")){
					//			if(token.endsWith("ida")||token.endsWith("ido")||token.endsWith("ada")||token.endsWith("ado")||token.endsWith("idas")||token.endsWith("idos")||token.endsWith("adas")||token.endsWith("ados"))
					//			return token+"/VP"; //contempla VP - verbo participio
					if(!(temp.isEmpty()) || token.matches(".*(i|�|a)(d|t)(o|a)[s]?"))				
						return token+"/VP";
					else				
						return token+"/NP";
				}else  if(pos.startsWith("ADJ") && !temp2.equals("SU"))
					return token+"/AJ";//PD,PI,PP, (PS) (PS em geral categorizado como adjetivo)
				else if(pos.startsWith("ADJ") && temp2.equals("SU"))
					return token+"/SU";//PD,PI,PP, (PS) (PS em geral categorizado como adjetivo)
				else if(pos.startsWith("IN")){
					if(lemma.contains("@card@"))
						return token+"/NC";
					else
						return token+"/IN";//interjei��o
				}else if(pos.equalsIgnoreCase("PROADJ"))
					return token+"/de";//PD,PI,PP, (PS) (PS em geral categorizado como adjetivo)
				else if(pos.equalsIgnoreCase("PROSUB"))//PRONOME SUBSTANTIVO
					return token+"/PI";//PD,PI,PP, (PS) (PS em geral categorizado como adjetivo)
				else if(pos.equalsIgnoreCase("PROPESS"))
					return token+"/PP";//PD,PI,PP, (PS) (PS em geral categorizado como adjetivo)
				else if(pos.equalsIgnoreCase("PRO-KS")  || pos.equalsIgnoreCase("PRO-KS-REL"))
					//PRONOME CONECTIVO SUBORDINATIVO (PRO-KS), PRONOME CONECTIVO SUBORDIN. RELATIVO (PRO-KS-REL)
					return token+"/PL";
				else if(pos.equalsIgnoreCase("PDEN"))
					return token+"/ct";//PD,PI,PP, (PS) (PS em geral categorizado como adjetivo)
				else if(pos.startsWith("ADV"))
					return token+"/AV";
				else if(pos.contains("KC")|| pos.contains("KS")){//CONJUN��O SUBORDINATIVA (KS) //CONJUN��O COORDENATIVA (KC) || pos.equalsIgnoreCase("KS")
					if("e em".contains(token.toLowerCase()))
						return token+"/CJ";
					else if(token.length() >= 2 && "no na nos nas do da dos das".contains(token.toLowerCase())){
						token = token.toLowerCase();
						int i = token.indexOf("o");
						int j = token.indexOf("a");
						if(token.indexOf("n") == 0){
							if(i >=0)
								return "em/PR "+token.substring(i)+"/AD";	
							else if(j>=0)
								return "em/PR "+token.substring(j)+"/AD";
							else
								return token+"/CJ";
						}else {//if(token.indexOf("d") == 0){
							if(i >=0)
								return "de/PR "+token.substring(i)+"/AD";	
							else if(j>=0)
								return "de/PR "+token.substring(j)+"/AD";
							else
								return token+"/CJ";
						} 
					}else 
						return token+"/ct";
				}else if(pos.startsWith("PREP")){
					if(pos.equals("PREP|+")){
						if(lemma.contains("@card"))
							return token + "/NC";
						else
							return token + "/PR";
					}
					else if(pos.endsWith("+DET")){
						int i = token.indexOf("o");
						int j = token.indexOf("a");
						if(i >=0)
							return lemma.toLowerCase()+"/PR "+token.substring(i)+"/AD";	
						else
							return lemma.toLowerCase()+"/PR "+token.substring(j)+"/AD";
					}else  if((token.startsWith("no") || token.startsWith("na")) && token.length() >= 2 && token.length()<=3){
						int i = token.indexOf("o");
						int j = token.indexOf("a");
						if(i >=0)
							return "em/PR "+token.substring(i)+"/AD";	
						else if(j>=0)
							return "em/PR "+token.substring(j)+"/AD";
						else
							return token+"/PR";
					}else if(temp2.equals("SU"))
						return token+"/"+temp2;
					else
						return token+"/PR";
				}else  if(pos.equalsIgnoreCase("ART")){
					if(token.startsWith("um")||token.startsWith("Um")) 
						return token+"/AI";
					else 
						return token +"/AD";
				}else{
					System.out.println("NAO CONTEMPLADA: "+token + " "+pos + " "+lemma);
					return token+"/NR";
				}

			}else if(myModel.equals(models[0])){//gamallo
			if(pos.equalsIgnoreCase("NOM")){
				return token+"/SU";
			}else if(pos.equalsIgnoreCase("ADJ")){//pronomes possessivos como adjetivo
				return token+"/AJ";
			}else if(pos.equalsIgnoreCase("QUOTE") || pos.equalsIgnoreCase("VIRG")|| pos.equalsIgnoreCase("SENT")){
				return token+"/PN";
			}else if(pos.equalsIgnoreCase("-")){//hifen
				return token+"/NP";
			}else if(pos.startsWith("V")){
				if(pos.equalsIgnoreCase("V")){
					if(token.endsWith("ida")||token.endsWith("ido")||token.endsWith("ada")||token.endsWith("ado")||token.endsWith("idas")||token.endsWith("idos")||token.endsWith("adas")||token.endsWith("ados"))
					return token+"/VP"; //contempla VP - verbo participio
					else
					return token+"/VB";
				}else{//V+P
					return token+"/VB";				
				}
			}else if(pos.equalsIgnoreCase("P")){
				return token+"/de";//PD,PI,PP, (PS) (PS em geral categorizado como adjetivo)
			}else if(pos.equalsIgnoreCase("PR") || pos.equalsIgnoreCase("CONJSUB")){
				return token+"/PL";
			}else if(pos.equalsIgnoreCase("ADV")){
				return token+"/AV";
			}else if(pos.equalsIgnoreCase("CONJ")){
				return token+"/CJ";
			}else if(pos.startsWith("PRP")){
				if(pos.endsWith("+DET")){
					int i = token.indexOf("o");
					int j = token.indexOf("a");
					if(i >=0)
						return lemma.toLowerCase()+"/PR "+token.substring(i)+"/AD";	
					else
						return lemma.toLowerCase()+"/PR "+token.substring(j)+"/AD";
				}else
					return token+"/PR";
			}else if(pos.equalsIgnoreCase("DET")){
				if(lemma.equalsIgnoreCase("a") || lemma.equalsIgnoreCase("o"))
					return token+"/AD";
				else if(lemma.equalsIgnoreCase("um"))
					return token+"/AI";
				else
					return token +"/de";
			}else if(pos.equalsIgnoreCase("CARD")){
				return token+"/NC";
			}else{
				System.out.println("NAO CONTEMPLADA: "+token + " "+pos + " "+lemma);
				return token+"/NR";
			}
		}else{
			return null;
		}
		
		
		
	}
	
		
	/**
	 * @param texto
	 * @return texto formatado para tokeniza��o e etiquetagem pelo TreeTagger
	 */
	private static String formatText(String texto) {
		texto = texto.replace("/",", "); //substitui /
	    texto = texto.replace("\"", " ");//substitui aspas
	    texto = texto.replace("\'", " ");//substitui aspas
		String [] pontChars = new String [] {"<",">", "=", ":",";","!","?","(",")","[","]","\""};
		for(int i = 0; i < pontChars.length; i++)
			texto = texto.replace(pontChars[i]," "+pontChars[i]+" ");
		//casos especiais: ",", ".","-" tratados abaixo
		//texto = texto.replaceAll("[.](?=($|[A-Za-z ]))"," . "); //casa com ponto de sigla
		//(\S.+?[.!?])(?=\s+|$) //separa senten�as, casa com cada senten�a.
		texto = texto.replaceAll("(?![A-Z])[.](?=([A-Z]|$))", ""); //elimina ponto de ex. S.O.M. => SOM.
		//texto = texto.replaceAll("[.](?=($|[ \n]+[A-Z�-�]{1}[a-z�-�]*))"," . ");//ponto ideal
		texto = texto.replaceAll("[.](?=($|[ \n]+[A-Z�-�a-z�-�]))"," . ");
		texto = texto.replaceAll("[,](?=[A-Z�-�a-z�-� ])"," , ");
		texto = texto.replace('\t', ' ');
		texto = texto.replace('\n', ' ');
		texto = texto.replace('\r', ' ');
		texto = texto.replaceAll("[ ]{2,}"," ");
		return texto;
	}
	
	/* (non-Javadoc)
	 * @see TaggerInterface#etiquetar(java.lang.String)
	 */
	@Override
	public TaggerData etiquetar(String texto){
		//data = new TaggerData();
		texto = formatText(texto);
		TokenHandler<String> tr=
			    new TokenHandler<String>() {
			         public TaggerData data = new TaggerData();

					public void token(String token, String pos, String lemma) {
			        	 //textoEtiquetado = textoEtiquetado+ token+"/"+pos+" ";
			        	 data.setTextoEtiquetado(data.getTextoEtiquetado()+ getOgmaFormat(token,pos,lemma)+" ");
			        	 data.setTextoTokens(data.getTextoTokens() + token + "/");
			        	 data.setTextoLemas(data.getTextoLemas() +lemma+ "/");
			        	 data.setTextoTags(data.getTextoTags() +pos+ "/");
			         }
					
					public String toString(){return data.toString();}
			     };
			     tt.setHandler(tr);
		
		String [] words = texto.split(" ");//semelhante a whitespace tokenizer 
		
		try{
			
			tt.process(Arrays.asList(words));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());
			e.printStackTrace();
		}catch (NullPointerException e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());
			e.printStackTrace();
		} catch (TreeTaggerException e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());
			e.printStackTrace();
		} 
		 finally {
		     //tt.destroy();
		 }
		return TaggerData.parse(tr.toString());
	}
	
	public void destroy(){
		tt.destroy();
		//data = null;
	}
	
	/* (non-Javadoc)
	 * @see TaggerInterface#getLemmas()
	 */
	@Override
	public String[] getLemmas(TaggerData d) {
		return d.getTextoLemas().split("/");
	}

	/* Return name of tagger
	 * @see TaggerInterface#getLemmas()
	 */
	@Override
	public String getName() {
		
		return "TreeTagger";
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		/*		 Point TT4J to the TreeTagger installation directory. The executable is expected
		 // in the "bin" subdirectory - in this example at "/opt/treetagger/bin/tree-tagger"
		 System.setProperty("treetagger.home", "c:\\TreeTagger");
		 TreeTaggerWrapper tt = new TreeTaggerWrapper<String>();
		 try {
		     //tt.setModel("english.par:iso8859-1");
			 tt.setModel("pt.par:iso8859-1");
		     tt.setHandler(new TokenHandler<String>() {
		         public void token(String token, String pos, String lemma) {
		        	 int count = 1;
		             System.out.println(token+"\t"+pos+"\t"+lemma);
		         }
		     });
		     //tt.process(Arrays.asList(new String[] {"This", "is", "a", "test", "."}));
		     tt.process(Arrays.asList(new String[] {"Isto", "�", "um", "teste", "."}));
		 } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TreeTaggerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 finally {
		     tt.destroy();
		 }*/
		JTreeTagger jt = JTreeTagger.getInstance(JTreeTagger.getModels()[1]);
	/*	System.out.println(jt.etiquetar("Isto � um teste.").getTextoEtiquetado());
		String fr1= new String("Eu comi minha ma�� hoje para auxiliar a digest�o");
		String fr2= new String("O presente trabalho objetiva analisar a roda.");
		String sent = new String("O novo c�lculo das aposentadorias resulta em valores menores do que os atuais para quem perde o benef�cio com menos tempo de contribui��o e idade.");
		System.out.println(JTreeTagger.getInstance().etiquetar(fr1).getTextoEtiquetado());
		System.out.println(JTreeTagger.getInstance().etiquetar(fr2).getTextoEtiquetado());
		*/
		String [] res = new String[7];
		res[0]="Os resultados obtidos revelaram a fraude.";
		res[0]="Uma base de dados existente foi reformulada a partir de os dados coletados com os atacadistas da CEASA-PE participantes do com�rcio da uva It�lia proveniente do Vale do S�o Francisco.";
		//res[0]="Nas empresas visitadas prevalece a estrutura familiar.";
		//res[0]="Al�m disso, os membros desses arranjos performatizam e significam suas a��es com fantasias.";
		//res[0]="O desenvolvimento local sustent�vel em um p�lo petrol�fero. ";
		//res[0]="A conserva��o sustent�vel do meio ambiente numa localidade no Estado de Sergipe.";
		//res[0]="O presente trabalho tem por tema avaliar os impactos.";
		//res[0]= "Viva a gest�o participativa dos funcion�rios.";
		//res[0]= "As perguntas das entrevistas estruturadas sobre temas gerais foram propostas por mim.";
		//res[0]="Os resultados indicaram que : 1 ) o mapeamento furou.";
		//res[0]="Os stakeholders interagem por meio de coopeti��o.";
		//res[0] = "O Desenvolvimento Local tem por estrat�gia inovadora os Arranjos Produtivos Locais.";
		//res[0] = "Diante deste novo cen�rio.";
		//res[0] = "Acompanhar as mudan�as, t�m sido o grande desafio dos empres�rios do setor de vestu�rio do arranjo produtivo local do agreste pernambucano.";
		//res[0] = "O fim do acordo multifibras aconteceu em 2000.";
        //res[0] =  "No per�odo de abril de 2005 a novembro de 2006 foram estudadas a distribui��o temporal, a partilha do habitat, a reprodu��o e a atividade vocal em uma assembleia de anf�bios anuros na Fazenda Serra da Esperan�a, munic�pio de Lebon R�gis, Estado de Santa Catarina. Os objetivos do trabalho foram verificar a import�ncia da pluviosidade e da temperatura na distribui��o temporal das esp�cies na assembleia, analisar a ocupa��o do habitat, realizar a an�lise ac�stica do repert�rio vocal das esp�cies e testar a influ�ncia da temperatura do ar e do tamanho e massa dos machos vocalizantes sobre os par�metros ac�sticos. Foram encontradas 32 esp�cies na �rea de estudo, a maior riqueza de anf�bios registrada para o Estado. A taxonomia de pelo menos sete dessas esp�cies � incerta, podendo tratar-se de t�xons ainda n�o descritos na literatura. A temperatura apresentou uma forte influ�ncia na distribui��o temporal das esp�cies. O n�mero de esp�cies em atividade de vocaliza��o e reprodu��o foi relacionado �s varia��es da temperatura mensal m�dia, m�nima e m�xima, significando que nos meses mais quentes foram encontradas mais esp�cies em atividade de vocaliza��o e reprodu��o. Foi documentada atividade reprodutiva em 14 esp�cies e um total de nove modos reprodutivos na assembleia. A compara��o das vocaliza��es de 23 esp�cies da assembleia com descri��es de vocaliza��es de outras assembleias indicou diferen�as que sugerem a exist�ncia de esp�cies ainda n�o descritas na �rea de estudo. Tamb�m foram documentadas varia��es intraespec�ficas nos cantos em decorr�ncia do tamanho e massa dos machos cantores e em fun��o da temperatura do ar. Encontraram-se influ�ncias da massa e tamanho do macho cantor na frequ�ncia dominante do canto de an�ncio, e tamb�m da temperatura do ar na dura��o das notas. A riqueza de esp�cies da assembleia apresentou forte semelhan�a biogeogr�fica com �reas de Floresta Ombr�fila Mista dos Estados de Santa Catarina, Paran� e Rio Grande do Sul. A presen�a de poss�veis novas esp�cies e da esp�cie Pleurodema bibroni, classificada na categoria quase amea�ada, salienta a import�ncia da conserva��o deste bioma altamente degradado e demonstra a nossa car�ncia de conhecimento acerca da anurofauna catarinense.";
        //res[0]="ELEMENTOS PARA A IDENTIFICA��O DA NECESSIDADE DE OFERTA, 24 HORAS, DE BENS ALIMENT�CIOS NO P�LO M�DICO HOSPITALAR LOCALIZADO NO BAIRRO DA ILHA DO LEITE RECIFE,PE. O presente estudo analisa os elementos para a identifica��o da necessidade de oferta, 24 horas, de bens aliment�cios no p�lo m�dico hospitalar localizado no Bairro da Ilha do Leite Recife, atrav�s da aplica��o de question�rios, objetivando constituir um indicador das necessidades n�o atendidas de consumo 24 horas de bens de alimentos nesta �rea reconhecida geograficamente como P�lo M�dico de Recife.";
		res[1]= new String("Apresenta de forma introdut�ria quest�es e conceitos fundamentais sobre metadados e a estrutura��o da descri��o padronizada de documentos eletr�nicos. Discorre sobre os elementos propostos no Dublin Core e comenta os projetos de cataloga��o dos recursos da Internet, CATRIONA, InterCat e CALCO.");
		res[2] = new String("Bibliografia internacional seletiva e anotada sobre bibliotecas digitais. Aborda os seguintes aspectos: a) Vision�rios, principais autores que escreveram sobre a biblioteca do futuro, no per�odo de 1945-1985; b) conceitua��o de biblioteca digital; c) projetos em andamento na Alemanha, Austr�lia, Brasil, Canad�, Dinamarca, Espanha, Estados Unidos, Franca, Holanda, Jap�o, Nova Zel�ndia, Reino Unido, Su�cia e Vaticano; d) aspectos t�cnicos relativos a constru��o de uma biblioteca digital: arquitetura do sistema, convers�o de dados e escaneamento, marca��o de textos, desenvolvimento de cole��es, cataloga��o, classifica��o/indexa��o, metadados, referencia, recupera��o da informa��o, direitos autorais e preserva��o da informa��o digital; e) principais fontes de reuni�es t�cnicas especificas, lista de discuss�o, grupos e centros de estudos, cursos e treinamento.");
		res[3] = new String("Apresenta a implanta��o de recursos multim�dia e interface Web no banco de dados desenvolvido para a cole��o de v�deos da Videoteca Multimeios, pertencente ao Departamento de Multimeios do Instituto de Artes da UNICAMP. Localiza a discuss�o conceitual no universo das bibliotecas digitais e prop�e altera��es na configura��o atual de seu banco de dados.");
		res[4] = new String("Este artigo aborda a necessidade de ado��o de padr�es de descri��o de recursos de informa��o eletr�nica, particularmente, no �mbito da Embrapa Inform�tica Agropecu�ria. O Rural M�dia foi desenvolvido utilizando o modelo Dublin Core (DC) para descri��o de seu acervo, acrescido de pequenas adapta��es introduzidas diante da necessidade de adequar-se a especificidades meramente institucionais. Este modelo de metadados baseado no Dublin Core, adaptado para o Banco de Imagem, possui caracter�sticas que endossam a sua ado��o, como a simplicidade na descri��o dos recursos, entendimento sem�ntico universal (dos elementos), escopo internacional e extensibilidade (o que permite sua adapta��o as necessidades adicionais de descri��o).");
		res[5] = new String("Relato da experi�ncia do Impa na informatiza��o de sua biblioteca, utilizando o software Horizon, e na constru��o de um servidor de preprints (disserta��es de mestrado, teses de doutorado e artigos ainda n�o publicados) atrav�s da participa��o no projeto internacional Math-Net.");
		res[6] = new String("Descreve as op��es tecnol�gicas e metodol�gicas para atingir a interoperabilidade no acesso a recursos informacionais eletr�nicos, dispon�veis na Internet, no �mbito do projeto da Biblioteca Digital Brasileira em Ci�ncia e Tecnologia, desenvolvido pelo Instituto Brasileiro de Informa��o em Ci�ncia e Tecnologia(IBCT). Destaca o impacto da Internet sobre as formas de publica��o e comunica��o em C&T e sobre os sistemas de informa��o e bibliotecas. S�o explicitados os objetivos do projeto da BDB de fomentar mecanismos de publica��o pela comunidade brasileira de C&T, de textos completos diretamente na Internet, sob a forma de teses, artigos de peri�dicos, trabalhos em congressos, literatura \"cinzenta\",ampliando sua visibilidade e acessibilidade nacional e internacional, e tamb�m de possibilitar a interoperabilidade entre estes recursos informacionais brasileiros em C&T, heterog�neos e distribu�dos, atrav�s de acesso unificado via um portal, sem a necessidade de o usu�rio navegar e consultar cada recurso individualmente.");
		for(int i=0; i < 1;i++){//res.length
			System.out.println(JTreeTagger.getInstance().etiquetar(res[i]).getTextoEtiquetado());
		}
		
		//pela/NP
		
		//TODO: Verificar os casos de extra��o de sns: 24 horas, demandas n�o atendidas
		
		String result = JTreeTagger.getInstance().etiquetar(res[0]).getTextoEtiquetado();//6-4-3 //BANCO DE DADOS n�o � capturado x2, ok para idcat2
		System.out.println(result);
		System.out.println(JOgma.extraiSNTextoEtiquetado(result).toString());//ogma for�a a barra substituindo "do que" por "que"
		
	}

	

}
