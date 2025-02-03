package com.tfsla.diario.videoConverter.jsp;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsSystemInfo;
import org.opencms.workplace.CmsWorkplaceAction;

import com.tfsla.diario.auditActions.data.TfsAuditActionDAO;
import com.tfsla.diario.auditActions.model.TfsAuditAction;
import com.tfsla.diario.videoConverter.DefaultFFMPEGLocator;
import com.tfsla.diario.videoConverter.FFMPEGExecutor;
import com.tfsla.diario.videoConverter.FFMPEGLocator;
import com.tfsla.opencms.exceptions.ProgramException;
import com.tfsla.workflow.QueryBuilder;
import com.tfsla.workflow.ResultSetProcessor;

public class TfsEnconderQueue {
	
	private String siteName;
    private String publication;
    private CmsObject m_cms;
    private PageContext m_context;
    private HttpServletRequest m_req;
    private HttpServletResponse m_res;
    
    private static final String TFS_ENCODER_QUEUE = "TFS_ENCODER_QUEUE";
    
    private static final String DB_ID          = "ID";
    private static final String DB_SOURCE      = "SOURCE";
    private static final String DB_FORMATS     = "FORMATS";
    private static final String DB_TYPE        = "TYPE";
    private static final String DB_PUBLICATION = "PUBLICATION";
    private static final String DB_SITENAME    = "SITENAME";
    private static final String DB_USERNAME    = "USERNAME";
    private static final String DB_FLAG        = "FLAG";
    
    public TfsEnconderQueue(CmsObject cms){
    	m_cms = cms;
    }
    
    public TfsEnconderQueue(CmsObject cms, PageContext context, HttpServletRequest req, HttpServletResponse res){
    	m_cms = cms;
    	m_context = context;
    	m_req = req;
    	m_res = res;
    	
    }
	
