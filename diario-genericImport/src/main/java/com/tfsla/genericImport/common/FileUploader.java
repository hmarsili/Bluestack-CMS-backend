package com.tfsla.genericImport.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsFileUtil;

public class FileUploader {
	
	public String upload(String fileName, InputStream content) throws Exception {
		String path = this.getUploadPath();
		LOG.debug(String.format("Subiendo archivo '%s' al directorio '%s'", fileName, path));
		File dir = new File(path);
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
		
		return fullPath;
	}
	
	private String getUploadPath() {
		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
		return config.getParam("", "", "importManager", "imporFilesPath","");
	}
	
	private static final Log LOG = CmsLog.getLog(FileUploader.class);
}
