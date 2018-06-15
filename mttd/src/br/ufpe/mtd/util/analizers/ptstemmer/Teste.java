package br.ufpe.mtd.util.analizers.ptstemmer;
/**
 * PTStemmer - A Stemming toolkit for the Portuguese language (C) 2008-2010 Pedro Oliveira
 * 
 * This file is part of PTStemmer.
 * PTStemmer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * PTStemmer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with PTStemmer. If not, see <http://www.gnu.org/licenses/>.
 * 
 */
import java.util.Scanner;

import br.ufpe.mtd.util.analizers.ptstemmer.*;
import br.ufpe.mtd.util.analizers.ptstemmer.exceptions.PTStemmerException;
import br.ufpe.mtd.util.analizers.ptstemmer.implementations.*;
import br.ufpe.mtd.util.analizers.ptstemmer.support.PTStemmerUtilities;


public class Teste {

	public static void main(String[] args) throws PTStemmerException {
		Stemmer st = Stemmer.StemmerFactory(Stemmer.StemmerType.RSLPS);
		//Stemmer st = Stemmer.StemmerFactory(Stemmer.StemmerType.TRUNC);
		//Stemmer st = Stemmer.StemmerFactory(Stemmer.StemmerType.SAVOY);
		//Stemmer st = Stemmer.StemmerFactory(Stemmer.StemmerType.ORENGO);
		//Stemmer st = Stemmer.StemmerFactory(Stemmer.StemmerType.PORTER);
		//st.enableCaching(1000);
		st.ignore("a","e");
//		System.out.println(st.getWordStem("�guas"));
//		System.out.println(st.getWordStem("algoritmos"));
//		System.out.println(st.getWordStem("alunos"));
//		System.out.println(st.getWordStem("ambientais"));
//		System.out.println(st.getWordStem("animais"));
		System.out.println(st.getWordStem("an�is"));
		System.out.println(st.getWordStem("anos"));
		System.out.println(st.getWordStem("aspectos"));
		System.out.println(st.getWordStem("fi�is"));
		System.out.println(st.getWordStem("barris"));
		System.out.println(st.getWordStem("biol�gicas"));
//		System.out.println(st.getWordStem("atividades"));
//		System.out.println(st.getWordStem("ben��os"));
//		System.out.println(st.getWordStem("can��es"));
//		System.out.println(st.getWordStem("can��o"));
//		System.out.println(st.getWordStem("c�lulas"));
//		System.out.println(st.getWordStem("cantora"));
//		System.out.println(st.getWordStem("cantoras"));
		System.out.println(st.getWordStem("caracter�sticas"));
		System.out.println(st.getWordStem("carret�is"));
		System.out.println(st.getWordStem("chap�us"));
		System.out.println(st.getWordStem("ci�ncias"));
		System.out.println(st.getWordStem("condi��es"));
//		System.out.println(st.getWordStem("crian�as"));
		System.out.println(st.getWordStem("dados"));
//		System.out.println(st.getWordStem("direitos"));
		System.out.println(st.getWordStem("efeitos"));
		System.out.println(st.getWordStem("energ�ticas"));
//		System.out.println(st.getWordStem("engenhos"));
//		System.out.println(st.getWordStem("empresas"));
//		System.out.println(st.getWordStem("esp�cies"));
		System.out.println(st.getWordStem("estudos"));
		System.out.println(st.getWordStem("fatores"));
		System.out.println(st.getWordStem("farmaceuticas"));
//		System.out.println(st.getWordStem("fungos"));
		System.out.println(st.getWordStem("fuzis"));
//		System.out.println(st.getWordStem("genes"));
		System.out.println(st.getWordStem("grupos"));
//		System.out.println(st.getWordStem("imagens"));
//		System.out.println(st.getWordStem("lectinas"));
		System.out.println(st.getWordStem("lilases"));
//		System.out.println(st.getWordStem("m�es"));
//		System.out.println(st.getWordStem("mares"));
//		System.out.println(st.getWordStem("mar�s"));
//		System.out.println(st.getWordStem("mercados"));
//		System.out.println(st.getWordStem("m�todos"));
//		System.out.println(st.getWordStem("modelos"));
//		System.out.println(st.getWordStem("mulheres"));
//		System.out.println(st.getWordStem("nanopart�culas"));
		System.out.println(st.getWordStem("nucleares"));
		System.out.println(st.getWordStem("pai"));
		System.out.println(st.getWordStem("pais"));
		System.out.println(st.getWordStem("pa�s"));
		System.out.println(st.getWordStem("pa�ses"));
		System.out.println(st.getWordStem("pacientes"));
		System.out.println(st.getWordStem("past�is"));
		System.out.println(st.getWordStem("pasteis"));//verbo pastar, pastel como cor n�o tem plural
		System.out.println(st.getWordStem("pr�ticas"));
		System.out.println(st.getWordStem("pol�ticas"));
		System.out.println(st.getWordStem("principais"));
		System.out.println(st.getWordStem("processos"));
//		System.out.println(st.getWordStem("projetos"));
//		System.out.println(st.getWordStem("propriedades"));
//		System.out.println(st.getWordStem("ratos"));
//		System.out.println(st.getWordStem("redes"));
		System.out.println(st.getWordStem("rel�s"));
		System.out.println(st.getWordStem("reptis"));
		System.out.println(st.getWordStem("r�pteis"));
		System.out.println(st.getWordStem("repteis"));
		System.out.println(st.getWordStem("sistemas"));
		System.out.println(st.getWordStem("s�tios"));
		System.out.println(st.getWordStem("sociais"));
		System.out.println(st.getWordStem("softwares"));
		System.out.println(st.getWordStem("solos"));
		System.out.println(st.getWordStem("tecnologias"));
		System.out.println(st.getWordStem("tecnologia"));
		System.out.println(st.getWordStem("tecnol�gicos"));
		System.out.println(st.getWordStem("valores"));
		
		
		
		//Em arquivo
//		String input = "./data/words_sample.txt";
//		String output = "./data/stems_rslps2.txt";
//		PTStemmerUtilities.applyStemmer(st, input, output);
	}
}