	public String checkEncoderQueue(){
		
		int inProcess = countVideosOnQueue(true);
		int   pending = countVideosOnQueue(false);
		
		HashMap <String, String> source = new HashMap<String, String>();
		String              logFilePath = "";
		String 			  publicationId = "";
		String      		   siteName = "";
		String      			formats = "";
		String 				  sourceVFS = "";
		String  				idQueue = "";
		String 					   type = "";
		String         idVideoInProcess = "";
		String                 username = "";
		
		TfsAuditAction      action = new TfsAuditAction();
		TfsAuditActionDAO auditDAO = new TfsAuditActionDAO();
		String           actionMsg = "";
		
		if(inProcess >0){
			
			     source = getEncoderInfo(true);
			logFilePath = getLogFilePath(source.get("source"));
		  publicationId = source.get("publication");
			   siteName = source.get("sitename");
			    formats = source.get("formats");
			  sourceVFS = source.get("source");
			    idQueue = source.get("id");
			       type = source.get("type");
			   username = source.get("username");
			    
		    int timeOut = getMinutesTimeout(publicationId,siteName);	    
			
			File temp = new File(logFilePath);
			int difM = timeOut+1;

			if (temp.exists()){
				long             ms = temp.lastModified();
				
				Date    currentDate = new java.util.Date();  
				long      currentms = currentDate.getTime();
				
				long dif = currentms - ms;
				difM = (int) ((dif / 1000) / 60);
				
				if(difM > timeOut){
					FFMPEGLocator locator = new DefaultFFMPEGLocator();
					FFMPEGExecutor ffmpeg = locator.createExecutor();
								   ffmpeg.destroy();
					
					deleteFromQueueDB(idQueue);		
					
					CmsUser cmsUserToNotify = null;
				    String     userToNotify = username;
				    String      languageMsg = "EN";
				    		  
				    try{
				    	  cmsUserToNotify = m_cms.readUser(username);
				    	     userToNotify = cmsUserToNotify.getFullName();
				    }catch(CmsException e){
				    	  CmsLog.getLog(this).error("Error obteniendo usuario para enviar notificaciones de conversion de videos "+e.getMessage());
				    }
				      
				    if(cmsUserToNotify!=null){
				    	 String prefLanguage = (String)cmsUserToNotify.getAdditionalInfo("USERPREFERENCES_workplace-startupsettingslocale");
				    	  
				    	 if(prefLanguage!=null){
				    		  if(prefLanguage.toUpperCase().equals("ES"))
				    			  languageMsg = "ES";
				    	 }
				    }
					
					actionMsg = userToNotify+ " an error occurred encodig the video "+sourceVFS+" with the formats "+formats;
					
					if(languageMsg.equals("ES")){
				           actionMsg = userToNotify +" el proceso de conversión del video "+sourceVFS+" a los formatos "+formats+" terminó con error";
				    }
					
					action.setActionId(TfsAuditAction.ACTION_VIDEO_ENCODER);
					action.setTimeStamp(new Date());
					action.setPublicacion(publicationId);
					action.setSitio(siteName+"/");
					
					try {
						
						action.setUserName(CmsWorkplaceAction.getInstance().getCmsAdminObject().getRequestContext().currentUser().getName());
						action.setDescription(actionMsg);

					
						auditDAO.insertUserAuditEvent(action);
						auditDAO.insertNotificationAuditEvent(action.getEventId(),action.getTimeStamp(),username);

					} catch (Exception e) {
						CmsLog.getLog(this).error("Error registrando el evento "+e.getMessage());
					}
					
					checkEncoderQueue();
				}else{
					idVideoInProcess = idQueue;
					CmsLog.getLog(this).error("No genero el archivo de conversion de video " + sourceVFS);
				}
			}
			if(difM > timeOut){
				deleteOnProcessFromQueueDB();
				CmsLog.getLog(this).info("Se borra registro en proceso de conversion para el video: " + sourceVFS);
			}
		}
		
		if(inProcess == 0 && pending >0){

			     source = getEncoderInfo(false); 
		  publicationId = source.get("publication");
			   siteName = source.get("sitename");
				formats = source.get("formats");
			  sourceVFS = source.get("source");
			    idQueue = source.get("id");
				   type = source.get("type");
			   username = source.get("username");
			   
			   CmsUser cmsUserToNotify = null;
			    String      languageMsg = "EN";
			    		  
			    try{
			    	  cmsUserToNotify = m_cms.readUser(username);
			    }catch(CmsException e){
			    	  CmsLog.getLog(this).error("Error obteniendo usuario para enviar notificaciones de conversion de videos "+e.getMessage());
			    }
			      
			    if(cmsUserToNotify!=null){
			    	 String prefLanguage = (String)cmsUserToNotify.getAdditionalInfo("USERPREFERENCES_workplace-startupsettingslocale");
			    	  
			    	 if(prefLanguage!=null){
			    		  if(prefLanguage.toUpperCase().equals("ES"))
			    			  languageMsg = "ES";
			    	 }
			    }
				   
			actionMsg = "Starting the encoder of "+sourceVFS+" to the formats "+formats;
			
			if(languageMsg.equals("ES")){
		           actionMsg = "Se inició el proceso de conversión de "+sourceVFS+" a los formatos "+formats;
		    }
					
			action.setActionId(TfsAuditAction.ACTION_VIDEO_ENCODER);
			action.setTimeStamp(new Date());
			action.setPublicacion(publicationId);
			action.setSitio(siteName+"/");
					
			try {

				action.setUserName(CmsWorkplaceAction.getInstance().getCmsAdminObject().getRequestContext().currentUser().getName());
				action.setDescription(actionMsg);

				auditDAO.insertUserAuditEvent(action);
			} catch (Exception e) {
				CmsLog.getLog(this).error("Error registrando el evento "+e.getMessage());
			}	   
			    
			sendNextConvert(idQueue, sourceVFS, formats, type , publicationId, siteName, username);
			idVideoInProcess = idQueue;
			
		} 
		
		return idVideoInProcess;
		
	}
	
	
	private int countVideosOnQueue(boolean onProcess){
		
		int count = 0;
		
		QueryBuilder<Integer> queryBuilder = new QueryBuilder<Integer>(m_cms);
		
		queryBuilder.setSQLQuery("SELECT count("+DB_ID+") from "+TFS_ENCODER_QUEUE+" WHERE "+DB_FLAG+"=? ");
		
		if(onProcess){
		   queryBuilder.addParameter(1);
		}else{
		   queryBuilder.addParameter(0);
		}
		
		ResultSetProcessor<Integer> proc = new ResultSetProcessor<Integer>() {

			private int count = 0;

			public void processTuple(ResultSet rs) {

				try {
					this.count = rs.getInt(1);
				}
				catch (SQLException e) {
					throw ProgramException.wrap(
							"error al intentar recuperar la cantidad de videos en cola de la base", e);
				}
			}

			public Integer getResult() {
				return this.count;
			}
		};
        
		count = queryBuilder.execute(proc);
		
		return count;
		
	}
	
