package br.ufpe.mtd.negocio.thread;

import java.util.Calendar;
import java.util.Date;
import java.util.TimerTask;

import br.ufpe.mtd.negocio.controle.MTDFacede;
import br.ufpe.mtd.util.MTDFactory;
import br.ufpe.mtd.util.MTDParametros;

/**
 * Classe realizara periodicamente a tarefa definida.
 * 
 * @author djalma
 * 
 */
public class MTDTask extends TimerTask {
	private final static long TEMPO_ESPERA = 60000l;// * 60 * 24;//60 s
	MTDFactory fabrica;
	
	public MTDTask(MTDFactory fabrica) {
		this.fabrica = fabrica;
	}
	
	
	
	@Override
	public void run() {
		boolean executar = deveExecutar();
		if(executar){
			executar();
		}else{
			fabrica.getLog().salvarDadosLog("MTDTask.run() Os crit�rios avaliados resultaram na n�o execu��o da Task de treinamento. Finalisando...");
		}
	}
	
	/**
	 * Avalia os criterios de execucao para confirmar se task deve 
	 * ser executada ou nao.
	 * Valores de WEB-INF/mtd_properties.properties
	 * @return
	 */
	private boolean deveExecutar(){
		boolean executar = false;
		Calendar cal = Calendar.getInstance();  
		cal.setTime(new Date());
		int day = cal.get(Calendar.DAY_OF_WEEK);  
		
		String diasTreino = MTDParametros.diasTreino();
		
		String[] dias = diasTreino.split(",");
		for (String string : dias) {
			try {
				if(Integer.parseInt(string) == day){
					executar = true;
					break;
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		try {
			
			if(!MTDFacede.isSistemaTreinado() || !MTDFacede.isSintagmasConcluidos()){
				executar = true;
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		return executar;
	}
	
	private void executar(){
		agendarIndexacao();
		fabrica.getTreinamentoPoolThread().aguardarFimDoPool();
		
		agendarTreino();
		fabrica.getTreinamentoPoolThread().aguardarFimDoPool();
		
		agendarAvaliacaoQualidadeTreino();
		fabrica.getTreinamentoPoolThread().aguardarFimDoPool();
		
		try {
			if(MTDFacede.isSistemaTreinado()){
				MTDFacede.resetarRepositorios();//carregando reposit�rios
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		try {
			if(MTDFacede.isSistemaTreinado() && !MTDFacede.isSintagmasConcluidos()){
				agendarGeracaoSintagmas();
				fabrica.getTreinamentoPoolThread().aguardarFimDoPool();
			}else{
				fabrica.getLog().salvarDadosLog("MTDTask.executar() Reagendando tarefas do sistema...");
				fabrica.agendarTarefas();
			}
		} catch (Exception e) {
			fabrica.getLog().salvarDadosLog(e);
		}
	}
	
	//TODO: agendar em properties data/hora da pr�xima execu��o (23h do dia seguinte), verificar
	//de hora em hora se � para executar (data/hora > data/hora properties e n�o executado)
	//Talvez utilizar http://www.quartz-scheduler.org/
	
	//TODO: Dado que o �ndice existe, uma verifica��o r�pida da n�o necessidade de indexar de novo
	//� verificar se a quantidade de documentos no reposit�rio e no �ndice � igual
	//Na coleta de indentificadores, primeira p�gina, voc� j� obtem esta informa
	//Obs: Levar em conta que esta estrategia pode n�o funcionar pra outros repositorios.
	//avaliar se o ganho seria relevante em detrimento da confiabilidade.
	void agendarIndexacao(){
		new BaseThread(){
			public void execucao() {
				fabrica.getLog().salvarDadosLog("MTDTask.agendarIndexacao() Task indexa��o iniciando em 60 segundos!!!");
				try {
					Thread.sleep(TEMPO_ESPERA);
					MTDFacede.indexar();
				} catch (Exception e) {
					fabrica.getLog().salvarDadosLog(e);
				}
				fabrica.getLog().salvarDadosLog("Task indexa��o finalisada!!!");
			};
			
		//precisa executar em outro pool, pois ela vai aguardar o fim do pool que esta indexando. 
		// do contrario fica em dead lock
		//esperando o fim do proprio pool que nunca termina por conta dela mesma.
		}.executarNoPool(fabrica.getTreinamentoPoolThread());
	}

	//TODO: iniciar treinamento se arquivos de sa�da do treinamento n�o existem e mapa serializado tamb�m n�o.
	private void agendarTreino() {
		new BaseThread(){
			public void execucao() {
				fabrica.getLog().salvarDadosLog("MTDTask.agendarTreino() Task treinamento iniciando em 60 segundos!!!");
				try {
					Thread.sleep(TEMPO_ESPERA);
					MTDFacede.realizarTreinamento();
				} catch (Exception e) {
					fabrica.getLog().salvarDadosLog(e);
				}
				fabrica.getLog().salvarDadosLog("Task treinamento finalisada!!!");
			};
		}.executarNoPool(fabrica.getTreinamentoPoolThread());
	}
	
	//TODO: iniciar treinamento se arquivos de sa�da do treinamento n�o existem e mapa serializado tamb�m n�o.
		private void agendarAvaliacaoQualidadeTreino() {
			new BaseThread(){
				public void execucao() {
					fabrica.getLog().salvarDadosLog("MTDTask.agendarAvaliacaoQualidadeTreino() Task avalia��o de qualidade iniciando em 60 segundos!!!");
					try {
						Thread.sleep(TEMPO_ESPERA);
						MTDFacede.gerarMedidasQualidadeRedeNeural();
					} catch (Exception e) {
						fabrica.getLog().salvarDadosLog(e);
					}
					fabrica.getLog().salvarDadosLog("Task avalia��o de qualidade finalisada!!!");
				};
			}.executarNoPool(fabrica.getTreinamentoPoolThread());
		}	

	//TODO: Dado que existe o indice com sintagma, uma forma de verificar a n�o necessidade de gerar sintagmas e se
	//os dois indices contem o mesmo n�mero de documentos.
	//Obs: Verificar baseado apenas na qtd de docs � uma forma muito alto nivel, que tras riscos para
	//a confiabilidade dos dados com pouco ganho de desempenho.
	private void agendarGeracaoSintagmas() {
		new BaseThread(){
			public void execucao() {
				fabrica.getLog().salvarDadosLog("MTDTask.agendarGeracaoSintagmas() Task gera��o sintagmas iniciando em 60 segundos!!!");
				try {
					while(!MTDFacede.isSintagmasConcluidos()){
					Thread.sleep(TEMPO_ESPERA);
					MTDFacede.salvarDadosIndiceSintagmas();
					}
					
				} catch (Exception e) {
					fabrica.getLog().salvarDadosLog(e);
				}
				fabrica.getLog().salvarDadosLog("Task gera��o sintagmas finalisada!!!");
			};
		}.executarNoPool(fabrica.getTreinamentoPoolThread());
	}
}