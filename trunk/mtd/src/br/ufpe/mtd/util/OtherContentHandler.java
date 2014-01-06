package br.ufpe.mtd.util;

import java.io.IOException;
import java.net.ContentHandler;
import java.net.URLConnection;

/**
 * Gerenciador de conteudo para outros tipos MIME. No nosso caso, por default,
 * retornaremos null quando o conteudo nao seja HTML.
 * 
 * @author cmtr@di.ufpe.br
 * @author aml@di.ufpe.br
 */
public class OtherContentHandler extends ContentHandler {
	public Object getContent(URLConnection urlc) throws IOException {
		return null;
	}
}