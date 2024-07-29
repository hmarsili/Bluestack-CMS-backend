package com.tfsla.opencmsdev.formatters;

import java.util.Locale;

import org.opencms.workplace.list.I_CmsListFormatter;

/**
 * Formatea el contenido de la columna como un texto con title html. <br>
 * El valor debe venir en la forma valor1|valor2, donde valor1 es la parte visible y valor2 sera lo que
 * aparezca en el title (tooltip).
 * 
 * @author jpicasso
 * 
 */
public class CmsListTitleFormatter implements I_CmsListFormatter {

	public String format(Object data, Locale locale) {
		String stringData = (String) data;

		if (stringData == null) {
			return "";
		}

		if (!stringData.contains("|")) {
			// no es un contenido compuesto
			return stringData;
		}
		else {
			String visiblePart = stringData.substring(0, stringData.indexOf("|"));
			String linkPart = stringData.substring(stringData.indexOf("|") + 1, stringData.length());

			return data != null ? ("<span title=\"" + linkPart + "\">" + visiblePart + "</span>") : "";
		}
	}

}
