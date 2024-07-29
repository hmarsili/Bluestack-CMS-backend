package com.tfsla.rankUsers.service;

import java.util.List;

import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.i18n.CmsMessages;
import org.opencms.main.CmsException;

import com.tfsla.statistics.model.TfsHitPage;
import com.tfsla.statistics.model.TfsKeyValue;
import com.tfsla.statistics.service.I_statisticsDataCollector;
import org.opencms.util.CmsUUID;

public class TfsRankUserDataCollector implements I_statisticsDataCollector {

	public TfsKeyValue[] collect(CmsObject cms, CmsResource res, CmsUser user, String sessionId, TfsHitPage page) throws Exception {

		String autor = page.getAutor();
		if (autor!=null) {
			try {
						
				CmsUser cmsUser = cms.readUser(new CmsUUID(autor));
				List<CmsGroup> groups = cms.getGroupsOfUser(cmsUser.getName(),true);
			
				if (groups.size()>0)
				{
					TfsKeyValue[] grupos = new TfsKeyValue[groups.size()+1];
		
					int j=0;
					for (CmsGroup group : groups)
					{
						grupos[j] = new TfsKeyValue();
						grupos[j].setKey("group" + (j+1));
						grupos[j].setValue(group.getId().getStringValue());
						j++;
					}
					
					grupos[j] = new TfsKeyValue();			
					grupos[j].setKey("ou");
					grupos[j].setValue(cmsUser.getOuFqn());
					
					return grupos;
				}
		
				TfsKeyValue[] keyValues = new TfsKeyValue[1];
				keyValues[0] = new TfsKeyValue();
				keyValues[0].setKey("ou");
				keyValues[0].setValue(cmsUser.getOuFqn());
	
				return keyValues;
			}
			catch (CmsException e) {
				TfsKeyValue[] keyValues = new TfsKeyValue[1];
				keyValues[0] = new TfsKeyValue();
				keyValues[0].setKey("ou");
				keyValues[0].setValue("");
				
				return keyValues;
			}
		}
		return null;

	}

	public String getContentName() {
		return null;
	}

	public String getContentType() {
		return "";
	}

	public String getValue(CmsObject cms, String uid, String key)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public String getDateValue(CmsMessages msg, CmsObject cms, String uid,
			String key) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
