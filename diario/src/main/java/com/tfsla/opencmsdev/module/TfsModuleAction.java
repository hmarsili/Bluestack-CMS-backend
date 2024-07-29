package com.tfsla.opencmsdev.module;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.opencms.configuration.CmsConfigurationManager;
import org.opencms.configuration.CmsMediosInit;
import org.opencms.db.CmsPublishList;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.module.CmsModule;
import org.opencms.module.I_CmsModuleAction;
import org.opencms.report.I_CmsReport;

import com.tfsla.cdnIntegration.service.PurgeCacheCdnListener;
import com.tfsla.diario.ediciones.services.WebhookServices;
import com.tfsla.diario.facebook.FacebookPublisher;
import com.tfsla.diario.multiselect.AudioDescriptionLoader;
import com.tfsla.diario.multiselect.ImageDescriptionLoader;
import com.tfsla.diario.multiselect.VideoCodeLoader;
import com.tfsla.diario.multiselect.VideoDescriptionLoader;
import com.tfsla.diario.multiselect.VideoVodDescriptionLoader;
import com.tfsla.diario.multiselect.VideoVodsCodeLoader;
import com.tfsla.diario.productivityPlans.ProductivityPlansListener;
import com.tfsla.diario.twitter.TwitterPublisher;
import com.tfsla.opencms.search.documents.CapituloContentExtractor;
import com.tfsla.opencms.search.documents.CmsDocumentXmlContentTFS;
import com.tfsla.opencms.search.documents.EncuestaContentExtractor;
import com.tfsla.opencms.search.documents.EventoContentExtractor;
import com.tfsla.opencms.search.documents.NoticiacontentExtrator;
import com.tfsla.opencms.search.documents.PeliculaContentExtractor;
import com.tfsla.opencms.search.documents.PlaylistContentExtractor;
import com.tfsla.opencms.search.documents.RecipeContentExtractor;
import com.tfsla.opencms.search.documents.SerieContentExtractor;
import com.tfsla.opencms.search.documents.TemporadaContentExtractor;
import com.tfsla.opencms.search.documents.TriviaContentExtractor;
import com.tfsla.opencmsdev.LinkedArticleListener;
import com.tfsla.opencmsdev.NewsOnPublishEvents;
import com.tfsla.opencmsdev.RecipeOnPublishEvent;
import com.tfsla.opencmsdev.TriviaOnPublishEvent;
import com.tfsla.opencmsdev.VodOnPublishEvent;
import com.tfsla.opencmsdev.VodPublishExpirationChangeEvent;
import com.tfsla.rankViews.service.RankService;

public class TfsModuleAction implements I_CmsModuleAction {

	private static final Log LOG = CmsLog.getLog(TfsModuleAction.class);
	
	private Map<Integer, List<I_CmsEventListener>> listeners = new HashMap<Integer, List<I_CmsEventListener>>();
	//private CmsObject cmsObject;

	public TfsModuleAction() {
		super();
	}

	public void initialize(CmsObject adminCms, CmsConfigurationManager configurationManager, CmsModule module) {
	
		//se agrega para varios tipos de eventos
		OpenCms.addCmsEventListener(new LinkedArticleListener());
		
		CmsDocumentXmlContentTFS.addExtractor(new NoticiacontentExtrator());
		CmsDocumentXmlContentTFS.addExtractor(new EncuestaContentExtractor());
		CmsDocumentXmlContentTFS.addExtractor(new EventoContentExtractor());
		CmsDocumentXmlContentTFS.addExtractor(new PeliculaContentExtractor());
		CmsDocumentXmlContentTFS.addExtractor(new SerieContentExtractor());
		CmsDocumentXmlContentTFS.addExtractor(new TemporadaContentExtractor());
		CmsDocumentXmlContentTFS.addExtractor(new CapituloContentExtractor());
		CmsDocumentXmlContentTFS.addExtractor(new PlaylistContentExtractor());
		CmsDocumentXmlContentTFS.addExtractor(new RecipeContentExtractor());
		CmsDocumentXmlContentTFS.addExtractor(new TriviaContentExtractor());
		
		OpenCms.addCmsEventListener(new WebhookServices());
		
		int [] eventBeforeResourceDeleted = {I_CmsEventListener.EVENT_BEFORE_RESOURCE_DELETED};
		OpenCms.addCmsEventListener(new ImageDescriptionLoader(), eventBeforeResourceDeleted);
		OpenCms.addCmsEventListener(new VideoDescriptionLoader(), eventBeforeResourceDeleted);
		OpenCms.addCmsEventListener(new AudioDescriptionLoader(), eventBeforeResourceDeleted);
		OpenCms.addCmsEventListener(new VideoCodeLoader(), eventBeforeResourceDeleted);
		OpenCms.addCmsEventListener(new VideoVodDescriptionLoader(),eventBeforeResourceDeleted);
		OpenCms.addCmsEventListener(new VideoVodsCodeLoader(),eventBeforeResourceDeleted);
		
		
		int [] eventBeforePublish = {I_CmsEventListener.EVENT_BEFORE_PUBLISH_PROJECT};
		OpenCms.addCmsEventListener(new NewsOnPublishEvents(), eventBeforePublish);
		OpenCms.addCmsEventListener(new PurgeCacheCdnListener(), eventBeforePublish);
		//OpenCms.addCmsEventListener(new TwitterPublisher(), eventBeforePublish);
		//OpenCms.addCmsEventListener(new FacebookPublisher(), eventBeforePublish);
		OpenCms.addCmsEventListener(new VodOnPublishEvent(), eventBeforePublish);
		OpenCms.addCmsEventListener(new RecipeOnPublishEvent(), eventBeforePublish);
		OpenCms.addCmsEventListener(new VodPublishExpirationChangeEvent(), eventBeforePublish);
		OpenCms.addCmsEventListener(new TriviaOnPublishEvent(), eventBeforePublish);
		
		int [] eventPublish = {I_CmsEventListener.EVENT_PUBLISH_PROJECT};
		OpenCms.addCmsEventListener(new ProductivityPlansListener(), eventPublish);
		
		int [] eventResoureDeleted = {I_CmsEventListener.EVENT_RESOURCE_DELETED};
		OpenCms.addCmsEventListener(new UnlockResourceEventListener(), eventResoureDeleted);
	

		CmsMediosInit.getInstance().addService(new RankService());
	}

