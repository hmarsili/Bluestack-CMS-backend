package com.tfsla.opencmsdev;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.opencms.db.CmsPublishList;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.relations.CmsRelationType;
import org.opencms.security.CmsSecurityException;
import org.opencms.site.CmsSite;
import org.opencms.xml.CmsXmlEntityResolver;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.I_CmsXmlContentValue;

import com.tfsla.diario.multiselect.A_DescriptionLoader;
import com.tfsla.utils.CmsObjectUtils;
import com.tfsla.utils.CmsResourceUtils;

public class LinkedArticleListener extends A_DescriptionLoader implements I_CmsEventListener {

	@Override
	public void cmsEvent(CmsEvent event) {
		
		int noticiaType =-1;

		try {
			noticiaType = OpenCms.getResourceManager().getResourceType("noticia").getTypeId();
		} catch (CmsLoaderException e) {
			CmsLog.getLog(this).error("Error al intentar obtener el identificador de la noticia",e);
		}

		CmsObject cmsObject = null;

		/* Eventos:
		 * EVENT_RESOURCE_AND_PROPERTIES_MODIFIED = usamos este evento para cuando viene de editar una nota desde el wp
		 * EVENT_RESOURCE_COPIED = este evento se usa para revisar los cambios cuando viene desde el formulario de la admin 
		 * EVENT_RESOURCE_DELETED = cuando borra una nota, tiene que revisar si es necesario desvicular alguna
		 * 
		 * NO usamos EVENT_RESOURCE_MODIFIED porque cuando una noticia tiene una asociada, entra en ciclo infinito
		 *  */

		if (event.getType()==I_CmsEventListener.EVENT_RESOURCE_AND_PROPERTIES_MODIFIED ||
				event.getType()==I_CmsEventListener.EVENT_RESOURCE_COPIED || 
				event.getType()==I_CmsEventListener.EVENT_BEFORE_PUBLISH_PROJECT || 
				event.getType()==I_CmsEventListener.EVENT_BEFORE_RESOURCE_DELETED ) {

				//cmsObject = TfsContext.getInstance().getCmsObject();

				try {
					CmsObject cmsObjectToClone = (CmsObject)event.getData().get(I_CmsEventListener.KEY_CMS_OBJECT);
					if (cmsObjectToClone != null) {
						cmsObject = CmsObjectUtils.getClone(cmsObjectToClone);
					} else {
						CmsUser user = (CmsUser)event.getData().get(I_CmsEventListener.KEY_USER);
						cmsObject = CmsObjectUtils.loginUser(user); 
					}
						
					if (cmsObject != null)
							cmsObject.getRequestContext().setCurrentProject(cmsObject.readProject("Offline"));
				} catch (Exception ex){
					CmsLog.getLog(this).error("Error al intentar obtener el cmsObject del evento",ex);
				}
				if (cmsObject == null) {
					cmsObject = CmsObjectUtils.loginAsAdmin();		
				}
				
				if(cmsObject!=null){
					
					CmsResource resource = (CmsResource) event.getData().get("resource");
					boolean estaLockeada = false;
					try { 
					estaLockeada = cmsObject.getLock(resource).isUnlocked();
					} catch (Exception ex) {
						
					}
					if (event.getType()==I_CmsEventListener.EVENT_RESOURCE_COPIED){
						List <CmsResource> resources = (List<CmsResource>) event.getData().get("resources");
						resource = resources.get(0);
					}
					
					if (event.getType()==I_CmsEventListener.EVENT_BEFORE_PUBLISH_PROJECT){
						CmsPublishList publishList = (CmsPublishList)event.getData().get("publishList");
						List<CmsResource> listaCms = publishList.getFileList();
						if(listaCms.size()>0)
						    resource = listaCms.get(0);
					}

					if (event.getType()==I_CmsEventListener.EVENT_RESOURCE_AND_PROPERTIES_MODIFIED || 
							event.getType()==I_CmsEventListener.EVENT_RESOURCE_COPIED || 
							event.getType()==I_CmsEventListener.EVENT_BEFORE_PUBLISH_PROJECT) {

						if(resource!=null && !resource.getState().equals(CmsResource.STATE_DELETED)){

							if (resource.getTypeId() == noticiaType) {
								String url = CmsResourceUtils.getLink(resource);

								if (!resource.getName().startsWith("~")) {

									try {
										cmsObject.getRequestContext().setCurrentProject(cmsObject.readProject("Offline"));
										
										
										CmsSite site = OpenCms.getSiteManager().getSiteForRootPath(resource.getRootPath());
										if (site==null)
											return;
										
										cmsObject.getRequestContext().setSiteRoot(site.getSiteRoot());

										CmsXmlContent content = getXmlContent(cmsObject, url);
										
										if (cmsObject.getLock(resource).getType() != org.opencms.lock.CmsLockType.PUBLISH) {
											LinkedArticles(cmsObject, content, site);
											
										}	
									} catch (CmsException e) {
										CmsLog.getLog(this).error("Error al relacionar noticias multipublicacion en la publicacion",e);
									}
								}
							}

						}

					}

					if (event.getType()==I_CmsEventListener.EVENT_BEFORE_RESOURCE_DELETED) {
					
						List <CmsResource> resources = (List<CmsResource>) event.getData().get("resources");
						resource = resources.get(0);

						if(resource!=null){

							if (!resource.getName().startsWith("~")) {
								if (resource.getTypeId() == noticiaType) {

									String url = CmsResourceUtils.getLink(resource);

									try {
										cmsObject.getRequestContext().setCurrentProject(cmsObject.readProject("Offline"));
										
										CmsSite site = OpenCms.getSiteManager().getSiteForRootPath(resource.getRootPath());
										if (site==null)
											return;
										
										cmsObject.getRequestContext().setSiteRoot(site.getSiteRoot());

										CmsXmlContent content = getXmlContent(cmsObject, url);
										
										if (cmsObject.getLock(resource).getType() != org.opencms.lock.CmsLockType.PUBLISH) {
										  
										      //  UnLinkedArticles(cmsObject, content, site);
										   
										}

									} catch (CmsException e) {
										CmsLog.getLog(this)
										.error("Error al intentar actualizar noticias relacionadas multipubliacion en borrado de noticia ",
												e);
									}

								}
							}
						}
					}

				}

		}
	}
	
