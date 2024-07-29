package com.tfsla.genericImport.model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.logging.Log;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.content.CmsXmlContentValueSequence;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.tfsla.genericImport.exception.DataTransformartionException;
import com.tfsla.genericImport.exception.ImportException;
import com.tfsla.genericImport.transformation.I_dataTransformation;
import com.tfsla.genericImport.transformation.TransformationManager;

public abstract class A_ImportServiceWires implements Runnable {
	
	protected String offset;
	protected String cantidad;
	protected String importName;
	protected String contentType;
	protected String importDefinitionPath;
	protected String fileLog;
	protected Writer objWriter;
	protected Locale contentLocale;
	protected CmsObject cms;
	protected String basePath;
	protected CmsFile importDefinitionFile;
	protected CmsXmlContent importDefinitionContent;
	protected SimpleDateFormat sdf;
	private TransformationManager tManager = new TransformationManager();
	protected final Log LOG = CmsLog.getLog(this.getClass());
	protected static String BASE_FOLDER = "/var/lib/tomcat7/webapps/ROOT/importFiles";
	protected String campoNombreArchivo;
	protected String destinationPath = "";
	protected Long lastModificationDate;
	protected String pathFilesContent;
	protected String S = "/";
	protected String currentFileName;
	protected String fileName;
	protected boolean isFotogaleria = false;

	
	
	public A_ImportServiceWires(CmsObject cms, String importDefinitionPath) throws CmsException {
		this.importDefinitionPath = importDefinitionPath;
		this.cms = cms;
		this.importDefinitionFile = cms.readFile(cms.getRequestContext().removeSiteRoot(importDefinitionPath));
		this.importDefinitionContent = CmsXmlContentFactory.unmarshal(cms, importDefinitionFile);
		this.sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		this.objWriter = null;
		
		if (getLocale()==null)
	    	setLocale(cms.getRequestContext().getLocale());
		
		cantidad = importDefinitionContent.getStringValue(cms, "Cantidad", contentLocale);
		offset = importDefinitionContent.getStringValue(cms, "Offset", contentLocale);
	}
	
	public A_ImportServiceWires(CmsObject cms, String importDefinitionPath, boolean isAgency) throws CmsException {
		this.importDefinitionPath = importDefinitionPath;
		this.cms = cms;
		if(!isAgency){
		this.importDefinitionFile = cms.readFile(cms.getRequestContext().removeSiteRoot(importDefinitionPath));
		this.importDefinitionContent = CmsXmlContentFactory.unmarshal(cms, importDefinitionFile);
		//cantidad = importDefinitionContent.getStringValue(cms, "Cantidad", contentLocale);
		//offset = importDefinitionContent.getStringValue(cms, "Offset", contentLocale);
		}
		
		
		this.sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		this.objWriter = null;
		
		if (getLocale()==null)
	    	setLocale(cms.getRequestContext().getLocale());
		
		if(contentLocale == null)
			contentLocale = cms.getRequestContext().getLocale();

		
	}
	
	public String createLog() {
		
		fileLog = getImportName() != null ? getImportName().replace(" ", "_").replace(".", "").replace(",", ""):
		importDefinitionContent.getStringValue(cms, "Nombre", contentLocale).replace(" ", "_").replace(".", "").replace(",", "");
		
		SimpleDateFormat sdfFile = new SimpleDateFormat("yyyyMMdd_HHmmss");
		
		fileLog += "_" + sdfFile.format(new Date()) + ".log";
		
		fileLog = getLogPath() + "/" + fileLog;
		File strFile = new File(fileLog);
		
		strFile.setWritable(true, false);
		
		try {
			objWriter = new BufferedWriter(new FileWriter(strFile));
			objWriter.write(sdf.format(new Date()) + " - Iniciando importación de contenidos\r\n"); 
			objWriter.write(getImportName() + " / " + getContentType() + "\r\n");
		} catch (IOException e) {
			//e.printStackTrace();
			LOG.error(this.getClass().getName() + " || Error en generación de logs en importación de noticias",e);
		}
		
		return fileLog;
	}
	
