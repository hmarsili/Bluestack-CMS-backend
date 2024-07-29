package com.tfsla.opencmsdev.encuestas;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.report.CmsLogReport;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

import com.tfsla.utils.CmsResourceUtils;
import com.tfsla.utils.DateUtils;
import com.tfsla.workflow.QueryBuilder;

/**
 * "Despublica" una encuesta. En realidad, esto quiere decir que sigue publicada en el offline, pero el modulo
 * marca su estado como "despublicada" y eso hace que no aparezca nunca mas.
 * 
 * @author jpicasso
 */
public class DespublicarEncuestasProgramadasProcess extends AbstractScheduledEncuestaProcess {

	public List<String> execute(String siteURL, CmsObject cms, boolean fixContent, int batchSize, int sleepTime) {
		// NO TIENE SENTIDO DESPUBLICAR UNA ENCUESTA ACTIVA; ASI QUE SOLO LO
		// HAGO PARA LAS CERRADAS
		List<String> log = new ArrayList<String>();
		List cerradasURLs = ModuloEncuestas.getEncuestasCerradas(cms, TFS_ENCUESTA_ONLINE, null, null);
		List<CmsResource> publishList = new ArrayList<CmsResource>();
		
		int cont = 0;
		boolean batchMode = false;
		
		if(batchSize>0 && sleepTime>0)
			batchMode = true;

		for (Iterator it = cerradasURLs.iterator(); it.hasNext();) {
			
			cont++;
			
			String url = (String) it.next();

			String result = despublicarSiHaceFalta(cms, siteURL, url, fixContent);
			
			if (!NADA.equals(result)) {
				
				log.add(result);
				
				if(result.indexOf("ERROR")==-1){
					CmsResource resource;
					try {
						resource = cms.readResource(url);
						publishList.add(resource);
					} catch (CmsException e) {
						log.add("ERROR - Despublicación encuesta:" + siteURL + url + " No se pudo publicar el contenido estructurado. Causa: "+ e.getMessage());
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
					
					log.add("Se procesaron "+cont+" de "+ cerradasURLs.size() + " encuestas para despublicar.");
					
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
				log.add("ERROR - Despublicación encuesta. No se pudo publicar la lista de encuestas modificadas en el vfs. Causa: "+ e.getMessage());
			}
		}

		return log;
	}

	public String despublicarSiHaceFalta(CmsObject cms, String siteURL, String encuestaURL, boolean fixContent) {
		
		// Intentamos deslockear el contenido estructurado
		try{
			CmsResourceUtils.forceLockResource(cms,encuestaURL);
		}catch(CmsException e){
			return "ERROR - Despublicación encuesta:" + siteURL + encuestaURL + " No se pudo lockear ["+ e.getMessage()+"]";	
		}
		
		// Validamos si se puede leer el contenido estructurado
		boolean isValidStructureContent = false;
		
		try{
			isValidStructureContent = isValidStructureContent(cms, encuestaURL);
			
			boolean fixValidStructureContent = false;
			
			if (!isValidStructureContent && fixContent){
				fixValidStructureContent = fixValidStructureContent(cms, encuestaURL);
			
				if(!fixValidStructureContent)
					return "ERROR - Despublicación encuesta:" + siteURL + encuestaURL + " El contenido estructurado no es válido y no se pudo corregir";
			
			}else if(!isValidStructureContent && !fixContent){
				return "ERROR - Despublicación encuesta:" + siteURL + encuestaURL + " El contenido estructurado no es válido";
			}
		}catch(Exception e){
			return "ERROR - Despublicación encuesta:" + siteURL + encuestaURL + " El contenido estructurado no es válido ["+ e.getMessage()+"]";
		}
		
		try{
			
			Encuesta encuesta = Encuesta.getEncuestaFromURL(cms, encuestaURL);

			Long fechaDespublicacion = Long.parseLong(encuesta.getFechaDespublicacion());
			Long fechaActual = Long.parseLong(DateUtils.today());

			if (fechaDespublicacion.longValue() != 0 && (fechaDespublicacion.longValue() <= fechaActual.longValue())) {
				// ya paso la fecha de despublicacion, ergo hay que
				// despublicarla
				
				String updateEstadoEncuestaContent = this.updateEstadoEncuestaContent(cms, encuestaURL, Encuesta.DESPUBLICADA);
				
				if(updateEstadoEncuestaContent!= null)
					return updateEstadoEncuestaContent;
				
				this.updateEstadoPublicacionDespublicada(cms, encuestaURL);
				CmsResourceUtils.unlockResource(cms, encuestaURL, false);
				
				return "Encuesta despublicada: " + siteURL + encuestaURL;
			}
			
			CmsResourceUtils.unlockResource(cms, encuestaURL, false);

			return NADA;
		}
		catch (Exception e) {
			//throw new RuntimeException("Error al intentar despublicar la encuesta [" + encuestaURL + "] Causa: " + e, e);
			return "ERROR - Despublicación encuesta:" + siteURL + encuestaURL + " No se pudo despublicar, causa: ["+ e.getMessage()+"]";
		}
	}

	private void updateEstadoPublicacionDespublicada(CmsObject cms, String encuestaURL) {
		String updateEstadoPublicacionSQL = getUpdateEstadoPublicacionQuery(cms, encuestaURL, Encuesta.DESPUBLICADA,
			TFS_ENCUESTA);

		new QueryBuilder<String>(cms).setSQLQuery(updateEstadoPublicacionSQL).execute();

		String updateEstadoPublicacionOnlineSQL = getUpdateEstadoPublicacionQuery(cms, encuestaURL, Encuesta.DESPUBLICADA,
			TFS_ENCUESTA_ONLINE);

		new QueryBuilder<String>(cms).setSQLQuery(updateEstadoPublicacionOnlineSQL).execute();
	}
	
	public String updateDateDespublicada(CmsObject cms, String encuestaURL) {
		try {
			
			CmsResourceUtils.forceLockResource(cms,encuestaURL);
			
		//	Encuesta encuesta = Encuesta.getEncuestaFromURL(jsp.getCmsObject(), encuestaURL);
		//	encuesta.despublicar();

			Locale locale = java.util.Locale.ENGLISH;
			
			CmsFile readFile = cms.readFile(encuestaURL);
			CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, readFile);
					      content.setAutoCorrectionEnabled(true); 
			              content.correctXmlStructure(cms);
			              content.getValue("fechaDespublicacion", locale).setStringValue(cms, DateUtils.today());
			              readFile.setContents(content.marshal());
			              cms.writeFile(readFile);
			       
			 CmsResourceUtils.unlockResource(cms, encuestaURL, false);
			              
			 return NADA;
		}
		catch (Exception e) {
			//throw new RuntimeException("Error al intentar despublicar la encuesta [" + encuestaURL + "] Causa: " + e, e);
			return "ERROR - set Fecha Despublicación encuesta:" + encuestaURL + " No se pudo despublicar, causa: ["+ e.getMessage()+"]";
		}
	}
	
}