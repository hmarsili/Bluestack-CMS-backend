package com.tfsla.genericImport.service;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.content.CmsXmlContentValueSequence;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.tfsla.diario.ediciones.services.ImagenService;
import com.tfsla.genericImport.exception.DataTransformartionException;
import com.tfsla.genericImport.exception.ImportException;
import com.tfsla.genericImport.model.A_ImportService;

public class ImportImagesService extends A_ImportService{
	
	private String pathFilesContent;
	private static Writer objWriter = null;
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private CmsObject cms;
	private static String BASE_FOLDER = "/var/lib/tomcat7/webapps/ROOT/importFiles";
	private String destinationPath = "";
	private String S = "/";
	private Locale contentLocale;
	private String currentFileName;
	private String importacionAutomatica;
	private String campoNombreArchivo;
	boolean isFotogaleria = false;
	List<String> articles;
	
	public String getOffset() {
		return offset;
	}

	public void setOffset(String offset) {
		this.offset = offset;
	}

	private String offset;
	
	public ImportImagesService(CmsObject cms, String importDefinitionPath) throws CmsException{	
		super(cms, importDefinitionPath, false);
		this.importDefinitionPath = importDefinitionPath;
		this.cms = cms;

		if (getLocale()==null)
	    	setLocale(cms.getRequestContext().getLocale());
		
		if(contentLocale == null)
			contentLocale = cms.getRequestContext().getLocale();
		importName = importDefinitionContent.getStringValue(cms, "Nombre", contentLocale);
		contentType = importDefinitionContent.getStringValue(cms, "TipoContenido", contentLocale);
		importacionAutomatica = importDefinitionContent.getStringValue(cms, "importacionAutomatica", contentLocale);
		campoNombreArchivo = importDefinitionContent.getStringValue(cms, "CampoNombreImportacion", contentLocale);		
	}
	
	public void getXmlContent() throws SAXException, ImportException, ParserConfigurationException {        
        BASE_FOLDER = getUploadPath();
        destinationPath = getDestinationFolderPath();
        ArrayList<String> i = new ArrayList<String>();
        					
        try {            
			           
            if(campoNombreArchivo != null){
            	pathFilesContent = BASE_FOLDER + S + campoNombreArchivo;
            }
                        
            articles = getListOfArticles(pathFilesContent);
            
            for(String item : articles){            	   	         	
        		String ElementToRead = BASE_FOLDER + campoNombreArchivo + S + item;
				currentFileName = "";
				try {
										
					uploadImageRFS(ElementToRead, item);							
					
					moveFileAfterProcess(item);
					
					writeToLog("... Fin importacion recurso (" + item +"): " + currentFileName);
					
				}  catch (CmsIllegalArgumentException e) {
					if (!currentFileName.equals(""))
					LOG.error(this.getClass().getName() + " || Error ",e);
					writeToLog("... Error en importacion recurso (" + item +"): " + currentFileName);
					writeToLog("causa: " + e.getMessage());
				} catch (CmsException e) {
					if (!currentFileName.equals(""))
					LOG.error(this.getClass().getName() + " || Error ",e);
					writeToLog("... Error en importacion recurso (" + item +"): " + currentFileName);
					writeToLog("causa: " + e.getMessage());
				} 
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
			if (objWriter!=null) {
				try {
					objWriter.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}       
 
    }
	
protected String uploadImageRFS(String imgPath,String fileName) throws Exception{
		
		Map<String,String> parameters = new HashMap<String,String>();
		parameters.put("section","import");
		
		String path = "/" + ImagenService.getInstance(cms).getDefaultVFSUploadFolder(parameters);
	
		BufferedInputStream buffIn = null;
        buffIn = new BufferedInputStream(new FileInputStream(imgPath));
		
		String lcFileName = fileName.toLowerCase();
		String imageVFS = ImagenService.getInstance(cms).uploadRFSFile(path,lcFileName,parameters,buffIn);
		
		buffIn.close();
		
		return imageVFS;
	}
	
	private void moveFileAfterProcess(String fileName){			
    	try{
    		String destFolder = destinationPath + campoNombreArchivo;
    		File afile =new File(BASE_FOLDER + campoNombreArchivo + "/" + fileName);
    		boolean success = false;
    		
    		File df = new File(destFolder);
    		if(!df.exists()){
	    		success = (new File(destFolder)).mkdirs();
	    		if (success) {
	    			if(afile.renameTo(new File(destFolder + "/" + afile.getName()))){
	    	    		afile.delete();
	    	    	}
	    		}	    	   	 
    		}else{
    			if(afile.renameTo(new File(destFolder + "/" + afile.getName()))){
    	    		afile.delete();
    	    	}
    		}
    	    	    	    
    	}catch(Exception e){
    	    e.printStackTrace();
    	    LOG.error(this.getClass().getName() + " || Error ",e);
			writeToLog("... Error copiando recurso a processed (" + fileName +"): ");
			writeToLog("causa: " + e.getMessage());
    	}
	}
	
	private List<String> getListOfArticles(String path) {
        List<String> list = new ArrayList();
        try {
        	File folder = new File(path);
        	File[] listOfFiles = folder.listFiles();

        	    for (int i = 0; i < listOfFiles.length; i++) {
        	      if (listOfFiles[i].isFile()) {
        	    	  if(listOfFiles[i].getName().contains("jpg")){        	    		  
        	    		  //String campo = "";
        	  			  //String[] itemParts = listOfFiles[i].getName().split("-");
        	  			  //campo = itemParts[1];
        	  			  //if(campo.contains("N01")){
        	  				list.add(listOfFiles[i].getName());
                	    	writeToLog("File " + listOfFiles[i].getName());
        	  			  //}
        	    	  }        	    	
        	      } 
        	    }
        } catch (Exception e) {
            e.printStackTrace();
        }
 
        return list;
    }

	
	
    public String getImportName() {
		return importName;
	}
    
    public String getContentType() {
		return contentType;
	}
      
    
    private String getUploadPath() {
		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
		return config.getParam("", "", "importAutomatic", "imporFilesPath","");
	}
    
    private String getFolderPath() {
		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
		return config.getParam("", "", "importAutomatic", "importNews","");
	}
    
    private String getDestinationFolderPath() {
		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
		return config.getParam("", "", "importAutomatic", "imporFilesDestinationPath","");
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			getXmlContent();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DataTransformartionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ImportException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		writeToLog("// Proceso de importacion finalizado");
		closeLog();
	}
}

