package com.tfsla.diario.admin.jobs;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.configuration.CmsSchedulerConfiguration;
import org.opencms.db.CmsDefaultUsers;
import org.opencms.db.CmsResourceState;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsContextInfo;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.scheduler.CmsScheduledJobInfo;
import org.opencms.scheduler.I_CmsScheduledJob;
import org.opencms.scheduler.jobs.CmsPublishScheduledJob;
import org.opencms.security.CmsRole;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplaceAction;
import org.opencms.workplace.commons.Messages;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import com.tfsla.planilla.herramientas.PlanillaFormConstants;

import com.tfsla.diario.admin.jsp.TfsNewsAdminJson;
import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.FreshnessService;
import com.tfsla.diario.ediciones.services.PinesService;
import com.tfsla.diario.ediciones.services.TipoEdicionService;
import com.tfsla.diario.freshness.FreshnessDAO;
import com.tfsla.diario.freshness.model.Freshness;
import com.tfsla.diario.pines.model.Pin;
import com.tfsla.utils.CmsResourceUtils;

import net.sf.json.JSONObject;

public class ProcesarFrescuras implements I_CmsScheduledJob {

	private static final Log LOG = CmsLog.getLog(ProcesarFrescuras.class);

	private String resultados = "";
	private String path="";
	private boolean doit = true;
	private CPMConfig configura;
	private CmsObject cms;
	private Freshness FreshnessProcess;
	private CmsResource resource;
	CmsUUID uui = null;