	public int idVideoOnProcess(){
		
		int id = 0;
		
		QueryBuilder<Integer> queryBuilder = new QueryBuilder<Integer>(m_cms);
		
		queryBuilder.setSQLQuery("SELECT "+DB_ID+" from "+TFS_ENCODER_QUEUE+" WHERE "+DB_FLAG+"=? ");
		queryBuilder.addParameter(1);
		
		ResultSetProcessor<Integer> proc = new ResultSetProcessor<Integer>() {

			private int id = 0;

			public void processTuple(ResultSet rs) {

				try {
					this.id = rs.getInt(1);
				}
				catch (SQLException e) {
					throw ProgramException.wrap(
							"error al intentar recuperar el id del video en proceso en cola de la base", e);
				}
			}

			public Integer getResult() {
				return this.id;
			}
		};
        
		id = queryBuilder.execute(proc);
		
		return id;
		
	}
	
	public int idNextVideoOnProcess(){
		
		int id = 0;
		
		QueryBuilder<Integer> queryBuilder = new QueryBuilder<Integer>(m_cms);
		
		queryBuilder.setSQLQuery("SELECT "+DB_ID+" from "+TFS_ENCODER_QUEUE+" WHERE "+DB_FLAG+"=? ORDER BY "+DB_ID+" limit 1 ");
		queryBuilder.addParameter(0);
		
		ResultSetProcessor<Integer> proc = new ResultSetProcessor<Integer>() {

			private int id = 0;

			public void processTuple(ResultSet rs) {

				try {
					this.id = rs.getInt(1);
				}
				catch (SQLException e) {
					throw ProgramException.wrap(
							"error al intentar recuperar el id del proximo video en proceso en cola de la base", e);
				}
			}

			public Integer getResult() {
				return this.id;
			}
		};
        
		id = queryBuilder.execute(proc);
		
		return id;
		
	}
	
	private HashMap<String, String> getEncoderInfo(boolean onProcess){
		
		HashMap <String, String> source = new HashMap<String, String>();
		
		QueryBuilder<HashMap<String, String>> queryBuilder = new QueryBuilder<HashMap<String, String>>(m_cms);
		 
		queryBuilder.setSQLQuery("SELECT * from "+TFS_ENCODER_QUEUE+" WHERE "+DB_FLAG+"=? ORDER by "+DB_ID+" desc limit 1");
		
		if(onProcess){
			   queryBuilder.addParameter(1);
		}else{
			   queryBuilder.addParameter(0);
		}
		
		
		ResultSetProcessor<HashMap<String, String>> proc = new ResultSetProcessor<HashMap<String, String>>() {

			private HashMap <String, String> sources = new HashMap<String, String>();

			public void processTuple(ResultSet rs) {

				try {
					this.sources.put("id", rs.getString(DB_ID));
					this.sources.put("source", rs.getString(DB_SOURCE));
					this.sources.put("formats", rs.getString(DB_FORMATS));
					this.sources.put("type", rs.getString(DB_TYPE));
					this.sources.put("publication",String.valueOf(rs.getInt(DB_PUBLICATION)));
					this.sources.put("sitename", rs.getString(DB_SITENAME));
					this.sources.put("username", rs.getString(DB_USERNAME));
					this.sources.put("flag", String.valueOf(rs.getInt(DB_FLAG)));
				}
				catch (SQLException e) {
					throw ProgramException.wrap(
							"error al intentar recuperar la info del video en cola de la base", e);
				}
			}

			public HashMap <String, String> getResult() {
				return this.sources;
			}
		};
		
		source = queryBuilder.execute(proc);
		
		//se agrega para cuando inserta en null el source
		if (source.get("source") == null) {
			deleteFromQueueByPathNull();
			CmsLog.getLog(this).debug("Se borra el registro ingresado como null");
			//source = getEncoderInfo(onProcess);
		}
		
		return source;
	}
	
