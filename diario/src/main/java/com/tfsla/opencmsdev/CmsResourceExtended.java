package com.tfsla.opencmsdev;

import org.opencms.db.CmsResourceState;
import org.opencms.file.CmsResource;
import org.opencms.util.CmsUUID;

public class CmsResourceExtended extends CmsResource {
	
	private int indexPublication;
		
	public CmsResourceExtended(CmsUUID structureId, CmsUUID resourceId,
			String rootPath, int type, boolean isFolder, int flags,
			CmsUUID projectId, CmsResourceState state, long dateCreated,
			CmsUUID userCreated, long dateLastModified,
			CmsUUID userLastModified, long dateReleased, long dateExpired,
			int linkCount, int size, long dateContent, int version) {
		
		super(structureId, resourceId, rootPath, type, isFolder, flags,
				projectId, state, dateCreated, userCreated, dateLastModified,
				userLastModified, dateReleased, dateExpired, linkCount, size,
				dateContent, version);
	}
	
	public static CmsResourceExtended getInstance(CmsResource resource, int indexPublication){
		CmsResourceExtended resourceExtended = new CmsResourceExtended(resource.getStructureId(), resource.getResourceId(), resource.getRootPath(),
				resource.getTypeId(), resource.isFolder(), resource.getFlags(),
				resource.getProjectLastModified(), resource.getState(), resource.getDateCreated(),
				resource.getUserCreated(), resource.getDateLastModified(),
				resource.getUserLastModified(), resource.getDateReleased(), resource.getDateExpired(),
				resource.getSiblingCount(), resource.getLength(),
				resource.getDateContent(), resource.getVersion());
		
		resourceExtended.setIndexPublication(indexPublication);
		return resourceExtended;
	}
	
	public int getIndexPublication(){
		return indexPublication;
	}
	
	public void setIndexPublication(int indexPublication){
		this.indexPublication = indexPublication;
	}	

}
