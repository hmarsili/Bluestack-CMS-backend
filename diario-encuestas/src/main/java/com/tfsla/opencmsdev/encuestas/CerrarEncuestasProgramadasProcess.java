package com.tfsla.opencmsdev.encuestas;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.report.CmsLogReport;

import com.tfsla.diario.ediciones.services.openCmsService;
import com.tfsla.utils.CmsResourceUtils;
import com.tfsla.utils.DateUtils;
import com.tfsla.workflow.QueryBuilder;
import com.tfsla.workflow.ResultSetProcessor;

public class CerrarEncuestasProgramadasProcess extends AbstractScheduledEncuestaProcess {

	public List<String> execute(String siteURL, CmsObject cms, boolean fixContent, int batchSize, int sleepTime) {
		// 2. CIERRE AUTOMATICO estado = "Activa" y tiene "fecha de cierre"
		// igual o menor a la fecha del día
		// entonces la cierra
		List<String> log = new ArrayList<String>();
		List activasURLs = getEncuestasActivas(cms);
		List<CmsResource> publishList = new ArrayList<CmsResource>();
		
		int cont = 0;
		boolean batchMode = false;
		
		if(batchSize>0 && sleepTime>0)
			batchMode = true;

		for (Iterator it = activasURLs.iterator(); it.hasNext();) {
			
			cont++;
			
			String url = (String) it.next();

			String result = cerrarSiHaceFalta(cms, siteURL, url, fixContent);
			if (!NADA.equals(result)) {
				
				log.add(result);
				
				if(result.indexOf("ERROR")==-1){
					CmsResource resource;
					try {
						resource = cms.readResource(url);
						publishList.add(resource);
					} catch (CmsException e) {
						log.add("ERROR - Cierre encuesta:" + siteURL + url + " No se pudo publicar el contenido estructurado. Causa: "+ e.getMessage());
					}
				}
			}
			
			if (batchMode && (cont % batchSize == 0)){
				try {
					
					// Publicamos las encuestas modificadas y vaciamos la lista
					try {
						OpenCms.getPublishManager().publishProject(cms, new CmsLogReport(Locale.getDefault(), this.getClass()), OpenCms.getPublishManager().getPublishList(cms,publishList, false));
						publishList = new ArrayList<CmsResource>();
						
					} catch (CmsException e) {
						log.add("ERROR - Cierre encuestas por lotes. No se pudo publicar la lista de encuestas modificadas en el vfs. Causa: "+ e.getMessage());
					}
					
					log.add("Se procesaron "+cont+" de "+ activasURLs.size() + " encuestas para cerrar.");
					
					Thread.sleep(sleepTime);
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		
		if(publishList.size()>0){
			try {
				OpenCms.getPublishManager().publishProject(cms, new CmsLogReport(Locale.getDefault(), this.getClass()), OpenCms.getPublishManager().getPublishList(cms,publishList, false));
			} catch (CmsException e) {
				log.add("ERROR - Cierre encuesta. No se pudo publicar la lista de encuestas modificadas en el vfs. Causa: "+ e.getMessage());
			}
		}

		return log;
	}

	private String cerrarSiHaceFalta(CmsObject cms, String siteURL, String encuestaURL, boolean fixContent) 
	{
		String encuestaFullPath = encuestaURL;
			
		// Intentamos deslockear el contenido estructurado
		try{
				CmsResourceUtils.forceLockResource(cms,encuestaURL);
		}catch(CmsException e){
				return "ERROR - Cierre encuesta:" + siteURL + encuestaURL + " No se pudo lockear ["+ e.getMessage()+"]";	
		}
			
		// Validamos si se puede leer el contenido estructurado
		boolean isValidStructureContent = false;
			
		try{
				isValidStructureContent = isValidStructureContent(cms, encuestaURL);
				
				boolean fixValidStructureContent = false;
				
				if (!isValidStructureContent && fixContent){
					fixValidStructureContent = fixValidStructureContent(cms, encuestaURL);
				
					if(!fixValidStructureContent)
						return "ERROR - Cierre encuesta:" + siteURL + encuestaURL + " El contenido estructurado no es válido y no se pudo corregir";
				
				}else if(!isValidStructureContent && !fixContent){
					return "ERROR - Cierre encuesta:" + siteURL + encuestaURL + " El contenido estructurado no es válido";
				}
		}catch(Exception e){
				return "ERROR - Cierre encuesta:" + siteURL + encuestaURL + " El contenido estructurado no es válido ["+ e.getMessage()+"]";
		}
			
			
		try {
			Encuesta encuesta = Encuesta.getEncuestaFromURL(cms, encuestaFullPath);

			Long fechaCierre = Long.parseLong(encuesta.getFechaCierre());
			Long fechaActual = Long.parseLong(DateUtils.today());

			if (fechaCierre.longValue() != 0 && (fechaCierre.longValue() <= fechaActual.longValue())) {
				// ya paso la fecha de cierre, y como se trata de una encuesta
				// activa, hay que cerrarla
				
				String updateEstadoEncuestaContent = this.updateEstadoEncuestaContent(cms, encuestaFullPath, Encuesta.CERRADA);
				
				if(updateEstadoEncuestaContent!= null)
					return updateEstadoEncuestaContent;
				
				this.updateEstadoPublicacionCerrada(cms, encuestaURL);

				CmsResourceUtils.unlockResource(cms, encuestaFullPath, false);
				
				return "Encuesta cerrada: "+ encuestaFullPath;
			}
			
			CmsResourceUtils.unlockResource(cms, encuestaFullPath, false);

			return NADA;
		}
		catch (Exception e) {
				return "ERROR - Cierre encuesta:" + siteURL + encuestaURL + " No se pudo cerrar, causa: ["+ e.getMessage()+"]";
		}
	}

	/**
	 * TODO esto deberia obtener las activas del cms y no de tablas custom
	 * 
	 * @param cms
	 * @return las encuestas activas en el online
	 */
	private static List<String> getEncuestasActivas(CmsObject cms) {
		String selectEncuestasActivaSQL = "SELECT " + URL_ENCUESTA + " FROM " + TFS_ENCUESTA_ONLINE + " "
				+ "WHERE " + ESTADO_PUBLICACION + " = '" + Encuesta.ACTIVA + "' AND " + SITIO + " = '" + openCmsService.getCurrentSite(cms) + "'";

		List<String> results = new QueryBuilder<List<String>>(cms).setSQLQuery(selectEncuestasActivaSQL)
				.execute(new EncuestasURLsResultSetProcessor());

		return results;
	}

	private void updateEstadoPublicacionCerrada(CmsObject cms, String encuestaURL) {
		String updateEstadoPublicacionSQL = getUpdateEstadoPublicacionQuery(cms, encuestaURL, Encuesta.CERRADA,
				TFS_ENCUESTA);

		new QueryBuilder<String>(cms).setSQLQuery(updateEstadoPublicacionSQL).execute();

		String updateEstadoPublicacionOnlineSQL = getUpdateEstadoPublicacionQuery(cms, encuestaURL,
				Encuesta.CERRADA, TFS_ENCUESTA_ONLINE);

		new QueryBuilder<String>(cms).setSQLQuery(updateEstadoPublicacionOnlineSQL).execute();
	}
	
}
