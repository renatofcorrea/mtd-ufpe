/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package br.ufpe.mtd.entidade;

import java.io.Serializable;

/**
 *
 * @author Bruno
 */
public class PalavrasNodo implements Serializable {

    int maior;
    int meio;
    int menor;
    float pesoMaior;
    float pesoMedio;
    float pesoMenor;

    public float getPesoMaior() {
        return pesoMaior;
    }

    public void setPesoMaior(float pesoMaior) {
        this.pesoMaior = pesoMaior;
    }

    public float getPesoMedio() {
        return pesoMedio;
    }

    public void setPesoMedio(float pesoMedio) {
        this.pesoMedio = pesoMedio;
    }

    public float getPesoMenor() {
        return pesoMenor;
    }

    public void setPesoMenor(float pesoMenor) {
        this.pesoMenor = pesoMenor;
    }
    
    public int getMaior() {
        return maior;
    }

    public void setMaior(int maior) {
        this.maior = maior;
    }

    public int getMeio() {
        return meio;
    }

    public void setMeio(int meio) {
        this.meio = meio;
    }

    public int getMenor() {
        return menor;
    }

    public void setMenor(int menor) {
        this.menor = menor;
    }


}
