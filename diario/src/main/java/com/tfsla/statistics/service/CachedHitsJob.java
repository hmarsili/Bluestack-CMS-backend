package com.tfsla.statistics.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.opencms.file.CmsObject;
import org.opencms.scheduler.I_CmsScheduledJob;

public class CachedHitsJob implements I_CmsScheduledJob {

	public String launch(CmsObject cms, Map parameters) throws Exception {
		Date now = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
		String resultados = "Almacenando hits en cache a las " + sdf.format(now);

		resultados += CachedHitsList.getInstance().sendHits();
		
		return resultados;
	}

}
