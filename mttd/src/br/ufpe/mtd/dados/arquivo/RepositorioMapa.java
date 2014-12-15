package br.ufpe.mtd.dados.arquivo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import br.ufpe.mtd.negocio.entidade.Mapa;
import br.ufpe.mtd.util.enumerado.MTDArquivoEnum;

/**
 * Classe que prove acesso ao objeto serializado mapa.
 * Faz a persistencia do mesmo no sistema e recupera suas informações
 * a partir de sua copia salva no sistema de arquivos.
 * @author djalma
 *
 */
public class RepositorioMapa {
	
	private Mapa mapa;
	
	
	public RepositorioMapa() throws Exception {
		carregarMapa();
	}
	
	public synchronized void salvaMapa(Mapa mapa) throws IOException{
		FileOutputStream f_out = new FileOutputStream(MTDArquivoEnum.TREINO_MAPA_DATA.getArquivo());
		ObjectOutputStream obj_out = new ObjectOutputStream(f_out);
		obj_out.writeObject(mapa);
		obj_out.close();
	}
	
	public synchronized Mapa getMapa()throws Exception{
		return mapa;
	}
	
	public synchronized  void carregarMapa() throws Exception{
		MTDArquivoEnum treinoMapa = MTDArquivoEnum.TREINO_MAPA_DATA;
		File fileMapa = MTDArquivoEnum.TREINO_MAPA_DATA.getArquivo();
		if (fileMapa.length() == 0) {
			FileOutputStream fos = treinoMapa.getFileOutputStream(false);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(new Mapa());
			oos.close();
			fos.close();
		}
		
		FileInputStream fis = treinoMapa.getFileInputStream();
		ObjectInputStream objIs = new ObjectInputStream(fis);
		mapa = (Mapa) objIs.readObject();
		
		objIs.close();
	}
}
