package br.ufpe.mtd.util;

/**
 * Classe que permite navegar sobre colecoes
 * de objetos ou colecoes de dados.
 * 
 * @author djalma
 *
 * @param <T>
 */
public abstract class MTDIterator<T> {

	public MTDIterator() throws Exception{
		init();
	}
	
	public abstract void init() throws Exception;
	public abstract boolean hasNext() throws Exception;
	public abstract T next() throws Exception;
	public abstract void close() throws Exception;
	
}
