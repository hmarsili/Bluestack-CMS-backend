package com.tfsla.diario.ediciones.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.UserPrincipal;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpSession;
import net.sf.json.JSONObject;


import org.apache.commons.logging.Log;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeExternalImage;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypeUnknownFile;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.main.TfsContext;
import org.opencms.util.CmsFileUtil;

import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.file.types.TfsResourceTypeTranscription;
import com.tfsla.diario.file.types.TfsResourceTypeUploadProcessing;
import com.tfsla.utils.CmsObjectUtils;

public class TranscriptService extends UploadService {

	private static final Log LOG = CmsLog.getLog(TranscriptService.class);
	private static Map<String, TranscriptService> instances = new HashMap<String, TranscriptService>();

	public static TranscriptService getInstance(CmsObject cms, String siteName, String publication) {
    	
    	String id = siteName + "||" + publication;
    	
    	TranscriptService instance = instances.get(id);
    	
    	if (instance == null) {
	    	instance = new TranscriptService(cms,siteName, publication);

	    	instances.put(id, instance);
    	}
    	
    	instance.cmsObject = cms;
    	instance.siteName = siteName;
    	instance.publication = publication;
    	
        return instance;
    }
	
	public static TranscriptService getInstance(CmsObject cms) {
    	String siteName = OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot();
    	String publication = "0";
    	
    	if (TfsContext.getInstance()!=null && TfsContext.getInstance().getRequest()!=null && TfsContext.getInstance().getRequest().getSession()!=null) {
	    	HttpSession m_session = TfsContext.getInstance().getRequest().getSession();
	    	TipoEdicion currentPublication = (TipoEdicion) m_session.getAttribute("currentPublication");
	    	
	    	if (currentPublication!=null)
	    		publication = String.valueOf(currentPublication.getId()); 
    	}
    	
    	if(publication.equals("0")){
			try {
				publication = String.valueOf(PublicationService.getPublicationId(cms));
			} catch (Exception e) {
				LOG.error(e);
			}
    	}

    	return getInstance( cms, siteName, publication);
    	
    }
	
	public TranscriptService(CmsObject cmsObject, String siteName, String publication) {
		this.loadProperties(siteName,publication);
	}
	
	public boolean isAmzTranslateEnabled() {
		return CmsMedios.getInstance().getCmsParaMediosConfiguration().getBooleanParam(siteName, publication, getModuleName(), "AmzTranscribeEnabled", false);
	}

	public void loadProperties(String siteName, String publication) {
	   	//String module = getModuleName();
	 	//CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();

	 	this.siteName = siteName;
		this.publication = publication;
	 	
		
		loadBaseProperties(siteName, publication);
	}
			
	
	@Override
	public String uploadFTPFile(String path, String fileName, Map<String,String> parameters, InputStream content) throws Exception {
		List properties = new ArrayList(0);
		return uploadFTPFile(path, fileName, parameters, content,properties);
	}
	
	@Override
	public String uploadFTPFile(String path, String fileName, Map<String,String> parameters, InputStream content, List properties) throws Exception {
		throw new UnsupportedOperationException("Feature not supported.");
	}
	
	@Override
	public CmsResource uploadVFSFile(String path, String fileName, InputStream content) throws CmsException, IOException {
		throw new UnsupportedOperationException("Feature not supported.");
	}
	
	@Override
	public CmsResource uploadVFSFile(String path, String fileName, InputStream content, List properties) throws CmsException, IOException {
		throw new UnsupportedOperationException("Feature not supported.");
	}
	
	@Override
	public String uploadRFSFile(String path, String fileName, Map<String,String> parameters, InputStream content) throws Exception {
		throw new UnsupportedOperationException("Feature not supported.");
	}
	
	@Override
	public String uploadRFSFile(String path, String fileName, Map<String,String> parameters, InputStream content, List properties) throws Exception {
		throw new UnsupportedOperationException("Feature not supported.");
	}
	
	@Override
	@Deprecated
	public String uploadAmzFile(String path, String fileName, Map<String,String> parameters, InputStream content) throws Exception {
		List properties = new ArrayList(0);
		return uploadAmzFile(path, fileName, parameters, content, properties);
	}
	
	@Override
	@Deprecated
	public String uploadAmzFile(String path, String fileName, Map<String,String> parameters, InputStream content, List properties) throws Exception {
		return uploadAmzFile(cmsObject, path, fileName, parameters, content, properties);
	}

