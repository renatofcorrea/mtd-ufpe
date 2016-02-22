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

	especiais.add(new Simbolo('\t', ' ', "&tab;","&#9;"));
	LINE_BREAK = new Simbolo('\n', ' ', "&NewLine;","&#10;");//<br>
	especiais.add(LINE_BREAK);
	especiais.add(new Simbolo('\r', ' ', "&#13;","&#13;"));
	especiais.add(new Simbolo(' ', ' ', "&nbsp;","&#32;"));

	
	especiais.add(new Simbolo('&', '&', "&amp;","&#38;"));//deve vir primeiro para processamento correto
	especiais.add(new Simbolo('\'', '\'', "&#39;","&#39;"));
	especiais.add(new Simbolo('<', '<', "&lt;","&#60;"));
	especiais.add(new Simbolo('>', '>', "&gt;","&#62;"));
	especiais.add(new Simbolo('<', '<', "&lt;","&#706;"));
	
	acentos.add(new Simbolo('^', '^', "&circ;","&#94;"));
	acentos.add(new Simbolo('`', '`', "&grave;","&#96;"));
	acentos.add(new Simbolo('^', '^', "&circ;","&#136;"));
	acentos.add(new Simbolo('~', '~', "&tilde;","&#152;"));
	acentos.add(new Simbolo('´', '´', "&acute;","&#180;"));
	
	especiais.add(new Simbolo('_', '-', "&#95;","&#95"));//underscore
	especiais.add(new Simbolo('–', '-', "&ndash;","&#150"));
	especiais.add(new Simbolo('—', '-', "&mdash;","&#151"));
	

	especiais.add(new Simbolo('…', '.', "&hellip;","&#133;"));
	
	especiais.add(new Simbolo('\"', '\"', "&quot;","&#34;"));
	especiais.add(new Simbolo('\'', '\'', "&#61602;","&#61602;"));
	especiais.add(new Simbolo('‘', '\'', "&lsquo;","&#145;"));//left single quotation mark, U+2018 ISOnum
	especiais.add(new Simbolo('’', '\'', "&rsquo;","&#146;"));//right single quotation mark, U+2019 ISOnum
	especiais.add(new Simbolo('“', '\"', "&ldquo;","&#147;"));//left double quotation mark, U+201C ISOnum
	especiais.add(new Simbolo('”', '\"', "&rdquo;","&#148;"));//right double quotation mark, U+201D ISOnum
	
	
	especiais.add(new Simbolo('¢', ' ', "&cent;","&#162;"));
	especiais.add(new Simbolo('©', ' ', "&copy;","&#169;"));
	especiais.add(new Simbolo('®', ' ', "&reg;","&#174;	"));
	especiais.add(new Simbolo('µ', ' ', "&micro;","&#181;"));
	
	especiais.add(new Simbolo('º',	' ',"&ordm;",	"&#186;"));
	especiais.add(new Simbolo('˚',	' ',"&ordm;",	"&#730;"));
	especiais.add(new Simbolo('⁰',	' ',"&ordm;",	"&#8304;")); 
	especiais.add(new Simbolo('◦',	' ',"&#9702;",	"&#9702;")); 
	
	acentos.add(new Simbolo('À', 'A', "&Agrave;","&#192;"));
	acentos.add(new Simbolo('Á', 'A', "&Aacute;","&#193;"));
	acentos.add(new Simbolo('Â', 'A', "&Acirc;","&#194;"));
	acentos.add(new Simbolo('Ã', 'A', "&Atilde;","&#195;"));
	acentos.add(new Simbolo('Ä', 'A', "&Auml;","&#196;"));
	acentos.add(new Simbolo('Å', 'A', "&Aring;","&#197;"));
	acentos.add(new Simbolo('Æ', 'A', "&Aelig;","&#198;"));
	acentos.add(new Simbolo('Ç', 'C', "&Ccedil;","&#199;"));
	
		
	acentos.add(new Simbolo('È', 'E', "&Egrave;","&#200;"));
	acentos.add(new Simbolo('É', 'E', "&Eacute;","&#201;"));
	acentos.add(new Simbolo('Ê', 'E', "&Ecirc;","&#202;"));
	acentos.add(new Simbolo('Ë', 'E', "&Euml;","&#203;"));
	acentos.add(new Simbolo('Ì', 'I', "&Igrave;","&#204;"));
	acentos.add(new Simbolo('Í', 'I', "&Iacute;","&#205;"));
	acentos.add(new Simbolo('Î', 'I', "&Icirc;","&#206;"));
	acentos.add(new Simbolo('Ï', 'I', "&Iuml;","&#207;"));
	acentos.add(new Simbolo('Ð', 'D', "&ETH;","&#208;"));
	acentos.add(new Simbolo('Ñ', 'N', "&Ntilde;","&#209;"));
	acentos.add(new Simbolo('Ò', 'O', "&Ograve;","&#210;"));
	acentos.add(new Simbolo('Ó', 'O', "&Oacute;","&#211;"));
	acentos.add(new Simbolo('Ô', 'O', "&Ocirc;","&#212;"));
	acentos.add(new Simbolo('Õ', 'O', "&Otilde;","&#213;"));
	acentos.add(new Simbolo('Ö', 'O', "&Ouml;","&#214;"));
	acentos.add(new Simbolo('Ø', 'o', "&Oslash;","&#216;"));
	acentos.add(new Simbolo('Ù', 'U', "&Ugrave;","&#217;"));
	acentos.add(new Simbolo('Ú', 'U', "&Uacute;","&#218;"));
	acentos.add(new Simbolo('Û', 'U', "&Ucirc;","&#219;"));
	acentos.add(new Simbolo('Ü', 'U', "&Uuml;","&#220;"));
	acentos.add(new Simbolo('Ý', 'y', "&Yacute;","&#221;"));
	
	especiais.add(new Simbolo('Þ', ' ', "&THORN;","&#222;"));
	especiais.add(new Simbolo('ß', ' ', "&szlig;","&#223;"));
	
	acentos.add(new Simbolo('à', 'a', "&agrave;","&#224;"));
	acentos.add(new Simbolo('á', 'a', "&aacute;","&#225;"));
	acentos.add(new Simbolo('â', 'a', "&acirc;","&#226;"));
	acentos.add(new Simbolo('ã', 'a', "&atilde;","&#227;"));
	acentos.add(new Simbolo('ä', 'a', "&auml;","&#228;"));
	acentos.add(new Simbolo('å', 'a', "&aring;","&#229;"));
	acentos.add(new Simbolo('æ', 'a', "&aelig;","&#230;"));
	acentos.add(new Simbolo('ç', 'c', "&ccedil;","&#231;"));
	
	acentos.add(new Simbolo('è', 'e', "&egrave;","&#232;"));
	acentos.add(new Simbolo('é', 'e', "&eacute;","&#233;"));
	acentos.add(new Simbolo('ê', 'e', "&ecirc;","&#234;"));
	acentos.add(new Simbolo('ë', 'e', "&euml;","&#235;"));
	acentos.add(new Simbolo('í', 'i', "&iacute;","&#237;"));
	acentos.add(new Simbolo('î', 'i', "&icirc;","&#238;"));
	acentos.add(new Simbolo('ï', 'i', "&iuml;","&#239;"));
	acentos.add(new Simbolo('í', 'i', "&iacute;","&#943;"));

	acentos.add(new Simbolo('ð', 'd', "&eth;","&#240;"));
	acentos.add(new Simbolo('ñ', 'n', "&ntilde;","&#241;"));
	acentos.add(new Simbolo('ò', 'o', "&ograve;","&#242;"));
	acentos.add(new Simbolo('ó', 'o', "&oacute;","&#243;"));
	acentos.add(new Simbolo('ô', 'o', "&ocirc;","&#244;"));
	acentos.add(new Simbolo('õ', 'o', "&otilde;","&#245;"));
	acentos.add(new Simbolo('ö', 'o', "&ouml;","&#246;"));
	acentos.add(new Simbolo('ø', 'o', "&oslash;","&#248;"));
	
	acentos.add(new Simbolo('ù', 'u', "&ugrave;","&#249;"));
	acentos.add(new Simbolo('ú', 'u', "&uacute;","&#250;"));
	acentos.add(new Simbolo('û', 'u', "&ucirc;","&#251;"));
	acentos.add(new Simbolo('ü', 'u', "&uuml;","&#252;"));
	acentos.add(new Simbolo('ű', 'u', "&uuml;","&#369;"));
	acentos.add(new Simbolo('ý', 'y', "&yacute;","&#253;"));
	
	acentos.add(new Simbolo('ÿ', 'y', "&yuml;","&#255;"));
	
	acentos.add(new Simbolo('ı', 'i', "&#305;","&#305;"));
	acentos.add(new Simbolo('İ', 'I', "&#304;","&#304;"));
	
	
	acentos.add(new Simbolo('ł', 'l', "&#322;","&#322;"));
	acentos.add(new Simbolo('ń', 'n', "&#324;","&#324;"));
	acentos.add(new Simbolo('Ż', 'Z', "&#379;","&#379;"));
	  
	especiais.add(new Simbolo(';', ';',"&#894;","&#894;")); 
	
	especiais.add(new Simbolo('þ', ' ', "&thorn;","&#254;"));
	especiais.add(new Simbolo('α', ' ', "&alpha;","&#945;"));
	especiais.add(new Simbolo('α', ' ', "&alpha;","#61537;"));
	especiais.add(new Simbolo('β', ' ', "&beta;","&#946;"));
	especiais.add(new Simbolo('μ', ' ', "&mu;","&#956;"));
	
	
	//Example	Named Entity	Numeric	Entity	Description
	especiais.add(new Simbolo('ƒ', ' ', "&fnof;","&#402;")); //Latin small f with hook
	especiais.add(new Simbolo('Α', ' ', "&Alpha;","&#913;")); //Greek capital letter alpha
	especiais.add(new Simbolo('Β', ' ', "&Beta;","&#914;")); //Greek capital letter beta
	especiais.add(new Simbolo('Γ', ' ', "&Gamma;","&#915;")); //Greek capital letter gamma
	especiais.add(new Simbolo('Δ', ' ', "&Delta;","&#916;")); //Greek capital letter delta
	especiais.add(new Simbolo('∆', ' ', "&Delta;","&#8710;"));
	especiais.add(new Simbolo('Ε', ' ', "&Epsilon;","&#917;")); //Greek capital letter epsilon
	especiais.add(new Simbolo('Ζ', ' ', "&Zeta;","&#918;")); //Greek capital letter zeta
	especiais.add(new Simbolo('Η', ' ', "&Eta;","&#919;")); //Greek capital letter eta
	especiais.add(new Simbolo('Θ', ' ', "&Theta;","&#920;")); //Greek capital letter theta
	especiais.add(new Simbolo('Ι', ' ', "&Iota;","&#921;")); //Greek capital letter iota
	especiais.add(new Simbolo('Κ', ' ', "&Kappa;","&#922;")); //Greek capital letter kappa
	especiais.add(new Simbolo('Λ', ' ', "&Lambda;","&#923;")); //Greek capital letter lambda
	especiais.add(new Simbolo('Μ', ' ', "&Mu;","&#924;")); //Greek capital letter mu
	especiais.add(new Simbolo('Ν', ' ', "&Nu;","&#925;")); //Greek capital letter nu
	especiais.add(new Simbolo('Ξ', ' ', "&Xi;","&#926;")); //Greek capital letter xi
	especiais.add(new Simbolo('Ο', ' ', "&Omicron;","&#927;")); //Greek capital letter omicron
	especiais.add(new Simbolo('Π', ' ', "&Pi;","&#928;")); //Greek capital letter pi
	especiais.add(new Simbolo('Ρ', ' ', "&Rho;","&#929;")); //Greek capital letter rho
	//Note: There’s no Sigmaf, ” final sigma”, &#930; defined in iso-grk
	especiais.add(new Simbolo('Σ', ' ', "&Sigma;","&#931;")); //Greek capital letter sigma
	especiais.add(new Simbolo('Τ', ' ', "&Tau;","&#932;")); //Greek capital letter tau
	especiais.add(new Simbolo('Υ', ' ', "&Upsilon;","&#933;")); //Greek capital letter upsilon
	especiais.add(new Simbolo('Φ', ' ', "&Phi;","&#934;")); //Greek capital letter phi
	
	especiais.add(new Simbolo('Χ', ' ', "&Chi;","&#935;")); //Greek capital letter chi
	especiais.add(new Simbolo('Ψ', ' ', "&Psi;","&#936;")); //Greek capital letter psi
	especiais.add(new Simbolo('Ω', ' ', "&Omega;","&#937;")); //Greek capital letter omega
	especiais.add(new Simbolo('α', ' ', "&alpha;","&#945;")); //Greek small letter alpha
	especiais.add(new Simbolo('β', ' ', "&beta;","&#946;")); //Greek small letter beta
	especiais.add(new Simbolo('γ', ' ', "&gamma;","&#947;")); //Greek small letter gamma
	especiais.add(new Simbolo('δ', ' ', "&delta;","&#948;")); //Greek small letter delta
	especiais.add(new Simbolo('ε', ' ', "&epsilon;","&#949;")); //Greek small letter epsilon
	especiais.add(new Simbolo('ζ', ' ', "&zeta;","&#950;")); //Greek small letter zeta
	especiais.add(new Simbolo('η', ' ', "&eta;","&#951;")); //Greek small letter eta
	especiais.add(new Simbolo('θ', ' ', "&theta;","&#952;")); //Greek small letter theta
	especiais.add(new Simbolo('ι', ' ', "&iota;","&#953;")); //Greek small letter iota
	especiais.add(new Simbolo('κ', ' ', "&kappa;","&#954;")); //Greek small letter kappa
	especiais.add(new Simbolo('λ', ' ', "&lambda;","&#955;")); //Greek small letter lambda
	especiais.add(new Simbolo('μ', ' ', "&mu;","&#956;")); //Greek small letter mu
	especiais.add(new Simbolo('μ', ' ', "&mu;","&#61549;"));
	especiais.add(new Simbolo('ν', ' ', "&nu;","&#957;")); //Greek small letter nu
	especiais.add(new Simbolo('ξ', ' ', "&xi;","&#958;")); //Greek small letter xi
	especiais.add(new Simbolo('ο', ' ', "&omicron;","&#959;")); //Greek small letter omicron
	especiais.add(new Simbolo('π', ' ', "&pi;","&#960;")); //Greek small letter pi
	especiais.add(new Simbolo('ρ', ' ', "&rho;","&#961;")); //Greek small letter rho
	especiais.add(new Simbolo('ς', ' ', "&sigmaf;","&#962;")); //Greek small letter final sigma
	especiais.add(new Simbolo('σ', ' ', "&sigma;","&#963;")); //Greek small letter sigma
	especiais.add(new Simbolo('σ', ' ', "&sigma;","&#61555;"));
	especiais.add(new Simbolo('τ', ' ', "&tau;","&#964;")); //Greek small letter tau
	especiais.add(new Simbolo('υ', ' ', "&upsilon;","&#965;")); //Greek small letter upsilon
	especiais.add(new Simbolo('φ', ' ', "&phi;","&#966;")); //Greek small letter phi
	especiais.add(new Simbolo('ϕ', ' ', "&phi;","&#981;")); 
	especiais.add(new Simbolo('χ', ' ', "&chi;","&#967;")); //Greek small letter chi
	especiais.add(new Simbolo('ψ', ' ', "&psi;","&#968;")); //Greek small letter psi
	especiais.add(new Simbolo('ω', ' ', "&omega;","&#969;")); //Greek small letter omega
	especiais.add(new Simbolo('ϑ', ' ', "&thetasym;","&#977;")); //Greek small letter theta symbol
	especiais.add(new Simbolo('ϒ', ' ', "&upsih;","&#978;")); //Greek upsilon with hook symbol
	especiais.add(new Simbolo('ϖ', ' ', "&piv;","&#982;")); //pi symbol
	
	
	especiais.add(new Simbolo('‐', '-', "&ndash;","&#8208"));
	especiais.add(new Simbolo('–', '-', "&ndash;","&#8211"));//en dash, U+2013 ISOpub
	especiais.add(new Simbolo('—', '-', "&mdash;","&#8212;"));//em dash, U+2014 ISOpub
	especiais.add(new Simbolo('“', '\"', "&lsquo;","&#8213;"));
	especiais.add(new Simbolo('”', '\"', "&rsquo;","&#8214;"));
	
	especiais.add(new Simbolo('‘', '\'', "&lsquo;","&#8216;"));//left single quotation mark, U+2018 ISOnum
	especiais.add(new Simbolo('’', '\'', "&rsquo;","&#8217;"));//right single quotation mark, U+2019 ISOnum
	especiais.add(new Simbolo('“', '\"', "&ldquo;","&#8220;"));//left double quotation mark, U+201C ISOnum
	especiais.add(new Simbolo('”', '\"', "&rdquo;","&#8221;"));//right double quotation mark, U+201D ISOnum
	especiais.add(new Simbolo('’', '\'', "&rsquo;","&#8223;"));
	
	especiais.add(new Simbolo('•', ' ', "&bull;","&#8226;")); //bullet
	especiais.add(new Simbolo('…', ' ', "&hellip;","&#8230;")); //horizontal ellipsis
	especiais.add(new Simbolo('′', ' ', "&prime;","&#8242;")); //prime
	especiais.add(new Simbolo('″', ' ', "&Prime;","&#8243;")); //double prime
	especiais.add(new Simbolo('‾', ' ', "&oline;","&#8254;")); //overline
	especiais.add(new Simbolo('⁄', '/', "&frasl;","&#8260;")); //fraction slash
		
	especiais.add(new Simbolo('℘', ' ', "&weierp;","&#8472;")); //script capital
	especiais.add(new Simbolo('ℑ', ' ', "&image;","&#8465;")); //blackletter capital I
	especiais.add(new Simbolo('ℜ', ' ', "&real;","&#8476;")); //blackletter capital R
	especiais.add(new Simbolo('™', ' ', "&trade;","&#8482;")); //trade mark sign
	especiais.add(new Simbolo('ℵ', ' ', "&alefsym;","&#8501;")); //alef symbol
	especiais.add(new Simbolo('←', ' ', "&larr;","&#8592;")); //Leftward arrow
	especiais.add(new Simbolo('↑', ' ', "&uarr;","&#8593;")); //upward arrow
	especiais.add(new Simbolo('→', ' ', "&rarr;","&#8594;")); //rightward arrow
	especiais.add(new Simbolo('↓', ' ', "&darr;","&#8595;")); //downward arrow
	especiais.add(new Simbolo('↔', ' ', "&harr;","&#8596;")); //Left right arrow
	especiais.add(new Simbolo('↵', ' ', "&crarr;","&#8629;")); //downward arrow with corner leftward
	especiais.add(new Simbolo('⇐', ' ', "&lArr;","&#8656;")); //Leftward double arrow
	especiais.add(new Simbolo('⇑', ' ', "&uArr;","&#8657;")); //upward double arrow
	especiais.add(new Simbolo('⇒', ' ', "&rArr;","&#8658;")); //rightward double arrow
	especiais.add(new Simbolo('⇓', ' ', "&dArr;","&#8659;")); //downward double arrow
	especiais.add(new Simbolo('⇔', ' ', "&hArr;","&#8660;")); //Left-right double arrow
	
	especiais.add(new Simbolo('∀', ' ', "&forall;","&#8704;")); //for all
	especiais.add(new Simbolo('∂', ' ', "&part;","&#8706;")); //partial differential
	especiais.add(new Simbolo('∃', ' ', "&exist;","&#8707;")); //there exists
	especiais.add(new Simbolo('∅', ' ', "&empty;","&#8709;")); //empty set
	especiais.add(new Simbolo('∇', ' ', "&nabla;","&#8711;")); //nabla
	especiais.add(new Simbolo('∈', ' ', "&isin;","&#8712;")); //element of
	especiais.add(new Simbolo('∉', ' ', "&notin;","&#8713;")); //not an element of
	especiais.add(new Simbolo('∋', ' ', "&ni;","&#8715;")); //contains as member
	especiais.add(new Simbolo('∏', ' ', "&prod;","&#8719;")); //n-ary product
	especiais.add(new Simbolo('∑', ' ', "&sum;","&#8721;")); //n-ary summation
	especiais.add(new Simbolo('−', '-', "&minus;","&#8722;")); //minus sign
	especiais.add(new Simbolo('∗', ' ', "&lowast;","&#8727;")); //asterisk operator
	especiais.add(new Simbolo('√', ' ', "&radic;","&#8730;")); //square root
	especiais.add(new Simbolo('∝', ' ', "&prop;","&#8733;")); //proportional to
	especiais.add(new Simbolo('∞', ' ', "&infin;","&#8734;")); //infinity
	especiais.add(new Simbolo('∠', ' ', "&ang;","&#8736;")); //angle
	especiais.add(new Simbolo('∧', ' ', "&and;","&#8743;")); //Logical and
	especiais.add(new Simbolo('∨', ' ', "&or;","&#8744;")); //Logical or
	especiais.add(new Simbolo('∩', ' ', "&cap;","&#8745;")); //intersection
	especiais.add(new Simbolo('∪', ' ', "&cup;","&#8746;")); //union
	especiais.add(new Simbolo('∫', ' ', "&int;","&#8747;")); //integral
	especiais.add(new Simbolo('∴', ' ', "&there4;","&#8756;")); //therefore
	especiais.add(new Simbolo('∼', ' ', "&sim;","&#8764;")); //tilde operator
	especiais.add(new Simbolo('≅', ' ', "&cong;","&#8773;")); //approximately equal to
	especiais.add(new Simbolo('≈', ' ', "&asymp;","&#8776;")); //almost equal to
	
	especiais.add(new Simbolo('≠', ' ', "&ne;","&#8800;")); //not equal to
	especiais.add(new Simbolo('≡', ' ', "&equiv;","&#8801;")); //identical to
	especiais.add(new Simbolo('≤', ' ', "&le;","&#8804;")); //Less-than or equal to
	especiais.add(new Simbolo('≥', ' ', "&ge;","&#8805;")); //Greater-than or equal to
	especiais.add(new Simbolo('⊂', ' ', "&sub;","&#8834;")); //subset of
	especiais.add(new Simbolo('⊃', ' ', "&sup;","&#8835;")); //superset of
	especiais.add(new Simbolo('⊄', ' ', "&nsub;","&#8836;")); //not a subset of
	//Note: &nsup; , &#8837; “not a superset of” is not defined.
	especiais.add(new Simbolo('⊆', ' ', "&sube;","&#8838;")); //subset of or equal to
	especiais.add(new Simbolo('⊇', ' ', "&supe;","&#8839;")); //superset of or equal to
	especiais.add(new Simbolo('⊕', ' ', "&oplus;","&#8853;")); //circled plus
	especiais.add(new Simbolo('⊗', ' ', "&otimes;","&#8855;")); //circled times
	especiais.add(new Simbolo('⊥', ' ', "&perp;","&#8869;")); //up tack
	especiais.add(new Simbolo('⋅', ' ', "&sdot;","&#8901;")); //dot operator
	especiais.add(new Simbolo('⌈', ' ', "&lceil;","&#8968;")); //Left ceiling
	especiais.add(new Simbolo('⌉', ' ', "&rceil;","&#8969;")); //right ceiling
	especiais.add(new Simbolo('⌊', ' ', "&lfloor;","&#8970;")); //Left floor
	especiais.add(new Simbolo('⌋', ' ', "&rfloor;","&#8971;")); //right floor
	especiais.add(new Simbolo('⟨', ' ', "&lang;","&#9001;")); //Left-pointing angle bracket
	especiais.add(new Simbolo('⟩', ' ', "&rang;","&#9002;")); //right-pointing angle bracket
	
	especiais.add(new Simbolo('─', '-', "&minus;","&#9472;")); //minus sign
	
	especiais.add(new Simbolo('◊', ' ', "&loz;","&#9674;")); //Lozenge
	
	especiais.add(new Simbolo('♀', 'F', "&#9792;","&#9792;"));
	especiais.add(new Simbolo('♂', 'M', "&#9794;","&#9794;"));
	
	especiais.add(new Simbolo('♠', ' ', "&spades;","&#9824;")); //black (solid) spade suit
	especiais.add(new Simbolo('♣', ' ', "&clubs;","&#9827;")); //black (solid) club suit
	especiais.add(new Simbolo('♥', ' ', "&hearts;","&#9829;")); //black (solid) heart suit
	especiais.add(new Simbolo('♦', ' ', "&diams;","&#9830;")); //black (solid) diamond suit
	
	especiais.add(new Simbolo('Є', ' ',"&#1028;","&#1028;")); 
	especiais.add(new Simbolo('±', ' ', "&plusmn;","&#177;")); 
	especiais.add(new Simbolo('±', ' ', "&plusmn;","&#61617;")); 

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
    			.replaceAll("\\^a","â").replaceAll("\\^e","ê").replaceAll("\\^o","ô").replaceAll("\\^E","Ê").replaceAll("\\^O","Ô")
    			.replaceAll("\\\"u","ü").replaceAll("¯","fi")
    			.replaceAll("\\¸c","ç").replaceAll("\\\'c","ç").replaceAll("»c","ç").replaceAll("\\´c","ç");
    }
    
    
    public static void main(String[] args){
           String s = "testando &aacute;gua para v&ecirc;r se d&aacute;&#32;certo.";
           s = "o ambiente din^amico exige processos de configura´c~ao ";
           System.out.println(s=corrigeAcentos(s));
           
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
           s = "Solução k&amp;#8722;Jato &#9794; e &#9792;  e de uma (©,k)&amp;#8722;Envoltória Convexa";
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