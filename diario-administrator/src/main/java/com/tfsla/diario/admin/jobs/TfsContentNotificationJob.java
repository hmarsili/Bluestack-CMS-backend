package com.tfsla.diario.admin.jobs;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.opencms.db.CmsDbEntryNotFoundException;
import org.opencms.db.CmsResourceState;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.file.history.CmsHistoryResourceHandler;
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.file.types.CmsResourceTypeXmlPage;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.i18n.CmsMessages;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.scheduler.I_CmsScheduledJob;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.comparison.*;
import com.tfsla.diario.history.tfsResourceHistory;
import com.tfsla.opencms.mail.SimpleMail;

public class TfsContentNotificationJob implements I_CmsScheduledJob{

	private List m_resources;
	private List m_responsibleUsers;
	private CmsObject m_cms;
	private static final Log LOG = CmsLog.getLog(TfsContentNotificationJob.class);
	CmsMessages messages;
	
	public String launch(CmsObject cms, Map parameters) throws Exception {
		
		m_resources = new ArrayList();
		m_responsibleUsers = new ArrayList();
        m_cms = cms;
        m_cms.getRequestContext().setCurrentProject(m_cms.readProject(OpenCms.getSystemInfo().getNotificationProject()));
        String folder = "/";
        
        Iterator resources;
        CmsResource resource;
        
        try {
        	 resources = m_cms.readResourcesWithProperty(folder, CmsPropertyDefinition.PROPERTY_NOTIFICATION_INTERVAL).iterator();
             while (resources.hasNext()) {
            	 resource = (CmsResource)resources.next();
            	 
                 int notification_interval = Integer.parseInt(m_cms.readPropertyObject(resource, CmsPropertyDefinition.PROPERTY_NOTIFICATION_INTERVAL,true).getValue());
                 
                 if(notification_interval>0){
                	 
	                 Calendar calendar = Calendar.getInstance();
	                 
	                 Date dateTo = calendar.getTime(); 
	                 Timestamp dateToTs = new Timestamp(dateTo.getTime());
	                 
	                 calendar.setTime(dateTo);  
	                 calendar.add(Calendar.HOUR,   -notification_interval);  
	                 
	                 Date dateFrom = calendar.getTime(); 
	                 Timestamp dateFromTs = new Timestamp(dateFrom.getTime());
	                 
	                 Timestamp fileModificationDate = new Timestamp(resource.getDateLastModified());
	                		 
	                 if(fileModificationDate.after(dateFromTs) && fileModificationDate.before(dateToTs)){
	                	 m_resources.add(resource);
	                	 
	                	 String enableNotification = m_cms.readPropertyObject(resource,CmsPropertyDefinition.PROPERTY_ENABLE_NOTIFICATION, true).getValue();
	                     
	                     if (Boolean.valueOf(enableNotification).booleanValue()) {
	                    	 try {
	                             Iterator responsibles = m_cms.readResponsibleUsers(resource).iterator();
	                             
	                             while (responsibles.hasNext()) {
	                                 CmsUser responsible = (CmsUser)responsibles.next();
	                                 
	                                 if(!m_responsibleUsers.contains(responsible))
	                                	 m_responsibleUsers.add(responsible);
	                             }
	                         } catch (CmsException e) {
	                             if (LOG.isInfoEnabled()) {
	                                 LOG.error(e.getLocalizedMessage(), e);
	                             }
	                         }
	                     }
	                 }
                 }
             }
             
             sendNotifications();
             
        } catch (CmsDbEntryNotFoundException e) {
        	LOG.error(e.getLocalizedMessage(), e);
        }
		
		return null;
	}
	
	public void sendNotifications(){
		
		Iterator responsibles = m_responsibleUsers.iterator();
		
		while (responsibles.hasNext()) {
			
			CmsUser responsible = (CmsUser)responsibles.next();
			
			try {	
				SimpleMail notificationMail = new SimpleMail();
				
				Date date = new Date();
				String dateStr = new SimpleDateFormat("dd/MM HH:mm").format(date);
				
				String subject =  dateStr + " - CMS Medios : Aviso de archivo modificado";
				 
				notificationMail.setSubject(subject);
				notificationMail.setHtmlContents(getHtmlMesagge(responsible));
				notificationMail.addTo(responsible.getEmail()); 
				notificationMail.send();
			
			} catch (CmsLoaderException e) {
				LOG.error("No se pude enviar el msg a "+responsible.getEmail()+". Error: "+e.getMessage());
			}
		}
	}
	
