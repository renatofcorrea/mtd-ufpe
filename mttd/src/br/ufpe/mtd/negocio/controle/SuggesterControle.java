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
import br.ufpe.mtd.util.StringConverter;

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
				qtdSugestoes = 15;
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
	 * Filtrar dentre as sugestoes retornadas aquelas que possuem todas as palavras digitadas pelo usuario na pesquisa (Filtrar por termo Digitados)
	 * Ajustar dados para serem devolvidos a classe chamadora (Formatar resposta)
	 * 
	 * @param texto
	 * @return
	 * @throws Exception
	 */
	public Collection<String> lookup(String texto) throws Exception{
		if(dicionario.isEmpty()){
			MTDException e = new MTDException(false,"Desculpe-nos. Esse tipo de sugestões ainda não está disponível. Tente novamente mais tarde.");
			throw e;
		}
		
		if(texto.trim().length() < 3){
			return new ArrayList<String>();
		}
		
		String[] tokkens = tokkenize(texto);
		
		Collection<String> palavrasFiltrado = filtrarStopwords(tokkens);
		Collection<String> sugestoes = null;
		if(false){
		//infere palavras para os termos digitados  afim de realizar busca
		Collection<String> palavras = inferirPalavrasDicionario(palavrasFiltrado);
			
		//busca documentos e gera sugestoes que contem algum dos termos digitados
		sugestoes = gerarSugestao(palavras,palavrasFiltrado);
		
		//filtra sugestoes que contem todos os termos digitados
		if(palavrasFiltrado.size()>1)
		sugestoes = filtrarPorTermosDigitados(palavrasFiltrado, sugestoes);
		}else
			sugestoes = gerarSugestao(palavrasFiltrado);
		
		return formatarResposta(new ArrayList<String>(sugestoes));
	}
	
	public Collection<String> filtrarPorTermosDigitados(Collection<String> palavrasDigitadas, Collection<String> sugestoes){
		Collection<String> lista = new ArrayList<String>();
		for(String sugestao: sugestoes){
			if(incluirTodosTermos){
				boolean incluir = true;
				for(String termo: palavrasDigitadas){
					if(!sugestao.toLowerCase().contains(termo)){//.trim().toLowerCase()
						incluir = false;
						break;
					}
				}
				if(incluir){
					lista.add(sugestao.toLowerCase().trim());
				}
			}else{
				for(String termo: palavrasDigitadas){
					if(sugestao.toLowerCase().contains(termo)){//.trim().toLowerCase()
						lista.add(sugestao.toLowerCase().trim());
						break;
					}
				}
			}
		}
		//System.out.println(lista.toString());
		return lista;
	}
	
