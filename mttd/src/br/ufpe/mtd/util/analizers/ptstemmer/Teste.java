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
		System.out.println(st.getWordStem("canções"));
		System.out.println(st.getWordStem("bençãos"));
		System.out.println(st.getWordStem("canção"));
		System.out.println(st.getWordStem("reptis"));
		System.out.println(st.getWordStem("cantora"));
		System.out.println(st.getWordStem("cantoras"));
		System.out.println(st.getWordStem("carretéis"));
		System.out.println(st.getWordStem("tecnologias"));
		System.out.println(st.getWordStem("tecnologia"));
		System.out.println(st.getWordStem("tecnologicos"));
		System.out.println(st.getWordStem("répteis"));
		System.out.println(st.getWordStem("barris"));
		System.out.println(st.getWordStem("país"));
		System.out.println(st.getWordStem("países"));
		System.out.println(st.getWordStem("mães"));
		System.out.println(st.getWordStem("pai"));
		System.out.println(st.getWordStem("pais"));
		System.out.println(st.getWordStem("engenhos"));
		String input = "./data/words_sample.txt";
		String output = "./data/stems_rslps2.txt";
		PTStemmerUtilities.applyStemmer(st, input, output);
	}
}
