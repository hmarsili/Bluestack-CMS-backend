package com.tfsla.rankUsers.action;

import org.opencms.main.CmsEvent;
import org.opencms.main.I_CmsEventListener;
import org.opencms.util.CmsUUID;

import com.tfsla.rankUsers.service.RankService;

public class DeleteStatisticsOnPublishEvent implements I_CmsEventListener {

	public void cmsEvent(CmsEvent event) {
		
		if (event.getType()==I_CmsEventListener.EVENT_USER_MODIFIED) {
			
			String id="";
			if (event.getData().get(I_CmsEventListener.KEY_USER_ID) instanceof CmsUUID)
			{
				id = ((CmsUUID)event.getData().get(I_CmsEventListener.KEY_USER_ID)).getStringValue();
			}
			else id = (String) event.getData().get(I_CmsEventListener.KEY_USER_ID);
			
			String useraction = (String) event.getData().get(I_CmsEventListener.KEY_USER_ACTION);
			
			if (useraction.equals(I_CmsEventListener.VALUE_USER_MODIFIED_ACTION_DELETE_USER)) {
				RankService rService = new RankService();
				rService.removeUserFromStatistics(id);
			}
		}
		


	}
}
