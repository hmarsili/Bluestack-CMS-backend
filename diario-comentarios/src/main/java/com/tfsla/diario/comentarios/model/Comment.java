package com.tfsla.diario.comentarios.model;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;

public class Comment {

	public static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

	public static final String ACEPTADO_STATE = "Aceptado";
	public static final String PENDIENTE_STATE = "Pendiente";
	public static final String RECHAZADO_STATE = "Rechazado";
	public static final String REVISION_STATE = "Revision";

	private int id;
	private String user;
	private Date date;
	private String text;
	private int cantReports;
	private String noticiaURL;
	private String state;
	private int commentcount;

	private String remoteIP;
	private int TipoEdicion;
	private int TipoEdicionShared;
	private int edicion;
	private String site;

	private int replyComment;

	// ******************************
	// ** accessors
	// ******************************


	public int getEdicion() {
		return edicion;
	}

	public void setEdicion(int edicion) {
		this.edicion = edicion;
	}

	public String getSite() {
		return site;
	}

	public void setSite(String site) {
		this.site = site;
	}

	public int getTipoEdicion() {
		return TipoEdicion;
	}

	public void setTipoEdicion(int tipoEdicion) {
		TipoEdicion = tipoEdicion;
	}
	
	public int getTipoEdicionShared() {
		return TipoEdicionShared;
	}

	public void setTipoEdicionShared(int tipoEdicionShared) {
		TipoEdicionShared = tipoEdicionShared;
	}

	public String getState() {
		return this.state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getDateAsString() {
		return dateFormat.format(this.date);
	}

	public void setCantReports(int cantReports) {
		this.cantReports = cantReports;
	}

	public int getCantReports() {
		return this.cantReports;
	}

	public int getId() {
		return this.id;
	}

	public Date getDate() {
		return this.date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public void setCommentCount(int commentCount) {
		this.commentcount = commentCount;
	}
	
	public int getCommentCount() {
		return this.commentcount;
	}

	public String getNoticiaURL() {
		return this.noticiaURL;
	}

	public void setNoticiaURL(String noticiaURL) {
		this.noticiaURL = noticiaURL;
	}

	public String getText() {
		
		return this.text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getUser() {
		return this.user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getRemoteIP() {
		return this.remoteIP;
	}

	public void setRemoteIP(String remoteIP) {
		this.remoteIP = remoteIP;
	}
	
	public void setReplyOf(int replyComment) {
		this.replyComment = replyComment;
	}
	
	public int getReplyoOf() {
		return this.replyComment;
	}

	public String getTituloNoticia(CmsObject cms) {
		try {
			CmsProperty titleProperty = cms.readPropertyObject(this.noticiaURL, "title", false);
			String titulo = titleProperty.getValue();
			if (titulo == null) {
				// tengo que hacer esto porque en el xsd de noticia existen las dos properties
				titleProperty = cms.readPropertyObject(this.noticiaURL, "Title", false);
				titulo = titleProperty.getValue();
			}

			return titulo;
		}
		catch (CmsException e) {
			try {
				CmsLog.getLog(this).debug(
						"No se pudo obtener la propiedad title de la noticia [" + this.noticiaURL + "] "
								+ "para mostrar en el dialogo de comentarios", e);
			}
			catch (Exception e2) {
				// error anidado, no se puede hacer nada mas
			}
			return "error leyendo titulo, verifique combo de site";
		}
	}

	public String getSeccionNoticia(CmsObject cms) {
		try {
			CmsProperty titleProperty = cms.readPropertyObject(this.noticiaURL, "seccion", false);
			String titulo = titleProperty.getValue();

			return titulo;
		}
		catch (CmsException e) {
			try {
				CmsLog.getLog(this).debug(
						"No se pudo obtener la propiedad seccion de la noticia [" + this.noticiaURL + "] "
								+ "para mostrar en el dialogo de comentarios", e);
			}
			catch (Exception e2) {
				// error anidado, no se puede hacer nada mas
			}
			return "error leyendo seccion, verifique combo de site";
		}
	}
}