package com.tfsla.diario.admin.jobs;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsLog;
import org.opencms.scheduler.I_CmsScheduledJob;

import com.tfsla.trivias.data.TfsTriviasDAO;

public class BorrarTriviasAntiguasJob implements I_CmsScheduledJob {
	
	private String resultados = "";
	protected static final Log LOG = CmsLog.getLog(BorrarVodsNews.class);
	
	
	public String launch(CmsObject cms, Map parameters) throws Exception {
		
		resultados = "";

		long daysBefore=0;
	
		String _daysBefore = (String)parameters.get("daysBefore");

		if (_daysBefore!=null)
			daysBefore = Integer.parseInt(_daysBefore);
		
		resultados += "Lanzando eliminacion de trivias anteriores a " + daysBefore + " dias\n";
		
		
		daysBefore = daysBefore *1000*60*60*24;
		Timestamp closeDate = new Timestamp(new Date().getTime()-daysBefore);
		
		TfsTriviasDAO tDAO = new TfsTriviasDAO();
		try {
			tDAO.deleteTriviaByCloseDate(closeDate);
		} catch (Exception ex) {
			LOG.error("Error al borrar trivias antigu:",ex);
		}
		resultados += "finaliza eliminacion de novedades de vods anteriores a " + daysBefore + " dias\n";

		return resultados;

	}

	


}