	protected synchronized void addListener(Integer cmsEventType, I_CmsEventListener listener) {
		List<I_CmsEventListener> list = this.listeners.get(cmsEventType);
		if (list == null) {
			list = new ArrayList<I_CmsEventListener>();
			this.listeners.put(cmsEventType, list);
		}
		list.add(listener);
	}

	public void moduleUninstall(CmsModule module) {

	}

	public void moduleUpdate(CmsModule module) {

	}

	public void publishProject(CmsObject cms, CmsPublishList publishList, int backupTagId, I_CmsReport report) {

	}

	public void shutDown(CmsModule module) {

	}

	public void cmsEvent(CmsEvent event) {
		LOG.debug("evento " + event.getType() + " params: " + event.getData());
		List<I_CmsEventListener> list = this.listeners.get(event.getTypeInteger());
		if (list != null) {
			for (I_CmsEventListener listener : list) {
				LOG.debug("Listener " + listener + " respondiendo al evento " + event.getType());
				listener.cmsEvent(event);
			}
		}
/*
		if (event.getType() == I_CmsEventListener.EVENT_BEFORE_PUBLISH_PROJECT) {
			List<CmsResource> o_eventResList = ((CmsPublishList) event.getData().get("publishList")).getFileList();

			for (Iterator iter = o_eventResList.iterator(); iter.hasNext();) {
				CmsResource o_eventRes = (CmsResource) iter.next();
				if (new IsNoticiaPredicate(cmsObject.getRequestContext()).evaluate(o_eventRes)) {
					getLogger().debug("Publicando noticia " + o_eventRes.getRootPath());
					if (!isPost(cmsObject, o_eventRes)) {
						getLogger().debug("... no es post deberia publicarse");
						
						if (TfsContext.getInstance().getCmsObject() != null) {
							NoticiaStateEngine.getInstance().execute(
								TfsConstants.ACTION_PUBLICAR_NOTA,
								new WorkFlowContext(o_eventRes, TfsContext.getInstance().getCmsObject())
							);
						} else {

							try {
								CmsObject contextCms = OpenCms.initCmsObject(cmsObject);
								contextCms.getRequestContext().setCurrentProject(contextCms.readProject("Offline"));

								CmsLock lockFile = contextCms.getLock(o_eventRes);

								if (!lockFile.isUnlocked()) {
									CmsProject lockInProject = lockFile.getProject();
									contextCms.getRequestContext().setCurrentProject(lockInProject);
								}

								NoticiaStateEngine.getInstance().execute(
									TfsConstants.ACTION_PUBLICAR_NOTA,
									new WorkFlowContext(o_eventRes, contextCms)
								);

							} catch (CmsException e) {
								e.printStackTrace();
							}
						}
					}
					
				}
			}
			
		}
*/
		}
	
	public static boolean isPost(CmsObject cms, CmsResource resource) {
		boolean isPost = false;
		try {
			CmsProperty prop = cms.readPropertyObject(resource, "newsType", false);
			isPost = "post".equals(prop.getValue(""));
		} catch (CmsException e) {
			e.printStackTrace();
		}

		return isPost;
	}


}