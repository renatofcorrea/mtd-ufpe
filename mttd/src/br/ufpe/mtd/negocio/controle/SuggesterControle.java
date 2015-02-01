package br.ufpe.mtd.negocio.controle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import br.ufpe.mtd.dados.indice.IRepositorioIndice;
import br.ufpe.mtd.dados.indice.RepositorioIndiceLucene;
import br.ufpe.mtd.negocio.entidade.MTDDocument;
import br.ufpe.mtd.util.MTDException;
import br.ufpe.mtd.util.MTDFactory;
import br.ufpe.mtd.util.MTDParametros;

/**
 * Implementa a regra de negocio da sugestao dentro do sistema.
 * Carrega um dicionario de palavras vindo do indice e a partir
 * dele cria as sugestoes.
 * 
 * Suporta sugestao por sintagmas e por palavra chave.
 * A diferenca basica e o indice que guarda os termos.
 * 
 * Existe um para docs e outro de copia que possui tambem os sintagmas.
 * 
 * @author djalma
 *
 */
public class SuggesterControle{
	private MTDFactory f;
	private Collection<String> dicionario;
	private Collection<String> stopwords;
	private IRepositorioIndice rep;
	private SuggesterType tipo;
	private int qtdSugestoes;
	private Boolean incluirTodosTermos; 
	
	public SuggesterControle(String tipoSugestao, MTDFactory fabrica) throws Exception{
		f = fabrica;
		try {
			qtdSugestoes = Integer.parseInt(MTDParametros.getMaxSuggestResults());
		} catch (Exception e) {
			if(qtdSugestoes == 0){
				qtdSugestoes = 20;
			}
		}
		
		try {
			incluirTodosTermos = Boolean.parseBoolean(MTDParametros.incluirTodosTermosBusca());
		} catch (Exception e) {
			if(incluirTodosTermos == null){
				incluirTodosTermos = false;
			}
		}
		try{
			tipo = SuggesterType.valueOf(tipoSugestao);
		}catch(Exception e){
			if(tipo == null){
				tipo = SuggesterType.KEY_WORD_SUGGESTER;
			}
		}
		
		TreeSet<String> palavras;
		RepositorioIndiceLucene rep;
		if(tipo.equals(SuggesterType.SINTAGMA_SUGGESTER)){
			rep = (RepositorioIndiceLucene)f.getSingleRepositorioSintagmas();
			palavras = rep.carregarDicionario(tipo.getCampos());
		}else{
			rep = (RepositorioIndiceLucene)f.getSingleRepositorioIndice();
			palavras = rep.carregarDicionario(tipo.getCampos());
		}
		
		Set<String> stopwords = rep.getStopWords();
		init(palavras, stopwords, rep);
	}
	
	private void init(Collection<String> dicionario, Collection<String> stopwords, IRepositorioIndice rep){
		this.dicionario = dicionario;
		this.stopwords = stopwords;
		this.rep = rep;
	}
	
	/**
	 * Representa o processo de busca de uma sugestao a partir
	 * de um termo informado.
	 * 
	 * O metodo foi dividido em metodos menores que representam o processo em
	 * 7 etapas.
	 * Filtrar termos muito pequenos
	 * Dividir o termo em Palavras (Tokkenizacao)
	 * Filtrar termos irrelevantes para a busca.(StopWords)
	 * Buscar no dicionario palavras que combinam com as palavras resultantes dos passos anteriores (Inferir)
	 * Buscar no indice os documentos que possuam as palavras resultantes da inferencia e extrair suas palavras-chave ou sintagmas (Gerar Sugestao)
	 * Filtrar dentre as sugestoes retornadas aquelas que possuem todas as palvras digitadas pelo usuario na pesquisa (Filtrar por termo Digitados)
	 * Ajustar dados para serem devolvidos a classe chamdora (Formatar resposta)
	 * 
	 * @param texto
	 * @return
	 * @throws Exception
	 */
	public Collection<String> lookup(String texto) throws Exception{
		if(dicionario.isEmpty()){
			MTDException e = new MTDException(false,"Esse grupo de sugestões está sendo preparado.");
			throw e;
		}
		
		if(texto.length() < 3){
			return new ArrayList<String>();
		}
		
		String[] tokkens = tokkenize(texto);
		
		Collection<String> palavrasFiltrado = filtrarStopwords(tokkens);
		
		Collection<String> palavras = inferirPalavrasDicionario(palavrasFiltrado);
		
		Collection<String> sugestoes = gerarSugestao(palavras);
		
		sugestoes = filtrarPorTermosDigitados(palavrasFiltrado, sugestoes);
		
		return formatarResposta(new ArrayList<String>(sugestoes));
	}
	
	public Collection<String> filtrarPorTermosDigitados(Collection<String> palavrasDigitadas, Collection<String> sugestoes){
		Collection<String> lista = new ArrayList<String>();
		for(String sugestao: sugestoes){
			if(incluirTodosTermos){
				boolean incluir = true;
				for(String termo: palavrasDigitadas){
					if(!sugestao.toLowerCase().contains(termo.trim().toLowerCase())){
						incluir = false;
						break;
					}
				}
				if(incluir){
					lista.add(sugestao.toLowerCase());
				}
			}else{
				for(String termo: palavrasDigitadas){
					if(sugestao.toLowerCase().contains(termo.trim().toLowerCase())){
						lista.add(sugestao.toLowerCase());
						break;
					}
				}
			}
		}
		
		return lista;
	}
	