	protected void LinkedArticles(CmsObject cmsObject, CmsXmlContent content, CmsSite site) throws CmsException{

		try {	
			CmsRelationType linkedArticleType = CmsRelationType.valueOf("linkedArticle");
			
			String masterPath = cmsObject.getRequestContext().removeSiteRoot(content.getFile().getRootPath());
			
			List<Locale> locales = content.getLocales();

			if (locales.size() == 0) {
				locales = OpenCms.getLocaleManager().getDefaultLocales(cmsObject,masterPath);
			}

			Locale locale = OpenCms.getLocaleManager().getBestMatchingLocale(CmsLocaleManager.getLocale(""),OpenCms.getLocaleManager().getDefaultLocales(cmsObject,masterPath), locales);

			List<I_CmsXmlContentValue> linkedArticles = content.getValues(
					"linkedArticles", locale);

			CmsRelationFilter filter = CmsRelationFilter.ALL.filterType(linkedArticleType);
			List<CmsRelation> relations = null;
			try {
				relations = cmsObject.getRelationsForResource(masterPath, filter);
			} catch (CmsException ex) {
				CmsLog.getLog(this).error("Error al intentar obtener relaciones de una nota ",ex);
			}
			for (CmsRelation rel : relations){

				String relation = "";

				String rel1 = cmsObject.getRequestContext().removeSiteRoot(rel.getTargetPath());
				String rel2 = cmsObject.getRequestContext().removeSiteRoot(rel.getSourcePath());

				if (rel1.equals(masterPath))
					relation = rel2;
				else
					relation = rel1;

				Boolean isExist = false;

				for (int j = 1; j <= linkedArticles.size(); j++) {

					I_CmsXmlContentValue value = content.getValue("linkedArticles["+j+"]/path[1]", Locale.ENGLISH);

					String linkedPath = cmsObject.getRequestContext().removeSiteRoot(value.getStringValue(cmsObject));

					if(relation.equals(linkedPath)){
						isExist = true;
						j = linkedArticles.size()+1;
					}
				}

				if(!isExist && !relation.equals(masterPath)){
					deleteRelationFromcontent(relation, masterPath,cmsObject, site);
					deleteRelation(relation, masterPath, cmsObject, site);
				}
			}


			for (int j = 1; j <= linkedArticles.size(); j++) {

				I_CmsXmlContentValue valuePath = content.getValue("linkedArticles[" + j
						+ "]/path[1]", Locale.ENGLISH);
				String linkedPath = cmsObject.getRequestContext().removeSiteRoot(valuePath.getStringValue(cmsObject));

				Boolean isExist = false;

				// Me fijo si la relacion ya existe para no agregarla de nuevo
				for (CmsRelation rel : relations){
					String relation = "";

					String rel1 = cmsObject.getRequestContext().removeSiteRoot(rel.getTargetPath());
					String rel2 = cmsObject.getRequestContext().removeSiteRoot(rel.getSourcePath());

					if (rel1.equals(masterPath))
						relation = rel2;
					else
						relation = rel1;

					if(relation.equals(linkedPath)) isExist = true;
				}

				// Si no tengo en la lista de relaciones el path del contenido estructurado, lo agregamos en la otra noticia y como relacion
				if(!isExist){
					
					if(!masterPath.equals(linkedPath) && !linkedPath.equals("")){
						try {
						
							CmsResourceUtils.forceLockResource(cmsObject,masterPath);
							addRelationToContent(masterPath, linkedPath, cmsObject);
		
						} catch (CmsSecurityException ex) {
							
							CmsResourceUtils.unlockResource(cmsObject, masterPath, false);
							CmsLog.getLog(this).info("El usuario no tiene suficientes permisos. Usuario: " + 
									cmsObject.getRequestContext().currentUser().getName());
							cmsObject = CmsObjectUtils.loginAsAdmin();
							
							if (cmsObject != null) {
								cmsObject.getRequestContext().setCurrentProject(cmsObject.readProject("Offline"));
								cmsObject.getRequestContext().setSiteRoot(site.getSiteRoot());
								
								CmsResourceUtils.forceLockResource(cmsObject,masterPath);
								addRelationToContent(masterPath, linkedPath, cmsObject);
							}
						}
						
						//se asume que si no puede ejecutar la accion anterior en esta ya trabaja con el usuario
						//tfs-admin
						CmsResourceUtils.forceLockResource(cmsObject,linkedPath);
						cmsObject.addRelationToResource(masterPath, linkedPath, "linkedArticle");
						CmsResourceUtils.unlockResource(cmsObject, linkedPath, false);
							
						CmsResourceUtils.unlockResource(cmsObject,masterPath, false);
								
					}
					
				}

			}

		} catch (CmsIllegalArgumentException e) {
			CmsLog.getLog(this).error("Falta configurar el relationType linkedArticle .",e);
			
		} catch (CmsException e1) {
			CmsLog.getLog(this).error("Error al intentar linkear una nota ",e1);
		}
	}

