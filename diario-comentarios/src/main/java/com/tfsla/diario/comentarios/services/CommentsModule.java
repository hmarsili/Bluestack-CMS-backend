package com.tfsla.diario.comentarios.services;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringEscapeUtils;
import org.htmlparser.util.ParserException;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.relations.CmsRelationType;
import org.opencms.util.CmsHtmlExtractor;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.content.CmsXmlContentValueSequence;
import org.opencms.xml.types.I_CmsXmlContentValue;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.tfsla.diario.comentarios.data.CommentPersistor;
import com.tfsla.diario.comentarios.model.AbuseReport;
import com.tfsla.diario.comentarios.model.Comment;
import com.tfsla.diario.ediciones.model.Edicion;
import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.EdicionService;
import com.tfsla.diario.ediciones.services.MailSettingsService;
import com.tfsla.diario.ediciones.services.TipoEdicionBaseService;
import com.tfsla.diario.ediciones.services.TipoEdicionService;
import com.tfsla.event.I_TfsEventListener;
import com.tfsla.opencms.exceptions.ProgramException;
import com.tfsla.opencms.mail.MailSender;
import com.tfsla.opencms.mail.SimpleMail;
import com.tfsla.opencms.webusers.RegistrationModule;
import com.tfsla.opencms.webusers.TfsUserHelper;
import com.tfsla.opencmsdev.modules.AbstractCmsModule;

public class CommentsModule extends AbstractCmsModule {

    private static Map<String, CommentsModule> instances = new HashMap<String, CommentsModule>();
   
    private static String module = "comments";
    
    private String siteName = null;
    private String publication = null;
    
	private CommentsModule(String siteName, String publication) {
		this.siteName = siteName;
		this.publication = publication;
	}
	
	private String getSiteName(CmsObject cms)
	{
		String siteName = OpenCms.getSiteManager().getCurrentSite(cms).getTitle();
		siteName = siteName.replaceFirst("/sites/", "");
        siteName = siteName.replaceFirst("/site/", "");
        
		return siteName.substring(0,siteName.length() -1);
	}
	
	

    public static CommentsModule getInstance(String siteName, String publication) {
    	String id = siteName + "||" + publication;
    	
    	CommentsModule instance = instances.get(id);
    	
    	if (instance == null) {
	    	instance = new CommentsModule(siteName, publication);

	    	instances.put(id, instance);
    	}
        return instance;
    }
   

    public static CommentsModule getInstance(CmsObject cms)
    {
    	
    	String siteName = OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot();
    	String publication = "0";
     	TipoEdicionBaseService tService = new TipoEdicionBaseService();
    	try {
			TipoEdicion tEdicion = tService.obtenerTipoEdicion(cms, cms.getRequestContext().getUri());			
			if (tEdicion!=null)
				publication = "" + tEdicion.getId();
		} catch (Exception e) {
			e.printStackTrace();
		}
    	

    	return getInstance(siteName, publication);
    }
    
    public static CommentsModule getInstance(CmsObject cms,TipoEdicion cPublication)
    {
    	
    	String siteName = OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot();
    	int publicationId = cPublication.getId();
    	
    	return getInstance(siteName, "" + publicationId);
    }

	// ******************************
	// ** template methods
	// ******************************
	protected CommentPersistor getCommentPersitor() {
		return new CommentPersistor(this);
	}

	// ******************************
	// ** Common interfaces
	// ******************************
	public Boolean getModerateComments() {
		return CmsMedios.getInstance().getCmsParaMediosConfiguration().getBooleanParam(siteName, publication, module, "moderateComments", false);
	}

	public Boolean getGuestComments() {
		return CmsMedios.getInstance().getCmsParaMediosConfiguration().getBooleanParam(siteName, publication, module, "guestCancomment", false);
	}
	
	public Boolean showCommentRevision(){
		return CmsMedios.getInstance().getCmsParaMediosConfiguration().getBooleanParam(siteName, publication, module, "showCommentsInRevision", false);
	}
	
	public Boolean showCommentRejected(){
		return CmsMedios.getInstance().getCmsParaMediosConfiguration().getBooleanParam(siteName, publication, module, "showRejectedComments", false);
	}
	
	public int getCantReportAbuseForRevision(){
		return CmsMedios.getInstance().getCmsParaMediosConfiguration().getIntegerParam(siteName, publication, module, "maxAbuseReportForRevision", -1);
	}
	
	public  String CronPremoderateComments(){
		return CmsMedios.getInstance().getCmsParaMediosConfiguration().getParam(siteName, publication, module, "premoderatedCron","-01:00:00");
	}

	// este metodo no se esta usando ahora, pero a futuro nunca se sabe asi que lo dejo. ver tambien
	// commentPersitor.getCommentTableName
	public boolean adminOfflineComments() {
		return CmsMedios.getInstance().getCmsParaMediosConfiguration().getBooleanParam(siteName, publication, module, "administrateOfflineComments", false);
	}

	public String getCantDiasMostrables() {
		return CmsMedios.getInstance().getCmsParaMediosConfiguration().getParam(siteName, publication, module, "commentsDisplayableDays", "1");
	}

	public int getAdminPageSize(){
		return CmsMedios.getInstance().getCmsParaMediosConfiguration().getIntegerParam(siteName, publication, module, "pageSizeAdminList", 20);
	}

	public int getFirstPageSize(){
		return CmsMedios.getInstance().getCmsParaMediosConfiguration().getIntegerParam(siteName, publication, module, "pageSizeFirst", 20);
	}
	
	public Boolean getCommentNotifications(){
		return CmsMedios.getInstance().getCmsParaMediosConfiguration().getBooleanParam(siteName, publication, module, "enableCommentsNotifications", false);

	}
	
	public String getStringNotificationPost(){
		return CmsMedios.getInstance().getCmsParaMediosConfiguration().getParam(siteName, publication, module, "commentNotificationPost");
	}
	
	public String getStringNotificationReply(){
		return CmsMedios.getInstance().getCmsParaMediosConfiguration().getParam(siteName, publication, module, "commentNotificationReply");
	}
	
	public int getMinAnswers(){
		return CmsMedios.getInstance().getCmsParaMediosConfiguration().getIntegerParam(siteName, publication, module, "minAnswersSize", 1);
	}

