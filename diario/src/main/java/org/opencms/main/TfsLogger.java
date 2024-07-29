package org.opencms.main;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import org.apache.commons.lang.WordUtils;

public class TfsLogger {
	static private FileHandler fileTxt;
	  static private SimpleFormatter formatterTxt;
	  private static String logPath = "";
	  
	  private static Logger LOGGER = Logger.getLogger("");
	  

	  public void setup(String logFile, String className){
		  
		LOGGER = Logger.getLogger(className);
		
		try{
			String logFolderPath = null;
			
			CmsSystemInfo cmsInfo = new CmsSystemInfo();
			logFolderPath = cmsInfo.getLogFileRfsPath();
			
			if(logFolderPath!=null){
				logFolderPath = logFolderPath.substring(0,logFolderPath.lastIndexOf("/"));
			}
			
			logPath = logFolderPath + "/" + logFile+".%g";
			  
			fileTxt = new FileHandler(logPath,2097152,5,true);
		    formatterTxt = new SimpleFormatter();
		    fileTxt.setFormatter(formatterTxt);
		    
		    LOGGER.setUseParentHandlers(false);
		    LOGGER.addHandler(fileTxt);
	    
		} catch (IOException e) {
			CmsLog.getLog(this).error("Error al generar logs en "+logPath+". Error:  "+e.getMessage());
		}
		
	  }
	  
	  public void log(String level,String msg){
		  
		 String logMsg = WordUtils.wrap(msg, 200, "\n", false);
		  
		  if(level.equals("INFO"))
		     LOGGER.log(Level.INFO, logMsg);
		  
		  if(level.equals("ERROR"))
			 LOGGER.log(Level.SEVERE,logMsg);
		  
	  }
	  
	  public void log(String msg){
		  
		  String logMsg = WordUtils.wrap(msg, 200, "\n", false);
		  
		  LOGGER.log(Level.INFO, logMsg);
	  }
	  
	  public String getFileLogPath(){
		  return logPath;
	  }
	  
	  public void close(){
		  fileTxt.close();
	  }
}
