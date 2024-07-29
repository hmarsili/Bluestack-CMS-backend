package com.tfsla.diario.videoConverter.jsp;

import java.util.Map;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsLog;
import org.opencms.scheduler.I_CmsScheduledJob;

public class EncoderRelationJob implements I_CmsScheduledJob {

	@Override
	public String launch(CmsObject cms, Map parameters) throws Exception {
		CmsLog.getLog(this).info ("Comenzando la ejecución del job EnconderRelationJob ");
		
		TFSEncoderRelationQueue encoderRelationQueue = new TFSEncoderRelationQueue(cms);
		encoderRelationQueue.processEncoderRelationQueue();
		
		CmsLog.getLog(this).info("Finaliza la ejecución del job EncoderRelationJob");
		
		return null;
	}

}
