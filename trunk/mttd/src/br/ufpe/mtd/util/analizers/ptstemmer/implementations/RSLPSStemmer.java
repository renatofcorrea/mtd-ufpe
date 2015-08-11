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
package br.ufpe.mtd.util.analizers.ptstemmer.implementations;

import br.ufpe.mtd.util.analizers.ptstemmer.Stemmer;
import br.ufpe.mtd.util.analizers.ptstemmer.exceptions.PTStemmerException;


/**
 * Orengo Stemmer as defined in:<br>
 * V. Orengo and C. Huyck, "A stemming algorithm for the portuguese language," String Processing and Information Retrieval, 2001. SPIRE 2001. Proceedings.Eighth International Symposium on, 2001, pp. 186-193.<br>
 * Added extra stemming rules and exceptions found in:<br>
 * http://www.inf.ufrgs.br/%7Earcoelho/rslp/integrando_rslp.html
 * @author Pedro Oliveira
 *
 */
public class RSLPSStemmer extends OrengoStemmer{

	public RSLPSStemmer() throws PTStemmerException
	{
		loadRules();
	}
	
    @Override
	protected void loadRules() throws PTStemmerException{
		readRulesFromXML("RSLP-SStemmerRules.xml");
	}
	
	@Override
	protected String algorithm(String st)
	{
		String aux, stem = st;
		if(st.length() <1)
			return st;
		
		char end = stem.charAt(stem.length()-1);	
		if(end == 's')
			stem = applyRules(stem, pluralreductionrules);
		end = stem.charAt(stem.length()-1);
		// if(end == 'a' || end == 'ã')
		// 	stem = applyRules(stem, femininereductionrules);
		// stem = applyRules(stem, augmentativediminutivereductionrules);
		// stem = applyRules(stem, adverbreductionrules);
		// aux = stem;
		// stem = applyRules(stem, nounreductionrules);
		// if(aux.equals(stem))
		// {
		// 	stem = applyRules(stem, verbreductionrules);
		//	if(aux.equals(stem))
				stem = applyRules(stem, vowelremovalrules);
		//}
		//stem = removeDiacritics(stem);		
		return stem;
	}

}