	private void deleteFromQueueByPathNull() {
		QueryBuilder queryBuilder = new QueryBuilder(m_cms);
		queryBuilder.setSQLQuery("DELETE FROM "+TFS_ENCODER_QUEUE+" WHERE "+DB_SOURCE+" is null;");
		
		queryBuilder.execute();	
	}

	public List<String> getEncoderInfoBySource(String sourceVFS){
		
		List<String> source = new ArrayList<String>();
		
		QueryBuilder <List<String>> queryBuilder = new QueryBuilder<List<String>>(m_cms);
		
		queryBuilder.setSQLQuery("SELECT "+DB_FORMATS+" from "+TFS_ENCODER_QUEUE+" WHERE "+DB_SOURCE+"=? ");
		
		queryBuilder.addParameter(sourceVFS);
		
		ResultSetProcessor<List<String>> proc = new ResultSetProcessor<List<String>>() {

			private List<String> sources = new ArrayList<String>();

			public void processTuple(ResultSet rs) {

				try {
					this.sources.add(rs.getString(DB_FORMATS));
				}
				catch (SQLException e) {
					throw ProgramException.wrap(
							"error al intentar verificar si un video ya tiene cargadas conversiones en la cola", e);
				}
			}

			public List<String> getResult() {
				return this.sources;
			}
		};
		
		source = queryBuilder.execute(proc);
		
		return source;
	}
	
	public int getQueueStatusByPath(String path){
		
		int flag = -1;
		
		QueryBuilder<Integer> queryBuilder = new QueryBuilder<Integer>(m_cms);
		queryBuilder.setSQLQuery("SELECT "+DB_FLAG+" from "+TFS_ENCODER_QUEUE+" WHERE "+DB_SOURCE+"=? ");
		queryBuilder.addParameter(path);
		
		ResultSetProcessor<Integer> proc = new ResultSetProcessor<Integer>() {

			private int flag = -1;

			public void processTuple(ResultSet rs) {

				try {
					this.flag = rs.getInt(DB_FLAG);
				}
				catch (SQLException e) {
					throw ProgramException.wrap(
							"error al intentar recuperar el estado del video en cola de la base", e);
				}
			}

			public Integer getResult() {
				return this.flag;
			}
		};
        
		flag = queryBuilder.execute(proc);
		
		return flag;
	}
	
	private void deleteFromQueueDB(String id){
		QueryBuilder queryBuilder = new QueryBuilder(m_cms);
		queryBuilder.setSQLQuery("DELETE FROM "+TFS_ENCODER_QUEUE+" WHERE "+DB_ID+"=? ");
		queryBuilder.addParameter(id);
		
		queryBuilder.execute();
	}
	
	public void deleteFromQueueByPath(String source){
		QueryBuilder queryBuilder = new QueryBuilder(m_cms);
		queryBuilder.setSQLQuery("DELETE FROM "+TFS_ENCODER_QUEUE+" WHERE "+DB_SOURCE+"=? ");
		queryBuilder.addParameter(source);
		
		queryBuilder.execute();
	}
	
	public void deleteOnProcessFromQueueDB(){
		QueryBuilder queryBuilder = new QueryBuilder(m_cms);
		queryBuilder.setSQLQuery("DELETE FROM "+TFS_ENCODER_QUEUE+" WHERE "+DB_FLAG+"=? ");
		queryBuilder.addParameter(1);
		
		queryBuilder.execute();
	}
	
