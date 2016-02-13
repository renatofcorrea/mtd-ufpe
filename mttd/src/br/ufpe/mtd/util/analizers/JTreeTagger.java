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
	
	//private TaggerData data = null;//deve ser único para cada thread

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
			ttHome = "WebContent\\WEB-INF\\aux_files\\Tagger\\TreeTagger";
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
		boolean isvp = temp.contains("VP") && token.toLowerCase().matches(".*(i|a)(d|t)(o|a)[s]?") && (temp2.isEmpty() || temp2.contains("AJ"));
		boolean isnom = temp2.contains("SU") && temp.isEmpty();
		
		if(myModel.equals(models[1])){//nilc mac-morpho http://www.nilc.icmc.usp.br/macmorpho/macmorpho-manual.pdf
			if(isverb && (pos.startsWith("V")||pos.startsWith("VAUX"))){
				return token+"/VB";
			}else if(isverb && token.toLowerCase().matches(".*(i|a|e|o)(r)$")){
				return token+"/VB";
			}else if(token.equals("é")){
				return token+"/VB";
			}else if(!isverb &&token.equals("deste")){
				return token+"/PD";
			}
			else if( pos.equalsIgnoreCase("PCP") && isvp){	
				//JOgmaEtiquetador.getInstance().buscaPalavra(token.toLowerCase(), "Nomes", null).isEmpty() &&
				return token+"/VP";
			}else if(isvp)
				//JOgmaEtiquetador.getInstance().buscaPalavra(token.toLowerCase(), "Nomes", null).isEmpty() &&
				return token+"/VP";
			else if(isnom && (pos.equals("N") || pos.equals("ADJ")))
				//JOgmaEtiquetador.getInstance().buscaPalavra(token.toLowerCase(), "Nomes", null).isEmpty() &&
				return token+"/SU";
			else
			if(lemma.contains("@card@")||pos.equalsIgnoreCase("CARD")||pos.equalsIgnoreCase("NUM")||pos.equalsIgnoreCase("SENT")||pos.equalsIgnoreCase("CUR")){
				return token+"/NC";
			}else if(pos.contains("@card@")||token.matches("[-+]?(?:[0-9]+(?:[.,][0-9]*)?|[.,][0-9]+)(?:[eE][-+]?[0-9]+)?[%]?"))
				return token + "/NC";
			else if(pos.equalsIgnoreCase("N")||pos.startsWith("N|")|| pos.contains("NPROP")|| pos.equalsIgnoreCase("PROP")){
//    			return token+"/NP";
			
			if(token.length() >= 2 && "de do da dos das".contains(token.toLowerCase())){
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
    					return token+"/NP";
    			}else if (token.equalsIgnoreCase("em"))
					return token.toLowerCase()+"/PR";
    			else if (token.equalsIgnoreCase("se"))
    				return token.toLowerCase()+"/ct";
    			else if(token.length() >= 5 && "nesse nisso nessa neste nisto nesta nesses nessas nestes nestas deste disto desta desse disso dessa dessas desses destes destas".contains(token.toLowerCase())){
					return token + "/PD";
				}
				else if(token.length() >= 4 && "dele dela deles delas".contains(token.toLowerCase()))
					return token + "/ct";
				else if(token.length() >= 3 && "lhe lhes".contains(token.toLowerCase()))
					return token + "/ct";
    			else if (token.split("-").length > 1 && "lhe lhes lo la los las o a os as se nos vos".contains(token.split("-")[1]))
    				return token.toLowerCase()+"/VB";
    			else if(token.length() >= 3 && "num numa nuns numas".contains(token.toLowerCase())){
    				int i = token.indexOf("u");
    				if(i >=0){
    					return "em/PR "+token.substring(i)+"/AI";
    				}else{
    					return token+"/ct";
    				}
				}
    			else if(token.length() >= 2 && "no na nos nas".contains(token.toLowerCase())){
    				token = token.toLowerCase();
    				int i = token.indexOf("o");
    				int j = token.indexOf("a");
    				if(i >=0)
    					return "em/PR "+token.substring(i)+"/AD";	
    				else if(j>=0)
    					return "em/PR "+token.substring(j)+"/AD";
    				else
    					return token+"/NP";
    			}else if(token.length() >= 4 && "pelo pela pelos pelas".contains(token.toLowerCase()))
    					return token+"/PR";
    			else if(token.length() >= 2 && "ao aos".contains(token.toLowerCase())){
    				int i = token.indexOf("o");
    				if(i>=0)
    				return "a/PR "+token.substring(i)+"/AD";
    				else
    					return token+"/AD";
    			}else if("à às".contains(token.toLowerCase())){
    				if(token.toLowerCase().equals("à"))
					return "a/PR "+"a/AD";
    				else
    				return "a/PR "+"as/AD";
    			}else if("=.,:;!?()[]{}<>'\"".contains(pos)||"=.,:;!?()[]{}<>'\"".contains(token))
    				return token+"/PN";
    			else
    			return token+"/NP";
		}else if("=.,:;!?()[]{}<>'\"".contains(pos)||".,:;!?()[]{}\"".contains(token)){
			return token+"/PN";
		}else if(pos.startsWith("V")||pos.startsWith("VAUX")){
			if (token.split("-").length > 1 && "o a os as se nos vos".contains(token.split("-")[1]))
				return token.toLowerCase()+"/VB";
			else if(token.matches("[-+]?(?:[0-9]+(?:[.,][0-9]*)?|[.,][0-9]+)(?:[eE][-+]?[0-9]+)?[%]?"))
				return token + "/NC";
			else if(isnom)
				return token+"/NP";
			else if(temp2.equals("PD"))
				return token+"/PD";
			else if(token.equalsIgnoreCase("para"))
				return token+"/PR";
			else
				return token+"/VB";
		}else if(pos.equalsIgnoreCase("PCP")){
//			if(token.endsWith("ida")||token.endsWith("ido")||token.endsWith("ada")||token.endsWith("ado")||token.endsWith("idas")||token.endsWith("idos")||token.endsWith("adas")||token.endsWith("ados"))
//			return token+"/VP"; //contempla VP - verbo participio
			if(!(temp.isEmpty()) || token.matches(".*(i|í|a)(d|t)(o|a)[s]?"))				
				return token+"/VP";
			else				
			return token+"/NP";
		}else if(pos.equals("-")){//hifen solto
			return token+"/PN";
		}else if(pos.startsWith("ADJ")){
			return token+"/AJ";//PD,PI,PP, (PS) (PS em geral categorizado como adjetivo)
		}else if(pos.startsWith("IN")){
			if(lemma.contains("@card@"))
				return token+"/NC";
			else
			return token+"/IN";//interjeição
		}else if(pos.equalsIgnoreCase("PROADJ")){
			return token+"/de";//PD,PI,PP, (PS) (PS em geral categorizado como adjetivo)
		}else if(pos.equalsIgnoreCase("PROSUB")){//PRONOME SUBSTANTIVO
			return token+"/PI";//PD,PI,PP, (PS) (PS em geral categorizado como adjetivo)
		}else if(pos.equalsIgnoreCase("PROPESS")){
			return token+"/PP";//PD,PI,PP, (PS) (PS em geral categorizado como adjetivo)
		}else if(pos.equalsIgnoreCase("PRO-KS")  || pos.equalsIgnoreCase("PRO-KS-REL")){
			//PRONOME CONECTIVO SUBORDINATIVO (PRO-KS), PRONOME CONECTIVO SUBORDIN. RELATIVO (PRO-KS-REL)
			return token+"/PL";
		}else if(pos.equalsIgnoreCase("PDEN")){
			return token+"/ct";//PD,PI,PP, (PS) (PS em geral categorizado como adjetivo)
		}else if(pos.startsWith("ADV")){
			return token+"/AV";
		}else if(pos.contains("KC")|| pos.contains("KS")){//CONJUNÇÃO SUBORDINATIVA (KS) //CONJUNÇÃO COORDENATIVA (KC) || pos.equalsIgnoreCase("KS")
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
			}else if(token.length() >= 4 && "pelo pela pelos pelas".contains(token.toLowerCase()))
				return token+"/PR";
			else
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
			}else 
				if(temp2.equals("SU"))
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
	 * @return texto formatado para tokenização e etiquetagem pelo TreeTagger
	 */
	private static String formatText(String texto) {
		texto = texto.replace("/",", "); //substitui /
	    texto = texto.replace("\"", ", ");//substitui aspas
		String [] pontChars = new String [] {"<",">", "=", ":",";","!","?","(",")","[","]","\""};
		for(int i = 0; i < pontChars.length; i++)
			texto = texto.replace(pontChars[i]," "+pontChars[i]+" ");
		//casos especiais: ",", ".","-" tratados abaixo
		//texto = texto.replaceAll("[.](?=($|[A-Za-z ]))"," . "); //casa com ponto de sigla
		texto = texto.replaceAll("[.](?=($|[ ][A-ZÀ-Ú]{1}[a-zà-ú]*))"," . ");
		texto = texto.replaceAll("[,](?=[A-Za-z ])"," , ");
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
		     tt.process(Arrays.asList(new String[] {"Isto", "é", "um", "teste", "."}));
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
	/*	System.out.println(jt.etiquetar("Isto é um teste.").getTextoEtiquetado());
		String fr1= new String("Eu comi minha maçã hoje para auxiliar a digestão");
		String fr2= new String("O presente trabalho objetiva analisar a roda.");
		String sent = new String("O novo cálculo das aposentadorias resulta em valores menores do que os atuais para quem perde o benefício com menos tempo de contribuição e idade.");
		System.out.println(JTreeTagger.getInstance().etiquetar(fr1).getTextoEtiquetado());
		System.out.println(JTreeTagger.getInstance().etiquetar(fr2).getTextoEtiquetado());
		*/
		String [] res = new String[7];
		res[0] = "Diante deste novo cenário.";
		//res[0] = "Acompanhar as mudanças, têm sido o grande desafio dos empresários do setor de vestuário do arranjo produtivo local do agreste pernambucano.";
		//res[0] = "O fim do acordo multifibras aconteceu em 2000.";
        //res[0] =  "No período de abril de 2005 a novembro de 2006 foram estudadas a distribuição temporal, a partilha do habitat, a reprodução e a atividade vocal em uma assembleia de anfíbios anuros na Fazenda Serra da Esperança, município de Lebon Régis, Estado de Santa Catarina. Os objetivos do trabalho foram verificar a importância da pluviosidade e da temperatura na distribuição temporal das espécies na assembleia, analisar a ocupação do habitat, realizar a análise acústica do repertório vocal das espécies e testar a influência da temperatura do ar e do tamanho e massa dos machos vocalizantes sobre os parâmetros acústicos. Foram encontradas 32 espécies na área de estudo, a maior riqueza de anfíbios registrada para o Estado. A taxonomia de pelo menos sete dessas espécies é incerta, podendo tratar-se de táxons ainda não descritos na literatura. A temperatura apresentou uma forte influência na distribuição temporal das espécies. O número de espécies em atividade de vocalização e reprodução foi relacionado às variações da temperatura mensal média, mínima e máxima, significando que nos meses mais quentes foram encontradas mais espécies em atividade de vocalização e reprodução. Foi documentada atividade reprodutiva em 14 espécies e um total de nove modos reprodutivos na assembleia. A comparação das vocalizações de 23 espécies da assembleia com descrições de vocalizações de outras assembleias indicou diferenças que sugerem a existência de espécies ainda não descritas na área de estudo. Também foram documentadas variações intraespecíficas nos cantos em decorrência do tamanho e massa dos machos cantores e em função da temperatura do ar. Encontraram-se influências da massa e tamanho do macho cantor na frequência dominante do canto de anúncio, e também da temperatura do ar na duração das notas. A riqueza de espécies da assembleia apresentou forte semelhança biogeográfica com áreas de Floresta Ombrófila Mista dos Estados de Santa Catarina, Paraná e Rio Grande do Sul. A presença de possíveis novas espécies e da espécie Pleurodema bibroni, classificada na categoria quase ameaçada, salienta a importância da conservação deste bioma altamente degradado e demonstra a nossa carência de conhecimento acerca da anurofauna catarinense.";
        //res[0]="ELEMENTOS PARA A IDENTIFICAÇÃO DA NECESSIDADE DE OFERTA, 24 HORAS, DE BENS ALIMENTÍCIOS NO PÓLO MÉDICO HOSPITALAR LOCALIZADO NO BAIRRO DA ILHA DO LEITE RECIFE,PE. O presente estudo analisa os elementos para a identificação da necessidade de oferta, 24 horas, de bens alimentícios no pólo médico hospitalar localizado no Bairro da Ilha do Leite Recife, através da aplicação de questionários, objetivando constituir um indicador das necessidades não atendidas de consumo 24 horas de bens de alimentos nesta área reconhecida geograficamente como Pólo Médico de Recife.";
		res[1]= new String("Apresenta de forma introdutória questões e conceitos fundamentais sobre metadados e a estruturação da descrição padronizada de documentos eletrônicos. Discorre sobre os elementos propostos no Dublin Core e comenta os projetos de catalogação dos recursos da Internet, CATRIONA, InterCat e CALCO.");
		res[2] = new String("Bibliografia internacional seletiva e anotada sobre bibliotecas digitais. Aborda os seguintes aspectos: a) Visionários, principais autores que escreveram sobre a biblioteca do futuro, no período de 1945-1985; b) conceituação de biblioteca digital; c) projetos em andamento na Alemanha, Austrália, Brasil, Canadá, Dinamarca, Espanha, Estados Unidos, Franca, Holanda, Japão, Nova Zelândia, Reino Unido, Suécia e Vaticano; d) aspectos técnicos relativos a construção de uma biblioteca digital: arquitetura do sistema, conversão de dados e escaneamento, marcação de textos, desenvolvimento de coleções, catalogação, classificação/indexação, metadados, referencia, recuperação da informação, direitos autorais e preservação da informação digital; e) principais fontes de reuniões técnicas especificas, lista de discussão, grupos e centros de estudos, cursos e treinamento.");
		res[3] = new String("Apresenta a implantação de recursos multimídia e interface Web no banco de dados desenvolvido para a coleção de vídeos da Videoteca Multimeios, pertencente ao Departamento de Multimeios do Instituto de Artes da UNICAMP. Localiza a discussão conceitual no universo das bibliotecas digitais e propõe alterações na configuração atual de seu banco de dados.");
		res[4] = new String("Este artigo aborda a necessidade de adoção de padrões de descrição de recursos de informação eletrônica, particularmente, no âmbito da Embrapa Informática Agropecuária. O Rural Mídia foi desenvolvido utilizando o modelo Dublin Core (DC) para descrição de seu acervo, acrescido de pequenas adaptações introduzidas diante da necessidade de adequar-se a especificidades meramente institucionais. Este modelo de metadados baseado no Dublin Core, adaptado para o Banco de Imagem, possui características que endossam a sua adoção, como a simplicidade na descrição dos recursos, entendimento semântico universal (dos elementos), escopo internacional e extensibilidade (o que permite sua adaptação as necessidades adicionais de descrição).");
		res[5] = new String("Relato da experiência do Impa na informatização de sua biblioteca, utilizando o software Horizon, e na construção de um servidor de preprints (dissertações de mestrado, teses de doutorado e artigos ainda não publicados) através da participação no projeto internacional Math-Net.");
		res[6] = new String("Descreve as opções tecnológicas e metodológicas para atingir a interoperabilidade no acesso a recursos informacionais eletrônicos, disponíveis na Internet, no âmbito do projeto da Biblioteca Digital Brasileira em Ciência e Tecnologia, desenvolvido pelo Instituto Brasileiro de Informação em Ciência e Tecnologia(IBCT). Destaca o impacto da Internet sobre as formas de publicação e comunicação em C&T e sobre os sistemas de informação e bibliotecas. São explicitados os objetivos do projeto da BDB de fomentar mecanismos de publicação pela comunidade brasileira de C&T, de textos completos diretamente na Internet, sob a forma de teses, artigos de periódicos, trabalhos em congressos, literatura \"cinzenta\",ampliando sua visibilidade e acessibilidade nacional e internacional, e também de possibilitar a interoperabilidade entre estes recursos informacionais brasileiros em C&T, heterogêneos e distribuídos, através de acesso unificado via um portal, sem a necessidade de o usuário navegar e consultar cada recurso individualmente.");
		for(int i=0; i < 1;i++){//res.length
			System.out.println(JTreeTagger.getInstance().etiquetar(res[i]).getTextoEtiquetado());
		}
		
		//pela/NP
		
		//TODO: Verificar os casos de extração de sns: 24 horas, demandas não atendidas
		
		String result = JTreeTagger.getInstance().etiquetar(res[0]).getTextoEtiquetado();//6-4-3 //BANCO DE DADOS não é capturado x2, ok para idcat2
		System.out.println(result);
		System.out.println(JOgma.extraiSNTextoEtiquetado(result).toString());//ogma força a barra substituindo "do que" por "que"
		
	}

	

}
