package com.tfsla.rankUsers.action;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsEventListener;
import org.opencms.util.CmsUUID;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.I_CmsXmlContentValue;

import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.TipoEdicionService;
import com.tfsla.opencms.exceptions.ProgramException;
import com.tfsla.workflow.QueryBuilder;
import com.tfsla.workflow.ResultSetProcessor;

public abstract class A_UserPublishRanks {

	protected static final Log LOG = CmsLog.getLog(A_UserPublishRanks.class);

	protected static String PROPERTYNAME_RANKED = "alreadyRanked";
	protected static String PROPERTYNAME_RANKPOSITIVE = "RankedPositive";

	public A_UserPublishRanks() {
		super();
	}

	public String getPropertyInShadowMode(CmsObject cms, CmsResource resource, String propertyDefName) {
		QueryBuilder<String> queryBuilder = new QueryBuilder<String>(cms);
		
	
		queryBuilder.setSQLQuery(
				"SELECT " +
				" PROPERTY_VALUE " +
				"FROM " +
				" CMS_OFFLINE_PROPERTIES " +
				"INNER JOIN " +
				" CMS_OFFLINE_STRUCTURE  " +
				"ON  " +
				" STRUCTURE_ID=PROPERTY_MAPPING_ID " +
				"INNER JOIN " +
				" CMS_OFFLINE_PROPERTYDEF " +
				"ON " +
				" CMS_OFFLINE_PROPERTYDEF.PROPERTYDEF_ID = CMS_OFFLINE_PROPERTIES.PROPERTYDEF_ID " +
				"WHERE " +
				" CMS_OFFLINE_PROPERTIES.PROPERTY_MAPPING_TYPE=1 " +
				" AND RESOURCE_PATH=? " +
				" AND PROPERTYDEF_NAME = ?;");
	
		queryBuilder.addParameter(resource.getRootPath());
		queryBuilder.addParameter(propertyDefName);
		
		LOG.debug("getPropertyInShadowMode: " + queryBuilder.toString());
		
		ResultSetProcessor<String> proc = new ResultSetProcessor<String>() {
	
			private String value = null;
	
			public void processTuple(ResultSet rs) {
	
				try {
					String value = rs.getString(1);
					if (value!=null)
							this.value = value;
				}
				catch (SQLException e) {
					throw ProgramException.wrap(
							"error al intentar recuperar la property", e);
				}
			}
	
			public String getResult() {
				return this.value;
			}
		};
	
		return queryBuilder.execute(proc);
	}

	public void updatePropertyInShadowMode(CmsObject cms, CmsResource resource,
			String propertyName, String value) {
				QueryBuilder queryBuilder = new QueryBuilder(cms);
				queryBuilder.setSQLQuery("UPDATE CMS_OFFLINE_PROPERTIES "
						+ "SET "
						+ "PROPERTY_VALUE=? "
						+ "WHERE "
						+ "PROPERTYDEF_ID=(select PROPERTYDEF_ID from CMS_OFFLINE_PROPERTYDEF where PROPERTYDEF_NAME = ?) AND "
						+ "PROPERTY_MAPPING_ID=? AND "
						+ "PROPERTY_MAPPING_TYPE=? "
					);
				
				String mappingId = resource.getStructureId().toString(); // PROPERTY_MAPPING_ID
			
				queryBuilder.addParameter(value);
			
				queryBuilder.addParameter(propertyName);
				queryBuilder.addParameter(mappingId);
				queryBuilder.addParameter(1);
				
				LOG.debug("updatePropertyInShadowMode: " + queryBuilder.toString());
				
				queryBuilder.execute();
			}

