package com.tfsla.opencmsdev.formatters;

import java.util.Locale;

import org.opencms.workplace.list.I_CmsListFormatter;

/**
 * Renderiza un link que se abre en un popup, con el detalle del texto contenido por la celda
 * 
 * @author jpicasso
 */
public class CommentTextFormatter implements I_CmsListFormatter {

	private String detailPageURL;

	public CommentTextFormatter(String detailPageURL) {
		this.detailPageURL = detailPageURL;
	}

	public String format(Object data, Locale locale) {
		if (data == null) {
			return "";
		}
		else {
			String stringData = "";
			// TODO: configuration
			if (data.toString().length() > 100) {
				stringData = data.toString().substring(0, 99);
			}
			else {
				stringData = data.toString();
			}
			return "<a href=# title=\""
					+ data.toString()
					+ "\" onclick=\"aWindow = window.open('" +
							detailPageURL +
							"','window',"
					+ "'height=100,width=400, toolbar=no, menubar=no, scrollbars=yes, "
					+ "resizable=yes,location=no, directories=no, status=no'); " + "aWindow.text = '" + data
					+ "'; return false\">" + stringData + "</a>";
		}
	}
}