public Collection<String> gerarSugestao(Collection<String> palavras) throws Exception {
		
	StringBuffer sbu = new StringBuffer();
	ArrayList<String> palavras1 = new ArrayList<String>(palavras);
	String ob = null;
	String bo = null;
	String auxn = null;
	TreeSet<String> pal = new TreeSet<String>();//filtra as repetições
	if (!palavras1.isEmpty()) {
		for (int i=0; i < palavras1.size();i++) {
			auxn = StringConverter.deleteAcentos(palavras1.get(i));//termos sem acento e ç no índice
			ob = (i<palavras1.size()-1)?"+":"";
			bo = (i<palavras1.size()-1)?"^2 ":"* "; //^2 pode tornar sns não sugestíveis, mas sem ele a consulta perde contexto
			sbu.append(ob+auxn+bo);
			
		}
		System.out.println(sbu);
		List<MTDDocument> docs = rep.consultar(sbu.toString(), tipo.getCampos(), tipo.getQtdDocsPesquisados());
		
		for (MTDDocument doc : docs) {
			Collection<String> results = tipo.equals(SuggesterType.SINTAGMA_SUGGESTER) ? doc.getSintagmas() : doc.getKeywords();
			for(String sugestao: results){
				if(!sugestao.trim().isEmpty()){
					if(incluirTodosTermos){
						boolean incluir = true;
						for(String termo: palavras){
							if(!sugestao.toLowerCase().contains(termo)){//.trim().toLowerCase()
								incluir = false;
								break;
							}
						}
						if(incluir){
							pal.add(sugestao.toLowerCase().trim());
						}
					}else
					for(String termo: palavras){
						if(sugestao.toLowerCase().contains(termo)){//.trim().toLowerCase()
							pal.add(sugestao.toLowerCase().trim());
							break;
						}
					}
				}
			}
		}
	}
	
		return pal;
	}
	
	public Collection<String> gerarSugestao(Collection<String> palavras,Collection<String> palavrasfiltrado) throws Exception{
		StringBuffer sbu = new StringBuffer();
		int ib = 0;
		String ob = null;
		Collection<String> sugestoes = new ArrayList<String>();
		if (!palavras.isEmpty()) {
			if(palavrasfiltrado.size() > 1)
				ib = 2 * Math.min(2,palavrasfiltrado.size()-1);//2 //se 2, duas primeiras palavras recebem bost
			for (String aux : palavras) {
				if(ib > 0){
					ob = (ib%2==0)?"+":"";
					sbu.append(ob+aux+"^2 ");//pode tornar sns não sugestíveis, mas sem ele a consulta perde contexto
					ib--;
				}else
				sbu.append(aux+" ");
			}
			sugestoes = consultar(sbu.toString().toLowerCase(), palavrasfiltrado);
		}
		System.out.println(sbu);
		
		return sugestoes;
	}
	
	//tokeniza e reduz para minúsculas
	public String[] tokkenize(String texto){
		String[] tokkens = texto.trim().split(" ");
		for (int i = 0; i < tokkens.length; i++) {
			tokkens[i] = tokkens[i].trim().toLowerCase().replace("\"", "");
		}
		return tokkens;
	}
	
	//Filtra stopwords e tokkens com menos de 3 caracteres excetuando a última tokken
	public ArrayList<String> filtrarStopwords(String[] tokkens){
		ArrayList<String> tokkensFiltrado = new ArrayList<String>();
		String tokken="";
		for (int i=0; i < tokkens.length;i++){
			tokken = tokkens[i];
			if(tokken.length() < 3 || ((i != (tokkens.length-1)) && stopwords.contains(tokken))){
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
		boolean addoutros = false;
		for (String termo : tokkens) {
			termo = StringConverter.deleteAcentos(termo);//termos sem acento e ç no índice
			if (!termo.isEmpty() && termo.length() > 2) {
				try {
					String termoIgual = null;
					List<String> iniciais = new ArrayList<String>();
					List<String> outros = new ArrayList<String>();
					
					for (String string : dicionario) {
						
						if(termoIgual == null && string.equals(termo)){
							termoIgual = termo;
						}
						
						else if(string.startsWith(termo) && !iniciais.contains(string))
							iniciais.add(string);
						else if (addoutros && string.contains(termo) && !iniciais.contains(string) && !outros.contains(string)) 
							outros.add(string);
						
					}
					if(termoIgual != null){
						palavras.add(termoIgual);
					}

					Collections.sort(iniciais, new Comparator<String>() {
						@Override
						public int compare(String arg0, String arg1) {
							return arg0.length() - arg1.length();
						}
					});
					if(iniciais.size() > limitePalavras){
						int numniveis = 2;//2, permite sugestões com dois tamanhos
						int numcharnivel = iniciais.get(0).length();
					for(int i = 0; i < limitePalavras; i++){
						if(iniciais.get(i).length()==numcharnivel){
								palavras.add(iniciais.get(i));
						}else if(numniveis > 1){
							numniveis--;
							palavras.add(iniciais.get(i));
							numcharnivel = iniciais.get(i).length();
						}else
							break;
					}
					}else
					palavras.addAll(iniciais);
					
					
					if(addoutros && (palavras.size() < limitePalavras)){
						//incrementar ate chegar ao limite de palavras ou fim de outros
						int limiteoutros = Math.min(limitePalavras - palavras.size(), palavras.size());
						for(int i = 0; i < outros.size(); i++){
							for(int j = 0; limiteoutros > 0 && j < palavras.size(); j++)
								if(outros.get(i).contains(palavras.get(j))){
										palavras.add(outros.get(i));
										limiteoutros--;
										break;
								}
						}
					}
					
				} catch (Exception e1) {
					f.getLog().salvarDadosLog(e1);
				}
			}
		}
		
		
		if(palavras.size() > limitePalavras)
			return palavras.subList(0, limitePalavras-1);
		else
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
						if(sugestao.toLowerCase().contains(termo)){//termo já em minusculas e sem espaços
							palavras.add(sugestao.toLowerCase().trim());
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
			lista = lista.subList(0, qtdSugestoes);
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
		
		SINTAGMA_SUGGESTER(new String[]{MTDDocument.SINTAGMA_NOMINAL}, 10),
		KEY_WORD_SUGGESTER(new String[]{MTDDocument.KEY_WORD}, 25);
		
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