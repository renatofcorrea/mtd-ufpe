package br.ufpe.mtd.negocio.entidade;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 *
 * @author Djalma
 */
public class Mapa implements Serializable{

	private static final long serialVersionUID = 1L;
	private List<Nodo> nodos;
	private List<MTDDocument> documentos;
	private TreeMap<Integer, Palavra> palavras;

	public Mapa() {
		nodos = new ArrayList<>();
		documentos = new ArrayList<>();
		palavras = new TreeMap<>();
	}
	
	public List<Nodo> getNodos() {
		return nodos;
	}

	public void setNodos(List<Nodo> nodos) {
		this.nodos = nodos;
	}
	
	public List<MTDDocument> getDocumentos() {
		return documentos;
	}
	
	public void addDocumentos(List<MTDDocument> docs){
		for (MTDDocument mtdDocument : docs) {
			if(!documentos.contains(mtdDocument)){
				documentos.add(mtdDocument);
				if(mtdDocument.getAreaCNPQ() != null){
					System.out.println("Doc "+mtdDocument.getDocId()+" Area "+mtdDocument.getAreaCNPQ());
				}
			}
		}
	}
	
	public void carregarDocumentos(){
		for(Nodo nodo : nodos){
			addDocumentos(nodo.getDocumentos());
		}
		
		System.out.println("Carregados "+ documentos.size()+" Documentos");
	}
	
	public Nodo getNodo(int nodoId){
		Nodo retorno = null;
		for(Nodo nodo : nodos){
			if(nodo.getId().intValue() == nodoId){
				retorno = nodo;
				break;
			}
		}
		
		return retorno;
	}
	
	public MTDDocument getDocumento(int docId){
		MTDDocument retorno = null;
		
		for(MTDDocument aux : documentos){
			if(aux.getDocId() == docId){
				retorno = aux;
				break;
			}
		}
		
		return retorno;
	}
	
	public TreeMap<Integer, Palavra> getPalavras() {
		return palavras;
	}
	
	public void addPalavra(Palavra palavra){
		palavras.put(palavra.getPalavraId(), palavra);
	}
	
	public Palavra getPalavra(int palavraId){
		return palavras.get(palavraId);
	}
}