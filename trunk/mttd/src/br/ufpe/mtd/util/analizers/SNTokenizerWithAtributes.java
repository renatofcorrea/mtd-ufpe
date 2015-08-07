package br.ufpe.mtd.util.analizers;


import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;



public class SNTokenizerWithAtributes extends SNTokenizer {
	TypeAttribute ptTypeAtt = null;
	OffsetAttribute offsetAtt = null;
	//ArrayList finalType = null;
	//ArrayList finalOffset = null;
	
	public SNTokenizerWithAtributes(Reader input, String fn) {
		super(input, fn);
		this.ptTypeAtt = (TypeAttribute)addAttribute(TypeAttribute.class);
		this.offsetAtt = (OffsetAttribute)addAttribute(OffsetAttribute.class);
	}

	
//	public SNTokenizerWithAtributes(Reader input, HashSet astopWordsList,String fn){
//		super(input,astopWordsList,fn);
//		this.ptTypeAtt = (TypeAttribute)addAttribute(TypeAttribute.class);
//		this.offsetAtt = (OffsetAttribute)addAttribute(OffsetAttribute.class);
//	}
	
	@Override
	public boolean incrementToken() throws IOException {
		clearAttributes();

		if (this.finalToken==null || this.tokenIndex>=this.finalToken.size()) {
			return false;
		}
		Token temp = (Token)this.finalToken.get(this.tokenIndex);
		//this.termAtt.setTermBuffer(temp.term());
		String word = temp.toString();
		termAtt.copyBuffer(word.toCharArray(),0,word.length());
		this.ptTypeAtt.setType(temp.type());
		this.offsetAtt.setOffset(temp.startOffset(),temp.endOffset());
		//System.out.println("increment:"+temp.term()+":"+temp.startOffset()+":"+temp.endOffset());
		this.tokenIndex++;
		return true;
		
	}


	
	void addToFinalToken(ArrayList result, HashMap gramType, HashMap gramOffset){
		this.finalToken = new ArrayList();
		//this.finalType = new ArrayList();
		//System.out.println("size: "+tokenString.size());
		for(int i=0;i<result.size();i++){
			String pattern = (String)result.get(i);
			String type = (String)gramType.get(pattern);
			int offset = (Integer)gramOffset.get(pattern);
			//NA LINHA ABAIXO***java.lang.IllegalArgumentException: startOffset must be non-negative, and endOffset must be >= startOffset, startOffset=-1,endOffset=40
			int itemp = offset-pattern.length();
			Token temp = new Token(pattern,(itemp>=0)?itemp:0, offset, type); //the field name here doesn't matter, it will follow the one in Document.add
			//Token temp = new Token(pattern.toCharArray(),0,pattern.length(),offset-pattern.length(),offset); //offset-pattern.length()+1
			//Token temp = new Token(pattern, int start, int end, String typ);
			temp.setType("SN");
			temp.setPositionIncrement(1);
			//Token temp = new Token(pattern+"\t"+type+"\t"+offset,i, i+1,fieldname); //the field name here doesn't matter, it will follow the one in Document.add
			this.finalToken.add(temp);

			//System.out.println(temp.term()+"\ttype: "+type);
			if(output!=null)
				output.println((String)result.get(i));
		}
	}
	