	public String createLog(boolean isAgency, String Name) throws IOException {
		
		if(isAgency){
			fileLog = getImportName() != null ? getImportName().replace(" ", "_").replace(".", "").replace(",", ""):
			importDefinitionContent.getStringValue(cms, "Nombre", contentLocale).replace(" ", "_").replace(".", "").replace(",", "");
		}else{
			fileLog = Name;
		}
		
		SimpleDateFormat sdfFile = new SimpleDateFormat("yyyyMMdd_HHmmss");
		
		fileLog += "_" + sdfFile.format(new Date()) + ".log";
		
		fileLog = getLogPath() + "/" + fileLog;
		File strFile = new File(fileLog);
		
		objWriter = new BufferedWriter(new FileWriter(strFile));
		objWriter.write(sdf.format(new Date()) + " - Iniciando importacion de contenidos\r\n"); 
		objWriter.write(getImportName() + " / " + getContentType() + "\r\n");
		return fileLog;
	}
	
	public void writeToLog(String msg) {
		if (objWriter!=null) {
			try {
				objWriter.write(sdf.format(new Date()) + " " + msg + "\r\n");
			} catch (IOException e) {
				e.printStackTrace();
			} 
		}
	}
	
	public void closeLog() {
		try {
			if (objWriter!=null) {
				objWriter.flush();
				objWriter.close();
			}
		} catch (IOException e) {
			LOG.error(e);
		} 		
	}
	
	public String getCantidad() {
		return cantidad;
	}

	public void setCantidad(String cantidad) {
		this.cantidad = cantidad;
	}

	public String getOffset() {
		return offset;
	}

	public void setOffset(String offset) {
		this.offset = offset;
	}
	
	public String getImportName() {
		return importName;
	}

	public void setImportName(String importName) {
		this.importName = importName;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	
	public String getImportDefinitionPath() {
		return importDefinitionPath;
	}
	
	public Locale getLocale() {
		return contentLocale;
	}
	
	public void setLocale(Locale locale) {
		contentLocale = locale;
	}
	
	protected String getLogPath() {
		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
    	String siteName = OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot();

    	return config.getParam(siteName, "", "importManager", "logPath");
	}
	
	protected Object transform(String path, Object[] value) throws DataTransformartionException {
		return transform(path, value, "Transformacion");
	}
	
	protected Object transform(String path, Object[] value, String campoTransformacionNombre) throws DataTransformartionException {
		Object[] result = value;
		CmsXmlContentValueSequence elementSequence = importDefinitionContent.getValueSequence(path + campoTransformacionNombre, contentLocale);
		if (elementSequence==null) {
			if (value!=null) 
				return value[0];
			else
				return null;
		}
		
		int elementCount = elementSequence.getElementCount();
        for (int j = 0; j < elementCount; j++) {
        	String transformation = importDefinitionContent.getStringValue(cms, path + campoTransformacionNombre + "[" + (j+1) + "]", contentLocale);
        	if(transformation.equals("")) continue;
        	
        	I_dataTransformation dataTransformation = tManager.getTransformation(transformation);
        	dataTransformation.setCms(cms);
        	try {
				result = dataTransformation.execute(transformation, result);
			} catch (DataTransformartionException e) {
				LOG.error(this.getClass().getName() + " || Error ",e);
				throw e;
			}
        }

		if (result!=null) 
			return result[0];

        return null;
	}
	
	protected Object transformWithoutCampo(String transformation, Object[] value) throws DataTransformartionException {
		Object[] result = value;
		
		I_dataTransformation dataTransformation = tManager.getTransformation(transformation);
    	dataTransformation.setCms(cms);
    	try {
			result = dataTransformation.execute(transformation, result);
		} catch (DataTransformartionException e) {
			LOG.error(this.getClass().getName() + " || Error ",e);
			throw e;
		}

		if (result!=null) 
			return result[0];

        return null;
	}
	
	protected List<String> getListOfArticles(String path) {
        List<String> list = new ArrayList();
        try {
        	File folder = new File(path);
        	File[] listOfFiles = folder.listFiles();

        	    for (int i = 0; i < listOfFiles.length; i++) {
        	      if (listOfFiles[i].isFile()) {
        	    	list.add(listOfFiles[i].getName());
        	    	writeToLog("File " + listOfFiles[i].getName());
        	      } 
        	    }
        } catch (Exception e) {
        	LOG.error ("WIRES - Busca el listado de arhcivos:", e);
        }
 
        return list;
    }
	
	protected void moveFileAfterProcess(String fileName){			
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
    	    LOG.error(this.getClass().getName() + " || Error ",e);
			writeToLog("... Error copiando recurso a processed (" + fileName +"): ");
			writeToLog("causa: " + e.getMessage());
    	}
	}
	
