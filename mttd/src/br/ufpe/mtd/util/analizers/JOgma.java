package br.ufpe.mtd.util.analizers;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.Vector;




public class JOgma {


//Utilizado por SNTokenizer e indiretamente por SNAnalyzer, e Etiquetar.jsp
//TODO: comece aperfeiçoar por este método, ele é chamado para extrair SN
//integrar pypln, palavras, nlpnet, taspardo no aperfeiçoamento do etiquetador (arquivos ogma)
//TODO: tratamento de nomes pessoais, remoção de VB VP prep art, suporte a quebra de sns aninhados, remoção de sns stopwords
	public static String identificaSNTextoEtiquetado(String frase)
	{
		String rst;
		String palavra;
		System.out.println("Executando identificaSNTextoEtiquetado(String frase) modificado...");
		rst = "";
		// Normaliza as tags para o padrão ED-CER
		frase = frase.replace("/AD", "/AR");
		frase = frase.replace("/AI", "/AR");
		//frase = frase.replace("/VP", "/AJ"); //adiado
		frase = frase.replace("/NC", "/NU");
		frase = frase.replace("/NM", "/NU");
		frase = frase.replace("/NO", "/NU");
		frase = frase.replace("/NR", "/NU");
		frase = frase.replace("/NU", "/ct");//forçando números a não fazer parte de sns
		frase = frase.replace("/VG", "/ct");//para não disparar regra com vírgulas, incluindo-as nos sns
		frase = frase.replace("/CJ", "/CO");
		frase = frase.replace("/PL", "/LG");
		
		//expressões comuns
		frase = frase.replace(" por/PR meio/SU de/PR", " por_meio_de/ct"); //eliminar por meio de
		frase = frase.replace(" em/PR função/SU de/PR", " em_função_de/ct"); //eliminar por meio de
		frase = frase.replace(" em/PR relação/SU", " em_relação/ct");
		frase = frase.replace(" melhor/AJ forma/SU", " melhor_forma/ct"); //eliminar por meio de
		frase = frase.replace(" tema/SU de/PR", " tema_de/ct");
		frase = frase.replace(" interesse/SU para/PR", " interesse_para/ct");
		frase = frase.replace(" questão/SU de/PR", " questão_de/ct");
		frase = frase.replace(" mais/AV influente/SU", " mais_influente/ct");
		frase = frase.replace(" inerente/SU a/PR", " inerente_a/ct");
		frase = frase.replace(" objetivo/SU de/PR", " objetivo_de/ct");
		frase = frase.replace(" experiência/SU de/PR", " experiência_de/ct");
		frase = frase.replace(" reorganização/SU de/PR", " reorganização_de/ct");
		frase = frase.replace(" grande/AJ medida/SU", " grande_medida/ct");
		frase = frase.replace(" envolvidas/AJ em/PR", " envolvidas_em/ct");
		frase = frase.replace(" implicações/SU em/PR", " implicações_em/ct");
		frase = frase.replace(" implicações/SU de/PR", " implicações_de/ct");
		frase = frase.replace(" relações/SU com/PR", " relações_com/ct");
		frase = frase.replace(" cada/de vez/SU", " cada_vez/ct");
		frase = frase.replace(" de/PR vista/SU", " de_vista/ct");
		frase = frase.replace(" em/PR vista/SU", " em_vista/ct");
		frase = frase.replace(" capaz/AJ de/PR", " capaz_de/ct");
		frase = frase.replace(" foco/SU de/PR", " foco_de/ct");
		frase = frase.replace(" forma/SU em/PR", " forma_em/ct");
		frase = frase.replace(" contexto/SU de/PR", " contexto_de/ct");
		frase = frase.replace(" interação/SU entre/PR", " interação_entre/ct");
		frase = frase.replace(" ponto/SU de/PR vista/SU", " ponto_de_vista/ct");
		frase = frase.replace(" objetivo/SU", " objetivo/ct");
		
		frase = frase.replace(" a/PR a/AR", " à/PR"); //tratamento de crase
		frase = frase.replace(" a/PR as/AR", " às/PR");
		frase = frase.replace(" a/PR o/AR", " ao/PR");//tratamento de ao
		//frase = frase.replace("como/PR", "como/ct");//incluido abaixo com demais preposições
		
		
		//ADAPTAÇÂO DO MODO SELETOR DO METODO ED-CER
		//Inicia analise
		frase = frase.replace("/LG", "/ct");
		//frase = frase.replace("/VB", "/ct"); //adiando
		frase = frase.replace("/PN", "/ct");
		int alterado = 1;
		while (alterado == 1)
		{
			alterado = 0;
			frase = frase.trim();
			String[] palavras = frase.split(" ");
			int npalavras = palavras.length;
			int ppalavras = 0;
			String esquerda = "";
			while (ppalavras < npalavras)
			{
				palavra = palavras[ppalavras];
				if (!palavra.equals(""))
				{
					//janela de quatro palavras
					//separa palavra da etiqueta
					String[] PC0 = palavras[ppalavras].split("/");
					String[] PC1 = { "", "" };
					String[] PC2 = { "", "" };
					String[] PC3 = { "", "" };

					esquerda = esquerda + " " + PC0[0];
					if (ppalavras + 1 < npalavras) PC1 = palavras[ppalavras +1].split("/");
					if (ppalavras + 2 < npalavras) PC2 = palavras[ppalavras +2].split("/");
					if (ppalavras + 3 < npalavras) PC3 = palavras[ppalavras +3].split("/");

					if((PC0[1].equals("NP")|| PC0[1].equals("SU")) && JOgmaEtiquetador.getInstance().isStopword(PC0[0].toLowerCase()))
						//adicionado tratamento de início de frase
					{
						esquerda = esquerda + "/ct";// 
					}else if(PC0[1].equals("AJ")&& PC0[0].matches("[A-ZÀ-Úa-zà-ú]+(a|i)d(a|o)(s)*"))
						//adicionado tratamento proveniente de, voltada para, reconhecido por
					//é necessário verificar se pc0 é verbo tirando o sufixo
					{
						esquerda = esquerda + "/VP";// 
					}else
					
					if (PC0[1].equals("AJ")&& PC0[0].matches("[A-ZÀ-Úa-zà-ú]+(a|o|i|e)nte(s)*") && PC1[1].equals("PR"))
						//adicionado tratamento proveniente de, referente a, existente em, participante de
					//é necessário verificar se pc0 é verbo tirando o sufixo------------------------------------
					{
						esquerda = esquerda + "_" + PC1[0] + "/ct";// 
						ppalavras++;//avança
					}else if(PC0[1].equals("AJ")&& PC0[0].matches("[A-ZÀ-Úa-zà-ú]+(a|i)d(a|o)(s)*") && (PC1[1].equals("PR")||PC1[1].equals("de")))
						//adicionado tratamento proveniente de, voltada para, reconhecido por
					//é necessário verificar se pc0 é verbo tirando o sufixo
					{
						esquerda = esquerda + "_" + PC1[0] + "/ct";// 
						ppalavras++;//avança
					}
					else
					if (PC0[1].equals("AJ")&& PC1[1].equals("CO") && PC2[1].equals("AJ") && !PC2[0].matches("[A-ZÀ-Úa-zà-ú]+(a|i)d(a|o)(s)*"))
							//adicionado tratamento proveniente de adjetivos múltiplos, 
						{
							esquerda = esquerda + "_" + PC1[0] + "_" + PC2[0] +"/AJ";// 
							ppalavras++;//avança
							ppalavras++;//avança
					}else
					if (PC0[1].equals("NP") && PC1[1].equals("NP")|| 
							PC0[1].equals("NP") && PC1[1].equals("SU")||
							PC0[1].equals("SU") && PC1[1].equals("NP") ||
							PC0[1].equals("SU") && PC1[1].equals("SU"))
						//adicionado tratamento NP e VP----deve detectar se início de frase
					{
						esquerda = esquerda + "=" + PC1[0] + "/NP";
						ppalavras++;//avança
					}else if (PC0[1].equals("NP") && PC1[1].equals("NP") && Character.isUpperCase(PC0[0].charAt(0)) && Character.isUpperCase(PC1[0].charAt(0)))//adicionado tratamento NP
					{
						esquerda = esquerda + "=" + PC1[0] + "/NP";//Qual o nível deste SN?? Ficará igual a zero.
						ppalavras++;//avança
					}else if (PC0[1].equals("NP") && "de do dos da das em e".contains(PC1[0]) && PC2[1].equals("NP") && Character.isUpperCase(PC0[0].charAt(0)) && Character.isUpperCase(PC2[0].charAt(0)))//adicionado tratamento NP
					{
						esquerda = esquerda + "=" +  PC1[0] + "=" + PC2[0] + "/NP";//Qual o nível deste SN?? Ficará igual a zero.
						ppalavras++;//avança
						ppalavras++;//avança
					}else if (PC0[1].startsWith("NP") && PC1[1].equals("PR") && PC2[1].equals("AR") && PC3[1].startsWith("NP") && Character.isUpperCase(PC0[0].charAt(0)) && Character.isUpperCase(PC3[0].charAt(0))) 
					{	//
						esquerda = esquerda + "=" + PC1[0] + "=" + PC2[0] + "=" + PC3[0] + "/NP";//Qual o nível deste SN?? Ficará igual a zero.
						ppalavras++;
						ppalavras++;
						ppalavras++;
					}else if (PC0[1].equals("VB") && ((PC1[1].equals("VP"))||(PC1[1].equals("AJ") && PC1[0].matches("[A-ZÀ-Úa-zà-ú]+(a|o|i)d(o|a)(s)*"))))//adicionado por mim por causa de "foram estudadas"
					{
						esquerda = esquerda + "_" + PC1[0] + "/ct";// 
						ppalavras++;//avança
					}else if (PC0[1].equals("VB") && (PC1[1].equals("AV")))//adicionado por mim por causa de "foram estudadas"
					{
						esquerda = esquerda + "_" + PC1[0] + "/ct";// 
						ppalavras++;//avança
					}else if (PC0[1].equals("AV") && (PC1[1].equals("VP")))//adicionado por mim por causa de "foram estudadas"
					{
						esquerda = esquerda + "_" + PC1[0] + "/ct";// 
						ppalavras++;//avança
					}else if ((PC0[1].equals("AV")|| PC0[1].equals("ct")) && (PC1[1].equals("AJ")))//adicionado por mim por causa de "menos compreendidas questões"
					{
						esquerda = esquerda + "_" + PC1[0] + "/ct";// 
						ppalavras++;//avança
					}else if (PC0[1].equals("ct") && (PC1[1].equals("de")|| PC1[1].equals("AJ")|| PC1[1].equals("AV")||PC1[1].equals("ct")|| PC1[1].equals("AR")||PC1[1].equals("PR")||PC1[1].equals("PI")||PC1[1].equals("PD")||PC1[1].equals("PP")||PC1[1].equals("PS")))//adicionado por mim por causa de "foram estudadas"
					{
						esquerda = esquerda + "_" + PC1[0] + "/ct";// 
						ppalavras++;//avança
					}else if (PC1[1].equals("ct") && (PC0[1].equals("de")|| PC0[1].equals("AV")||PC0[1].equals("ct")||PC0[1].equals("AR")||PC0[1].equals("PR")||PC0[1].equals("PI")||PC0[1].equals("PD")||PC0[1].equals("PP")||PC0[1].equals("PS")))//adicionado por mim por causa de "foram estudadas"
					{
						esquerda = esquerda + "_" + PC1[0] + "/ct";// 
						ppalavras++;//avança
					}else if (PC0[1].equals("VP") && (PC1[1].equals("AV") || PC1[1].equals("VB") || PC1[1].equals("AR") || PC1[1].equals("PR")))//adicionado por mim por causa de "foram estudadas"
					{
						esquerda = esquerda + "_" + PC1[0] + "/ct";// 
						ppalavras++;//avança
					}else if (PC0[1].equals("VP") && PC1[1].equals("AV") && (PC2[1].equals("AR") || PC2[1].equals("PR")))//adicionado por mim por causa de "localizadas geograficamente em"
					{
						esquerda = esquerda + "_" + PC1[0] + "_" + PC2[0] +"/ct";// 
						ppalavras++;//avança
						ppalavras++;//avança
					}else if (!(PC0[1].equals("NP")|| PC0[1].equals("SU")|| PC0[1].equals("AJ")) && PC1[1].equals("AJ") && (PC2[1].equals("AR") || PC2[1].equals("PR")|| PC2[1].equals("de")|| PC2[1].equals("PI")|| PC2[1].equals("PD")|| PC2[1].equals("PS")|| PC2[1].equals("PP")))//adicionado por mim por causa de "localizadas geograficamente em"
					{
						esquerda = esquerda + "_" + PC1[0] + "_" + PC2[0] +"/ct";// 
						ppalavras++;//avança
						ppalavras++;//avança
					}else if (PC0[1].equals("PR") && !("de em".contains(PC0[0]) && "a o os as".contains(PC1[0])) && PC1[1].equals("AR"))//adicionado regra de normalização: de sua, sobre a, a o, tbci tem alguns termos com ao, mas que podem ser quebrados serviço de auxílio ao, quanto ao 
					{
						esquerda = esquerda + "_" +  PC1[0] + "/ct";//.
						ppalavras++;//avança
				
					}else if (PC0[1].equals("PR") && ("como sobre pela pelas pelo".contains(PC0[0]))) // regra de normalização: pela pelas pelo (pelos tem no tbci, sobre e como tem um) tbci só tem termo: entidade coletiva como autor
					{
						esquerda = esquerda + "/ct";//.
				
					}else if (PC0[1].equals("AR") && (PC1[1].equals("AR")||PC1[1].equals("de")|| PC1[1].equals("PP")||PC1[1].equals("PD")||PC1[1].equals("PI")||PC1[1].equals("PS")))//adicionado regra de normalização: de sua, sobre a
					{
						esquerda = esquerda + "_" +  PC1[0] + "/ct";//.
						ppalavras++;//avança
				
					}else if ((PC0[1].equals("PR")||PC0[1].equals("AR")||PC0[1].equals("ct")) && (PC1[1].equals("VP")|| PC1[1].equals("de")|| PC1[1].equals("PP")||PC1[1].equals("PD")||PC1[1].equals("PI")||PC1[1].equals("PS")))//adicionado regra de normalização: de sua, sobre a
					{
						esquerda = esquerda + "_" +  PC1[0] + "/ct";//.
						ppalavras++;//avança
				
					}else if ((PC1[1].equals("PR")|| PC1[1].equals("AR")) && (PC0[1].equals("PP")||PC0[1].equals("PD")||PC0[1].equals("PI")||PC0[1].equals("PS")))//adicionado regra de normalização: de sua, sobre a
					{
						esquerda = esquerda + "_" +  PC1[0] + "/ct";//.
						ppalavras++;//avança
				
					}else if (PC0[1].equals("de")|| PC0[1].equals("PP")||PC0[1].equals("PD")||PC0[1].equals("PI")||PC0[1].equals("PS"))//adicionado regra de normalização: de sua, sobre a
					{
						esquerda = esquerda + "/ct";//.
					
				
					}else if (PC0[1].equals("CO") && (PC1[1].equals("VP")|| PC1[1].equals("de")|| PC1[1].equals("PR")|| PC1[1].equals("AR")||PC1[1].equals("PP")||PC1[1].equals("PD")||PC1[1].equals("PI")||PC1[1].equals("PS")))//adicionado regra de normalização: de sua, sobre a
					{
						esquerda = esquerda + "_" +  PC1[0] +  "/ct";//.
						ppalavras++;//avança
					}
					else//--------------		
						//regras ED-CER
						if (PC0[1].equals("AV") && PC1[1].equals("ct"))
						{
							esquerda = esquerda + "_" + PC1[0] + "/ct";
							ppalavras++;//avança
						}
						else//adicionado para sn não começar com adv seguido de artigo
							if (PC0[1].equals("AV") && PC1[1].equals("AR"))
							{
								esquerda = esquerda + "/ct";
								//ppalavras++;//avança
							}
							else
								if ((PC0[1].equals("CO")) && (PC1[1].equals("ct")))
								{
									esquerda = esquerda + "_" + PC1[0] + "/ct";
									ppalavras++;
								}
								else
									if ((PC0[1].equals("AR")) && (PC1[1].equals("ct")))
									{
										esquerda = esquerda + "_" + PC1[0] + "/ct";
										ppalavras++;
									}
									else
										if ((PC0[1].equals("NU")) && (PC1[1].equals("ct")))
										{
											esquerda = esquerda + "_" + PC1[0] + "/ct";
											ppalavras++;
										}
										else
											if ((PC0[1].equals("ct")) && (PC1[1].equals("PR")))
											{
												esquerda = esquerda + "_" + PC1[0] + "/ct";
												ppalavras++;
											}
											else
												if ((PC0[1].equals("ct")) && (PC1[1].equals("CO")))
												{
													esquerda = esquerda + "_" + PC1[0] +"/ct";
													ppalavras++;
												}
												else
													esquerda = esquerda + "/" + PC0[1];
				}
				ppalavras++;
			}
			esquerda = esquerda.trim();
			if (!frase.equals(esquerda)) alterado = 1;
			frase = esquerda;
		}
		System.out.println("Identifica SN...");

		frase = frase.replace("/VP", "/AJ");
		frase = frase.replace("/VB", "/ct");
		//ADAPTAÇÂO DO MODO ANALISADOR DO METODO ED-CER
		// Inicia gramatica
		frase = frase.replace("/AR","/de");
		frase = frase.replace("/PD","/de");
		frase = frase.replace("/PI","/de");
		frase = frase.replace("/AJ","/MD");
		frase = frase.replace("/NU","/MD");
		frase = frase.replace("/PS","/MD");
		frase = frase.replace("/CO","/co");
		frase = frase.replace("/PR","/pr");
		frase = frase.replace("/PP","/de"); // Modificação no ED-CER antes PP -> re
		frase = frase.replace("/SU","/re");
		frase = frase.replace("/NP","/re");
		//Incia analise
		frase = frase.replace("/re", "/NS");//***regra SN1a
		alterado = 1;
		while (alterado == 1)
		{
			alterado = 0;
			frase = frase.trim();
			String[] palavras = frase.split(" ");

			int npalavras = palavras.length;
			int ppalavras = 0;
			String esquerda = "";
			while (ppalavras < npalavras)
			{
				palavra = palavras[ppalavras];
				if (!palavra.equals(""))//(palavra != "")
				{
					//define janela
					String[] PC0 = palavras[ppalavras].split("/");
					String[] PC1 = {"",""};
					String[] PC2 = {"",""};
					String[] PC3 = {"",""};
					esquerda = esquerda + " " + PC0[0];
					if (ppalavras+1<npalavras) PC1 = palavras[ppalavras+1].split("/");
					if (ppalavras+2<npalavras) PC2 = palavras[ppalavras+2].split("/");
					if (ppalavras+3<npalavras) PC3 = palavras[ppalavras+3].split("/");
					//regras ED-CER
					if ((PC0[1].equals("AV")) && (PC1[1].equals("AV")))
					{
						esquerda = esquerda + "_" + PC1[0] + "/AV";
						ppalavras++;
					} else
						if ((PC0[1].equals("AV")) && (PC1[1].equals("MD")))
						{
							esquerda = esquerda + "_" + PC1[0] + "/MD";
							ppalavras++;
						} else
							if ((PC0[1].equals("MD")) && (PC1[1].equals("co")) && (PC2[1].equals("MD")))
							{
								esquerda = esquerda + "_" + PC1[0] + "_" + PC2[0] + "/MD";
								ppalavras++;
								ppalavras++;
							} else//adicionei esta para os casos: o projeto internacional math-net, o software horizon
								if (PC0[1].startsWith("NS") && PC1[1].startsWith("NS")) //***SN2 
								{
									//nivel 2 em diante
									String sn = PC0[0] + "_" + PC1[0];
									esquerda = esquerda + "_" + PC1[0]  + "/NS";
									ppalavras++;

								}else
									if ((PC0[1].equals("NS")) && (PC1[1].equals("MD")))//***SN1b
									{
										esquerda = esquerda + "_" + PC1[0] + "/NS";
										ppalavras++;
									} else
										if ((PC0[1].equals("MD")) && (PC1[1].equals("NS")))//***SN1b
										{
											esquerda = esquerda + "_" + PC1[0] + "/NS";
											ppalavras++;
										} else
											if ((PC0[1].equals("NS")) && (PC1[1].equals("pr")) && (PC2[1].equals("NS"))) //***SN2 
											{
												esquerda = esquerda + "_" + PC1[0] + "_" + PC2[0] + "/NS";
												ppalavras++;
												ppalavras++;
											} else
												if ((PC0[1].equals("NS")) && (PC1[1].equals("pr")) && (PC2[1].equals("de") && (PC3[1].equals("NS")))) //***SN3 
												{
													esquerda = esquerda + "_" + PC1[0] + "_" + PC2[0] + "_" +
															PC3[0] + "/NS";
													ppalavras++;
													ppalavras++;
													ppalavras++;
												} else
													if ((PC0[1].equals("NS")) && (PC1[1].equals("VG")) && (PC2[1].equals("MD")))//***?
													{
														esquerda = esquerda + "_" + PC1[0] + "_" +
																PC2[0] + "/NS";
														ppalavras++;
														ppalavras++;
													} else
														if ((PC0[1].equals("NS")) && (PC1[1].equals("VG")) && (PC2[1].equals("de")) && (PC3[1].equals("MD")))//***?
														{
															esquerda = esquerda + "_" + PC1[0] + "_" + PC2[0] + "_"
																	+ PC3[0] + "/NS";
															ppalavras++;
															ppalavras++;
															ppalavras++;
														}
														else
															if ((PC0[1].equals("NS")) && (PC1[1].equals("co")) && (PC2[1].equals("NS"))) //***SN3,4,5+
															{
																esquerda = esquerda + "_" + PC1[0] + "_" + PC2[0] + "/NS";
																ppalavras++;
																ppalavras++;
															} else
																if ((PC0[1].equals("NS")) && (PC1[1].equals("co")) &&
																		(PC2[1].equals("de")) && (PC3[1].equals("NS"))) //***SN3,4,5+
																{
																	esquerda = esquerda + "_" + PC1[0] + "_" +
																			PC2[0] + "_" + PC3[0] + "/NS";
																	ppalavras++;
																	ppalavras++;
																	ppalavras++;
																} else
																	if ((PC0[1].equals("AV")) && (PC1[1].equals("NS")))//***SN3,4,5+
																	{
																		esquerda = esquerda + "_" + PC1[0] + "/NS";
																		ppalavras++;
																	} else
																		if ((PC0[1].equals("de")) && (PC1[1].equals("NS")))//***SN3,4,5+
																		{
																			esquerda = esquerda + "_" + PC1[0] + "/NS";
																			ppalavras++;
																		}
																		else
																		{
																			esquerda = esquerda + "/" + PC0[1];
																		}
				}
				ppalavras++;
			}
			esquerda = esquerda.trim();
			if (!frase.equals(esquerda)) alterado = 1;
			frase = esquerda;
		}
		frase = frase.replace("/NS", "/SN");
		rst = frase;
		return rst;
	}