	private void updateQueueDB(String id){
		QueryBuilder queryBuilder = new QueryBuilder(m_cms);
		queryBuilder.setSQLQuery("UPDATE "+TFS_ENCODER_QUEUE+" SET "+DB_FLAG+" =? WHERE "+DB_ID+"=? ");
		
		queryBuilder.addParameter(1);
		queryBuilder.addParameter(id);

		queryBuilder.execute();
	}
	
	public int insertQueueDB(String source,String formats,String type,String publication,String sitename, String username ){
		QueryBuilder queryBuilder = new QueryBuilder(m_cms);
		queryBuilder.setSQLQuery("INSERT INTO "+TFS_ENCODER_QUEUE+" ("+DB_SOURCE+","+DB_FORMATS+","+DB_TYPE+","+DB_PUBLICATION+","+DB_SITENAME+","+DB_USERNAME+","+DB_FLAG+") " +
				"VALUES (?,?,?,?,?,?,?);");
       
		queryBuilder.addParameter(source);
		queryBuilder.addParameter(formats);
		queryBuilder.addParameter(type);
		queryBuilder.addParameter( Integer.valueOf(publication) );
		queryBuilder.addParameter(sitename);
		queryBuilder.addParameter(username);
		queryBuilder.addParameter(0);
		
		queryBuilder.execute();
		
		int lastId = 0;
		
		QueryBuilder<Integer> queryBuilderS = new QueryBuilder(m_cms);
		queryBuilderS.setSQLQuery("SELECT "+DB_ID+" from "+TFS_ENCODER_QUEUE+" ORDER by "+DB_ID+" desc limit 1");
		
		ResultSetProcessor<Integer> proc = new ResultSetProcessor<Integer>() {

			private int lastId = 0;

			public void processTuple(ResultSet rs) {

				try {
					this.lastId = rs.getInt(1);
				}
				catch (SQLException e) {
					CmsLog.getLog(this).error("error al intentar recuperar el id del videos en cola de la base "+e.getMessage());
				}
			}

			public Integer getResult() {
				return this.lastId;
			}
		};
        
		lastId = (Integer) queryBuilderS.execute(proc);
		
		return lastId;
	}
	
	private static String getLogFilePath(String VFSsource){
		
		String logFileName = VFSsource.substring(VFSsource.lastIndexOf("/")+1);
		       logFileName = logFileName + ".log";
		       
		String logFolderPath = null;
				
		CmsSystemInfo cmsInfo = new CmsSystemInfo();
			    logFolderPath = cmsInfo.getLogFileRfsPath();
				
		if(logFolderPath!=null){
			logFolderPath = logFolderPath.substring(0,logFolderPath.lastIndexOf("/"));
		}
		
		String logFilePath = logFolderPath+"/cmsMediosVideoConverter/"+logFileName;
		
		return logFilePath;
	}
	
	private int getMinutesTimeout(String publicationId,String site)
	{
    	siteName = site;
    	
    	publication = publicationId;
    	String moduleConfigName = "videoConvert";
    	
		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
		
		String timeOutStr = config.getParam(siteName, publication, moduleConfigName, "timeoutMin","5");
		
		int timeOut = Integer.valueOf(timeOutStr);
		
		return timeOut;
	}
	
	private void sendNextConvert(String id, String source, String formats, String type ,String publication, String site, String username){
		
		try {
			
			TfsVideosAdmin videosEncoder;
			
			if(m_req!=null)
				videosEncoder = new TfsVideosAdmin(m_context, m_req, m_res);
			else
				videosEncoder = new TfsVideosAdmin(m_cms, site, publication);
			
			updateQueueDB(id);
			
			    videosEncoder.convert(source, formats, type,username);
			
		} catch (Exception e) {
			CmsLog.getLog(this).error("Error al obtener proximo a convertir" + e.getMessage());
		}
		
	}
}