	/**
	 * Obtiene una lista de todos los comentarios de un autor determinado.
	 * @param cms
	 * @param userName
	 * @param pageNumber
	 * @return List<Comment>
	 */
	public List<Comment> getCommentsByAuthor(CmsObject cms, String userName, String pageNumber) {
		String siteName = getSiteName(cms); 
		return this.getCommentPersitor().getCommentsByAuthor(cms, userName, pageNumber,siteName);
	}

	/**
	 * Retorna la cantidad de comentarios de un usuario.
	 * @param cms
	 * @param userName
	 * @return int
	 */
	public int getCommentsByAuthorCount(CmsObject cms, String userName) {
		String siteName = getSiteName(cms); 
		return this.getCommentPersitor().getCommentsByAuthorCount(cms, userName, siteName);
	}

	// ******************************
	// ** comments module web interface
	// ******************************
	/**
	 * Obtiene los comentarios de una noticia.
	 * @param cms
	 * @param noticiaURL
	 * @param pageNumber
	 * @return List<Comment>
	 */
	public List<Comment> getComments(CmsObject cms, String noticiaURL, String pageNumber) {
		String siteName = getSiteName(cms); 
		
		String noticiaPrincipal = noticiaURL;
		String masterNews = getMasterNews(cms,noticiaURL);
		
		if(masterNews!=null && masterNews != "" ) noticiaPrincipal = masterNews;
		
		return this.getCommentPersitor().getComments(cms, noticiaPrincipal, pageNumber,siteName);
	}

	/**
	 * Retorna la cantidad de comentarios de una noticia.
	 * @param cms
	 * @param noticiaURL
	 * @return int
	 */
	public int getCommentsCount(CmsObject cms, String noticiaURL) {
		String siteName = getSiteName(cms); 
		
		String noticiaPrincipal = noticiaURL;
		String masterNews = getMasterNews(cms,noticiaURL);
		
		if(masterNews!=null && masterNews != "" ) noticiaPrincipal = masterNews;
		
		return this.getCommentPersitor().getCommentsCount(cms, noticiaPrincipal, siteName);
	}

	/**
	 * Retorna la cantidad de respuestas a todos a los comentarios de una noticia.
	 * @param cms
	 * @param noticiaURL
	 * @return int
	 */
	public int getCommentsAnswersCount(CmsObject cms, String noticiaURL) {
		String siteName = getSiteName(cms); 
		
		String noticiaPrincipal = noticiaURL;
		String masterNews = getMasterNews(cms,noticiaURL);
		
		if(masterNews!=null && masterNews != "" ) noticiaPrincipal = masterNews;
		
		return this.getCommentPersitor().getCommentsAnswersCount(cms, noticiaPrincipal, siteName);
	}
	
	/**
	 * Retorna la cantidad de comentarios del primer nivel de una noticia.
	 * @param cms
	 * @param noticiaURL
	 * @return int
	 */
	public int getCommentsCountByParent(CmsObject cms, String noticiaURL, int parendID) {
		String siteName = getSiteName(cms); 
		
		String noticiaPrincipal = noticiaURL;
		String masterNews = getMasterNews(cms,noticiaURL);
		
		if(masterNews!=null && masterNews != "" ) noticiaPrincipal = masterNews;
		
		return this.getCommentPersitor().getCommentsCountByParent(cms, noticiaPrincipal ,parendID,siteName);
	}

	public int getAllCommentsCount(CmsObject cms){
		
		String siteName = getSiteName(cms);
		
		return this.getCommentPersitor().getAllCommentsCount(cms, siteName);
	}

	public int getPagesCount(int commentsCount) {
		if (commentsCount<=this.getFirstPageSize())
			return 1;
		return (commentsCount - this.getFirstPageSize()) / this.getQueryPageSize() + 2;
	}

	public int getQueryPageSize() {
		return CmsMedios.getInstance().getCmsParaMediosConfiguration().getIntegerParam(siteName, publication, module, "pageSize", 20);
	}

	public String getNotificationPost() {	
	 return CmsMedios.getInstance().getCmsParaMediosConfiguration().getParam(siteName, publication, module, "commentNotificationPost");
	}
	
	public String getNotificationReply() {
		return CmsMedios.getInstance().getCmsParaMediosConfiguration().getParam(siteName, publication, module, "commentNotificationReply");
	}
	
	public String getAbuseMailDestination() {
		return CmsMedios.getInstance().getCmsParaMediosConfiguration().getParam(siteName, publication, module, "reportAbuseMail");
	}
	
	 /**
	 * Registra un nuevo comentario.
	 * @param cms
	 * @param userName
	 * @param noticiaURL
	 * @param textoComentario
	 * @param replyComment
	 * @param tipoNoticia
	 */
	public void newComment(CmsObject cms, String userName, String noticiaURL, String textoComentario, int replyComment, boolean tipoNoticia) {
		// no estan mas soportados los comentarios en el offline. ver tambien
		// commentPersistor.getCommentsTableName().
		ProgramException.assertTrue("Solo pueden darse de alta comentarios en el ONLINE", cms
				.getRequestContext().currentProject().isOnlineProject());

		int commentTextMaxSize = this.getCommentTextMaxSize();
		if (textoComentario.length() > commentTextMaxSize) {
			textoComentario = textoComentario.substring(0, commentTextMaxSize - 1);
		}

		textoComentario = textoComentario.replaceAll("\\&[^;]*;","");
		textoComentario = textoComentario.replaceAll("\\<[^>]*>","");
		
		String noticiaPrincipal = noticiaURL;
		String masterNews = getMasterNews(cms,noticiaURL);
		
		if(masterNews!=null && masterNews != "" ) noticiaPrincipal = masterNews;
		
		Comment c = new Comment();
		c.setCantReports(0);
		c.setDate(new Date());
		c.setNoticiaURL(noticiaPrincipal);

		TipoEdicionService tService = new TipoEdicionService();

		int tEd = 0;
		int tEdS = 0;
		int ed = 0;
		
		if(tipoNoticia){
		  try {
			TipoEdicion tEdicion = tService.obtenerTipoEdicion(cms, noticiaURL);
			if (tEdicion==null)
				throw new Exception("No se encontro el tipo de edicion de la noticia \"" + noticiaURL + "\"");
			tEd = tEdicion.getId();
			
			if(!noticiaPrincipal.equals(noticiaURL)){
				TipoEdicion tEdicionShared = tService.obtenerTipoEdicion(cms, noticiaURL);
				if (tEdicionShared==null)
					throw new Exception("No se encontro el tipo de edicion de la noticia \"" + noticiaURL + "\"");
				tEdS = tEdicionShared.getId();
			}else
				tEdS = tEd;
			
			if (!tEdicion.isOnline()) {
				EdicionService eService = new EdicionService();
				Edicion edicion = eService.obtenerEdicionImpresa(cms, noticiaURL);
				if (edicion!=null)
					ed = edicion.getNumero();
			}
		  } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		  }
		}
		
