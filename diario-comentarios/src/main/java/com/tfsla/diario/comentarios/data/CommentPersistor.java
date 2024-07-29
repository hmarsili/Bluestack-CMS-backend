package com.tfsla.diario.comentarios.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import com.tfsla.collections.CollectionFactory;
import com.tfsla.diario.comentarios.model.AbuseReport;
import com.tfsla.diario.comentarios.model.Comment;
import com.tfsla.diario.comentarios.services.CommentsModule;
import com.tfsla.opencms.exceptions.ProgramException;
import com.tfsla.opencms.persistence.AbstractBusinessObjectPersitor;
import com.tfsla.workflow.QueryBuilder;
import com.tfsla.workflow.ResultSetProcessor;

public class CommentPersistor extends AbstractBusinessObjectPersitor {

	
	private CommentsModule commentModule = null;
	
	public CommentPersistor(CommentsModule commentModule)
	{
		this.commentModule = commentModule;
	}
	
	private static final String TFS_COMMENTS = "TFS_COMMENTS";
	private static final String TFS_COMMENTS_REPORTS = "TFS_COMMENTS_REPORT";
	//Comentar
	//private static final String TFS_COMMENTS_OFFLINE = TFS_COMMENTS + "_OFFLINE";

	private static final String ID_COMMENT = "ID_COMMENT";
	private static final String USERNAME = "USERNAME";
	private static final String FECHA = "FECHA";
	private static final String TEXT = "TEXT";
	private static final String CANT_REPORTS = "CANT_REPORTS";
	private static final String NOTICIA_URL = "NOTICIA_URL";
	private static final String STATE = "STATE";
	private static final String REMOTE_IP = "REMOTE_IP";
	
	private static final String ID_REPORTABUSE = "ID";
	private static final String PATH = "PATH";
	private static final String MOTIVO = "MOTIVO";
	private static final String USUARIO = "USUARIO";
	private static final String DESCRIPCION = "DESCRIPCION";
	private static final String SITIO = "SITIO";
	private static final String COMMENTID = "COMMENTID";

	private static final String EDICION = "EDICION";
	private static final String TIPO_EDICION = "TIPO_EDICION";
	private static final String TIPO_EDICION_SHARED = "TIPO_EDICION_SHARED";
	private static final String SITE = "SITE";
	private static final String RESPUESTA_DE = "RESPUESTA_DE";

