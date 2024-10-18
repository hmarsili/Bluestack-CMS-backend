package com.tfsla.diario.admin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.jsp.PageContext;

import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.db.CmsResourceState;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.flex.CmsFlexController;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.relations.CmsRelationType;
import org.opencms.report.CmsLogReport;
import org.opencms.workplace.commons.CmsTouch;

import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.TipoEdicionService;
import com.tfsla.diario.videoConverter.jsp.TfsEnconderQueue;

public class TfsAudiosAdmin {
	
	private CPMConfig config;
	private CmsObject m_cms;
	private String siteName;
    private String publication;
    private String moduleConfigName;
    private TipoEdicion currentPublication;
    private CmsFlexController m_controller;
    private HttpSession m_session;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private PageContext m_context;
	
	public TfsAudiosAdmin(CmsObject cms, String site, String publicationId) throws Exception
    {
    	m_cms = cms;
    	siteName = site;
    	
    	publication = publicationId;
    	moduleConfigName = "audioUpload";
    	
    	config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
    }
	
	public TfsAudiosAdmin(PageContext context, HttpServletRequest req, HttpServletResponse res) throws Exception
    {
		m_controller = CmsFlexController.getController(req);
		m_session = req.getSession();
		request = req;
		response = res;
		m_context = context;
		
		siteName = OpenCms.getSiteManager().getCurrentSite(getCmsObject()).getSiteRoot();
		
		currentPublication = (TipoEdicion) m_session.getAttribute("currentPublication");

    	if (currentPublication==null) {
        	TipoEdicionService tService = new TipoEdicionService();

    		currentPublication = tService.obtenerEdicionOnlineRoot(siteName);
    		m_session.setAttribute("currentPublication",currentPublication);
    	}
    	
    	publication = "" + currentPublication.getId();
    	moduleConfigName = "videoConvert";
    	
    	config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
		
    }
	
	public CmsObject getCmsObject() {
		if(m_cms!=null)
			return m_cms;
		else
	        return m_controller.getCmsObject();
	}
	
	public void deleteAudio(String sourceVFSPath, CmsObject cms){
		
		try {	
			if (!cms.getLock(sourceVFSPath).isUnlocked()){
			     if(!cms.getLock(sourceVFSPath).isOwnedBy(cms.getRequestContext().currentUser())){
				      cms.changeLock(sourceVFSPath);
			    }
			}else{
			     cms.lockResource(sourceVFSPath);
			}
			
			CmsResource  resource = cms.readResource(sourceVFSPath);
			CmsResourceState  estado = resource.getState();
	    	String         estadoStr = estado.toString();
	    	
			cms.deleteResource(sourceVFSPath, CmsResource.DELETE_PRESERVE_SIBLINGS);
			
	    	if( !estadoStr.equals("2") )
			     OpenCms.getPublishManager().publishResource(cms,sourceVFSPath);
		
		} catch (CmsException e1) {
			CmsLog.getLog(this).error("Error al borrar audios: "+e1.getMessage());
		} catch (Exception e) {
			CmsLog.getLog(this).error("Error al borrar audios: "+e.getMessage());
		}
		
	}
	