	public String getHtmlMesagge(CmsUser user) throws CmsLoaderException{
		
		String msg = "";
		
		msg += "Dear "+user.getFullName()+",<br><br>";

		msg += "You are responsible for a number of resources.  Some of them were updated<br><br>";
		
		Iterator resourcesUpdated = m_resources.iterator();
		
		while (resourcesUpdated.hasNext()) {
			CmsResource res = (CmsResource)resourcesUpdated.next();
			
			messages = OpenCms.getWorkplaceManager().getMessages(m_cms.getRequestContext().getLocale());
			
			int currentVersion = res.getVersion();
			String version1 = ""+currentVersion;
			
			int earlierVersion = currentVersion-1;
			String version2 = ""+earlierVersion;
			
			CmsUUID IDResource = res.getStructureId();
			I_CmsResourceType resourceType = OpenCms.getResourceManager().getResourceType(res.getTypeId());
			
			CmsResource m_resource1 = null;
			CmsResource m_resource2 = null;
			
			CmsResourceState state = res.getState();
			
			if(!state.toString().equals("2")){
				try {
					
					if(state.toString().equals("1"))
						version1 = ""+CmsHistoryResourceHandler.PROJECT_OFFLINE_VERSION;
					
					m_resource1 = tfsResourceHistory.getInstance().getResource(m_cms,IDResource,version1);
					m_resource2 = tfsResourceHistory.getInstance().getResource(m_cms,IDResource,version2);
					
				} catch (CmsException e2) {
					LOG.error("No se pudieron obtener los recursos de las diferentes versiones: "+e2.getMessage());
				}
			}

			try {
					Iterator responsibles = m_cms.readResponsibleUsers(res).iterator();
					
					while (responsibles.hasNext()) {
		                 CmsUser responsible = (CmsUser)responsibles.next();
		                 
		                 if(user.equals(responsible)){
		                	 
		                	 String path = m_cms.getRequestContext().removeSiteRoot(res.getRootPath());
		         			 String rootPath = res.getRootPath();
		         			 String site = "/";
		         			 
		         			 if(!path.equals(rootPath))
		         				 site = rootPath.replace(path, "");
		         			
		         			 CmsUUID modifiedByID = res.getUserLastModified();
		         			 CmsUser modifiedByUser = null;
		         			
		         			 try {
		         				modifiedByUser = m_cms.readUser(modifiedByID);
		         			 } catch (CmsException e) {
		         				LOG.error("No se pudo obtener el usuario responsable del recurso modificado. "+e.getMessage());
		         			 }
		         			
		         			 String modifiedBy = "";
		         			
		         			 if(modifiedByUser!=null)
		         				modifiedBy = modifiedByUser.getFullName();
		         			
		         			 
		         			 String dateLastModifiedStr = ""+res.getDateLastModified();
		         		            dateLastModifiedStr = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(res.getDateLastModified());
		         				 
		         		     msg += "<b>*"+path+"</b><br>";
		         		     
		         			 msg += "Site: "+site+"<br>";
		         			 msg += "Modified by: "+modifiedBy+"<br>";
		         			 msg += "Modification date: "+dateLastModifiedStr+"<br>";
		         			 
		         			if(state.toString().equals("2")){
		         				
		         				String dateCreatedStr = ""+res.getDateCreated();
	         		            dateCreatedStr = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(res.getDateCreated());
	         			
		         				msg += "Creation date: "+dateCreatedStr+"<br>";
		         			}
		         			
		         			 msg += "<br><br>";
		         			 
		         			 if(currentVersion-earlierVersion>0 && (m_resource1!=null && m_resource2!=null)){
			         			 msg += getDiffProperties(m_resource1,version1,m_resource2,version2);
			         			 msg += getDiffAttributes(m_resource1,version1,m_resource2,version2);
			         			 msg += getDiffContent(m_resource1,version1,m_resource2,version2,resourceType);
			         			 msg += "<br><br>";
		         			 }
		         			 
		         			
		                 }
		             }
					
			} catch (CmsException e1) {
					LOG.error("Error al notificar modificaciones en un recurso. "+e1.getMessage());
			}
             
			
		}
		
		return msg;
		
	}

	private String getDiffProperties(CmsResource resource1, String version1, CmsResource resource2, String version2){
		
		String rows = "";
		String htmlDiff = "";
		
		try {
			Iterator diffs = CmsResourceComparison.compareProperties(m_cms,resource1,version1,resource2,version2).iterator();
		
			while (diffs.hasNext()){
	
			      CmsAttributeComparison comparison = (CmsAttributeComparison)diffs.next();
			      
			      if(!comparison.getStatus().equals("unchanged")){
			    	  rows += "<tr>";
			  		
			    	  rows += "<td>"+comparison.getStatus()+"</td>";
			    	  rows += "<td>"+comparison.getName()+"</td>";
			    	  rows += "<td>"+comparison.getVersion2()+"</td>";
			    	  rows += "<td>"+comparison.getVersion1()+"</td>";
			  		
			    	  rows += "</tr>";
			      }
			}
		
		} catch (CmsException e) {
			LOG.error("No se pudieron obtener las diferencias en las propiedades: "+e.getMessage());
		}
		
		if(!rows.equals("")){
			htmlDiff = "<b>Properties</b>";
			htmlDiff += "<table  width=\"90%\" border=\"1\"  cellpadding=\"2\" cellspacing=\"0\">";
			
			htmlDiff += "<tr>";
			htmlDiff += "<td><b>Status</b></td>";
			htmlDiff += "<td><b>Property</b></td>";
			htmlDiff += "<td width=\"40%\"><b>Version "+version2+"</b></td>";
			htmlDiff += "<td width=\"40%\"><b>Version "+resource1.getVersion()+"</b></td>";
			htmlDiff += "</tr>";
			
			htmlDiff += rows;
			
			htmlDiff += "</table><br>";
		}

		return htmlDiff;
	}
	
