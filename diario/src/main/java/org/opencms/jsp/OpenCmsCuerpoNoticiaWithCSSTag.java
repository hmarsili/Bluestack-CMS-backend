package org.opencms.jsp;

import java.io.IOException;
import java.util.Iterator;

import jakarta.servlet.jsp.JspException;

import com.tfsla.utils.StringParser;
import com.tfsla.utils.StringUtils;

/**
 * Es una nueva version del OpenCmsCuerpoNoticiaTag, que encapsula los parrafos en divs y permite configurar
 * los estilos CSS.
 * 
 * @author jpicasso
 */
public class OpenCmsCuerpoNoticiaWithCSSTag extends AbstractOpenCmsTag {

	/**
	 * Token separador de párrafos en el cuerpo de la noticia.
	 */
	private static String token = "<br><br>";
	private String cantParrafos;
	private StringParser parser;
	private String cuerpo;
	private String espacio;

	private String cssSuperior;
	private String cssInferior;
	private String cssBanner;
	private String cssSeparador;

	@Override
	public int doStartTag() throws JspException {
		this.cuerpo = StringUtils.normalizeBr(this.getContent());
		this.parser = StringParser.newInstance(this.getCuerpo(), token, true);

		if (this.getParser().size() <= this.getCantidadParrafos()) {
			this.imprimirCuerpoCompleto();
			this.imprimir(this.getDivComienzoBanner());
			this.imprimirBanner();
			this.imprimir("</div>");
			return SKIP_BODY;
		}

		this.imprimirPrincipioCuerpo();
		this.imprimirBannerConSeparadores();
		this.imprimirFinCuerpo();

		return SKIP_BODY;
	}

	// ****************************************************************
	// ** Métodos Privados Helpers Para Cuando No Se Corta El Cuerpo **
	// ****************************************************************

	/**
	 * Imprime el cuerpo completo de la noticia con el banner al final. <br>
	 * Debe cerrar el div luego del cuerpo por como está armado el template.
	 * 
	 * Utiliza como estilo el cssSuperior.
	 */
	private void imprimirCuerpoCompleto() throws JspException {
		this.imprimir("<div class=\"" + this.cssSuperior + "\">" + this.getCuerpo() + "</div>");
		this.imprimir("<br><br>");
	}

	/**
	 * Imprime el banner en la JSP. Sólo se imprime el banner, sin separadores.
	 */
	private void imprimirBanner() throws JspException {
		this.imprimir(this.getBannerCode());
	}

	// *************************************************************
	// ** Métodos Privados Helpers Para Cuando Se Corta El Cuerpo **
	// *************************************************************

	/**
	 * Imprime los primeros párrafos del cuerpo, que se mostrarán antes del banner.<br>
	 * Encierra cada parrafo en un div que tiene el estilo cssSuperior.
	 */
	private void imprimirPrincipioCuerpo() throws JspException {
		this.imprimir("<div class=\"" + this.cssSuperior + "\">");

		Iterator<String> iter = this.getParser().iterator();
		for (int index = 1; iter.hasNext() && index <= this.getCantidadParrafos(); index++) {
			this.imprimir(iter.next());
		}

		this.imprimir("</div>");
	}

	/**
	 * Imprime los últimos párrafos del cuerpo, luego del banner.
	 */
	private void imprimirFinCuerpo() throws JspException {
		Iterator<String> iter = this.getParser().iterator();
		for (int index = 1; iter.hasNext(); index++) {
			if (index > this.getCantidadParrafos()) {
				this.imprimir("<div class=\"" + this.cssInferior + "\">" + iter.next() + "</div>");
			}
			else {
				iter.next();
			}
		}
	}

	/**
	 * Imprime el banner incluyendo los separadores para continuar el art�culo.
	 */
	private void imprimirBannerConSeparadores() throws JspException {
		this.imprimir(this.getSeparadorInicialCode());
		this.imprimir(this.getDivComienzoBanner());
		this.imprimirBanner();
		this.imprimir("<br><br>");
		this.imprimir("</div>");
		this.imprimir(this.getSeparadorFinalCode());
	}

	// ******************************
	// ** Métodos Privados Helpers **
	// ******************************
	/**
	 * Abre un div con el estilo cssBanner
	 */
	private String getDivComienzoBanner() {
		return "<div class=\"" + this.cssBanner + "\">";
	}

	/**
	 * Imprime un String en la JSP.
	 * 
	 * @param contenido String a imprimir
	 */
	private void imprimir(String contenido) throws JspException {
		try {
			this.getPageContext().getOut().print(contenido);
		}
		catch (IOException e) {
			if (getLog().isErrorEnabled()) {
				getLog().error(Messages.get().getBundle().key(Messages.LOG_ERR_JSP_BEAN_0), e);
			}
			throw new JspException(e);
		}
	}

	/**
	 * Devuelve un StringTokenizer que separa el cuerpo en párrafos.
	 */
	private StringParser getParser() {
		return this.parser;
	}

	/**
	 * Devuelve el código del banner a ser insertado en la página. REFACTORME Esto está hardcodeado con un
	 * ejemplo para el prototipo. Cuando tengamos el script de OA habrá que reemplazarlo por ese código.
	 */
	private String getBannerCode() {
		return "<!-- banner AltaVisual -->\n"
				+ "<script type=\"text/javascript\" language=\"JavaScript\">eplAD(\"" + this.getEspacio()
				+ "\");</script>\n" + "<!-- fin banner AltaVisual -->\n\n";
	}

	/**
	 * Devuelve el código para agregar el separador inicial antes del banner.
	 */
	private String getSeparadorInicialCode() {
		return this.getDivComienzoSeparador() + "<p align=\"right\"><a href=\"#sigue\">sigue</a></p>"
				+ "</div>";
	}

	private String getDivComienzoSeparador() {
		return "<div class=\"" + this.cssSeparador + "\">";
	}

	/**
	 * Devuelve el código para agregar el separador final luego del banner.
	 */
	private String getSeparadorFinalCode() {
		return this.getDivComienzoSeparador() + "<a name=\"sigue\" id=\"sigue\">&nbsp;</a></div>";
	}

	// ***************
	// ** Accessors **
	// ***************

	public int getCantidadParrafos() {
		return new Integer(this.cantParrafos).intValue();
	}

	public String getCantParrafos() {
		return this.cantParrafos;
	}

	public void setCantParrafos(String cantParrafos) {
		this.cantParrafos = cantParrafos;
	}

	public String getCuerpo() {
		return this.cuerpo;
	}

	public String getEspacio() {
		return espacio;
	}

	public void setEspacio(String espacio) {
		this.espacio = espacio;
	}

	public String getCssInferior() {
		return this.cssInferior;
	}

	public void setCssInferior(String cssInferior) {
		this.cssInferior = cssInferior;
	}

	public String getCssSuperior() {
		return this.cssSuperior;
	}

	public void setCssSuperior(String cssSuperior) {
		this.cssSuperior = cssSuperior;
	}

	public String getCssBanner() {
		return this.cssBanner;
	}

	public void setCssBanner(String cssBanner) {
		this.cssBanner = cssBanner;
	}

	public String getCssSeparador() {
		return this.cssSeparador;
	}

	public void setCssSeparador(String cssSeparador) {
		this.cssSeparador = cssSeparador;
	}
}