	//TODO: Verificar regras, se é bom contemplar
	//NP AV VP | PR demandas não atendidas de
	//NP VP AV | PR área reconhecida geograficamente como
	//TODO: niv de NP está zero, incrementar nivel em cada regra
	//Guardar para cada sn: posição, nivel sn, num palavras, freq, rank
	public static Vector<Vector<SNData>> idCatSNTextoEtiquetado2(String frase)
	{
		//TODO: modificando disparo das regras
		String rst;
		String palavra;
		rst = "";
		int[] freqregras = new int[10];
		frase = frase.trim();


		Vector<Vector<SNData>> sns =new Vector<Vector<SNData>>( );
		sns.add(new Vector<SNData>());//0 SN1A
		sns.add(new Vector<SNData>());//1 SN1B
		sns.add(new Vector<SNData>());//2 SN2
		sns.add(new Vector<SNData>());//3 SN3
		sns.add(new Vector<SNData>());//4 SN4
		sns.add(new Vector<SNData>());//5 SN5+

		//Remove vírgulas das tags
		frase = frase.replace("/VG", "/PN");//adicionado para impedir disparo de regras com vírgula

		// Normaliza as tags para o padrão ED-CER
		frase = frase.replace("/AD", "/AR");
		frase = frase.replace("/AI", "/AR");
		frase = frase.replace("/NC", "/NU");
		frase = frase.replace("/NM", "/NU");
		frase = frase.replace("/NO", "/NU");
		frase = frase.replace("/NR", "/NU");
		//frase = frase.replace("/VG", "/CO");//seria útil apenas no nome de instituições
		frase = frase.replace("/CJ", "/CO");
		frase = frase.replace("/PL", "/LG");


		//ADAPTAÇÂO DO MODO SELETOR DO METODO ED-CER
		//Inicia analise
		frase = frase.replace("/LG", "/ct");
		frase = frase.replace("/VB", "/ct");//TODO: tratar o complemento verbal
		frase = frase.replace("/PN", "/ct");
		int alterado = 1;
		while (alterado == 1)
		{
			alterado = 0;
			frase = frase.trim();
			String[] palavras = frase.split(" ");
			int npalavras = palavras.length;
			int ppalavras = 0;
			String esquerda = "";
			while (ppalavras < npalavras)
			{
				palavra = palavras[ppalavras];
				if (!palavra.equals(""))
				{
					//janela de quatro palavras
					//separa palavra da etiqueta
					String[] PC0 = palavras[ppalavras].split("/");
					String[] PC1 = { "", "" };
					String[] PC2 = { "", "" };
					String[] PC3 = { "", "" };

					esquerda = esquerda + " " + PC0[0];
					if (ppalavras + 1 < npalavras) PC1 = palavras[ppalavras +1].split("/");
					if (ppalavras + 2 < npalavras) PC2 = palavras[ppalavras +2].split("/");
					if (ppalavras + 3 < npalavras) PC3 = palavras[ppalavras +3].split("/");

					if (PC0[1].equals("NP") && PC1[1].equals("NP") && Character.isUpperCase(PC0[0].charAt(0)) && Character.isUpperCase(PC1[0].charAt(0)))//adicionado tratamento NP
					{
						esquerda = esquerda + "=" + PC1[0] + "/NP";//Qual o nível deste SN?? Ficará igual a zero.
						ppalavras++;//avança
					}else if (PC0[1].equals("NP") && "de do dos da das em e".contains(PC1[0]) && PC2[1].equals("NP") && Character.isUpperCase(PC0[0].charAt(0)) && Character.isUpperCase(PC2[0].charAt(0)))//adicionado tratamento NP
					{
						esquerda = esquerda + "=" +  PC1[0] + "=" + PC2[0] + "/NP";//Qual o nível deste SN?? Ficará igual a zero.
						ppalavras++;//avança
						ppalavras++;//avança
					}else if (PC0[1].startsWith("NP") && PC1[1].equals("PR") && PC2[1].equals("AR") && PC3[1].startsWith("NP") && Character.isUpperCase(PC0[0].charAt(0)) && Character.isUpperCase(PC3[0].charAt(0))) 
					{	//
						esquerda = esquerda + "=" + PC1[0] + "=" + PC2[0] + "=" + PC3[0] + "/NP";//Qual o nível deste SN?? Ficará igual a zero.
						ppalavras++;
						ppalavras++;
						ppalavras++;
					}else if (PC0[1].equals("ct") && (PC1[1].equals("VP")))//adicionado por mim por causa de "foram estudadas"
					{
						esquerda = esquerda + "_" + PC1[0] + "/ct";// 
						ppalavras++;//avança
					}else if (PC0[1].equals("VP") && (PC1[1].equals("PR")||PC1[1].equals("AR")))//adicionado por mim por causa de "metadados baseado"
						{
							esquerda = esquerda + "/ct";//+"_" + PC1[0] 
							//ppalavras++;//avança
						}
						
						//regras ED-CER
					else if (PC0[1].equals("AV") && PC1[1].equals("ct"))
						{
							esquerda = esquerda + "_" + PC1[0] + "/ct";
							ppalavras++;//avança
						}else if (PC0[1].equals("AV") && PC1[1].equals("PRP"))//adicionei mas não sei se efetivo
						{
							esquerda = esquerda + "_" + PC1[0] + "/ct";
							ppalavras++;//avança
						}
						
						else if (PC0[1].equals("AV") && PC1[1].equals("AR"))
							{
								esquerda = esquerda + "/ct";
								//ppalavras++;//avança
							}
							else
								if ((PC0[1].equals("CO")) && (PC1[1].equals("ct")))
								{
									esquerda = esquerda + "_" + PC1[0] + "/ct";
									ppalavras++;
								}
								else
									if ((PC0[1].equals("AR")) && (PC1[1].equals("ct")))
									{
										esquerda = esquerda + "_" + PC1[0] + "/ct";
										ppalavras++;
									}
									else
										if ((PC0[1].equals("NU")) && (PC1[1].equals("ct")))
										{
											esquerda = esquerda + "_" + PC1[0] + "/ct";
											ppalavras++;
										}
										else
											if ((PC0[1].equals("ct")) && (PC1[1].equals("PR")))
											{
												esquerda = esquerda + "_" + PC1[0] + "/ct";
												ppalavras++;
											}
											else
												if ((PC0[1].equals("ct")) && (PC1[1].equals("CO")))
												{
													esquerda = esquerda + "_" + PC1[0] +"/ct";
													ppalavras++;
												}
												else
													esquerda = esquerda + "/" + PC0[1];
				}
				ppalavras++;
			}
			esquerda = esquerda.trim();
			if (!frase.equals(esquerda)) alterado = 1;
			frase = esquerda;
		}
		
		

		//ADAPTAÇÂO DO MODO ANALISADOR DO METODO ED-CER
		// Inicia gramatica
		frase = frase.replace("/VP", "/AJ");//verbo no participio para adjetivo //movi para cá para filtrar VP seguindo de PREP
		frase = frase.replace("/AR","/de");
		frase = frase.replace("/PD","/de");
		frase = frase.replace("/PI","/de");
		frase = frase.replace("/AJ","/MD");//adj e vp viram MD
		frase = frase.replace("/NU","/ct");//numero viram MD //altrerei de /MD para /ct
		frase = frase.replace("/PS","/MD");//pronome possessivo viram MD
		frase = frase.replace("/CO","/co");
		frase = frase.replace("/PR","/pr");
		frase = frase.replace("/SU","/re");
		frase = frase.replace("/PP","/ct"); //mudei para ct, PP -> de Segundo Maia Modificação sobre o ED-CER (PP -> re)
		frase = frase.replace("/NP","/re");
		//Inicia analise
		frase = frase.replace("/re", "/NS0");//***regra SN1a, requer adaptação no cálculo do nivel dos sns superiores

		String[] palavras = frase.split(" ");
		String[] etiquetas = new String[palavras.length];
		String [] temp = frase.split("/| ");
		int nivelsn=0;
		int offset = 0;
		for(int i=0;i<temp.length;i+=2){
			etiquetas[i/2] = temp[i+1];
			if(temp[i+1].startsWith("NS")){
				nivelsn = Integer.parseInt(temp[i+1].substring(2));//tags de tamanho 2
				int index = sns.get(nivelsn).indexOf(new SNData(temp[i],0));
				//Adicionando SN1a
				if(index >= 0){//sn já na lista
					sns.get(nivelsn).get(index).addPosition(offset);//base para calculo do offset dos sns de nivel superior

				}else{//sn não está na lista
					index = sns.get(nivelsn).size();
					sns.get(nivelsn).add(index,new SNData(temp[i],frase.indexOf(temp[i],offset)));//define offset

				}
			}
			offset = offset + temp[i].length()+ temp[i+1].length() + 2;//sn + taglength + 1 barra + 1 espaço em branco 
		}

		//regras envolvendo Modificadores
		//permitiu encontrar opções_tecnológicas_e_metodológicas (o que só seria possível com SN -> SN co MD)
		//artigos/SN0 de/pr periódicos/MD, não classificado como sn
		//remover " da frase original
		//acabou impedindo o encontro de "recursos informacionais" de (recursos informacionais eletrônicos, recursos informacionais brasileiros)
		alterado = 1;
		while (alterado == 1)
		{
			alterado = 0;
			frase = frase.trim();
			palavras = frase.split(" ");

			int npalavras = palavras.length;
			int ppalavras = 0;
			String esquerda = "";
			while (ppalavras < npalavras)
			{
				palavra = palavras[ppalavras];

				if (!palavra.equals(""))//(palavra != "")
				{
					//define janela
					String[] PC0 = palavras[ppalavras].split("/");
					String[] PC1 = {"",""};
					String[] PC2 = {"",""};
					String[] PC3 = {"",""};
					esquerda = esquerda + " " + PC0[0];
					if (ppalavras+1<npalavras) PC1 = palavras[ppalavras+1].split("/");
					if (ppalavras+2<npalavras) PC2 = palavras[ppalavras+2].split("/");
					if (ppalavras+3<npalavras) PC3 = palavras[ppalavras+3].split("/");
					//regras ED-CER
					if ((PC0[1].equals("AV")) && (PC1[1].equals("AV")))
					{
						esquerda = esquerda +"_" +  PC1[0] + "/AV";
						ppalavras++;
					} else
						//descartado por gerar efeito colateral indesejado
//						if ((PC0[1].equals("MD")) && (PC1[1].equals("MD")))//adicionei para o caso de bibliografia internacional seletiva
//						{
//							esquerda = esquerda + "_" + PC1[0] + "/MD";
//							ppalavras++;
//						} else
							if ((PC0[1].equals("AV")) && (PC1[1].equals("MD")))
							{
								esquerda = esquerda + "_" + PC1[0] + "/MD";
								ppalavras++;
							} else
								if ((PC0[1].equals("MD")) && (PC1[1].equals("co")) && (PC2[1].equals("MD")))
								{
									esquerda = esquerda + "_" + PC1[0] + "_" + PC2[0] + "/MD";
									ppalavras++;
									ppalavras++;
								} else
								{
									esquerda = esquerda + "/" + PC0[1];
								}
				}
				ppalavras++;
			}
			esquerda = esquerda.trim();
			if (!frase.equals(esquerda)) alterado = 1;
			frase = esquerda;
		}


		//regras envolvendo Modificadores ligados a substantivo
		System.out.println("=-=regras envolvendo Modificadores ligados a substantivo=-=");
		alterado = 1;
		while (alterado == 1)
		{
			alterado = 0;
			frase = frase.trim();
			palavras = frase.split(" ");

			int npalavras = palavras.length;
			int ppalavras = 0;
			String esquerda = "";
			while (ppalavras < npalavras)
			{
				palavra = palavras[ppalavras];

				if (!palavra.equals(""))//(palavra != "")
				{
					//define janela
					String[] PC0 = palavras[ppalavras].split("/");
					String[] PC1 = {"",""};
					String[] PC2 = {"",""};
					String[] PC3 = {"",""};
					esquerda = esquerda + " " + PC0[0];
					if (ppalavras+1<npalavras) PC1 = palavras[ppalavras+1].split("/");
					if (ppalavras+2<npalavras) PC2 = palavras[ppalavras+2].split("/");
					if (ppalavras+3<npalavras) PC3 = palavras[ppalavras+3].split("/");
					//regras ED-CER
					//adicionado para o caso projeto/SN internacional/MD Math-Net/SN
					if ((PC0[1].startsWith("NS")) && (PC1[1].equals("MD") && PC2[1].startsWith("NS")))//***SN2+
					{
						//regra altera o nível do SN
						nivelsn = Integer.parseInt(PC2[1].substring(2));//tags de tamanho 2
						int nant = Integer.parseInt(PC0[1].substring(2));
						/*if(nivelsn == 0)
							nivelsn = nivelsn + 1;//neste caso colocamos no nível 1
						if(nant == 0)
							nant = nant + 1;*/
						String sn = PC0[0] + "_" + PC1[0]+ "_" + PC2[0];
						int nivelsnn = ((nivelsn>0)?nivelsn:(nivelsn+1)) + ((nant>0)?nant:(nant+1));
						System.out.println("NS -> NS MD NS => "+sn);
						int index = sns.get(nivelsnn).indexOf(new SNData(sn,0));
						int indexsnant = sns.get(nant).indexOf(new SNData(PC0[0],0));
						int countesq = WordList.howManyStringsIn(PC0[0], esquerda);//
						int indiceinicio = sns.get(nant).get(indexsnant).getIndiceInicio(countesq-1);
						if(index >= 0){//sn já na lista, adiciona posição
							sns.get(nivelsnn).get(index).addPosition(indiceinicio);
						}else{//sn não está na lista, adiciona no fim
							index = sns.get(nivelsnn).size();	   
							sns.get(nivelsnn).add(index,new SNData(sn,indiceinicio));
							sns.get(nivelsnn).get(index).setNivel(nivelsnn);
						}
						//
						esquerda = esquerda + "_" + PC1[0] + "_" + PC2[0] +"/NS"+ nivelsnn; //+ "/NS";
						ppalavras++;
						ppalavras++;
					} else
					if ((PC0[1].startsWith("NS")) && (PC1[1].equals("MD")))//***SN1b+
					{
						//regra não altera o nível do SN
						nivelsn = Integer.parseInt(PC0[1].substring(2));//tags de tamanho 2
						int nant = nivelsn;
						if(nivelsn == 0)
							nivelsn = nivelsn + 1;//neste caso colocamos no nível 1
						String sn = PC0[0] + "_" + PC1[0];
						System.out.println("NS -> NS MD => "+sn);
						int index = sns.get(nivelsn).indexOf(new SNData(sn,0));
						int indexsnant = sns.get(nant).indexOf(new SNData(PC0[0],0));
						int countesq = WordList.howManyStringsIn(PC0[0], esquerda);//
						int indiceinicio = sns.get(nant).get(indexsnant).getIndiceInicio(countesq-1);
						if(index >= 0){//sn já na lista, adiciona posição
							sns.get(nivelsn).get(index).addPosition(indiceinicio);
						}else{//sn não está na lista, adciona no fim
							index = sns.get(nivelsn).size();	   
							sns.get(nivelsn).add(index,new SNData(sn,indiceinicio));
							sns.get(nivelsn).get(index).setNivel(nivelsn);
						}
						//
						esquerda = esquerda + "_" + PC1[0] +"/NS"+ nivelsn; //+ "/NS";
						ppalavras++;
					} else
						if ((PC0[1].equals("MD")) && (PC1[1].startsWith("NS")))//***SN1b
						{
							//regra não altera o nível do SN
							nivelsn = Integer.parseInt(PC1[1].substring(2));//tags de tamanho 2
							int nant = nivelsn;
							if(nivelsn == 0)
								nivelsn = nivelsn + 1;//neste caso sn1a
							String sn = PC0[0] + "_" + PC1[0];
							System.out.println("NS -> MD NS => "+sn);
							int index = sns.get(nivelsn).indexOf(new SNData(sn,0));
							int indexsnant = sns.get(nant).indexOf(new SNData(PC1[0],0));
							int countesq = WordList.howManyStringsIn(PC1[0], esquerda);//
							int indiceinicio = sns.get(nant).get(indexsnant).getIndiceInicio(countesq-1);


							if(index >= 0){//sn já na lista, adiciona posição
								sns.get(nivelsn).get(index).addPosition(indiceinicio);
							}else{//sn não está na lista, adiciona no fim
								index = sns.get(nivelsn).size();
								sns.get(nivelsn).add(index,new SNData(sn,indiceinicio-PC0[0].length()-1-3));//espaco e tag

								sns.get(nivelsn).get(index).setNivel(nivelsn);
							}
							//
							esquerda = esquerda + "_" + PC1[0] +"/NS"+ nivelsn; //+ "/NS";
							ppalavras++;
						} else
							if ((PC0[1].equals("AV")) && (PC1[1].startsWith("NS")))//***SN1+
							{//regra não altera o nível do SN
								nivelsn = Integer.parseInt(PC1[1].substring(2));//tags de tamanho 2
								String sn = PC0[0] + "_" + PC1[0];
								System.out.println("NS -> AV NS => "+sn);
								int index = sns.get(nivelsn).indexOf(new SNData(sn,0));
								int indexsnant = sns.get(nivelsn).indexOf(new SNData(PC1[0],0));
								int countesq = WordList.howManyStringsIn(PC1[0], esquerda);//	
								int indiceinicio = sns.get(nivelsn).get(indexsnant).getIndiceInicio(countesq-1);


								if(index >= 0){//sn já na lista, adiciona posição
									sns.get(nivelsn).get(index).addPosition(indiceinicio);
								}else{//sn não está na lista, adiciona no fim
									index = sns.get(nivelsn).size();
									sns.get(nivelsn).add(index,new SNData(sn,indiceinicio-PC0[0].length()-1-3));//espaco e tag

									sns.get(nivelsn).get(index).setNivel(nivelsn);
								}
								//
								esquerda = esquerda + "_" + PC1[0] + "/"+PC1[1];
								ppalavras++;
							} else
							{
								esquerda = esquerda + "/" + PC0[1];
							}
				}
				ppalavras++;
			}
			esquerda = esquerda.trim();
			if (!frase.equals(esquerda)) alterado = 1;
			frase = esquerda;
		}

		System.out.println("=-=regras envolvendo SNs adjacentes ou unidos por preposições=-=");
		//SNs unidos por preposições
		//todas as regras
		alterado = 1;
		while (alterado == 1)
		{
			alterado = 0;
			frase = frase.trim();
			palavras = frase.split(" ");

			int npalavras = palavras.length;
			int ppalavras = 0;
			String esquerda = "";
			while (ppalavras < npalavras)
			{
				palavra = palavras[ppalavras];

				if (!palavra.equals(""))//(palavra != "")
				{
					//define janela
					String[] PC0 = palavras[ppalavras].split("/");
					String[] PC1 = {"",""};
					String[] PC2 = {"",""};
					String[] PC3 = {"",""};
					esquerda = esquerda + " " + PC0[0];
					if (ppalavras+1<npalavras) PC1 = palavras[ppalavras+1].split("/");
					if (ppalavras+2<npalavras) PC2 = palavras[ppalavras+2].split("/");
					if (ppalavras+3<npalavras) PC3 = palavras[ppalavras+3].split("/");
					//regras ED-CER //tentando priorizar ligados por de
					if ((PC0[1].startsWith("NS")) && (PC1[1].equals("pr") && PC1[0].contains("de")) && (PC2[1].startsWith("NS"))) //***SN2 
					{
						//nivel 2 em diante
						int nivelfirst = Integer.parseInt(PC0[1].substring(2));
						int nivelsec = Integer.parseInt(PC2[1].substring(2));
						int indexfirst = sns.get(nivelfirst).indexOf(new SNData(PC0[0],0));
						int countesq = WordList.howManyStringsIn(PC0[0], esquerda);//	
						int inicio = sns.get(nivelfirst).get(indexfirst).getIndiceInicio(countesq-1);

						nivelsn = ((nivelfirst>=1)?nivelfirst:1) + ((nivelsec>=1)?nivelsec:1);
						nivelsn = (nivelsn<=5 && nivelsn>=0)?nivelsn:5;
						String sn = PC0[0] + "_" + PC1[0] + "_" + PC2[0];
						System.out.println("NS -> NS pr NS => "+sn);
						int index = sns.get(nivelsn).indexOf(new SNData(sn,0));
						if(index >= 0){//sn já na lista
							sns.get(nivelsn).get(index).addPosition(inicio);

						}else{//sn não está na lista
							index = sns.get(nivelsn).size();

							sns.get(nivelsn).add(new SNData(sn,inicio));

							sns.get(nivelsn).get(index).setNivel(nivelsn);
						}
						//
						esquerda = esquerda + "_" + PC1[0] + "_" + PC2[0] + "/NS" + nivelsn;
						ppalavras++;
						ppalavras++;
					} else
						if ((PC0[1].startsWith("NS")) && (PC1[1].equals("pr")) && (PC2[1].equals("de") && "o a os as um uma uns umas".contains(PC2[0]) && (PC3[1].startsWith("NS")))) //***SN3
						//if ((PC0[1].startsWith("NS")) && (PC1[1].equals("pr") && PC1[0].contains("de")) && (PC2[1].equals("de") && (PC3[1].startsWith("NS")))) //***SN3 
						{
							//nivel 2 em diante TODO: não enquadrar pronome possessivo como /de, apenas artigos
							int nivelfirst = Integer.parseInt(PC0[1].substring(2));
							int nivelsec = Integer.parseInt(PC3[1].substring(2));
							int indexfirst = sns.get(nivelfirst).indexOf(new SNData(PC0[0],0));
							int countesq = WordList.howManyStringsIn(PC0[0], esquerda);//	
							int inicio = sns.get(nivelfirst).get(indexfirst).getIndiceInicio(countesq-1);

							nivelsn = ((nivelfirst>=1)?nivelfirst:1) + ((nivelsec>=1)?nivelsec:1);
							nivelsn = (nivelsn<=5 && nivelsn>=0)?nivelsn:5;
							String sn = PC0[0] + "_" + PC1[0] + "_" + PC2[0]+ "_" + PC3[0];
							System.out.println("NS -> NS pr de NS => "+sn);
							int index = sns.get(nivelsn).indexOf(new SNData(sn,0));
							if(index >= 0){//sn já na lista
								sns.get(nivelsn).get(index).addPosition(inicio);
								//int countesq = WordList.howManyStringsIn(PC0[0], esquerda);//
								//int indiceinicio = sns.get(nant).get(indexsnant).getIndiceInicio(countesq-1);

							}else{//sn não está na lista
								index = sns.get(nivelsn).size();

								sns.get(nivelsn).add(new SNData(sn,inicio));

								sns.get(nivelsn).get(index).setNivel(nivelsn);
							}
							//
							esquerda = esquerda + "_" + PC1[0] + "_" + PC2[0] + "_" + PC3[0] + "/NS"+nivelsn;
							ppalavras++;
							ppalavras++;
							ppalavras++;
							/*}else
							if ((PC0[1].startsWith("NS")) && (PC1[1].equals("co")) && (PC2[1].startsWith("NS"))) //***SN3,4,5+
							{
								//2 em diante
								int nivelfirst = Integer.parseInt(PC0[1].substring(2));
								int nivelsec = Integer.parseInt(PC2[1].substring(2));
								int indexfirst = sns.get(nivelfirst).indexOf(new SNData(PC0[0],0));
								int countesq = WordList.howManyStringsIn(PC0[0], esquerda);//	
								int inicio = sns.get(nivelfirst).get(indexfirst).getIndiceInicio(countesq-1);

								nivelsn = ((nivelfirst>=1)?nivelfirst:1) + ((nivelsec>=1)?nivelsec:1);
								nivelsn = (nivelsn<=5 && nivelsn>=0)?nivelsn:5;
								String sn = PC0[0] + "_" + PC1[0] + "_" + PC2[0];
								System.out.println("NS -> NS co NS => "+sn);
								int index = sns.get(nivelsn).indexOf(new SNData(sn,0));
								if(index >= 0){//sn já na lista
									sns.get(nivelsn).get(index).addPosition(inicio);

								}else{//sn não está na lista
									index = sns.get(nivelsn).size();

									sns.get(nivelsn).add(new SNData(sn,inicio));

									sns.get(nivelsn).get(index).setNivel(nivelsn);
								}
								//
								esquerda = esquerda + "_" + PC1[0] + "_" + PC2[0] + "/NS"+nivelsn;
								ppalavras++;
								ppalavras++;
*/	
						} else//acionei esta para o caso: software horizon
							if (PC0[1].startsWith("NS") && PC1[1].startsWith("NS")) //***SN2 
							{
								//nivel 2 em diante
								int nivelfirst = Integer.parseInt(PC0[1].substring(2));
								int nivelsec = Integer.parseInt(PC1[1].substring(2));
								int indexfirst = sns.get(nivelfirst).indexOf(new SNData(PC0[0],0));
								int countesq = WordList.howManyStringsIn(PC0[0], esquerda);//	
								int inicio = sns.get(nivelfirst).get(indexfirst).getIndiceInicio(countesq-1);

								nivelsn = ((nivelfirst>=1)?nivelfirst:1) + ((nivelsec>=1)?nivelsec:1);
								nivelsn = (nivelsn<=5 && nivelsn>=0)?nivelsn:5;
								String sn = PC0[0] + "_" + PC1[0];
								System.out.println("NS -> NS NS => "+sn);
								int index = sns.get(nivelsn).indexOf(new SNData(sn,0));
								if(index >= 0){//sn já na lista
									sns.get(nivelsn).get(index).addPosition(inicio);

								}else{//sn não está na lista
									index = sns.get(nivelsn).size();

									sns.get(nivelsn).add(new SNData(sn,inicio));

									sns.get(nivelsn).get(index).setNivel(nivelsn);
								}
								//
								esquerda = esquerda + "_" + PC1[0]  + "/NS" + nivelsn;
								ppalavras++;

							}
							else			{
								esquerda = esquerda + "/" + PC0[1];
							}
				}
				ppalavras++;
			}
			esquerda = esquerda.trim();
			if (!frase.equals(esquerda)) alterado = 1;
			frase = esquerda;
		}


		//todas as regras
		System.out.println("=-=todas as regras=-=");
		alterado = 1;
		while (alterado == 1)
		{
			alterado = 0;
			frase = frase.trim();
			palavras = frase.split(" ");

			int npalavras = palavras.length;
			int ppalavras = 0;
			String esquerda = "";
			while (ppalavras < npalavras)
			{
				palavra = palavras[ppalavras];

				if (!palavra.equals(""))//(palavra != "")
				{
					//define janela
					String[] PC0 = palavras[ppalavras].split("/");
					String[] PC1 = {"",""};
					String[] PC2 = {"",""};
					String[] PC3 = {"",""};
					esquerda = esquerda + " " + PC0[0];
					if (ppalavras+1<npalavras) PC1 = palavras[ppalavras+1].split("/");
					if (ppalavras+2<npalavras) PC2 = palavras[ppalavras+2].split("/");
					if (ppalavras+3<npalavras) PC3 = palavras[ppalavras+3].split("/");
					//regras ED-CER
					if ((PC0[1].equals("AV")) && (PC1[1].equals("AV")))
					{
						esquerda = esquerda +"_" +  PC1[0] + "/AV";
						ppalavras++;
					} else
						if ((PC0[1].equals("AV")) && (PC1[1].equals("MD")))
						{
							esquerda = esquerda + "_" + PC1[0] + "/MD";
							ppalavras++;
						} else
							if ((PC0[1].equals("MD")) && (PC1[1].equals("co")) && (PC2[1].equals("MD")))
							{
								esquerda = esquerda + "_" + PC1[0] + "_" + PC2[0] + "/MD";
								ppalavras++;
								ppalavras++;
							} else//acionei esta para o caso: software horizon
								if (PC0[1].startsWith("NS") && PC1[1].startsWith("NS")) //***SN2 
								{
									//nivel 2 em diante
									int nivelfirst = Integer.parseInt(PC0[1].substring(2));
									int nivelsec = Integer.parseInt(PC1[1].substring(2));
									int indexfirst = sns.get(nivelfirst).indexOf(new SNData(PC0[0],0));
									int countesq = WordList.howManyStringsIn(PC0[0], esquerda);//	
									int inicio = sns.get(nivelfirst).get(indexfirst).getIndiceInicio(countesq-1);

									nivelsn = ((nivelfirst>=1)?nivelfirst:1) + ((nivelsec>=1)?nivelsec:1);
									nivelsn = (nivelsn<=5 && nivelsn>=0)?nivelsn:5;
									String sn = PC0[0] + "_" + PC1[0];
									System.out.println("NS -> NS NS => "+sn);
									int index = sns.get(nivelsn).indexOf(new SNData(sn,0));
									if(index >= 0){//sn já na lista
										sns.get(nivelsn).get(index).addPosition(inicio);

									}else{//sn não está na lista
										index = sns.get(nivelsn).size();



										sns.get(nivelsn).add(new SNData(sn,inicio));

										sns.get(nivelsn).get(index).setNivel(nivelsn);
									}
									//
									esquerda = esquerda + "_" + PC1[0]  + "/NS" + nivelsn;
									ppalavras++;

								}else
									if ((PC0[1].startsWith("NS")) && (PC1[1].equals("MD")))//***SN1b+
									{
										//regra não altera o nível do SN

										nivelsn = Integer.parseInt(PC0[1].substring(2));//tags de tamanho 2
										int nant = nivelsn;
										if(nivelsn == 0)
											nivelsn = nivelsn + 1;//neste caso colocamos no nível 1
										String sn = PC0[0] + "_" + PC1[0];
										System.out.println("NS -> NS MD => "+sn);
										int index = sns.get(nivelsn).indexOf(new SNData(sn,0));
										int indexsnant = sns.get(nant).indexOf(new SNData(PC0[0],0));
										int countesq = WordList.howManyStringsIn(PC0[0], esquerda);//
										int indiceinicio = sns.get(nant).get(indexsnant).getIndiceInicio(countesq-1);

										if(index >= 0){//sn já na lista, adiciona posição
											sns.get(nivelsn).get(index).addPosition(indiceinicio);
										}else{//sn não está na lista, adciona no fim
											index = sns.get(nivelsn).size();	   
											sns.get(nivelsn).add(index,new SNData(sn,indiceinicio));
											sns.get(nivelsn).get(index).setNivel(nivelsn);
										}
										//
										esquerda = esquerda + "_" + PC1[0] +"/NS"+ nivelsn; //+ "/NS";
										ppalavras++;
									} else
										if ((PC0[1].equals("MD")) && (PC1[1].startsWith("NS")))//***SN1b
										{
											//regra não altera o nível do SN
											nivelsn = Integer.parseInt(PC1[1].substring(2));//tags de tamanho 2
											int nant = nivelsn;
											if(nivelsn == 0)
												nivelsn = nivelsn + 1;//neste caso sn1a
											String sn = PC0[0] + "_" + PC1[0];
											System.out.println("NS -> MD NS => "+sn);
											int index = sns.get(nivelsn).indexOf(new SNData(sn,0));
											int indexsnant = sns.get(nant).indexOf(new SNData(PC1[0],0));
											int countesq = WordList.howManyStringsIn(PC1[0], esquerda);//
											int indiceinicio = sns.get(nant).get(indexsnant).getIndiceInicio(countesq-1);


											if(index >= 0){//sn já na lista, adiciona posição
												sns.get(nivelsn).get(index).addPosition(indiceinicio);
											}else{//sn não está na lista, adiciona no fim
												index = sns.get(nivelsn).size();
												sns.get(nivelsn).add(index,new SNData(sn,indiceinicio-PC0[0].length()-1-3));//espaco e tag

												sns.get(nivelsn).get(index).setNivel(nivelsn);
											}
											//
											esquerda = esquerda + "_" + PC1[0] +"/NS"+ nivelsn; //+ "/NS";
											ppalavras++;
										} else
											if ((PC0[1].startsWith("NS")) && (PC1[1].equals("pr")) && (PC2[1].startsWith("NS"))) //***SN2 
											{
												//nivel 2 em diante
												int nivelfirst = Integer.parseInt(PC0[1].substring(2));
												int nivelsec = Integer.parseInt(PC2[1].substring(2));
												int indexfirst = sns.get(nivelfirst).indexOf(new SNData(PC0[0],0));
												nivelsn = ((nivelfirst>=1)?nivelfirst:1) + ((nivelsec>=1)?nivelsec:1);
												nivelsn = (nivelsn<=5 && nivelsn>=0)?nivelsn:5;
												int countesq = WordList.howManyStringsIn(PC0[0], esquerda);//	
												int inicio = sns.get(nivelfirst).get(indexfirst).getIndiceInicio(countesq-1);

												String sn = PC0[0] + "_" + PC1[0] + "_" + PC2[0];
												System.out.println("NS -> NS pr NS => "+sn);
												int index = sns.get(nivelsn).indexOf(new SNData(sn,0));
												if(index >= 0){//sn já na lista
													sns.get(nivelsn).get(index).addPosition(inicio);

												}else{//sn não está na lista
													index = sns.get(nivelsn).size();

													sns.get(nivelsn).add(new SNData(sn,inicio));

													sns.get(nivelsn).get(index).setNivel(nivelsn);
												}
												//
												esquerda = esquerda + "_" + PC1[0] + "_" + PC2[0] + "/NS" + nivelsn;
												ppalavras++;
												ppalavras++;
											} else
												if ((PC0[1].startsWith("NS")) && (PC1[1].equals("pr")) && (PC2[1].equals("de") && (PC3[1].startsWith("NS")))) //***SN3 
												{
													//nivel 2 em diante
													int nivelfirst = Integer.parseInt(PC0[1].substring(2));
													int nivelsec = Integer.parseInt(PC3[1].substring(2));
													int indexfirst = sns.get(nivelfirst).indexOf(new SNData(PC0[0],0));
													int countesq = WordList.howManyStringsIn(PC0[0], esquerda);//	
													int inicio = sns.get(nivelfirst).get(indexfirst).getIndiceInicio(countesq-1);

													nivelsn = ((nivelfirst>=1)?nivelfirst:1) + ((nivelsec>=1)?nivelsec:1);
													nivelsn = (nivelsn<=5 && nivelsn>=0)?nivelsn:5;
													String sn = PC0[0] + "_" + PC1[0] + "_" + PC2[0]+ "_" + PC3[0];
													System.out.println("NS -> NS pr de NS => "+sn);
													int index = sns.get(nivelsn).indexOf(new SNData(sn,0));
													if(index >= 0){//sn já na lista
														sns.get(nivelsn).get(index).addPosition(inicio);

													}else{//sn não está na lista
														index = sns.get(nivelsn).size();

														sns.get(nivelsn).add(new SNData(sn,inicio));

														sns.get(nivelsn).get(index).setNivel(nivelsn);
													}
													//
													esquerda = esquerda + "_" + PC1[0] + "_" + PC2[0] + "_" + PC3[0] + "/NS"+nivelsn;
													ppalavras++;
													ppalavras++;
													ppalavras++;
												} else
													if ((PC0[1].startsWith("NS")) && (PC1[1].equals("VG")) && (PC2[1].equals("MD")))//***?
													{//não dispara se "," categorizado como PN, não implementado em phython
														esquerda = esquerda + "_" + PC1[0] + "_" + PC2[0] + "/"+ PC0[1];
														ppalavras++;
														ppalavras++;
														System.out.println("Regra VG disparando!");
													} else
														if ((PC0[1].startsWith("NS")) && (PC1[1].equals("VG")) && (PC2[1].equals("de")) && (PC3[1].equals("MD")))//***?
														{//não dispara se "," categorizado como PN, não implementado em phython
															esquerda = esquerda + "_" + PC1[0] + "_" + PC2[0] + "_"
																	+ PC3[0] + "/"+ PC0[1];
															ppalavras++;
															ppalavras++;
															ppalavras++;
															System.out.println("Regra VG disparando!");
														}
														else
															if ((PC0[1].startsWith("NS")) && (PC1[1].equals("co")) && (PC2[1].startsWith("NS"))) //***SN3,4,5+
															{
																//2 em diante
																int nivelfirst = Integer.parseInt(PC0[1].substring(2));
																int nivelsec = Integer.parseInt(PC2[1].substring(2));
																int indexfirst = sns.get(nivelfirst).indexOf(new SNData(PC0[0],0));
																int countesq = WordList.howManyStringsIn(PC0[0], esquerda);//	
																int inicio = sns.get(nivelfirst).get(indexfirst).getIndiceInicio(countesq-1);

																nivelsn = ((nivelfirst>=1)?nivelfirst:1) + ((nivelsec>=1)?nivelsec:1);
																nivelsn = (nivelsn<=5 && nivelsn>=0)?nivelsn:5;
																String sn = PC0[0] + "_" + PC1[0] + "_" + PC2[0];
																System.out.println("NS -> NS co NS => "+sn);
																int index = sns.get(nivelsn).indexOf(new SNData(sn,0));
																if(index >= 0){//sn já na lista
																	sns.get(nivelsn).get(index).addPosition(inicio);

																}else{//sn não está na lista
																	index = sns.get(nivelsn).size();

																	sns.get(nivelsn).add(new SNData(sn,inicio));

																	sns.get(nivelsn).get(index).setNivel(nivelsn);
																}
																//
																esquerda = esquerda + "_" + PC1[0] + "_" + PC2[0] + "/NS"+nivelsn;
																ppalavras++;
																ppalavras++;
															} else
																if ((PC0[1].startsWith("NS")) && (PC1[1].equals("co")) &&
																		(PC2[1].equals("de")) && (PC3[1].startsWith("NS"))) //***SN3,4,5+
																{

																	//nivel 2 em diante
																	int nivelfirst = Integer.parseInt(PC0[1].substring(2));
																	int nivelsec = Integer.parseInt(PC3[1].substring(2));
																	int indexfirst = sns.get(nivelfirst).indexOf(new SNData(PC0[0],0));
																	int countesq = WordList.howManyStringsIn(PC0[0], esquerda);//	
																	int inicio = sns.get(nivelfirst).get(indexfirst).getIndiceInicio(countesq-1);

																	nivelsn = ((nivelfirst>=1)?nivelfirst:1) + ((nivelsec>=1)?nivelsec:1);
																	nivelsn = (nivelsn<=5 && nivelsn>=0)?nivelsn:5;
																	String sn = PC0[0] + "_" + PC1[0] + "_" + PC2[0]+ "_" + PC3[0];
																	System.out.println("NS -> NS co de NS => "+sn);
																	int index = sns.get(nivelsn).indexOf(new SNData(sn,0));
																	if(index >= 0){//sn já na lista
																		sns.get(nivelsn).get(index).addPosition(inicio);

																	}else{//sn não está na lista
																		index = sns.get(nivelsn).size();

																		sns.get(nivelsn).add(new SNData(sn,inicio));

																		sns.get(nivelsn).get(index).setNivel(nivelsn);
																	}
																	//
																	esquerda = esquerda + "_" + PC1[0] + "_" +
																			PC2[0] + "_" + PC3[0] + "/NS"+nivelsn;
																	ppalavras++;
																	ppalavras++;
																	ppalavras++;
																} else//adicionei por causa de "impacto sobre NS e sobre NS"
																	if ((PC0[1].startsWith("NS")) && (PC1[1].equals("co")) && (PC2[1].startsWith("pr"))&& (PC3[1].startsWith("NS"))) //***SN3,4,5+
																	{
																		//2 em diante
																		int nivelfirst = Integer.parseInt(PC0[1].substring(2));
																		int nivelsec = Integer.parseInt(PC3[1].substring(2));
																		int indexfirst = sns.get(nivelfirst).indexOf(new SNData(PC0[0],0));
																		int countesq = WordList.howManyStringsIn(PC0[0], esquerda);//	
																		int inicio = sns.get(nivelfirst).get(indexfirst).getIndiceInicio(countesq-1);

																		nivelsn = ((nivelfirst>=1)?nivelfirst:1) + ((nivelsec>=1)?nivelsec:1);
																		nivelsn = (nivelsn<=5 && nivelsn>=0)?nivelsn:5;
																		String sn = PC0[0] + "_" + PC1[0] + "_" + PC2[0]+ "_" + PC3[0];
																		System.out.println("NS -> NS co pr NS => "+sn);
																		int index = sns.get(nivelsn).indexOf(new SNData(sn,0));
																		if(index >= 0){//sn já na lista
																			sns.get(nivelsn).get(index).addPosition(inicio);

																		}else{//sn não está na lista
																			index = sns.get(nivelsn).size();

																			sns.get(nivelsn).add(new SNData(sn,inicio));

																			sns.get(nivelsn).get(index).setNivel(nivelsn);
																		}
																		//
																		esquerda = esquerda + "_" + PC1[0] + "_" + PC2[0] + "_" + PC3[0]+ "/NS"+nivelsn;
																		ppalavras++;
																		ppalavras++;
																		ppalavras++;
																	}else
																		if ((PC0[1].equals("AV")) && (PC1[1].startsWith("NS")))//***SN1+
																		{//regra não altera o nível do SN
																			nivelsn = Integer.parseInt(PC1[1].substring(2));//tags de tamanho 2
																			String sn = PC0[0] + "_" + PC1[0];
																			System.out.println("NS -> AV NS => "+sn);
																			int index = sns.get(nivelsn).indexOf(new SNData(sn,0));
																			int indexsnant = sns.get(nivelsn).indexOf(new SNData(PC1[0],0));
																			int countesq = WordList.howManyStringsIn(PC1[0], esquerda);//	

																			int indiceinicio = sns.get(nivelsn).get(indexsnant).getIndiceInicio(countesq-1);

																			if(index >= 0){//sn já na lista, adiciona posição
																				sns.get(nivelsn).get(index).addPosition(indiceinicio);
																			}else{//sn não está na lista, adiciona no fim
																				index = sns.get(nivelsn).size();
																				sns.get(nivelsn).add(index,new SNData(sn,indiceinicio-PC0[0].length()-1-3));//espaco e tag

																				sns.get(nivelsn).get(index).setNivel(nivelsn);
																			}
																			//
																			esquerda = esquerda + "_" + PC1[0] + "/"+PC1[1];
																			ppalavras++;
																		} else
																			if ((PC0[1].equals("de")) && (PC1[1].startsWith("NS")))//***SN1+
																			{//regra não altera o nível do SN
																				nivelsn = Integer.parseInt(PC1[1].substring(2));//tags de tamanho 2
																				String sn = PC0[0] + "_" + PC1[0];
																				System.out.println("NS -> de NS => "+sn);
																				int index = sns.get(nivelsn).indexOf(new SNData(sn,0));
																				int indexsnant = sns.get(nivelsn).indexOf(new SNData(PC1[0],0));
																				int countesq = WordList.howManyStringsIn(PC1[0], esquerda);//	

																				int indiceinicio = sns.get(nivelsn).get(indexsnant).getIndiceInicio(countesq-1);

																				if(index >= 0){//sn já na lista, adiciona posição
																					sns.get(nivelsn).get(index).addPosition(indiceinicio);//na verdade não precisa fazer nada
																				}else{//sn não está na lista, adiciona no fim
																					index = sns.get(nivelsn).size();
																					sns.get(nivelsn).add(index,new SNData(sn,indiceinicio-PC0[0].length()-1-3));//espaco e tag
																					sns.get(nivelsn).get(index).setNivel(nivelsn);
																				}
																				//
																				esquerda = esquerda + "_" + PC1[0] + "/"+PC1[1];
																				ppalavras++;
																			}
																			else
																			{
																				esquerda = esquerda + "/" + PC0[1];
																			}
				}
				ppalavras++;
			}
			esquerda = esquerda.trim();
			if (!frase.equals(esquerda)) alterado = 1;
			frase = esquerda;
		}
		frase = frase.replace("/NS", "/SN");
		rst = frase;
		System.out.println(rst);
		//return rst;
		return sns;
	}

