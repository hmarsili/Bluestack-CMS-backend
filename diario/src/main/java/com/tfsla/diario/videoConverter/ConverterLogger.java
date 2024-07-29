package com.tfsla.diario.videoConverter;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.opencms.main.CmsLog;
import org.opencms.main.CmsSystemInfo;

public class ConverterLogger {

	  static private FileHandler fileTxt;
	  static private SimpleFormatter formatterTxt;
	  private static String logPath = "";
	  
	  private static Logger LOGGER = Logger.getLogger("");

	  public void setup(String logFilePath, String className){

		LOGGER = Logger.getLogger(className);
		
		try{
			
			String logFolderPath = null;
			
			CmsSystemInfo cmsInfo = new CmsSystemInfo();
			logFolderPath = cmsInfo.getLogFileRfsPath();
			
			if(logFolderPath!=null){
				logFolderPath = logFolderPath.substring(0,logFolderPath.lastIndexOf("/"));
			}
			
			File temp = new File(logFolderPath, "cmsMediosVideoConverter");
			if (!temp.exists()) {
				temp.mkdirs();
				temp.deleteOnExit();
			}
			
			Runtime runtime = Runtime.getRuntime();
			try {
				runtime.exec(new String[] { "/bin/chmod", "775",temp.getAbsolutePath() });
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			logPath = temp.getAbsolutePath() + "/" + logFilePath;
			  
		    fileTxt = new FileHandler(logPath);
		    formatterTxt = new SimpleFormatter();
		    fileTxt.setFormatter(formatterTxt);
		    
		    LOGGER.setUseParentHandlers(false);
		    LOGGER.addHandler(fileTxt);
		    
	    
		} catch (IOException e) {
			CmsLog.getLog(this).error("Error al guardar logs de video Converter"+e.getMessage());
		}

	  }
	  
	  public void log(String level,String msg){
		  
		  if(level.equals("INFO"))
		     LOGGER.log(Level.INFO, msg);
		  
		  if(level.equals("ERROR"))
			  LOGGER.log(Level.SEVERE,msg);
	  }
	  
	  public void log(String msg){
		     LOGGER.log(Level.INFO, msg);
	  }
	  
	  public String getFileLogPath(){
		  return logPath;
	  }
	  
	  public void close(){
		  fileTxt.close();
	  }
}