	public void createPropertyInShadowMode(CmsObject cms, CmsResource resource,
			String propertyName, String value) {
				QueryBuilder queryBuilder = new QueryBuilder(cms);
				queryBuilder.setSQLQuery(
						"INSERT INTO CMS_OFFLINE_PROPERTIES "
						+ "(PROPERTY_ID, PROPERTYDEF_ID, PROPERTY_MAPPING_ID, PROPERTY_MAPPING_TYPE, PROPERTY_VALUE) "
						+ "VALUES ("
						+ "?,"
						+ "(select PROPERTYDEF_ID from CMS_OFFLINE_PROPERTYDEF where PROPERTYDEF_NAME = ?),"
						+ "?,"
						+ "?,"
						+ "?)");
				
				String propertyUuid = new CmsUUID().toString(); // PROPERTY_ID
				String mappingId = resource.getStructureId().toString(); // PROPERTY_MAPPING_ID
			
				queryBuilder.addParameter(propertyUuid);
				queryBuilder.addParameter(propertyName);
				queryBuilder.addParameter(mappingId);
				queryBuilder.addParameter(1);
				queryBuilder.addParameter(value);
				
				LOG.debug("createPropertyInShadowMode: " + queryBuilder.toString());
				
				queryBuilder.execute();
				
			}
	
	public CmsUser getAutor(CmsResource res, CmsObject cms, String siteName, int tEd) throws CmsException
	{
		String userAnon = getParam(siteName, ""+tEd,"anonymousUser");
		String _excludeAnonymousFromRankings = getParam(siteName, ""+tEd,"excludeAnonymousFromRankings");
		boolean excludeAnonymousFromRankings = _excludeAnonymousFromRankings !=null && (_excludeAnonymousFromRankings.trim().toLowerCase().equals("yes") || _excludeAnonymousFromRankings.trim().toLowerCase().equals("true"));

		CmsFile file = cms.readFile(res);

		CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, file);

		I_CmsXmlContentValue contentValue = content.getValue("autor/internalUser",Locale.ENGLISH);
		CmsUser user = null;
		if (contentValue!=null)
		{
			String autor = contentValue.getStringValue(cms);
			if (autor!=null && autor.trim().length()>0)
			{
				if (!excludeAnonymousFromRankings || (excludeAnonymousFromRankings && !autor.equals(userAnon))) {
					user = cms.readUser(autor);
					if (user==null)
						user = cms.readUser(new CmsUUID(autor));
				}
			}
			
			
		}
		if (user!=null)
			return user;
		
		String indexingMode = getParam(siteName, ""+tEd,"indexingModeOnEmpty");
		//empty | anonymousUser | newsCreator
		if (!excludeAnonymousFromRankings && indexingMode.equals("anonymousUser")) {
			try {
				user = cms.readUser(userAnon);
			}
			catch (org.opencms.db.CmsDbEntryNotFoundException e){}
		} 
		else if (indexingMode.equals("newsCreator")) {
			try {
				user = cms.readUser(content.getFile().getUserCreated());
			}
			catch (org.opencms.db.CmsDbEntryNotFoundException e){}
		}

		return user;
	}
	
	private String getParam(String siteName, String publicationName, String paramName)
	{
    	String module = "newsAuthor";
 		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
 		
		return config.getParam(siteName, publicationName, module, paramName, "userCreated");

	}
	
	protected int getPublicationID(CmsObject cms, CmsResource resource,
			String siteName) {
		int tEd=0;
		try {
			String proyecto = siteName.replaceFirst("/sites/", "");
			
			String urlNoPath = cms.getRequestContext().removeSiteRoot(resource.getRootPath());
			TipoEdicionService tService = new TipoEdicionService();
			TipoEdicion tEdicion;
			
				tEdicion = tService.obtenerTipoEdicion(cms, urlNoPath);
			
			if (tEdicion==null)
				tEdicion = tService.obtenerEdicionOnline(proyecto);

			if (tEdicion!=null) {
				tEd = tEdicion.getId();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return tEd;
	}
	
	protected String getState(CmsObject cms, CmsResource resource) {
		
	    CmsProperty property=null;
		try {
			property = cms.readPropertyObject(resource, "state", false);
		} catch (CmsException e) {
			LOG.debug("Error reading property state",e);
		}
	    
	    if (property!=null)
	    	return property.getValue("");
	    
	    return "";
	}



}