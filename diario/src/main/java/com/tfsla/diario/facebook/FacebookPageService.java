package com.tfsla.diario.facebook;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;


import com.tfsla.diario.ediciones.model.Seccion;
import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.SeccionesService;
import com.tfsla.diario.ediciones.services.TipoEdicionService;


@Deprecated
public class FacebookPageService {

	private static final Log LOG = CmsLog.getLog(FacebookService.class);
	private static FacebookPageService instance = new FacebookPageService();
	FacebookAccountConsumer accountConsumer = null;
	List<FacebookPageAccountPublisher> publishers = null;

	public static FacebookPageService getInstance() {
		return instance;
	}

	public void setAccountConsumer(CmsObject cms, FacebookAccountConsumer accountConsumer) {
		this.accountConsumer = accountConsumer;
		FacebookConsumerReader rReader = new FacebookConsumerReader(cms);
		rReader.setConsumerData(accountConsumer);
	}

	public FacebookAccountConsumer getAccountConsumer(CmsObject cms) {
		if (accountConsumer == null) {
			FacebookConsumerReader rReader = new FacebookConsumerReader(cms);
			accountConsumer = rReader.getConsumerData();
		}
		return accountConsumer;
	}

	public List<FacebookPageAccountPublisher> getPagePublishers(CmsObject cms) {
		if (publishers==null) {
			FacebookPagePublisherReader pReader = new FacebookPagePublisherReader(cms);
			publishers = pReader.getPublishersPageData();
		}
		return publishers;
	}
	
	public String getPublishersPageId(CmsObject cms, String PageAccountName) {
		if (PageAccountName!=null) {
			FacebookPagePublisherReader pReader = new FacebookPagePublisherReader(cms);
			String PageId = pReader.getPublishersPageId(PageAccountName);
			return PageId;
		}
		return null;
	}
	
	public String getPublishersKey(CmsObject cms, String PageAccountName) {
		if (PageAccountName!=null) {
			FacebookPagePublisherReader pReader = new FacebookPagePublisherReader(cms);
			String KeyPage = pReader.getPublishersKey(PageAccountName);
			return KeyPage;
		}
		return null;
	}

	public void addPagePublishers(CmsObject cms,FacebookPageAccountPublisher publisher) {
		FacebookPagePublisherReader pReader = new FacebookPagePublisherReader(cms);
		pReader.addPublishersPageData(publisher);
		publishers.add(publisher);
	}

	public void updatePagePublishers(CmsObject cms,FacebookPageAccountPublisher publisher) {
		FacebookPagePublisherReader pReader = new FacebookPagePublisherReader(cms);
		pReader.updatePublishersPageData(publisher);
		publishers.remove(publisher);
		publishers.add(publisher);
	}

	public void removePagePublishers(CmsObject cms,FacebookPageAccountPublisher publisher) {
		FacebookPagePublisherReader pReader = new FacebookPagePublisherReader(cms);
		pReader.removePublishersPageData(publisher);
		publishers.remove(publisher);
	}

	public FacebookPageAccountPublisher getPagePublisher(CmsObject cms, String facebookAccount) {
		List<FacebookPageAccountPublisher> publishers = getPagePublishers(cms);
		FacebookPageAccountPublisher publisher = new FacebookPageAccountPublisher();
		publisher.setName(facebookAccount);
		
		Iterator iter = publishers.iterator();
		while (iter.hasNext()){
			FacebookPageAccountPublisher pub = (FacebookPageAccountPublisher)iter.next();
			if(pub.getName().equals(facebookAccount)){
				return publisher;
			}
		}
		
		//int idx = publishers.indexOf(publisher);
		//if (idx!=-1)
		//	return publishers.get(idx);

		return null;
	}

	public void publishPageNews(CmsResource res, CmsObject cms) throws Exception {
		
		throw new Exception("Deprecated");
		
		/*
		CmsProperty fbID = cms.readPropertyObject(res, "fbPageID", false);
		if (fbID.getValue()==null || fbID.getValue().isEmpty()) {
			
			CmsProperty titleProp = cms.readPropertyObject(res, "Title", false);
			String titulo = titleProp.getValue();

			CmsProperty sectionProp = cms.readPropertyObject(res, "seccion", false);
			String sectionName = sectionProp.getValue();

			try {
				TipoEdicion tEdicion = null;			
				TipoEdicionService tService = new TipoEdicionService();
				tEdicion = tService.obtenerTipoEdicion(cms, cms.getRequestContext().removeSiteRoot(res.getRootPath()));
				SeccionesService sService = new SeccionesService();

				Seccion seccion = sService.obtenerSeccion(sectionName,tEdicion.getId());
				if (seccion.getFacebookPageAccount()!=null &&  !seccion.getFacebookPageAccount().equals("")) {
					String prefix = OpenCms.getSiteManager().getCurrentSite(cms).getServerPrefix(cms,res);

					if (prefix.matches("^http:.*:[0-9]*"))
						prefix = prefix.substring(0,prefix.lastIndexOf(":"));
					String path = prefix + cms.getRequestContext().removeSiteRoot(res.getRootPath());
					FacebookPageAccountPublisher accountPub = getPagePublisher(cms,seccion.getFacebookPageAccount());
					String pageId = getPublishersPageId(cms, accountPub.getName());
					String accessToken = getPublishersKey(cms, accountPub.getName());
					String ID = pageId;
					
					if(pageId.equals("")) {
						ID = "me";
					}
					
					String remoteUrl = String.format("https://graph.facebook.com/%s/%s/feed", FacebookService.getApiVersion(cms), ID);
					CmsLog.getLog(this).info("Facebook - Publicacion en la fan page: "+remoteUrl);
					
			        URL url = new URL(remoteUrl);
			        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

			        connection.setRequestMethod("POST");
			        connection.setDoOutput(true);
			        connection.setDoInput(true);

			        PrintWriter out = new PrintWriter(connection.getOutputStream());
			        StringBuffer sb = new StringBuffer();
			        sb.append("access_token=");
			        sb.append(accessToken);
			        sb.append("&message=");
			        sb.append(titulo);
			        sb.append("&link=");
			        sb.append(path);
			        sb.append("&name=");
			        sb.append(titulo);
			        
			        CmsLog.getLog(this).info("Facebook - Publicacion en fan page String: "+sb.toString());

			        // Send the message
			        out.print(sb.toString());
			        out.close();

			        // Get back the response
			        StringBuffer respSb = new StringBuffer();
			        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			        String line = in.readLine();

			        while (line != null) {
			            respSb.append(line);
			            line = in.readLine();
			        }

			        in.close();
			        CmsLog.getLog(this).info("Facebook - Publicacion en la fan page respuesta: "+line);
					cms.lockResource(cms.getRequestContext().removeSiteRoot(res.getRootPath()));
					cms.writePropertyObject(cms.getRequestContext().removeSiteRoot(res.getRootPath()), new CmsProperty("fbID","published", null));
					cms.unlockResource(cms.getRequestContext().removeSiteRoot(res.getRootPath()));
				}
			} catch (Exception e) {
				e.printStackTrace();
				LOG.error(e);
			}
		}
		
		*/
	}
}
