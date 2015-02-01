package br.ufpe.mtd.teste;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import br.ufpe.mtd.negocio.controle.MTDFacede;

public class TesteMTTD {

	public static void main(String[] args) {

		final JFrame tela = new JFrame("MTTD - UFPE");
		tela.setBounds(0, 0, 300, 300);
		tela.setLocationRelativeTo(null);
		tela.setResizable(false);
		tela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		tela.setLayout(new BorderLayout());
		
		JPanel painelSuperior = new JPanel();
		JPanel painelCentro = new JPanel();
		final JCheckBox cbIndex = new JCheckBox("Indexar");
		final JCheckBox cbTreinar = new JCheckBox("Treinar");
		final JCheckBox cbGerarSintagmas = new JCheckBox("Gerar Sintagmas");
		final JProgressBar barraStatus = new JProgressBar(0, 100);
		final JButton btExecutar = new JButton("Executar");


		barraStatus.setValue(0);
		barraStatus.setStringPainted(true);
		
		btExecutar.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				btExecutar.setEnabled(false);
				new Thread() {
					public void run() {
						try {
							if (cbIndex.isSelected()) {
								MTDFacede.indexar();
							}
							barraStatus.setValue(33);

							if (cbTreinar.isSelected()) {
								System.out.println("Aguardando para iniciar o treinamento...");
								Thread.sleep(5000);
								MTDFacede.realizarTreinamento();
							}
							barraStatus.setValue(66);

							
							if (cbGerarSintagmas.isSelected()) {
								System.out.println("Aguardando para iniciar o geração de sintagmas...");
								Thread.sleep(5000);
								MTDFacede.salvarDadosIndiceSintagmas();
							}
							barraStatus.setValue(100);
							
							JOptionPane.showMessageDialog(tela, "Concluido!!!");
							
						} catch (Exception e) {
							e.printStackTrace();
						}finally{
							tela.dispose();
							System.exit(0);
						}
					};
				}.start();;
			}
		});

		painelSuperior.add(cbIndex);
		painelSuperior.add(cbTreinar);
		painelSuperior.add(cbGerarSintagmas);
		
		btExecutar.setAlignmentX(Component.CENTER_ALIGNMENT); 
		painelCentro.setBorder(BorderFactory.createLineBorder(Color.black, 1, false));
		painelCentro.setLayout(new BoxLayout(painelCentro, BoxLayout.PAGE_AXIS)); 
		painelCentro.add(Box.createVerticalGlue()); 
		painelCentro.add(btExecutar);
		painelCentro.add(Box.createVerticalGlue()); 

		
		tela.add(painelSuperior, BorderLayout.NORTH);
		tela.add(painelCentro, BorderLayout.CENTER);
		tela.add(barraStatus,BorderLayout.SOUTH);
		tela.setVisible(true);
	}
}
