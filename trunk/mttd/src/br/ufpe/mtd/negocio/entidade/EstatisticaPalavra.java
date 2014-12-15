package br.ufpe.mtd.negocio.entidade;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.TreeMap;

/**
 * Classe que representa uma abstra��o da palavra 
 * que esta contida no indice e informa��es associadas a ela como
 * 
 * @author djalma
 *
 */
public class EstatisticaPalavra implements Comparable<EstatisticaPalavra>{

	private static int tamCorpus;
	private TreeMap<Integer, Integer> mapaDocFreq;
	private TreeMap<Integer, Double> mapaDocFreqNorm;
	private Integer docFreq;
	private Integer freqMax;
	private Integer freqMin;
	private Double freqMaxNorm;
	private Double freqMinNorm;
	private Long totalDocFreq;
	private Double freqMediaGlobal, freqMediaLocal;
	private Double freqMediaGlobalNorm, freqMediaLocalNorm;
	private String palavra;

	public EstatisticaPalavra(TreeMap<Integer, Integer> mapaDocFreq, String palavra){
		this.palavra = palavra;
		this.mapaDocFreq = mapaDocFreq;
		gerarEstatistica();
	}
	
	public EstatisticaPalavra(String palavra) {
		this.palavra = palavra;
		this.mapaDocFreq = new TreeMap<Integer, Integer>();
		this.mapaDocFreqNorm = new TreeMap<Integer, Double>();
	}
	
	/**
	 * Atualiza os valores de 
	 * freqMax, freqMin e freqMedia.
	 * 
	 * 
	 * 
	 */
	public void gerarEstatistica(){
		
		Integer freqMin = null;
		Integer freqMax = null;
		Integer freqAcumulada = 0;
		
		for(Integer docId : mapaDocFreq.keySet()){
			
			Integer frequencia = mapaDocFreq.get(docId);
			
			if(freqMin == null || freqMin.intValue() > frequencia.intValue()){
				freqMin = frequencia ;
			}
			
			if(freqMax == null || freqMax.intValue() < frequencia.intValue()){
				freqMax = frequencia ;
			}
			
			freqAcumulada += frequencia;
		}
		
		//se existem documentos onde a palavra nao apareceu a freq min sera zero. Do contrario mantem o que foi calculado.
		if(tamCorpus > mapaDocFreq.size()){
			freqMin = 0;
		}
		
		this.freqMax = freqMax;
		this.freqMin = freqMin;
		
		Double d = (mapaDocFreq.size() == 0 ? 0: new Double(freqAcumulada) / mapaDocFreq.size());
		BigDecimal bd = new BigDecimal(d);
		this.freqMediaLocal = bd.setScale(16, BigDecimal.ROUND_HALF_DOWN).doubleValue();
		
		d = (tamCorpus == 0 ? 0 : new Double(freqAcumulada) / tamCorpus);
		bd = new BigDecimal(d);
		this.freqMediaGlobal = bd.setScale(16, BigDecimal.ROUND_HALF_DOWN).doubleValue();
	}
	/**
	 * Atualiza os valores de 
	 * freqMax, freqMin e freqMedia.
	 * 
	 * 
	 * 
	 */
	public void gerarEstatisticaNorm(){
		
		Double freqMin = null;
		Double freqMax = null;
		Double freqAcumulada = 0.0;
		
		for(Integer docId : mapaDocFreqNorm.keySet()){
			
			Double frequencia = mapaDocFreqNorm.get(docId);
			
			if(freqMin == null || freqMin.intValue() > frequencia.intValue()){
				freqMin = frequencia ;
			}
			
			if(freqMax == null || freqMax.intValue() < frequencia.intValue()){
				freqMax = frequencia ;
			}
			
			freqAcumulada += frequencia;
		}
		
		//se existem documentos onde a palavra nao apareceu a freq min sera zero. Do contrario mantem o que foi calculado.
		if(tamCorpus > mapaDocFreq.size()){
			freqMin = 0.0;
		}
		
		this.freqMaxNorm = freqMax;
		this.freqMinNorm = freqMin;
		
		Double d = (mapaDocFreqNorm.size() == 0 ? 0: new Double(freqAcumulada) / mapaDocFreqNorm.size());
		BigDecimal bd = new BigDecimal(d);
		this.freqMediaLocalNorm = bd.setScale(16, BigDecimal.ROUND_HALF_DOWN).doubleValue();
		
		d = (tamCorpus == 0 ? 0 : new Double(freqAcumulada) / tamCorpus);
		bd = new BigDecimal(d);
		this.freqMediaGlobalNorm = bd.setScale(16, BigDecimal.ROUND_HALF_DOWN).doubleValue();
	}
	
