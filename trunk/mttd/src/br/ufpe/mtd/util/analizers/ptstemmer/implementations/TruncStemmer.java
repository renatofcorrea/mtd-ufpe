package br.ufpe.mtd.util.analizers.ptstemmer.implementations;

import br.ufpe.mtd.util.analizers.ptstemmer.Stemmer;

public class TruncStemmer extends Stemmer{
	private int n = 0;
	
	public TruncStemmer(int n) {
		super();
		if(n>0)
		    this.n = n;
		else
			this.n = 5;//better performance in information retrieval
	}

	public static void main(String[] args) {
		Stemmer st = new TruncStemmer(5);
		System.out.println(st.getWordStem("bacana"));
		System.out.println(st.getWordStem("besta"));

	}

	@Override
	protected String stemming(String word) {
		if(word.length() > n)
			return word.substring(0,n);
		else return word;
	}

}