	public static Vector<Vector<SNData>> idCatSNTextoEtiquetado(String frase)
	{
		String rst;
		String palavra;
		rst = "";
		int[] freqregras = new int[10];
		frase = frase.trim();


		Vector<Vector<SNData>> sns =new Vector<Vector<SNData>>( );
		sns.add(new Vector<SNData>());//0 SN1A
		sns.add(new Vector<SNData>());//1 SN1B
		sns.add(new Vector<SNData>());//2 SN2
		sns.add(new Vector<SNData>());//3 SN3
		sns.add(new Vector<SNData>());//4 SN4
		sns.add(new Vector<SNData>());//5 SN5+

		//Remove vírgulas das tags
		frase = frase.replace("/VG", "/PN");//adicionado para impedir disparo de regras com vírgula

		// Normaliza as tags para o padrão ED-CER
		frase = frase.replace("/AD", "/AR");
		frase = frase.replace("/AI", "/AR");
		frase = frase.replace("/VP", "/AJ");
		frase = frase.replace("/NC", "/NU");
		frase = frase.replace("/NM", "/NU");
		frase = frase.replace("/NO", "/NU");
		frase = frase.replace("/NR", "/NU");
		//frase = frase.replace("/VG", "/CO");//seria útil apenas no nome de instituições
		frase = frase.replace("/CJ", "/CO");
		frase = frase.replace("/PL", "/LG");


		//ADAPTAÇÂO DO MODO SELETOR DO METODO ED-CER
		//Inicia analise
		frase = frase.replace("/LG", "/ct");
		frase = frase.replace("/VB", "/ct");
		frase = frase.replace("/PN", "/ct");
		int alterado = 1;
		while (alterado == 1)
		{
			alterado = 0;
			frase = frase.trim();
			String[] palavras = frase.split(" ");
			int npalavras = palavras.length;
			int ppalavras = 0;
			String esquerda = "";
			while (ppalavras < npalavras)
			{
				palavra = palavras[ppalavras];
				if (!palavra.equals(""))
				{
					//janela de quatro palavras
					//separa palavra da etiqueta
					String[] PC0 = palavras[ppalavras].split("/");
					String[] PC1 = { "", "" };
					String[] PC2 = { "", "" };
					String[] PC3 = { "", "" };

					esquerda = esquerda + " " + PC0[0];
					if (ppalavras + 1 < npalavras) PC1 = palavras[ppalavras +1].split("/");
					if (ppalavras + 2 < npalavras) PC2 = palavras[ppalavras +2].split("/");
					if (ppalavras + 3 < npalavras) PC3 = palavras[ppalavras +3].split("/");

					/*if (PC0[1].equals("NP") && PC1[1].equals("NP"))//adicionado tratamento NP
					{
						esquerda = esquerda + "=" + PC1[0] + "/NP";//Qual o nível deste SN?? Ficará igual a zero.
						ppalavras++;//avança
					}
					else*/		
						//regras ED-CER
						if (PC0[1].equals("AV") && PC1[1].equals("ct"))
						{
							esquerda = esquerda + "_" + PC1[0] + "/ct";
							ppalavras++;//avança
						}
						else
							if (PC0[1].equals("AV") && PC1[1].equals("AR"))
							{
								esquerda = esquerda + "/ct";
								//ppalavras++;//avança
							}
							else
								if ((PC0[1].equals("CO")) && (PC1[1].equals("ct")))
								{
									esquerda = esquerda + "_" + PC1[0] + "/ct";
									ppalavras++;
								}
								else
									if ((PC0[1].equals("AR")) && (PC1[1].equals("ct")))
									{
										esquerda = esquerda + "_" + PC1[0] + "/ct";
										ppalavras++;
									}
									else
										if ((PC0[1].equals("NU")) && (PC1[1].equals("ct")))
										{
											esquerda = esquerda + "_" + PC1[0] + "/ct";
											ppalavras++;
										}
										else
											if ((PC0[1].equals("ct")) && (PC1[1].equals("PR")))
											{
												esquerda = esquerda + "_" + PC1[0] + "/ct";
												ppalavras++;
											}
											else
												if ((PC0[1].equals("ct")) && (PC1[1].equals("CO")))
												{
													esquerda = esquerda + "_" + PC1[0] +"/ct";
													ppalavras++;
												}
												else
													esquerda = esquerda + "/" + PC0[1];
				}
				ppalavras++;
			}
			esquerda = esquerda.trim();
			if (!frase.equals(esquerda)) alterado = 1;
			frase = esquerda;
		}

		//ADAPTAÇÂO DO MODO ANALISADOR DO METODO ED-CER
		// Inicia gramatica
		frase = frase.replace("/AR","/de");
		frase = frase.replace("/PD","/de");
		frase = frase.replace("/PI","/de");
		frase = frase.replace("/AJ","/MD");
		frase = frase.replace("/NU","/MD");
		frase = frase.replace("/PS","/MD");
		frase = frase.replace("/CO","/co");
		frase = frase.replace("/PR","/pr");
		frase = frase.replace("/SU","/re");
		frase = frase.replace("/PP","/ct"); //mudei para ct, PP -> de Segundo Maia Modificação sobre o ED-CER (PP -> re)
		frase = frase.replace("/NP","/re");
		//Inicia analise
		frase = frase.replace("/re", "/NS0");//***regra SN1a, requer adaptação no cálculo do nivel dos sns superiores

		String[] palavras = frase.split(" ");
		String[] etiquetas = new String[palavras.length];
		String [] temp = frase.split("/| ");
		int nivelsn=0;
		int offset = 0;
		for(int i=0;i<temp.length;i+=2){
			etiquetas[i/2] = temp[i+1];
			if(temp[i+1].startsWith("NS")){
				nivelsn = Integer.parseInt(temp[i+1].substring(2));//tags de tamanho 2
				int index = sns.get(nivelsn).indexOf(new SNData(temp[i],0));
				//Adicionando SN1a
				if(index >= 0){//sn já na lista
					sns.get(nivelsn).get(index).addPosition(offset);//base para calculo do offset dos sns de nivel superior

				}else{//sn não está na lista
					index = sns.get(nivelsn).size();
					sns.get(nivelsn).add(index,new SNData(temp[i],frase.indexOf(temp[i],offset)));//define offset

				}
			}
			offset = offset + temp[i].length()+ temp[i+1].length() + 2;//sn + taglength + 1 barra + 1 espaço em branco 
		}

		alterado = 1;
		while (alterado == 1)
		{
			alterado = 0;
			frase = frase.trim();
			palavras = frase.split(" ");

			int npalavras = palavras.length;
			int ppalavras = 0;
			String esquerda = "";
			while (ppalavras < npalavras)
			{
				palavra = palavras[ppalavras];

				if (!palavra.equals(""))//(palavra != "")
				{
					//define janela
					String[] PC0 = palavras[ppalavras].split("/");
					String[] PC1 = {"",""};
					String[] PC2 = {"",""};
					String[] PC3 = {"",""};
					esquerda = esquerda + " " + PC0[0];
					if (ppalavras+1<npalavras) PC1 = palavras[ppalavras+1].split("/");
					if (ppalavras+2<npalavras) PC2 = palavras[ppalavras+2].split("/");
					if (ppalavras+3<npalavras) PC3 = palavras[ppalavras+3].split("/");
					//regras ED-CER
					if ((PC0[1].equals("AV")) && (PC1[1].equals("AV")))
					{
						esquerda = esquerda +"_" +  PC1[0] + "/AV";
						ppalavras++;
					} else
						if ((PC0[1].equals("AV")) && (PC1[1].equals("MD")))
						{
							esquerda = esquerda + "_" + PC1[0] + "/MD";
							ppalavras++;
						} else
							if ((PC0[1].equals("MD")) && (PC1[1].equals("co")) && (PC2[1].equals("MD")))
							{
								esquerda = esquerda + "_" + PC1[0] + "_" + PC2[0] + "/MD";
								ppalavras++;
								ppalavras++;
							} else//acionei esta para os casos: o projeto internacional math-net, o software horizon
								if (PC0[1].startsWith("NS") && PC1[1].startsWith("NS")) //***SN2 
								{
									//nivel 2 em diante
									int nivelfirst = Integer.parseInt(PC0[1].substring(2));
									int nivelsec = Integer.parseInt(PC1[1].substring(2));
									int indexfirst = sns.get(nivelfirst).indexOf(new SNData(PC0[0],0));
									int countesq = WordList.howManyStringsIn(PC0[0], esquerda);//
									int inicio = sns.get(nivelfirst).get(indexfirst).getIndiceInicio(countesq-1);
									//int inicio = sns.get(nivelfirst).get(indexfirst).getIndiceInicio();
									nivelsn = ((nivelfirst>=1)?nivelfirst:1) + ((nivelsec>=1)?nivelsec:1);
									nivelsn = (nivelsn<=5 && nivelsn>=0)?nivelsn:5;
									String sn = PC0[0] + "_" + PC1[0];
									System.out.println("NS -> NS NS => "+sn);
									int index = sns.get(nivelsn).indexOf(new SNData(sn,0));
									if(index >= 0){//sn já na lista
										sns.get(nivelsn).get(index).addPosition(inicio);

									}else{//sn não está na lista
										index = sns.get(nivelsn).size();
										sns.get(nivelsn).add(new SNData(sn,inicio));

										sns.get(nivelsn).get(index).setNivel(nivelsn);
									}
									//
									esquerda = esquerda + "_" + PC1[0]  + "/NS" + nivelsn;
									ppalavras++;

								}else
									if ((PC0[1].startsWith("NS")) && (PC1[1].equals("MD")))//***SN1b+
									{
										//regra não altera o nível do SN

										nivelsn = Integer.parseInt(PC0[1].substring(2));//tags de tamanho 2
										int nant = nivelsn;
										if(nivelsn == 0)
											nivelsn = nivelsn + 1;//neste caso colocamos no nível 1
										String sn = PC0[0] + "_" + PC1[0];
										System.out.println("NS -> NS MD => "+sn);
										int index = sns.get(nivelsn).indexOf(new SNData(sn,0));
										int indexsnant = sns.get(nant).indexOf(new SNData(PC0[0],0));
										int countesq = WordList.howManyStringsIn(PC0[0], esquerda);//
										int indiceinicio = sns.get(nant).get(indexsnant).getIndiceInicio(countesq-1);

										if(index >= 0){//sn já na lista, adiciona posição
											sns.get(nivelsn).get(index).addPosition(indiceinicio);
										}else{//sn não está na lista, adciona no fim
											index = sns.get(nivelsn).size();	   
											sns.get(nivelsn).add(index,new SNData(sn,indiceinicio));
											sns.get(nivelsn).get(index).setNivel(nivelsn);
										}
										//
										esquerda = esquerda + "_" + PC1[0] +"/NS"+ nivelsn; //+ "/NS";
										ppalavras++;
									} else
										if ((PC0[1].equals("MD")) && (PC1[1].startsWith("NS")))//***SN1b
										{
											//regra não altera o nível do SN
											nivelsn = Integer.parseInt(PC1[1].substring(2));//tags de tamanho 2
											int nant = nivelsn;
											if(nivelsn == 0)
												nivelsn = nivelsn + 1;//neste caso sn1a
											String sn = PC0[0] + "_" + PC1[0];
											System.out.println("NS -> MD NS => "+sn);
											int index = sns.get(nivelsn).indexOf(new SNData(sn,0));
											int indexsnant = sns.get(nant).indexOf(new SNData(PC1[0],0));
											int countesq = WordList.howManyStringsIn(PC1[0], esquerda);//
											int indiceinicio = sns.get(nant).get(indexsnant).getIndiceInicio(countesq-1);


											if(index >= 0){//sn já na lista, adiciona posição
												sns.get(nivelsn).get(index).addPosition(indiceinicio);
											}else{//sn não está na lista, adiciona no fim
												index = sns.get(nivelsn).size();
												sns.get(nivelsn).add(index,new SNData(sn,indiceinicio-PC0[0].length()-1-3));//espaco e tag

												sns.get(nivelsn).get(index).setNivel(nivelsn);
											}
											//
											esquerda = esquerda + "_" + PC1[0] +"/NS"+ nivelsn; //+ "/NS";
											ppalavras++;
										} else
											if ((PC0[1].startsWith("NS")) && (PC1[1].equals("pr")) && (PC2[1].startsWith("NS"))) //***SN2 
											{
												//nivel 2 em diante
												int nivelfirst = Integer.parseInt(PC0[1].substring(2));
												int nivelsec = Integer.parseInt(PC2[1].substring(2));
												int indexfirst = sns.get(nivelfirst).indexOf(new SNData(PC0[0],0));
												int countesq = WordList.howManyStringsIn(PC0[0], esquerda);//
												int inicio = sns.get(nivelfirst).get(indexfirst).getIndiceInicio(countesq-1);
												nivelsn = ((nivelfirst>=1)?nivelfirst:1) + ((nivelsec>=1)?nivelsec:1);
												nivelsn = (nivelsn<=5 && nivelsn>=0)?nivelsn:5;
												String sn = PC0[0] + "_" + PC1[0] + "_" + PC2[0];
												System.out.println("NS -> NS pr NS => "+sn);
												int index = sns.get(nivelsn).indexOf(new SNData(sn,0));
												if(index >= 0){//sn já na lista
													sns.get(nivelsn).get(index).addPosition(inicio);

												}else{//sn não está na lista
													index = sns.get(nivelsn).size();
													sns.get(nivelsn).add(new SNData(sn,inicio));
													sns.get(nivelsn).get(index).setNivel(nivelsn);
												}
												//
												esquerda = esquerda + "_" + PC1[0] + "_" + PC2[0] + "/NS" + nivelsn;
												ppalavras++;
												ppalavras++;
											} else
												if ((PC0[1].startsWith("NS")) && (PC1[1].equals("pr")) && (PC2[1].equals("de") && (PC3[1].startsWith("NS")))) //***SN3 
												{
													//nivel 2 em diante
													int nivelfirst = Integer.parseInt(PC0[1].substring(2));
													int nivelsec = Integer.parseInt(PC3[1].substring(2));
													int indexfirst = sns.get(nivelfirst).indexOf(new SNData(PC0[0],0));
													int countesq = WordList.howManyStringsIn(PC0[0], esquerda);//
													int inicio = sns.get(nivelfirst).get(indexfirst).getIndiceInicio(countesq-1);
													nivelsn = ((nivelfirst>=1)?nivelfirst:1) + ((nivelsec>=1)?nivelsec:1);
													nivelsn = (nivelsn<=5 && nivelsn>=0)?nivelsn:5;
													String sn = PC0[0] + "_" + PC1[0] + "_" + PC2[0]+ "_" + PC3[0];
													System.out.println("NS -> NS pr de NS => "+sn);
													int index = sns.get(nivelsn).indexOf(new SNData(sn,0));
													if(index >= 0){//sn já na lista
														sns.get(nivelsn).get(index).addPosition(inicio);

													}else{//sn não está na lista
														index = sns.get(nivelsn).size();
														sns.get(nivelsn).add(new SNData(sn,inicio));

														sns.get(nivelsn).get(index).setNivel(nivelsn);
													}
													//
													esquerda = esquerda + "_" + PC1[0] + "_" + PC2[0] + "_" + PC3[0] + "/NS"+nivelsn;
													ppalavras++;
													ppalavras++;
													ppalavras++;
												} else
													if ((PC0[1].startsWith("NS")) && (PC1[1].equals("VG")) && (PC2[1].equals("MD")))//***?
													{//não dispara se "," categorizado como PN, não implementado em phython
														esquerda = esquerda + "_" + PC1[0] + "_" + PC2[0] + "/"+ PC0[1];
														ppalavras++;
														ppalavras++;
														System.out.println("Regra VG disparando!");
													} else
														if ((PC0[1].startsWith("NS")) && (PC1[1].equals("VG")) && (PC2[1].equals("de")) && (PC3[1].equals("MD")))//***?
														{//não dispara se "," categorizado como PN, não implementado em phython
															esquerda = esquerda + "_" + PC1[0] + "_" + PC2[0] + "_"
																	+ PC3[0] + "/"+ PC0[1];
															ppalavras++;
															ppalavras++;
															ppalavras++;
															System.out.println("Regra VG disparando!");
														}
														else
															if ((PC0[1].startsWith("NS")) && (PC1[1].equals("co")) && (PC2[1].startsWith("NS"))) //***SN3,4,5+
															{
																//2 em diante
																int nivelfirst = Integer.parseInt(PC0[1].substring(2));
																int nivelsec = Integer.parseInt(PC2[1].substring(2));
																int indexfirst = sns.get(nivelfirst).indexOf(new SNData(PC0[0],0));
																int countesq = WordList.howManyStringsIn(PC0[0], esquerda);//
																int inicio = sns.get(nivelfirst).get(indexfirst).getIndiceInicio(countesq-1);
																nivelsn = ((nivelfirst>=1)?nivelfirst:1) + ((nivelsec>=1)?nivelsec:1);
																nivelsn = (nivelsn<=5 && nivelsn>=0)?nivelsn:5;
																String sn = PC0[0] + "_" + PC1[0] + "_" + PC2[0];
																System.out.println("NS -> NS co NS => "+sn);
																int index = sns.get(nivelsn).indexOf(new SNData(sn,0));
																if(index >= 0){//sn já na lista
																	sns.get(nivelsn).get(index).addPosition(inicio);

																}else{//sn não está na lista
																	index = sns.get(nivelsn).size();
																	sns.get(nivelsn).add(new SNData(sn,inicio));
																	sns.get(nivelsn).get(index).setNivel(nivelsn);
																}
																//
																esquerda = esquerda + "_" + PC1[0] + "_" + PC2[0] + "/NS"+nivelsn;
																ppalavras++;
																ppalavras++;
															} else
																if ((PC0[1].startsWith("NS")) && (PC1[1].equals("co")) &&
																		(PC2[1].equals("de")) && (PC3[1].startsWith("NS"))) //***SN3,4,5+
																{

																	//nivel 2 em diante
																	int nivelfirst = Integer.parseInt(PC0[1].substring(2));
																	int nivelsec = Integer.parseInt(PC3[1].substring(2));
																	int indexfirst = sns.get(nivelfirst).indexOf(new SNData(PC0[0],0));
																	int countesq = WordList.howManyStringsIn(PC0[0], esquerda);//
																	int inicio = sns.get(nivelfirst).get(indexfirst).getIndiceInicio(countesq-1);
																	nivelsn = ((nivelfirst>=1)?nivelfirst:1) + ((nivelsec>=1)?nivelsec:1);
																	nivelsn = (nivelsn<=5 && nivelsn>=0)?nivelsn:5;
																	String sn = PC0[0] + "_" + PC1[0] + "_" + PC2[0]+ "_" + PC3[0];
																	System.out.println("NS -> NS co de NS => "+sn);
																	int index = sns.get(nivelsn).indexOf(new SNData(sn,0));
																	if(index >= 0){//sn já na lista
																		sns.get(nivelsn).get(index).addPosition(inicio);

																	}else{//sn não está na lista
																		index = sns.get(nivelsn).size();

																		sns.get(nivelsn).add(new SNData(sn,inicio));

																		sns.get(nivelsn).get(index).setNivel(nivelsn);
																	}
																	//
																	esquerda = esquerda + "_" + PC1[0] + "_" +
																			PC2[0] + "_" + PC3[0] + "/NS"+nivelsn;
																	ppalavras++;
																	ppalavras++;
																	ppalavras++;
																} else
																	if ((PC0[1].equals("AV")) && (PC1[1].startsWith("NS")))//***SN1+
																	{//regra não altera o nível do SN
																		nivelsn = Integer.parseInt(PC1[1].substring(2));//tags de tamanho 2
																		String sn = PC0[0] + "_" + PC1[0];
																		System.out.println("NS -> AV NS => "+sn);
																		int index = sns.get(nivelsn).indexOf(new SNData(sn,0));
																		int indexsnant = sns.get(nivelsn).indexOf(new SNData(PC1[0],0));
																		int countesq = WordList.howManyStringsIn(PC1[0], esquerda);//
																		int indiceinicio = sns.get(nivelsn).get(indexsnant).getIndiceInicio(countesq-1);
																		//int indiceinicio = sns.get(nivelsn).get(indexsnant).getIndiceInicio();

																		if(index >= 0){//sn já na lista, adiciona posição
																			sns.get(nivelsn).get(index).addPosition(indiceinicio);
																		}else{//sn não está na lista, adiciona no fim
																			index = sns.get(nivelsn).size();
																			sns.get(nivelsn).add(index,new SNData(sn,indiceinicio-PC0[0].length()-1-3));//espaco e tag

																			sns.get(nivelsn).get(index).setNivel(nivelsn);
																		}
																		//
																		esquerda = esquerda + "_" + PC1[0] + "/"+PC1[1];
																		ppalavras++;
																	} else
																		if ((PC0[1].equals("de")) && (PC1[1].startsWith("NS")))//***SN1+
																		{//regra não altera o nível do SN
																			nivelsn = Integer.parseInt(PC1[1].substring(2));//tags de tamanho 2
																			String sn = PC0[0] + "_" + PC1[0];
																			System.out.println("NS -> de NS => "+sn);
																			int index = sns.get(nivelsn).indexOf(new SNData(sn,0));
																			int indexsnant = sns.get(nivelsn).indexOf(new SNData(PC1[0],0));
																			int countesq = WordList.howManyStringsIn(PC1[0], esquerda);//
																			int indiceinicio = sns.get(nivelsn).get(indexsnant).getIndiceInicio(countesq-1);


																			if(index >= 0){//sn já na lista, adiciona posição
																				sns.get(nivelsn).get(index).addPosition(indiceinicio);//na verdade não precisa fazer nada
																			}else{//sn não está na lista, adiciona no fim
																				index = sns.get(nivelsn).size();
																				sns.get(nivelsn).add(index,new SNData(sn,indiceinicio-PC0[0].length()-1-3));//espaco e tag
																				sns.get(nivelsn).get(index).setNivel(nivelsn);
																			}
																			//
																			esquerda = esquerda + "_" + PC1[0] + "/"+PC1[1];
																			ppalavras++;
																		}
																		else
																		{
																			esquerda = esquerda + "/" + PC0[1];
																		}
				}
				ppalavras++;
			}
			esquerda = esquerda.trim();
			if (!frase.equals(esquerda)) alterado = 1;
			frase = esquerda;
		}
		frase = frase.replace("/NS", "/SN");
		rst = frase;
		System.out.println(rst);
		//return rst;
		return sns;
	}