	private String getDiffAttributes(CmsResource resource1, String version1, CmsResource resource2, String version2){
		
		String rows = "";
		String htmlDiff = "";
		
		Iterator diffs = CmsResourceComparison.compareAttributes(m_cms,resource1,resource2).iterator();
		
		while (diffs.hasNext()){
	
			   CmsAttributeComparison comparison = (CmsAttributeComparison)diffs.next();
			      
			   if(!comparison.getStatus().equals("unchanged")){
			    	  rows += "<tr>";
			  		
			    	  rows += "<td>"+comparison.getStatus()+"</td>";
			    	  rows += "<td>"+messages.key(comparison.getName())+"</td>";
			    	  rows += "<td>"+comparison.getVersion2()+"</td>";
			    	  rows += "<td>"+comparison.getVersion1()+"</td>";
			  		
			    	  rows += "</tr>";
			   }
		}
	
		if(!rows.equals("")){
			htmlDiff = "<b>Atributes</b>";
			htmlDiff += "<table  width=\"90%\" border=\"1\"  cellpadding=\"2\" cellspacing=\"0\">";
			
			htmlDiff += "<tr>";
			htmlDiff += "<td><b>Status</b></td>";
			htmlDiff += "<td><b>Attribute</b></td>";
			htmlDiff += "<td width=\"40%\"><b>Version "+version2+"</b></td>";
			htmlDiff += "<td width=\"40%\"><b>Version "+resource1.getVersion()+"</b></td>";
			htmlDiff += "</tr>";
			
			htmlDiff += rows;
			
			htmlDiff += "</table><br>";
		}

		return htmlDiff;
	}
	
	private String getDiffContent(CmsResource resource1, String version1, CmsResource resource2, String version2, I_CmsResourceType resourceType){
		
		String rows = "";
		String htmlDiff = "";
		
		if ((resourceType instanceof CmsResourceTypeXmlContent) || (resourceType instanceof CmsResourceTypeXmlPage)) {
			
			try{
				CmsFile file1 = tfsResourceHistory.getInstance().readFile(m_cms,resource1.getStructureId(),version1);
			    CmsFile file2 = tfsResourceHistory.getInstance().readFile(m_cms,resource2.getStructureId(),version2);
			      
			    List contentDiffList = new CmsXmlDocumentComparison(m_cms, file2, file1).getElements();
			    Iterator diffC = contentDiffList.iterator();
				
				
				while (diffC.hasNext()){
			
					 CmsAttributeComparison comparison = (CmsAttributeComparison)diffC.next();
					 
					 String value1 = CmsStringUtil.escapeHtml(CmsStringUtil.substitute(CmsStringUtil.trimToSize(comparison.getVersion1(),60), "\n", ""));
			         String value2 = CmsStringUtil.escapeHtml(CmsStringUtil.substitute(CmsStringUtil.trimToSize(comparison.getVersion2(),60), "\n", ""));
					      
					 if(!comparison.getStatus().equals("unchanged")){
					    	  rows += "<tr>";
					  		
					    	  rows += "<td>"+comparison.getStatus()+"</td>";
					    	  rows += "<td>"+comparison.getName()+"</td>";
					    	  rows += "<td>"+value1+"</td>";
					    	  rows += "<td>"+value2+"</td>";
					  		
					    	  rows += "</tr>";
					 }
				}
					
			} catch (CmsException e) {
				LOG.error("No se pudieron obtener las diferencias en el contenido: "+e.getMessage());
			}
			
		}
		
		if(!rows.equals("")){
			htmlDiff = "<b>Contenido</b>";
			htmlDiff += "<table  width=\"90%\" border=\"1\"  cellpadding=\"2\" cellspacing=\"0\">";
			
			htmlDiff += "<tr>";
			htmlDiff += "<td><b>Status</b></td>";
			htmlDiff += "<td><b>Element</b></td>";
			htmlDiff += "<td width=\"40%\"><b>Version "+version2+"</b></td>";
			htmlDiff += "<td width=\"40%\"><b>Version "+resource1.getVersion()+"</b></td>";
			htmlDiff += "</tr>";
			
			htmlDiff += rows;
			
			htmlDiff += "</table><br>";
		}

		return htmlDiff;
		
	}
}