	public String launch(CmsObject cmsObj, Map parameters) throws Exception {

		resultados = "";
		int publication=0;
		int hoursAfter=0;

		String _hoursAfter = (String)parameters.get("hoursAfter");
		String _publication = (String)parameters.get("publication");

		String _test = (String)parameters.get("test");
		doit = (_test==null || !_test.toLowerCase().trim().equals("true"));


		if (_hoursAfter!=null)
			hoursAfter = Integer.parseInt(_hoursAfter);

		if (_publication!=null)
			publication = Integer.parseInt(_publication);

		TipoEdicionService tService = new TipoEdicionService();
		TipoEdicion pub = tService.obtenerTipoEdicion(publication);
		String _sitename = "/sites/"+pub.getProyecto();

		cms = cmsObj;
		
		CPMConfig configxsml = CmsMedios.getInstance().getCmsParaMediosConfiguration();
		
		
		LOG.info("Lanzando ejecucion de noticias  con frescura entre la/s ultima/s " + hoursAfter + " hora/s " + 
				" de la _publication "+ _publication + " en _sitename " +_sitename);


		resultados += "\n";
		if (!doit)
			resultados += "TEST MODE\n";

		resultados += "Lanzando ejecucion de noticias  con frescura entre la/s ultima/s " + hoursAfter + " hora/s " + 
				" de la _publication "+ _publication + " en _sitename " + _sitename ;


		//NAA-3552 Desde el front se guarda hora redaccion. Desde el back levantamos igual, pero por alguna razon desde el front se levanta 
		// con -1 hora entonces desde el back, lo levantamos asi. 
		int timeZoneRedaction = Integer.parseInt(configxsml.getParam(_sitename, _publication, "admin-settings", "gmtRedaction").replaceFirst("-", ""))+1;		
		String timeZoneRedactionGMT = "GMT-"+timeZoneRedaction;
		
		Calendar nowGMT = Calendar.getInstance();		
		nowGMT.setTimeZone(TimeZone.getTimeZone(timeZoneRedactionGMT));
		nowGMT.add(Calendar.HOUR, 1);
		nowGMT.set(Calendar.MILLISECOND, 0);
		nowGMT.set(Calendar.SECOND, 0);
		nowGMT.set(Calendar.MINUTE, 0);
		

		Calendar nextCal = Calendar.getInstance();
		nextCal.setTimeZone(TimeZone.getTimeZone(timeZoneRedactionGMT));
		nextCal.add(Calendar.HOUR, hoursAfter + 1);
		nextCal.set(Calendar.MILLISECOND, 0);
		nextCal.set(Calendar.SECOND, 0);
		nextCal.set(Calendar.MINUTE, 0);
		

		LOG.info("Se busca en la base de datos la/s noticia/s entre  " + nowGMT.getTimeInMillis() + " y " +  nextCal.getTimeInMillis());

		FreshnessDAO freshDAO = new FreshnessDAO();
		@SuppressWarnings("unchecked")
		//List<Freshness> FreshnessList = freshDAO.getFreshness(publication, nowGMT.getTimeInMillis(), nextCal.getTimeInMillis());
		ArrayList<Freshness> FreshnessList = freshDAO.getFreshness(publication, nowGMT.getTimeInMillis(), nextCal.getTimeInMillis());

		LOG.info("Se encontraron " + FreshnessList.size() + " noticias a procesar");

		for (int i=0;i<FreshnessList.size();i++) {

			FreshnessProcess = FreshnessList.get(i);
			LOG.info("Proceso de la noticia " + FreshnessProcess.getUrl() + " ("+i+"/"+FreshnessList.size()+")");

			resource = cms.readResource(cms.getRequestContext().removeSiteRoot(FreshnessProcess.getUrl()),
					CmsResourceFilter.ALL);

		// SE comenta por ticket NAA-2380
			//if (!resource.getState().equals(CmsResourceState.STATE_NEW)) {

				try {
					uui = resource.getUserCreated();
				}catch (Exception e) {
					e.printStackTrace();
				}	

				if (!FreshnessProcess.getZone().isEmpty() && !FreshnessProcess.getZone().equals("")
						&& FreshnessProcess.getPriority() > 0) {

					JSONObject jsonRequest = new JSONObject();
					JSONObject jsonAuthe = new JSONObject();
					jsonAuthe.put("publication", _publication);
					jsonRequest.put("authentication", jsonAuthe);
					JSONObject jsonNew = new JSONObject();
					jsonRequest.put("news", jsonNew);


					TfsNewsAdminJson newsAdmin = new TfsNewsAdminJson(jsonRequest, cms);

					String dateUpdate = (FreshnessProcess.getRepublication().equals("DATE_UPDATED")) ? ""+FreshnessProcess.getDate() : "";

					JSONObject newsAdminResult = newsAdmin.save(FreshnessProcess.getUrl(), "",FreshnessProcess.getZone(), Integer.toString(FreshnessProcess.getPriority()), "", "", "", dateUpdate);	

					if (!newsAdminResult.getString("status").equals("ok")) {
						resultados += "ERROR al Actualizar la zona y prioridad de la noticia: " +
								FreshnessProcess.getUrl() + " - " + FreshnessProcess.getZone() + " - " + Integer.toString(FreshnessProcess.getPriority()) +" - "+ FreshnessProcess.getDate();
						LOG.error("Error al actualizar los datos de zona y prioridad (" +
								FreshnessProcess.getUrl() + " - " + FreshnessProcess.getZone() + " - " + Integer.toString(FreshnessProcess.getPriority()) +" - "+ FreshnessProcess.getDate() + ")");
					} else {
						resultados += "Se Actualizó correctamente la noticia: " +
								FreshnessProcess.getUrl() + " - " + FreshnessProcess.getZone() + " - " + Integer.toString(FreshnessProcess.getPriority()) +" - "+ FreshnessProcess.getDate();
						LOG.info("Se actualizaron correctamente los datos de zona y prioridad en la noticia");
					}
				}


				if (FreshnessProcess.getRepublication().equals("MANUAL")) {

					pinearNoticia();

					// 	se comenta por call con hm 
					// updateLastModDate();

				} else {
					//automatca

					path = FreshnessProcess.getUrl();

					//Date publishDate = new Date(FreshnessProcess.getDate());

					Calendar publishDateCalChk = Calendar.getInstance(TimeZone.getTimeZone("GMT-0"));
					publishDateCalChk.setTimeInMillis(FreshnessProcess.getDate());
					
					// check if the selected date is in the future
					if (nowGMT.compareTo(publishDateCalChk) > 0 ) {
						// the selected date in in the past, this is not possible

						LOG.error("Problema con la fecha de próxima ejecución para la noticia " + path + " con fecha " + publishDateCalChk.getTime().getTime());

						//Seguir con el siguiente registro .
						continue;
						//throw new CmsException(
						//		Messages.get().container(Messages.ERR_PUBLISH_SCHEDULED_DATE_IN_PAST_1, publishDate));
					}
					Calendar publishDateCal = Calendar.getInstance();
					publishDateCal.setTimeInMillis(FreshnessProcess.getDate());
					


					if (FreshnessProcess.getRepublication().equals("DATE_UPDATED")) {
						updateLastModDate();	    				
					}

					//NAA-3486 seteamos el estado pendiente de publicarion
					if (resource.getState().equals(CmsResourceState.STATE_NEW)) {
						setPendingPublicationStatus();
					}
					
					CmsWorkplaceAction action = CmsWorkplaceAction.getInstance();
					CmsObject cmsAdmin = action.getCmsAdminObject();
					cmsAdmin.getRequestContext().setSiteRoot(cms.getRequestContext().getSiteRoot());
					
					CmsObject cmsFressUser = OpenCms.initCmsObject(new CmsDefaultUsers().getUserGuest());
					cmsFressUser.loginUser(FreshnessProcess.getUserName());
					cmsFressUser.getRequestContext().setSiteRoot(cms.getRequestContext().getSiteRoot());
					cmsFressUser.getRequestContext().setCurrentProject(cms.readProject("Offline"));
					
					String jobDte = "";
					
					configura = CmsMedios.getInstance().getCmsParaMediosConfiguration();

					String dateFormat = configura.getParam(_sitename, _publication, "admin-settings", "dateFormat", "");
					String timeFormat = configura.getParam(_sitename, _publication, "admin-settings", "timeFormat", "12h");

					jobDte = dateFormat.toLowerCase();

					jobDte = jobDte.replaceAll("dd", String.valueOf(publishDateCal.get(Calendar.DAY_OF_MONTH)));
					jobDte = jobDte.replaceAll("mm", String.valueOf(publishDateCal.get(Calendar.MONTH) + 1));
					jobDte = jobDte.replaceAll("yyyy", String.valueOf(publishDateCal.get(Calendar.YEAR)));

					if (timeFormat.equals("12h"))
						jobDte = jobDte + " on "+  String.valueOf(publishDateCal.get(Calendar.HOUR)) +":"+ String.format("%02d",publishDateCal.get(Calendar.MINUTE))  + String.valueOf(publishDateCal.get(Calendar.AM_PM)); 
					else
						jobDte = jobDte + " on "+  String.valueOf(publishDateCal.get(Calendar.HOUR_OF_DAY)) +":"+ String.format("%02d",publishDateCal.get(Calendar.MINUTE)); 

					// the resource name to publish scheduled
					String resName = path;
					String projectName = "Publish " + resName + " at " + jobDte + " in " + _sitename;
					// the HTML encoding for slashes is necessary because of the slashes in english
					// date time format
					// in project names slahes are not allowed, because these are separators for
					// organizaional units
					projectName = projectName.replace("/", "&#47;");
					// create the project
					CmsProject tmpProject = cmsAdmin.createProject(projectName, "", CmsRole.WORKPLACE_USER.getGroupName(),
							CmsRole.PROJECT_MANAGER.getGroupName(), CmsProject.PROJECT_TYPE_TEMPORARY);
					// make the project invisible for all users
					tmpProject.setHidden(true);
					// write the project to the database
					cmsAdmin.writeProject(tmpProject);
					// set project as current project
					cmsAdmin.getRequestContext().setCurrentProject(tmpProject);
					cmsFressUser.getRequestContext().setCurrentProject(tmpProject);

					// copy the resource to the project
					cmsAdmin.copyResourceToProject(path);
					CmsResourceUtils.forceLockResource(cmsFressUser, path);
					
					/** INI NAA-2704
					 	CmsProperty prop = new CmsProperty("isScheduled", CmsProperty.DELETE_VALUE, "true");
					 
					cmsFressUser.writePropertyObject(path, prop);
					
					prop = new CmsProperty("isScheduledFresh", CmsProperty.DELETE_VALUE, "true");
					cmsFressUser.writePropertyObject(path, prop);
					
					prop = new CmsProperty("isScheduledData", CmsProperty.DELETE_VALUE, projectName);
					cmsFressUser.writePropertyObject(path, prop);
					*/
					
					cms.writePropertyObject(path, new CmsProperty("isScheduled","true",null));
					cms.writePropertyObject(path, new CmsProperty("isScheduledFresh","true",null));
					cms.writePropertyObject(path, new CmsProperty("isScheduledData",projectName,null));

					/** FIN NAA-2704 */


					LOG.info("Se programa el job para la frescura con el nombre " + projectName);

					CmsLock lock = cmsFressUser.getLock(path);
					if ((lock != null) && lock.isOwnedBy(cmsFressUser.getRequestContext().currentUser())
							&& !lock.isOwnedInProjectBy(cmsFressUser.getRequestContext().currentUser(),
									cmsFressUser.getRequestContext().currentProject())) {
						cmsFressUser.changeLock(path);
					}
					cmsFressUser.lockResource(path);
					lock = cmsFressUser.getLock(path);

					// Agreagamos tambien los recursos relacionados
					List<CmsResource> relatedResources = new ArrayList<CmsResource>();
					relatedResources = addRelatedResourcesToPublish(resource, true, true, new ArrayList<CmsResource>(), cmsFressUser);

					for (CmsResource relatedResource : relatedResources) {
						String rel_resourceName = cmsFressUser.getRequestContext().removeSiteRoot(relatedResource.getRootPath());
						cmsAdmin.copyResourceToProject(rel_resourceName);
						CmsLock lockRel = cmsFressUser.getLock(rel_resourceName);

						if ((lockRel != null) && lockRel.isOwnedBy(cmsFressUser.getRequestContext().currentUser())
								&& !lockRel.isOwnedInProjectBy(cmsFressUser.getRequestContext().currentUser(),
										cmsFressUser.getRequestContext().currentProject())) {
							cmsFressUser.changeLock(rel_resourceName);
						}

						cmsFressUser.lockResource(rel_resourceName);
					}

					CmsScheduledJobInfo job = new CmsScheduledJobInfo();
					String jobName = projectName;
					job.setJobName(jobName);
					job.setClassName("org.opencms.scheduler.jobs.CmsPublishScheduledJob");


					//Calendar calendarJob = Calendar.getInstance();
					//calendarJob.setTime(publishDate);
					String cronExpr = "" + publishDateCal.get(Calendar.SECOND) + " " + publishDateCal.get(Calendar.MINUTE) + " "
							+ publishDateCal.get(Calendar.HOUR_OF_DAY) + " " + publishDateCal.get(Calendar.DAY_OF_MONTH) + " "
							+ (publishDateCal.get(Calendar.MONTH) + 1) + " " + "?" + " " + publishDateCal.get(Calendar.YEAR);
					job.setCronExpression(cronExpr);
					job.setActive(true);
					CmsContextInfo contextInfo = new CmsContextInfo();
					contextInfo.setProjectName(projectName);

					contextInfo.setUserName(FreshnessProcess.getUserName());

/*					String userName = null;		
					try {
						userName =  cms.readUser(uui).getName().toString();
					}
					catch (Exception e) {
						e.printStackTrace();
					}	*/
					SortedMap<String, String> params = new TreeMap<String, String>();
					params.put(CmsPublishScheduledJob.PARAM_USER, FreshnessProcess.getUserName());
					params.put(CmsPublishScheduledJob.PARAM_JOBNAME, jobName);
					params.put(CmsPublishScheduledJob.PARAM_LINKCHECK, "true");
					job.setParameters(params);
					job.setContextInfo(contextInfo);
					OpenCms.getScheduleManager().scheduleJob(cmsAdmin, job);
					OpenCms.writeConfiguration(CmsSchedulerConfiguration.class);

				}

				if (FreshnessProcess.getType().equals("RECURRENCE")){ 

					Freshness freshness = new Freshness();
					FreshnessService freshService = new FreshnessService();

					Long newDateExcecute = 0L; //fresnessService.setDateFreshness(FreshnessProcess.getDate(),FreshnessProcess.getRecurrece());

					Calendar calNow = Calendar.getInstance();
					//calNow.getTime();

					Calendar calNext = Calendar.getInstance();
					calNext.setTime(new Date(FreshnessProcess.getDate()));
					calNext.add(calNext.DAY_OF_YEAR, FreshnessProcess.getRecurrece());
					//calNext.getTime();

					while(calNow.getTime().after(calNext.getTime())) {
						calNext.add(calNext.DAY_OF_YEAR, FreshnessProcess.getRecurrece());
						//calNow.getTime();
					}

					newDateExcecute = calNext.getTimeInMillis();

					freshness = FreshnessProcess;
					freshness.setDate(newDateExcecute);
					freshService.updateFreshness(freshness);

				} else {

					FreshnessService freshService = new FreshnessService();

					freshService.deleteFreshness(FreshnessProcess.getPublication(),FreshnessProcess.getSiteName(),FreshnessProcess.getUrl());

				}

			/**
			 * NAA-2380
			  } else {
				LOG.error("La noticia no esta publicada por ende no se procesa la frescura.");
			}
			*/

		}	
		return resultados;
	}


