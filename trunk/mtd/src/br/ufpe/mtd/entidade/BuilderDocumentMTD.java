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
public class BuilderDocumentMTD {
	
	public DocumentMTD buildDocument() {
		DocumentMTD novoDocumento = new DocumentMTD();
		novoDocumento.setKeywords(new ArrayList<String>());
		return novoDocumento;
	}
	
	public DocumentMTD buildDocument(Document document) {
		ArrayList<String> keyWords = new ArrayList<String>();
		String[] arrayKeyWords = document.getValues(DocumentMTD.KEY_WORD);
		
		for (int i = 0; i < arrayKeyWords.length; i++) {
			keyWords.add(arrayKeyWords[i]);
		}
		
		DocumentMTD novoDocumento = buildDocument(document.get(DocumentMTD.TITULO), 
				document.get(DocumentMTD.RESUMO), 
				keyWords,
				MTDUtil.recuperarDataFormatosSuportados(document.get(DocumentMTD.DATA_DEFESA).trim()), 
				document.get(DocumentMTD.AUTOR), 
				document.get(DocumentMTD.PROGRAMA), 
				document.get(DocumentMTD.ORIENTADOR), 
				document.get(DocumentMTD.AREA_CNPQ), 
				document.get(DocumentMTD.ID), 
				document.get(DocumentMTD.AREA_PROGRAMA));
		
		
		
		novoDocumento.setRepositorio(document.get(DocumentMTD.REPOSITORIO));
		novoDocumento.setUrl(document.get(DocumentMTD.URL));
		novoDocumento.setGrau(document.get(DocumentMTD.GRAU));
		
		return novoDocumento;
				
	}



	public DocumentMTD buildDocument(String titulo, String resumo,List<String> keywords, Date dataDefesa, String autor,
			String programa, String orientador, String areaCNPQ, String id,String areaPrograma) {
		DocumentMTD novoDocumento = buildDocument();
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
	
	public DocumentMTD buildDocument(SolrDocument document){
		
		ArrayList<String> keyWords = new ArrayList<String>();
		Collection<Object> arrayKeyWords = document.getFieldValues(DocumentMTD.KEY_WORD);
		
		for (Object item : arrayKeyWords) {
			keyWords.add(item.toString());
		}

		
		DocumentMTD novoDocumento = buildDocument(document.get(DocumentMTD.TITULO).toString(), 
				document.get(DocumentMTD.RESUMO).toString(), 
				keyWords,
				MTDUtil.recuperarDataFormatosSuportados(document.get(DocumentMTD.DATA_DEFESA).toString().trim()), 
				document.get(DocumentMTD.AUTOR).toString(), 
				document.get(DocumentMTD.PROGRAMA).toString(), 
				document.get(DocumentMTD.ORIENTADOR).toString(), 
				document.get(DocumentMTD.AREA_CNPQ).toString(), 
				document.get(DocumentMTD.ID).toString(), 
				document.get(DocumentMTD.AREA_PROGRAMA).toString());
		
		novoDocumento.setRepositorio(document.get(DocumentMTD.REPOSITORIO).toString());
		novoDocumento.setUrl(document.get(DocumentMTD.URL).toString());
		novoDocumento.setGrau(document.get(DocumentMTD.GRAU).toString());
		
		return novoDocumento;
	}
}