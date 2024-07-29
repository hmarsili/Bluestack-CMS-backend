package com.tfsla.mail;


import org.apache.commons.mail.EmailException;

import com.tfsla.exceptions.ApplicationException;

public class EnviarArticulo {


	public static void enviarArticulo(String url, String titulo, String nombreFrom, String from, String nombreTo, String to, String comentario) {
		try {
			org.apache.commons.mail.HtmlEmail mail = new org.opencms.mail.CmsHtmlMail();
			
			mail.setHtmlMsg(
				"<a href=\"" + url +"\" >" +titulo+"</a><br />" +  comentario);
			
			mail.setSubject(titulo);
			mail.addTo(to, nombreTo);
			mail.setFrom(from, nombreFrom);
			mail.send();	
		}
		catch (EmailException e) {
			throw new ApplicationException("No se pudo enviar el mail", e);
		}
		

	}

}
