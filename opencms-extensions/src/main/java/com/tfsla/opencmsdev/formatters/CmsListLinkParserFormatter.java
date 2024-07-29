package com.tfsla.opencmsdev.formatters;

import java.util.Locale;

/**
 * Esta clase necesita como input que el valor recibido tenga la forma valor1|valor2. <br>
 * Arma un link cuya descripcion es valor1, apuntando el href a valor2.
 * 
 * @author jpicasso
 */
public class CmsListLinkParserFormatter extends CmsListLinkFormatter {

	/**
	 * @param linkText si el valor no tiene la forma valor1|valor2, se comporta igual que la superclase.
	 */
	public CmsListLinkParserFormatter(String linkText) {
		super(linkText);
	}

	@Override
	public String format(Object data, Locale locale) {
		String stringData = (String) data;

		if (stringData == null) {
			return "";
		}

		if (!stringData.contains("|")) {
			// no es un contenido compuesto
			return super.format(data, locale);
		}
		else {
			String visiblePart = stringData.substring(0, stringData.indexOf("|"));
			String linkPart = stringData.substring(stringData.indexOf("|") + 1, stringData.length());

			return data != null ? ("<a href=\"" + linkPart + "\" title=\"" + linkPart
					+ "\" target=\"_blank\">" + visiblePart + "</a>") : "";
		}
	}
}