	protected void UnLinkedArticles(CmsObject cmsObject, CmsXmlContent content, CmsSite site ){
			
			String deletedPath = cmsObject.getRequestContext().removeSiteRoot(content.getFile().getRootPath());

			CmsRelationFilter filter = CmsRelationFilter.ALL.filterType(CmsRelationType.valueOf("linkedArticle"));
			List<CmsRelation> relations = null;
			try {
				relations = cmsObject.getRelationsForResource(deletedPath, filter);
			} catch (CmsException e1) {
				CmsLog.getLog(this).error("Error al intentar obtener las relaciones de la nota ",e1);
			}
			for ( CmsRelation rel : relations) {
				String relation = "";

				String rel1 = cmsObject.getRequestContext().removeSiteRoot(rel.getTargetPath());
				String rel2 = cmsObject.getRequestContext().removeSiteRoot(rel.getSourcePath());

				if (rel1.equals(deletedPath))
					relation = rel2;
				else
					relation = rel1;

				deleteRelationFromcontent(deletedPath, relation, cmsObject, site);
				deleteRelation(deletedPath, relation, cmsObject, site);
			}
	}

	protected void addRelationToContent(String master, String slave, CmsObject cmsObject){

		try {
			CmsXmlContent content = getXmlContent(cmsObject, cmsObject.getRequestContext().removeSiteRoot(slave));

			List<Locale> locales = content.getLocales(); 

			if (locales.size() == 0) {
				locales = OpenCms.getLocaleManager().getDefaultLocales(cmsObject, slave);
			}

			Locale locale = OpenCms.getLocaleManager().getBestMatchingLocale(
					CmsLocaleManager.getLocale(""),
					OpenCms.getLocaleManager().getDefaultLocales(cmsObject,
							slave), locales);

			List<I_CmsXmlContentValue> linkedArticles = content.getValues(
					"linkedArticles", locale);
			int index = linkedArticles.size() +1;

			content.addValue(cmsObject, "linkedArticles", locale, linkedArticles.size());
			content.getValue("linkedArticles["+index+"]/path[1]", locale).setStringValue(cmsObject,master);
			
			
			CmsResourceUtils.forceLockResource(cmsObject,slave);

			CmsFile file = cmsObject.readFile(slave);
			file.setContents(content.marshal());
			cmsObject.writeFile(file);
			
			CmsResourceUtils.unlockResource(cmsObject, slave, false);
		} catch (CmsXmlException e) {
			CmsLog.getLog(cmsObject).error("Error al agregar relacion: "+e.getMessage());
		} catch (CmsException e) {
			CmsLog.getLog(cmsObject).error("Error al agregar relacion: "+e.getMessage());
		}
	}

