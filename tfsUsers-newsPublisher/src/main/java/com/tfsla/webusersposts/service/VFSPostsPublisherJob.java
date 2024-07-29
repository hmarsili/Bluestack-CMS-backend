package com.tfsla.webusersposts.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.opencms.db.CmsResourceState;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.scheduler.I_CmsScheduledJob;

import com.tfsla.event.I_TfsEventListener;
import com.tfsla.webusersposts.common.*;
import com.tfsla.webusersposts.dataaccess.PostsDAO;

/**
 * A job to process posts imported into the VFS but not yet published 
 */
public class VFSPostsPublisherJob implements I_CmsScheduledJob {

	@SuppressWarnings("rawtypes")
	@Override
	public String launch(CmsObject cms, Map parameters) throws Exception {
		String publication = (String)parameters.get("publication");
		String vfsPath = (String)parameters.get("vfsPath");
		int batchSize = Integer.parseInt((String) parameters.get("batchSize"));
		int maxPostNumber = 20520;
		if (parameters.containsKey("maxPostNumber")) {
			maxPostNumber = Integer.parseInt((String) parameters.get("maxPostNumber"));
		}
		LOG.debug("Procesando " + maxPostNumber + " posts en " + vfsPath);
		
		try {
			List<CmsResource> publishList = null;
			int counter = 1;
			
			while (counter <= maxPostNumber) {
				String postName = "";
				try {
					publishList = new ArrayList<CmsResource>();
					for (int i=0; i<batchSize; i++) {
						try {
							postName = vfsPath + String.format("noticia_%04d.html", counter);
							LOG.debug("Procesando post " + postName + " (" + counter + "/" + maxPostNumber + ")");
							if (!cms.existsResource(postName, CmsResourceFilter.IGNORE_EXPIRATION)) {
								LOG.debug("El post " + postName + " no existe");
								continue;
							}
							CmsProperty cmsProperty = cms.readPropertyObject(postName, "newsType", false);
							if (cmsProperty == null || !cmsProperty.getValue().equals("post")) {
								LOG.debug(postName + " no es un post");
								continue;
							}
							CmsResource resource = cms.readResource(postName);
							if (resource.getState() == CmsResourceState.STATE_NEW || resource.getState() == CmsResourceState.STATE_CHANGED) {
								LOG.debug(postName + " para publicar");
								publishList.add(resource);
							}
						} catch (Exception e) {
							LOG.debug("Error procesando post " + postName + " - " + e.getMessage());
							LOG.error("Error procesando post " + postName, e);
						} finally {
							counter++;
						}
					}
					
					if (publishList.size() > 0) {
						PostsDAO dao = new PostsDAO();
						try {
							dao.openConnection();
							for (CmsResource item : publishList) {
								try {
									LOG.debug("Publicando post " + item.getRootPath());
									this.unlockFile(cms, item, item.getRootPath());
									OpenCms.getPublishManager().publishResource(cms, item.getRootPath());
									UserPost userPost = dao.getPostByUrlAndPublication(item.getRootPath(), publication);
									if (userPost != null) {
										dao.deleteDraft(userPost.getId());
									}
									this.sendNotifications(item, cms, I_TfsEventListener.EVENT_POST_ACEPTED);
								} catch (Exception e) {
									LOG.error("Error publicando post " + item.getRootPath(), e);
								}
							}
							dao.closeConnection();
							OpenCms.getPublishManager().waitWhileRunning();
						} catch (Exception e) {
							LOG.error("Error procesando lote para publicación, counter: " + counter, e);
						} finally {
							dao.closeConnection();
						}
					}
					
					LOG.debug("Fin de batch de publicación");
				} catch (Exception e) {
					LOG.error("Error procesando post en lote - " + postName, e);
				}
			}
			
			return "Job finalizado, posts procesados: " + maxPostNumber;
		} catch (Exception e) {
			LOG.error("Error procesando posts importados - " + e.getMessage(), e);
			return "Error procesando posts importados - " + e.getMessage();
		}
	}
	
	private void unlockFile(CmsObject cms, CmsResource file, String path) throws Exception {
		try {
			CmsLock lock = cms.getLock(file);
			if (!lock.isUnlocked()) {
				cms.changeLock(path);
				cms.unlockResource(path);
			}
			cms.lockResource(path);
		} catch (Exception e) {
			LOG.error("Error lockeando post " + path, e);
		}
	}
	
	private void sendNotifications(CmsResource post, CmsObject cms, int eventListener) throws Exception {
		HashMap<String, Object> eventData = new HashMap<String, Object>();
		eventData.put(I_TfsEventListener.KEY_RESOURCE, post);
		eventData.put(I_TfsEventListener.KEY_USER_NAME, cms.readUser(post.getUserCreated()).getName());
		OpenCms.fireCmsEvent(new CmsEvent(eventListener, eventData));
	}
	
	private Log LOG = CmsLog.getLog(this);
}
