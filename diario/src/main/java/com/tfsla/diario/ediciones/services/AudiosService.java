package com.tfsla.diario.ediciones.services;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTPClient;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.db.CmsDbSqlException;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.security.CmsSecurityException;
import org.opencms.util.CmsFileUtil;

import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.videoConverter.AudioInfo;
import com.tfsla.diario.videoConverter.Encoder;
import com.tfsla.diario.videoConverter.EncoderException;
import com.tfsla.diario.videoConverter.InputFormatException;
import com.tfsla.diario.videoConverter.MultimediaInfo;
import com.tfsla.diario.videoConverter.VideoInfo;
import com.tfsla.diario.videoConverter.VideoSize;
import com.tfsla.utils.CmsResourceUtils;

import net.sf.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.SocketException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AudiosService extends UploadService {

	
	private static Map<String, AudiosService> instances = new HashMap<String, AudiosService>();

	private String defaultAudioPath = "";
			
	static private boolean createDateFolderDefault = false;
	
	public static AudiosService getInstance(CmsObject cms)
    {
    	
    	String siteName = OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot();
    	String publication = "0";
     	TipoEdicionBaseService tService = new TipoEdicionBaseService();
    	try {
			TipoEdicion tEdicion = tService.obtenerTipoEdicion(cms, cms.getRequestContext().getUri());			
			if (tEdicion!=null)
				publication = "" + tEdicion.getId();
		} catch (Exception e) {
			e.printStackTrace();
		}

    	return getInstance(cms, siteName, publication);
    }

	public static AudiosService getInstance(CmsObject cms, String siteName, String publication)
    {
    	
        String id = siteName + "||" + publication;
    	
    	AudiosService instance = instances.get(id);
    	
    	if (instance == null) {
	    	instance = new AudiosService(cms,siteName, publication);

	    	instances.put(id, instance);
    	}
    	
    	instance.cmsObject = cms;
    	
        return instance;
    }

	
	public AudiosService(CmsObject cmsObject, String siteName, String publication) {
		this.cmsObject = cmsObject;
		this.loadProperties(siteName,publication);
	}

	public void loadProperties(String siteName, String publication) {
		
    	String module = getModuleName();
 		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
		
		
		defaultAudioPath = config.getParam(siteName, publication, module, "defaultAudioPath","");
		
		createDateFolderDefault = config.getBooleanParam(siteName, publication, module, "createDateFolderDefault",false);
				
		loadBaseProperties(siteName, publication);
	}


	@Override
	protected int getVFSResourceType(String fileName) throws CmsException {
		return  OpenCms.getResourceManager().getResourceType("audio").getTypeId();
	}

	public void addNewsCountToAudio(CmsObject cmsObject, String audioPath)
	{
		try {
			CmsResourceUtils.forceLockResource(cmsObject, audioPath);
		
			CmsRelationFilter filter = CmsRelationFilter.SOURCES;
			List<CmsRelation> rels = cmsObject.getRelationsForResource(audioPath, filter);
			int cantNoticias = 0;
	
			for (CmsRelation rel : rels)
			{
				int type = rel.getSource(cmsObject, CmsResourceFilter.ALL).getTypeId();
				if (type == OpenCms.getResourceManager().getResourceType("noticia").getTypeId())
					cantNoticias++;
			}
			if (cantNoticias>0)
				cmsObject.writePropertyObject(audioPath, new CmsProperty("newsCount","" + cantNoticias,"" + cantNoticias));
			else
				cmsObject.writePropertyObject(audioPath, new CmsProperty("newsCount",CmsProperty.DELETE_VALUE,CmsProperty.DELETE_VALUE));


			cmsObject.unlockResource(audioPath);

		} catch (CmsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	

	public static boolean isCreateDateFolderDefault() {
		return createDateFolderDefault;
	}
	
	public String getDefaultUploadFolder(Map<String,String> parameters, int videoType) throws Exception
	{
		return defaultAudioPath + "/" + getVFSSubFolderPath(defaultAudioPath, getVfsFolderType(), vfsSubFolderFormat, parameters);
	}
	
	@Override
	protected int getVfsFolderType() {	
		return CmsResourceTypeFolder.getStaticTypeId();
	}

	@Override
	protected String getModuleName() {
		return "audioUpload";
	}
	
	@Override
	protected int getPointerType()  throws CmsLoaderException
	{
		return OpenCms.getResourceManager().getResourceType("audio-link").getTypeId();
	}
	
	public CmsResource uploadVFSFile(String path, String fileName, InputStream content) throws CmsException, IOException {
		List properties = new ArrayList(0);
		return uploadVFSFile(path,fileName,content,properties);
	}
	
	public CmsResource uploadVFSFile(String path, String fileName, InputStream content, List properties) throws CmsException, IOException {
		byte[] buffer = CmsFileUtil.readFully(content, false);
		
		try {

		    String tmpFile = tmpFileFFmepg(fileName, buffer);
		    
			if(tmpFile != null) {
				
				  try {
					File uploadedFile = new File(tmpFile);
					
					Encoder encoder = new Encoder();
					MultimediaInfo infoAudio = new MultimediaInfo();
								   infoAudio = encoder.getInfo(uploadedFile);
								   
					long durationMills = infoAudio.getDuration();
					int 	   seconds = (int)((durationMills / 1000)%60);  
					int 	   minutes = (int)((durationMills / 1000)/60);  
					int 	     hours = (int)((durationMills / (1000*60*60))%24);  
													   
					DecimalFormat formateador = new DecimalFormat("00");
					String duration = formateador.format(hours)+":"+formateador.format(minutes)+":"+formateador.format(seconds);
										
					CmsProperty prop = new CmsProperty();
						        prop = new CmsProperty();
						        prop.setName("audio-duration");
						        prop.setAutoCreatePropertyDefinition(true);
						        prop.setStructureValue(duration);
						        properties.add(prop);
						 
					
				  } catch (InputFormatException e) {
						e.printStackTrace();
				  } catch (EncoderException e) {
						e.printStackTrace();
				  }
			}

			int type = getVFSResourceType(fileName);
			
			CmsResource res = cmsObject.createResource(path + fileName, type, buffer, properties);
			cmsObject.unlockResource(path + fileName);
			
			return res;
		} catch (CmsSecurityException e) {
			// in case of not enough permissions, try to create a plain text file	
			CmsResource res = cmsObject.createResource(path + fileName, CmsResourceTypePlain.getStaticTypeId(), buffer, properties);
			cmsObject.unlockResource(path + fileName);
			return res;
		} catch (CmsDbSqlException sqlExc) {
			// SQL error, probably the file is too large for the database settings, delete file
			cmsObject.lockResource(path + fileName);
			cmsObject.deleteResource(path + fileName, CmsResource.DELETE_PRESERVE_SIBLINGS);
		    throw  sqlExc;   		        
		}
	}
	
	public String uploadRFSFile(String path, String fileName, Map<String,String> parameters, InputStream content) throws Exception {
		List properties = new ArrayList(0);
		return uploadRFSFile(path, fileName, parameters, content,properties);
	}
	
	public String uploadRFSFile(String path, String fileName, Map<String,String> parameters, InputStream content, List properties) throws Exception {
		fileName = getValidFileName(fileName);

		String subFolderRFSPath = getRFSSubFolderPath(rfsSubFolderFormat, parameters);

		File dir = new File(rfsDirectory + "/" + subFolderRFSPath);
		
		if (!dir.exists() && !dir.mkdirs()) {
			LOG.error("Error al intentar crear el directorio " + dir.getAbsolutePath());
	    }

		String fullPath = dir.getAbsolutePath() + "/" + fileName;
		String url = rfsVirtualUrl + subFolderRFSPath + fileName;
		
		File uploadedFile = new File(fullPath);
		
		try {
			FileOutputStream fOut = new FileOutputStream(uploadedFile);
			fOut.write(CmsFileUtil.readFully(content, true));
			fOut.close();
		} catch (Exception e1) {
			LOG.error("Error al intentar subir el archivo " + fullPath,e1);
			throw e1;
		}
		
		Encoder encoder = new Encoder();
		MultimediaInfo infoAudio = new MultimediaInfo();
					   infoAudio = encoder.getInfo(uploadedFile);
					   
			long durationMills = infoAudio.getDuration();
			int 	   seconds = (int)((durationMills / 1000)%60);  
			int 	   minutes = (int)((durationMills / 1000)/60);  
			int 	     hours = (int)((durationMills / (1000*60*60))%24);  
										   
			DecimalFormat formateador = new DecimalFormat("00");
			String duration = formateador.format(hours)+":"+formateador.format(minutes)+":"+formateador.format(seconds);
							
			CmsProperty prop = new CmsProperty();
			            prop = new CmsProperty();
			            prop.setName("audio-duration");
			            prop.setAutoCreatePropertyDefinition(true);
			            prop.setStructureValue(duration);
			            properties.add(prop);
			 

		try {
			String linkName = path + fileName;
			
			cmsObject.createResource(linkName, 
					getPointerType(),
					url.getBytes(),
					properties);

			cmsObject.unlockResource(linkName);
			return linkName;
		} catch (CmsIllegalArgumentException e) {
			LOG.error("Error al intentar subir el archivo " + fullPath,e);
			throw e;
		} catch (CmsLoaderException e) {
			LOG.error("Error al intentar subir el archivo " + fullPath,e);
			throw e;
		} catch (CmsException e) {
			LOG.error("Error al intentar subir el archivo " + fullPath,e);
			throw e;
		}
	}
	
	public String uploadAmzFile(String path, String fileName, Map<String,String> parameters, InputStream content) throws Exception {
		List properties = new ArrayList(0);
		return uploadAmzFile(path, fileName, parameters, content, properties);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public String uploadAmzFile(String path, String fileName, Map<String,String> parameters, InputStream content, List properties) throws Exception {
		fileName = getValidFileName(fileName);
		String subFolderRFSPath = getRFSSubFolderPath(rfsSubFolderFormat, parameters);
		File dir = new File(rfsDirectory + "/" + subFolderRFSPath);
		
		if (!dir.exists() && !dir.mkdirs()) {
			LOG.error("Error al intentar crear el directorio " + dir.getAbsolutePath());
	    }

		String fullPath = dir.getAbsolutePath() + "/" + fileName;
		File uploadedFile = new File(fullPath);
		
		try {
			FileOutputStream fOut = new FileOutputStream(uploadedFile);
			fOut.write(CmsFileUtil.readFully(content, true));
			fOut.close();
		} catch (Exception e1) {
			LOG.error("Error al intentar subir el archivo " + fullPath,e1);
			throw e1;
		}
		
		Encoder encoder = new Encoder();
		MultimediaInfo infoAudio = encoder.getInfo(uploadedFile);
		
		long durationMills = infoAudio.getDuration();
		int 	   seconds = (int)((durationMills / 1000)%60);  
		int 	   minutes = (int)((durationMills / 1000)/60);  
		int 	     hours = (int)((durationMills / (1000*60*60))%24);  
									   
		DecimalFormat formateador = new DecimalFormat("00");
		String duration = formateador.format(hours)+":"+formateador.format(minutes)+":"+formateador.format(seconds);
		
		CmsProperty prop = new CmsProperty();
        prop.setName("audio-duration");
        prop.setAutoCreatePropertyDefinition(true);
        prop.setStructureValue(duration);
        properties.add(prop);

        InputStream fileStream = CloneStreamsService.getInstance().clone(uploadedFile);
        try {
        	uploadedFile.delete();
        } catch(Exception e) {
        	e.printStackTrace();
        }
        
		return super.uploadAmzFile(path, fileName, parameters, fileStream, properties);
	}
	
	public String uploadFTPFile(String path, String fileName, Map<String,String> parameters, InputStream content) throws Exception {
		List properties = new ArrayList(0);
		return uploadFTPFile(path, fileName, parameters, content,properties);
	}
	
	public String uploadFTPFile(String path, String fileName, Map<String,String> parameters, InputStream content, List properties) throws Exception {
		String ftpSubfolder = "";
		fileName = getValidFileName(fileName);

		FTPClient client = new FTPClient();
		client.addProtocolCommandListener(new PrintCommandListener(new
				PrintWriter(System.out)));

		byte[] buffer = CmsFileUtil.readFully(content, false);
		
		try {
			client.connect(ftpServer);
			client.login(ftpUser, ftpPassword);
			client.changeWorkingDirectory(ftpDirectory);

			ftpSubfolder = getFTPSubFolderPath(client,parameters);
			
			client.enterLocalPassiveMode();
			client.setFileType(FTPClient.BINARY_FILE_TYPE);

			// Upload a file
			OutputStream os = client.storeFileStream( fileName);

			os.write(buffer);
			os.close();

			client.completePendingCommand();

			//System.out.println(client.printWorkingDirectory());

			client.logout();
			client.disconnect();
		} catch (SocketException e1) {
			LOG.error("Error al intentar subir el archivo a " + ftpServer,e1);
			throw e1;
		} catch (IOException e1) {
			LOG.error("Error al intentar subir el archivo a " + ftpServer,e1);
			throw e1;
		}

		String audioMultimediaPath = ftpVirtualUrl + ftpSubfolder + fileName;
		
		try {
			
			String tmpFile = tmpFileFFmepg(fileName, buffer);
				
			if(tmpFile!=null){
				   try {
					File uploadedFile = new File(tmpFile);
					
					Encoder encoder = new Encoder();
					MultimediaInfo infoAudio = new MultimediaInfo();
								   infoAudio = encoder.getInfo(uploadedFile);
								   
						long durationMills = infoAudio.getDuration();
						int 	   seconds = (int)((durationMills / 1000)%60);  
						int 	   minutes = (int)((durationMills / 1000)/60);  
						int 	     hours = (int)((durationMills / (1000*60*60))%24);  
													   
						DecimalFormat formateador = new DecimalFormat("00");
						String duration = formateador.format(hours)+":"+formateador.format(minutes)+":"+formateador.format(seconds);
										
						CmsProperty prop = new CmsProperty();
						            prop = new CmsProperty();
						            prop.setName("audio-duration");
						            prop.setAutoCreatePropertyDefinition(true);
						            prop.setStructureValue(duration);
						            properties.add(prop);
						            
				  } catch (InputFormatException e) {
						e.printStackTrace();
				  } catch (EncoderException e) {
						e.printStackTrace();
				  }
			}
			
			String linkName = path + fileName;
			cmsObject.createResource(linkName, 
					getPointerType(),
					audioMultimediaPath.getBytes(),
					properties);

			cmsObject.unlockResource(linkName);
			
			return linkName;
		} catch (CmsIllegalArgumentException e) {
			LOG.error("Error al intentar subir el archivo a " + ftpServer,e);
			throw e;
		} catch (CmsLoaderException e) {
			LOG.error("Error al intentar subir el archivo a " + ftpServer,e);
			throw e;
		} catch (CmsException e) {
			LOG.error("Error al intentar subir el archivo a " + ftpServer,e);
			throw e;
		}
	}

	@Override
	public JSONObject callbackUpload(JSONObject data) {
		// TODO Falta implementar
		throw new RuntimeException("Metodo no implementado!");
	}
	
	@Override
	protected void addPreloadParameters(Map<String, String> metadata) {
		// Sin agregados...
		
	}
	
}