	public Collection<String> gerarSugestao(Collection<String> palavras) throws Exception{
		StringBuffer sbu = new StringBuffer();
		Collection<String> sugestoes = new ArrayList<String>();
		if (!palavras.isEmpty()) {
			for (String aux : palavras) {
				sbu.append(aux+" ");
			}
			sugestoes = consultar(sbu.toString().toLowerCase(), palavras);
		}
		
		return sugestoes;
	}
	
	public String[] tokkenize(String texto){
		String[] tokkens = texto.trim().split(" ");
		for (int i = 0; i < tokkens.length; i++) {
			tokkens[i] = tokkens[i].trim();
			tokkens[i] = tokkens[i].replace("\"", "");
		}
		return tokkens;
	}
	
	public ArrayList<String> filtrarStopwords(String[] tokkens){
		ArrayList<String> tokkensFiltrado = new ArrayList<String>();
		
		for (String tokken : tokkens) {
			if(stopwords.contains(tokken.toLowerCase())){
				continue;
			}else{
				tokkensFiltrado.add(tokken);
			}
		}
		
		return tokkensFiltrado;
	}
	
	public Collection<String> inferirPalavrasDicionario(Collection<String> tokkens){
		ArrayList<String> palavras = new ArrayList<String>();
		int limitePalavras = 20;
		
		for (String termo : tokkens) {
			
			if (!termo.isEmpty() && termo.length() > 2) {
				try {
					String termoIgual = null;
					List<String> iniciais = new ArrayList<String>();
					List<String> outros = new ArrayList<String>();
					
					for (String string : dicionario) {
						
						if(termoIgual == null && string.toUpperCase().equals(termo.toUpperCase())){
							termoIgual = termo;
						}
						
						if(string.toUpperCase().startsWith(termo.toUpperCase()) && !outros.contains(string)){
							iniciais.add(string);
						}
						
						if (iniciais.size() < limitePalavras && string.toUpperCase().contains(termo.toUpperCase()) && !outros.contains(string)) {
							outros.add(string);
						}
					}
					
					if(termoIgual != null){
						palavras.add(termoIgual);
					}else{
						palavras.add(termo);
					}
					
					palavras.addAll(iniciais);
					
					if(iniciais.size() < limitePalavras){
						//incrmentar ate chegar ao limite de palavras ou fim de outros
						for(int i = 0; i < (limitePalavras - iniciais.size()) && i < outros.size(); i++){
							palavras.add(outros.get(i));
						}
					}
					
				} catch (Exception e1) {
					f.getLog().salvarDadosLog(e1);
				}
			}
		}
		
		return palavras;
	}
	
	public Collection<String> consultar(final String busca, Collection<String> termos) throws Exception {
		
		List<MTDDocument> docs = rep.consultar(busca, tipo.getCampos(), tipo.getQtdDocsPesquisados());
		TreeSet<String> palavras = new TreeSet<String>();//filtra as repetições
		for (MTDDocument doc : docs) {
			Collection<String> results = tipo.equals(SuggesterType.SINTAGMA_SUGGESTER) ? doc.getSintagmas() : doc.getKeywords();
			for(String sugestao: results){
				if(!sugestao.trim().isEmpty()){
					for(String termo: termos){
						if(sugestao.toLowerCase().contains(termo.trim().toLowerCase())){
							palavras.add(sugestao.toLowerCase());
							break;
						}
					}
				}
			}
		}
		return palavras;
	}
	
	/*
	 * Da os ajustes finais a resposta.
	 * 
	 * Ordena por tamanho, depois 
	 * Retira registros que estejam a mais.
	 * 
	 * Ordena o que sobrou por ordem alfabetica.
	 * 
	 */
	private Collection<String> formatarResposta(List<String> lista){
		Collections.sort(lista, new Comparator<String>() {
			@Override
			public int compare(String arg0, String arg1) {
				return arg0.length() - arg1.length();
			}
		});
		
		if(lista.size() > qtdSugestoes){
			lista = lista.subList(0, qtdSugestoes-1);
		}
		
		Collections.sort(lista);
		
		return lista;
	}
	
	public static String getSugestaoPadrao(){
		return SuggesterType.KEY_WORD_SUGGESTER.toString();
	}
	
	public static String getSugestaoSintagma(){
		return SuggesterType.SINTAGMA_SUGGESTER.toString();
	}
	
	public static String getTipoSugestao(String aux){
		return SuggesterType.valueOf(aux).toString();
	}
	
	private static enum SuggesterType{
		
		SINTAGMA_SUGGESTER(new String[]{MTDDocument.SINTAGMA_NOMINAL}, 5),
		KEY_WORD_SUGGESTER(new String[]{MTDDocument.KEY_WORD}, 20);
		
		private SuggesterType(String[] campos,int qtdResultadosBusca) {
			this.campos = campos;
			this.qtdDocsPesquisados = qtdResultadosBusca;
		}
		
		private String[] campos;
		private int qtdDocsPesquisados;
		
		public String[] getCampos() {
			return campos;
		}
		
		public int getQtdDocsPesquisados() {
			return qtdDocsPesquisados;
		}
	}
}