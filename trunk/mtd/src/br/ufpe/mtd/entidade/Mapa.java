/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufpe.mtd.entidade;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

/**
 *
 * @author Junior
 */
public class Mapa implements Serializable{

    private HashMap<Integer, Vector<String>> nodoDocumento = null;
    private HashMap<Integer, PalavrasNodo> hashNodoPalavras = null;
    private Vector<String> palavras = new Vector<String>();
    private HashMap<String, Integer> documentoNodo = null;
    private Vector<String> areas = new Vector<String>();
    private ArrayList<Integer> idsDocsPesquisa;
    private static final long serialVersionUID = 501;  

    public Mapa() {
    }

    public HashMap<Integer, Vector<String>> getNodoDocumento() {
        return nodoDocumento;
    }

    public void setNodoDocumento(HashMap<Integer, Vector<String>> nodoDocumento) {
        this.nodoDocumento = nodoDocumento;
    }

    public HashMap<Integer, PalavrasNodo> getHashNodoPalavras() {
        return hashNodoPalavras;
    }

    public void setHashNodoPalavras(HashMap<Integer, PalavrasNodo> hashNodoPalavras) {
        this.hashNodoPalavras = hashNodoPalavras;
    }

    public Vector<String> getPalavras() {
        return palavras;
    }

    public void setPalavras(Vector<String> palavras) {
        this.palavras = palavras;
    }

    public HashMap<String, Integer> getDocumentoNodo() {
        return documentoNodo;
    }

    public void setDocumentoNodo(HashMap<String, Integer> documentoNodo) {
        this.documentoNodo = documentoNodo;
    }

    public Vector<String> getAreas() {
        return areas;
    }

    public void setAreas(Vector<String> areas) {
        this.areas = areas;
    }

    public ArrayList<Integer> getIdsDocsPesquisa() {
        return idsDocsPesquisa;
    }

    public void setIdsDocsPesquisa(ArrayList<Integer> idsDocsPesquisa) {
        this.idsDocsPesquisa = idsDocsPesquisa;
    }
    
    
    
}
