package com.tfsla.publishFolders.jobs;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.opencms.file.CmsObject;
import org.opencms.scheduler.I_CmsScheduledJob;
import org.opencms.main.OpenCms;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;

public class CreatePublishFolders implements I_CmsScheduledJob{
	
	protected String results = "";
	
	@Override
	public String launch(CmsObject cms, Map parameters) throws Exception {
		results = "Ejecutando creación y publicación automatizada de carpetas: ";
		
		String site = cms.getRequestContext().getSiteRoot();
		String folder = (String) parameters.get("folder");
		String folderFormat = (String) parameters.get("folderFormat");
		String publication = (String) parameters.get("publication");
		String section = (String) parameters.get("section"); 
		String nextDayStr = (String) parameters.get("nextDay");
		
		Boolean nextDay = false;
		
		if(nextDayStr!=null && nextDayStr.toLowerCase().trim().equals("true"))
			nextDay = true;
			

		int folderType = OpenCms.getResourceManager().getResourceType("imagegallery").getTypeId();
		
		if(folder==null || ( folder!=null && folder.equals("") ))
			folder = "/img";
		
		if(folderFormat==null || (folderFormat!=null && folderFormat.equals("")) )
		{
			CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
			folderFormat = config.getParam(site, publication, "imageUpload", "vfsSubFolderFormat","yyyy/MM/dd");
		}
		
		Map<String,String> parametersFolder = new HashMap<String,String>();
		
		if(section!=null && !section.equals(""))
			parametersFolder.put("section",section);
		
		results += "\nSite: "+site+" Publicación: "+publication+" Carpeta: "+folder+" SubfolderFormat: "+folderFormat;
		
		Date now = new Date();
		String createFolder = "";
		
		if(nextDayStr==null || !nextDay) {
		   createFolder = getVFSSubFolderPath(cms,folder, folderType, folderFormat, parametersFolder, now);
		   results += "\nSe creó y publicó la carpeta "+folder+"/"+createFolder;
		}
		
		if(nextDayStr==null || nextDay) {
			Calendar c = Calendar.getInstance();
		    c.setTime(now);
		    c.add(Calendar.DATE, 1);
		    Date currentDatePlusOne = c.getTime();
		     
		    createFolder = getVFSSubFolderPath(cms,folder, folderType, folderFormat, parametersFolder, currentDatePlusOne);
				
			results += "\nSe creó y publicó la carpeta "+folder+"/"+createFolder;
		}
		
		return results;
	}
	
	
	public String getVFSSubFolderPath(CmsObject cms,String parentPath, int folderType, String subFolderFormat, Map<String,String> parameters, Date now) throws Exception {

		String subFolder = "";
		if (subFolderFormat.trim().equals(""))
			return "";
		
		String partialFolder = parentPath + "/";
		String firstFolderCreated  = "";
		String[] parts = subFolderFormat.split("/");
		
		for (String part : parts) {
			String subfolderName = "";
			if (parameters.get(part)!=null) {
				subfolderName = parameters.get(part);
			} else {
				SimpleDateFormat sdf = new SimpleDateFormat(part);
				subfolderName = sdf.format(now);
			}
			partialFolder += subfolderName;
			subFolder += subfolderName;
			
			if (!cms.existsResource(partialFolder)) {
				cms.createResource(partialFolder, folderType);
				if (firstFolderCreated.equals(""))
					firstFolderCreated  = partialFolder;
			}
			partialFolder += "/";
			subFolder += "/";
		}
		if (!firstFolderCreated.equals("")) {
			OpenCms.getPublishManager().publishResource(cms, firstFolderCreated);
		}
		return subFolder;
	}
	
}
