//Trata o conteúdo da string para fins de processamento do conteúdo baixados da web
package br.ufpe.mtd.util;

import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/* Utilização da classe java.util.Normalizer para remover acentos e absorção do StringChanger.
 */

/* TODO: Usar StringBuilder aonde for possível. */
/* TODO: Utilizar regex aonde for melhor. */
/* TODO: Remover métodos não utilizados em lugar nenhum. */
public class StringConverter {

    private StringConverter() {}

    private static final class Simbolo {
	private final char letra;
	private final char semAcento;
	private final String escapeHtml;
	private final String asciiHtml;//escape html em codigo ascii

	public Simbolo(char letra, char semAcento, String escapeHtml, String asciiHtml) {
	    this.letra = letra;
	    this.semAcento = semAcento;
	    this.escapeHtml = escapeHtml;
	    this.asciiHtml = asciiHtml;
	}

	public char getLetra() {
	    return letra;
	}

	public char getSemAcento() {
	    return semAcento;
	}

	public String getEscapeHtml() {
	    return escapeHtml;
	}
	
	public String getAsciiHtml() {
	    return asciiHtml;
	}
    }

    private static final Collection<Simbolo> ACENTOS;
    private static final Collection<Simbolo> CARACTERES_ESPECIAIS;
    private static final Collection<Simbolo> TODOS_SIMBOLOS;
    private static final Simbolo LINE_BREAK;

