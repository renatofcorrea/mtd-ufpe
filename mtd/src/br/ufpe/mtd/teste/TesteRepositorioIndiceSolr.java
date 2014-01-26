package br.ufpe.mtd.teste;

import br.ufpe.mtd.dados.IRepositorioIndice;
import br.ufpe.mtd.negocio.ControleIndice;
import br.ufpe.mtd.thread.MTDThreadPool;
import br.ufpe.mtd.util.MTDFactory;
import br.ufpe.mtd.util.MTDUtil;

/*
	http://www.ibm.com/developerworks/br/java/library/os-apache-lucenesearch/

	http://www.lucenetutorial.com/lucene-in-5-minutes.html
	
	http://faladede.opportunitas.com.br/lucene-facil-indexando-conteudo-forma-simples-direta/
	
	http://vitorpamplona.com/wiki/Introdu%C3%A7%C3%A3o%20ao%20Apache%20Lucene
	
 */
public class TesteRepositorioIndiceSolr {

	public static void main(String [] args){
		
		indexar();
	}

	static void indexar(){
		
		long inicio = System.currentTimeMillis();
		IRepositorioIndice rep = null;
		MTDThreadPool logPool = MTDFactory.getInstancia().getLogPoolThread();
		MTDThreadPool pool = MTDFactory.getInstancia().getPoolThread();
		
		try {
			// precisa setar a propriedade do arquivo de properties da aplicacao para slr_usar = true
			rep = MTDFactory.getInstancia().getSingleRepositorioIndice();
			ControleIndice controle = new ControleIndice(rep);
			controle.indexar("http://tede.pucrs.br/tde_oai/oai3.php", "mtd2-br");
			
			logPool.aguardarFimDoPool();
			
			if(rep != null){
				rep.fecharRepositorio();
			}
			
		} catch (Exception e) {
			//enviar meail de alert para equipe tecnica se servico der excecao
			e.printStackTrace();
			
		} finally{
			
			logPool.fecharPool();
			pool.fecharPool();
			
			MTDUtil.imprimirConsole("Tempo total : "+ (System.currentTimeMillis() - inicio));
		}
	}
}