	//resultado - frase_etiquetada_analisada com uma etiqueta por palavra
	//resultado = AnalisaFraseEtiquetada(s); //A partir desta linha extração do sintagma de uma frase
	//usado por ExtraiSNTexto
	private static Vector<String> extraiSNIdentificado(String resultado){
		Vector<String> resultados = new Vector<String>();
		String SN = "";
		String[] palavras = resultado.split(" ");
		String palavra = null;
		for(int i=0; i < palavras.length; i++)//foreach (String palavra in palavras)
		{
			palavra = palavras[i];
			String[] PC = palavra.split("/");
			if (PC.length>1) if (PC[1].equals("SN")) {
				PC[0]=substituiContracoes(PC[0]);
				SN = " "+PC[0].replace("_"," ").replace("+"," ").replace("="," ")+" ";
				//if (!(resultados.contains(SN)))
				//{
				resultados.add(SN); //permite duplicatas na lista
				//}
			};
		}
		return resultados;

	}

	private static HashMap<String,Integer> extraiSNIdentificadoIndice(String resultado){
		HashMap<String,Integer> resultados = new HashMap<String,Integer>();
		String SN = "";
		resultado = substituiContracoes(resultado);//só substituirá contrações dentro dos sintagmas nominais ou ct
		String[] palavras = resultado.split(" ");
		String palavra = null;
		int indice = 0;
		for(int i=0; i < palavras.length; i++)//foreach (String palavra in palavras)
		{
			palavra = palavras[i];
			String[] PC = palavra.split("/");
			if (PC.length>1) if (PC[1].equals("SN")) {
				//				// Volta com contrações básicas
				String original = new String(PC[0]);
				PC[0]= substituiContracoes(PC[0]);
				SN = " "+PC[0].replace("_"," ").replace("+"," ").replace("="," ")+" ";
				SN = SN.trim();
				//if (!(resultados.contains(SN)))
				//{
				indice = resultado.indexOf(original,indice) - i * 3;//barra mais 2 caracteres da tag sn ou ct anteriores
				resultados.put(SN,new Integer(indice)); //ops hashmap não permite duplicatas na lista, ops indice impreciso devido a contracoes fora de ct ou sn
				//}
			};
		}
		return resultados;

	}
	
