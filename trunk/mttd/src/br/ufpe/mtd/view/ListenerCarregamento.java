package br.ufpe.mtd.view;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import br.ufpe.mtd.util.MTDFactory;
import br.ufpe.mtd.util.MTDParametros;

/**
 * Classe que e chamada na inicializacao e fechamento do sistema.
 * 
 * Serve para carregar os recursos necessarios e encerrar recursos.
 * 
 * Deve ser informada como listener do sistema no web.config
 * 
 * @author djalma
 *
 */
public class ListenerCarregamento implements ServletContextListener{

	
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		MTDFactory.getInstancia().fechar();
		System.out.println("Sistema destruido!!!");
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		System.out.println("Contruindo sistema");
		System.out.println("Sistema Operacional : "+System.getProperty("os.name"));
		
		String propriedade = MTDParametros.getPastaWeb();
		if(propriedade == null){
			MTDParametros.setPastaWeb(arg0.getServletContext().getRealPath("/WEB-INF"));
			System.out.println(arg0.getServletContext().getRealPath("/WEB-INF"));
			System.out.println("Ambiente : "+MTDParametros.getTipoAmbiente());
		}
		MTDFactory.getInstancia();//
	}

}
