package com.tfsla.diario.model;

import java.util.Locale;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsException;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

import twitter4j.Paging;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterObjectFactory;
import twitter4j.conf.ConfigurationBuilder;

import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class TfsTwitterFeed {

	private CmsObject cmsObject = null;
	private CmsResource feed = null;

	private String usuarioMuro = "";
	private String palabrasIncluidas = "";
	private String palabrasExcluidas = "";
	private String contenido = "";
	private int count = 10;
	private boolean soloImagenes = false;
	private boolean soloTexto = false;
	private boolean sinRetweets = true;
	private String nombre = "";
	private String titulo = "";
	private String descripcion = "";

	JSONArray statusesFiltered = null;

	/* Lee un contenido estructurado y arma una consulta a twitter mediante la search api */
	public TfsTwitterFeed(CmsObject cms, CmsResource feed) {

		cmsObject = cms;
		this.feed = feed;

		String _cantidad = "";
		String _sinRetweets = "";
		try {
			CmsFile m_file = cmsObject.readFile(cmsObject.getRequestContext().getSitePath(feed), CmsResourceFilter.ALL);
			CmsXmlContent m_content = CmsXmlContentFactory.unmarshal(cmsObject, m_file);

			Locale locale = cmsObject.getRequestContext().getLocale();

			_cantidad = m_content.getStringValue(cmsObject,"cantidad", locale);
			usuarioMuro = m_content.getStringValue(cmsObject,"usuarioMuro", locale);
			palabrasIncluidas = m_content.getStringValue(cmsObject,"palabrasIncluidas", locale);
			palabrasExcluidas = m_content.getStringValue(cmsObject,"palabrasExcluidas", locale);
			contenido = m_content.getStringValue(cmsObject,"contenido", locale);

			nombre = m_content.getStringValue(cmsObject,"nombre", locale);
			titulo = m_content.getStringValue(cmsObject,"titulo", locale);
			descripcion = m_content.getStringValue(cmsObject,"descripcion", locale);

			soloTexto = contenido.equals("texto");
			soloImagenes = contenido.equals("imagen");

			_sinRetweets = m_content.getStringValue(cmsObject,"sinRetweets", locale);
			sinRetweets = _sinRetweets.equals("true");
			
			try {
				count = Integer.parseInt(_cantidad);
			}
			catch (NumberFormatException ex) {
			}


		} catch (CmsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}
	
	public String getTweetsJson()
	{
		if (statusesFiltered == null) {
			statusesFiltered = new JSONArray();
			getTweets();
		}


		JSONObject jObj = new JSONObject();
		
		
		jObj.put("name", nombre);
		jObj.put("title", titulo);
		jObj.put("description", descripcion);
		jObj.put("tweets", statusesFiltered);

		return jObj.toString();
	}

	private void getTweets() {
		try {
			ConfigurationBuilder cb = new ConfigurationBuilder();
			cb.setJSONStoreEnabled(true);

			Twitter twitter = new TwitterFactory(cb.build()).getInstance();


			List<Status> statuses = null;
			long lowestTweetId = Long.MAX_VALUE;
			int currIt = 0;
			if ( usuarioMuro!=null && !usuarioMuro.trim().equals("") ) {
				Paging paging = new Paging();	

				do {
					if (soloTexto || soloImagenes)
						paging.setCount(100);
					else
						paging.setCount(count);

					statuses = twitter.getUserTimeline(usuarioMuro,paging);

					for (Status tweet : statuses){
						
						if (!tweet.isRetweet() || !sinRetweets)
						{						
							int mediaCount = tweet.getMediaEntities().length;
							if(soloImagenes && mediaCount>0)
								statusesFiltered.add(JSONObject.fromObject(TwitterObjectFactory.getRawJSON(tweet)));
							else if (soloTexto && mediaCount==0)
								statusesFiltered.add(JSONObject.fromObject(TwitterObjectFactory.getRawJSON(tweet)));
							else if (!soloTexto && !soloImagenes)
								statusesFiltered.add(JSONObject.fromObject(TwitterObjectFactory.getRawJSON(tweet)));
						}
						//Do anything you want with image
						//tweet.getMediaEntities()[0].getMediaURL() is url of image in a tweet.
						if (tweet.getId() < lowestTweetId) {
							lowestTweetId = tweet.getId();
						}
						if (statusesFiltered.size()==count)
							break;
					}
					currIt ++;

					paging.setMaxId(lowestTweetId);

				}
				while (statusesFiltered.size()<count && currIt <10);

			}
			else {
				String q = palabrasIncluidas;
				if (palabrasExcluidas!=null && !palabrasExcluidas.trim().equals("")) {
					String[] excl = palabrasExcluidas.split(" ");
					for (String word : excl) {
						if (!word.trim().equals(""))
							q += " -" + word.trim();
					}
				}

				if (sinRetweets)
					q += " +exclude:retweets";
				
				Query query = new Query(q);
				do {
					if (soloTexto || soloImagenes)
						query.setCount(100);
					else
						query.setCount(count);
					QueryResult result = twitter.search(query);
					statuses = result.getTweets();


					for(twitter4j.Status tweet : statuses){
						int mediaCount = tweet.getMediaEntities().length;
						if(soloImagenes && mediaCount>0)
							statusesFiltered.add(JSONObject.fromObject(TwitterObjectFactory.getRawJSON(tweet)));
						else if (soloTexto && mediaCount==0)
							statusesFiltered.add(JSONObject.fromObject(TwitterObjectFactory.getRawJSON(tweet)));
						else if (!soloTexto && !soloImagenes)
							statusesFiltered.add(JSONObject.fromObject(TwitterObjectFactory.getRawJSON(tweet)));
						//Do anything you want with image
						//tweet.getMediaEntities()[0].getMediaURL() is url of image in a tweet.
						if (tweet.getId() < lowestTweetId) {
							lowestTweetId = tweet.getId();
						}
						if (statusesFiltered.size()==count)
							break;
					}
					currIt ++;
					query.setMaxId(lowestTweetId);

				}
				while (statusesFiltered.size()<count && currIt <10);
			}



		} catch (TwitterException te) {
			te.printStackTrace();
		}

	}

	public String getRootPath() {
		return feed.getRootPath();
	}

	public String getUsuarioMuro() {
		return usuarioMuro;
	}

	public void setUsuarioMuro(String usuarioMuro) {
		this.usuarioMuro = usuarioMuro;
	}

	public String getPalabrasIncluidas() {
		return palabrasIncluidas;
	}

	public void setPalabrasIncluidas(String palabrasIncluidas) {
		this.palabrasIncluidas = palabrasIncluidas;
	}

	public String getPalabrasExcluidas() {
		return palabrasExcluidas;
	}

	public void setPalabrasExcluidas(String palabrasExcluidas) {
		this.palabrasExcluidas = palabrasExcluidas;
	}

	public String getContenido() {
		return contenido;
	}

	public void setContenido(String contenido) {
		this.contenido = contenido;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public boolean isSoloImagenes() {
		return soloImagenes;
	}

	public void setSoloImagenes(boolean soloImagenes) {
		this.soloImagenes = soloImagenes;
	}

	public boolean isSoloTexto() {
		return soloTexto;
	}

	public void setSoloTexto(boolean soloTexto) {
		this.soloTexto = soloTexto;
	}

}
