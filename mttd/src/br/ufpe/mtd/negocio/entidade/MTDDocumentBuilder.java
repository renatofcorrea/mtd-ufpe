package br.ufpe.mtd.negocio.entidade;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.solr.common.SolrDocument;

import br.ufpe.mtd.util.MTDUtil;

/**
 * Classe resposnsavel de contruir adequadamente Objetos do tipo
 * DocumentoMTD. Opacao adotada para dimunuir a complexidade da classe documento MTD.
 * Retirar da classe documento MTD a dependencia de bibiotecas externas ou do Solr.
 * baseada no padrao de projeto Builder.
 * 
 * @author djalma
 *
 */
public class MTDDocumentBuilder {
	
	private MTDDocument documento;
	
	public MTDDocument build(){
		return documento;
	}
	
	public MTDDocumentBuilder buildDocument() {
		documento = new MTDDocument();
		documento.setKeywords(new ArrayList<String>());
		documento.setSintagmas(new ArrayList<String>());
		return this;
	}
	
	public MTDDocumentBuilder buildDocument(Document document) {
		ArrayList<String> keyWords = new ArrayList<String>();
		String[] arrayKeyWords = document.getValues(MTDDocument.KEY_WORD);
		if(arrayKeyWords!= null){
			for (int i = 0; i < arrayKeyWords.length; i++) {
				keyWords.add(arrayKeyWords[i]);
			}
		}
		
		ArrayList<String> sintagmas = new ArrayList<String>();
		String[] arraySintagmas = document.getValues(MTDDocument.SINTAGMA_NOMINAL);
		if(arraySintagmas!= null){
			for (int i = 0; i < arraySintagmas.length; i++) {
				sintagmas.add(arraySintagmas[i]);
			}
		}
		String datas = document.get(MTDDocument.DATA_DEFESA);
		Date data = null;
		if(datas != null){
		data = MTDUtil.recuperarDataFormatosSuportados(datas.trim());
		}else{
			System.out.println("Erro: "+document.get(MTDDocument.ID)+" sem data.");
			data = MTDUtil.recuperarDataFormatosSuportados("2015-01-01");
		}
		documento = initDocument(document.get(MTDDocument.TITULO), 
				document.get(MTDDocument.RESUMO), 
				keyWords,
				data, 
				document.get(MTDDocument.AUTOR), 
				document.get(MTDDocument.PROGRAMA), 
				document.get(MTDDocument.ORIENTADOR), 
				document.get(MTDDocument.AREA_CNPQ), 
				document.get(MTDDocument.ID), 
				document.get(MTDDocument.AREA_PROGRAMA));
		
		
		
		documento.setRepositorio(document.get(MTDDocument.REPOSITORIO));
		documento.setUrl(document.get(MTDDocument.URL));
		documento.setGrau(document.get(MTDDocument.GRAU));
		documento.setNomeInstituicao(document.get(MTDDocument.INSTITUICAO));
		documento.setSintagmas(sintagmas);
		return this;
				
	}



	private MTDDocument initDocument(String titulo, String resumo,List<String> keywords, Date dataDefesa, String autor,
			String programa, String orientador, String areaCNPQ, String id,String areaPrograma) {
		MTDDocument novoDocumento = buildDocument().build();
		novoDocumento.setTitulo(titulo);
		novoDocumento.setResumo(resumo);
		novoDocumento.setKeywords(keywords);
		novoDocumento.setDataDeDefesa(dataDefesa);
		novoDocumento.setAutor(autor);
		novoDocumento.setPrograma(programa);
		novoDocumento.setOrientador(orientador);
		novoDocumento.setAreaCNPQ(areaCNPQ);
		novoDocumento.setId(id);
		novoDocumento.setAreaPrograma(areaPrograma);
		
		return novoDocumento;
	}
	
	public MTDDocumentBuilder buildDocument(SolrDocument document){
		
		ArrayList<String> keyWords = new ArrayList<String>();
		Collection<Object> arrayKeyWords = document.getFieldValues(MTDDocument.KEY_WORD);
		
		if(arrayKeyWords!= null){
			for (Object item : arrayKeyWords) {
				keyWords.add(item.toString());
			}
		}
		
		ArrayList<String> sintagmas = new ArrayList<String>();
		Collection<Object> arraySintagmas = document.getFieldValues(MTDDocument.SINTAGMA_NOMINAL);
		if(arraySintagmas!= null){
			for (Object item : arraySintagmas) {
				sintagmas.add(item.toString());
			}
		}
		
		MTDDocument novoDocumento = initDocument(document.get(MTDDocument.TITULO).toString(), 
				document.get(MTDDocument.RESUMO).toString(), 
				keyWords,
				MTDUtil.recuperarDataFormatosSuportados(document.get(MTDDocument.DATA_DEFESA).toString().trim()), 
				document.get(MTDDocument.AUTOR).toString(), 
				document.get(MTDDocument.PROGRAMA).toString(), 
				document.get(MTDDocument.ORIENTADOR).toString(), 
				document.get(MTDDocument.AREA_CNPQ).toString(), 
				document.get(MTDDocument.ID).toString(), 
				document.get(MTDDocument.AREA_PROGRAMA).toString());
		
		novoDocumento.setRepositorio(document.get(MTDDocument.REPOSITORIO).toString());
		novoDocumento.setUrl(document.get(MTDDocument.URL).toString());
		novoDocumento.setGrau(document.get(MTDDocument.GRAU).toString());
		novoDocumento.setNomeInstituicao(document.get(MTDDocument.INSTITUICAO).toString());
		novoDocumento.setSintagmas(sintagmas);
		return this;
	}
}