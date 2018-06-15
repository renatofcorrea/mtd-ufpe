package br.ufpe.mtd.negocio.entidade;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import br.ufpe.mtd.util.enumerado.AreaCNPQEnum;

public class Nodo implements Serializable, Comparable<Nodo>{
	
	private static final long serialVersionUID = 1L;
	
	private Integer id;
	private List<MTDDocument> documentos;
	private AreaQtd areaQtd;
	private ArrayList<PesoPalavra> maioresPesos;
	
	
	
	public Nodo() {
		documentos = new ArrayList<>();
		maioresPesos = new ArrayList<>();
	}
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public List<MTDDocument> getDocumentos() {
		return documentos;
	}

	public void addDocumento(MTDDocument doc){		
		documentos.add(doc);
	}
	
	public void setDocumentos(List<MTDDocument> documentos) {
		this.documentos = documentos;
	}
	
	public void calcularAreaPredominante(){
		
		AreaQtd[] areas = new AreaQtd[AreaCNPQEnum.class.getEnumConstants().length];
		int i = 0;
		for(AreaCNPQEnum aux : AreaCNPQEnum.class.getEnumConstants()){
			AreaQtd areaQtd = new AreaQtd();
			areaQtd.area = aux; 
			areaQtd.qtd = 0; 
			areas[i++] = areaQtd;
		}
		
		if(documentos == null || documentos.size() == 0){
			this.areaQtd = new AreaQtd();
			this.areaQtd.area = AreaCNPQEnum.NAO_ENCONTRADO;
			this.areaQtd.qtd = 0;
			return;
		}
		
		for(MTDDocument doc: documentos){
			for(AreaQtd areaQtd : areas){
				if(doc.getAreaCNPQ()== null || areaQtd.area == null){
					continue;
				}
				if(doc.getAreaCNPQ().toUpperCase().equals(areaQtd.area.name().toUpperCase())){
					areaQtd.qtd++;
					break;
				}
			}
		}
		
		AreaQtd areaQtdMaior = null;
		for(AreaQtd areaQtd : areas){
			if(areaQtdMaior == null){
				areaQtdMaior = areaQtd;
			}
			
			if(areaQtdMaior.qtd < areaQtd.qtd){
				areaQtdMaior = areaQtd;
			}			
		}
		
		this.areaQtd = areaQtdMaior;
	}
	
	
	public int getQtdAreaPredominante(){
		if(areaQtd == null){
			calcularAreaPredominante();
		}
		return this.areaQtd.qtd;
	}
	
	public AreaCNPQEnum getAreaPredominante(){
		if(areaQtd == null){
			calcularAreaPredominante();
		}
		return this.areaQtd.area;
	}
	
	public void setMaioresPesos(ArrayList<PesoPalavra> maioresPesos) {
		this.maioresPesos = maioresPesos;
	}
	
	public ArrayList<PesoPalavra> getMaioresPesos() {
		return maioresPesos;
	}
	
	private class AreaQtd implements Serializable{
		private static final long serialVersionUID = 1L;
		AreaCNPQEnum area;
		int qtd=0;
	}

	@Override
	public int compareTo(Nodo o) {
		return id.compareTo(o.id);
	}
}
