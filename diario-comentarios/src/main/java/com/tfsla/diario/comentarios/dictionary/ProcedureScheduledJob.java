package com.tfsla.diario.comentarios.dictionary;

import java.util.Date;
import java.util.Map;

import org.opencms.file.CmsObject;
import org.opencms.scheduler.I_CmsScheduledJob;


public class ProcedureScheduledJob implements I_CmsScheduledJob {
	
	public String launch(CmsObject cms, Map parameters) throws Exception {
		DictionaryPersistor.CheckCommentsProcedure(cms);

		return "Pre-Moderacion de comentarios ejecutada en " + new Date();
	}

}
