package br.ufpe.mtd.teste;

public class TesteConceito {

	
	public static void main(String[] args) {
		System.out.println(Math.ceil(10.5));
		System.out.println(Math.floor(10.5));
		
		
		String txt = "";
		
		txt += "asdadasd,";
		
		txt = txt.isEmpty() ? "" : txt.substring(0, txt.lastIndexOf(","));
		
		System.out.println(txt);
	}
}