	public List<CmsResource> addRelatedResourcesToPublish(CmsResource resource, boolean unlock,
			boolean recursivePublish, List<CmsResource> resourcesList, CmsObject cms) {

		try {
			boolean publishRelatedContent = true;
			boolean publishRelatedNews = true;
			boolean forcePublish = true;

			int tipoNoticia = OpenCms.getResourceManager().getResourceType("noticia").getTypeId();
			int tipoVideoLink = OpenCms.getResourceManager().getResourceType("video-link").getTypeId();
			int tipoVideo = OpenCms.getResourceManager().getResourceType("video").getTypeId();

			int tipoOrigen = resource.getTypeId();
			boolean esVideoOrigen = (tipoOrigen == tipoVideo) || (tipoOrigen == tipoVideoLink);

			@SuppressWarnings("unchecked")
			List<CmsRelation> relations = cms.getRelationsForResource(cms.getSitePath(resource), CmsRelationFilter.ALL);

			for (CmsRelation relation : relations) {

				try {
					String rel1 = relation.getTargetPath();
					String rel2 = relation.getTargetPath();

					String rel = "";
					if (rel1.equals(resource.getRootPath()))
						rel = rel2;
					else
						rel = rel1;

					CmsResource res = cms.readResource(cms.getRequestContext().removeSiteRoot(rel));

					CmsResourceState estado = res.getState();

					if (estado.equals(CmsResourceState.STATE_UNCHANGED))
						continue;

					int tipo = res.getTypeId();

					CmsLock lock = cms.getLock(res);
					boolean esNoticia = (tipo == tipoNoticia);
					boolean esVideo = (tipo == tipoVideo) || (tipo == tipoVideoLink);

					if (!lock.isUnlocked() && unlock) {
						if (lock.getUserId().equals(cms.getRequestContext().currentUser().getId())) {
							cms.unlockResource(cms.getRequestContext().removeSiteRoot(rel));
						} else if (forcePublish) {
							cms.changeLock(cms.getRequestContext().removeSiteRoot(rel));
							cms.unlockResource(cms.getRequestContext().removeSiteRoot(rel));
						} else {
							continue;
						}
					}

					// LOG.info("esVideoOrigen " + esVideoOrigen + " | esVideo " + esVideo + " |
					// publishRelatedContent " + publishRelatedContent );
					if (esVideoOrigen && esVideo) {
						if (publishRelatedContent)
							if (!resourcesList.contains(res))
								resourcesList.add(res);

					} else if (!esNoticia) {
						if (publishRelatedContent)
							if (!resourcesList.contains(res))
								resourcesList.add(res);

						if (esVideo && publishRelatedContent)
							addRelatedResourcesToPublish(res, unlock, true, resourcesList, cms);
					} else if (publishRelatedNews && recursivePublish) {
						if (!resourcesList.contains(res))
							resourcesList.add(res);
						addRelatedResourcesToPublish(res, unlock, false, resourcesList, cms);

					}

				} catch (CmsException e) {
					LOG.error("Error en frescura dentro de funcion addRelatedResourcesToPublish",e);
					//					e.printStackTrace();
				}
			}
		} catch (CmsException e) {
			LOG.error("Error en frescura dentro de funcion addRelatedResourcesToPublish",e);
			//			e.printStackTrace();
		}

		return resourcesList;

	}


