package com.tfsla.cmsMedios.releaseManager.installer.service;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsContextInfo;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.scheduler.CmsScheduledJobInfo;
import org.opencms.site.CmsSite;
import org.opencms.workplace.CmsWorkplaceAction;

import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.TipoEdicionService;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class JobsService {
	public JobsService(JSONObject manifest, CmsObject cmsObject) {
		this.manifest = manifest;
		this.cmsObject = cmsObject;
	}
	
	@SuppressWarnings("unchecked")
	public int createJobs() throws CmsException, InterruptedException {
		JSONArray jobs = manifest.getJSONArray("jobs");
		int counter = 0;
		for (Object job : jobs) {
			JSONObject jsonJob = (JSONObject)job;
			if (jsonJob.containsKey("runPerSite")) {
				for (CmsSite site : (List<CmsSite>)OpenCms.getSiteManager().getAvailableSites(this.cmsObject, true)) {
					String proy = site.getSiteRoot().replaceAll("/sites/", "");
					TipoEdicionService tService = new TipoEdicionService();
					for (TipoEdicion tEdicion : tService.obtenerTipoEdiciones(proy)) {
						createJob(String.format(SITE_JOB, site.getTitle(), jsonJob.getString("name")), jsonJob, site.getSiteRoot(), String.valueOf(tEdicion.getId()), tEdicion.getNombre());
						counter++;
					}
				}
			} else {
				createJob(jsonJob.getString("name"), jsonJob, null, null, null);
				counter++;
			}
		}
		return counter;
	}
	
	protected void createJob(String name, JSONObject jsonJob, String site, String publicationID, String publicationName) throws CmsException, InterruptedException {
		if (this.getJobByName(name) != null) return;
		
		CmsScheduledJobInfo newJob = new CmsScheduledJobInfo();
		newJob.setJobName(name);
		newJob.setClassName(jsonJob.getString("className"));
		newJob.setCronExpression(jsonJob.getString("cronExpression"));
		newJob.setActive(false);
		
		if(jsonJob.containsKey("params") || publicationID != null || publicationName != null) {
			SortedMap<String, String> params = new TreeMap<String, String>();
			if (jsonJob.containsKey("params")) {
				for(Object p : jsonJob.getJSONArray("params")) {
					JSONObject param = (JSONObject)p;
					params.put(param.getString("name"), param.getString("value"));
				}
			}
			
			if (publicationID != null) {
				params.put("publicationID", publicationID);
			}
			
			if (publicationName != null) {
				params.put("publication", publicationName);
			}
			newJob.setParameters(params);
		}
		
		if (site == null || site.equals("")) {
			site = "/";
		}
		
		CmsContextInfo contextInfo = new CmsContextInfo(cmsObject.getRequestContext());
		contextInfo.setSiteRoot(site);
		contextInfo.setProjectName("Offline");
		CmsWorkplaceAction action = CmsWorkplaceAction.getInstance();
		contextInfo.setUserName(
			action.getCmsAdminObject().getRequestContext().currentUser().getName()
		);
		newJob.setContextInfo(contextInfo);
		OpenCms.getScheduleManager().scheduleJob(cmsObject, newJob);
		SetupProgressService.reportProgress("Created job " + jsonJob.getString("name"));
		Thread.sleep(1000);
	}
	
	@SuppressWarnings("rawtypes")
	protected CmsScheduledJobInfo getJobByName(String jobName) {
		List jobs = OpenCms.getScheduleManager().getJobs();
		for(Object job : jobs) {
			CmsScheduledJobInfo jobInfo = (CmsScheduledJobInfo)job;
			if(jobInfo.getJobName().equals(jobName)) return jobInfo;
		}
		return null;
	}
	
	protected JSONObject manifest;
	protected CmsObject cmsObject;
	protected final String SITE_JOB = "(%s) %s";
}
