package com.tfsla.diario.twitterFeedCollector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.logging.Log;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.search.CmsSearchResult;
import org.opencms.search.TfsAdvancedSearch;

import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.TipoEdicionService;
import com.tfsla.diario.ediciones.services.openCmsService;
import com.tfsla.diario.friendlyTags.TfsTwitterFeedListTag;
import com.tfsla.diario.newsCollector.A_Collector;

public class LuceneTwitterFeedCollector extends A_Collector {

    protected static final Log LOG = CmsLog.getLog(LuceneTwitterFeedCollector.class);
 	
	final static String OnlineSerarchIndexName = "TWITTERFEEDS_ONLINE";
	final static String OfflineSerarchIndexName = "TWITTERFEEDS_OFFLINE";

	final static String OR_OPERATOR = " OR ";
	final static String AND_OPERATOR = " AND ";
	
	String serarchIndexName = OnlineSerarchIndexName;

	public List<CmsResource> collectFeeds(Map<String, Object> parameters, CmsObject cms) {
		
		List<CmsResource> feeds = new ArrayList<CmsResource>();
		
		int size = 1;
		if (parameters.get("size")!=null)
			size = (Integer)parameters.get(TfsTwitterFeedListTag.param_size);

		int page = 1;
		
		String[] nombre = getValues((String)parameters.get(TfsTwitterFeedListTag.param_name));
		String[] tags = getValues((String)parameters.get(TfsTwitterFeedListTag.param_tags));
		String[] categorias = getValues((String)parameters.get(TfsTwitterFeedListTag.param_categories));

		String searchIndex = (String)parameters.get(TfsTwitterFeedListTag.param_searchIndex);

		String publication = (String)parameters.get(TfsTwitterFeedListTag.param_publication);
		
		TipoEdicion tEdicion = getTipoEdicion(cms, publication);
		
		if (searchIndex!=null)
			serarchIndexName=searchIndex;
		else 
		{
			if (cms.getRequestContext().currentProject().isOnlineProject())
				serarchIndexName= tEdicion!=null ? tEdicion.getTwitterFeedIndex() : OnlineSerarchIndexName;
			else
				serarchIndexName= tEdicion!=null ? tEdicion.getTwitterFeedIndexOffline() : OfflineSerarchIndexName;

		}
		
		TfsAdvancedSearch adSearch = new TfsAdvancedSearch();
		
		String query = "";

		query += getQueryClause(tags,"keywords",OR_OPERATOR);
		query += getCategoryQueryClause(categorias,"categoria",OR_OPERATOR);

		query += getQueryClause(nombre,"nombre",OR_OPERATOR);

		query = query.replaceFirst(" AND ", "");
		
		LOG.debug(this.getClass().getName() + "- index:" + serarchIndexName + " - query: '" + query + "'");
		adSearch.init(cms);
		adSearch.setMatchesPerPage(size);
		adSearch.setQuery(query);
		adSearch.setMaxResults(size*page+1);	
		adSearch.setPage(page);
		adSearch.setIndex(serarchIndexName);

		adSearch.setSortOrder(new Sort(getOrder()));
		
		List<CmsSearchResult> resultados = adSearch.getSearchResult();
		
		if (resultados!=null)
			for (CmsSearchResult resultado : resultados)
			{
	 			String path = cms.getRequestContext().removeSiteRoot(resultado.getPath());
	 			try {
	 				feeds.add(cms.readResource(path));
				
	 			} catch (CmsException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		
		return feeds;

	}
	
	protected SortField[]  getOrder() {
		
		List<SortField> sortBy = new ArrayList<SortField>();
		sortBy.add(SortField.FIELD_SCORE);
		
		return sortBy.toArray(new SortField[sortBy.size()]);
	}

	private TipoEdicion getTipoEdicion(CmsObject cms, String publication) {
		
		TipoEdicionService tEService = new TipoEdicionService();
		TipoEdicion tEdicion = null;
		if (publication!=null)
		{
			try {
				
				if (publication.toLowerCase().trim().equals("current"))
					tEdicion = tEService.obtenerTipoEdicion(cms,cms.getRequestContext().getUri());
				else {
					Scanner scanner = new Scanner(publication);   
					if (scanner.hasNextInt())
						tEdicion = tEService.obtenerTipoEdicion(scanner.nextInt()); 

				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (tEdicion==null)
			try {
				tEdicion = tEService.obtenerTipoEdicion(cms,cms.getRequestContext().getUri());
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}			
		
		if (tEdicion==null)
		{
			String siteName = openCmsService.getSiteName(cms.getRequestContext().getSiteRoot());
			try {
				tEdicion = tEService.obtenerEdicionOnlineRoot(siteName);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return tEdicion;
	}
	
	protected String getQueryClause(String[] values,String categoryName, String operator)
	{
		String clauseLucene = "";
		if (values!=null)
		{
			clauseLucene = categoryName + ":(";
			for (String value : values)
				clauseLucene += " " + value + operator ;
			
			clauseLucene = clauseLucene.substring(0,clauseLucene.length()-operator.length());
			clauseLucene +=")";
			
			clauseLucene = " AND " + clauseLucene;
		}
		
		return clauseLucene;
	}

	protected String getCategoryQueryClause(String[] values,String categoryName, String operator)
	{
		String clauseLucene = "";
		if (values!=null)
		{
			clauseLucene = categoryName + ":(";
			for (String value : values) {
				if (value.charAt(0)=='+')
    			{
					value = value.substring(1);
					operator = AND_OPERATOR;
    			}
				
				String itemValue = "\"";
				for (String part : value.split("/"))
				{
					if (part.trim().length()>0)
					{
						itemValue += part + " ";
					}
				}
				if (!itemValue.equals("\""))
					clauseLucene += " " + itemValue + "\"" + operator ;
			}
			clauseLucene = clauseLucene.substring(0,clauseLucene.length()-operator.length());
			clauseLucene +=")";
			
			clauseLucene = " AND " + clauseLucene;
		}
		
		return clauseLucene;
	}


}
