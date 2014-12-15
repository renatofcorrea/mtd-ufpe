package br.ufpe.mtd.negocio.entidade;

import java.io.Serializable;

/**
 * Representa os possiveis estados pelo qual o sistema 
 * pode passar. Sera usada para ajudar a monitorar 
 * e gerenciar estes estados.
 *  
 * @author djalma
 *
 */
public class StatusSistema implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String status;

	public StatusSistema() {
		status = Status.VAZIO.name();
	}
	
	public Status getStatus() {
		return Status.valueOf(status);
	}

	/**
	 * Faz o status andar em um nivel para frente
	 */
	public void updateStatus() {
		boolean mudar = false;
		for(Status s :Status.values()){
			if(s.name().equals(status)){
				mudar = true;
			}else if(mudar ){
				status = s.name();
				break;
			}
		}
	}
	
	/**
	 * Procura voltar op status anterior
	 * se o status passado por parametro
	 * for realmente um status anterior.
	 * @param status
	 */
	public void voltarStatus(Status status){
		for(Status s :Status.values()){
			if(s.equals(status)){
				this.status = status.name();
				break;
			}else if(s.name().equals(this.status)){
				break;
			}
		}
	}
	
	
	private static enum Status{
		VAZIO, INDEXANDO, TREINANDO, GERANDO_SINTAGMAS, COMPLETO;
	}
	
	public static void main(String[] args) {
		StatusSistema s = new StatusSistema();
		System.out.println(s.getStatus());
		s.updateStatus();
		System.out.println(s.getStatus());
		s.updateStatus();
		System.out.println(s.getStatus());
		s.updateStatus();
		System.out.println(s.getStatus());
		s.updateStatus();
		System.out.println(s.getStatus());
		s.updateStatus();
		System.out.println(s.getStatus());
		s.updateStatus();
		
		System.out.println("Voltando status ");
		
		s.voltarStatus(Status.INDEXANDO);
		System.out.println(s.getStatus());
		s.updateStatus();
		System.out.println(s.getStatus());
		s.updateStatus();
		System.out.println(s.getStatus());
		s.updateStatus();
		System.out.println(s.getStatus());
		s.updateStatus();
		System.out.println(s.getStatus());
		s.updateStatus();
		System.out.println(s.getStatus());
		s.updateStatus();
	}
}
