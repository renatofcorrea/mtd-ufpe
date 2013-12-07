package br.ufpe.mtd.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

//TODO: Melhorar a classe de log para verificar qdo o arquivo esta aberto e so qdo estiver fechado tentar abri-lo
public class Log {

	private File logAplicDir;
	private File logAplicDados;
	private File logAplicExcecao;

	public Log() {
		logAplicDir = new File(MTDParametros.getExternalStorageDirectory(),
				"log");
		logAplicDados = new File(logAplicDir, "log_dados.txt");
		logAplicExcecao = new File(logAplicDir, "log_excecao.txt");
	}

	/**
	 * Salva as excecoes no log da aplicacao.
	 * 
	 * @param excecao
	 */
	public synchronized void salvarDadosLog(final Exception excecao) {
		if (MTDParametros.getTipoAmbiente().isDesenvovimento()) {
			excecao.printStackTrace();
		}

		try {

			logAplicDir.mkdirs();
			if (logAplicDir.exists()) {
				logAplicExcecao.createNewFile();
				if (logAplicExcecao.exists()) {

					
					MTDFactory.getInstancia().getLogPoolThread()
					.execute(new Runnable() {

						@Override
						public void run() {
							FileOutputStream fos = null;
							PrintStream printStream = null;
							try {
								
							int tentativas = 0;	
							while(fos == null && tentativas < 10){
								try {
									tentativas ++;
									fos = new FileOutputStream(logAplicExcecao, true);
									
								} catch (Exception e) {
									System.out.println(Thread.currentThread().getName()+"Tentado criar stream arquivo tentativa "+ tentativas);
									try {
										Thread.sleep(100);
									} catch (InterruptedException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									}
								}
							}
							if(fos == null){
								System.out.println("Nao conseguiu criar");
								return;
							}
							
							System.out.println(Thread.currentThread().getName()+" Criado "+ tentativas);
							printStream = new PrintStream(fos);

							String cabecalho = "---------- Excecao ---------: ";
							cabecalho += new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss")
									.format(new Date());

								fos.write(cabecalho.getBytes());
								fos.write('\n');
								excecao.printStackTrace(printStream);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} finally {// trata a excecao fora mas fecha a stream
										// sempre
								try {
									if(fos != null){
										fos.close();
									}
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								if(printStream!= null){
									printStream.close();
								}
							}
						}
					});
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Salva as excecoes no log da aplicacao.
	 * 
	 * @param excecao
	 */
	public void salvarDadosLog(final String dado) {
		if (MTDParametros.getTipoAmbiente().isDesenvovimento()) {
			MTDUtil.imprimirConsole(dado);
		}

		try {
			logAplicDir.mkdirs();
			if (logAplicDir.exists()) {
				logAplicDados.createNewFile();
				if (logAplicDados.exists()) {
					final String cabecalho = "---------- Dado ---------: "
							+ new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss")
									.format(new Date());

					MTDFactory.getInstancia().getLogPoolThread()
							.execute(new Runnable() {

								@Override
								public void run() {
									FileOutputStream fos = null;
									
									int tentativas = 0;	
									while(fos == null && tentativas < 10){
										try {
											tentativas ++;
											fos = new FileOutputStream(logAplicDados, true);
											
										} catch (Exception e) {
											System.out.println(Thread.currentThread().getName()+"Tentado criar stream arquivo tentativa "+ tentativas);
											try {
												Thread.sleep(100);
											} catch (InterruptedException e1) {
												// TODO Auto-generated catch block
												e1.printStackTrace();
											}
										}
									}
									
									if(fos == null){
										System.out.println("Nao conseguiu criar");
										return;
									}
									
									System.out.println(Thread.currentThread().getName()+" Criado "+ tentativas);
									
									try {
										fos.write(cabecalho.getBytes());
										fos.write('\n');
										fos.write(dado.getBytes());
										fos.write('\n');

									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									} finally {// trata a excecao fora mas fecha
												// a stream sempre
										if (fos != null) {
											try {
												fos.close();
											} catch (IOException e) {
												// TODO Auto-generated catch
												// block
												e.printStackTrace();
											}
										}
									}

								}
							});

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

		}
	}
}
