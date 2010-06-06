package main;


import java.io.IOException;
import java.io.InputStream;
import java.net.ContentHandler;
import java.net.ContentHandlerFactory;
import java.net.URLConnection;

/** 
 * Criamos um contentHandlerFactory para eliminar o ContentHandler
 * default de Java para HTML e XML. Neste caso, se não for HTML ou XML retornar
 * null.
 *
 * @author Renato
 * @author cmtr@di.ufpe.br
 * @author aml@di.ufpe.br 
 */
class HTTPContentHandler implements ContentHandlerFactory 
{
	public ContentHandler createContentHandler( String mimeType ) 
	{
		if (mimeType == null) return null;
		if (mimeType.equalsIgnoreCase("text/html") || mimeType.equalsIgnoreCase("text/xml"))
		{	return new TextContentHandler(); }         // retornar gerenciador para HTML/XML
		else
		{ return new OtherContentHandler(); }			// retornar gerenciador para outros tipos de conteúdo.
	}
}

/** 
 * Um gerenciador de conteudo para paginas HTML.  Segundo o API de java.net
 * o gerenciador de conteúdo só tem a responsabilidade de abrir o arquivo e
 * ler dele o conteudo.  Convertindo para o tipo de dados específico:
 * HTML = String, Text = String, Img = (java.awt.Image), etc..  A implementação
 * por default de Java, pega o conteúdo e elimina dele todos os carateres
 * acentuados ou especiais.  Esta implementação pega o conteúdo do jeito que
 * está e retorna ele como um String.
 *
 * @author cmtr@di.ufpe.br
 * @author aml@di.ufpe.br 
 */
class TextContentHandler extends ContentHandler 
{
	public Object getContent(URLConnection urlConn) throws IOException 
	{
		String conteudo = "";                           // String para o conteúdo
		InputStream entrada = urlConn.getInputStream(); // Criamos o arquivo de entrada
		int caracter;                                   // para ler carater/carater

    while ((caracter = entrada.read()) != -1)				// ler o conteudo da URL
    {	conteudo = conteudo + (char) caracter; }			// somar o conteúdo
    
    entrada.close();
    if (conteudo.length() == 0) return null;				// sem conteúdo? retorna null
    { return conteudo; }														// senão, retorna o conteúdo
	}
}

/**
 * Gerenciador de conteúdo para outros tipos MIME. No nosso caso, por
 * default, retornaremos null quando o conteúdo nao seja HTML.
 *
 * @author cmtr@di.ufpe.br
 * @author aml@di.ufpe.br 
 */
class OtherContentHandler extends ContentHandler 
{
	public Object getContent(URLConnection urlc) throws IOException 
	{ return null; }
}