	public void save(CmsObject cms, Comment c) {
		QueryBuilder queryBuilder = new QueryBuilder(cms);
		queryBuilder.setSQLQuery("INSERT INTO " + getCommentsTableName(cms) + " (" + USERNAME + ", " + FECHA
				+ ", " + TEXT + ", " + CANT_REPORTS + ", " + NOTICIA_URL + ", " + STATE + ", " + REMOTE_IP
				 + ", " + EDICION  + ", " + TIPO_EDICION + ", " + TIPO_EDICION_SHARED + ", " + SITE
				+ ", " + RESPUESTA_DE+ ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
		queryBuilder.addParameter(c.getUser());
		queryBuilder.addParameter(c.getDate());
		queryBuilder.addParameter(c.getText());
		queryBuilder.addParameter(c.getCantReports());
		queryBuilder.addParameter(c.getNoticiaURL());
		queryBuilder.addParameter(c.getState());
		queryBuilder.addParameter(c.getRemoteIP());

		queryBuilder.addParameter(c.getEdicion());
		queryBuilder.addParameter(c.getTipoEdicion());
		queryBuilder.addParameter(c.getTipoEdicionShared());
		queryBuilder.addParameter(c.getSite());
		queryBuilder.addParameter(c.getReplyoOf());

		queryBuilder.execute();
	}
	
	public void save(CmsObject cms, AbuseReport r) {
		QueryBuilder queryBuilder = new QueryBuilder(cms);
		queryBuilder.setSQLQuery("INSERT INTO " + getAbuseReportTableName() + " (" + FECHA + ", " + PATH
				+ ", " + MOTIVO + ", " + USUARIO + ", " + DESCRIPCION + ", " + SITIO + ", " + COMMENTID
				 + ") VALUES (?, ?, ?, ?, ?, ?, ?)");
		queryBuilder.addParameter(r.getFecha());
		queryBuilder.addParameter(r.getPath());
		queryBuilder.addParameter(r.getMotivo());
		queryBuilder.addParameter(r.getUsuario());
		queryBuilder.addParameter(r.getDescription());
		queryBuilder.addParameter(r.getSitio());
		queryBuilder.addParameter(r.getCommentId());
		queryBuilder.execute();
	}

	private String getCommentsTableName(CmsObject cms) {
		// CmsProject currentProject = cms.getRequestContext().currentProject();
		// A pedido del publico deshabilitamos los comentarios en el offline, solo habra comentarios en el
		// online. Igual lo dejo comentado porque nunca se sabe.
		// if (currentProject.isOnlineProject()) {
			 //Solo Dejar esto
			 return TFS_COMMENTS;
		// }
		// else {
		// return TFS_COMMENTS_OFFLINE;
		// }
	}
	
	private String getAbuseReportTableName() {
		
		return TFS_COMMENTS_REPORTS;
	}

	public void incrementReportsCount(CmsObject cms, String commentId) {
		QueryBuilder queryBuilder = new QueryBuilder(cms);
		queryBuilder.setSQLQuery("UPDATE " + getCommentsTableName(cms) + " " + "SET " + CANT_REPORTS + " = "
				+ CANT_REPORTS + " + 1 " + "WHERE " + ID_COMMENT + " = ?");

		queryBuilder.addParameter(commentId);

		queryBuilder.execute();
	}

	public void resetReportsCount(CmsObject cms, String commentId) {
		QueryBuilder queryBuilder = new QueryBuilder(cms);
		queryBuilder.setSQLQuery("UPDATE " + getCommentsTableName(cms) + " " + "SET " + CANT_REPORTS + " = 0 WHERE " + ID_COMMENT + " = ?");

		queryBuilder.addParameter(commentId);

		queryBuilder.execute();
	}
	
	public int getCommentsCount(CmsObject cms, String noticiaURL, String site) {
		QueryBuilder<Integer> queryBuilder = new QueryBuilder<Integer>(cms);
		
        String state = "AND (STATE ='"+Comment.ACEPTADO_STATE+"' ";
		
        if(commentModule.showCommentRevision()){
        	state += " OR STATE ='"+Comment.REVISION_STATE+"' ";
        }
        
        if(commentModule.showCommentRejected()){
        	state += " OR STATE = '"+Comment.RECHAZADO_STATE+"' ";
        }
        
        state +=  " )";
		
		queryBuilder.setSQLQuery("SELECT COUNT(" + ID_COMMENT + ") FROM " + getCommentsTableName(cms)
				+ " WHERE " + NOTICIA_URL + " = ? " + state + " AND "+ SITE + "= ?");

		queryBuilder.addParameter(noticiaURL);
		queryBuilder.addParameter(site);
		//queryBuilder.addParameter(Comment.ACEPTADO_STATE);

		ResultSetProcessor<Integer> proc = new ResultSetProcessor<Integer>() {

			private int count = 0;

			public void processTuple(ResultSet rs) {

				try {
					this.count = rs.getInt(1);
				}
				catch (SQLException e) {
					throw ProgramException.wrap(
							"error al intentar recuperar la cantidad de comentarios de la base", e);
				}
			}

			public Integer getResult() {
				return this.count;
			}
		};

		return queryBuilder.execute(proc);
	}
	
	public int getCommentsAnswersCount(CmsObject cms, String noticiaURL, String site) {
		QueryBuilder<Integer> queryBuilder = new QueryBuilder<Integer>(cms);
		
        String state = "AND (STATE ='"+Comment.ACEPTADO_STATE+"' ";
		
        if(CommentsModule.getInstance(cms).showCommentRevision()){
        	state += " OR STATE ='"+Comment.REVISION_STATE+"' ";
        }
        
        if(CommentsModule.getInstance(cms).showCommentRejected()){
        	state += " OR STATE = '"+Comment.RECHAZADO_STATE+"' ";
        }
        
        state +=  " )";
		
		queryBuilder.setSQLQuery("SELECT COUNT(" + ID_COMMENT + ") FROM " + getCommentsTableName(cms)
				+ " WHERE RESPUESTA_DE <>'0' AND " + NOTICIA_URL + " = ? " + state + " AND "+ SITE + "= ?");

		queryBuilder.addParameter(noticiaURL);
		queryBuilder.addParameter(site);
		//queryBuilder.addParameter(Comment.ACEPTADO_STATE);

		ResultSetProcessor<Integer> proc = new ResultSetProcessor<Integer>() {

			private int count = 0;

			public void processTuple(ResultSet rs) {

				try {
					this.count = rs.getInt(1);
				}
				catch (SQLException e) {
					throw ProgramException.wrap(
							"error al intentar recuperar la cantidad de comentarios de la base", e);
				}
			}

			public Integer getResult() {
				return this.count;
			}
		};

		return queryBuilder.execute(proc);
	}

	public int getCommentsCountByParent(CmsObject cms, String noticiaURL, int parentId, String site) {
		QueryBuilder<Integer> queryBuilder = new QueryBuilder<Integer>(cms);
		
		String state = "AND (STATE ='"+Comment.ACEPTADO_STATE+"' ";
		
        if(commentModule.showCommentRevision()){
        	state += " OR STATE ='"+Comment.REVISION_STATE+"' ";
        }
        
        if(commentModule.showCommentRejected()){
        	state += " OR STATE = '"+Comment.RECHAZADO_STATE+"' ";
        }
        
        state +=  " )";

		
		queryBuilder.setSQLQuery("SELECT COUNT(" + ID_COMMENT + ") FROM " + getCommentsTableName(cms)
				+ " WHERE " + NOTICIA_URL + " = ? AND RESPUESTA_DE = ? " + state + "AND "+ SITE + "= ?");

		
		queryBuilder.addParameter(noticiaURL);
		queryBuilder.addParameter(parentId);
		queryBuilder.addParameter(site);
		//queryBuilder.addParameter(Comment.ACEPTADO_STATE);

		ResultSetProcessor<Integer> proc = new ResultSetProcessor<Integer>() {

			private int count = 0;

			public void processTuple(ResultSet rs) {

				try {
					this.count = rs.getInt(1);
				}
				catch (SQLException e) {
					throw ProgramException.wrap(
							"error al intentar recuperar la cantidad de comentarios de la base", e);
				}
			}

			public Integer getResult() {
				return this.count;
			}
		};

		return queryBuilder.execute(proc);
	}
    

	public int getAllCommentsCount(CmsObject cms, String sitio) {
		QueryBuilder<Integer> queryBuilder = new QueryBuilder<Integer>(cms);
		
		String state = "AND (STATE ='"+Comment.ACEPTADO_STATE+"' ";
		
        if(commentModule.showCommentRevision()){
        	state += " OR STATE ='"+Comment.REVISION_STATE+"' ";
        }
        
        if(commentModule.showCommentRejected()){
        	state += " OR STATE = '"+Comment.RECHAZADO_STATE+"' ";
        }
        
        state +=  " )";

		queryBuilder.setSQLQuery("SELECT COUNT(" + ID_COMMENT + ") FROM " + getCommentsTableName(cms)
				+ " WHERE " + SITE + " = ? " + state );

		queryBuilder.addParameter(sitio);
		//queryBuilder.addParameter(Comment.ACEPTADO_STATE);

		ResultSetProcessor<Integer> proc = new ResultSetProcessor<Integer>() {

			private int count = 0;

			public void processTuple(ResultSet rs) {

				try {
					this.count = rs.getInt(1);
				}
				catch (SQLException e) {
					throw ProgramException.wrap(
							"error al intentar recuperar la cantidad de comentarios de la base", e);
				}
			}

			public Integer getResult() {
				return this.count;
			}
		};

		return queryBuilder.execute(proc);
		
	}
	
	public List<Comment> getCommentsByAuthor(CmsObject cms, String userName, String pageNumber, String site) {
		QueryBuilder<List<Comment>> queryBuilder = new QueryBuilder<List<Comment>>(cms);
		queryBuilder.setSQLQuery("SELECT * FROM " + getCommentsTableName(cms) + " WHERE " + USERNAME
				+ " = ?  AND "+ SITE + "= ? ORDER BY " + FECHA + " DESC LIMIT " + commentModule.getQueryPageSize() + " OFFSET "
				+ (new Integer(pageNumber) - 1) * commentModule.getQueryPageSize());

		queryBuilder.addParameter(userName);
		queryBuilder.addParameter(site);

		ResultSetProcessor<List<Comment>> proc = this.getComentariosListProcessor();

		return queryBuilder.execute(proc);

	}

	public int getCommentsByAuthorCount(CmsObject cms, String userName, String site) {
		QueryBuilder<Integer> queryBuilder = new QueryBuilder<Integer>(cms);
		queryBuilder.setSQLQuery("SELECT COUNT(" + ID_COMMENT + ") FROM " + getCommentsTableName(cms)
				+ " WHERE " + USERNAME + " = ? AND "+ SITE + "= ?");

		queryBuilder.addParameter(userName);
		queryBuilder.addParameter(site);

		ResultSetProcessor<Integer> proc = new ResultSetProcessor<Integer>() {

			private int count = 0;

			public void processTuple(ResultSet rs) {

				try {
					this.count = rs.getInt(1);
				}
				catch (SQLException e) {
					throw ProgramException.wrap(
							"error al intentar recuperar la cantidad de comentarios de la base", e);
				}
			}

			public Integer getResult() {
				return this.count;
			}
		};

		return queryBuilder.execute(proc);
	}
	
	
	public List<Comment> getCommentsWhitMoreAnswers(CmsObject cms, String noticiaURL, int min_answers, String pageNumber, String site) {
		QueryBuilder<List<Comment>> queryBuilder = new QueryBuilder<List<Comment>>(cms);
		
		String state = " AND (STATE ='"+Comment.ACEPTADO_STATE+"' ";
		
        if(commentModule.showCommentRevision()){
        	state += " OR STATE ='"+Comment.REVISION_STATE+"' ";
        }
        
        if(commentModule.showCommentRejected()){
        	state += " OR STATE = '"+Comment.RECHAZADO_STATE+"' ";
        }
        
        state +=  " )";
		
		String QrNoticiaURL = "";
		if(noticiaURL != null){
			QrNoticiaURL = " AND " + getCommentsTableName(cms) + "." + NOTICIA_URL + " = '"+ noticiaURL +"'";
		}
		
		String QrMinAnswersHaving = "";
		if(min_answers > 0){
			QrMinAnswersHaving = 
							   " HAVING count(" + RESPUESTA_DE + ") >="+min_answers;	
		}
		
		queryBuilder.setSQLQuery("SELECT * FROM " + getCommentsTableName(cms) + ", " +
				" ( SELECT " + RESPUESTA_DE + " FROM " + getCommentsTableName(cms) + "" +
				" WHERE " + RESPUESTA_DE + " <>'0' "+ state + " AND "+ SITE + "= '"+site+"'" +
				QrNoticiaURL +
				"   GROUP BY " + RESPUESTA_DE +
				QrMinAnswersHaving +
				" ORDER BY count(" + RESPUESTA_DE +") desc) as top  " +
				
				" WHERE  " + getCommentsTableName(cms) + "." + ID_COMMENT + " = top. " + RESPUESTA_DE + 
				state +
				QrNoticiaURL +
				" AND "+ SITE + "= '"+site+"' "+
				" LIMIT " + commentModule.getQueryPageSize() + " OFFSET "
				+ (new Integer(pageNumber) - 1) * commentModule.getQueryPageSize());

		ResultSetProcessor<List<Comment>> proc = this.getComentariosListProcessor();

		return queryBuilder.execute(proc);
		
	}
	
	public int getCommentsWhitMoreAnswersCount(CmsObject cms, String noticiaURL, int min_answers, String site) {
		QueryBuilder<Integer> queryBuilder = new QueryBuilder<Integer>(cms);
		
		String state = " AND (STATE ='"+Comment.ACEPTADO_STATE+"' ";
		
        if(commentModule.showCommentRevision()){
        	state += " OR STATE ='"+Comment.REVISION_STATE+"' ";
        }
        
        if(commentModule.showCommentRejected()){
        	state += " OR STATE = '"+Comment.RECHAZADO_STATE+"' ";
        }
        
        state +=  " )";
		
		String QrNoticiaURL = "";
		if(noticiaURL != null){
			QrNoticiaURL = " AND " + getCommentsTableName(cms) + "." + NOTICIA_URL + " = '"+ noticiaURL +"'";
		}
		
		String QrMinAnswersFrom = "";
		String QrMinAnswersWhere = "";
		if(min_answers > -1){
			QrMinAnswersFrom = ", ( SELECT " + RESPUESTA_DE + ", count(" + RESPUESTA_DE + ") as cant  FROM " + getCommentsTableName(cms) + 
							   " WHERE " + RESPUESTA_DE + " <>'0'  "+ state + " AND "+ SITE + "= '"+site+"'  GROUP BY " + RESPUESTA_DE + ") as min";
			
			QrMinAnswersWhere =" AND min.cant> "+min_answers+" AND min." + RESPUESTA_DE + "=top." + RESPUESTA_DE + "";
		}
		
		queryBuilder.setSQLQuery("SELECT count("+getCommentsTableName(cms)+"." + RESPUESTA_DE + ") FROM " + getCommentsTableName(cms) + ", " +
				" ( SELECT " + RESPUESTA_DE + " FROM " + getCommentsTableName(cms) + "" +
				" WHERE " + RESPUESTA_DE + " <>'0' "+ state + " AND "+ SITE + "= '"+site+"' GROUP BY " + RESPUESTA_DE +" ORDER BY count(" + RESPUESTA_DE +") desc) as top  " +
				QrMinAnswersFrom +
				" WHERE  " + getCommentsTableName(cms) + "." + ID_COMMENT + " = top. " + RESPUESTA_DE + 
				state +
				" AND "+ SITE + "= '"+site+"' "+
				QrNoticiaURL +
				QrMinAnswersWhere);

		ResultSetProcessor<Integer> proc = new ResultSetProcessor<Integer>() {

			private int count = 0;

			public void processTuple(ResultSet rs) {

				try {
					this.count = rs.getInt(1);
				}
				catch (SQLException e) {
					throw ProgramException.wrap(
							"error al intentar recuperar la cantidad de comentarios de la base", e);
				}
			}

			public Integer getResult() {
				return this.count;
			}
		};

		return queryBuilder.execute(proc);
		
	}
	
	public int getCommentAnswersCount(CmsObject cms, String IdComment) {
		QueryBuilder<Integer> queryBuilder = new QueryBuilder<Integer>(cms);
		
		String state = "AND (STATE ='"+Comment.ACEPTADO_STATE+"' ";
		
        if(commentModule.showCommentRevision()){
        	state += " OR STATE ='"+Comment.REVISION_STATE+"' ";
        }
        
        if(commentModule.showCommentRejected()){
        	state += " OR STATE = '"+Comment.RECHAZADO_STATE+"' ";
        }
        
        state +=  " )";
		
		queryBuilder.setSQLQuery("SELECT count("+getCommentsTableName(cms)+"." + RESPUESTA_DE + ") FROM " + getCommentsTableName(cms) + 
				" WHERE  " + getCommentsTableName(cms) + "."+ RESPUESTA_DE +"=" + IdComment +
				state );

		ResultSetProcessor<Integer> proc = new ResultSetProcessor<Integer>() {

			private int count = 0;

			public void processTuple(ResultSet rs) {

				try {
					this.count = rs.getInt(1);
				}
				catch (SQLException e) {
					throw ProgramException.wrap(
							"error al intentar recuperar la cantidad de comentarios de la base", e);
				}
			}

			public Integer getResult() {
				return this.count;
			}
		};

		return queryBuilder.execute(proc);
		
	}
		
		
	/**
	 * @param noticiaURL si es null, no filtra por noticiaURL y trae todos
	 */
	public List<Comment> getNewComments(CmsObject cms, String noticiaURL, int fromCommentId, String site) {
		QueryBuilder<List<Comment>> queryBuilder = new QueryBuilder<List<Comment>>(cms);
		
		String state = "AND (STATE ='"+Comment.ACEPTADO_STATE+"' ";
		
        if(commentModule.showCommentRevision()){
        	state += " OR STATE ='"+Comment.REVISION_STATE+"' ";
        }
        
        if(commentModule.showCommentRejected()){
        	state += " OR STATE = '"+Comment.RECHAZADO_STATE+"' ";
        }
        
        state +=  " )";
		
		queryBuilder.setSQLQuery("SELECT * FROM " + getCommentsTableName(cms)
				+ " WHERE " + NOTICIA_URL + " = ? " 
				+ state 
				+ " AND "+ SITE + "= ? " 
				+ " AND "+ ID_COMMENT + "> ? " 
				+ " ORDER BY " + ID_COMMENT + " DESC ");

		queryBuilder.addParameter(noticiaURL);
		queryBuilder.addParameter(site);
		queryBuilder.addParameter(fromCommentId);
		

		ResultSetProcessor<List<Comment>> proc = this.getComentariosListProcessor();

		return queryBuilder.execute(proc);

	}

	/**
	 * @param noticiaURL si es null, no filtra por noticiaURL y trae todos
	 */
	public List<Comment> getPreviousComments(CmsObject cms, String noticiaURL, int commentIdTo, String site, int CommentPadreID ) {
		QueryBuilder<List<Comment>> queryBuilder = new QueryBuilder<List<Comment>>(cms);
		
		String state = "AND (STATE ='"+Comment.ACEPTADO_STATE+"' ";
		
        if(commentModule.showCommentRevision()){
        	state += " OR STATE ='"+Comment.REVISION_STATE+"' ";
        }
        
        if(commentModule.showCommentRejected()){
        	state += " OR STATE = '"+Comment.RECHAZADO_STATE+"' ";
        }
        
        state +=  " )";
		
		queryBuilder.setSQLQuery("SELECT * FROM " + getCommentsTableName(cms) 
				+ " WHERE " + NOTICIA_URL + " = ? " 
				+ state 
				+ " AND "+ SITE + "= ? " 
				+ " AND "+ ID_COMMENT + "< ? " 
				+ " AND RESPUESTA_DE = ? "
				+ " ORDER BY " + ID_COMMENT + " DESC " 
				+ " LIMIT " + commentModule.getQueryPageSize());

		queryBuilder.addParameter(noticiaURL);
		queryBuilder.addParameter(site);
		queryBuilder.addParameter(commentIdTo);
		queryBuilder.addParameter(CommentPadreID);
		//queryBuilder.addParameter(Comment.ACEPTADO_STATE);
		

		ResultSetProcessor<List<Comment>> proc = this.getComentariosListProcessor();

		return queryBuilder.execute(proc);

	}

	/**
	 * @param noticiaURL si es null, no filtra por noticiaURL y trae todos
	 */
	public List<Comment> getComments(CmsObject cms, String noticiaURL, String pageNumber, String site) {
		QueryBuilder<List<Comment>> queryBuilder = new QueryBuilder<List<Comment>>(cms);
		
		String state = "AND (STATE ='"+Comment.ACEPTADO_STATE+"' ";
		
        if(commentModule.showCommentRevision()){
        	state += " OR STATE ='"+Comment.REVISION_STATE+"' ";
        }
        
        if(commentModule.showCommentRejected()){
        	state += " OR STATE = '"+Comment.RECHAZADO_STATE+"' ";
        }
        
        state +=  " )";
		
		queryBuilder.setSQLQuery("SELECT * FROM " + getCommentsTableName(cms) + " WHERE " + NOTICIA_URL
				+ " = ? " + state + " AND "+ SITE + "= ?  ORDER BY " + FECHA + " DESC LIMIT " + commentModule.getQueryPageSize() + " OFFSET "
				+ (new Integer(pageNumber) - 1) * commentModule.getQueryPageSize());

		queryBuilder.addParameter(noticiaURL);
		queryBuilder.addParameter(site);
		//queryBuilder.addParameter(Comment.ACEPTADO_STATE);
		

		ResultSetProcessor<List<Comment>> proc = this.getComentariosListProcessor();

		return queryBuilder.execute(proc);

	}
	
	/**
	 * @param noticiaURL si es null, no filtra por noticiaURL y trae todos
	 */
	public List<AbuseReport> getListAbuseReportsByComment(CmsObject cms, String commentId) {
		QueryBuilder<List<AbuseReport>> queryBuilder = new QueryBuilder<List<AbuseReport>>(cms);
			
		//queryBuilder.setSQLQuery("SELECT * FROM " + getAbuseReportTableName() + " WHERE " + NOTICIA_URL
		//		+ " = ? AND "+ SITE + "= ?  ORDER BY " + FECHA + " DESC LIMIT " + commentModule.getQueryPageSize() + " OFFSET "
		//		+ (new Integer(pageNumber) - 1) * commentModule.getQueryPageSize());
		queryBuilder.setSQLQuery("SELECT * FROM " + getAbuseReportTableName() 
				+ " WHERE COMMENTID = ? " );

		queryBuilder.addParameter(commentId);
		//queryBuilder.addParameter(site);
		//queryBuilder.addParameter(Comment.ACEPTADO_STATE);
		

		ResultSetProcessor<List<AbuseReport>> proc = this.getAbuseReportsListProcessor();

		return queryBuilder.execute(proc);

	}

	public List<Comment> getCommentsInRevision(CmsObject cms, Date from) {
		QueryBuilder<List<Comment>> queryBuilder = new QueryBuilder<List<Comment>>(cms);
		
		queryBuilder.setSQLQuery("SELECT * FROM " + getCommentsTableName(cms) + " WHERE STATE ='"+Comment.REVISION_STATE+"' AND "+ FECHA + ">= ? ");

		queryBuilder.addParameter(from);
		

		ResultSetProcessor<List<Comment>> proc = this.getComentariosListProcessor();

		return queryBuilder.execute(proc);

	}

	/**
	 * @param noticiaURL 
	 * @param pageNumber 
	 * @param PageSize si es 0 trae todas
	 * @param CommentPadreID si es 0 trae los comentarios de primer nivel 
	 */	
	
	public  List<Comment> getCommentsByLevels(CmsObject cms, String noticiaURL, int CommentPadreID, String pageNumber, int PageSize){
		return getCommentsByLevels(cms, noticiaURL, CommentPadreID, pageNumber, PageSize, 0);
	}
	
	public  List<Comment> getCommentsByLevels(CmsObject cms, String noticiaURL, int CommentPadreID, String pageNumber, int PageSize, int offset){
		
		  String siteName = OpenCms.getSiteManager().getCurrentSite(cms).getTitle();
		  siteName = siteName.replaceFirst("/sites/", "");
          siteName = siteName.replaceFirst("/site/", "");
		  siteName = siteName.substring(0,siteName.length() -1);
		
		  QueryBuilder<List<Comment>> queryBuilder = new QueryBuilder<List<Comment>>(cms);
		
		  String state = "AND (STATE ='"+Comment.ACEPTADO_STATE+"' ";
			
	      if(commentModule.showCommentRevision()){
	        	state += " OR STATE ='"+Comment.REVISION_STATE+"' ";
	      }
	        
	      if(commentModule.showCommentRejected()){
	        	state += " OR STATE = '"+Comment.RECHAZADO_STATE+"' ";
	      }
	        
	      state +=  " )";
		  
	      
		  if (PageSize !=0 && CommentPadreID==0){
			
			  
			  
		 	queryBuilder.setSQLQuery("SELECT * FROM " + getCommentsTableName(cms) + " WHERE " + NOTICIA_URL
										+ " = ? "+ " AND RESPUESTA_DE = ?"+ " "+state + " AND "+ SITE + "= ? ORDER BY " + FECHA 
										+ " DESC LIMIT " + PageSize + " OFFSET "
										+ ((new Integer(pageNumber) - 1) * PageSize + offset));
		  }else{
			queryBuilder.setSQLQuery("SELECT * FROM " + getCommentsTableName(cms) + " WHERE " + NOTICIA_URL
										+ " = ? "+ " AND RESPUESTA_DE = ?"+ " "+state + " AND "+ SITE + "= ? ORDER BY " + FECHA 
										+ " DESC ");
		  }
		  
		  queryBuilder.addParameter(noticiaURL);
		  queryBuilder.addParameter(CommentPadreID);
		  queryBuilder.addParameter(siteName);
		  
		  CmsLog.getLog(this).debug(queryBuilder.toString());
		  
		  ResultSetProcessor<List<Comment>> proc = this.getComentariosListProcessor();
		  return queryBuilder.execute(proc);
	}
	
	
	private ResultSetProcessor<List<Comment>> getComentariosListProcessor() {
		ResultSetProcessor<List<Comment>> proc = new ResultSetProcessor<List<Comment>>() {

			private List<Comment> results = CollectionFactory.createList();

			public void processTuple(ResultSet rs) {

				try {
					Comment c = new Comment();
					c.setId(rs.getInt(ID_COMMENT));
					c.setCantReports(rs.getInt(CANT_REPORTS));
					c.setDate(rs.getTimestamp(FECHA));
					c.setNoticiaURL(rs.getString(NOTICIA_URL));
					c.setState(rs.getString(STATE));
					c.setUser(rs.getString(USERNAME));
					c.setSite(rs.getString(SITE));
					c.setTipoEdicion(rs.getInt(TIPO_EDICION));
					c.setTipoEdicionShared(rs.getInt(TIPO_EDICION_SHARED));
					String text = rs.getString(TEXT);
					text = text.replace("<", "&lt;");
					text = text.replace(">", "&gt;");
							
					c.setText(text);
					c.setRemoteIP(rs.getString(REMOTE_IP));
					c.setReplyOf(rs.getInt(RESPUESTA_DE));

					this.results.add(c);
				}
				catch (SQLException e) {
					throw ProgramException.wrap("error al intentar recuperar los comentarios de la base", e);
				}
			}

			public List<Comment> getResult() {
				return this.results;
			}
		};
		return proc;
	}
	
	private ResultSetProcessor<List<AbuseReport>> getAbuseReportsListProcessor() {
		ResultSetProcessor<List<AbuseReport>> proc = new ResultSetProcessor<List<AbuseReport>>() {

			private List<AbuseReport> results = CollectionFactory.createList();

			public void processTuple(ResultSet rs) {

				try {
					AbuseReport c = new AbuseReport();
					c.setId(rs.getInt(ID_REPORTABUSE));					
					c.setFecha(rs.getTimestamp(FECHA));
					c.setMotivo(rs.getString(MOTIVO));
					c.setPath(rs.getString(PATH));
					c.setSitio(rs.getString(SITIO));
					c.setUsuario(rs.getString(USUARIO));
					c.setDescription(rs.getString(DESCRIPCION));
					
					this.results.add(c);
				}
				catch (SQLException e) {
					throw ProgramException.wrap("error al intentar recuperar los comentarios de la base", e);
				}
			}

			public List<AbuseReport> getResult() {
				return this.results;
			}
		};
		return proc;
	}	

	public void delete(CmsObject cms, String commentId) {
		QueryBuilder queryBuilder = new QueryBuilder(cms);
		queryBuilder.setSQLQuery("DELETE FROM " + getCommentsTableName(cms) + " " + "WHERE " + ID_COMMENT
				+ " = ?");

		queryBuilder.addParameter(commentId);

		queryBuilder.execute();
	}

	public Comment getComment(CmsObject cms, final String commentId) {
		QueryBuilder<Comment> queryBuilder = new QueryBuilder<Comment>(cms);
		queryBuilder.setSQLQuery("SELECT * FROM " + getCommentsTableName(cms) + " WHERE " + ID_COMMENT
				+ " = ?");

		queryBuilder.addParameter(commentId);

		ResultSetProcessor<Comment> proc = new ResultSetProcessor<Comment>() {

			private Comment c;

			public void processTuple(ResultSet rs) {

				try {
					this.c = new Comment();
					this.c.setId(rs.getInt(ID_COMMENT));
					this.c.setCantReports(rs.getInt(CANT_REPORTS));
					this.c.setDate(rs.getDate(FECHA));
					this.c.setNoticiaURL(rs.getString(NOTICIA_URL));
					this.c.setState(rs.getString(STATE));
					this.c.setUser(rs.getString(USERNAME));
					this.c.setReplyOf(rs.getInt(RESPUESTA_DE));
					this.c.setSite(rs.getString(SITE));
					
					String text = rs.getString(TEXT);
					text = text.replace("<", "&lt;");
					text = text.replace(">", "&gt;");
							
					this.c.setText(text);
					this.c.setRemoteIP(rs.getString(REMOTE_IP));
				}
				catch (SQLException e) {
					throw ProgramException.wrap("error al intentar recuperar la info de usuarios de la base",
							e);
				}
			}

			public Comment getResult() {
				if (c != null) {
					return this.c;
				}
				else {
					throw new ProgramException("No se encontro un comentario con el id [" + commentId + "]");
				}
			}
		};

		return queryBuilder.execute(proc);
	}

	public void acceptComment(CmsObject cms, String commentId) {
		QueryBuilder queryBuilder = new QueryBuilder(cms);
		queryBuilder.setSQLQuery("UPDATE " + getCommentsTableName(cms) + " " + "SET " + STATE + " = ? WHERE "
				+ ID_COMMENT + " = ?");

		queryBuilder.addParameter(Comment.ACEPTADO_STATE);
		queryBuilder.addParameter(commentId);

		queryBuilder.execute();
	}
	
	public void revisionComment(CmsObject cms, String commentId) {
		QueryBuilder queryBuilder = new QueryBuilder(cms);
		queryBuilder.setSQLQuery("UPDATE " + getCommentsTableName(cms) + " " + "SET " + STATE + " = ? WHERE "
				+ ID_COMMENT + " = ?");

		queryBuilder.addParameter(Comment.REVISION_STATE);
		queryBuilder.addParameter(commentId);

		queryBuilder.execute();
	}

	public void rejectComment(CmsObject cms, String commentId) {
		QueryBuilder queryBuilder = new QueryBuilder(cms);
		queryBuilder.setSQLQuery("UPDATE " + getCommentsTableName(cms) + " " + "SET " + STATE + " = ? WHERE "
				+ ID_COMMENT + " = ?");

		queryBuilder.addParameter(Comment.RECHAZADO_STATE);
		queryBuilder.addParameter(commentId);

		queryBuilder.execute();
	}

	/**
	 * @param verHistorico true si queremos ver todos los comentarios desde el inicio de los tiempos. false si
	 *            solo queremos ver los mas recientes.
	 */
	public List<Comment> getAllComments(CmsObject cms, boolean verHistorico, String site) {
		QueryBuilder<List<Comment>> queryBuilder = new QueryBuilder<List<Comment>>(cms);

		String cantDiasMostrables = commentModule.getCantDiasMostrables();

		String wherePorFecha = verHistorico ? "WHERE " + SITE + "= '"+site+"'" : "WHERE " + SITE + "= '"+site+"' AND  DATEDIFF(CURDATE(), FECHA) < " + cantDiasMostrables;

		queryBuilder.setSQLQuery("SELECT * FROM " + getCommentsTableName(cms) + " " + wherePorFecha);
		
		ResultSetProcessor<List<Comment>> proc = this.getComentariosListProcessor();
        
		return queryBuilder.execute(proc);
	}
	
	public List<Comment> getListComments(CmsObject cms, String site, String publication , String dateFrom, String dateTo, String state, String user , String newsPath, String text, int reportNumber, int pageNumber, int PageSize, String order ){
		return getListComments(cms,  site, publication, dateFrom, dateTo, state, user , newsPath, text, reportNumber, pageNumber, PageSize, order, false);
	}
	
	public List<Comment> getListComments(CmsObject cms, String site,String publication, String dateFrom, String dateTo, String state, String user , String newsPath, String text, int reportNumber, int pageNumber, int PageSize, String order, boolean abuseReport){

		    String cantDiasMostrables = commentModule.getCantDiasMostrables();
			String		   whereFecha = "";
			String		   whereState = "";
			String		 whereUsuario = "";
		    String       whereNoticia = "";
		    String           whereTxt = "";
		    String 		 whereReports = "";
		    
		    String		orderType = "";
		    if(abuseReport){
		    	orderType = " TFS_COMMENTS_REPORT.FECHA ASC ";
			}else{
				orderType = " FECHA ASC ";
			}

			if(order!=null) orderType = order;

		    // Filtro por fecha
			String fecha = "";
			if(abuseReport){
				fecha = "TFS_COMMENTS_REPORT.FECHA";
			}else{
				fecha = "FECHA";
			}
		    if(dateFrom==null && dateTo==null){ 
		    	if(abuseReport){
		    		orderType = "TFS_COMMENTS_REPORT.FECHA ASC";
		    	}		        
		    	
		    	whereFecha = " AND  DATEDIFF(CURDATE(), "+fecha+") < " + cantDiasMostrables +" ";
		    }
		    if(dateFrom!=null && dateTo==null)
		       whereFecha = " AND "+fecha+" >= '" +dateFrom+ "' ";
			if(dateFrom==null && dateTo!=null)
			   whereFecha = " AND "+fecha+" <= '" +dateTo+ "' ";
			if(dateFrom!=null && dateTo!=null)
			   whereFecha = " AND "+fecha+" >= '" +dateFrom+ "' AND "+fecha+" < '" +dateTo+ "' ";

			// Filtro por estado
			if(state!=null && state.contains(",")){
				String[] stateItems = state.split(",");
				whereState += " AND (";
				int index = 0;
				
				for (String stateValue : stateItems ){
					index++;
					if(state!=null) whereState += STATE + "='"+stateValue+"' ";
					
					if(hasMoreItems(index, stateItems.length)){
						whereState += " OR ";
        	    	}
				}
				
				whereState += ") ";
			}else{
				if(state!=null) whereState = " AND " + STATE + "='"+state+"' ";
			}
			
			// Filtro por cantidad de reportes
			if(reportNumber>-1) whereReports = " AND "+ CANT_REPORTS + ">='"+reportNumber+"' ";

			// Filtro por usuario
			if(user!=null) whereUsuario = " AND " + USERNAME + " like '%"+user+"%' ";

			// Filtro por noticia
			if(newsPath!=null) whereNoticia = " AND " + NOTICIA_URL + "='"+newsPath+"' ";

			// Filtro por txt
			if(text!=null) whereTxt = " AND " + TEXT + "like '%"+text+"%' ";


			QueryBuilder<List<Comment>> queryBuilder = new QueryBuilder<List<Comment>>(cms);
			
			if(abuseReport){

				
				queryBuilder.setSQLQuery("SELECT TFS_COMMENTS_REPORT.COMMENTID, TFS_COMMENTS_REPORT.ID, TFS_COMMENTS."+ID_COMMENT+",TFS_COMMENTS."+CANT_REPORTS+",TFS_COMMENTS."+FECHA+", TFS_COMMENTS."+NOTICIA_URL+", TFS_COMMENTS."+STATE+", TFS_COMMENTS."+USERNAME+", TFS_COMMENTS."+SITE+", TFS_COMMENTS."+TIPO_EDICION+", TFS_COMMENTS."+TIPO_EDICION_SHARED+", TFS_COMMENTS."+TEXT+", TFS_COMMENTS."+REMOTE_IP+", TFS_COMMENTS."+RESPUESTA_DE+" "
						   + " FROM " + getAbuseReportTableName() 
						   + " INNER JOIN CMS_ONLINE_STRUCTURE ON CMS_ONLINE_STRUCTURE.RESOURCE_PATH = CONCAT(TFS_COMMENTS_REPORT.SITIO,TFS_COMMENTS_REPORT.PATH) "
						   + " INNER JOIN TFS_COMMENTS ON TFS_COMMENTS.ID_COMMENT = TFS_COMMENTS_REPORT.COMMENTID"
						   + " WHERE TFS_COMMENTS."+SITE+"= '"+site+"' AND ( TFS_COMMENTS."+TIPO_EDICION+" = '"+publication+"' OR TFS_COMMENTS."+TIPO_EDICION_SHARED+" = '"+publication+"' ) " 
						   + whereFecha
						   + " GROUP BY TFS_COMMENTS_REPORT.COMMENTID, TFS_COMMENTS_REPORT.ID"
						   + " ORDER BY "
						   + orderType + " LIMIT " + PageSize + " OFFSET "
						   + (new Integer(pageNumber) - 1) * PageSize);
			
			}else{
				queryBuilder.setSQLQuery("SELECT * FROM " + getCommentsTableName(cms) + " INNER JOIN CMS_ONLINE_STRUCTURE ON  CMS_ONLINE_STRUCTURE.RESOURCE_PATH = CONCAT('/sites/',TFS_COMMENTS.SITE,TFS_COMMENTS.NOTICIA_URL)"
						   + " WHERE " + SITE + "= '"+site+"' AND ( TFS_COMMENTS."+TIPO_EDICION+" = '"+publication+"' OR TFS_COMMENTS."+TIPO_EDICION_SHARED+" = '"+publication+"' ) "
						   + whereFecha
						   + whereState
						   + whereUsuario
						   + whereNoticia
						   + whereTxt
						   + whereReports
						   + " ORDER BY "
						   + orderType + " LIMIT " + PageSize + " OFFSET "
						   + (new Integer(pageNumber) - 1) * PageSize);
			}
			
			CmsLog.getLog(this).info(queryBuilder.toString());

			ResultSetProcessor<List<Comment>> proc = this.getComentariosListProcessor();

			return queryBuilder.execute(proc);

		}
	
	public List<AbuseReport> getListAbuseReports(CmsObject cms, String dateFrom, String dateTo, String user , String newsPath, String text, int reportNumber, int pageNumber, int PageSize, String order ){

	    String cantDiasMostrables = commentModule.getCantDiasMostrables();
		String		   whereFecha = "";
		String		 whereUsuario = "";
	    String           whereTxt = "";
	    String 		 whereReports = "";

		String		orderType = " FECHA ASC ";
		
		if(order!=null) orderType = order;

	    // Filtro por fecha
	    if(dateFrom==null && dateTo==null) 
	       whereFecha = " DATEDIFF(CURDATE(), FECHA) < " + cantDiasMostrables +" "; //saqué el AND
	    if(dateFrom!=null && dateTo==null)
	       whereFecha = " AND FECHA >= '" +dateFrom+ "' ";
		if(dateFrom==null && dateTo!=null)
		   whereFecha = " AND FECHA <= '" +dateTo+ "' ";
		if(dateFrom!=null && dateTo!=null)
		   whereFecha = " AND FECHA >= '" +dateFrom+ "' AND FECHA < '" +dateTo+ "' ";

		// Filtro por cantidad de reportes
		if(reportNumber>-1) whereReports = " AND "+ CANT_REPORTS + ">='"+reportNumber+"' ";

		// Filtro por usuario
		if(user!=null) whereUsuario = " AND " + USERNAME + " like '%"+user+"%' ";
		
		// Filtro por txt
		if(text!=null) whereTxt = " AND " + TEXT + "like '%"+text+"%' ";


		QueryBuilder<List<AbuseReport>> queryBuilder = new QueryBuilder<List<AbuseReport>>(cms);
		
		queryBuilder.setSQLQuery("SELECT * FROM " + getAbuseReportTableName() + " WHERE " 
							   + whereFecha
							   + whereUsuario
							   + whereTxt
							   + whereReports
							   + " ORDER BY "
							   + orderType + " LIMIT " + PageSize + " OFFSET "
							   + (new Integer(pageNumber) - 1) * PageSize);
		
		CmsLog.getLog(this).info(queryBuilder.toString());

		ResultSetProcessor<List<AbuseReport>> proc = this.getAbuseReportsListProcessor();

		return queryBuilder.execute(proc);

	}
	
	public int getPublicationIdShared(CmsObject cms, int idComment) {
		QueryBuilder<Integer> queryBuilder = new QueryBuilder<Integer>(cms);
		queryBuilder.setSQLQuery("SELECT " + TIPO_EDICION_SHARED + " FROM " + getCommentsTableName(cms)
				+ " WHERE " + ID_COMMENT + " = ? ");

		queryBuilder.addParameter(idComment);

		ResultSetProcessor<Integer> proc = new ResultSetProcessor<Integer>() {

			private int tipoEdicionShared = 0;

			public void processTuple(ResultSet rs) {

				try {
					this.tipoEdicionShared = rs.getInt(1);
				}
				catch (SQLException e) {
					throw ProgramException.wrap(
							"error al intentar recuperar el id de la publicacion en la que se hizo el comentarios de la base", e);
				}
			}

			public Integer getResult() {
				return this.tipoEdicionShared;
			}
		};

		return queryBuilder.execute(proc);
	}
	
	private boolean hasMoreItems(int index, int size) {
		if(index != size){
			return true;
		}
		return false;
	}
		
}