	//Utilizado por SNTokenizer e indiretamente por SNAnalyzer
		public static List<SNData>  extraiSNIdentificadoIndiceOrdenado(String texto, String textoMarcado){
			String resultado = textoMarcado;
			List<SNData>  resultados = new Vector<SNData>();
			String SN = new String("");
			resultado = substituiContracoes(resultado);//só substituirá contrações dentro dos sintagmas nominais ou ct
			String[] palavras = resultado.split(" ");
			//String[] palavraso = texto.split(" ");
			String palavra = null;
			int indice = 0, last_indice=0;
			int temp = 0;
			for(int i=0; i < palavras.length; i++)//foreach (String palavra in palavras)
			{
				palavra = palavras[i];
				String[] PC = palavra.split("/");
				if (PC.length>1) if (PC[1].equals("SN")) {
					//				// Volta com contrações básicas
					//String original = new String(PC[0]);
					PC[0]= substituiContracoes(PC[0]);
					SN = " "+PC[0].replace("_"," ").replace("="," ")+" ";
					SN = SN.trim();
					SN = SN.replaceAll("[ ]{2,}", " ");//excesso de espaço em branco
					
					//if (!(resultados.contains(SN)))
					//{
					//indice = resultado.indexOf(original,indice) - i * 3;//barra mais 2 caracteres da tag sn ou ct anteriores
					temp = texto.indexOf(SN,indice);
					if(temp >= indice && indice >= 0){
						indice = temp+SN.length();
						last_indice = indice;
					}else{
						indice = (indice>=0 && indice <= (texto.length()-1))?indice:last_indice;
						int ifinal = indice+SN.length();
						ifinal = ifinal>(texto.length()-1)?(texto.length()-1):ifinal;
						//TODO: oai:repositorio.ufpe.br:123456789/7636
						//java.lang.StringIndexOutOfBoundsException: String index out of range: -1 at java.lang.String.substring
						//****br.ufpe.mtd.util.analizers.JOgma.extraiSNIdentificadoIndiceOrdenado(JOgma.java:1796)
						if(indice > ifinal){
							indice = 0;
							ifinal = texto.length()-1;
						}
							
						String sub = texto.substring(indice, ifinal);
						String[] ss = SN.split(" ");
						int [] iss = new int[ss.length];
						int indexend = indice;
						for(int j=0; j< ss.length; j++){
							iss[j] = sub.indexOf(ss[j]);
							if(ss[j].length() > 2 &&  iss[j]>=0)
							indexend = iss[j] + ss[j].length()-1;
							else{
								iss[j]=-1;
								indexend +=  ss[j].length();
							}
						}
						int indexfirst = -1;
						for(int k=0; k< ss.length; k++){
							if(iss[k] < 0){
								if(k > 1 && iss[k-1] >=0)
									iss[k] = sub.indexOf(ss[k], iss[k-1]+ss[k-1].length());
								else if(k < (iss.length-1) && iss[k+1] >=0){
									iss[k] = sub.indexOf(ss[k], iss[k+1]-ss[k].length());
									if(iss[k] >= iss[k+1])
										iss[k] = -1;
								}
							}
						}
						int count = 0;
						int indexe =-1;
						for(int k=0; k< ss.length; k++){
							if(iss[k] >= 0){
								if(indexfirst < 0)
									indexfirst = k;
								if(indexe < k)
									indexe=k;
							}else
								count++;
						}
						if(indexfirst == 0 && indexe==(ss.length-1)){
							indice = indice+iss[indexfirst];
						}
							
					}
					resultados.add(new SNData(SN,indice)); //ops hashmap não permite duplicatas na lista, ops indice impreciso devido a contracoes fora de ct ou sn
					//}
				}
			}
			return resultados;
		}


//Utilizado por SNTokenizer e indiretamente por SNAnalyzer
	public static List<SNData>  extraiSNIdentificadoIndiceOrdenado(String textoMarcado){
		String resultado = textoMarcado;
		List<SNData>  resultados = new Vector<SNData>();
		String SN = new String("");
		resultado = substituiContracoes(resultado);//só substituirá contrações dentro dos sintagmas nominais ou ct
		String[] palavras = resultado.split(" ");
		String palavra = null;
		int indice = 0;
		for(int i=0; i < palavras.length; i++)//foreach (String palavra in palavras)
		{
			palavra = palavras[i];
			String[] PC = palavra.split("/");
			if (PC.length>1) if (PC[1].equals("SN")) {
				//				// Volta com contrações básicas
				String original = new String(PC[0]);
				PC[0]= substituiContracoes(PC[0]);
				SN = " "+PC[0].replace("_"," ").replace("="," ")+" ";
				SN = SN.trim();
				//if (!(resultados.contains(SN)))
				//{
				indice = resultado.indexOf(original,indice) - i * 3;//barra mais 2 caracteres da tag sn ou ct anteriores
				resultados.add(new SNData(SN,indice)); //indice impreciso devido a contracoes fora de ct ou sn
				//}
			};
		}
		return resultados;
	}


	/**
	 * Susbstitui expressões por contrações no texto etiquetado
	 * @param PC
	 */
	public static String substituiContracoes(String PC) {
		PC = PC.replace("=", "_");
		PC = PC.replace("_a_a_", "_à_");
		PC = PC.replace("_a_as_", "_às_");
		PC = PC.replace("_de_os_", "_dos_");
		PC = PC.replace("_de_o_", "_do_");
		PC = PC.replace("_de_as_", "_das_");
		PC = PC.replace("_de_a_", "_da_");
		PC = PC.replace("_em_os_", "_nos_");
		PC = PC.replace("_em_as_", "_nas_");
		PC = PC.replace("_em_o_", "_no_");
		PC = PC.replace("_em_a_", "_na_");
		PC = PC.replace("_a_o_", "_ao_");
		PC = PC.replace("_a_os_", "_aos_");
		PC = PC.replace("_por_o_", "_pelo_");
		PC = PC.replace("_por_os_", "_pelos_");
		PC = PC.replace("_por_a_", "_pela_");
		PC = PC.replace("_por_as_", "_pelas_");
		return PC;
	}

	private static Vector<String> derivaTextoEtiquetado (String frase){
		//    	Deriva todas as combinações da frase no caso de ambiguidades
		frase.replace('\t',' ');
		frase.trim();
		String delimiterChars = " ";
		String[] words = frase.split(delimiterChars);
		//StringTokenizer words = new StringTokenizer(frase, delimiterChars);
		Vector<String> deriva = new Vector<String>();
		int n = 0;
		int nd = 0;
		String s = null;
		//foreach (String s in words)
		//while (words.hasMoreElements())
		for(int i=0; i < words.length;i++)
		{

			//s=words.nextToken();
			s= words[i];
			//System.out.println(s);
			if (!(s.equals("")) && (s.contains("/")) && !s.equals("/"))//(s != "")
			{
				String[] PC = s.split("/");
				if (deriva.size() < 2500)
				{
					n = PC[1].length() / 2;
					int cd = deriva.size();
					while (n > 1)
					{
						int cdd = cd;
						while (cdd > 0)
						{
							deriva.add(deriva.get(cdd - 1));
							cdd--;
						}
						n = n - 1;
					}
					//System.out.println(deriva.Count);
					n = PC[1].length() / 2;
					nd = deriva.size();
					if (n != 0) nd = nd / n;
					if (nd == 0) nd = 1;
					int contd = 0;
					for (int x = 0; x < nd; x++)
					{
						for (int tag = 0; tag < n; tag++)
						{
							while (deriva.size() - 1 < contd) deriva.add("");
							deriva.set(contd, deriva.get(contd) + " " + PC[0] + "/" + PC[1].substring(tag * 2, tag * 2+2));
							contd++;
						}
					}
				}
				else
				{
					nd = deriva.size();
					for (int x = 0; x < nd; x++)
					{
						if (PC[1].length() > 1)
							deriva.set(x, deriva.get(x) + " " + PC[0] + "/" + PC[1].substring(0, 2));
						else
							deriva.set(x,deriva.get(x) + " " + PC[0] + "/");
					}
				}
			}
		}
		return deriva;
	}

//Chamado por SNTokenizer e indiretamente por SNAnalyzer
	public static List<SNData> extraiSNOrdenadoTextoEtiquetado(String textoEtiquetado){//texto etiquetado
		//Etiquete o texto
		//String  setiq = Frase.Etiquetar(texto);
		String setiq = textoEtiquetado;
		//Deriva todas as combinações da frase no caso de ambiguidades
		Vector<String> deriva = JOgma.derivaTextoEtiquetado(setiq);
		// Analisa o resultado de cada frase
		String resultado = "";
		//String SN = "";
		String s = "";
		ArrayList<SNData> resultados= new ArrayList<SNData>();
		//List<SNData> resultados;
		Enumeration<String> enderiva =deriva.elements();
		//Enumeration<String> ensns = null;//**
		List<SNData> ensns = null;
		String nsn = null;
		while(enderiva.hasMoreElements())//foreach (String s in deriva)
		{
			s = enderiva.nextElement(); 
			resultado = JOgma.identificaSNTextoEtiquetado(s);
			System.out.println(resultado);

			//ensns = JOgma.extraiSNIdentificado(resultado).elements();//**
			ensns = JOgma.extraiSNIdentificadoIndiceOrdenado(resultado);//**
			//			int indexres = 0;
			//			while(ensns.hasMoreElements()){
			//				nsn = ensns.nextElement().trim();
			//				int indextmp = resultados.indexOf(nsn,indexres);
			//				if(indextmp < 0){ //!resultados.contains(nsn), não adicionava duplicatas por causa de deriva, mas removido para manter a ordem de extração
			//					//a inserção tem que ser ordenada de acordo com o indice da primeira palavra do sn
			//					resultados.add(indexres,nsn.trim());
			//					indexres = indexres+1;
			//				}else
			//					indexres= indextmp + 1;
			//			}
			//chama insere
			resultados.addAll(ensns);//não trata duplicatas se deriva gera mais de uma combinação
		}
		return resultados;
	}