    static {
	Collection<Simbolo> acentos = new ArrayList<Simbolo>();
	Collection<Simbolo> especiais = new ArrayList<Simbolo>();
	Collection<Simbolo> tudo = new ArrayList<Simbolo>();

	acentos.add(new Simbolo('à', 'a', "&agrave;","&#224;"));
	acentos.add(new Simbolo('á', 'a', "&aacute;","&#225;"));
	acentos.add(new Simbolo('â', 'a', "&acirc;","&#226;"));
	acentos.add(new Simbolo('ã', 'a', "&atilde;","&#227;"));
	acentos.add(new Simbolo('ä', 'a', "&auml;","&#228;"));
	acentos.add(new Simbolo('å', 'a', "&aring;","&#229;"));
	acentos.add(new Simbolo('æ', 'a', "&aelig;","&#230;"));
	acentos.add(new Simbolo('À', 'A', "&Agrave;","&#192;"));
	acentos.add(new Simbolo('Á', 'A', "&Aacute;","&#193;"));
	acentos.add(new Simbolo('Â', 'A', "&Acirc;","&#194;"));
	acentos.add(new Simbolo('Ã', 'A', "&Atilde;","&#195;"));
	acentos.add(new Simbolo('Ä', 'A', "&Auml;","&#196;"));
	acentos.add(new Simbolo('Å', 'A', "&Aring;","&#197;"));
	acentos.add(new Simbolo('Æ', 'A', "&Aelig;","&#198;"));

	acentos.add(new Simbolo('è', 'e', "&egrave;","&#232;"));
	acentos.add(new Simbolo('é', 'e', "&eacute;","&#233;"));
	acentos.add(new Simbolo('ê', 'e', "&ecirc;","&#234;"));
	acentos.add(new Simbolo('ë', 'e', "&euml;","&#235;"));
	acentos.add(new Simbolo('È', 'E', "&Egrave;","&#200;"));
	acentos.add(new Simbolo('É', 'E', "&Eacute;","&#201;"));
	acentos.add(new Simbolo('Ê', 'E', "&Ecirc;","&#202;"));
	acentos.add(new Simbolo('Ë', 'E', "&Euml;","&#203;"));
	

	acentos.add(new Simbolo('ı', 'i', "&#305;","&#305;"));
	acentos.add(new Simbolo('İ', 'I', "&#304;","&#304;"));
	acentos.add(new Simbolo('í', 'i', "&iacute;","&#237;"));
	acentos.add(new Simbolo('î', 'i', "&icirc;","&#238;"));
	acentos.add(new Simbolo('ï', 'i', "&iuml;","&#239;"));
	acentos.add(new Simbolo('Ì', 'I', "&Igrave;","&#204;"));
	acentos.add(new Simbolo('Í', 'I', "&Iacute;","&#205;"));
	acentos.add(new Simbolo('Î', 'I', "&Icirc;","&#206;"));
	acentos.add(new Simbolo('Ï', 'I', "&Iuml;","&#207;"));

	acentos.add(new Simbolo('ò', 'o', "&ograve;","&#242;"));
	acentos.add(new Simbolo('ó', 'o', "&oacute;","&#243;"));
	acentos.add(new Simbolo('ô', 'o', "&ocirc;","&#244;"));
	acentos.add(new Simbolo('õ', 'o', "&otilde;","&#245;"));
	acentos.add(new Simbolo('ö', 'o', "&ouml;","&#246;"));
	acentos.add(new Simbolo('ø', 'o', "&oslash;","&#248;"));
	acentos.add(new Simbolo('Ò', 'O', "&Ograve;","&#210;"));
	acentos.add(new Simbolo('Ó', 'O', "&Oacute;","&#211;"));
	acentos.add(new Simbolo('Ô', 'O', "&Ocirc;","&#212;"));
	acentos.add(new Simbolo('Õ', 'O', "&Otilde;","&#213;"));
	acentos.add(new Simbolo('Ö', 'O', "&Ouml;","&#214;"));
	acentos.add(new Simbolo('Ø', 'o', "&Oslash;","&#216;"));

	acentos.add(new Simbolo('ù', 'u', "&ugrave;","&#249;"));
	acentos.add(new Simbolo('ú', 'u', "&uacute;","&#250;"));
	acentos.add(new Simbolo('û', 'u', "&ucirc;","&#251;"));
	acentos.add(new Simbolo('ü', 'u', "&uuml;","&#252;"));
	acentos.add(new Simbolo('Ù', 'U', "&Ugrave;","&#217;"));
	acentos.add(new Simbolo('Ú', 'U', "&Uacute;","&#218;"));
	acentos.add(new Simbolo('Û', 'U', "&Ucirc;","&#219;"));
	acentos.add(new Simbolo('Ü', 'U', "&Uuml;","&#220;"));
	
	acentos.add(new Simbolo('ç', 'c', "&ccedil;","&#231;"));
	acentos.add(new Simbolo('Ç', 'C', "&Ccedil;","&#199;"));
	
	acentos.add(new Simbolo('ð', 'd', "&eth;","&#240;"));
	acentos.add(new Simbolo('Ð', 'D', "&ETH;","&#208;"));

	acentos.add(new Simbolo('ý', 'y', "&yacute;","&#253;"));
	acentos.add(new Simbolo('ÿ', 'y', "&yuml;","&#255;"));
	acentos.add(new Simbolo('Ý', 'y', "&Yacute;","&#221;"));
	
	acentos.add(new Simbolo('ñ', 'n', "&ntilde;","&#241;"));
	acentos.add(new Simbolo('Ñ', 'N', "&Ntilde;","&#209;"));
	
	acentos.add(new Simbolo('~', '~', "&tilde;","&#152;"));
	acentos.add(new Simbolo('`', '`', "&grave;","&#96;"));
	acentos.add(new Simbolo('´', '´', "&acute;","&#180;"));
	acentos.add(new Simbolo('^', '^', "&circ;","&#94;"));
	acentos.add(new Simbolo('^', '^', "&circ;","&#136;"));
	
	especiais.add(new Simbolo('<', '<', "&lt;","&#60;"));
	especiais.add(new Simbolo('>', '>', "&gt;","&#62;"));
	especiais.add(new Simbolo('&', '&', "&amp;","&#38;"));//deve vir primeiro para processamento correto
	
	especiais.add(new Simbolo('…', '.', "&hellip;","&#133;"));
	especiais.add(new Simbolo('¢', ' ', "&cent;","&#162;"));
	especiais.add(new Simbolo('®', ' ', "&reg;","&#174;	"));
	especiais.add(new Simbolo('©', ' ', "&copy;","&#169;"));
	especiais.add(new Simbolo('™', ' ', "&trade;","&#8482;"));
	especiais.add(new Simbolo('Þ', ' ', "&THORN;","&#222;"));
	especiais.add(new Simbolo('þ', ' ', "&thorn;","&#254;"));
	especiais.add(new Simbolo('ß', ' ', "&szlig;","&#223;"));
	especiais.add(new Simbolo('µ', ' ', "&micro;","&#181;"));
	
	especiais.add(new Simbolo('α', ' ', "&alpha;","&#945;"));
	especiais.add(new Simbolo('β', ' ', "&beta;","&#946;"));
	especiais.add(new Simbolo('μ', ' ', "&mu;","&#956;"));
	especiais.add(new Simbolo('∂', ' ', "&part;","&#8706;"));
	
	especiais.add(new Simbolo('∞', ' ', "&infin;","&#8734;"));
	especiais.add(new Simbolo('≤', ' ', "&le;","&#8804;"));
	especiais.add(new Simbolo('≥', ' ', "&ge;","&#8805;"));
	
	especiais.add(new Simbolo('\'', '\'', "&#39;","&#39;"));
	especiais.add(new Simbolo('\"', '\"', "&quot;","&#34;"));
	especiais.add(new Simbolo('‘', '\"', "&lsquo;","&#145;"));//left single quotation mark, U+2018 ISOnum
	especiais.add(new Simbolo('’', '\"', "&rsquo;","&#146;"));//right single quotation mark, U+2019 ISOnum
	especiais.add(new Simbolo('“', '\"', "&ldquo;","&#147;"));//left double quotation mark, U+201C ISOnum
	especiais.add(new Simbolo('”', '\"', "&rdquo;","&#148;"));//right double quotation mark, U+201D ISOnum
	
	especiais.add(new Simbolo('‘', '\"', "&lsquo;","&#8216;"));//left single quotation mark, U+2018 ISOnum
	especiais.add(new Simbolo('’', '\"', "&rsquo;","&#8217;"));//right single quotation mark, U+2019 ISOnum
	especiais.add(new Simbolo('“', '\"', "&ldquo;","&#8220;"));//left double quotation mark, U+201C ISOnum
	especiais.add(new Simbolo('”', '\"', "&rdquo;","&#8221;"));//right double quotation mark, U+201D ISOnum
	
	especiais.add(new Simbolo('–', '-', "&ndash;","&#150"));
	especiais.add(new Simbolo('—', '-', "&mdash;","&#151"));
	especiais.add(new Simbolo('_', '-', "&#95;","&#95"));//underscore
	
	especiais.add(new Simbolo('‐', '-', "&ndash;","&#8208"));
	especiais.add(new Simbolo('–', '-', "&ndash;","&#8211"));//en dash, U+2013 ISOpub
	especiais.add(new Simbolo('—', '-', "&mdash;","&#8212;"));//em dash, U+2014 ISOpub
	especiais.add(new Simbolo('−', '-', "&minus;","&#8722;"));
	
	especiais.add(new Simbolo('\t', ' ', "&tab;","&#9;"));
	especiais.add(new Simbolo(' ', ' ', "&nbsp;","&#32;"));
	especiais.add(new Simbolo('\r', ' ', "&#13;","&#13;"));
	LINE_BREAK = new Simbolo('\n', ' ', "&NewLine;","&#10;");//<br>
	especiais.add(LINE_BREAK);

	tudo.addAll(acentos);
	tudo.addAll(especiais);

	ACENTOS = Collections.unmodifiableCollection(acentos);
	CARACTERES_ESPECIAIS = Collections.unmodifiableCollection(especiais);
	TODOS_SIMBOLOS = Collections.unmodifiableCollection(tudo);
    }