	@Override
	public String uploadAmzFile(CmsObject cmsObject, String path, String fileName, Map<String,String> parameters, InputStream content) throws Exception {
		List properties = new ArrayList(0);
		return uploadAmzFile(cmsObject, path, fileName, parameters, content, properties);
	}
	
	@Override
	public String uploadAmzFile(CmsObject cmsObject, String path, String fileName, Map<String,String> parameters, InputStream content, List properties) throws Exception {
		
		fileName = getValidFileName(fileName);
		fileName = checkFileName(path,fileName);
		
		String linkName = processPath(path, fileName);
		
		if(cmsObject.existsResource(linkName)){
			throw new Exception("Ya existe un recurso con el nombre: "+linkName);
		}
		
		String subFolderRFSPath = getRFSSubFolderPath(rfsSubFolderFormat, parameters);

		File dir = new File(rfsDirectory + "/" + subFolderRFSPath);
		
		if (!dir.exists())
			if (dir.mkdirs())
				checkOwnerFolder(dir);
			else
			    LOG.error("Error al intentar crear el directorio " + dir.getAbsolutePath());

		String fullPath = dir.getAbsolutePath() + "/" + fileName;
		File uploadedFile = new File(fullPath);
		try {
			try {
				content = ImageOrientationFixer.transformImage(content);
			} catch(Exception e) {
				e.printStackTrace();
			}
			FileOutputStream fOut = new FileOutputStream(uploadedFile);
			fOut.write(CmsFileUtil.readFully(content, true));
			fOut.close();
			
		} catch (Exception e1) {
			LOG.error("Error al intentar subir el archivo " + fullPath,e1);
			throw e1;
		}
		
		
		InputStream fileStream = CloneStreamsService.getInstance().clone(uploadedFile);
		try {
        	uploadedFile.delete();
        } catch(Exception e) {
        	e.printStackTrace();
        }
		
		return super.uploadAmzFile(cmsObject, path, fileName, parameters, fileStream, properties);
	}
	
	
	protected void deleteResource(String link) throws Exception {
		//To be implemented in child classes
	}
	
	public int getFolderType() {
		return this.getVfsFolderType();
	}
	
	@Override
	protected int getVfsFolderType() {
		try {
			return OpenCms.getResourceManager().getResourceType("imagegallery").getTypeId();
		} catch (CmsLoaderException e) {
			LOG.error(e);
			return CmsResourceTypeFolder.getStaticTypeId();
		}
	}

	@Override
	protected String getModuleName() {
		return "transcription";
	}

	@Override
	protected int getPointerType() throws CmsLoaderException {
		return TfsResourceTypeUploadProcessing.getStaticTypeId();
	}

public void checkOwnerFolder(File dir){
		
		File rfsDir = new File(rfsDirectory + "/");
		
		String newFolder = dir.getPath().substring(rfsDirectory.length()+1);
		
		String [] folders = newFolder.split("/");
		
		String folder = "";
		
		for (int i=0; i< folders.length; i++ ){
			
			folder = folder + "/" +folders[i];
			String newDirStr = rfsDirectory + folder;
		
			File newDir = new File(newDirStr + "/");
			
			try{
				
				UserPrincipal rfsOwner = java.nio.file.Files.getOwner(rfsDir.toPath());
				String rfsOwnerStr = rfsOwner.getName();
				
				UserPrincipal dirOwner = java.nio.file.Files.getOwner(newDir.toPath());
				String dirOwnerStr = dirOwner.getName();
				
				if(rfsOwnerStr!=null && dirOwnerStr!=null && !dirOwnerStr.equals(rfsOwnerStr))
				{
					Path p = newDir.toPath();
					String group = rfsOwner.getName();
					UserPrincipalLookupService lookupService = FileSystems.getDefault().getUserPrincipalLookupService();
					GroupPrincipal groupPrincipal = lookupService.lookupPrincipalByGroupName(group);
					Files.getFileAttributeView(p, PosixFileAttributeView.class, LinkOption.NOFOLLOW_LINKS).setGroup(groupPrincipal);
					Files.getFileAttributeView(p, PosixFileAttributeView.class, LinkOption.NOFOLLOW_LINKS).setOwner(rfsOwner);	
				}
				
			}catch(UnsupportedOperationException error){
				LOG.error("Error al intentar revisar permisos del directorio " + rfsDir.getAbsolutePath()+"EROR: "+error.getMessage());
			}catch(Exception error){
				LOG.error("Error al intentar revisar permisos del directorio " + rfsDir.getAbsolutePath()+"EROR: "+error.getMessage());
			}
			
		}
		
		return;
	}
	
