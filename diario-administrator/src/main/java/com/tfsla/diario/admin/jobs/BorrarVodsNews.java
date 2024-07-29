package com.tfsla.diario.admin.jobs;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsLog;
import org.opencms.scheduler.I_CmsScheduledJob;

import com.tfsla.vod.data.VodMyListDAO;

public class BorrarVodsNews implements I_CmsScheduledJob {

	private String resultados = "";
	protected static final Log LOG = CmsLog.getLog(BorrarVodsNews.class);
	
	
	public String launch(CmsObject cms, Map parameters) throws Exception {
		resultados = "";
		int daysBefore=0;
	
		String _daysBefore = (String)parameters.get("daysBefore");
	

		if (_daysBefore!=null)
			daysBefore = Integer.parseInt(_daysBefore);
		
		resultados += "Lanzando eliminacion de novedades de vods anteriores a " + daysBefore + " dias\n";
		
		VodMyListDAO myListDAO = new VodMyListDAO();
		try {
			myListDAO.removeOldNews(daysBefore);
		} catch (Exception ex) {
			LOG.error("Error al borrar novedades de vods:",ex);
		}
		resultados += "finaliza eliminacion de novedades de vods anteriores a " + daysBefore + " dias\n";

		return resultados;

	}

	


}