	public static HashMap<String,Integer> extraiSNTextoEtiquetado(String textoEtiquetado){//texto etiquetado
		//Etiquete o texto
		//String  setiq = Frase.Etiquetar(texto);
		String setiq = textoEtiquetado;
		//Deriva todas as combinações da frase no caso de ambiguidades
		Vector<String> deriva = JOgma.derivaTextoEtiquetado(setiq);
		// Analisa o resultado de cada frase
		String resultado = "";
		//String SN = "";
		String s = "";
		HashMap<String,Integer> resultados = new HashMap<String,Integer>();
		Enumeration<String> enderiva =deriva.elements();
		//Enumeration<String> ensns = null;//**
		HashMap<String,Integer> ensns = null;
		String nsn = null;
		System.out.println("Numero de derivações da frase etiquetada: "+deriva.size());
		while(enderiva.hasMoreElements())//foreach (String s in deriva)
		{
			s = enderiva.nextElement(); 
			resultado = JOgma.identificaSNTextoEtiquetado(s);
			System.out.println(s);
			//TODO: como usar idcat corretamente
			Vector<Vector<SNData>> snet = JOgma.idCatSNTextoEtiquetado2(s);//tornar operacional***
			System.out.println(snet.toString());

			//ordenando por pontuação
			Vector<SNData> result = SNData.concat(snet);
			Collections.sort(result, SNData.RANK_ORDER);
			System.out.println("sn;"+"\t\tpos;" + "\t\tniv;" + "\t\tfreq;" + "\t\trank");
			for (SNData p : result) {
				System.out.println(p.getSN() + "\t;" + p.getIndiceInicio()+ "\t;" + p.getNivel() + "\t;" + p.getFreq()+ "\t;" + p.getRank());
			}

			//ensns = JOgma.extraiSNIdentificado(resultado).elements();//**
			ensns = JOgma.extraiSNIdentificadoIndice(resultado);//**
			//			int indexres = 0;
			//			while(ensns.hasMoreElements()){
			//				nsn = ensns.nextElement().trim();
			//				int indextmp = resultados.indexOf(nsn,indexres);
			//				if(indextmp < 0){ //!resultados.contains(nsn), não adicionava duplicatas por causa de deriva, mas removido para manter a ordem de extração
			//					//a inserção tem que ser ordenada de acordo com o indice da primeira palavra do sn
			//					resultados.add(indexres,nsn.trim());
			//					indexres = indexres+1;
			//				}else
			//					indexres= indextmp + 1;
			//			}
			//chama insere
			resultados.putAll(ensns);
		}
		////		 remove SN duplicados e escolhe os maiores.
		//		resultadosfinal.clear();
		//		Enumeration<String> enrs =resultados.elements();
		//		String SNR = null;
		//		while(enrs.hasMoreElements())//foreach (String SNR in resultados)
		//		{
		//			SNR = enrs.nextElement();
		//			JaCadastrado = false;
		//			for(int pos=0; pos < resultadosfinal.size();pos++)
		//			{
		//				String SNRV = resultadosfinal.elementAt(pos);
		//				if(SNRV.equals(""))
		//					continue;
		//				if (SNRV.equals(SNR))
		//					JaCadastrado = true;
		//				else{
		//					//o SN é menor e já esta cadastrado
		//					if (SNRV.indexOf(SNR) >= 0)
		//					{
		//						JaCadastrado = true;
		//					}
		//					// o SN é maior que o do cadastrado
		//					if (SNR.indexOf(SNRV) >= 0)
		//					{
		//						if(!resultadosfinal.contains(SNR))
		//							resultadosfinal.setElementAt(SNR, pos);
		//						else
		//							resultadosfinal.setElementAt("", pos);
		//						JaCadastrado = true;
		//					}
		//				}
		//			}
		//			if (!JaCadastrado)
		//				resultadosfinal.add(SNR);
		//		}
		//		while(resultadosfinal.removeElement(""));
		//		return resultadosfinal;
		return resultados;
	}

