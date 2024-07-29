package com.tfsla.utils;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
//import org.opencms.file.CmsResourceFilter;
import org.opencms.lock.CmsLockType;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsUUID;

import com.tfsla.exceptions.ApplicationException;
import com.tfsla.opencms.exceptions.ProgramException;
import com.tfsla.workflow.QueryBuilder;
import com.tfsla.workflow.ResultSetProcessor;

public class CmsResourceUtils {

	/**
	 * Desbloquea un recurso del vfs
	 * @param cms
	 * @param url
	 * @param throwExceptionIfIsNotPossible
	 */
	public static void unlockResource(CmsObject cms, String url, boolean throwExceptionIfIsNotPossible) {
		try {
			if(!cms.getLock(url).isUnlocked()) {
				
				try {
					cms.unlockResource(url);
				}
	        	catch (Exception e)
	        	{
	        		
//	                cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));

	        		cms.changeLock(url);
/*	        		
	        		int flags = cms.readResource(url, CmsResourceFilter.ALL).getFlags();
	                if ((flags & CmsResource.FLAG_TEMPFILE) == CmsResource.FLAG_TEMPFILE) {
	                    flags ^= CmsResource.FLAG_TEMPFILE;
	                    cms.chflags(url, flags);
	                }
*/
	        		cms.unlockResource(url);
	        	}
			}
		}
		catch (CmsException e) {
			if(throwExceptionIfIsNotPossible) {
				throw new ApplicationException("No se pudo deslockear el resource " + url, e);
			}
		}
	}

	/** 
	 * 
	 * @param cms
	 * @param resource
	 * @param throwExceptionIfIsNotPossible
	 * @return si cambio el estado del lockeo
	 */
	public static boolean lockResource(CmsObject cms, CmsResource resource, boolean throwExceptionIfIsNotPossible) {
		try {
			 if(cms.getLock(resource).getType() == CmsLockType.UNLOCKED) {
				cms.lockResource(CmsResourceUtils.getLink(resource));
				return true;
			 }
			 return false;
		}
		catch (CmsException e) {
			if(throwExceptionIfIsNotPossible) {
				throw new ApplicationException("No se pudo deslockear el resource " + CmsResourceUtils.getLink(resource), e);
			}
			return false;
		}
	}

	/**
	 * Fuerza el bloqueo de un recurso.
	 * @param cms (CmsObject)
	 * @param resource (String)
	 * @throws CmsException
	 */
	public static void forceLockResource(CmsObject cms, String resource) throws CmsException {

		if (cms.getLock(resource).isUnlocked())
	    	cms.lockResource(resource);
	    else
	    {
	    	try {
	    		cms.unlockResource(resource);
	    		cms.lockResource(resource);
	    	}
	    	catch (Exception e)
	    	{
	        	cms.changeLock(resource);	            		
	    	}
	    }
	}
	
	public static String getLink(CmsResource file) {
        return getLink(file.getRootPath());
    }

	public static String getLink(String file) {
        if (file.indexOf("/sites") != -1) {
            int index = file.indexOf("/", "/sites/".length());
            return file.substring(++index);
        }
        return file;
	}

		
	public static String getTempFileName(String resourceName) {
		return resourceName.substring(0, resourceName
			.lastIndexOf('/') + 1)
			+ "~"
			+ resourceName.substring(resourceName.lastIndexOf('/') + 1,
				resourceName.length());
	}
	
	protected static final Log LOG = CmsLog.getLog(CmsResourceUtils.class);

	public static String getPropertyInShadowMode(CmsObject cms, CmsResource resource, String propertyDefName) {
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
	
	public static void updatePropertyInShadowMode(CmsObject cms, CmsResource resource,
			String propertyName, String value) {
				QueryBuilder queryBuilder = new QueryBuilder(cms);
				queryBuilder.setSQLQuery(
						"UPDATE CMS_OFFLINE_PROPERTIES SET "
						+ "PROPERTY_VALUE=? "
						+ "WHERE "
						+ "PROPERTYDEF_ID = (select PROPERTYDEF_ID from CMS_OFFLINE_PROPERTYDEF where PROPERTYDEF_NAME = ?) AND "
						+ "PROPERTY_MAPPING_ID = ? ");
				
				String mappingId = resource.getStructureId().toString(); // PROPERTY_MAPPING_ID
				
				queryBuilder.addParameter(value);
				queryBuilder.addParameter(propertyName);
				queryBuilder.addParameter(mappingId);
				
				LOG.debug("updatePropertyInShadowMode: " + queryBuilder.toString());
				
				queryBuilder.execute();
				
			}
	
	public static void createPropertyInShadowMode(CmsObject cms, CmsResource resource,
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

	public static boolean mustDiscardTempResourceChanges(CmsObject cmsObject, CmsResource resource) throws CmsException {

		CmsProperty title = cmsObject.readPropertyObject(resource, "title", false);
		LOG.debug("chequeando si recurso debe ignorar cambios " + resource + " >> " + title);

		String _discardChanges = getPropertyInShadowMode(cmsObject, resource, "disardChanges");

		LOG.debug("chequeando si recurso debe ignorar cambios " + resource + " >> " + _discardChanges);
		
		boolean discardChanges = false;
		if (_discardChanges!=null && _discardChanges.trim().toLowerCase().equals("true"))
			discardChanges = true;
		boolean unchangedContent = (resource.getDateContent() == resource.getDateCreated());
		
		return discardChanges || unchangedContent;
	}
	
}
