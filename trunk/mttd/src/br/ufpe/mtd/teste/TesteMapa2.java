package br.ufpe.mtd.teste;

import java.io.IOException;
import java.util.List;

import br.ufpe.mtd.negocio.entidade.Mapa;
import br.ufpe.mtd.negocio.entidade.Nodo;
import br.ufpe.mtd.util.MTDFactory;

public class TesteMapa2 {

	
	public static void main(String[] args) {
		
		try {
			Mapa mapa = MTDFactory.getInstancia().getSingleRepositorioMapa().getMapa();
			
			
			List<Nodo> nodos = mapa.getNodos();
			
			for (Nodo nodo : nodos) {
				System.out.print("Nodo "+nodo.getId());
				nodo.calcularAreaPredominante();
				
				System.out.println("Area"+nodo.getAreaPredominante());
				
			}
			
			
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}
