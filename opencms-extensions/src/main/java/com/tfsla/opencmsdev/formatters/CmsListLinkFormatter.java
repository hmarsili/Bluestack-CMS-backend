package com.tfsla.opencmsdev.formatters;

import java.util.Locale;

import org.opencms.workplace.list.I_CmsListFormatter;

/**
 * Formatea un item de una celda como un link que se abre en una ventana nueva, y que tiene como tooltip la
 * url a donde apunte el link.<br>
 * Opcionalmente se puedde proporcionar un <code>linkText</code> para usar como descripcion del link.Si no
 * se proporciona, el default es mostrar la url.
 * 
 * @author jpicasso
 */
public class CmsListLinkFormatter implements I_CmsListFormatter {

	private String linkText;

	// ******************************
	// ** Constructor
	// ******************************
	/**
	 * @param linkText el texto que tiene que aparecer en el link. Si es null, defaultea al href.
	 */
	public CmsListLinkFormatter(String linkText) {
		this.linkText = linkText;
	}

	// ******************************
	// ** formatter
	// ******************************
	public String format(Object data, Locale locale) {
		return data != null ? ("<a href=\"" + data.toString() + "\" title=\"" + data.toString()
				+ "\" target=\"_blank\">" + (this.linkText != null ? this.linkText : data.toString()) + "</a>")
				: "";
	}
}