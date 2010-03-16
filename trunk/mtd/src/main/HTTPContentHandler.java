package main;


import java.io.IOException;
import java.io.InputStream;
import java.net.ContentHandler;
import java.net.ContentHandlerFactory;
import java.net.URLConnection;

/** 
 * Criamos um contentHandlerFactory para eliminar o ContentHandler
 * default de Java para HTML e XML. Neste caso, se n�o for HTML ou XML retornar
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
		{ return new OtherContentHandler(); }			// retornar gerenciador para outros tipos de conte�do.
	}
}

/** 
 * Um gerenciador de conteudo para paginas HTML.  Segundo o API de java.net
 * o gerenciador de conte�do s� tem a responsabilidade de abrir o arquivo e
 * ler dele o conteudo.  Convertindo para o tipo de dados espec�fico:
 * HTML = String, Text = String, Img = (java.awt.Image), etc..  A implementa��o
 * por default de Java, pega o conte�do e elimina dele todos os carateres
 * acentuados ou especiais.  Esta implementa��o pega o conte�do do jeito que
 * est� e retorna ele como um String.
 *
 * @author cmtr@di.ufpe.br
 * @author aml@di.ufpe.br 
 */
class TextContentHandler extends ContentHandler 
{
	public Object getContent(URLConnection urlConn) throws IOException 
	{
		String conteudo = "";                           // String para o conte�do
		InputStream entrada = urlConn.getInputStream(); // Criamos o arquivo de entrada
		int caracter;                                   // para ler carater/carater

    while ((caracter = entrada.read()) != -1)				// ler o conteudo da URL
    {	conteudo = conteudo + (char) caracter; }			// somar o conte�do
    
    entrada.close();
    if (conteudo.length() == 0) return null;				// sem conte�do? retorna null
    { return conteudo; }														// sen�o, retorna o conte�do
	}
}

/**
 * Gerenciador de conte�do para outros tipos MIME. No nosso caso, por
 * default, retornaremos null quando o conte�do nao seja HTML.
 *
 * @author cmtr@di.ufpe.br
 * @author aml@di.ufpe.br 
 */
class OtherContentHandler extends ContentHandler 
{
	public Object getContent(URLConnection urlc) throws IOException 
	{ return null; }
}