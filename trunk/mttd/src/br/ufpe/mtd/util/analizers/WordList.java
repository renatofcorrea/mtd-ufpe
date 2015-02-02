package br.ufpe.mtd.util.analizers;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import br.ufpe.mtd.util.MTDFactory;
import br.ufpe.mtd.util.enumerado.MTDArquivoEnum;

public class WordList {
	private Vector<String> palavras = null;
	private Vector<String> etiquetas = null;
	
	
	public WordList(String fin){
		palavras = new Vector<String>();
		etiquetas = new Vector<String>();
		loadCVS(fin);
		
	}
	
	//quantas vezes a string seq ocorre dentro de frase
    //usada para quantificar quantas vezes uma etiqueta aparece na frase etiquetada
    //Usado por tabelaSNR e tabelaSNRA
	static public int howManyStringsIn(String seq, String frase)
		{
			int num = 0;
			while (frase.contains(seq))
			{
				int index = frase.indexOf(seq);
				int inextchar = index+seq.length();
				inextchar = inextchar < frase.length()?inextchar:-1;
				if(inextchar < 0 || !Character.isAlphabetic(frase.charAt(inextchar)))
					num++;
				frase = frase.substring( index + seq.length());//+2
				
			}
			return num;
		}

	private void writeCVS(String fout) {

		String encoding = "ISO-8859-1"; //"UTF-8"
		//FileReader f = new FileReader(fin);
		//encoding = f.getEncoding();
		BufferedWriter tr;
		try {
			tr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fout,false), encoding));

			//BufferedReader tr = new BufferedReader (new FileReader(fin)); //Encoding.GetEncoding("ISO-8859-1")
			String text = null;
			String [] entradas = null; 
			tr.write("\"palavra\",\"tipo\"\n"); // cabeçalho
			for(int i=0; i < palavras.size();i++)
			{
				tr.write("\""+palavras.elementAt(i)+"\",\""+etiquetas.elementAt(i)+"\"\n");
			}
			tr.close();
		} catch (UnsupportedEncodingException | FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	//despreza a primeira linha de cabecalho, carrega valores em duas colunas separadas por vírgula
	private void loadCVS(String fin) {
		try{
			System.out.println("WordList loading "+fin);
			String encoding = "ISO-8859-1"; //"UTF-8"
		    //FileReader f = new FileReader(fin);
		    //encoding = f.getEncoding();
		    BufferedReader tr = new BufferedReader( new InputStreamReader(new FileInputStream(fin), encoding));
		//BufferedReader tr = new BufferedReader (new FileReader(fin)); //Encoding.GetEncoding("ISO-8859-1")
		String text = null;
		String [] entradas = null; 
		tr.readLine(); // desprezando cabeçalho
		int lines=0;
		while ((text = tr.readLine()) != null)
		{
			lines++;
			//char[] delimiterChars = {' ', ',', '.', ':','\t','!','?','/','<','>','(',')'};
			String delimiterChars = ",";
			text = text.replace("\"", "");
			//text = text.toLowerCase();
			entradas= text.split(delimiterChars);
			
			if(entradas.length == 2){
				//int i = palavras.indexOf(entradas[0]);
				//search an element using binarySearch method of Collections class.    
				int i = Collections.binarySearch(palavras,entradas[0]);
				if(i >= 0){
					if(!etiquetas.elementAt(i).contains(entradas[1])){
						etiquetas.setElementAt(etiquetas.elementAt(i)+entradas[1],i);
						//System.out.println("Adicionando etiqueta "+entradas[1]+" à palavra "+palavras.elementAt(i));
					}
					else{
						//System.out.println("Etiqueta "+entradas[1]+" já encontrada para palavra "+palavras.elementAt(i));
					}
				}
				else{
//					 Add the non-existent item to the vectors
					if (i < 0) {
					    palavras.add(-i-1, entradas[0]);
					    etiquetas.add(-i-1, entradas[1]);
					    }
				}
			}
			else{
				System.out.println("Erro: Arquivo " + fin +" com mais de duas colunas");
			}
		}
		System.out.println("WordList lines read "+lines);
		tr.close();
		}catch(IOException e){
			System.out.println(e.getMessage());
		}
		
	}
	 public String BuscaPalavra(String palavra) {
			int i = Collections.binarySearch(palavras,palavra);
			if(i >=0)
				return etiquetas.elementAt(i);
			else
				return "";
		}
	 
	 //Retorna uma string contendo o conteúdo de um arquivo
	 public static String readFile( String file ) throws IOException {
		    String encoding = "ISO-8859-1"; //"UTF-8"
		    //FileReader f = new FileReader(file);
		    //encoding = f.getEncoding();
		    BufferedReader reader = new BufferedReader( new InputStreamReader(new FileInputStream(file), encoding));
		    String         line = null;
		    StringBuilder  stringBuilder = new StringBuilder();
		    String         ls = System.getProperty("line.separator");

		    while( ( line = reader.readLine() ) != null ) {
		        stringBuilder.append( line );
		        stringBuilder.append( ls );
		    }
		    reader.close();

		    return stringBuilder.toString();
		}
	 
	//Retorna o conteudo do arquivo em string
	//Consome tres vezes o tamanho do arquivo em memória
	 public static String readFile(String path, Charset encoding) 
			  throws IOException 
			{
		      if(encoding == null)
		    	  encoding = Charset.defaultCharset();
			  byte[] encoded = Files.readAllBytes(Paths.get(path));
			  return encoding.decode(ByteBuffer.wrap(encoded)).toString();
			}
	 
	 static List<String> readLinesFromFile(String path){
		 Charset encoding = Charset.defaultCharset();
		 List<String> lines= null;
		try {
			lines = Files.readAllLines(Paths.get(path), encoding);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		 return lines;
	 }
	 
	 public static void printString(String filename, String content) throws FileNotFoundException{
		 PrintWriter out = new PrintWriter(filename);
		 out.print(content);
		 out.close();
		 return;
	 }
	 
	 public static void main(String[] args){
		 
//		 String stopFile = MTDArquivoEnum.PASTA_ARQUIVOS_AUXILIARES.getArquivo().getAbsolutePath()+"\\JOgma\\sn_stoplist.txt";
		 
//		 WordList gramatica = new WordList(MTDArquivoEnum.J_OGMA_GRAMATICA.getArquivo().getAbsolutePath());
//		 WordList nomes = new WordList(MTDArquivoEnum.J_OGMA_NOMES.getArquivo().getAbsolutePath());
//		 WordList verbos = new WordList(MTDArquivoEnum.J_OGMA_VERBOS.getArquivo().getAbsolutePath());
		 WordList gramatica = new WordList("WebContent/WEB-INF/aux_files/JOgma/Ogma-GRAMATICA-sort.csv");
		 WordList nomes = new WordList("WebContent/WEB-INF/aux_files/JOgma/Ogma-NOMES-sort.csv");
		 WordList verbos = new WordList("WebContent/WEB-INF/aux_files/JOgma/Ogma-VERBOS-sort.csv");
		 String stopFile = "WebContent/WEB-INF/aux_files/"+"JOgma/sn_stoplist.txt";
		 WordList stp = new WordList(stopFile);//vai dar erro, esperado duas colunas
//		 WordList stp = new WordList(MTDArquivoEnum.J_OGMA_STOP_LIST.getArquivo().getAbsolutePath());	
		 System.out.println(verbos.BuscaPalavra("há"));
		 System.out.println(verbos.BuscaPalavra("é"));
		 System.out.println(verbos.BuscaPalavra("consiste"));
		 //gramatica.writeCVS("WebContent/WEB-INF/aux_files/JOgma/Ogma-GRAMATICA-sort2.csv");
		 //nomes.writeCVS("WebContent/WEB-INF/aux_files/JOgma/Ogma-NOMES-sort2.csv");
		 
			
	 }
}
