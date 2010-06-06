/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package servidor;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import mtd.handler.PalavrasNodo;

/**
 *
 * @author Bruno
 */
public class ArquivosUtil {

    public static HashMap<Integer, Vector<String>> nodoDocumento = null;
    public static HashMap<Integer, PalavrasNodo> hashNodoPalavras = null;
    public static Vector<String> palavras = new Vector<String>();

    static {
        FileReader fileReader;
        try {
            fileReader = new FileReader("C:\\Documents and Settings\\Bruno\\Meus documentos\\NetBeansProjects\\MTDWeb\\web\\arquivos\\fword.csv");

            BufferedReader leitor = new BufferedReader(fileReader);
            String[] valores;
            String string;
            String a = null;
            int i = 0;
            do {
                palavras.add(a);
                string = leitor.readLine();
                if (string != null) {
                    a = string.trim();
                    valores = a.split(" ");
                    a = valores[valores.length - 1];
                } else {
                    a = string;
                }

            } while (a != null);


            fileReader = new FileReader("C:\\Documents and Settings\\Bruno\\Meus documentos\\NetBeansProjects\\MTDWeb\\web\\arquivos\\docfnodedoc.csv");
            leitor = new BufferedReader(fileReader);

            nodoDocumento = new HashMap<Integer, Vector<String>>();
            Vector<String> vetor;
            String idDocumento;
            do {

                string = leitor.readLine();
                if (string != null) {
                    a = string.trim();
                    valores = a.split(" ");
                    int idNodo = Integer.parseInt(valores[0]);
                    vetor = nodoDocumento.get(idNodo);
                    if (vetor == null) {
                        vetor = new Vector<String>();
                    }
                    idDocumento = valores[valores.length - 1];
                    vetor.add(idDocumento);
                    nodoDocumento.put(idNodo, vetor);
                } else {
                    a = string;
                }

            } while (a != null);


            fileReader = new FileReader("C:\\Documents and Settings\\Bruno\\Meus documentos\\NetBeansProjects\\MTDWeb\\web\\arquivos\\docfcodebook.csv");
            leitor = new BufferedReader(fileReader);

            hashNodoPalavras = new HashMap<Integer, PalavrasNodo>();

            StringTokenizer stringT;
            int idNodo;
            int idVirtualPalavra;
            float peso;
            PalavrasNodo palavrasNodo;
            int segundo;
            int terceiro;
            float pesoSegundo;
            float pesoTerceiro;

            do {

                string = leitor.readLine();
                System.out.println();
                if (string != null) {
                    a = string.trim();                   

                    stringT = new StringTokenizer(a, " ");

                    idNodo = Integer.parseInt(stringT.nextToken());
                    idVirtualPalavra = Integer.parseInt(stringT.nextToken());
                    peso = Float.parseFloat(stringT.nextToken());
                    
                    palavrasNodo = hashNodoPalavras.get(idNodo);
                    if (palavrasNodo == null) {
                        palavrasNodo = new PalavrasNodo();
                    }
                   

                    if (peso >= palavrasNodo.getPesoMaior()) {
                        segundo = palavrasNodo.getMaior();
                        terceiro = palavrasNodo.getMeio();
                        pesoSegundo = palavrasNodo.getPesoMaior();
                        pesoTerceiro = palavrasNodo.getPesoMedio();
                        palavrasNodo.setMaior(idVirtualPalavra);
                        palavrasNodo.setMeio(segundo);
                        palavrasNodo.setMenor(terceiro);
                        palavrasNodo.setPesoMaior(peso);
                        palavrasNodo.setPesoMedio(pesoSegundo);
                        palavrasNodo.setPesoMenor(pesoTerceiro);
                    } else if (peso >= palavrasNodo.getPesoMedio()) {
                        terceiro = palavrasNodo.getMeio();
                        pesoTerceiro = palavrasNodo.getPesoMedio();
                        palavrasNodo.setMeio(idVirtualPalavra);
                        palavrasNodo.setPesoMedio(peso);
                        palavrasNodo.setMenor(terceiro);
                        palavrasNodo.setPesoMenor(pesoTerceiro);
                    } else if (peso >= palavrasNodo.getPesoMenor()) {
                        palavrasNodo.setMenor(idVirtualPalavra);
                        palavrasNodo.setPesoMenor(peso);
                    }

                    hashNodoPalavras.put(idNodo, palavrasNodo);

                    System.out.println("nodo: " + idNodo);

                } else {
                    a = string;
                }

            } while (a != null);


        } catch (IOException ex) {
            Logger.getLogger(ArquivosUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
