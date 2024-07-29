package com.tfsla.opencms.mail;

import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.mail.EmailException;
import javax.mail.internet.MimeUtility;

import org.opencms.mail.CmsHtmlMail;
import org.opencms.mail.CmsMailTransport;

import com.tfsla.collections.CollectionFactory;
import com.tfsla.opencms.exceptions.ProgramException;

public class SimpleMail {

	private CmsHtmlMail mail = new CmsHtmlMail();
	private Map<String, String> variables = CollectionFactory.createMap();

	public void addTo(String email) {
		try {
			this.mail.addTo(email);
		}
		catch (EmailException e) {
			throw ProgramException.wrap("error en mail.addTo", e);
		}
	}

	public void setFrom(String from) {
		try {
			this.mail.setFrom(from);
		} catch (EmailException e) {
			throw ProgramException.wrap("error en mail.setFrom", e);
		}
	}

	public void setContents(String messageContent) {
		String contents = this.fillVariables(messageContent);
		try {
			this.mail.setMsg(contents);
		} catch (EmailException e) {
			throw ProgramException.wrap("error en mail.setMsg", e);
		}
	}

	public void setHtmlContents(String htmlContents) {
		String contents = this.fillVariables(htmlContents);
		try {
			this.mail.setHtmlMsg(contents);
		} catch (EmailException e) {
			throw ProgramException.wrap("error en mail.setHtmlMsg", e);
		}
	}

	@SuppressWarnings("unchecked")
	private String fillVariables(String htmlContents) {
		for (Iterator iter = this.variables.entrySet().iterator(); iter.hasNext();) {
			Map.Entry<String, String> entry = (Map.Entry<String, String>) iter.next();
			String variableName = entry.getKey();
			String variableValue = entry.getValue();
			htmlContents = htmlContents.replace("${" + variableName + "}", variableValue);
		}

		return htmlContents;
	}

	public void send() {
		try {
			//this.mail.send();
			CmsMailTransport mTransport = new CmsMailTransport(this.mail);
			mTransport.send();
		}
		catch (Exception e) {
			throw ProgramException.wrap("error al intentar enviar un mail", e);
		}
	}

	public void setSubject(String subject) {
		try {
			this.mail.setCharset("UTF-8");
			this.mail.setSubject(MimeUtility.encodeText(subject));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//this.mail.setSubject(subject);
	}

	/**
	 * Setea valores a las variables del template del mail
	 */
	public void setValue(String name, Object value) {
		this.variables.put(name, value + "");
	}
}