	protected void deleteRelationFromcontent(String sourceArticle, String deletedArticle,CmsObject cmsObject, CmsSite site ){

		try{

			CmsXmlContent content = getXmlContent(cmsObject, cmsObject.getRequestContext().removeSiteRoot(sourceArticle));

			List<Locale> locales = content.getLocales();

			if (locales.size() == 0) {
				locales = OpenCms.getLocaleManager().getDefaultLocales(cmsObject,
						sourceArticle);
			}

			Locale locale = OpenCms.getLocaleManager().getBestMatchingLocale(
					CmsLocaleManager.getLocale(""),
					OpenCms.getLocaleManager().getDefaultLocales(cmsObject,
							sourceArticle), locales);

			List<I_CmsXmlContentValue> linkedArticles = content.getValues(
					"linkedArticles", locale);

			int linkedArticlesSize = linkedArticles.size();
			
			for (int j = 1; j <= linkedArticlesSize; j++) {

				I_CmsXmlContentValue value = content.getValue("linkedArticles["+j+"]/path[1]", Locale.ENGLISH);

				String linkedPath = cmsObject.getRequestContext().removeSiteRoot(value.getStringValue(cmsObject));

				if(deletedArticle.equals(linkedPath)){
					content.removeValue("linkedArticles", locale, j-1);
					linkedArticlesSize = linkedArticlesSize -1;
					j = j -1;
				}
			}
			try {
				CmsResourceUtils.forceLockResource(cmsObject, sourceArticle);

				CmsFile file = cmsObject.readFile(sourceArticle);
				file.setContents(content.marshal());
				cmsObject.writeFile(file);
				CmsResourceUtils.unlockResource(cmsObject, sourceArticle, false);
				
			} catch (CmsSecurityException ex ){
				CmsResourceUtils.unlockResource(cmsObject, sourceArticle, false);
				CmsLog.getLog(this).info("El usuario no tiene suficientes permisos. Usuario: " + 
						cmsObject.getRequestContext().currentUser().getName());
				cmsObject = CmsObjectUtils.loginAsAdmin();
				if (cmsObject != null) {
					cmsObject.getRequestContext().setCurrentProject(cmsObject.readProject("Offline"));
					cmsObject.getRequestContext().setSiteRoot(site.getSiteRoot());
					
					CmsResourceUtils.forceLockResource(cmsObject, sourceArticle);

					CmsFile file = cmsObject.readFile(sourceArticle);
					file.setContents(content.marshal());
					cmsObject.writeFile(file);
					CmsResourceUtils.unlockResource(cmsObject, sourceArticle, false);
				}	
				
			}
			

		} catch (CmsXmlException e) {
			CmsLog.getLog(cmsObject).error("Error al borrar relacion: "+e.getMessage());
		} catch (CmsException e) {
			CmsLog.getLog(cmsObject).error("Error al borrar relacion: "+e.getMessage());
		}

	}

