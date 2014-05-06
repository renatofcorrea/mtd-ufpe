package br.ufpe.mtd.entidade;

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
	
	public MTDDocument buildDocument() {
		MTDDocument novoDocumento = new MTDDocument();
		novoDocumento.setKeywords(new ArrayList<String>());
		return novoDocumento;
	}
	
	public MTDDocument buildDocument(Document document) {
		ArrayList<String> keyWords = new ArrayList<String>();
		String[] arrayKeyWords = document.getValues(MTDDocument.KEY_WORD);
		if(arrayKeyWords!= null){
			for (int i = 0; i < arrayKeyWords.length; i++) {
				keyWords.add(arrayKeyWords[i]);
			}
		}
		
		MTDDocument novoDocumento = buildDocument(document.get(MTDDocument.TITULO), 
				document.get(MTDDocument.RESUMO), 
				keyWords,
				MTDUtil.recuperarDataFormatosSuportados(document.get(MTDDocument.DATA_DEFESA).trim()), 
				document.get(MTDDocument.AUTOR), 
				document.get(MTDDocument.PROGRAMA), 
				document.get(MTDDocument.ORIENTADOR), 
				document.get(MTDDocument.AREA_CNPQ), 
				document.get(MTDDocument.ID), 
				document.get(MTDDocument.AREA_PROGRAMA));
		
		
		
		novoDocumento.setRepositorio(document.get(MTDDocument.REPOSITORIO));
		novoDocumento.setUrl(document.get(MTDDocument.URL));
		novoDocumento.setGrau(document.get(MTDDocument.GRAU));
		
		return novoDocumento;
				
	}



	public MTDDocument buildDocument(String titulo, String resumo,List<String> keywords, Date dataDefesa, String autor,
			String programa, String orientador, String areaCNPQ, String id,String areaPrograma) {
		MTDDocument novoDocumento = buildDocument();
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
	
	public MTDDocument buildDocument(SolrDocument document){
		
		ArrayList<String> keyWords = new ArrayList<String>();
		Collection<Object> arrayKeyWords = document.getFieldValues(MTDDocument.KEY_WORD);
		
		if(arrayKeyWords!= null){
			for (Object item : arrayKeyWords) {
				keyWords.add(item.toString());
			}
		}
		
		MTDDocument novoDocumento = buildDocument(document.get(MTDDocument.TITULO).toString(), 
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
		
		return novoDocumento;
	}
}