package com.tfsla.diario.model;

import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;

import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.TipoEdicionService;

public class TfsVideoFormat {
	private String name;
	private String duration;
	private String bitrate;
	private String size;
	private String vfsPath;
	private String link;
	private String videoFormatPath;
	
	public TfsVideoFormat(CmsObject m_cms, String sourcePath, String formatName) {
		try {
			videoFormatPath = generateVideoFormatPath(m_cms, sourcePath, formatName);
			this.loadProperties(videoFormatPath, formatName, m_cms);
		} catch (CmsException e) {
			e.printStackTrace();
		}
	}
	
	public String getHlsVideoPath(CmsObject m_cms, String sourcePath, String formatName) {
		String siteName = this.getSiteName(m_cms);
		String publication = this.getPublication(siteName);
		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
		String param = config.getItemGroupParam(siteName, publication, "videoConvert", "formats", formatName);
		String linkFolderName = getOptionParamGroup(formatName, "folder", param);
		String defaultVfsFolder = config.getParam(siteName, publication, "videoUpload", "defaultVideoFlashPath","");
		String subFolder = sourcePath.substring(0, sourcePath.lastIndexOf("/") );
		if(sourcePath.indexOf(defaultVfsFolder) > -1)
        	subFolder = subFolder.replace("/" + defaultVfsFolder, "");
		String targetName = getHlsFileName(sourcePath);
		String linkName = "/" + linkFolderName + subFolder + "/" + targetName + ".m3u8";
		return linkName;
	}
	
	public String generateVideoFormatPath(CmsObject m_cms, String sourcePath, String formatName) {
		if(formatName.toLowerCase().contains("hls")) {
			return getHlsVideoPath(m_cms, sourcePath, formatName);
		}
		String prefix = getParamFormat(m_cms, formatName, "prefix" );
		String vfsFolder = getParamFormat(m_cms, formatName, "folderVFS" );
		String filenameExt = getParamFormat(m_cms, formatName, "output" );
		String siteName = OpenCms.getSiteManager().getCurrentSite(m_cms).getSiteRoot();
		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
		
		siteName = siteName.replaceFirst("/sites/", "");
		siteName = siteName.replace("/site/", "");
		
		TipoEdicionService tService = new TipoEdicionService();
		TipoEdicion currentPublication = null;
			
		try {
			 currentPublication = tService.obtenerEdicionOnlineRoot(siteName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		String publication = "" + currentPublication.getId();
		String defaultVfsFolder = config.getParam(siteName, publication, "videoUpload", "defaultVideoFlashPath","");
        String subFolder = sourcePath.substring(0, sourcePath.lastIndexOf("/") );

        if(sourcePath.indexOf(defaultVfsFolder) > -1)
        	subFolder = subFolder.replace("/"+defaultVfsFolder,"");
        
        String filename = sourcePath.substring(sourcePath.lastIndexOf("/")+1, sourcePath.lastIndexOf("."));
        String filenameF = filename + prefix +"."+ filenameExt;
        String videoVfsPath = "/"+ vfsFolder +  subFolder + "/" + filenameF;
		
		return videoVfsPath;
	}
	
	public String getOptionParamGroup(String paramName, String type, String param) {
		String[] paramParts = param.split(",");
		for(String part : paramParts) {
			int ind = part.indexOf(":");
			String partName = part.substring(0, ind);
			if(partName.equals(type)) {
				return part.substring(ind+1);
			}
		}
		return null;
	}
	
	public String getOptionParamGroup(String paramName, String type, CmsObject m_cms) {
		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
		String siteName = getSiteName(m_cms);
		String publication = getPublication(siteName);
		String param = config.getItemGroupParam(siteName, publication, "videoConvert", "formats", paramName);
		return getOptionParamGroup(paramName, type, param);
	}
	
	public String getParamFormat(CmsObject m_cms, String paramName, String type) {
		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
		String siteName = getSiteName(m_cms);
		String publication = getPublication(siteName);
		String moduleConfigName = "videoConvert";
		
		paramName = paramName.trim();
		String param = config.getItemGroupParam(siteName, publication, moduleConfigName,"formats",paramName);
		
		if(param == null || param.equals("")) {
			return "";
		}
		
		if(paramName.toLowerCase().contains("hls")) {
			return this.getOptionParamGroup(paramName, type, param);
		}
		
		String [] paramParts = param.split(",");
		int ind = 0;
		
		String value = null;
		
		if(type.equals("options")) {
		    ind = paramParts[0].indexOf(":");
			value = paramParts[0].substring(ind+1);
		}
		
		if(type.equals("output")) {
		    ind = paramParts[1].indexOf(":");
			value = paramParts[1].substring(ind+1);
		}
		
		if(type.equals("folderVFS")) {
		    ind = paramParts[2].indexOf(":");
			value = paramParts[2].substring(ind+1);
		}
		
		if(type.equals("folderRFS")) {
		    ind = paramParts[3].indexOf(":");
			value = paramParts[3].substring(ind+1);
		}
		
		if(type.equals("folderFTP")) {
		    ind = paramParts[4].indexOf(":");
			value = paramParts[4].substring(ind+1);
		}
		
		if(type.equals("prefix")) {
		    ind = paramParts[5].indexOf(":");
			value = paramParts[5].substring(ind+1);
		}
		
		return value;
    }
	
	private void loadProperties(String videoFormatPath, String formatName, CmsObject m_cms) throws CmsException {
		CmsProperty prop;
		CmsResource res = m_cms.readResource(videoFormatPath);
		
		vfsPath = videoFormatPath;
		   name = formatName;
		
		prop = m_cms.readPropertyObject(res, "video-duration", false);
		if (prop!=null)
			duration = prop.getValue();
		
		prop = m_cms.readPropertyObject(res, "video-bitrate", false);
		if (prop!=null)
			bitrate = prop.getValue();
		
		prop = m_cms.readPropertyObject(res, "video-size", false);
		if (prop!=null)
			size = prop.getValue();
		
		CmsFile file = m_cms.readFile(res); 
		link = new String(file.getContents());
	}
	
	private String getSiteName(CmsObject m_cms) {
		String  siteName = OpenCms.getSiteManager().getCurrentSite(m_cms).getSiteRoot();
		siteName = siteName.replaceFirst("/sites/", "");
		siteName = siteName.replace("/site/", "");
		
		return siteName;
	}
	
	private String getPublication(String siteName) {
		TipoEdicionService tService = new TipoEdicionService();
		TipoEdicion currentPublication = null;
			
		try {
			 currentPublication = tService.obtenerEdicionOnline(siteName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return "" + currentPublication.getId();
	}
	
	private String getHlsFileName(String sourcePath){
		String fileName = sourcePath.substring(sourcePath.lastIndexOf("/")+1, sourcePath.length()-1); 
		fileName = fileName.substring(0,fileName.lastIndexOf("."));
		
		return fileName;
	}
	
	public String getSize() {
		return size;
	}
	
	public void setSize(String size) {
		this.size = size;
	}
	
	public String getBitrate() {
		return bitrate;
	}
	
	public void setBitrate(String bitrate) {
		this.bitrate = bitrate;
	}
	
	public String getDuration() {
		return duration;
	}
	
	public void setDuration(String duration){
		this.duration = duration;
	}
	 
	public String getVfspath() {
		return vfsPath;
	}
	
	public void setVfspath(String path) {
		this.vfsPath = path;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}
	
	public String getVideoFormatPath() {
		return this.videoFormatPath;
	}
}