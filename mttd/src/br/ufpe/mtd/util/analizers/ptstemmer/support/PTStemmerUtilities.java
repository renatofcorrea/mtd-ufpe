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
package br.ufpe.mtd.util.analizers.ptstemmer.support;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import br.ufpe.mtd.util.analizers.ptstemmer.Stemmer;
import br.ufpe.mtd.util.analizers.ptstemmer.exceptions.PTStemmerException;

public abstract class PTStemmerUtilities {
	
	/**
	 * Parse text file (one word per line) to Set 
	 * @param filename
	 * @return
	 * @throws PTStemmerException
	 */
	public static Set<String> fileToSet(String filename) throws PTStemmerException
	{
		HashSet<String> res = new HashSet<String>();
		String aux;
		try {
			BufferedReader in = new BufferedReader(new FileReader(filename));
			while((aux=in.readLine())!=null)
				res.add(aux.trim().toLowerCase());
			in.close();

		} catch (IOException e) {
			throw new PTStemmerException("Problems opening file "+filename, e);
		}
		return res;
	}
	
	/**
	 * Parse text file (one word per line) to Array of String 
	 * @param filename
	 * @return
	 * @throws PTStemmerException
	 */
	public static String[] fileToArray(String filename) throws PTStemmerException
	{
		ArrayList<String> res = new ArrayList<String>();
		String aux;
		try {
			BufferedReader in = new BufferedReader(new FileReader(filename));
			while((aux=in.readLine())!=null)
				res.add(aux.trim().toLowerCase());
			in.close();

		} catch (IOException e) {
			throw new PTStemmerException("Problems opening file "+filename, e);
		}
		return res.toArray(new String[res.size()]);
	}

	
	/**
	 * Remove diacritics (i.e., accents) from String
	 * @param st
	 * @return
	 */
	public static String removeDiacritics(String st)
	{
		st = Normalizer.normalize(st, Normalizer.Form.NFD);
		return st.replaceAll("[^\\p{ASCII}]","");
	}
	
	/**
	 * Apply stemmer over a text file (one word per line) and write results to a file 
	 * @param stemmer
	 * @param inputfilename
	 * @param outputfilename
	 * @return
	 * @throws PTStemmerException
	 */
	public static void applyStemmer(Stemmer st,String inputfilename,String outputfilename) throws PTStemmerException
	{
		
		String aux;
		try {
			String[] in = fileToArray(inputfilename);
			BufferedWriter out = new BufferedWriter(new FileWriter(outputfilename));
			for(int i=0; i < in.length;i++){
				aux=in[i];
				if(aux.equals("*"))
					out.append(aux+"\n");
				else
				out.append(removeDiacritics(st.getWordStem(aux))+"\n");
			}
			out.flush();
			out.close();
		} catch (IOException e) {
			throw new PTStemmerException("Problems opening file "+outputfilename, e);
		}
		return;
	}

}
