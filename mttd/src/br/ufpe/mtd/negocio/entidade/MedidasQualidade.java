package br.ufpe.mtd.negocio.entidade;

import java.io.Serializable;

/**
 * 
 * @author Djalma
 */
public class MedidasQualidade implements Serializable {

	private static final long serialVersionUID = 1L;
	private double teMap, te8Map, mqe, mmqe, idMap, tpMap, npMap, distMap, erroQuantizacao;

	public double getTeMap() {
		return teMap;
	}

	public void setTeMap(double teMap) {
		this.teMap = teMap;
	}

	public double getTe8Map() {
		return te8Map;
	}

	public void setTe8Map(double te8Map) {
		this.te8Map = te8Map;
	}

	public double getMqe() {
		return mqe;
	}

	public void setMqe(double mqe) {
		this.mqe = mqe;
	}

	public double getMmqe() {
		return mmqe;
	}

	public void setMmqe(double mmqe) {
		this.mmqe = mmqe;
	}

	public double getIdMap() {
		return idMap;
	}

	public void setIdMap(double idMap) {
		this.idMap = idMap;
	}

	public double getTpMap() {
		return tpMap;
	}

	public void setTpMap(double tpMap) {
		this.tpMap = tpMap;
	}

	public double getNpMap() {
		return npMap;
	}

	public void setNpMap(double npMap) {
		this.npMap = npMap;
	}

	public double getDistMap() {
		return distMap;
	}

	public void setDistMap(double distMap) {
		this.distMap = distMap;
	}

	//QUANTERROR_VEC
	public double getErroQuantizacao() {
		return erroQuantizacao;
	}

	public void setErroQuantizacao(double erroQuantizacao) {
		this.erroQuantizacao = erroQuantizacao;
	}
}