	private void pinearNoticia() throws CmsException {

		//PINEO LA NOTICIA    			
		PinesService pService = new PinesService();

		if (!pService.isExistsPin(uui.toString(), FreshnessProcess.getPublication(), FreshnessProcess.getUrl()).getBoolean("isExitPin")) {
			Pin newPin = new Pin();			
			newPin.setResource(FreshnessProcess.getUrl());
			newPin.setUser(uui.toString());
			newPin.setOrder(1);
			newPin.setPublication(FreshnessProcess.getPublication());
			newPin.setResourceType(OpenCms.getResourceManager().getResourceType("noticia").getTypeId());

			JSONObject resultPin = pService.newPin(newPin);
			if (!resultPin.getString("status").equals("ok")) {
				LOG.error("Tipo de Frescura DATE_EXACT - Error al Pinear la noticia");
				resultados += "ERROR al Pinear la noticia" + newPin.getResource() + " - " +  newPin.getPublication() + " - " +  newPin.getUser();
			} else {

				LOG.info("Tipo de Frescura DATE_EXACT - Se agrego correctamente la noticia como pin");
				resultados += "Se agrego correctamente la noticia como Pin" + newPin.getResource() + " - " +  newPin.getPublication() + " - " +  newPin.getUser();

			}
		}

	}
	