	protected void deleteRelation(String deletedArticle, String pathArticle,CmsObject cmsObject, CmsSite site){

		try{	
			CmsRelationFilter filterSources = CmsRelationFilter.SOURCES.filterType(CmsRelationType.valueOf("linkedArticle"));

			List<CmsRelation> relationsSources = cmsObject.getRelationsForResource(pathArticle, filterSources);

			List<String> sources = new ArrayList<String>();

			for ( CmsRelation rel : relationsSources) {
				String relation = cmsObject.getRequestContext().removeSiteRoot(rel.getSourcePath());

				if(!relation.equals(deletedArticle)) sources.add(relation);
			}

			CmsRelationFilter filterTargets = CmsRelationFilter.TARGETS.filterType(CmsRelationType.valueOf("linkedArticle"));

			List<CmsRelation> relationsTargets = cmsObject.getRelationsForResource(pathArticle, filterTargets);

			List<String> targets = new ArrayList<String>();

			for ( CmsRelation rel : relationsTargets) {
				String relation = cmsObject.getRequestContext().removeSiteRoot(rel.getTargetPath());
				if(!relation.equals(deletedArticle)) targets.add(relation);
			}
			
			try {
				CmsResourceUtils.forceLockResource(cmsObject, pathArticle);
			
				CmsRelationFilter filter = CmsRelationFilter.ALL.filterType(CmsRelationType.valueOf("linkedArticle"));
				cmsObject.deleteRelationsFromResource(pathArticle, filter);
			} catch (CmsSecurityException ex) {
				CmsResourceUtils.unlockResource(cmsObject, pathArticle, false); 
				CmsLog.getLog(this).info("El usuario no tiene suficientes permisos. Usuario: " + 
						cmsObject.getRequestContext().currentUser().getName());
				
				cmsObject = CmsObjectUtils.loginAsAdmin();
				
				if (cmsObject != null) {
					cmsObject.getRequestContext().setCurrentProject(cmsObject.readProject("Offline"));
					cmsObject.getRequestContext().setSiteRoot(site.getSiteRoot());
					CmsResourceUtils.forceLockResource(cmsObject, pathArticle);
				
					CmsRelationFilter filter = CmsRelationFilter.ALL.filterType(CmsRelationType.valueOf("linkedArticle"));
					cmsObject.deleteRelationsFromResource(pathArticle, filter);
				}
			}
			for (int j = 0; j < sources.size(); j++) {
				String source = sources.get(j);

				source = cmsObject.getRequestContext().removeSiteRoot(source);

				CmsResourceUtils.forceLockResource(cmsObject, source);

				cmsObject.addRelationToResource(source, pathArticle, "linkedArticle");
			
				CmsResourceUtils.unlockResource(cmsObject, source, false);
			}

			for (int j = 0; j < targets.size(); j++) {
				String target = targets.get(j);

				target = cmsObject.getRequestContext().removeSiteRoot(target);
				CmsResourceUtils.forceLockResource(cmsObject, target);
				cmsObject.addRelationToResource(pathArticle, target, "linkedArticle");
					
				CmsResourceUtils.unlockResource(cmsObject, target, false);
			}

			CmsResourceUtils.unlockResource(cmsObject, pathArticle, false);


		} catch (CmsXmlException e) {
			CmsLog.getLog(cmsObject).error("Error al borrar relacion: "+e.getMessage());
		} catch (CmsException e) {
			CmsLog.getLog(cmsObject).error("Error al borrar relacion: "+e.getMessage());
		}	

	}

	@Override
	protected CmsXmlContent getXmlContent(CmsObject cmsObject, String url)
			throws CmsXmlException, CmsException {

		CmsFile contentFile = cmsObject.readFile(url);

		CmsXmlContent content = CmsXmlContentFactory.unmarshal(cmsObject, contentFile);

		try {	
			CmsXmlUtils.validateXmlStructure(contentFile.getContents(), new CmsXmlEntityResolver(cmsObject));
		} catch (CmsException e) {
			//	CmsResourceUtils.forceLockResource(cmsObject, cmsObject.getSitePath(contentFile));
			content.setAutoCorrectionEnabled(true);
			content.correctXmlStructure(cmsObject);
			//  CmsResourceUtils.unlockResource(cmsObject, cmsObject.getSitePath(contentFile), false);
		}
		return content;
	}


}
