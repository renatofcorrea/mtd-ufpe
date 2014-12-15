package br.ufpe.mtd.view;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import br.ufpe.mtd.negocio.MTDFacede;
import br.ufpe.mtd.util.MTDException;
import br.ufpe.mtd.util.MTDFactory;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

/**
 * Servlet implementation class SuggestServlet
 */
@WebServlet("/SuggestServlet")
public class SuggestServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private MTDFactory f;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public SuggestServlet() {
		super();
		f = MTDFactory.getInstancia();
	}

	@Override
	public void init() throws ServletException {

	}

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			super.service(request, response);
		} catch (Exception e) {
			retornarExcecao(request, response, "", e);
			f.getLog().salvarDadosLog(e);
		}
	}

	/**
	 * TODO: Devolver os dados como Json para serem renderizados corretamente na
	 * tela. No codigo atual estamos criando o html da sugestão aqui...
	 * 
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
			try {
				response.setContentType("text/html");
				request.setCharacterEncoding("ISO-8859-1");
				response.setCharacterEncoding("ISO-8859-1");
				
				String retorno = "";
				String dados = request.getParameter("dados");
				JSONObject j = new JSONObject(dados);
				
				String termo = j.getString("termo");
				termo = new String(Base64.decode(termo));
				String tipoSugestao = j.getString("tipo_sugestao");
				if (tipoSugestao == null) {
					tipoSugestao = MTDFacede.sugestaoPadrao();
				}
				JSPHelper.salvarTipoSugestao(request, tipoSugestao);
				JSPHelper.validarTermoBusca(termo);
				
				if (termo != null) {
					
					Collection<String> sugestoes = MTDFacede.buscarSugestoes(termo, tipoSugestao);
					
					for (String string : sugestoes) {
						
						//tratamento para sugestão que estava chegando quebrada na tela, com quebra de linha por caractere indejado.
						//gerava não funcionamento da linha de sugestão.
						string = string.replaceAll("\\n", " ").replaceAll("\\s", " ");
						
						retorno+="<div style=\"width:100%; clear:both;padding:1px; font-size: 12px; font-family: \"Trebuchet MS\", Arial, sans-serif;\" onmouseover=\"mudarCorFundo(this,'#E8E8E8')\" onmouseout=\"mudarCorFundo(this,'white')\" "
								+ "onclick=\"get('input-termo-busca').value='&quot;"+string+"&quot;'; executarForm('formBusca','./','input-termo-busca');\">";
						retorno+= "&nbsp;<span>"+string+"</span>";
						retorno+="</div>";
						retorno+="<div style=\"clear:both;background-color:#E8E8E8; width:100%;height:1px;\"></div>";
					}
				}
				
				enviarResposta(request, response, retorno);
				
			} catch (Exception e) {
				if(e instanceof MTDException){
					retornarExcecao(request, response, "", e);
				}
				f.getLog().salvarDadosLog(e);
			}
	}

	private void retornarExcecao(HttpServletRequest request, HttpServletResponse response, String retorno, Exception e1) {
		retorno += "<div style=\"width:100%; clear:both;padding:1px; font-size: 12px; font-family: \"Trebuchet MS\", Arial, sans-serif;\" " + "onmouseover=\"mudarCorFundo(this,'#E8E8E8')\" onmouseout=\"mudarCorFundo(this,'white')\" " + ">";
		retorno += "&nbsp;<span>" + e1.getMessage() + "</span>";
		retorno += "</div>";
		retorno += "<div style=\"clear:both;background-color:#E8E8E8; width:100%;height:1px;\"></div>";
		enviarResposta(request, response, retorno);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	private void enviarResposta(HttpServletRequest request, HttpServletResponse response, String dados) {
		try {
			String so = System.getProperty("os.name");
			if (so != null && so.toUpperCase().contains("LINUX")) {
				PrintWriter out = response.getWriter();
				out.write(dados);
				out.close();
			} else {
				ServletOutputStream out = response.getOutputStream();
				out.print(dados);
				out.close();
			}
		} catch (IOException e) {
			f.getLog().salvarDadosLog(e);
		}
	}
}