	private void setPendingPublicationStatus() {

		try {		
			CmsResourceUtils.forceLockResource(cms,path);
			CmsFile fileNew = cms.readFile(path,CmsResourceFilter.ALL);

			CmsXmlContent contentNew = CmsXmlContentFactory.unmarshal(cms, fileNew);
			contentNew.setAutoCorrectionEnabled(true);
			contentNew.correctXmlStructure(cms);
			if (!contentNew.getValue("estado", Locale.ENGLISH).getStringValue(cms).equals(PlanillaFormConstants.PUBLICADA_VALUE))
				contentNew.getValue("estado", Locale.ENGLISH).setStringValue(cms, PlanillaFormConstants.PENDIENTE_PUBLICACION_VALUE);

			String fileEncoding = cms.readPropertyObject(cms.getRequestContext().removeSiteRoot(fileNew.getRootPath()), CmsPropertyDefinition.PROPERTY_CONTENT_ENCODING, true).getValue(OpenCms.getSystemInfo().getDefaultEncoding());

			String decodedContent = contentNew.toString();
			decodedContent = decodedContent.replaceAll("(?!\\n)[\\p{C}]", "");

			fileNew.setContents(decodedContent.getBytes(fileEncoding));

			cms.writeFile(fileNew);							
			cms.unlockResource(path);

		} catch (Exception ex) {
			CmsLog.getLog(this).error ("Error al intentar modificar la noticia con la fecha de ultima modificacion - Publica la nota", ex  );
		}

	}

