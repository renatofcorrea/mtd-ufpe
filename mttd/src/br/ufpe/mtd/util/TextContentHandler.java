package br.ufpe.mtd.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.ContentHandler;
import java.net.URLConnection;

/**
 * Um gerenciador de conteudo para paginas HTML. Segundo o API de java.net o
 * gerenciador de conteudo so tem a responsabilidade de abrir o arquivo e ler
 * dele o conteudo. Convertindo para o tipo de dados especifico: HTML = String,
 * Text = String, Img = (java.awt.Image), etc.. A implementacao por default de
 * Java, pega o conteudo e elimina dele todos os carateres acentuados ou
 * especiais. Esta implementacao pega o conteudo do jeito que esta e retorna ele
 * como um String.
 * 
 * @author cmtr@di.ufpe.br
 * @author aml@di.ufpe.br
 */
public class TextContentHandler extends ContentHandler {
	public Object getContent(URLConnection urlConn) throws IOException {
		String conteudo = ""; // String para o conteudo
		InputStream entrada = urlConn.getInputStream(); // Criamos o arquivo de
														// entrada
		int caracter; // para ler carater/carater

		while ((caracter = entrada.read()) != -1) // ler o conteudo da URL
		{
			conteudo = conteudo + (char) caracter;
		} // somar o conteudo

		entrada.close();
		if (conteudo.length() == 0)
			return null; // sem conteudo? retorna null
		{
			return conteudo;
		} // sen�o, retorna o conte�do
	}
}
