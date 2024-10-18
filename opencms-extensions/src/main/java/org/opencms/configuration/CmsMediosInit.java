package org.opencms.configuration;

import java.util.ArrayList;
import java.util.List;
import jakarta.servlet.http.HttpSession;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;

public class CmsMediosInit {

	private static CmsMediosInit instance;
	 
	List<I_HitCounterService> hitServices = new ArrayList<I_HitCounterService>();
	public void addService(I_HitCounterService service) {
		hitServices.add(service);
	}
	
	public void addHit(CmsResource res,CmsObject cms, HttpSession session) {
		for (I_HitCounterService service : hitServices)
			service.countHitView(res, cms, session);
		
	}
	
	public static CmsMediosInit getInstance() {
		if (instance == null) {
			instance = new CmsMediosInit();
		}
		return instance;
	}

}
