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

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import br.ufpe.mtd.util.analizers.ptstemmer.Stemmer;
import br.ufpe.mtd.util.analizers.ptstemmer.exceptions.PTStemmerException;
import br.ufpe.mtd.util.analizers.ptstemmer.support.XMLUtils;
import br.ufpe.mtd.util.analizers.ptstemmer.support.datastructures.Pair;
import br.ufpe.mtd.util.analizers.ptstemmer.support.datastructures.SuffixTree;


/**
 * Orengo Stemmer as defined in:<br>
 * V. Orengo and C. Huyck, "A stemming algorithm for the portuguese language," String Processing and Information Retrieval, 2001. SPIRE 2001. Proceedings.Eighth International Symposium on, 2001, pp. 186-193.<br>
 * Added extra stemming rules and exceptions found in:<br>
 * http://www.inf.ufrgs.br/%7Earcoelho/rslp/integrando_rslp.html
 * @author Pedro Oliveira
 *
 */
public class OrengoStemmer extends Stemmer{

	protected class Rule
	{
		public int size;
		public String replacement;
		public SuffixTree<Boolean> exceptions;

		public Rule(int size, String replacement, String[] exceptions){
			this.size = size;
			if(replacement != null)
				this.replacement = replacement;
			else
				this.replacement = "";
			if(exceptions == null)
				this.exceptions  = new SuffixTree<Boolean>();
			else
				this.exceptions = new SuffixTree<Boolean>(true, exceptions);
		}	
	}

	public OrengoStemmer() throws PTStemmerException
	{
		loadRules();
	}

	protected void loadRules()throws PTStemmerException{
		readRulesFromXML("OrengoStemmerRules.xml");
	}
	
	protected String stemming(String word)
	{		
		return algorithm(word);
	}
	
	protected String algorithm(String st)
	{
		String aux, stem = st;
		if(st.length() <1)
			return st;
		
		char end = stem.charAt(stem.length()-1);	
		if(end == 's')
			stem = applyRules(stem, pluralreductionrules);
		end = stem.charAt(stem.length()-1);
		if(end == 'a' || end == 'ã')
			stem = applyRules(stem, femininereductionrules);
		stem = applyRules(stem, augmentativediminutivereductionrules);
		stem = applyRules(stem, adverbreductionrules);
		aux = stem;
		stem = applyRules(stem, nounreductionrules);
		if(aux.equals(stem))
		{
			stem = applyRules(stem, verbreductionrules);
			if(aux.equals(stem))
				stem = applyRules(stem, vowelremovalrules);
		}
		//stem = removeDiacritics(stem);		
		return stem;
	}

	protected String applyRules(String st, SuffixTree<Rule> rules) {
		if(st.length() < rules.getProperty("size"))	//If the word is smaller than the minimum stemming size of this step, ignore it
			return st;
		int w = 0;
		List<Pair<String, Rule>> res = rules.getLongestSuffixesAndValues(st);
	
		for(int i=res.size()-1; i>=0; i--)
		{
			Pair<String, Rule> r = res.get(i);
			String suffix = r.a;
			Rule rule = r.b;
	
			if(rules.getProperty("exceptions") == 1){	//Compare entire word with exceptions
				if(rule.exceptions.contains(st))
					continue;
			}else{	//Compare only the longest suffix
				if(rule.exceptions.getLongestSuffixValue(st) != null)
					break;				
			}
			if(st.length() >= suffix.length()+rule.size)
				return st.substring(0, st.length()-suffix.length())+rule.replacement;
		}
		return st;
	}

	protected void readRulesFromXML(String filename) throws PTStemmerException {
		DocumentBuilder builder;
		Document document;
		try {
			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			document = builder.parse(SavoyStemmer.class.getResourceAsStream(filename));
		} catch (Exception e) {
			throw new PTStemmerException("Problem while parsing Orengo's XML stemming rules file.", e);
		}
	
		Element root = document.getDocumentElement();
	
		for(Element step: XMLUtils.getChilds(root))
		{
			if(!step.hasAttribute("name"))
				throw new PTStemmerException("Problem while parsing Orengo's XML stemming rules file: Invalid step.");
	
			String stepName = step.getAttribute("name");
			SuffixTree<Rule> suffixes = new SuffixTree<Rule>();
			XMLUtils.setProperty(suffixes, "size", 0, step);
			XMLUtils.setProperty(suffixes, "exceptions", 0, step);
	
			for(Element rule: XMLUtils.getChilds(step))
			{
				if(!rule.hasAttribute("size") || !rule.hasAttribute("replacement") || !rule.hasAttribute("suffix"))
					throw new PTStemmerException("Problem while parsing Orengo's XML stemming rules file: Invalid rule in "+stepName+".");
	
	
				String suffix = new String(rule.getAttribute("suffix").getBytes(),Charset.forName("ISO-8859-1"));
				//String suffix = new String(rule.getAttribute("suffix").getBytes(),Charset.forName("ISO-8859-1"));
	
				List<String> exceptions = new ArrayList<String>();
				for(Element exception: XMLUtils.getChilds(rule))
				{
	
					if(!exception.getTagName().equals("exception") || !exception.hasChildNodes())
						throw new PTStemmerException("Problem while parsing Orengo's XML stemming rules file: Invalid exception in step "+stepName+", rule "+suffix+".");
					String exc = new String(exception.getChildNodes().item(0).getNodeValue().getBytes(),Charset.forName("ISO-8859-1"));
					//String exc = new String(exception.getChildNodes().item(0).getNodeValue().getBytes(),Charset.forName("ISO-8859-1"));
					exceptions.add(exc);
				}
				Rule r;
				try
				{
					String rep = new String(rule.getAttribute("replacement").getBytes(),Charset.forName("ISO-8859-1"));
					//String rep = new String(rule.getAttribute("replacement").getBytes(),Charset.forName("ISO-8859-1"));
					r = new Rule(Integer.parseInt(rule.getAttribute("size")), rep, exceptions.toArray(new String[exceptions.size()]));
				}catch (NumberFormatException e) {
					throw new PTStemmerException("Problem while parsing Orengo's XML stemming rules file: Missing or invalid rules properties on step "+stepName+".", e);
				}
				suffixes.addSuffix(suffix, r);
			}
	
			if(stepName.equals("pluralreduction"))
				pluralreductionrules = suffixes;
			else if(stepName.equals("femininereduction"))
				femininereductionrules = suffixes;
			else if(stepName.equals("adverbreduction"))
				adverbreductionrules = suffixes;
			else if(stepName.equals("augmentativediminutivereduction"))
				augmentativediminutivereductionrules = suffixes;
			else if(stepName.equals("nounreduction"))
				nounreductionrules = suffixes;
			else if(stepName.equals("verbreduction"))
				verbreductionrules = suffixes;
			else if(stepName.equals("vowelremoval"))
				vowelremovalrules = suffixes;
		}
	
		if(pluralreductionrules == null)
			throw new PTStemmerException("Problem while parsing RSLP-S's XML stemming rules file.");
		//|| !(femininereductionrules == null || adverbreductionrules == null ||
				//augmentativediminutivereductionrules == null || nounreductionrules == null || verbreductionrules == null)
	}



	protected SuffixTree<Rule> pluralreductionrules;
	protected SuffixTree<Rule> femininereductionrules;
	protected SuffixTree<Rule> adverbreductionrules;
	protected SuffixTree<Rule> augmentativediminutivereductionrules;
	protected SuffixTree<Rule> nounreductionrules;
	protected SuffixTree<Rule> verbreductionrules;
	protected SuffixTree<Rule> vowelremovalrules;

}
