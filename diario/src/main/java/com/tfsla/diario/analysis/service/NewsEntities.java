package com.tfsla.diario.analysis.service;

import java.util.List;

import org.apache.commons.logging.Log;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import com.amazonaws.services.comprehend.model.Entity;
import com.tfsla.diario.ediciones.services.AmzComprehendService;
import com.tfsla.diario.ediciones.services.AmzComprehendService.DocEntity;
import com.tfsla.diario.terminos.data.TermsTypesDAO;
import com.tfsla.diario.terminos.model.TermsTypes;

public class NewsEntities {
	
	private static final Log LOG = CmsLog.getLog(NewsEntities.class);
	
	private CmsObject cmsObject;
	
	private boolean usingComprehend;
	private AmzComprehendService comprehend;
	private MltNewsAnalysisService mlt;
	
	private final float weight =0.3f;
	
	private CPMConfig config ;

	private String siteName ;
	private long termTypeId = -1;
	private String enableEntitiesType = "";
	private String enableEntitiesScore = "0";
	
	public NewsEntities(CmsObject cmsObject) {
		this.cmsObject = cmsObject;

		comprehend = AmzComprehendService.getInstance(cmsObject);
		usingComprehend = comprehend.isAmzComprehendEnabled();
		mlt = new MltNewsAnalysisService();
		
		config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
		siteName = OpenCms.getSiteManager().getCurrentSite(cmsObject).getSiteRoot();

	}

	private long getCmsmediosConfig(String publicationId, CmsObject cmsObject) {
		if (termTypeId!=-1)
			return termTypeId;
		
		String type = config.getParam(siteName, publicationId, "terms","termsType","tags");
		
		TermsTypesDAO ttDAO = new TermsTypesDAO ();
		TermsTypes oTermTypes = null;
		Long typeId = new Long(1);
		try {
			oTermTypes = ttDAO.getTermType(type);
			typeId = oTermTypes.getId_termType();
		} catch (Exception e) {
			
		}
		
		termTypeId = typeId;
		
		String entityTypesCMS = config.getParam(siteName, publicationId, "adminNewsConfiguration","enableEntitiesTypes","ORGANIZATION,PERSON");
		String entityScoreCMS = config.getParam(siteName, publicationId, "adminNewsConfiguration","enableEntitiesScore","0.70");
		
		enableEntitiesType = entityTypesCMS;
		enableEntitiesScore = entityScoreCMS;
		
		LOG.debug("enableEntitiesType : " +enableEntitiesType);
		LOG.debug("entityScoreCMS : " +entityScoreCMS);
		return typeId;
	}
	
	/*
	 * Este metodo develve las entidades posibles que se obtienen de amz  
	 * Se procesan segun tipo y score seteados dentro del cmsmedios.xml
	 * Se validan si las mismos ya existen en nuestra base de datos. 
	 */
	
	public List<DocEntity> analizeNews(String newsPath, String tEdicionId) throws Exception {
		
		
		LOG.debug("newsPath - tEdicionId"+ newsPath +" - "+ tEdicionId);
		getCmsmediosConfig(tEdicionId,cmsObject);
		
		List<DocEntity> entities = null;		
		List<Entity> amzEntities=null;
		if (!usingComprehend) 
			return entities;
		
		CmsFile file = cmsObject.readFile(newsPath);
		comprehend.setResource(file, new String[]{"titulo","copete","cuerpo","noticiaLista[x]/titulo","noticiaLista[x]/cuerpo"});
		
		amzEntities = comprehend.dectecEntities();
		
		List<DocEntity> docEntities = comprehend.processEntitiesCMS(amzEntities,enableEntitiesType,enableEntitiesScore);
				
		return docEntities;
	}
	
}