	public void atualizarMapa(ArrayList<int[]> lista){
		for (int[] arrayDocFreq : lista) {
			int docId = arrayDocFreq[0];
			if(mapaDocFreq.containsKey(docId)){
				int freq = mapaDocFreq.get(docId) + arrayDocFreq[1];
				mapaDocFreq.put(docId, freq);
			}else{
				mapaDocFreq.put(docId, arrayDocFreq[1]);
			}
		}
		
	}
	
	public void normalizarMapa(TreeMap<Integer, Double> mapaNormas){
		for(Integer docId :mapaDocFreq.keySet()){
			Integer freq = mapaDocFreq.get(docId);
			Double norma = mapaNormas.get(docId);
			mapaDocFreqNorm.put(docId, (freq / norma));
		}
	}
	
	/**
	 * Maior frequencia de ocorrencia da palavra em um mesmo documento
	 * @return
	 */
	public Integer getFreqMax() {
		return freqMax;
	}

	/**
	 * Menor ocorrencia da palavra em um mesmo documento
	 * 
	 * @return
	 */
	public Integer getFreqMin() {
		return freqMin;
	}

	/**
	 * Somatorio de vezes que a palavra apareceu dentro dos documentos.
	 * 
	 * Frequencia acumulada.
	 * 
	 * @return
	 */
	public Long getTotalDocFreq() {
		return totalDocFreq;
	}

	/**
	 * Total de documentos onde a palavra apareceu
	 * 
	 * @return
	 */
	public Integer getDocFreq() {
		return docFreq;
	}
	
	/**
	 * Divisao entre a quantidade de vezes que a palavra apareceu dividido
	 * pelo universo de documentos (corpus)
	 * @return
	 */
	public Double getFreqMediaGlobal() {
		return freqMediaGlobal;
	}
	
	/**
	 * Frequencia media de ocorrencia da palavra nos documentos
	 * onde a palavra realmente aparece.
	 * 
	 * @return
	 */
	public Double getFreqMediaLocal() {
		return freqMediaLocal;
	}

	public TreeMap<Integer, Integer> getMapaDocFreq() {
		return mapaDocFreq;
	}
	
	public String getPalavra() {
		return palavra;
	}

	public void setDocFreq(Integer docOcurrence) {
		this.docFreq = docOcurrence;
	}

	public void setTotalDocFreq(Long qtdAcumulada) {
		this.totalDocFreq = qtdAcumulada;
	}
	
	public static void setTamCorpus(int tamCorpus) {
		EstatisticaPalavra.tamCorpus = tamCorpus;
	}
	
	public static int getTamCorpus() {
		return tamCorpus;
	}
	
	public TreeMap<Integer, Double> getMapaDocFreqNorm() {
		return mapaDocFreqNorm;
	}
	
	public Double getFreqMaxNorm() {
		return freqMaxNorm;
	}

	public Double getFreqMinNorm() {
		return freqMinNorm;
	}

	public Double getFreqMediaGlobalNorm() {
		return freqMediaGlobalNorm;
	}

	public Double getFreqMediaLocalNorm() {
		return freqMediaLocalNorm;
	}

	/**
	 * Compara o objeto corrente com outros objetos do tipo estatistica palavra
	 * e verifica se representam a mesma coisa. O criterio de igualdade � a palavra.
	 * 
	 * Caso seja passado apenas a palavra como parametro tambem sera considerado igual se 
	 * as palavras passada e a palavra contida no objeto corrente forem iguais. 
	 */
	@Override
	public boolean equals(Object obj) {
		return this.palavra.equals(((EstatisticaPalavra)obj).getPalavra());
	}

	@Override
	public int compareTo(EstatisticaPalavra o) {
		return palavra.compareTo(o.getPalavra());
	}

	@Override
	public String toString() {
		return "EstatisticaPalavra [mapaDocFreq=" + mapaDocFreq + ", mapaDocFreqNorm=" + mapaDocFreqNorm + ", docFreq=" + docFreq + ", freqMax=" + freqMax + ", freqMin=" + freqMin + ", freqMaxNorm=" + freqMaxNorm + ", freqMinNorm=" + freqMinNorm + ", totalDocFreq=" + totalDocFreq + ", freqMediaGlobal=" + freqMediaGlobal + ", freqMediaLocal=" + freqMediaLocal + ", freqMediaGlobalNorm="
				+ freqMediaGlobalNorm + ", freqMediaLocalNorm=" + freqMediaLocalNorm + ", palavra=" + palavra + "]";
	}
}