		String siteName = getSiteName(cms);
		
		c.setSite(siteName);
		c.setTipoEdicion(tEd);
		c.setTipoEdicionShared(tEdS);
		c.setEdicion(ed);
		
		if (this.getModerateComments()) {
			c.setState(Comment.PENDIENTE_STATE);
		}
		else {
			c.setState(Comment.ACEPTADO_STATE);
		}
		
		String remoteAddr = getRemoteAddress(cms.getRequestContext().getRemoteAddress());
		
		c.setText(textoComentario);
		c.setUser(userName);
		c.setRemoteIP(remoteAddr);
		c.setReplyOf(replyComment);
		
		this.getCommentPersitor().save(cms, c);
		
		if(this.getCommentNotifications()){
			sendNotificationPostComment(cms, noticiaURL,userName,textoComentario );
			
			if(replyComment!=0){
				sendNotificationReplyComment(cms,noticiaURL, userName, textoComentario, replyComment);
			}
		}
		
		Map<String, Comment> eventData = new HashMap<String, Comment>();
        eventData.put(I_TfsEventListener.KEY_COMMENT, c);
        OpenCms.fireCmsEvent(new CmsEvent(I_TfsEventListener.EVENT_COMMENT_NEW, eventData));
		if (!this.getModerateComments())
	        OpenCms.fireCmsEvent(new CmsEvent(I_TfsEventListener.EVENT_COMMENT_ACEPTED, eventData));

	}
	
	public void sendNotificationPostComment(CmsObject cms, String NoticiaURL, String UserComentario, String TxtComentario){

		   if(this.getNotificationPost()!=null && !this.getNotificationPost().equals("")){
			   // Con la url de la nota traigo el autor
			   try {
				   CmsFile file = cms.readFile(NoticiaURL);
				   CmsXmlContent xmlContent = CmsXmlContentFactory.unmarshal(cms, file);
				   xmlContent.setAutoCorrectionEnabled(true); // now correct the XML 
				   //xmlContent.correctXmlStructure(cms);
				   
				   String absolutePath = xmlContent.getFile().getRootPath();
				   List<Locale> locales = xmlContent.getLocales();
			        
			        if (locales.size() == 0) {
			            locales = OpenCms.getLocaleManager().getDefaultLocales(cms, absolutePath);
			        }
			        Locale locale = OpenCms.getLocaleManager().getBestMatchingLocale(CmsLocaleManager.getLocale(""),
			        	    OpenCms.getLocaleManager().getDefaultLocales(cms, absolutePath),locales);
				   
			        List<I_CmsXmlContentValue> autores = xmlContent.getValues("autor",locale);
			        I_CmsXmlContentValue tituloXml = xmlContent.getValue("titulo",locale);
			        String TituloNoticia = "";
			        
			        try {
						TituloNoticia = CmsHtmlExtractor.extractText(tituloXml.getStringValue(cms),xmlContent.getEncoding());
					} catch (ParserException e1) {
						CmsLog.getLog(this).error("ERROR al intentar enviar la notificación al autor: ", e1);
					} catch (UnsupportedEncodingException e1) {
						CmsLog.getLog(this).error("ERROR al intentar enviar la notificación al autor: ",e1);
					}
			        
			        for(int j=1; j<=autores.size();j++){
			            String campo = "autor["+j+"]/internalUser[1]";
			            
			            try {
			                I_CmsXmlContentValue value = xmlContent.getValue(campo,locale);
			        	    String internalUser = CmsHtmlExtractor.extractText(value.getStringValue(cms),xmlContent.getEncoding());            
			                
			        	    if(internalUser!=null && !internalUser.equals("")){
			        	    	
			        	      // Traigo los datos del usuario para saber si tiene habilitado el envio de notificaciones
			        	       String autorMail = "";
			        	       CmsUser currentUser = null;
			        	       
			        	       if(internalUser.indexOf("/")>-1){
			        	    	    String[] arrayUser = internalUser.split("/");
			        	    	    autorMail = arrayUser[1];
			        	    	    
			        	    	    currentUser = RegistrationModule.getInstance(cms).retrieveUser(cms, autorMail);
			        	    	    
			        	    	  }else{
			        	    		  currentUser  = cms.readUser(internalUser);
			        	    		  autorMail = currentUser.getEmail();
			        	    	  }
			        	       
			      			   TfsUserHelper tfsUser = new TfsUserHelper(currentUser);
			      			   Boolean UserNotificationPost = Boolean.valueOf(tfsUser.getValorAdicional(this.getNotificationPost()));
			      			   
			        	       if(UserNotificationPost){
			        	    	 String apodo = tfsUser.getValorAdicional("APODO");
			        	    	 
			        	    	 if(apodo==null || apodo.equals("")){
			        	    		 apodo = tfsUser.getName();
			        	    	 }
			        	    	 
			        	    	 if(apodo.indexOf("/")>-1){
				        	    	    String[] arrayApodo = apodo.split("/");
				        	    	    apodo = arrayApodo[1];
				        	     }
			        	    	 
			        	    	 if(!tfsUser.getName().equals(UserComentario)){
			        	    	 
			        			 SimpleMail NotificationMail = new SimpleMail();

			        			 String MailDestination = autorMail;

			        			 NotificationMail.addTo(MailDestination);
			        			 
			        			 String fileNameContents = getMailFile("postCommentNotificationMail", NoticiaURL,0,cms);
			        			 String     fileContents = readFileContents(fileNameContents);
			        			 
			        			 if(UserComentario.indexOf("/")>-1){
				        	    	    String[] arrayUserComentario = UserComentario.split("/");
				        	    	    UserComentario = arrayUserComentario[1];
				        	     }

			        			 byte[] apodoTmp = apodo.getBytes();
			        			 apodo = new String(apodoTmp, "UTF-8");
			        			 
			        			 byte[] UserComentarioTmp = UserComentario.getBytes();
			        			 UserComentario = new String(UserComentarioTmp, "UTF-8");
			        			 
			        			 String apodo_Tmp = apodo;
			        			 apodo_Tmp = apodo_Tmp.replace("Ã¡", "á");
			        			 apodo_Tmp = apodo_Tmp.replace("Ã", "Á");
			        			 apodo_Tmp = apodo_Tmp.replace("Ã©", "é");
			        			 apodo_Tmp = apodo_Tmp.replace("Ã‰", "É");
			        			 apodo_Tmp = apodo_Tmp.replace("Ã­", "í");
			        			 apodo_Tmp = apodo_Tmp.replace("Ã", "Í");
			        			 apodo_Tmp = apodo_Tmp.replace("Ã³", "ó");
			        			 apodo_Tmp = apodo_Tmp.replace("Ã“", "Ó");
			        			 apodo_Tmp = apodo_Tmp.replace("Ãº", "ú");
			        			 apodo_Tmp = apodo_Tmp.replace("Ãš", "Ú");
			        			 apodo_Tmp = apodo_Tmp.replace("Ã±", "ñ");
			        			 apodo_Tmp = apodo_Tmp.replace("Ã‘", "Ñ");
			        			 
			        			 apodo = apodo_Tmp;
			        			 
			        			 String UserComentario_Tmp = UserComentario;
			        			 UserComentario_Tmp = UserComentario_Tmp.replace("Ã¡", "á");
			        			 UserComentario_Tmp = UserComentario_Tmp.replace("Ã", "Á");
			        			 UserComentario_Tmp = UserComentario_Tmp.replace("Ã©", "é");
			        			 UserComentario_Tmp = UserComentario_Tmp.replace("Ã‰", "É");
			        			 UserComentario_Tmp = UserComentario_Tmp.replace("Ã­", "í");
			        			 UserComentario_Tmp = UserComentario_Tmp.replace("Ã", "Í");
			        			 UserComentario_Tmp = UserComentario_Tmp.replace("Ã³", "ó");
			        			 UserComentario_Tmp = UserComentario_Tmp.replace("Ã“", "Ó");
			        			 UserComentario_Tmp = UserComentario_Tmp.replace("Ãº", "ú");
			        			 UserComentario_Tmp = UserComentario_Tmp.replace("Ãš", "Ú");
			        			 UserComentario_Tmp = UserComentario_Tmp.replace("Ã±", "ñ");
			        			 UserComentario_Tmp = UserComentario_Tmp.replace("Ã‘", "Ñ");
			        			 
			        			 UserComentario = UserComentario_Tmp;
			        			 
			        			 NotificationMail.setValue("apodo", StringEscapeUtils.escapeHtml(apodo));
			        			 NotificationMail.setValue("tituloNoticia",TituloNoticia );
			        			 NotificationMail.setValue("FromUsername",StringEscapeUtils.escapeHtml(UserComentario));
			        			 NotificationMail.setValue("txtComentario",TxtComentario);
			        			 NotificationMail.setValue("urlNoticia",NoticiaURL);

			        			 NotificationMail.setHtmlContents(fileContents);

			        			 NotificationMail.setSubject("Su Post ha recibido un comentario");
			        			 //NotificationMail.setFrom(OpenCms.getSystemInfo().getMailSettings().getMailFromDefault());
			        			 NotificationMail.setFrom(MailSettingsService.getMailFrom(cms));
			        			 MailSender.getInstance().sendMail(NotificationMail, "Administrator", MailDestination);
			        	      
			        	    	 }
			        	       }
			        	    }
			        	    
			            } catch (Exception e) {
			            	e.printStackTrace();
			            	CmsLog.getLog(this).error("ERROR al intentar enviar la notificación al autor: "+e.getMessage());
			            }
			        }	
				    
				    
				  
			    } catch (CmsException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					CmsLog.getLog(this).error("ERROR al intentar enviar la notificación al autor: "+e.getMessage());
				}
			   
		   }else{
			   return;
		   }

	}
	
	public void sendNotificationReplyComment(CmsObject cms, String NoticiaURL, String UserComentario, String TxtComentario, int replyComment){
		
		if(this.getNotificationReply() !=null && !this.getNotificationReply().equals("")){
			
			String TituloNoticia = "";
			 
			try {
				CmsFile file = cms.readFile(NoticiaURL);
				CmsXmlContent xmlContent = CmsXmlContentFactory.unmarshal(cms, file);
				              xmlContent.setAutoCorrectionEnabled(true);
				              
				String absolutePath = xmlContent.getFile().getRootPath();
				List<Locale> locales = xmlContent.getLocales();
						        
				if (locales.size() == 0) {
					locales = OpenCms.getLocaleManager().getDefaultLocales(cms, absolutePath);
				}
				
				Locale locale = OpenCms.getLocaleManager().getBestMatchingLocale(CmsLocaleManager.getLocale(""),
				OpenCms.getLocaleManager().getDefaultLocales(cms, absolutePath),locales);
				
				I_CmsXmlContentValue tituloXml = xmlContent.getValue("titulo",locale);
				
		        try {
					TituloNoticia = CmsHtmlExtractor.extractText(tituloXml.getStringValue(cms),xmlContent.getEncoding());
				} catch (ParserException e1) {
					CmsLog.getLog(this).error("ERROR al intentar enviar la notificación al autor: ", e1);
				} catch (UnsupportedEncodingException e1) {
					CmsLog.getLog(this).error("ERROR al intentar enviar la notificación al autor: ",e1);
				}
						        
			} catch (CmsException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			Comment ComentarioPadre = this.getCommentPersitor().getComment(cms, String.valueOf(replyComment));
			
			String ToUsername = ComentarioPadre.getUser();
			CmsUser currentUser = null;
			
			String ToUser = "";
	        if(ToUsername.indexOf("/")>-1){
	        	String[] arrayUser = ToUsername.split("/");
	    	    ToUser = arrayUser[1];
	    	    currentUser = RegistrationModule.getInstance(cms).retrieveUser(cms, ToUser);
	    	  }else{
	    		  try {
						currentUser  = cms.readUser(ToUsername);
					} catch (CmsException e) {
						e.printStackTrace();
						CmsLog.getLog(this).error("ERROR al intentar enviar la notificación en respuesta de comentario: "+e.getMessage());
					}
		    		 ToUser = currentUser.getEmail();
	    	  }
	        
		    TfsUserHelper tfsUser = new TfsUserHelper(currentUser);
		    Boolean UserNotificationReply = Boolean.valueOf(tfsUser.getValorAdicional(this.getNotificationReply()));
			 
		    if(UserNotificationReply){
		    	String apodo = tfsUser.getValorAdicional("APODO");
	    	 
	    	    if(apodo==null || apodo.equals("")){
	    		 apodo = tfsUser.getName();
	    	    }
	    	    
	    	    if(apodo.indexOf("/")>-1){
    	    	    String[] arrayApodo = apodo.split("/");
    	    	    apodo = arrayApodo[1];
    	        }
	    	 
			    SimpleMail NotificationMail = new SimpleMail();

			    String MailDestination = tfsUser.getEmail();

			    NotificationMail.addTo(MailDestination);
			 
			    String fileContents = "";
			    
			    String fileNameContents = getMailFile("replyCommentNotificationMail", NoticiaURL,replyComment,cms);
   			               fileContents = readFileContents(fileNameContents);

			    if(UserComentario.indexOf("/")>-1){
    	    	    String[] arrayUserComentario = UserComentario.split("/");
    	    	    UserComentario = arrayUserComentario[1];
    	        }
			    
			    byte[] apodoTmp = apodo.getBytes();
			    
	   			try {
					apodo = new String(apodoTmp, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	   			 
	   			byte[] UserComentarioTmp = UserComentario.getBytes();
	   			
	   			try {
					UserComentario = new String(UserComentarioTmp, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				 String apodo_Tmp = apodo;
	   			 apodo_Tmp = apodo_Tmp.replace("Ã¡", "á");
	   			 apodo_Tmp = apodo_Tmp.replace("Ã", "Á");
	   			 apodo_Tmp = apodo_Tmp.replace("Ã©", "é");
	   			 apodo_Tmp = apodo_Tmp.replace("Ã‰", "É");
	   			 apodo_Tmp = apodo_Tmp.replace("Ã­", "í");
	   			 apodo_Tmp = apodo_Tmp.replace("Ã", "Í");
	   			 apodo_Tmp = apodo_Tmp.replace("Ã³", "ó");
	   			 apodo_Tmp = apodo_Tmp.replace("Ã“", "Ó");
	   			 apodo_Tmp = apodo_Tmp.replace("Ãº", "ú");
	   			 apodo_Tmp = apodo_Tmp.replace("Ãš", "Ú");
	   			 apodo_Tmp = apodo_Tmp.replace("Ã±", "ñ");
	   			 apodo_Tmp = apodo_Tmp.replace("Ã‘", "Ñ");
	   			 
	   			 apodo = apodo_Tmp;
   			 
	   			 String UserComentario_Tmp = UserComentario;
	   			 UserComentario_Tmp = UserComentario_Tmp.replace("Ã¡", "á");
	   			 UserComentario_Tmp = UserComentario_Tmp.replace("Ã", "Á");
	   			 UserComentario_Tmp = UserComentario_Tmp.replace("Ã©", "é");
	   			 UserComentario_Tmp = UserComentario_Tmp.replace("Ã‰", "É");
	   			 UserComentario_Tmp = UserComentario_Tmp.replace("Ã­", "í");
	   			 UserComentario_Tmp = UserComentario_Tmp.replace("Ã", "Í");
	   			 UserComentario_Tmp = UserComentario_Tmp.replace("Ã³", "ó");
	   			 UserComentario_Tmp = UserComentario_Tmp.replace("Ã“", "Ó");
	   			 UserComentario_Tmp = UserComentario_Tmp.replace("Ãº", "ú");
	   			 UserComentario_Tmp = UserComentario_Tmp.replace("Ãš", "Ú");
	   			 UserComentario_Tmp = UserComentario_Tmp.replace("Ã±", "ñ");
	   			 UserComentario_Tmp = UserComentario_Tmp.replace("Ã‘", "Ñ");
	   			 
	   			 UserComentario = UserComentario_Tmp;

			    NotificationMail.setValue("apodo", StringEscapeUtils.escapeHtml(apodo));
			    NotificationMail.setValue("tituloNoticia",TituloNoticia );
			    NotificationMail.setValue("FromUsername",StringEscapeUtils.escapeHtml(UserComentario));
			    NotificationMail.setValue("txtComentario",TxtComentario);
			    NotificationMail.setValue("urlNoticia",NoticiaURL);

			    NotificationMail.setHtmlContents(fileContents);

			    NotificationMail.setSubject("Su comentario fue respondido");
			    //NotificationMail.setFrom(OpenCms.getSystemInfo().getMailSettings().getMailFromDefault());
			    NotificationMail.setFrom(MailSettingsService.getMailFrom(cms));
			    MailSender.getInstance().sendMail(NotificationMail, "Administrator", MailDestination);
	      
		    }
			
		}
	}

	/**
	 * Determina la longitud maxima del comentario
	 * @return int
	 */
	private int getCommentTextMaxSize() {
		return CmsMedios.getInstance().getCmsParaMediosConfiguration().getIntegerParam(siteName, publication, module, "commentTextMaxSize", 255);
	}

	/**
	 * Permite reportar un comentario como abuso.
	 * @param cms
	 * @param userName
	 * @param commentId
	 * @param abuseType
	 * @param abuseDetails
	 */
	public void reportAbuse(CmsObject cms, String userName, String commentId, String abuseType,
			String abuseDetails) {
		this.getCommentPersitor().incrementReportsCount(cms, commentId);
		String siteName = OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot();
		Comment abusiveComment = this.getCommentPersitor().getComment(cms, commentId);
		
		AbuseReport r = new AbuseReport();
		r.setFecha(new Date());
		r.setMotivo(this.getAbuseTypeDescription(new Integer(abuseType)));
		r.setDescription(abuseDetails);
		r.setUsuario(userName);
		r.setPath(abusiveComment.getNoticiaURL());
		r.setSitio(siteName);
		r.setCommentId(abusiveComment.getId());
		
		this.getCommentPersitor().save(cms, r);
				
		Map<String, Object> eventData = new HashMap<String, Object>();
        eventData.put(I_TfsEventListener.KEY_COMMENT, abusiveComment);
        eventData.put(I_TfsEventListener.KEY_ABUSETYPE, abuseType);
        eventData.put(I_TfsEventListener.KEY_USER_NAME, userName);
        
        OpenCms.fireCmsEvent(new CmsEvent(I_TfsEventListener.EVENT_COMMENT_REPORTED, eventData));

		SimpleMail abuseMail = new SimpleMail();

		abuseMail.addTo(getAbuseMailDestination());

		String fileNameContents = getMailFile("abuseMailModel", abusiveComment.getNoticiaURL(),0,cms);
		//descomentar
		String     fileContents = readFileContents(fileNameContents);

		abuseMail.setValue("userName", userName);
		abuseMail.setValue("abusiveComment", abusiveComment.getText());
		abuseMail.setValue("abuseType", this.getAbuseTypeDescription(new Integer(abuseType)));
		abuseMail.setValue("abuseDetails", abuseDetails);
		abuseMail.setValue("noticiaURL", abusiveComment.getNoticiaURL());

		//descomentar
		abuseMail.setHtmlContents(fileContents);

		abuseMail.setSubject("Se ha recibido un reporte de abuso");
		//abuseMail.setFrom(OpenCms.getSystemInfo().getMailSettings().getMailFromDefault());
		abuseMail.setFrom(MailSettingsService.getMailFrom(cms));

		MailSender.getInstance().sendMail(abuseMail, "Administrator", getAbuseMailDestination());
	}

	public String getAbuseTypeDescription(int abuseType) {
		switch (abuseType) {
			case 0:
				return "Obscenidad / Vulgaridad";
			case 1:
				return "Ataque Personal";
			case 2:
				return "Publicidad / SPAM";
			case 3:
				return "Plagio";
			case 4:
				return "Otro";
		}

		throw new ProgramException("Tipo de Abuso desconocido [" + abuseType + "]");
	}

	// *************************************
	// ** comments module backend interface
	// *************************************
	public void deleteComment(CmsObject cms, String commentId) {
		this.getCommentPersitor().delete(cms, commentId);
	}

	public void acceptComment(CmsObject cms, String commentId) {
		this.getCommentPersitor().acceptComment(cms, commentId);
		this.getCommentPersitor().resetReportsCount(cms, commentId);
		Map<String, Comment> eventData = new HashMap<String, Comment>();
        eventData.put(I_TfsEventListener.KEY_COMMENT, this.getCommentPersitor().getComment(cms, commentId));
        OpenCms.fireCmsEvent(new CmsEvent(I_TfsEventListener.EVENT_COMMENT_ACEPTED, eventData));
	}

	public Comment getComment(CmsObject cms, String commentId) {
		return this.getCommentPersitor().getComment(cms, commentId);		
	}

	public List<Comment> getCommentsInReVision(CmsObject cms, Date from) {
		return this.getCommentPersitor().getCommentsInRevision(cms, from);		
	}

	public void revisionComment(CmsObject cms, String commentId) {
		this.getCommentPersitor().revisionComment(cms, commentId);

		Map<String, Comment> eventData = new HashMap<String, Comment>();
        eventData.put(I_TfsEventListener.KEY_COMMENT, this.getCommentPersitor().getComment(cms, commentId));
        OpenCms.fireCmsEvent(new CmsEvent(I_TfsEventListener.EVENT_COMMENT_REVISION, eventData));

	}

	public void rejectComment(CmsObject cms, String commentId) {
		this.getCommentPersitor().rejectComment(cms, commentId);

		Map<String, Comment> eventData = new HashMap<String, Comment>();
        eventData.put(I_TfsEventListener.KEY_COMMENT, this.getCommentPersitor().getComment(cms, commentId));
        OpenCms.fireCmsEvent(new CmsEvent(I_TfsEventListener.EVENT_COMMENT_REJECTED, eventData));

	}

	public List<Comment> getAllComments(CmsObject cms, boolean verHistorico) {
		String siteName = getSiteName(cms); 
		CmsLog.getLog(this).debug("Vista Admin - lista los comentarios del site: "+siteName);
		return this.getCommentPersitor().getAllComments(cms, verHistorico, siteName);
	}
	
	public List<Comment> getListComments(CmsObject cms, String dateFrom, String dateTo, String state, String user , String newsPath, String text, int reportNumber, int pageNumber, int PageSize, String order ){
		return getListComments(cms, dateFrom, dateTo, state, user , newsPath, text, reportNumber, pageNumber, PageSize, order, false);
	}
	
	public List<Comment> getListComments(CmsObject cms, String dateFrom, String dateTo, String state, String user , String newsPath, String text, int reportNumber, int pageNumber, int PageSize, String order, boolean abuseReport ){
		   String siteName = getSiteName(cms);
		   
		   return this.getCommentPersitor().getListComments(cms, siteName, publication, dateFrom, dateTo, state, user , newsPath, text, reportNumber, pageNumber, PageSize, order, abuseReport);
	}
	
	public List<AbuseReport> getListAbuseReports(CmsObject cms, String dateFrom, String dateTo, String user , String newsPath, String text, int reportNumber, int pageNumber, int PageSize, String order ){
		   		   
		   return this.getCommentPersitor().getListAbuseReports(cms, dateFrom, dateTo, user, newsPath, text, reportNumber, pageNumber, PageSize, order );
	}
	
	public List<AbuseReport> getListAbuseReportsByComment(CmsObject cms, String commentId){
		   
		   return this.getCommentPersitor().getListAbuseReportsByComment(cms, commentId);
	}

	public int getAutoRefreshRate() {
		return CmsMedios.getInstance().getCmsParaMediosConfiguration().getIntegerParam(siteName, publication, module, "autoRefreshRate", 20);
	}

	public int getOldNewsAutoRefreshRate() {
		return CmsMedios.getInstance().getCmsParaMediosConfiguration().getIntegerParam(siteName, publication, module, "oldNewsAutoRefreshRate", 0);
	}

	
	public int getOldNewsAutoRefreshLimit() {
		return CmsMedios.getInstance().getCmsParaMediosConfiguration().getIntegerParam(siteName, publication, module, "oldNewsAutoRefreshLimit", 24);
	}
	
	
	public Boolean getEnableDynamicRefresh() {
		return CmsMedios.getInstance().getCmsParaMediosConfiguration().getBooleanParam(siteName, publication, module, "enableDynamicRefresh", false);
	}

	
	/**
	 * Permite conocer si se utiliza captcha en el ingreso de comentarios.
	 * @return true si se utiliza
	 */
	public Boolean getUseCapcha() {
		return CmsMedios.getInstance().getCmsParaMediosConfiguration().getBooleanParam(siteName, publication, module, "useCaptcha", false);
	}
	
	/**
	 * Permite conocer si se muestran los comentarios en estado de revision.
	 * @return true si se utiliza
	 */
	public Boolean getShowRevisionComments() {
		return CmsMedios.getInstance().getCmsParaMediosConfiguration().getBooleanParam(siteName, publication, module, "showCommentsInRevision", false);
	}

	
	
	/**
	 * @param noticiaURL 
	 * @param pageNumber 
	 * @param PageSize si es 0 trae todas
	 */
	public List<Comment> getCommentsTree(CmsObject cms, String noticiaURL, int CommentPadreID, String pageNumber){
		
		String noticiaPrincipal = noticiaURL;
		String masterNews = getMasterNews(cms,noticiaURL);
		
		if(masterNews!=null && masterNews != "" ) noticiaPrincipal = masterNews;
		
		CommentPersistor cPer = new CommentPersistor(this);
		
		List<Comment> comments = cPer.getCommentsByLevels(cms, noticiaPrincipal, CommentPadreID, pageNumber, this.getQueryPageSize());

		List<Comment> OrderComments = new ArrayList<Comment>();
		
		for (Iterator<Comment> it = comments.iterator(); it.hasNext();) {
			Comment comment = it.next();
			
			OrderComments.add(comment);
			int commentReplyof = comment.getId();
			
			List<Comment> replys = getCommentsTree(cms,comment.getNoticiaURL(),commentReplyof, pageNumber);
			OrderComments.addAll(replys);
		}
		
		return OrderComments;
  
	}
	
	/**
	 * Obtiene la lista de comentarios con mas respuestas de una noticia o en gral
	 * @param cms
	 * @param noticiaURL si se ingresa en null trae los comentarios de todas las noticias con mas respuestas
	 * @param min_answers cantidad minimas de respuestas para considerar como rankeable al comentario
	 * @param pageNumber
	 * @param ShowAnswers indica si lista los comentarios con sus respuestas
	 * @return List<Comment>
	 */
	public List<Comment> getCommentsWhitMoreAnswers(CmsObject cms, String noticiaURL, int min_answers, String pageNumber, boolean ShowAnswers) {
		
		String noticiaPrincipal = noticiaURL;
		String masterNews = getMasterNews(cms,noticiaURL);
		
		if(masterNews!=null && masterNews != "" ) noticiaPrincipal = masterNews;
		
		String siteName = getSiteName(cms); 
		List<Comment> Comentarios = this.getCommentPersitor().getCommentsWhitMoreAnswers(cms, noticiaPrincipal ,min_answers,pageNumber, siteName);
		
		if(ShowAnswers){
		  
		  List<Comment> ComentariosP = new ArrayList<Comment>();
			
		  for (Iterator<Comment> it = Comentarios.iterator(); it.hasNext(); ){
			
			List<Comment> Respuestas = new ArrayList<Comment>();
		    Comment commentP = (Comment) it.next();
		    Respuestas = getCommentsTree(cms, noticiaURL, commentP.getId(), pageNumber);
		    ComentariosP.add(commentP);
		    
		    if(Respuestas.size()>0)  ComentariosP.addAll(Respuestas); 
		  }
		  
		  return ComentariosP;
		
		}else{
		  return Comentarios;
		}
		
	}
	
	public int getCommentsWhitMoreAnswersCount(CmsObject cms, String noticiaURL, int min_answers) {
		String siteName = getSiteName(cms); 
		
		String noticiaPrincipal = noticiaURL;
		String masterNews = getMasterNews(cms,noticiaURL);
		
		if(masterNews!=null && masterNews != "" ) noticiaPrincipal = masterNews;
		
		return this.getCommentPersitor().getCommentsWhitMoreAnswersCount(cms, noticiaPrincipal ,min_answers, siteName);
	}
	
	public int getCommentAnswersCount(CmsObject cms, String IdComment){
		return this.getCommentPersitor().getCommentAnswersCount(cms, IdComment);
	}
	
	// Dejamos este metodo para que sea compatible con noticias viejas, tenemos que contemplar que el campo ya no existe en la noticia.
	public String getMasterNews(CmsObject cms, String NewsURL){
        
        String masterNews = null;

	    try{
	         CmsFile file = cms.readFile(NewsURL);
		     CmsXmlContent xmlContent = CmsXmlContentFactory.unmarshal(cms, file);
		     xmlContent.setAutoCorrectionEnabled(true); 
	
		     String  absolutePath = cms.getRequestContext().removeSiteRoot(xmlContent.getFile().getRootPath());
		     List<Locale> locales = xmlContent.getLocales();
			        
		     if (locales.size() == 0) {
			      locales = OpenCms.getLocaleManager().getDefaultLocales(cms, absolutePath);
		     }
		     
		     Locale locale = OpenCms.getLocaleManager().getBestMatchingLocale(CmsLocaleManager.getLocale(""),
		     OpenCms.getLocaleManager().getDefaultLocales(cms, absolutePath),locales);
	
		    // I_CmsXmlContentValue masterNewsXml = xmlContent.getValue("sharedComments",locale);
		     List<I_CmsXmlContentValue> linkedArticles = xmlContent.getValues("linkedArticles", locale);
		     
		     String newsXml = null;
		     
		     // Si comparte comentarios con alguna tengo que con ese path determinar cual es la master
		     for (int j = 1; j <= linkedArticles.size(); j++) {
		    	 	
		    	I_CmsXmlContentValue valuePath = xmlContent.getValue("linkedArticles[" + j + "]/path[1]", Locale.ENGLISH);
				String linkedPath = valuePath.getStringValue(cms);
				 
				String sharedComments = "false";
					
				try {
					
					DocumentBuilder newDocumentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
					Document document = newDocumentBuilder.parse(new ByteArrayInputStream(xmlContent.marshal()));
					NodeList nodes = document.getElementsByTagName("linkedArticles");
				
					if(nodes!=null && nodes.getLength()>0){
						Element element = (Element) nodes.item(j-1);
							
						Node sharedCommentsItem = element.getElementsByTagName("sharedComments").item(0);
						
						if(sharedCommentsItem!=null)
							sharedComments = sharedCommentsItem.getFirstChild().getNodeValue();
					}
				} catch (SAXException e) {
					CmsLog.getLog(this).error("ERROR al intentar obtener el valor de sharedComments en noticiasLinkeadas "+e.getMessage());
				} catch (IOException e) {
					CmsLog.getLog(this).error("ERROR al intentar obtener el valor de sharedComments en noticiasLinkeadas "+e.getMessage());
				} catch (ParserConfigurationException e) {
					CmsLog.getLog(this).error("ERROR al intentar obtener el valor de sharedComments en noticiasLinkeadas "+e.getMessage());
				}
					
				if(sharedComments.equals("true")){ 
					newsXml = cms.getRequestContext().removeSiteRoot(linkedPath);
					j = linkedArticles.size()+1;
				}
		     }
		     
		     if(newsXml !=null){
		    	 // Si newsXml esta en la lista de sources del path de la noticia en la que hago el comentarios, 
		    	 // tengo que registrar el comentario en newsXml
		    	 CmsRelationFilter    filter = CmsRelationFilter.SOURCES.filterType(CmsRelationType.valueOf("linkedArticle"));
		    	 List<CmsRelation> relations = cms.getRelationsForResource(NewsURL, filter);
		    	 
		    	 for (CmsRelation relation : relations){
		    		 String rel = cms.getRequestContext().removeSiteRoot(relation.getSourcePath());
		    		 
		    		 if(rel.equals(newsXml)) 
		    			 masterNews = rel;
		    	 }
		     }
		     
	    }catch (CmsException e) {
					CmsLog.getLog(this).error("ERROR al intentar obtener la noticia principal: "+e.getMessage());
	   }
	
	       return masterNews;
	}
	
	public String getMailFile(String type, String NewsURL, int replyCommentId, CmsObject cms){
		
		String fileName = null;
		String fileNameByDefect = null;
		
		if(type.equals("abuseMailModel")) 
			fileNameByDefect = "abuseMailModel.html";
		else if(type.equals("postCommentNotificationMail")) 
			fileNameByDefect = "PostCommentNotificationMail.html";
		else if (type.equals("replyCommentNotificationMail"))
			fileNameByDefect = "ReplyCommentNotificationMail.html";
		
		String siteName = OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot();
	    int publicationId = 1;
	    
	    TipoEdicionBaseService tService = new TipoEdicionBaseService();
	    	try {
				TipoEdicion tEdicion = tService.obtenerTipoEdicion(cms, NewsURL);			
				if (tEdicion!=null)
					publicationId = tEdicion.getId();
			} catch (Exception e) {
				e.printStackTrace();
			}
		
		if(replyCommentId >0 ){
			try {
			  int publicationReplyComment = this.getCommentPersitor().getPublicationIdShared(cms, replyCommentId);
			  
			  if(publicationReplyComment>0)
				   publicationId = publicationReplyComment;  
				  
			} catch (Exception e) {
				CmsLog.getLog(this).error(e.getMessage());
			}
		}
		
		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
		String publication = Integer.toString(publicationId);
		
		fileName = config.getParam(siteName, publication, "comments", type, fileNameByDefect);
			
		return fileName;
	}
	
	public String getRemoteAddress(String remoteAddress){
		  
		String ip = "";
		
		// Expresion regular ip clase A (10.0.0.0 - 10.255.255.255)
		String regexClassA = "(10\\.(([0,1]?[0-9]{1,2}|2[0-4][0-9]|25[0-5])\\.){2}([0,1]?[0-9]{1,2}|2[0-4][0-9]|25[0-5]))";

		// Expresion regular ip clase B (172.16.0.0 - 172.31.255.255)
		String regexClassB = "(172\\.(1[6-9]|2[0-9]|3[0-1])\\.([0,1]?[0-9]{1,2}|2[0-4][0-9]|25[0-5])\\.([0,1]?[0-9]{1,2}|2[0-4][0-9]|25[0-5]))";

		// Expresion regular ip clase C (192.168.0.0 - 192.168.255.255)
		String regexClassC = "(192\\.168\\.([0,1]?[0-9]{1,2}|2[0-4][0-9]|25[0-5])\\.([0,1]?[0-9]{1,2}|2[0-4][0-9]|25[0-5]))";

		Pattern pA = Pattern.compile(regexClassA);
		Pattern pB = Pattern.compile(regexClassB);
		Pattern pC = Pattern.compile(regexClassC);
		
		Matcher mA, mB, mC;
		
		String[] remoteAddrArray = remoteAddress.split(",");
	    int                 size = remoteAddrArray.length;
	    
	    for (int a=0;a< size; a++){
	    	 mA = pA.matcher(remoteAddrArray[a]);
	         mB = pB.matcher(remoteAddrArray[a]);
	         mC = pC.matcher(remoteAddrArray[a]);
		
		if (!mA.find() && !mB.find() && !mC.find())
		    ip = remoteAddrArray[a];
	    }
		
		return ip;
	}

	
}
