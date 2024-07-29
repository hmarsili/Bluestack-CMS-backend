package com.tfsla.genericImport.model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.content.CmsXmlContentValueSequence;

import com.tfsla.genericImport.exception.DataTransformartionException;
import com.tfsla.genericImport.transformation.I_dataTransformation;
import com.tfsla.genericImport.transformation.TransformationManager;

public abstract class A_ImportService implements Runnable {
	
	public A_ImportService(CmsObject cms, String importDefinitionPath) throws CmsException {
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
	
	public A_ImportService(CmsObject cms, String importDefinitionPath, boolean isAgency) throws CmsException {
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
			e.printStackTrace();
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
	
	protected String offset;
	protected String cantidad;
	protected String importName;
	protected String contentType;
	protected String importDefinitionPath;
	protected String fileLog;
	protected Writer objWriter;
	protected Locale contentLocale;
	protected CmsObject cms;
	protected CmsFile importDefinitionFile;
	protected CmsXmlContent importDefinitionContent;
	protected SimpleDateFormat sdf;
	private TransformationManager tManager = new TransformationManager();
	protected final Log LOG = CmsLog.getLog(this.getClass());
}