	public Long getLastModificationDate() {
		return lastModificationDate;
	}

	public void setLastModificationDate(Long lastModificationDate) {
		this.lastModificationDate = lastModificationDate;
	}
	
	@Override
	public void run() {
		try {
			getXmlContent();
		} catch (SAXException e) {
			 LOG.error( "WIRES - Ejecucion ",e);
				
		} catch (DataTransformartionException e) {
			 LOG.error( "WIRES - Ejecucion ",e);
		} catch (ImportException e) {
			 LOG.error( "WIRES - Ejecucion ",e);
		} catch (ParserConfigurationException e) {
			 LOG.error( "WIRES - Ejecucion ",e);
		}
		writeToLog("// Proceso de importacion finalizado");
		closeLog();
	}
	
	protected void getXmlContent() throws SAXException, DataTransformartionException, ImportException, ParserConfigurationException {}
	

    protected String getUploadPath() {
		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
		return config.getParam("", "", "importAgency", "imporFilesPath","");
	}
    
   
    protected String getDestinationFolderPath() {
		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
		return config.getParam("", "", "importAgency", "imporFilesDestinationPath","");
	}

    protected String getValueForItem(String path, Document doc, XPath xpath) {
		String campo = "";
		
		try {			
            XPathExpression expr =  xpath.compile(path);
            String campoBuscado = (String) expr.evaluate(doc, XPathConstants.STRING);  
          
            campo = campoBuscado; 
        } catch (XPathExpressionException e) {
            LOG.error("Error al validar expresion:", e);
        }
		
		return campo;
	}

    protected CmsFile createResource(String resourceName) throws CmsIllegalArgumentException, CmsException{
		LOG.debug(this.getClass().getName() + " || creating resource type '" + contentType + "': " + resourceName);
		writeToLog("Creando tipo de recurso '" + contentType + "': " + resourceName);

		int typeResource = OpenCms.getResourceManager().getResourceType("noticia").getTypeId();
		CmsResource res = cms.createResource(resourceName,typeResource);
		return cms.readFile(res);
	}
	
	protected String generateResourceName(String vfsPath) throws CmsException, DataTransformartionException {
		int maxNewsValue  = 0;
		List<CmsResource> cmsFiles = cms.getResourcesInFolder(vfsPath, CmsResourceFilter.ALL);
		for (CmsResource resource : cmsFiles) {
			fileName = resource.getName();
			if (fileName.matches(".*" + "noticia" + "_[0-9]{4}.html")) {
				String auxFileName =fileName.substring(fileName.indexOf("noticia" + "_"));
				int newsValue = Integer.parseInt(auxFileName.replace("noticia" + "_","").replace(".html",""));
				if (maxNewsValue<newsValue)
					maxNewsValue=newsValue;
			}
		}

		DecimalFormat df = new DecimalFormat("0000");
		if (!vfsPath.endsWith("/"))
			vfsPath += "/";
		fileName = vfsPath + "noticia" + "_" + df.format(maxNewsValue+1) + ".html";
		return fileName;
	}
	
	protected void createFolders(String baseFolder, String subFolder) throws CmsIllegalArgumentException, CmsException {
		String[] subFolders = subFolder.split("/");
		String folderName = baseFolder;
		if (!cms.existsResource(folderName)) {
			writeToLog("Creando carpeta " + folderName);
			cms.createResource(folderName, CmsResourceTypeFolder.RESOURCE_TYPE_ID);
			cms.unlockResource(folderName);
		}
		for (String subpath : subFolders){
			if(!subpath.isEmpty()){
				folderName += subpath + "/"; 
				if (!cms.existsResource(folderName)) {
					writeToLog("Creando carpeta " + folderName);
					cms.createResource(folderName, CmsResourceTypeFolder.RESOURCE_TYPE_ID);
					cms.unlockResource(folderName);
				}
			}
		} 
		
	}
}