	private void updateLastModDate() {
		Date publishDate = new Date(FreshnessProcess.getDate());

		String lastModification = "";
		String strLastModifDate = ""+publishDate.getTime();
		if (!strLastModifDate.equals("") || !strLastModifDate.equals("0"))
			lastModification = strLastModifDate;

		try {		
			CmsResourceUtils.forceLockResource(cms,path);
			CmsFile fileNew = cms.readFile(path,CmsResourceFilter.ALL);

			CmsXmlContent contentNew = CmsXmlContentFactory.unmarshal(cms, fileNew);
			contentNew.setAutoCorrectionEnabled(true);
			contentNew.correctXmlStructure(cms);
			if (!lastModification.equals(""))
				contentNew.getValue("ultimaModificacion", Locale.ENGLISH).setStringValue(cms, String.valueOf(lastModification));

			String fileEncoding = cms.readPropertyObject(cms.getRequestContext().removeSiteRoot(fileNew.getRootPath()), CmsPropertyDefinition.PROPERTY_CONTENT_ENCODING, true).getValue(OpenCms.getSystemInfo().getDefaultEncoding());

			String decodedContent = contentNew.toString();
			decodedContent = decodedContent.replaceAll("(?!\\n)[\\p{C}]", "");

			fileNew.setContents(decodedContent.getBytes(fileEncoding));

			cms.writeFile(fileNew);							
			cms.unlockResource(path);

		} catch (Exception ex) {
			CmsLog.getLog(this).error ("Error al intentar modificar la noticia con la fecha de ultima modificacion - Publica la nota", ex  );
		}

	}
}