	/**
	 * tokenize the context, lower case all the words
	 * Unigram will be extracted, which has POS tag: VB, JJ, NN, or their extentions.
	 * 
	 * @param aContext
	 * @return
	 */
	void tokenize(String aContext){ //define as tokens, o método a alterar é este
		System.out.println("SNTokenizerWithAtributes");
		try{
			//O metodo etiquetar chama  o etiquetador parametrizado em Analyzer através
			// da chamada ao método setTagger de Tokenizer
			//-----Etiquetando o texto
			String res2 = etiquetar(aContext);
			System.out.println(res2);
			String resultado  = JOgma.identificaSNTextoEtiquetado(res2);
			System.out.println(resultado);
			//----Extraindo os sintagmas nominais do texto etiquetado
			//List<SNData> lsns = JOgma.extraiSNOrdenadoTextoEtiquetado(res2);
			//NA LINHA ABAIXO****java.lang.StringIndexOutOfBoundsException: String index out of range: -1 at java.lang.String.substring(String.java:1955)
			List<SNData> lsns = JOgma.extraiSNIdentificadoIndiceOrdenado(aContext,resultado);
//			Vector<String> sns =new Vector<String>(hm.keySet());
//			Vector<Integer> snsind =new Vector<Integer>(hm.values());
//			List<SNData> lsns = new ArrayList<SNData>(SNData.converttoSNDataMap(hm).values());
			//sns contém os sintagmas nominais como aparecem no texto
			//TODO: Neste local pode ser incluido código para eliminar stopwords e lematizar ou reduzir ao radical
			//ver LemmaAnalyzer como exemplo
			//org.apache.lucene.analysis.pt contem stemmers para portugues
			Collections.sort(lsns);
			//Obtendo atributos dos sintagmas nominais
			//String[] chunks = sns.toArray(new String[sns.size()]);
			//Obtendo offsets e type (tag do SN)
			ArrayList resultTokens = new ArrayList();
			ArrayList resultTags = new ArrayList();
			ArrayList resultOffsets = new ArrayList();
			int endOffset= aContext.length();
			int index = 0;
			aContext = aContext.toLowerCase();
			int delta = -1;
			int iaprox = -1;
			for(SNData sni: lsns){
				String tempWord = sni.getSN().toLowerCase();
				if(tempWord.length()<=1 && this.punc.indexOf(tempWord)>=0)//if there is a letter or punc, pull.
					continue;
				//if(!this.stopWords.contains(tempWord) && this.openTag.get(chunks2[1])!=null && !Util.isNumber(tempWord)){
				//because we will use phrase filter, so we don't need to filter it here now.
				if(!SNAnalyser.isStopword(tempWord)){
					if(tempWord.length() > 1){
						//resultTags.add(chunks2[1]);
						int indexW = aContext.indexOf(tempWord,sni.getIndiceInicio());
						int initk = sni.getIndiceInicio();
						if(indexW < 0 || indexW > (initk+tempWord.length())){
							if(delta <= 0)
								delta = tempWord.length();
							iaprox = sni.getIndiceInicio() - delta;//-10 correcao de erro no indice devido a contracoes não substituidas
							iaprox = (iaprox>=0)?iaprox:0;
							
							indexW = aContext.indexOf(tempWord,iaprox);
							if(indexW < 0){
								indexW = aContext.indexOf(tempWord);
							}
						}

						if(indexW >=0 && indexW < aContext.length()){
							//***java.lang.IllegalArgumentException: startOffset must be non-negative, and endOffset must be >= startOffset, startOffset=-1,endOffset=40
							resultTokens.add(tempWord);
							resultTags.add("SN");
							index = indexW;
							endOffset = index+tempWord.length();
							resultOffsets.add(endOffset);
							index = endOffset;
							delta =  sni.getIndiceInicio() - indexW;
							//System.out.println("SNTokenizer.tokenize():"+tempWord+" start:"+indexW+" end: "+endOffset);
						}else
						{
							//TODO: reconstruindo os indices, dando pau!!!!!!!!!!!!!!!!!
							//problema na conversão de contrações não realizadas no texto original ou espaços em branco
							//tratar à, de este
							int e=-1, s=-1;
							String[] ss = tempWord.split(" ");
							int [] iss = new int[ss.length];
							iaprox = iaprox - sni.getSN().length();
							iaprox = (iaprox>=0)?iaprox:0;
							int eaprox = iaprox + 2* sni.getSN().length();
							eaprox = (eaprox<aContext.length())?eaprox:(aContext.length()-1);
							int indexaprox = iaprox;
							for(int i=0; i< ss.length; i++){
								iss[i] = aContext.indexOf(ss[i],indexaprox);
								if(ss[i].length() > 2 &&  iss[i]>=0 && iss[i] < eaprox)
								indexaprox = iss[i] + ss[i].length()-1;
								else{
									iss[i]=-1;
									indexaprox +=  ss[i].length();
								}
							}
													
							int indexfirst = -1;
							int indexend = - 1;
							int init = 0;
							int dist = 0;
							boolean distok = false;
							for(int i=1; i< ss.length; i++){
								dist = iss[i] - iss[i-1];
								distok = (dist > ss[i-1].length()) && (dist < 2* ss[i-1].length());
								if(iss[i] >= 0 && iss[i-1] >=0 &&  distok){
									if(indexfirst < 0)
										indexfirst = i-1;
									else if(indexend < i)
										indexend  = i;
								}else{
									if(iss[i-1] >=0 && iss[i] >=0 && !distok)
										iss[i-1]=-1;
									else if(iss[i-1] >=0 && indexfirst < 0)
										indexfirst = i-1;
									if(iss[i] >=0 && indexend < i)
											indexend  = i;
									
								}
							}
							
							if(indexfirst >= 0 && indexend>= indexfirst){
								//Reconstrua indice inicial
								s = iss[indexfirst];
								if(indexfirst != 0){
									int reali=0;
									for(int i=indexfirst-1; i >= 0; i--){
										s -= ss[i].length()+1;
										reali = aContext.indexOf(ss[i],s);
										if( reali != s && reali >= 0 )
											s = aContext.indexOf(ss[i],s);
//										else{
//										while(s>0 && aContext.charAt(s)==' ')
//											s--;
//										while(s>0 && Character.isLetter(aContext.charAt(s-1)))
//											s--;
//										}
									}
									indexfirst = 0;
									int tempi = aContext.indexOf(ss[indexfirst],s);
									if(s != tempi && tempi >= 0 && tempi < iss[indexend])
										s = aContext.indexOf(ss[indexfirst],s);
								
								}
								//Reconstrua indice final
								int maxe = s + sni.getSN().length();
								e = iss[indexend] + ss[indexend].length();
								e = (e>maxe)?maxe:e;
								if((indexend < ss.length-1) && indexend > 0){
									
//									for(int i=indexend; i < ss.length-1; i++){
//										if(e < maxe)
//										e += ss[i].length()+1;
//									}
									indexend = ss.length - 1;
									int tempi = aContext.indexOf(ss[indexend],e-2*ss[indexend].length());
									if(e != tempi && tempi >= 0){
										e = aContext.indexOf(ss[indexend],tempi);
										e += ss[indexend].length()-1;
									}
								}
								
								s = (s>=0)?s:(e-sni.getSN().length());
								e = (e<aContext.length() && e > 0 && e > s)?e:Math.min(aContext.length()-1, s+sni.getSN().length());
								
								
						String subs = aContext.substring(s,e+1);//*****StringIndexOutOfBounds
							int d = StringUtils.indexOfDifference(subs,tempWord);
							
							if (StringUtils.getLevenshteinDistance(subs, tempWord) <= 6){//espaços antes do sinal de pontuação tb
								delta =  sni.getIndiceInicio() - s; 
								index = s;
								if(index < 0)
									index = 0;
								endOffset = e;
								if(endOffset - subs.length() < 0)
									endOffset = subs.length()-1;
								//java.lang.IllegalArgumentException: startOffset must be non-negative, and endOffset must be >= startOffset, startOffset=-1,endOffset=40
								resultTags.add("SN");
								resultOffsets.add(endOffset);
								index = endOffset;
								resultTokens.add(subs);
							}else{
								System.out.println("SNTokenizerWithAtributes.tokenize(): Não encontrado: "+tempWord+ " Encontrado: "+subs);
								System.out.println("\n===>Não contornado!!!");
							}
						}
					}
				}
			}
		}
			HashMap tags = new HashMap();
			HashMap offsets = new HashMap();
			//formatando
			for(int i = 0;i<resultTokens.size();i++){
				//tokens.add((String)resultTokens.get(i));
				tags.put((String)resultTokens.get(i),(String)resultTags.get(i));
				offsets.put((String)resultTokens.get(i),(Integer)resultOffsets.get(i));
			}
			addToFinalToken(resultTokens, tags, offsets);

		}catch(Exception e){
			System.out.println("ERROR SNTokenizerWithAtributes.tokenize content:"+aContext+"\n"+e.getMessage());
			throw e;
		}


	}