	public void publishAudios(List audios,  CmsObject cms){
		
		try{	
			
			List<CmsResource> publishList = new ArrayList<CmsResource>();
            Iterator it =  audios.iterator();

			while(it.hasNext())
			{
				String audio = (String)it.next();
				
				if (!cms.getLock(audio).isUnlocked()){
				     if(!cms.getLock(audio).isOwnedBy(cms.getRequestContext().currentUser())){
					      cms.changeLock(audio);
				    }
				}else{
				     cms.lockResource(audio);
				}
				
				CmsResource  resource = cms.readResource(audio);
				publishList.add(resource);
				
                CmsRelationFilter filterImages = CmsRelationFilter.ALL.filterType(CmsRelationType.valueOf("audioImage"));
			    List<CmsRelation> relatedImages = cms.getRelationsForResource(resource, filterImages);
			
			    for (CmsRelation rel : relatedImages){
					
					String relation = "";
			      	
			      	String rel1 = cms.getRequestContext().removeSiteRoot(rel.getTargetPath());
	   				String rel2 = cms.getRequestContext().removeSiteRoot(rel.getSourcePath());

	   				if (rel1.equals(audio))
	   					relation = rel2;
	   				else
	   					relation = rel1;
	   				
	   				if (!cms.getLock(relation).isUnlocked()){
					     if(!cms.getLock(relation).isOwnedBy(cms.getRequestContext().currentUser())){
						      cms.changeLock(relation);
					    }
					}else{
					     cms.lockResource(relation);
					}
					
					CmsResource  resourceRelation = cms.readResource(relation);
	   				
	   				publishList.add(resourceRelation);
				 }
				
			}
			OpenCms.getPublishManager().publishProject(cms, new CmsLogReport(Locale.getDefault(), this.getClass()), OpenCms.getPublishManager().getPublishList(cms,publishList, false));

		   } catch (CmsException e1) {
			  CmsLog.getLog(this).error("Error al publicar audios: "+e1.getMessage());
		   }
	}
	
	public String editProperties(String audioPath, HashMap<String,String> propertiesM, CmsObject cmsO, CmsUser currentAdmin){
		
		String msg = null;
		
		try{
			com.tfsla.utils.CmsResourceUtils.forceLockResource(cmsO,audioPath);
			
			CmsProperty             prop = new CmsProperty();
			List<CmsProperty> properties = new ArrayList<CmsProperty>();
			
			Set           set = propertiesM.entrySet();
		    Iterator iterator = set.iterator();
		    
		    while(iterator.hasNext()) {
		         Map.Entry mentry = (Map.Entry)iterator.next();
		         
		         prop = new CmsProperty();
				 prop.setName((String) mentry.getKey());
				 prop.setAutoCreatePropertyDefinition(true);
				 prop.setStructureValue((String) mentry.getValue());
				 properties.add(prop);
		    }
		    
			cmsO.writePropertyObjects(audioPath, properties);
			
			CmsResource sourceRes = cmsO.readResource(audioPath, CmsResourceFilter.ALL);
			hardTouch(sourceRes,cmsO);
			
			com.tfsla.utils.CmsResourceUtils.unlockResource(cmsO,audioPath, false);
			
		}catch(Exception e){
	           msg = "Error - Path:"+audioPath+" ["+e.getMessage()+"]";
	    }
		
		return msg;
	}
	
	public void assignImage(String audioPath, String imagePath) throws CmsException{
		
		
		if (!getCmsObject().getLock(audioPath).isUnlocked()){
		     if(!getCmsObject().getLock(audioPath).isOwnedBy(getCmsObject().getRequestContext().currentUser()))
		    	 getCmsObject().changeLock(audioPath);
		}else{
			getCmsObject().lockResource(audioPath);
		}
		
		if (!getCmsObject().getLock(imagePath).isUnlocked()){
		     if(!getCmsObject().getLock(imagePath).isOwnedBy(getCmsObject().getRequestContext().currentUser()))
		    	 getCmsObject().changeLock(imagePath);
		}else{
			getCmsObject().lockResource(imagePath);
		}
		
		CmsProperty prop = new CmsProperty();
        prop.setName("prevImage");
        prop.setValue(imagePath, CmsProperty.TYPE_INDIVIDUAL);
        getCmsObject().writePropertyObject(audioPath,prop);
        
        CmsResource sourceRes = getCmsObject().readResource(audioPath, CmsResourceFilter.ALL);
		hardTouch(sourceRes,getCmsObject());
      
        getCmsObject().addRelationToResource( audioPath, imagePath, "audioImage");
		
        getCmsObject().unlockResource(audioPath);
        getCmsObject().unlockResource(imagePath);
	}
	
	private void hardTouch(CmsResource resource , CmsObject cmsO) throws CmsException {

        CmsFile file = cmsO.readFile(resource);
        file.setContents(file.getContents());
        cmsO.writeFile(file);
    }


}