    /**
     * Converte caracteres especiais do word e pdf
     * @param str palavra a ser convertida
     * @return String palavra convertida
     */
    public static String converteCaracteresEspeciais(String str) {
	// Converte caracteres especiais do Word
	String returnStr = str;
	char c;
	c = 145; // abre aspas simples do word
	returnStr = returnStr.replace(c, '\'');
	c = 146; // fecha aspas simples do word
	returnStr = returnStr.replace(c, '\'');
	c = 147; // abre aspas do word
	returnStr = returnStr.replace(c, '"');
	c = 148; // fecha aspas do word
	returnStr = returnStr.replace(c, '"');
	c = 150; // travessão
	returnStr = returnStr.replace(c, '-');
	c = 151; // travessão
	returnStr = returnStr.replace(c, '-');

	//Converte caracteres especiais do PDF
	c = '“';// abre aspas do pdf
	returnStr = returnStr.replace(c, '"');
	c = '”';// fecha aspas do pdf
	returnStr = returnStr.replace(c, '"');
	c = '-';// travessão do pdf
	returnStr = returnStr.replace(c, '-');
	returnStr = returnStr.replace('‘', '\''); //left single quotation mark, U+2018 ISOnum
	returnStr = returnStr.replace('’', '\''); //right single quotation mark, U+2019 ISOnum
	returnStr = returnStr.replace('“', '\"');//left double quotation mark, U+201C ISOnum
	returnStr = returnStr.replace('”', '\"');//right double quotation mark, U+201D ISOnum
	returnStr = returnStr.replace('–', '-');//en dash, U+2013 ISOpub
	returnStr = returnStr.replace('—', '-');//em dash, U+2014 ISOpub
	returnStr = returnStr.replace('−', '-');//"&minus;" "&#8722;"
	
	return returnStr;
    }
    


