package mtd.handler;

import java.util.Map;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

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

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
    
    public String getMapaRandomico(){
        if(mapaRandomico==null){
            this.carregarMapaRandomico();
        }
       return this.mapaRandomico;
    }
    
    public void carregarMapaRandomico(){
         String retorno = "<table border=\"1\" width=\"300px\">";

        for(int i=0;i<10;i++){
            retorno = retorno+"<tr>";
            for(int j=0;j<10;j++){
                retorno = retorno + "<td align=\"center\">" + Math.random() + "</td>";
            }
            retorno = retorno + "</tr><tr>";
        }
        retorno = retorno +"</tr></table>";
        this.mapaRandomico = retorno;
    }

    public String mudarImagem(){
        this.mapaRandomico = "Bruno";
        return "mudarImagem";
    }


}