	/**
	 * tokenize the context, filter out numbers and some stopwords.
	 * @param aContext
	 * @param tokens
	 * @param tags
	 */
	public void getContext(String aContext, ArrayList tokens, ArrayList tags){
	//separa tokens, tags e filtra stopwords	
		String[] chunks = aContext.split(" ");
		int i=0;
		for(;i<chunks.length;i++){
			if(chunks[i].length()>1){
				String[] chunks2 = chunks[i].split("/");//word/tag/lemma
				if(chunks2.length==3){
					String tempWord = chunks2[2].trim().toLowerCase();
					if(!SNAnalyser.isStopword(tempWord)){
						tokens.add(tempWord);
						tags.add(chunks2[1]);
					}
				}
			}
		}
	}

	

	/**
	 * Because it may has -1, so it is in the form of a,b;c,d;e,f
	 * @param offset
	 * @return
	 */
	public static int[] parseOffset(String offset){
		int [] offsets = new int[6];
		String[] chunks = offset.split(";");
		for(int i=0;i<3;i++){
			String[] chunks2 = chunks[i].split(",");
			offsets[2*i] = Integer.parseInt(chunks2[0]);
			offsets[2*i+1]=Integer.parseInt(chunks2[1]);
		}
		return offsets;
	}

	public static int[] parseOffsetPair(String offset){
		int[] offsets = new int[4];
		String[] chunks = offset.split(";");
		for(int i=0;i<chunks.length;i++){
			String[] chunks2 = chunks[i].split("-");
			offsets[2*i] = Integer.parseInt(chunks2[0]);
			offsets[2*i+1]=Integer.parseInt(chunks2[1]);
		}
		return offsets;
	}

	/**
	 * Get the more previous start offset
	 * @param pre
	 * @param current
	 * @return
	 */
	int getStart(int pre, int current){
		int start=current;
		if(pre!=start){
			if(start==-1){
				if(pre!=-1)
					start = pre;
			}else{
				if(pre!=-1 && pre<start)
					start = pre;
			}
		}
		return start;
	}

	/**
	 * Get the bigger range offset.
	 * @param pre
	 * @param current
	 * @return
	 */
	int getEnd(int pre, int current){
		int end = current;
		if(pre!=end){
			if(end==-1){
				if(pre!=-1)
					end = pre;
			}else{
				if(pre!=-1 && pre>end)
					end = pre;
			}
		}
		return end;
	}

	boolean isOverlap(int start1, int start2, int end1, int end2){
		if(start1<=start2 && start2<=end1)
			return true;
		if(start2<=start1 && start1<=end2)
			return true;
		return false;
	}

}
	