	@Override
	public JSONObject callbackUpload(JSONObject data) {
		
		JSONObject response = new JSONObject();
		
		// TODO Auto-generated method stub
		String urlImage = data.getString("vfsurl");
		String site = data.getString("site");
		String user = data.getString("user");
		String publication = data.getString("publication");
		String status = data.getString("status");
		
		response.put("vfsurl", urlImage);
		response.put("site",site);
		response.put("user",user);
		response.put("publication",publication);
		
		CmsObject cmsObjectClone = CmsObjectUtils.getClone(cmsObject);
		try {
			cmsObjectClone.loginUser(user);
			cmsObjectClone.getRequestContext().setSiteRoot(site);
			cmsObjectClone.getRequestContext().setCurrentProject(cmsObject.readProject("Offline"));
			
			cmsObjectClone.lockResource(urlImage);
			CmsFile newFile = cmsObjectClone.readFile(urlImage, CmsResourceFilter.IGNORE_EXPIRATION);
			//CmsResource res = cmsObjectClone.readResource(urlImage);
			
			
			if (status.equals("completed")) {
				
			
				newFile.setType(TfsResourceTypeTranscription.getStaticTypeId());
				
				
				/*
				{
					  site: '/sites/generic1',
					  publication: '1',
					  user: 'vpod',
					  transcriptFile: 'transcript/audios/2025/01/22/TrailerHistoriayReview_Loom.mp3.json',
					  language_codes: 'es-US',
					  transcriptText: ''
					}
				*/
				
				newFile.setContents(data.getString("transcriptText").getBytes());
				cmsObjectClone.writeFile(newFile);
				
				CmsProperty propimg;
				
				//language_codes
				if (data.get("language_codes")!=null && data.getString("language_codes").trim().length()>0) {
					String language_codes = data.getString("language_codes").trim();
					propimg =  new CmsProperty("languageCodes", null,language_codes);
					cmsObjectClone.writePropertyObject(urlImage,propimg);
					
					response.put("languageCodes",language_codes);
				}
				
				String transcriptFile = data.getString("transcriptFile");
				propimg =  new CmsProperty("transcriptFile", null,transcriptFile);
				cmsObjectClone.writePropertyObject(urlImage,propimg);
				
				String mediaFile = data.getString("mediaFile");
				propimg =  new CmsProperty("mediaFile", null,mediaFile);
				cmsObjectClone.writePropertyObject(urlImage,propimg);
				
				//Punto focal
				//double x = data.getJSONObject("focalPoint").getDouble("x");
	
				cmsObjectClone.unlockResource(urlImage);
				
				String msg = "{ url: " + urlImage + ", " +
					", status: ok" +
					", site: " + site +
					", publication: " + publication + " }";
				
				SSEService.getInstance().addEvent("transcriptUpload", msg, user);
			}
			else if (status.equals("failed")) {
				//Fallo la traduccion y guardamos el motivo...
				newFile.setType(CmsResourceTypeUnknownFile.getStaticTypeId());
				cmsObjectClone.writeFile(newFile);
				
				CmsProperty propimg;
				String errorDescription = data.getString("errorDescription");
				propimg =  new CmsProperty("errorDescription", null,errorDescription);
				cmsObjectClone.writePropertyObject(urlImage,propimg);
				
				cmsObjectClone.unlockResource(urlImage);
				
				String msg = "{ url: " + urlImage + ", " +
						", status: error" +
						", errorCode: " + errorDescription +
						", site: " + site +
						", publication: " + publication + " }";
					
					SSEService.getInstance().addEvent("transcriptUpload", msg, user);
			}
			
		} catch (CmsException e) {
			LOG.error("Error al recibir callback de alta de transcripcion",e);
			
			String msg = "{ url: " + urlImage + ", " +
					", status: error" +
					", errorCode: " + e.getMessage() +
					", site: " + site +
					", publication: " + publication + " }";
				
			SSEService.getInstance().addEvent("transcriptUpload", msg, user);
		}
		
		return response;
	}

	@Override
	protected void addPreloadParameters(Map<String, String> metadata) {
		metadata.put("transcribe", "true");
	}
	
	
}