    public static String convertSpaces(String aString) {
	return aString == null ? "" : changeSubstring(aString, " ", "%20");
    }

    
    /**
     * changes the Html &; notation (used just to store in DB)
     * to special letters of an input string
     * @return java.lang.String
     * @param aString java.lang.String
     */
    public static String fromHtmlNotation(String aString) {
	if (aString == null) return "";

	String returnStr = aString;
	for (Simbolo s : TODOS_SIMBOLOS) {
	    returnStr = changeSubstring(returnStr, s.getEscapeHtml(), String.valueOf(s.getLetra()));
	    returnStr = changeSubstring(returnStr, s.getAsciiHtml(), String.valueOf(s.getLetra()));
	}

	return returnStr;
    }

    /**
     * This method was created in VisualAge.
     * @return java.lang.String
     * @param aString java.lang.String
     */
    public static String fromHtmlNotationWithoutLineBreak(String aString) {
	if (aString == null) return "";

	String returnStr = aString;

	for (Simbolo s : TODOS_SIMBOLOS) {
	    if (s == LINE_BREAK) continue;
	    returnStr = changeSubstring(returnStr, s.getEscapeHtml(), String.valueOf(s.getLetra()));
	    returnStr = changeSubstring(returnStr, s.getAsciiHtml(), String.valueOf(s.getLetra()));
	}

	return returnStr;
    }

