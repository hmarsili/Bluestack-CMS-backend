package com.tfsla.diario.twitter;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.file.CmsResource;

import com.tfsla.diario.ediciones.model.Seccion;
import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.SeccionesService;
import com.tfsla.diario.ediciones.services.TipoEdicionService;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.Status;
import twitter4j.auth.AccessToken;

import java.util.List;

@Deprecated
public class TwitterService {

	private static final Log LOG = CmsLog.getLog(TwitterService.class);
	   
	TwitterAccountConsumer accountConsumer = null;
	List<TwitterAccountPublisher> publishers = null;
	
	private TwitterService(){
    	//PropertiesProvider properties = new PropertiesProvider(this.getClass(), "twitterConfig.properties");

    	//twitterPassword = properties.get("twitterPassword");
    	//twitterID = properties.get("twitterID");

		
	}
	
	private static TwitterService instance = new TwitterService();
	
	//private String twitterPassword = "";
	//private String twitterID="";

	/**
	 * Publica una noticia en Twitter, si no fue publicada antes.
	 * @param res (CmsResource) - Noticia a publicar 
	 * @param cms (CmsObject)
	 * @throws Exception 
	 */
	public void publishNews(CmsResource res, CmsObject cms) throws Exception
	{

		throw new Exception("Deprecated");
		
		/*
		CmsProperty twitIDProp = cms.readPropertyObject(res, "twitID", false);
		if (twitIDProp.getValue()==null || twitIDProp.getValue().isEmpty()) {

			CmsProperty titleProp = cms.readPropertyObject(res, "Title", false);
			String titulo = titleProp.getValue();

			CmsProperty sectionProp = cms.readPropertyObject(res, "seccion", false);
			String sectionName = sectionProp.getValue();

			String prefix = OpenCms.getSiteManager().getCurrentSite(cms).getServerPrefix(cms,res);

			if (prefix.matches("^http:.*:[0-9]*"))
				prefix = prefix.substring(0,prefix.lastIndexOf(":"));
			String path = prefix + cms.getRequestContext().removeSiteRoot(res.getRootPath());

			String shortPath = BitlyService.getInstance(cms).getShortenUrl(path);

			Twitter twitter = null;

			try {
				TipoEdicion tEdicion = null;			
				TipoEdicionService tService = new TipoEdicionService();
				tEdicion = tService.obtenerTipoEdicion(cms, cms.getRequestContext().removeSiteRoot(res.getRootPath()));

				SeccionesService sService = new SeccionesService();

				Seccion seccion = sService.obtenerSeccion(sectionName,tEdicion.getId());
				if (seccion.getTwitterAccount()!=null &&  !seccion.getTwitterAccount().equals(""))
				{
					TwitterAccountPublisher accountPub = getPublisher(cms,seccion.getTwitterAccount());
					TwitterAccountConsumer accountCons = getAccountConsumer(cms);
					
					AccessToken accessToken = new AccessToken(accountPub.getKey(), accountPub.getSecret());
					
					twitter = new TwitterFactory().getInstance();
					twitter.setOAuthConsumer(accountCons.getKey(), accountCons.getSecret());
					twitter.setOAuthAccessToken(accessToken);

					Status stat = twitter.updateStatus(titulo + " " + shortPath);
					long statId = stat.getId();
					cms.lockResource(cms.getRequestContext().removeSiteRoot(res.getRootPath()));
					cms.writePropertyObject(cms.getRequestContext().removeSiteRoot(res.getRootPath()), new CmsProperty("twitID","" + statId, null));
					cms.unlockResource(cms.getRequestContext().removeSiteRoot(res.getRootPath()));

				}
				

			} catch (Exception e) {
				LOG.error(e);
				throw new TwitterException(e);

			}

		}
		*/
	}

	/**
	 * Obtiene una cuenta Publisher de Twitter en base a su nombre
	 * @param cms 
	 * @param twitterAccount (String)
	 * @return TwitterAccountPublisher
	 */	
	public TwitterAccountPublisher getPublisher(CmsObject cms, String twitterAccount) {
		List<TwitterAccountPublisher> publishers = getPublishers(cms);
		TwitterAccountPublisher publisher = new TwitterAccountPublisher();
		publisher.setName(twitterAccount);
		
		
		int idx = publishers.indexOf(publisher);
		if (idx!=-1)
			return publishers.get(idx);

		return null;
	}

	public static TwitterService getInstance() {
		return instance;
	}

	
	
	public TwitterAccountConsumer getAccountConsumer(CmsObject cms) {
		if (accountConsumer == null) 
		{
			TwitterConsumerReader rReader = new TwitterConsumerReader(cms);
			accountConsumer = rReader.getConsumerData();
		}
		return accountConsumer;
	}

	public void setAccountConsumer(CmsObject cms, TwitterAccountConsumer accountConsumer) {
		this.accountConsumer = accountConsumer;
		
		TwitterConsumerReader rReader = new TwitterConsumerReader(cms);
		rReader.setConsumerData(accountConsumer);

	}

	public List<TwitterAccountPublisher> getPublishers(CmsObject cms) {
		if (publishers==null)
		{
			TwitterPublisherReader pReader = new TwitterPublisherReader(cms);
			publishers = pReader.getPublishersData();
		
		}
		return publishers;
	}

	/**
	 * Agrega una cuenta de publisher de Twitter
	 * @param cms
	 * @param publisher (TwitterAccountPublisher)
	 */
	public void addPublishers(CmsObject cms,TwitterAccountPublisher publisher) {
		TwitterPublisherReader pReader = new TwitterPublisherReader(cms);
		pReader.addPublishersData(publisher);
		publishers.add(publisher);
	}

	/**
	 * Actualiza una cuenta de publisher de Twitter
	 * @param cms
	 * @param publisher (TwitterAccountPublisher)
	 */
	public void updatePublishers(CmsObject cms,TwitterAccountPublisher publisher) {
		TwitterPublisherReader pReader = new TwitterPublisherReader(cms);
		pReader.updatePublishersData(publisher);
		publishers.remove(publisher);
		publishers.add(publisher);
	}

	/**
	 * Borra una cuenta de publisher de Twitter
	 * @param cms
	 * @param publisher (TwitterAccountPublisher)
	 */
	public void removePublishers(CmsObject cms,TwitterAccountPublisher publisher) {
		TwitterPublisherReader pReader = new TwitterPublisherReader(cms);
		pReader.removePublishersData(publisher);
		publishers.remove(publisher);
	}

}
