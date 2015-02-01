package br.ufpe.mtd.teste;

import java.awt.Frame;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Collection;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import br.ufpe.mtd.negocio.controle.SuggesterControle;
import br.ufpe.mtd.util.MTDFactory;

public class TesteAutoSuggest {

	public static void main(String[] args) {
		try {
			
			SuggesterControle suggester = new SuggesterControle(SuggesterControle.getSugestaoPadrao(), MTDFactory.getInstancia());
			
			new TelaTeste(suggester);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	static class TelaTeste extends JFrame {
		SuggesterControle suggester;
		private static final long serialVersionUID = 1L;

		public TelaTeste(SuggesterControle suggester) {
			this.suggester = suggester;
			init();
		}

		private void init() {
			setBounds(0, 0, 1200, 600);
			setDefaultCloseOperation(EXIT_ON_CLOSE);
			setState(Frame.MAXIMIZED_BOTH);
			
			JPanel painel = new JPanel();
			JTextField campo = new JTextField(30);

			JTextArea jta = new JTextArea(30, 30);
			jta.setEditable(false);

			campo.addKeyListener(new MyKeyListener(jta, suggester));

			painel.add(campo);
			painel.add(jta);
			add(painel);

			setLocationRelativeTo(null);
			setVisible(true);
			campo.requestFocus();
		}
	}

	static class MyKeyListener implements KeyListener {
		private JTextArea jta;
		private SuggesterControle suggester;

		public MyKeyListener(JTextArea jta, SuggesterControle suggester) {
			this.suggester = suggester;
			this.jta = jta;
		}

		@Override
		public void keyTyped(KeyEvent e) {

		}

		@Override
		public void keyReleased(KeyEvent e) {
			
			if (e.getSource() instanceof JTextField) {
				JTextField campo = (JTextField) e.getSource();
				String texto = campo.getText();
				jta.setText("");
				
				try {
					StringBuilder stb = new StringBuilder();
					Collection<String> palavras = suggester.lookup(texto);
					for (String string : palavras) {
						stb.append(string + "\n");
					}
					jta.setText(stb.toString());
					
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}

			System.out.println(e.getKeyCode());
		}

		@Override
		public void keyPressed(KeyEvent e) {

		}
	};
}
