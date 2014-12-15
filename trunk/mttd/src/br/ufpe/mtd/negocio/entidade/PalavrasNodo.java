package br.ufpe.mtd.negocio.entidade;
import java.io.Serializable;

/**
 *	Representa o conjunto das palavras que representam
 *  um nodo. sao as N palavras mais importantes ou mais representativas 
 *  de um nodo. Com sua ordem de relevancia. E pesos que foram calculados
 *  para elas;
 *
 * @author Djalma
 */
public class PalavrasNodo implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
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
