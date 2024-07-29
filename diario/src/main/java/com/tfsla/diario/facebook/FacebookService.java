package com.tfsla.diario.facebook;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import org.apache.commons.logging.Log;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import com.tfsla.diario.ediciones.model.Seccion;
import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.PublicationService;
import com.tfsla.diario.ediciones.services.SeccionesService;
import com.tfsla.diario.ediciones.services.TipoEdicionService;


public class FacebookService {

	private static FacebookService instance = new FacebookService();
	private static final Log LOG = CmsLog.getLog(FacebookService.class);

	FacebookAccountConsumer accountConsumer = null;
	List<FacebookAccountPublisher> publishers = null;

	public static FacebookService getInstance() {
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

	public List<FacebookAccountPublisher> getPublishers(CmsObject cms) {
		if (publishers==null) {
			FacebookPublisherReader pReader = new FacebookPublisherReader(cms);
			publishers = pReader.getPublishersData();
		
		}
		return publishers;
	}

	public void addPublishers(CmsObject cms,FacebookAccountPublisher publisher) {
		FacebookPublisherReader pReader = new FacebookPublisherReader(cms);
		pReader.addPublishersData(publisher);
		publishers.add(publisher);
	}

	public void updatePublishers(CmsObject cms,FacebookAccountPublisher publisher) {
		FacebookPublisherReader pReader = new FacebookPublisherReader(cms);
		pReader.updatePublishersData(publisher);
		publishers.remove(publisher);
		publishers.add(publisher);
	}

	public void removePublishers(CmsObject cms,FacebookAccountPublisher publisher) {
		FacebookPublisherReader pReader = new FacebookPublisherReader(cms);
		pReader.removePublishersData(publisher);
		publishers.remove(publisher);
	}

	public FacebookAccountPublisher getPublisher(CmsObject cms, String facebookAccount) {
		List<FacebookAccountPublisher> publishers = getPublishers(cms);
		FacebookAccountPublisher publisher = new FacebookAccountPublisher();
		publisher.setName(facebookAccount);
		
		int idx = publishers.indexOf(publisher);
		if (idx!=-1)
			return publishers.get(idx);

		return null;
	}

	public static String getApiVersion(CmsObject cms) {
		try {
			CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
			String siteName = cms.getRequestContext().getSiteRoot();
			String publication = String.valueOf(PublicationService.getPublicationId(cms));
			String version = config.getParam(siteName, publication, "webusers-facebook", "apiVersion");
			if(version != null && !version.equals("")) {
				return "v" + version;
			}
			return "v2.2";
		} catch(Exception e) {
			e.printStackTrace();
			return "v2.2";
		}
	}
	
	public void publishNews(CmsResource res, CmsObject cms) throws Exception {
		
		throw new Exception("Deprecated");
		
		/*
		CmsProperty fbID = cms.readPropertyObject(res, "fbID", false);
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
				
				if (seccion.getFacebookAccount()!=null &&  !seccion.getFacebookAccount().equals("")) {
					String prefix = OpenCms.getSiteManager().getCurrentSite(cms).getServerPrefix(cms,res);

					if (prefix.matches("^http:.*:[0-9]*"))
						prefix = prefix.substring(0,prefix.lastIndexOf(":"));
					String path = prefix + cms.getRequestContext().removeSiteRoot(res.getRootPath());
					
					FacebookAccountPublisher accountPub = getPublisher(cms,seccion.getFacebookAccount());
					//FacebookAccountConsumer accountCons = getAccountConsumer(cms);
					
					String remoteUrl = String.format("https://graph.facebook.com/%s/me/feed", getApiVersion(cms));

			        URL url = new URL(remoteUrl);
			        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

			        connection.setRequestMethod("POST");
			        connection.setDoOutput(true);
			        connection.setDoInput(true);

			        PrintWriter out = new PrintWriter(connection.getOutputStream());
			        StringBuffer sb = new StringBuffer();

			        sb.append("access_token=");
			        sb.append(accountPub.getKey());
			        sb.append("&message=");
			        sb.append(titulo);
			        sb.append("&link=");
			        sb.append(path);
			        sb.append("&name=");
			        sb.append(titulo);

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

					cms.lockResource(cms.getRequestContext().removeSiteRoot(res.getRootPath()));
					cms.writePropertyObject(cms.getRequestContext().removeSiteRoot(res.getRootPath()), new CmsProperty("fbID","published", null));
					cms.unlockResource(cms.getRequestContext().removeSiteRoot(res.getRootPath()));
				}
			} catch (Exception e) {
				LOG.error(e);
				e.printStackTrace();
			}
		}
		*/
	}
}
