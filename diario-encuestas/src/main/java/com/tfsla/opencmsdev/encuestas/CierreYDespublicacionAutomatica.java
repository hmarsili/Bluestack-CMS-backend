package com.tfsla.opencmsdev.encuestas;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.opencms.file.CmsObject;
import org.opencms.scheduler.I_CmsScheduledJob;
import org.opencms.site.CmsSiteManager;

public class CierreYDespublicacionAutomatica implements I_CmsScheduledJob {

	public String launch(CmsObject cms, Map parameters) throws Exception {

		String siteName = CmsSiteManager.getCurrentSite(cms).getTitle();
		
		boolean fixContent = false;
		int batchSize = 0;
		int sleepTime = 0;
		
		if(parameters.get("fixContent")!= null)
			fixContent = Boolean.parseBoolean((String)parameters.get("fixContent"));
		
		if(parameters.get("batchSize")!=null)
			batchSize = Integer.parseInt(parameters.get("batchSize").toString());
		
		if (parameters.get("sleepTime")!=null)
			sleepTime = Integer.parseInt(parameters.get("sleepTime").toString());
		
		
		// le quitamos la barra del final
		siteName = siteName.substring(0, siteName.length() - 1);

		// 1.DESPUBLICACION AUTOMATICA estado = "Activa" o "Cerrada" y publicada
		// como "Encuesta Activa" o "Encuesta Anterior"
		// y que tiene "fecha de despublicación" igual o menor a la fecha del
		// día.
		// Entonces el sistema saca de publicación, la mencionada encuesta.
		List<String> logDespublicadas = ModuloEncuestas.despublicarEncuestasProgramadas(siteName,cms,fixContent,batchSize, sleepTime);

		// 2. CIERRE AUTOMATICO estado = "Activa" y tiene "fecha de cierre"
		// igual o menor a la fecha del día
		// entonces la cierra
		List<String> logCerradas = ModuloEncuestas.cerrarEncuestasProgramadas(siteName,cms,fixContent,batchSize, sleepTime);
		
		StringBuffer sb = new StringBuffer();
		sb.append("Despublicacion Automática\n");
		for (Iterator it = logDespublicadas.iterator(); it.hasNext();) {
			sb.append( it.next() + "\n");
		}

		sb.append("Cierre Automático\n");
		for (Iterator it = logCerradas.iterator(); it.hasNext();) {
			sb.append(it.next() + "\n");
		}
		
		// 3.Purga votos de encuestas cerradas de forma manual
		ModuloEncuestas.PurgarVotosEncuestasCerradas(cms);
		
		sb.append("Se purgo la tabla de votos por IP/Usuario para las encuestas cerradas y despublicadas \n");
		
		return sb.toString();

	}

}
