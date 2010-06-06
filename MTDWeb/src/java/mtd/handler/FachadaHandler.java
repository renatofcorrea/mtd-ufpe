package mtd.handler;

import java.util.Iterator;
import java.util.Vector;
import servidor.ArquivosUtil;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Bruno
 */
public class FachadaHandler {

    private String nome = "<a href=\"http://www.globo.com\" >BRuno</a>";
    private String[][] matriz = new String[10][10];
    private String mapaRandomico;
    private String listaDocumentos;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getMapaRandomico() {
        if (mapaRandomico == null) {
            this.carregarMapaRandomico();
        }
        return this.mapaRandomico;
    }

    public void carregarMapaRandomico() {
        

        String retorno = "<table border=\"1\" width=\"300px\">";


        Iterator<Integer> idsNodos = ArquivosUtil.hashNodoPalavras.keySet().iterator();
        

        for (int i = 0; i < 10; i++) {
            retorno = retorno + "<tr>";
            for (int j = 0; j < 12; j++) {
                int nodo = idsNodos.next();
                PalavrasNodo palavrasPrincipais = ArquivosUtil.hashNodoPalavras.get(nodo);
                retorno = retorno + "<td align=\"center\"><a href=\"ListaDocumentos.jsp?id="+ nodo +"\">" + ArquivosUtil.palavras.get(palavrasPrincipais.getMaior())+"<br>"+ ArquivosUtil.palavras.get(palavrasPrincipais.getMeio()) +"<br>"+ ArquivosUtil.palavras.get(palavrasPrincipais.getMenor()) + "</a></td>";
            }
            retorno = retorno + "</tr><tr>";
        }
        retorno = retorno + "</tr></table>";
        this.mapaRandomico = retorno;
    }

     public String getListaDocumentos(int idNodo) {
            this.carregarListaDocumentos(idNodo);
        
        return this.listaDocumentos;
    }

    public void carregarListaDocumentos(int idNodo){
        Vector<String> documentos = ArquivosUtil.nodoDocumento.get(idNodo);

        String retorno = "";

        for(int i=0; i<documentos.size(); i++){
            retorno =retorno + "<a href=\"http://www.bdtd.ufpe.br/tedeSimplificado/tde_oai/oai3.php?verb=GetRecord&metadataPrefix=mtd2-br&identifier=oai:bdtd.ufpe.br:" +  documentos.elementAt(i) +"\">"+documentos.elementAt(i)+"</a>"+"<br/>";
        }

        this.listaDocumentos = retorno;

    }

    public String mudarImagem() {
        this.mapaRandomico = "Bruno";
        return "mudarImagem";
    }
}