	/**
	 * @param texto
	 * @return texto formatado para tokenização e etiquetagem
	 */
	private static String formatText(String texto) {//a mesma de treetagger
		texto = texto.replace("/",", "); //substitui /
	    texto = texto.replace("\"", " ");//substitui aspas por espaço
	    texto = texto.replace("'", " ");//substitui aspas
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
//	public static String formatText(String texto) {//antiga
//		texto = texto.replace("/",", ");
//		String [] pontChars = new String [] {",", ".", ":",";","!","?","(",")","[","]","\""};
//		for(int i = 0; i < pontChars.length; i++)
//			texto = texto.replace(pontChars[i]," "+pontChars[i]+" ");
//		texto = texto.replace('\t', ' ');
//		texto = texto.replace('\n', ' ');
//		return texto;
//	}


	public static String etiquetar(String text) {
		String oleDb = "";
		JOgmaEtiquetador etiquetador = JOgmaEtiquetador.getInstance();
		text = formatText(text);
		//  	char[] delimiterChars = {' ', ',', '.', ':','\t','!','?','/','<','>','(',')'};
		String delimiterChars = " ";//" \t";
		text = text.toLowerCase();
		String resultado = new String();
		//    	// inicia o tratamento do texto
		text = text.replace(" do que ", " que "); //forçando
		text = text.replace(" ao ", " a o ");
		text = text.replace(" aos ", " a os ");
		text = text.replace(" pela ", " por a ");
		text = text.replace(" pelas ", " por as ");
		text = text.replace(" pelos ", " por os ");
		text = text.replace(" pelo ", " por o ");
		text = text.replace(" neste ", " em este ");
		text = text.replace(" nisto ", " em isto ");
		text = text.replace(" nums ", " em ums ");
		text = text.replace(" numas ", " em umas ");
		text = text.replace(" num ", " em um ");
		text = text.replace(" numa ", " em uma ");
		text = text.replace(" dums ", " de ums ");
		text = text.replace(" dumas ", " de umas ");
		text = text.replace(" dum ", " de um ");
		text = text.replace(" duma ", " de uma ");
		text = text.replace(" nos ", " em os ");
		text = text.replace(" dos ", " de os ");
		text = text.replace(" do ", " de o ");
		text = text.replace(" das ", " de as ");
		text = text.replace(" da ", " de a ");
		text = text.replace(" nas ", " em as ");
		text = text.replace(" no ", " em o ");
		text = text.replace(" na ", " em a ");
		text = text.replace(" às ", " a as ");
		//conjunto de palavras
		text = text.replace(" a fim de ", " a+fim+de ");
		text = text.replace(" a que ", " a+que ");
		text = text.replace(" a qual ", " a+qual ");
		text = text.replace(" a respeito de ", " a+respeito+de ");
		text = text.replace(" abaixo de ", " abaixo+de ");
		text = text.replace(" acerca de ", " acerca+de ");
		text = text.replace(" acima de ", " acima+de ");
		text = text.replace(" além de ", " além+de ");
		text = text.replace(" antes de ", " antes+de ");
		text = text.replace(" ao invés de ", " ao+invés+de ");
		text = text.replace(" ao redor de ", " ao+redor+de ");
		text = text.replace(" apessar de ", " apessar+de ");
		text = text.replace(" as quais ", " as+quais ");
		text = text.replace(" até a ", " até+a ");
		text = text.replace(" bem como ", " bem+como ");
		text = text.replace(" como também ", " como+também ");
		text = text.replace(" como um ", " como+um ");
		text = text.replace(" de acordo com ", " de+acordo+com ");
		text = text.replace(" debaixo de ", " debaixo+de ");
		text = text.replace(" defronte de ", " defronte+de ");
		text = text.replace(" dentreo de ", " dentreo+de ");
		text = text.replace(" depois de ", " depois+de ");
		text = text.replace(" diante de ", " diante+de ");
		text = text.replace(" e não ", " e+não ");
		text = text.replace(" em cima de ", " em+cima+de ");
		text = text.replace(" em face de ", " em+face+de ");
		text = text.replace(" em frente a ", " em+frente+a ");
		text = text.replace(" em frente de ", " em+frente+de ");
		text = text.replace(" em lugar de ", " em+lugar+de ");
		text = text.replace(" em vez de ", " em+vez+de ");
		text = text.replace(" mas ainda ", " mas+ainda ");
		text = text.replace(" mas também ", " mas+também ");
		text = text.replace(" não obstane ", " não+obstane ");
		text = text.replace(" não obstante ", " não+obstante ");
		text = text.replace(" não sí ", " não+sí ");
		text = text.replace(" não só ", " não+só ");
		text = text.replace(" no caso de ", " no+caso+de ");
		text = text.replace(" no entanto ", " no+entanto ");
		text = text.replace(" o qual ", " o+qual ");
		text = text.replace(" o que ", " o+que ");
		text = text.replace(" os quais ", " os+quais ");
		text = text.replace(" para com ", " para+com ");
		text = text.replace(" perto de ", " perto+de ");
		text = text.replace(" por conseguinte ", " por+conseguinte ");
		text = text.replace(" por isso ", " por+isso ");
		text = text.replace(" por trás de ", " por+trás+de ");
		//text = text.replace('-', ' ');
		text = text.replace("/", " ");
		text = text.replace("@", " ");
		text = text.replace("\'", " ");
		text = text.replace("\"", " ");
		text = text.replace('\u0093', ' ');
		text = text.replace('\u0094', ' ');
		text = text.replace("\'", " ");
		text = text.replace(",", " ,/VG ");
		text = text.replace("]", " ]/PN ");
		text = text.replace("[", " [/PN ");
		text = text.replace(")", " )/PN ");
		text = text.replace("(", " (/PN ");
		text = text.replace(">", " >/PN ");
		text = text.replace("<", " </PN ");
		text = text.replace("=", " =/PN ");
		text = text.replace(".", " ./PN ");
		text = text.replace("!", " !/PN ");
		text = text.replace(":", " :/PN ");
		text = text.replace(";", " ;/PN ");
		text = text.replace("?", " ?/PN ");
		text = text.replace("-se ", "/VB a ele ");
		text = text.replace("-lhe ", "/VB a ele ");
		text = text.replace("-la ", "/VB a ela ");
		text = text.replace("-lo ", "/VB a ele ");
		text = text.replace("-las ", "/VB a elas ");
		text = text.replace("-los ", "/VB a eles ");
		text = text.replace("-las ", "/VB a elas ");
		text = text.replace("-los ", "/VB a eles ");
		//StringTokenizer words = new StringTokenizer(text, delimiterChars);
		String[] words = text.split(delimiterChars);
		String Palavra;
		//String PalavraOriginal;
		String Etiqueta = "";
		String s = null;
		int ContP = 0;
		//while ( (s=words.nextToken()) != null)
		for(int w=0; w < words.length; w++)
		{
			s = words[w];
			ContP++;
			Etiqueta = "";
			Palavra = s;
			//PalavraOriginal = Palavra;
			if (!s.equals("")) {
				//Tex.write(s);
				resultado = resultado + s;
				if (!s.contains("/") && !(s.equals("\n"))) //s!=Environment.NewLine *ver aqui barras sobrando apos PN
					resultado = resultado + "/";//Tex.write("/");

				//número
				try
				{
					if (Integer.parseInt(s) > 0) 
						resultado = resultado + "NA";//Tex.write("NA");
				}
				catch (Exception e){ e.getMessage();}
				// palavras simples
				Palavra = Palavra.replace("\"", " ");
				//System.out.println("[" + Palavra + "]");
				if (Etiqueta.equals(""))
					Etiqueta = etiquetador.buscaPalavra(Palavra, "Gramatica", oleDb);
				if (Etiqueta.equals(""))
				{
					// procura na lista de verbos
					Etiqueta = Etiqueta + etiquetador.buscaPalavra(Palavra, "Verbos", oleDb);
					// procura na lista de nomes
					Etiqueta = Etiqueta + etiquetador.buscaPalavra(Palavra, "Nomes", oleDb);
					if ((Palavra.contains("-")) && (Etiqueta.equals("")))
					{
						Palavra = Palavra.substring(Palavra.lastIndexOf('-') + 1);
						Etiqueta = Etiqueta + etiquetador.buscaPalavra(Palavra, "Nomes", oleDb);
					}
					if (Etiqueta.equals(""))
					{
						if (Palavra.endsWith("íssimo")) Etiqueta = "AJ";
						if (Palavra.endsWith("íssima")) Etiqueta = "AJ";
						if (Palavra.endsWith("oso")) Etiqueta = "AJ";
						if (Palavra.endsWith("rimo")) Etiqueta = "AJ";
						if (Palavra.endsWith("inho")) Etiqueta = "AJ";
						if (Palavra.endsWith("inha")) Etiqueta = "AJ";
					}
					//nomes proprios
					//if (PalavraOriginal.Length > 2)
					// if (PalavraOriginal == PalavraOriginal.ToUpper().SubString(0, 1) + PalavraOriginal.toLowerCase().substring(1, PalavraOriginal.length() - 1)) Etiqueta = "NP";
				}
				// Se não encontro a classe classifica como Nome proprio
				if (((Etiqueta.equals("") || Etiqueta.equals("NR"))&& (Palavra.length()>2) && (!Palavra.endsWith("VB")) && (!Palavra.endsWith("PN")) && (!Palavra.endsWith("VG"))))
					Etiqueta = "NP";//SU
				if( Etiqueta.equals("") && Palavra.length() <= 2)
					Etiqueta = "NR"; //provavelmente um marcador como b) f) colocando como número
				//if (s != "")
				if (!Palavra.contains("PN"))
					resultado = resultado + Etiqueta + " ";//Tex.write(Etiqueta + " ");
				else
					resultado = resultado + Etiqueta + " ";//Tex.write(Etiqueta+"\n");
			}
		}
		//Tex.WriteLine();
		return resultado;
	}

	//	opção “TC” que contabiliza também os sintagmas nominais dentro dos outros sintagmas nominais
	//	encontrados. Esta opção utiliza os mesmo parâmetros da opção anterior a “TS” (tabelaSNF). 
	//  Nesta opção também é gerada uma tabela com três colunas: a primeira contém
	//	uma lista de Sintagmas Nominais, a segunda o número de vezes que aquele Sintagma
	//	Nominal aparece no texto, e a terceira o cálculo em relação a todo o documento. A
	//	opção trabalha com dois parâmetros: o primeiro deverá ser a relação de sintagmas
	//	nominais, um em cada linha, e o segundo o arquivo de saída.
	//	Sintaxe:
	//	“ogma tc relacaodesn.txt tabsn.txt”
	//tc - gera tabela de sn etiquetados com n. que aparecem no texto todo
	//  ex: ogma tc relacaosn.txt tabsnf.txt
	public static void tabelaSNFC(String fin, String fout) throws IOException
	{
		BufferedReader textReader;
		FileWriter streamWriter;
		FileReader fr= new FileReader(fin);
		textReader = new BufferedReader(fr);//texto já etiquetado
		streamWriter = new FileWriter(fout, true);
		List<String> list = new ArrayList<String>();
		List<Integer> list2 = new ArrayList<Integer>();
		System.out.println("Analisando sintagmas do arquivo " + fin);
		System.out.println("Escrevendo tabela no arquivo " + fout);
		int nSNunicos = 0;
		int nSNanalisados = 0;
		String text;
		while ((text = textReader.readLine()) != null)
		{
			text = text.toLowerCase();
			String text2 = text.trim();
			if (text2.trim().length() > 2)
			{
				nSNanalisados++;
				if (list.contains(text2))
				{
					List<Integer> list3;
					int index;
					(list3 = list2).set(index = list.indexOf(text2), list3.get(index) + 1);
				}
				else
				{
					nSNunicos += 1;
					list.add(text2);
					list2.add(1);
				}
			}
		}
		int num3 = 0;
		float nSNencontrados = nSNunicos;
		for (String current : (Iterable<String>) list)
		{
			for (String current2 : (Iterable<String>) list)
			{
				if (current2.indexOf(current) > -1)
				{
					List<Integer> list4;
					int index2;
					(list4 = list2).set(index2 = num3, list4.get(index2) + 1);
					nSNencontrados += 1;
				}
			}
			num3++;
		}
		num3 = 0;
		float num5 = 0f;
		System.out.println("Numero de SN analisadas "+nSNanalisados);
		System.out.println("Numero de SN unicos "+ nSNunicos);
		System.out.println("Numero de SN encontrados "+ nSNencontrados);
		for (String current3 : (Iterable<String>) list)
		{
			float num6 = (float)list2.get(num3);
			if (nSNencontrados > 0)
			{
				num5 = num6 / nSNencontrados;
			}

			streamWriter.write(
					current3+
					"/"+
					Integer.toString(list2.get(num3))+
					"/"+ num5);
			num3++;
		}
		textReader.close();
		streamWriter.close();
	}

	//	O processo de extração de SN realizada pelo método ED-CER resulta em uma
	//	lista de SNs na sua forma máxima. Nesta pesquisa utilizou-se também a opção de SN
	//	Aninhados. O SN Aninhado considera também como descritor os núcleos que compõe
	//	o SN máximo.
	//	Por exemplo:
	//	“A gestão do conhecimento nas organizações nacionais” corresponde a um SN
	//	máximo.
	//	Entretanto existem três SN Aninhados:
	//	1) “A Gestão”
	//	2) “conhecimento”
	//	3) “as organizações nacionais.”
	//	Realizou-se então uma adaptação no método “TR”, descrito acima, para
	//	considerar não só o Sintagma Nominal Máximo, mas também todos os aninhados.
	//	Duas opções foram criadas: a “TCA” que realiza a extração dos SN mais os aninhados e
	//	a “TRA” que além desta extração os pontua na metodologia de Souza (2005).
	//	Sintaxe:
	//	“ogma tra relacaodesn.txt tabsn.txt” ou “ogma tca relacaodesn.txt tabsn.txt”
	//Extrai sintagmas de um documento fin e grava tabela no arquivo fout
	//"tra"
	//analisador.etiquetar(args[1], "temp$.txt");
	//analisador.tabelaSNRA("temp$.txt", args[2], args[1], true);
	//"tca"
	//analisador.etiquetar(args[1], "temp$.txt");
	//analisador.tabelaSNRA("temp$.txt", args[2], args[1], false);
	public static void tabelaSNRA(String fin, String fout, String finorg, boolean pontua) throws IOException
	{
		BufferedReader textReader;
		FileWriter streamWriter;
		FileReader fr= new FileReader(fin);
		textReader = new BufferedReader(fr);//texto já etiquetado
		streamWriter = new FileWriter(fout, true);
		List<String> listSN = new ArrayList<String>();
		new ArrayList<String>();
		List<Integer> listfreq = new ArrayList<Integer>();
		List<String> listNivel = new ArrayList<String>();
		System.out.println("Analisando sintagmas do arquivo " + fin);
		System.out.println("Escrevendo tabela no arquivo " + fout);
		int nSNunicos = 0;
		int nSNanalisados = 0;
		int num3 = 0;
		String text;
		while ((text = textReader.readLine()) != null)
		{
			String text2 = text.trim();
			text2 = text2.replace("  ", "|");
			String[] array2 = text2.split("|");

			for (int i = 0; i < array2.length; i++)
			{
				String text4 = array2[i].trim();

				if (text4.length() > 5)
				{
					text4 = text4.replace("VP", "MD");
					text4 = text4.replace("VB", "");
					text4 = text4.replace("AJ", "MD");
					text4 = text4.replace("PS", "MD");
					text4 = text4.replace("PD", "MD");
					text4 = text4.replace("CO", "PR");
					text4 = text4.replace("SU", "RE");
					text4 = text4.replace("PP", "RE");
					text4 = text4.replace("NP", "RE");
					nSNanalisados++;

					String text5 = text4.replace("PR", "P;") + " /P;";
					String[] array3 = text5.split(";");
					for (int j = 0; j < array3.length; j++)
					{
						String text6 = array3[j].trim();
						if (array3[j].lastIndexOf(' ') > 0)
						{
							text6 = array3[j].substring((0), (0) + (array3[j].lastIndexOf(' '))).trim();
						}
						if (text6.length() > 2)
						{
							if (listSN.contains(text6))
							{
								List<Integer> list4;
								int index;
								(list4 = listfreq).set(index = listSN.indexOf(text6), list4.get(index) + 1);
								if (listfreq.get(listSN.indexOf(text6)) > num3)
								{
									num3 = listfreq.get(listSN.indexOf(text6));
								}
							}
							else
							{
								nSNunicos += 1;
								listSN.add(text6);
								listfreq.add(0);
								String item = "1a";
								if (array3[j].contains(" ") && WordList.howManyStringsIn("RE", text6) > 0 && WordList.howManyStringsIn("MD", text6) > 0)
								{
									item = "1b";
								}
								listNivel.add(item);
							}
						}
					}
					if (listSN.contains(text4))
					{
						List<Integer> list5;
						int index2;
						(list5 = listfreq).set(index2 = listSN.indexOf(text4), list5.get(index2) + 1);
						if (listfreq.get(listSN.indexOf(text4)) > num3)
						{
							num3 = listfreq.get(listSN.indexOf(text4));
						}
					}
					else
					{
						nSNunicos += 1;
						listSN.add(text4);
						listfreq.add(0);
						String item2 = "1a";
						if (text4.contains(" "))
						{
							if (WordList.howManyStringsIn("RE", text4.trim()) > 0 && WordList.howManyStringsIn("MD", text4.trim()) > 0)
							{
								item2 = "1b";
							}
							if (WordList.howManyStringsIn("RE", text4.trim()) > 1 && WordList.howManyStringsIn("PR", text4.trim()) > 0)
							{
								item2 = "2";
							}
							if (WordList.howManyStringsIn("PR", text4.trim()) > 1)
							{
								item2 = "3";
							}
							if (WordList.howManyStringsIn("PR", text4.trim()) > 2)
							{
								item2 = "4";
							}
							if (WordList.howManyStringsIn("PR", text4.trim()) > 3)
							{
								item2 = "5";
							}
						}
						listNivel.add(item2);
					}
				}
			}
		}
		int num4 = 0;
		float nSNencontrados = nSNunicos;
		for (int k = 0; k < listSN.size(); k++)
		{
			String text7 = listSN.get(k);
			String text8 = text7;
			if (text7.contains("/"))
			{
				text8 = "";
				String[] array4 = text7.split(" ");
				for (int l = 0; l < array4.length; l++)
				{
					if (array4[l].contains("/"))
					{
						String[] array5 = array4[l].split("/");
						text8 = text8 + " " + array5[0].trim();
					}
				}
			}
			text8 = " " + text8;
			text8 = text8.replace(" de os ", " dos ");
			text8 = text8.replace(" de o ", " do ");
			text8 = text8.replace(" de as ", " das ");
			text8 = text8.replace(" de a ", " da ");
			text8 = text8.replace(" em as ", " nas ");
			text8 = text8.replace(" em o ", " no ");
			text8 = text8.replace(" em a ", " na ");
			listSN.set(k, text8.trim());
		}
		for (String current : (Iterable<String>) listSN)
		{
			if (current.trim().length() > 2)
			{
				for (String current2 : (Iterable<String>) listSN)
				{
					if (current2.indexOf(current) > -1)
					{
						List<Integer> list6;
						int index3;
						(list6 = listfreq).set(index3 = num4, list6.get(index3) + 1);
						if (listfreq.get(num4) > num3)
						{
							num3 = listfreq.get(num4);
						}
						nSNencontrados += 1f;
					}
				}
			}
			num4++;
		}
		num4 = 0;
		float pontSN = 0f;
		System.out.println("Numero de SN analisadas "+ nSNanalisados);
		System.out.println("Numero de SN unicos "+ nSNunicos);
		System.out.println("Numero de SN encontrados "+ nSNencontrados);
		for (String s : (Iterable<String>) listSN)
		{
			float num7 = (float)listfreq.get(num4);
			float num8 = 1f;
			if (pontua)
			{
				if ("1a".equals(listNivel.get(num4)))
				{
					num8 = 0.2f;
				}
				if ("1b".equals(listNivel.get(num4)))
				{
					num8 = 0.8f;
				}
				if ("2".equals(listNivel.get(num4)))
				{
					num8 = 1.1f;
				}
				if ("3".equals(listNivel.get(num4)))
				{
					num8 = 1.4f;
				}
				if ("4".equals(listNivel.get(num4)))
				{
					num8 = 1.2f;
				}
				if ("5".equals(listNivel.get(num4)))
				{
					num8 = 0.8f;
				}
			}
			if (num3 > 0)
			{
				pontSN = num7 / (float)num3 * num8;
			}

			streamWriter.write(
					listSN.get(num4)+
					"/"+
					Integer.toString(listfreq.get(num4))+
					"/"+
					pontSN+"/" + listNivel.get(num4)+"\n");
			num4++;
		}
		textReader.close();
		streamWriter.close();
	}


	//    Gera uma tabela contendo na primeira coluna a lista de todos os termos utilizados,
	//    na segunda coluna o número de vezes na qual o termo aparece em todo o texto,
	//    e na terceira coluna a porcentagem que aquele termo responde na composição de todo o documento.
	//    Para utilizar esta opção, “tt”, deve-se
	//    fornecer como primeiro parâmetro o texto a ser analisado, e como segundo parâmetro o arquivo de saída.
	//    Sintaxe:
	//    “ogma tt textooriginal.txt tabtermos.txt”
	//    Gera tabela ignorando as palavras que aparecem na lista de stopwords. A relação completa das palavras que
	//    compõem esta lista encontra-se no Anexo II de Maia 2008. Para gerar esta tabela,
	//    utiliza-se a opção “tts”, com dois parâmetros: o primeiro o arquivo texto de entrada e
	//    o segundo o arquivo de saída na qual será armazenada a tabela. O arquivo de saída
	//    possui também 3 colunas e segue a especificação da tabela gerada pela opção de
	//    termos, “tt”.
	//    Sintaxe:
	//    “ogma tts textooriginal.txt tabtermos.txt”

	//tt - gera tabela de termos com número de vezes que aparecem no texto
	//  ex: ogma tt texto.txt tabtermos.txt
	//tts - gera tabela de termos com n. de vezes que aparecem no texto (filtra stopwords) ");
	//  ex: ogma tt texto.txt tabtermos.txt");
	//it - calcula a similaridade entre dois textos (met termo)
	//  ex: ogma i texto1.txt texto2.txt");
	public static void tabelaTF(String fin, String fout, boolean stopword) throws IOException
	{
		String oleDbConnection = "";
		FileReader fr= new FileReader(fin);
		BufferedReader textReader = new BufferedReader(fr);
		FileWriter streamWriter = new FileWriter(fout, true);
		List<String> listterm = new ArrayList<String>();
		List<Integer> listfreq = new ArrayList<Integer>();
		System.out.println("Analisando palavras do arquivo " + fin);
		System.out.println("Escrevendo tabela no arquivo " + fout);
		String separator = " ";
		int ntermos = 0;
		int npal = 0;
		String text;
		while ((text = textReader.readLine()) != null) //para cada linha do arquivo
		{
			text = text.toLowerCase();
			text = text.replace("\t", " ");
			text = text.replace("/", " ");
			text = text.replace(".", " ");
			text = text.replace(";", " ");
			text = text.replace(":", " ");
			text = text.replace(",", " ");
			text = text.replace(")", " ");
			text = text.replace("(", " ");
			text = text.replace(">", " ");
			text = text.replace("<", " ");
			text = text.replace("=", " ");
			text = text.replace(".", " ");
			text = text.replace("?", " ");
			text = text.replace("\"", " ");
			text = text.replace("'", " ");
			String[] arrayTerm = text.split(new String(separator)); //separa as palavras da linha

			for (int i = 0; i < arrayTerm.length; i++)
			{
				String text2 = arrayTerm[i].trim();
				String a = "";
				if (stopword)
				{
					a = JOgmaEtiquetador.getInstance().buscaPalavra(text2, "Gramatica", oleDbConnection);
				}
				if (text2.length() > 2 && "".equals(a))
				{
					npal++;
					if (listterm.contains(text2)) //se a palavra já está na lista de termos
					{
						List<Integer> listfreq1 ;
						int index;
						(listfreq1= listfreq).set(index = listterm.indexOf(text2),listfreq1.get(index) + 1);

					}
					else //adiciona palavra a lista de termos
					{
						ntermos += 1;
						listterm.add(text2);
						listfreq.add(1);
					}
				}
			}
		}
		int index = 0;
		float num4 = 0f;
		System.out.println("Numero de palavras analisadas "+ npal);
		System.out.println("Numero de palavras unicas "+ ntermos);
		for (String current : (Iterable<String>) listterm)
		{
			float freqterm = (float)listfreq.get(index);
			if (ntermos > 0)
			{
				num4 = freqterm / ntermos;
			}

			streamWriter.write(
					current+
					"/"+
					Integer.toString(listfreq.get(index))+
					"/"+num4+"\n"
					);
			index++;
		}
		textReader.close();
		streamWriter.close();
		//oleDbConnection.Close();
	}

	//Compara dois arquivos usando o coseno como métrica de similaridade
	//Usado por:
	//i - calcula a similaridade entre duas tabelas
	//  ex: ogma i tabela1.txt tabela2.txt
	//it - calcula a similaridade entre dois textos (met termo)
	//  ex: ogma i texto1.txt texto2.txt");
	//ir - calcula a similaridade entre dois textos (met SN pont)
	//  ex: ogma i texto1.txt texto2.txt");
	//ic - calcula a similaridade entre dois textos (met SN todo texto)
	//  ex: ogma i texto1.txt texto2.txt");
	//Tabela são a saída de tabelaTF,tabelaSNR,tabelaSNFC
	//Recebe dois arquivos de tabela e um arquivo onde será gravado a similaridade
	//fsim string vazia imprime somente na tela a similaridade
	public void similaridade(String ftaba, String ftabb, String fsim) throws IOException
	{
		List<String> listfaba = new ArrayList<String>();
		List<String> list2 = new ArrayList<String>();
		List<String> listftabb = new ArrayList<String>();
		List<String> list4 = new ArrayList<String>();
		List<String> list5 = new ArrayList<String>();
		System.out.println("Comparando o arquivo " + ftaba + " com " + ftabb);
		BufferedReader textReader;
		FileWriter streamWriter;
		FileReader fr= new FileReader(ftaba);
		textReader = new BufferedReader(fr);

		String text;
		//carregando ftaba
		while ((text = textReader.readLine()) != null)
		{
			if (text.length() > 1)
			{
				listfaba.add(text);
			}
		}
		textReader.close();
		fr.close();
		fr= new FileReader(ftabb);
		textReader = new BufferedReader(fr);
		//carregando ftabb
		while ((text = textReader.readLine()) != null)
		{
			if (text.length() > 1)
			{
				listftabb.add(text);
			}
		}
		textReader.close();
		fr.close();
		String[] array3 = null;
		String[] array4 = null;
		String text2 = "";
		int i = 0;
		int j = 0;
		while (i < listfaba.size())
		{
			array3 = listfaba.get(i).split("/");
			j = 0;
			boolean flag = false;
			while (j < listftabb.size())
			{
				array4 = listftabb.get(j).split("/");
				if (array3[0].equals(array4[0]) && array3[0].length() > 1)
				{
					flag = true;
					text2 = array4[2];
				}
				j++;
			}
			if (!flag && array3[0].length() > 1)
			{
				list2.add(listfaba.get(i));
			}
			if (flag && array3[0].length() > 1)
			{
				list5.add(array3[0]+
						"/"+
						array3[2]+
						"/"+
						text2);
			}
			i++;
		}
		while (j < listftabb.size())
		{
			array4 = listftabb.get(i).split("/");
			i = 0;
			boolean flag = false;
			while (i < listfaba.size())
			{
				array3 = listfaba.get(i).split("/");
				if (array3[0].equals(array4[0]) && array3[0].length() > 1)
				{
					flag = true;
				}
				i++;
			}
			if (!flag && array4[0].length() > 1)
			{
				list4.add(listftabb.get(i));
			}
			j++;
		}
		listfaba = list2;
		listftabb = list4;
		double num = 0.0;
		double num2 = 0.0;
		double num3 = 0.0;

		for (String current1 : (Iterable<String>) list2)
		{
			array3 = current1.split("/");
			double num4 = Double.parseDouble(array3[2]);
			num4 = Math.pow(num4, 2.0);
			num += num4;
		}
		for (String current2 : (Iterable<String>) list4)
		{
			array4 = current2.split("/");
			double num5 = Double.parseDouble(array4[2]);
			num5 = Math.pow(num5, 2.0);
			num2 += num5;
		}
		for (String current3 : (Iterable<String>) list5)
		{
			array4 = current3.split("/");
			double num6 = Double.parseDouble(array4[1]);
			double num7 = Double.parseDouble(array4[2]);
			num3 += num6 * num7;
			num6 = Math.pow(num6, 2.0);
			num += num6;
			num7 = Math.pow(num7, 2.0);
			num2 += num7;
		}
		double cos = num3 / (Math.sqrt(num) * Math.sqrt(num2));
		System.out.println("Similaridade (cos): "+ cos);
		if (!"".equals(fsim))
		{
			System.out.println("Escrevendo resultado no arquivo " + fsim);
			streamWriter = new FileWriter(fsim, true);
			streamWriter.write(ftaba+
					"/"+
					ftabb+
					"/"+
					cos);
			streamWriter.close();
		}
	}



	//  	A próxima tabela seria a opção “TR” que gera uma tabela com os Sintagmas
	//  	Nominais pontuados de acordo com a metodologia proposta por SOUZA (2005). Para
	//  	cumprir esta função, foi preciso etiquetar novamente a relação de sintagmas nominais,
	//  	fornecida como parâmetro, para descobrir a classe de sintagma nominal (CSN), item
	//  	necessário para o cálculo da pontuação. Esta etiquetagem é realizada internamente
	//  	pelo OGMA para que os parâmetros permanecessem os mesmo das opções de geração
	//  	de tabelas de sintagmas nominais. A tabela segue o mesmo formato das anteriores
	//  	com o acréscimo de uma quarta coluna onde é salva a classificação (CSN) encontrada
	//  	para o sintagma nominal.
	//  	Sintaxe:
	//  	“ogma tr relacaodesn.txt tabsn.txt”
	//Le relação de sintagmas extraidos e etiquetados de um documento fin e grava tabela no arquivo fout
	//tr - gera tabela de sn pontuados (usa etiqueta e tabelaSNR)
	//  ex: ogma tr relacaosn.txt tabsnf.txt
	//ir - calcula a similaridade entre dois textos (met SN pont)
	//  ex: ogma i texto1.txt texto2.txt");
	public static void tabelaSNR(String fin, String fout, String finorg)  throws IOException
	{
		BufferedReader textReader;
		FileWriter streamWriter;
		FileReader fr= new FileReader(fin);
		textReader = new BufferedReader(fr);//texto já etiquetado
		streamWriter = new FileWriter(fout, true);
		List<String> listSN = new ArrayList<String>();
		List<Integer> listfreq = new ArrayList<Integer>();
		List<String> listSNnivel = new ArrayList<String>();
		System.out.println("Analisando sintagmas do arquivo " + fin);
		System.out.println("Escrevendo tabela no arquivo " + fout);
		int nSNunicos = 0;
		int nSNanalisados = 0;
		int num3 = 0;
		String text;

		while ((text = textReader.readLine()) != null)//um sn por linha?
		{
			String text2 = text.trim();
			text2 = text2.replace("  ", "|");//dois espaços?
			String[] array2 = text2.split("|");//?

			for (int i = 0; i < array2.length; i++)
			{
				String text4 = array2[i].trim();

				if (text4.length() > 5)
				{
					text4 = text4.replace("VP", "MD");
					text4 = text4.replace("VB", "");
					text4 = text4.replace("AJ", "MD");
					text4 = text4.replace("PS", "MD");
					text4 = text4.replace("PD", "MD");
					text4 = text4.replace("CO", "PR");
					text4 = text4.replace("SU", "RE");
					text4 = text4.replace("PP", "RE");
					text4 = text4.replace("NP", "RE");
					nSNanalisados++;

					if (listSN.contains(text4))
					{
						List<Integer> list4;
						int index;
						(list4 = listfreq).set(index = listSN.indexOf(text4),list4.get(index) + 1);

						if (listfreq.get(listSN.indexOf(text4)) > num3)
						{
							num3 = listfreq.get(listSN.indexOf(text4));
						}
					}
					else
					{
						nSNunicos += 1;
						listSN.add(text4);
						listfreq.add(0);
						String item = "1a";
						if (text4.contains(" "))
						{
							if (WordList.howManyStringsIn("RE", text4) > 0 && WordList.howManyStringsIn("MD", text4.trim()) > 0)
							{
								item = "1b";
							}
							if (WordList.howManyStringsIn("RE", text4) > 1 && WordList.howManyStringsIn("PR", text4.trim()) > 0)
							{
								item = "2";
							}
							if (WordList.howManyStringsIn("PR", text4) > 1)
							{
								item = "3";
							}
							if (WordList.howManyStringsIn("PR", text4) > 2)
							{
								item = "4";
							}
							if (WordList.howManyStringsIn("PR", text4) > 3)
							{
								item = "5";
							}
						}
						listSNnivel.add(item);
					}
				}
			}
		}

		int num4 = 0;
		float nSNencontrados = nSNunicos;
		for (int j = 0; j < listSN.size(); j++)
		{
			String text5 = listSN.get(j);
			String text6 = text5;
			if (text5.contains("/"))
			{
				text6 = "";
				String[] array3 = text5.split(" "); 
				for (int k = 0; k < array3.length; k++)
				{
					if (array3[k].contains("/"))
					{
						String[] array4 = array3[k].split("/");
						text6 = text6 + " " + array4[0].trim();
					}
				}
			}
			text6 = " " + text6;
			text6 = text6.replace(" de os ", " dos ");
			text6 = text6.replace(" de o ", " do ");
			text6 = text6.replace(" de as ", " das ");
			text6 = text6.replace(" de a ", " da ");
			text6 = text6.replace(" em as ", " nas ");
			text6 = text6.replace(" em o ", " no ");
			text6 = text6.replace(" em a ", " na ");
			listSN.add(j, text6.trim());
		}
		for (String current : (Iterable<String>) listSN)
		{
			if (current.trim().length() > 2)
			{
				for (String current2 : (Iterable<String>) listSN)
				{
					if (current2.indexOf(current) > -1)
					{
						List<Integer> list5;
						int index2;
						(list5 = listfreq).add(index2 = num4, list5.get(index2) + 1);
						if (listfreq.get(num4) > num3)
						{
							num3 = listfreq.get(num4);
						}
						nSNencontrados += 1;
					}
				}
			}
			num4++;
		}
		num4 = 0;
		float num6 = 0f;
		System.out.println("Numero de SN analisados "+ nSNanalisados);
		System.out.println("Numero de SN unicos "+nSNunicos);
		System.out.println("Numero de SN encontrados "+nSNencontrados);
		for (String s : (Iterable<String>) listSN)
		{
			float num7 = (float)listfreq.get(num4);
			float num8 = 0f;
			if ("1a".equals(listSNnivel.get(num4)))
			{
				num8 = 0.2f;
			}
			if ("1b".equals(listSNnivel.get(num4)))
			{
				num8 = 0.8f;
			}
			if ("2".equals(listSNnivel.get(num4)))
			{
				num8 = 1.1f;
			}
			if ("3".equals(listSNnivel.get(num4)))
			{
				num8 = 1.4f;
			}
			if ("4".equals(listSNnivel.get(num4)))
			{
				num8 = 1.2f;
			}
			if ("5".equals(listSNnivel.get(num4)))
			{
				num8 = 0.8f;
			}
			if (num3 > 0)
			{
				num6 = num7 / (float)num3 * num8;
			}

			try {
				streamWriter.write(
						listSN.get(num4).trim()+
						"/"+
						Integer.toString(listfreq.get(num4))+
						"/");
				streamWriter.write(""+num6);
				streamWriter.write("/" + listSNnivel.get(num4)+"\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
			num4++;
		}

		textReader.close();
		streamWriter.close();

	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {

		if(args.length==0){
			System.out.println("Testando...");	
			String sent = new String("O novo cálculo das aposentadorias resulta em valores menores do que os atuais para quem perde o benefício com menos tempo de contribuição e idade.");
			String result = null;
			sent = "Transferência da Informação: análise para valoração de unidades de conhecimento. Entender e medir o valor do conhecimento é uma das mais discutidas e menos compreendidas questões nos estudos sobre a gestão do conhecimento. Porém, se esta dificuldade fica mais explícita ao analisar o conjunto do conhecimento de uma organização em relação ao mercado, torna-se necessária a definição de parâmetros e mecanismos de avaliação de cada unidade de conhecimento disponível, principalmente aquele que, por algum processo, já foi registrado e precisa ser gerenciado, tanto em seu processo de obtenção, armazenamento, acesso e, até mesmo, descarte. Neste artigo, objetivamos descrever questões envolvidas na identificação do valor do conhecimento registrado em função de sua multidimensionalidade funcional e do processo de transmissão de informações.";
			sent = "Dentre as funções sociais e delineados alguns perfis.";
			//sent = "Em relação ao mercado, educação à distância sofre pressões sociais e econômicas.";
			//ogma força a barra substituindo "do que" por "que"

			String [] res = new String[7];
			res[0] = sent;
			res[1]= new String("Apresenta de forma introdutória questões e conceitos fundamentais sobre metadados e a estruturação da descrição padronizada de documentos eletrônicos. Discorre sobre os elementos propostos no Dublin Core e comenta os projetos de catalogação dos recursos da Internet, CATRIONA, InterCat e CALCO.");
			res[2] = new String("Bibliografia internacional seletiva e anotada sobre bibliotecas digitais. Aborda os seguintes aspectos: a) Visionários, principais autores que escreveram sobre a biblioteca do futuro, no período de 1945-1985; b) conceituação de biblioteca digital; c) projetos em andamento na Alemanha, Austrália, Brasil, Canadá, Dinamarca, Espanha, Estados Unidos, Franca, Holanda, Japão, Nova Zelândia, Reino Unido, Suécia e Vaticano; d) aspectos técnicos relativos a construção de uma biblioteca digital: arquitetura do sistema, conversão de dados e escaneamento, marcação de textos, desenvolvimento de coleções, catalogação, classificação/indexação, metadados, referencia, recuperação da informação, direitos autorais e preservação da informação digital; e) principais fontes de reuniões técnicas especificas, lista de discussão, grupos e centros de estudos, cursos e treinamento.");
			res[3] = new String("Apresenta a implantação de recursos multimídia e interface Web no banco de dados desenvolvido para a coleção de vídeos da Videoteca Multimeios, pertencente ao Departamento de Multimeios do Instituto de Artes da UNICAMP. Localiza a discussão conceitual no universo das bibliotecas digitais e propõe alterações na configuração atual de seu banco de dados.");
			res[4] = new String("Este artigo aborda a necessidade de adoção de padrões de descrição de recursos de informação eletrônica, particularmente, no âmbito da Embrapa Informática Agropecuária. O Rural Mídia foi desenvolvido utilizando o modelo Dublin Core (DC) para descrição de seu acervo, acrescido de pequenas adaptações introduzidas diante da necessidade de adequar-se a especificidades meramente institucionais. Este modelo de metadados baseado no Dublin Core, adaptado para o Banco de Imagem, possui características que endossam a sua adoção, como a simplicidade na descrição dos recursos, entendimento semântico universal (dos elementos), escopo internacional e extensibilidade (o que permite sua adaptação as necessidades adicionais de descrição).");
			res[5] = new String("Relato da experiência do Impa na informatização de sua biblioteca, utilizando o software Horizon, e na construção de um servidor de preprints (dissertações de mestrado, teses de doutorado e artigos ainda não publicados) através da participação no projeto internacional Math-Net.");
			res[6] = new String("Descreve as opções tecnológicas e metodológicas para atingir a interoperabilidade no acesso a recursos informacionais eletrônicos, disponíveis na Internet, no âmbito do projeto da Biblioteca Digital Brasileira em Ciência e Tecnologia, desenvolvido pelo Instituto Brasileiro de Informação em Ciência e Tecnologia(IBCT). Destaca o impacto da Internet sobre as formas de publicação e comunicação em C&T e sobre os sistemas de informação e bibliotecas. São explicitados os objetivos do projeto da BDB de fomentar mecanismos de publicação pela comunidade brasileira de C&T, de textos completos diretamente na Internet, sob a forma de teses, artigos de periódicos, trabalhos em congressos, literatura \"cinzenta\",ampliando sua visibilidade e acessibilidade nacional e internacional, e também de possibilitar a interoperabilidade entre estes recursos informacionais brasileiros em C&T, heterogêneos e distribuídos, através de acesso unificado via um portal, sem a necessidade de o usuário navegar e consultar cada recurso individualmente.");

			//somente o resumo que repete biblioteca digital
			int i = 0;
			//for(int i=6; i <res.length;i++){//res.length
				result = JOgma.etiquetar(res[i]);
				System.out.println(result);
				System.out.println(JOgma.extraiSNTextoEtiquetado(result).toString());
				System.out.println("------------------------------------------------------------------");

			//}


			return;


		}

		if(false)
			if (true)
			{
				System.out.println("\nAJUDA:");
				System.out.println("Informe o comando e os parametros!");
				System.out.println("Comandos disponÃ­veis:");
				System.out.println("e - Etiquetar");
				System.out.println("  ex: ogma e texto.txt textoetiquetado.txt");
				System.out.println("s - Extrair os Sintagmas Nominais e gravar em um arquivo");
				System.out.println("  ex: ogma s textoetiquetado.txt relacaosn.txt");
				System.out.println("x - Mostrar Sintagmas Nominais do arquivo");
				System.out.println("  ex: ogma x texto.txt");
				System.out.println("tt - gera tabela de termos com n. de vezes que aparecem no texto");
				System.out.println("  ex: ogma tt texto.txt tabtermos.txt");
				System.out.println("tts - gera tabela de termos com n. de vezes que aparecem no texto (filtra stopwords) ");
				System.out.println("  ex: ogma tt texto.txt tabtermos.txt");
				System.out.println("ts - gera tabela de sn etiquetados com n. de vezes que aparecem");
				System.out.println("  ex: ogma ts relacaosn.txt tabsnf.txt");
				System.out.println("tc - gera tabela de sn etiquetados com n. que aparecem no texto todo");
				System.out.println("  ex: ogma tc relacaosn.txt tabsnf.txt");
				System.out.println("tr - gera tabela de sn pontuados");
				System.out.println("  ex: ogma tr relacaosn.txt tabsnf.txt");
				System.out.println("i - calcula a similaridade entre duas tabelas");
				System.out.println("  ex: ogma i tabela1.txt tabela2.txt");
				System.out.println("it - calcula a similaridade entre dois textos (met termo)");
				System.out.println("  ex: ogma i texto1.txt texto2.txt");
				System.out.println("ir - calcula a similaridade entre dois textos (met SN pont)");
				System.out.println("  ex: ogma i texto1.txt texto2.txt");
				System.out.println("ic - calcula a similaridade entre dois textos (met SN todo texto)");
				System.out.println("  ex: ogma i texto1.txt texto2.txt");
			}

		String opcao = "", args1 = "", args2="", args3="";
		Scanner scan = new Scanner(System.in);
		opcao = scan.next();
		try{
			switch(opcao){
			case "tt":

				System.out.print("Digite o nome do arquivo: ");
				args1 = scan.next();
				System.out.print("Digite o nome do arquivo: ");
				args2 = scan.next();
				tabelaTF(args1, args2, false);
				break;

			case "tts":

				System.out.print("Digite o nome do arquivo: ");
				args1 = scan.next();
				System.out.print("Digite o nome do arquivo: ");
				args2 = scan.next();
				tabelaTF(args1, args2, true);
				break;

				//			case "ts":
				//			{
				//				tabelaSNF(args1, args2);
				//			}
			case "tc":

				System.out.print("Digite o nome do arquivo: ");
				args1 = scan.next();
				System.out.print("Digite o nome do arquivo: ");
				args2 = scan.next();
				tabelaSNFC(args1, args2);
				break;

			case "tr":

				System.out.print("Digite o nome do arquivo: ");
				args1 = scan.next();
				System.out.print("Digite o nome do arquivo: ");
				args2 = scan.next();
				File temp = new File("temp$.txt");
				JOgma.etiquetar(args1, "temp$.txt");
				tabelaSNR("temp$.txt", args2, args1);
				temp.delete();
				break;

			case "tra":

				System.out.print("Digite o nome do arquivo: ");
				args1 = scan.next();
				System.out.print("Digite o nome do arquivo: ");
				args2 = scan.next();
				File temp2 = new File("temp$.txt");
				etiquetar(args1, "temp$.txt");
				tabelaSNRA("temp$.txt", args2, args1, true);
				temp2.delete();
				break;

			case "tca":

				System.out.print("Digite o nome do arquivo: ");
				args1 = scan.next();
				System.out.print("Digite o nome do arquivo: ");
				args2 = scan.next();
				File temp3 = new File("temp$.txt");
				etiquetar(args1, "temp$.txt");
				tabelaSNRA("temp$.txt", args2, args1, false);
				temp3.delete();
				break;

			case "e":

				System.out.print("Digite o nome do arquivo: ");
				args1 = scan.next();
				System.out.print("Digite o nome do arquivo: ");
				args2 = scan.next();
				etiquetar(args1, args2);
				break;

			case "s":

				System.out.print("Digite o nome do arquivo: ");
				args1 = scan.next();
				System.out.print("Digite o nome do arquivo: ");
				args2 = scan.next();
				extraiSN(args1, args2);
				break;

			}
		}catch(IOException e){
			e.printStackTrace();
		}

		//			if ("i".equals(args[0]))
		//			{
		//				if (args.Count<String>() == 4)
		//				{
		//					analisador.similaridade(args[1], args[2], args[3]);
		//				}
		//				if (args.Count<String>() == 3)
		//				{
		//					analisador.similaridade(args[1], args[2], "");
		//				}
		//			}
		//			if ("it".equals(args[0]))
		//			{
		//				analisador.tabelaTF(args[1], "temp1.$$$", false);
		//				analisador.tabelaTF(args[2], "temp2.$$$", false);
		//				if (args.Count<String>() == 4)
		//				{
		//					analisador.similaridade("temp1.$$$", "temp2.$$$", args[3]);
		//				}
		//				if (args.Count<String>() == 3)
		//				{
		//					analisador.similaridade("temp1.$$$", "temp2.$$$", "");
		//				}
		//				File.delete("temp1.$$$");
		//				File.delete("temp2.$$$");
		//			}
		//			if ("its".equals(args[0]))
		//			{
		//				analisador.tabelaTF(args[1], "temp1.$$$", true);
		//				analisador.tabelaTF(args[2], "temp2.$$$", true);
		//				if (args.Count<String>() == 4)
		//				{
		//					analisador.similaridade("temp1.$$$", "temp2.$$$", args[3]);
		//				}
		//				if (args.Count<String>() == 3)
		//				{
		//					analisador.similaridade("temp1.$$$", "temp2.$$$", "");
		//				}
		//				File.delete("temp1.$$$");
		//				File.delete("temp2.$$$");
		//			}
		//			if ("ir".equals(args[0]))
		//			{
		//				analisador.etiquetar(args[1], "temp1e.$$$");
		//				analisador.extraiSN("temp1e.$$$", "temp1s.$$$");
		//				analisador.etiquetar("temp1s.$$$", "temp1se.$$$");
		//				analisador.tabelaSNR("temp1se.$$$", "temp1.$$$", args[1]);
		//				analisador.etiquetar(args[2], "temp2e.$$$");
		//				analisador.extraiSN("temp2e.$$$", "temp2s.$$$");
		//				analisador.etiquetar("temp2s.$$$", "temp2se.$$$");
		//				analisador.tabelaSNR("temp2se.$$$", "temp2.$$$", args[1]);
		//				if (args.Count<String>() == 4)
		//				{
		//					analisador.similaridade("temp1.$$$", "temp2.$$$", args[3]);
		//				}
		//				if (args.Count<String>() == 3)
		//				{
		//					analisador.similaridade("temp1.$$$", "temp2.$$$", "");
		//				}
		//				File.delete("temp1e.$$$");
		//				File.delete("temp2e.$$$");
		//				File.delete("temp1se.$$$");
		//				File.delete("temp2se.$$$");
		//				File.delete("temp1s.$$$");
		//				File.delete("temp2s.$$$");
		//				File.delete("temp1.$$$");
		//				File.delete("temp2.$$$");
		//			}
		//			if ("ic".equals(args[0]))
		//			{
		//				analisador.etiquetar(args[1], "temp1e.$$$");
		//				analisador.extraiSN("temp1e.$$$", "temp1s.$$$");
		//				analisador.tabelaSNFC("temp1s.$$$", "temp1.$$$");
		//				analisador.etiquetar(args[2], "temp2e.$$$");
		//				analisador.extraiSN("temp2e.$$$", "temp2s.$$$");
		//				analisador.tabelaSNFC("temp2s.$$$", "temp2.$$$");
		//				if (args.Count<String>() == 4)
		//				{
		//					analisador.similaridade("temp1.$$$", "temp2.$$$", args[3]);
		//				}
		//				if (args.Count<String>() == 3)
		//				{
		//					analisador.similaridade("temp1.$$$", "temp2.$$$", "");
		//				}
		//				File.delete("temp1e.$$$");
		//				File.delete("temp2e.$$$");
		//				File.delete("temp1s.$$$");
		//				File.delete("temp2s.$$$");
		//				File.delete("temp1.$$$");
		//				File.delete("temp2.$$$");
		//			}
		////			Visualização rápida dos sintagmas nominais sem necessitar de passar pelas duas etapas anteriores.
		////			Esta opção “x” recebe apenas um parâmetro relativo ao texto que se pretende analisar.
		////			Sintaxe:
		////			“ogma x textooriginal.txt”
		//			if ("x".equals(args[0]))
		//			{
		//				analisador.etiquetar(args[1], "temp$.txt");
		//				analisador.extraiSN("temp$.txt", "temp$$.txt");
		//				File.delete("temp$.txt");
		//				File.delete("temp$$.txt");
		//			}
		//			if ("q".equals(args[0]))
		//			{
		//				analisador.quadro(args[1], Integer.parseInt(args[2]), Integer.parseInt(args[3]));
		//			}
		//			System.out.println("------------------------------------------------------------");
		//		}



		if(false){
			String res0= new String("Cada homem bom tem seu caminho");
			String res1= new String("Apresenta de forma introdutória questões e conceitos fundamentais sobre metadados e a estruturação da descrição padronizada de documentos eletrônicos. Discorre sobre os elementos propostos no Dublin Core e comenta os projetos de catalogação dos recursos da Internet, CATRIONA, InterCat e CALCO.");

			String res2 = new String("Bibliografia internacional seletiva e anotada sobre bibliotecas digitais. Aborda os seguintes aspectos: a) Visionários, principais autores que escreveram sobre a biblioteca do futuro, no período de 1945-1985; b) conceituação de biblioteca digital; c) projetos em andamento na Alemanha, Austrália, Brasil, Canadá, Dinamarca, Espanha, Estados Unidos, Franca, Holanda, Japão, Nova Zelândia, Reino Unido, Suécia e Vaticano; d) aspectos técnicos relativos a construção de uma biblioteca digital: arquitetura do sistema, conversão de dados e escaneamento, marcação de textos, desenvolvimento de coleções, catalogação, classificação/indexação, metadados, referencia, recuperação da informação, direitos autorais e preservação da informação digital; e) principais fontes de reuniões técnicas especificas, lista de discussão, grupos e centros de estudos, cursos e treinamento.");
			String res3 = new String("Apresenta a implantação de recursos multimídia e interface Web no banco de dados desenvolvido para a coleção de vídeos da Videoteca Multimeios, pertencente ao Departamento de Multimeios do Instituto de Artes da UNICAMP. Localiza a discussão conceitual no universo das bibliotecas digitais e propõe alterações na configuração atual de seu banco de dados.");
			String res4 = new String("Este artigo aborda a necessidade de adoção de padrões de descrição de recursos de informação eletrônica, particularmente, no âmbito da Embrapa Informática Agropecuária. O Rural Mídia foi desenvolvido utilizando o modelo Dublin Core (DC) para descrição de seu acervo, acrescido de pequenas adaptações introduzidas diante da necessidade de adequar-se a especificidades meramente institucionais. Este modelo de metadados baseado no Dublin Core, adaptado para o Banco de Imagem, possui características que endossam a sua adoção, como a simplicidade na descrição dos recursos, entendimento semântico universal (dos elementos), escopo internacional e extensibilidade (o que permite sua adaptação as necessidades adicionais de descrição).");
			String res5 = new String("Relato da experiência do Impa na informatização de sua biblioteca, utilizando o software Horizon, e na construção de um servidor de preprints (dissertações de mestrado, teses de doutorado e artigos ainda não publicados) através da participação no projeto internacional Math-Net.");
			String res6 = new String("Descreve as opções tecnológicas e metodológicas para atingir a interoperabilidade no acesso a recursos informacionais eletrônicos, disponíveis na Internet, no âmbito do projeto da Biblioteca Digital Brasileira em Ciência e Tecnologia, desenvolvido pelo Instituto Brasileiro de Informação em Ciência e Tecnologia(IBCT). Destaca o impacto da Internet sobre as formas de publicação e comunicação em C&T e sobre os sistemas de informação e bibliotecas. São explicitados os objetivos do projeto da BDB de fomentar mecanismos de publicação pela comunidade brasileira de C&T, de textos completos diretamente na Internet, sob a forma de teses, artigos de periódicos, trabalhos em congressos, literatura \"cinzenta\",ampliando sua visibilidade e acessibilidade nacional e internacional, e também de possibilitar a interoperabilidade entre estes recursos informacionais brasileiros em C&T, heterogêneos e distribuídos, através de acesso unificado via um portal, sem a necessidade de o usuário navegar e consultar cada recurso individualmente.");

			String fr4 = new String("O novo cálculo das aposentadorias resulta em valores menores do que os atuais para quem perde o benefício com menos tempo de contribuição e idade.");
			String fret4 = new String("o/AD novo/AJ cálculo/SU de/PR as/AD aposentadorias/SU resulta/VB em/PR valores/SU menores/AJSU que/CJPL os/ADPR atuais/VBAJSU para/PR quem/PL perde/VB o/AD benefício/SU com/PR menos/AV tempo/SU de/PR contribuição/SU e/CJ idade/SU ./PN");

			String fret3= new String("evolução/SU de/PR a/AD ciência/SU de/PR a/AD informação/SU (/PN ci/NR )/PN enfocando/VB os/AD problemas/SU surgidos/AJ a/AD o/AD longo/AJ dos/AV tempos/SU ./PN a/AD origem/SU histórica/AJ de/PR a/AD ci/NR é/VB discutida/AJ ,/VG juntamente/AV com/PR seu/PS papel/SU social/AJ em/PR a/AD evolução/SU de/PR a/AD sociedade/SU de/PR a/AD informação/SU ./PN o/AD trabalho/VBSU de/PR recuperação/SU de/PR a/AD informação/SU é/VB analisado/VPAJ em/PR termos/VB de/PR sua/PS influência/SU em/PR o/AD desenvolvimento/SU de/PR a/AD ci/NR e/CJ de/PR a/AD indústria/SU de/PR a/AD informação/SU ./PN a/AD evolução/SU de/PR os/ADPR diferentes/AJ enfoques/VB de/PR o/AD problema/SU é/VB apresentada/AJ e/CJ proposta/AJSU uma/AI definição/SU contemporânea/AJ de/PR ci/NR ./PN são/VB examinadas/AJ as/AD relações/SU interdisciplinares/SU com/PR quatro/NC campos/SU :/PN biblioteconomia/SU ,/VG ciência/SU de/PR a/AD computação/SU ,/VG ciência/SU cognitiva/AJ (/PN incluindo/VB inteligência/SU artificial/AJ )/PN e/CJ comunicação/SU ./PN como/VBAV conclusão/SU são/VB sumariadas/SU algumas/SU questões/SU e/CJ problemas/SU enfrentados/SU contemporaneamente/SU por/PR a/AD ci/NR ./PN");


			//String fret4p = JPalavras.getInstance().Etiquetar(res2);
			//Para obter os sns pelo palavras use http://beta.visl.sdu.dk/visl/pt/parsing/automatic/trees.php
			//Visualization: Source
			//Notational convention: VLSI - Default
			fret4 = JOgma.etiquetar(res1);
			//res = Frase.AnalisaFraseEtiquetada(fret4);
			//		System.out.println(Frase.BuscaPalavra("como","",""));

			//Vector<String> sns = Frase.ExtraiSNs(res);
			//Vector<String> der = Frase.DerivaFraseEtiquetada(fret4);
			Vector<String> snsf = new Vector(JOgma.extraiSNTextoEtiquetado(fret4).keySet());

			//System.out.println(fret);
			System.out.println(fret4);

			//System.out.println(res);
			//System.out.println(der.size());
			//System.out.println(der.toString());
			System.out.println(snsf.toString());

			//System.out.println(fret4p);
			//snsf = JOgma.extraiSNTextoEtiquetado(fret4p);
			//System.out.println(snsf.toString());
		}

		return;

	}

	public static void extraiSN(String arquivoTexto, String arquivoSN) throws IOException{
		String texto = WordList.readFile(arquivoTexto);
		String teq = etiquetar(texto);
		Vector<String> sns = new Vector<String>(extraiSNTextoEtiquetado(teq).keySet());
		String content = "";
		for(int i=0; i < sns.size(); i++){
			content = content.concat(sns.get(i) + "\n"); 
		}
		WordList.printString(arquivoSN, content );
		return;

	}

	//Etiqueta texto de arquivo e escreve texto etiquetado em arquivo
	public static void etiquetar(String arquivoTexto, String arquivoTextoEtiquetado) throws IOException {
		String texto = WordList.readFile(arquivoTexto);
		WordList.printString(arquivoTextoEtiquetado, etiquetar(texto));
		return;

	}

}