    /**
     * Coloca a inicial de uma string em letra maiúscula
     * @param str palavra a ser alterada
     * @return String palvra alterada
     */
    public static String inicialMaiuscula(String str) {
	if (str == null) return null;
	if (str.isEmpty()) return "";
	return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /**
     * Coloca a inicial de uma string em letra minúscula
     * @param str palavra a ser alterada
     * @return String palavra alterada
     */
    public static String inicialMinuscula(String str) {
	if (str == null) return null;
	if (str.isEmpty()) return "";
	return str.substring(0, 1).toLowerCase() + str.substring(1);
    }

    /**
     * Deleta os acentos de um String
     * @param aString palavra a ser removida os acentos
     * @return String palvra alterada
     */
    public static String deleteAcentos(String aString) {
        if (aString == null) return null;
        String norm = Normalizer.normalize(aString, Form.NFKD);
        return norm.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    /**
     * Retira os espaços desnecessários de um String
     * @param aString palavra a ser retirado os espaços
     * @return String palavra alterada
     */
    public static String unecessarySpaces(String aString) {

	if (aString == null) return "";

	String returnStr = aString.trim();

	while (returnStr.indexOf("  ") != -1) {
	    returnStr = changeSubstring(returnStr, "  ", " ");
	}

	return returnStr;
    }

    /**
     * Substitui uma palvra dentro de um texto
     * @param aString texto a ser modificado
     * @param txtToFind texto a ser encontrado
     * @param txtToReplace texto a ser trocado
     * @return String Texto modificado
     */
    public static String replace(String aString, String txtToFind, String txtToReplace) {
	return (aString == null) ? "" : changeSubstring(aString, txtToFind,txtToReplace);
    }

    /**
     * toDataBaseNotation method
     *
     * changes the special letters of an input string
     * to Html &; notation (just to store in DB)
     *
     * @return java.lang.String
     * @param aString java.lang.String	input string
     */
    public static String toDataBaseNotation(String aString) {
	if (aString == null) return "";

	String returnStr = aString;

	char c[] = new char[1];
	c[0] = 147; // abre aspas do word
	returnStr = changeSubstring(returnStr, String.valueOf(c), "\"");
	c[0] = 148; // fecha aspas do word
	returnStr = changeSubstring(returnStr, String.valueOf(c), "\"");
	returnStr = changeSubstring(returnStr, "'", "&#39;");
	returnStr = changeSubstring(returnStr, "?", "&#63;");
	returnStr = changeSubstring(returnStr, "\"", "&quot;");

	return returnStr;
    }

    /**
     * toHtmlNotation method
     *
     * changes the stressed and the special letters of an input string
     * to Html &; notation
     *
     * @return java.lang.String
     * @param aString java.lang.String	input string
     */
    public static String toHtmlNotation(String aString) {
	if (aString == null) return "";

	String returnStr = aString;

	for (Simbolo s : TODOS_SIMBOLOS) {
	    returnStr = changeSubstring(returnStr, String.valueOf(s.getLetra()), s.getEscapeHtml());
	}

	// Altera os amps existentes para nao confundir com os acrescentados
	returnStr = changeSubstring(returnStr, "&", "&amp;");
	returnStr = changeSubstring(returnStr, "\r", "");
	returnStr = changeSubstring(returnStr, "<", "&lt;");
	returnStr = changeSubstring(returnStr, ">", "&gt;");
	return returnStr;
    }

    /**
     * toHtmlNotation method
     *
     * changes the [\n] caracter to [\n" + "] string to avoid the "Unterminated string constant"
     * JavaScript error
     *
     * @return java.lang.String
     * @param aString java.lang.String	input string
     */
    public static String toJavaScriptNotation(String aString) {
	if (aString == null) return "";

	String returnStr = aString;

	returnStr = changeSubstring(returnStr, "\r", "");
	returnStr = changeSubstring(returnStr, "\n", "\\n");
	returnStr = changeSubstring(returnStr, "\"", "\\\"");
	returnStr = changeSubstring(returnStr, "'", "\\'");

	return returnStr;
    }

    /**
     * toUnStressedNotation method
     *
     * changes the stressed, the special letters and the Html &; notation
     * to unstressed letters in an input string
     *
     * @return java.lang.String
     * @param aString java.lang.String input string
     */
    public static String toUnStressedNotation(String aString) {

	if (aString == null) return "";

	String returnStr = aString;

	for (Simbolo s : ACENTOS) {
	    returnStr = changeSubstring(returnStr, String.valueOf(s.getLetra()), String.valueOf(s.getSemAcento()));
	}
	for (Simbolo s : TODOS_SIMBOLOS) {
	    returnStr = changeSubstring(returnStr, s.getEscapeHtml(), String.valueOf(s.getSemAcento()));
	    returnStr = changeSubstring(returnStr, s.getAsciiHtml(), String.valueOf(s.getSemAcento()));
	}

	return returnStr;
    }

    private static String changeSubstring(String aString, String oldString, String newString) {
	int currentPosition = 0;

	while (true) {
            currentPosition = aString.indexOf(oldString, currentPosition);
            if (currentPosition == -1) break;

	    aString =
                    aString.substring(0, currentPosition) +
	            newString +
	            aString.substring(currentPosition + oldString.length());

	    currentPosition += newString.length();
	}

	return aString;
    }
    
    public static String corrigeAcentos(String texto){
    	
    	return texto.replaceAll("\\~a","ã").replaceAll("\\~o","õ").replaceAll("\\~A","Ã").replaceAll("\\~O","Õ")
    			.replaceAll("\\\'a","á").replaceAll("\\\'e","é").replaceAll("\\\'i","í").replaceAll("\\\'o","ó").replaceAll("\\\'u","ú")
    			.replaceAll("\\¶a","á").replaceAll("\\¶e","é").replaceAll("\\¶i","í").replaceAll("\\¶o","ó").replaceAll("\\¶u","ú")
    			.replaceAll("\\´a","á").replaceAll("\\´e","é").replaceAll("\\´i","í").replaceAll("\\´o","ó").replaceAll("\\´u","ú")
    			.replaceAll("\\^e","ê").replaceAll("\\^o","ô").replaceAll("\\^E","Ê").replaceAll("\\^O","Ô")
    			.replaceAll("\\\"u","ü").replaceAll("¯","fi")
    			.replaceAll("\\¸c","ç").replaceAll("\\\'c","ç").replaceAll("»c","ç").replaceAll("\\´c","ç");
    }
    
    
    public static void main(String[] args){
           String s = "testando &aacute;gua para v&ecirc;r se d&aacute;&#32;certo.";
           System.out.println(s);
           System.out.println(StringConverter.fromHtmlNotation(s));
           System.out.println(deleteAcentos(StringConverter.fromHtmlNotation(s)));
           System.out.println(toUnStressedNotation(s));
           System.out.println(s.replaceAll("&(#[0-9]*|[A-Za-z]{2,6});", " "));//html code)
           
           String NPregexp = "([A-Z]{2,}|[A-Z]{1}([a-z]+|[.])([ ]*([A-Z][a-z]+|[a-z]{5,}|([A-Z][.][ ]?)+))*([ ]*[0-9A-Z])*)";
           String siglasMaiuscExp = "([A-Z]{2,}|[A-Z]{1}[a-zà-ú]*[.]([ ]*([A-Z][a-z]+|[a-z]{3,}|([A-Z][.])+))*([ ]*[0-9A-Z])*)";
           s.replaceAll("[.](?=($|[ ][A-ZÀ-Ú]{1}[a-zà-ú]*))"," . ");//identificando ponto final
           String charEpontof = "(?:([^A-Z]))[.](?=($|[ ][A-ZÀ-Ú]{1}[a-zà-ú]*[^.]))";
           String aux = "990";
           System.out.println(aux.matches("[0-9]+[.|,]?[0-9]*[a-z°]{0,3}?")&& !aux.matches("[0-9]{4}"));
           s = "Solução k&amp;#8722;Jato e de uma (©,k)&amp;#8722;Envoltória Convexa";
           System.out.println(StringConverter.fromHtmlNotation(s));
           System.out.println(corrigeAcentos("informa c~ao e publica»c~ao t´ecnicas, gerenciamento e a recupera»c~ao da informa»c~ao digital. existentes ¶e a aus^encia de mecanismos de integra»c~ao de dados, de maneira a fornecer ao usu¶ario, acesso uni¯cado e transparente aos reposit¶orios gerenciados por diferentes servi»cos."));
           
           //http://www.devmedia.com.br/validando-formularios-usando-regular-expression/12042
           String numeroInteiro = "^[0-9]+$";
           String numeroReal2 = "^[0-9]+?(.|,[0-9]+)$";
           String email = "^([\\w\\-]+\\.)*[\\w\\- ]+@([\\w\\- ]+\\.)+([\\w\\-]{2,3})$";
           String telefone2 = "^[0-9]{2}-[0-9]{4}-[0-9]{4}$";
           String cpf = "^\\d{3}\\.?\\d{3}\\.?\\d{3}\\-?\\d{2}$";
           String cnpj = "^\\d{3}.?\\d{3}.?\\d{3}/?\\d{3}-?\\d{2}$";
           String data = "^((0[1-9]|[12]\\d)\\/(0[1-9]|1[0-2])|30\\/(0[13-9]|1[0-2])|31\\/(0[13578]|1[02]))\\/\\d{4}$";
           String cep = "^\\d{5}\\-?\\d{3}$";
           String url = "^((http)|(https)|(ftp)):\\/\\/([\\- \\w]+\\.)+\\w{2,3}(\\/ [%\\-\\w]+(\\.\\w{2,})?)*$";
           String ip =  "^\\b\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\b$";
           String nome2 = "^[aA-zZ]+((\\s[aA-zZ]+)+)?$";
           
           
           
    }
}