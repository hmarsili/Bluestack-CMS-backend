package com.tfsla.diario.videoCollector;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.logging.Log;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.SortedNumericSelector;
import org.apache.lucene.search.SortedNumericSortField;
import org.apache.lucene.search.SortedSetSortField;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.search.CmsSearchIndex;
import org.opencms.search.CmsSearchResult;
import org.opencms.search.TfsAdvancedSearch;
import org.opencms.search.fields.CmsSearchField;
import org.opencms.search.fields.CmsSearchFieldConfiguration;

import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.TipoEdicionService;
import com.tfsla.diario.ediciones.services.openCmsService;
import com.tfsla.diario.friendlyTags.TfsNoticiasListTag;
import com.tfsla.diario.friendlyTags.TfsVideosListTag;
import com.tfsla.diario.newsCollector.A_Collector;
import com.tfsla.diario.newsCollector.order.OrderDirective;

public class LuceneVideoCollector extends A_Collector {

    protected static final Log LOG = CmsLog.getLog(LuceneVideoCollector.class);

	 	
	static protected List<OrderDirective> supportedOrders = new ArrayList<OrderDirective>();
	
	public static final OrderDirective ORDER_BY_TAG  = new  OrderDirective("tag","","Keywords","",OrderDirective.TYPE_STRING,true);
	//public static final OrderDirective ORDER_BY_AUTHOR  = new  OrderDirective("author","","Author","",OrderDirective.TYPE_STRING,true);
	public static final OrderDirective ORDER_BY_AGENCY  = new  OrderDirective("agency","","Agency","",OrderDirective.TYPE_STRING,true);
	public static final OrderDirective ORDER_BY_CREATIONDATE  = new  OrderDirective("creation-date","",CmsSearchField.FIELD_DATE_CREATED,"",OrderDirective.TYPE_STRING,false);
	public static final OrderDirective ORDER_BY_USERMODIFICATIONDATE  = new  OrderDirective("user-modification-date","",CmsSearchField.FIELD_DATE_LASTMODIFIED,"",OrderDirective.TYPE_STRING,false);
	public static final OrderDirective ORDER_BY_MODIFICATIONDATE  = new  OrderDirective("modification-date","",CmsSearchField.FIELD_DATE_LASTMODIFIED,"",OrderDirective.TYPE_STRING,false);

	{
		supportedOrders.add(ORDER_BY_TAG);
		//supportedOrders.add(ORDER_BY_AUTHOR);
		supportedOrders.add(ORDER_BY_AGENCY);
		supportedOrders.add(ORDER_BY_CREATIONDATE);
		supportedOrders.add(ORDER_BY_USERMODIFICATIONDATE);
		supportedOrders.add(ORDER_BY_MODIFICATIONDATE);
	}

	final static String OnlineSerarchIndexName = "VIDEOS_ONLINE";
	final static String OfflineSerarchIndexName = "VIDEOS_OFFLINE";

	final static String OR_OPERATOR = " OR ";
	final static String AND_OPERATOR = " AND ";
	
	String serarchIndexName = OnlineSerarchIndexName;
	
	public List<CmsResource> collectVideos(Map<String, Object> parameters, CmsObject cms) {
		
		List<CmsResource> imagenes = new ArrayList<CmsResource>();
		
		int size = Integer.MAX_VALUE;
		if (parameters.get("size")!=null)
			size = (Integer)parameters.get(TfsVideosListTag.param_size);

		int page = (Integer)parameters.get(TfsVideosListTag.param_page);

		String[] type = getValues((String)parameters.get(TfsVideosListTag.param_type));
		//String[] autores = getValues((String)parameters.get(TfsVideosListTag.param_author));
		String[] tags = getValues((String)parameters.get(TfsVideosListTag.param_tags));
		String[] classification = getValues((String)parameters.get(TfsVideosListTag.param_classification));
		String[] agencias = getValues((String)parameters.get(TfsVideosListTag.param_agency));
		String[] videoformats = getValues((String)parameters.get(TfsVideosListTag.param_formats));

		String[] categorias = getValues((String)parameters.get(TfsVideosListTag.param_category));

		String from = (String)parameters.get(TfsVideosListTag.param_from);
		String to = (String)parameters.get(TfsVideosListTag.param_to);

		String onnews = (String)parameters.get(TfsVideosListTag.param_onnews);
		
		String publication = (String)parameters.get(TfsVideosListTag.param_publication);

		Boolean searchInHistory = (Boolean)parameters.get(TfsVideosListTag.param_searchinhistory);
		
		String uid = (String)parameters.get(TfsVideosListTag.param_id);
		
		String searchIndex = (String)parameters.get(TfsVideosListTag.param_searchIndex);
		String filter = (String)parameters.get(TfsVideosListTag.param_filter);
		String advancedFilter = (String)parameters.get(TfsVideosListTag.param_advancedFilter);

		TipoEdicion tEdicion = getTipoEdicion(cms, publication);
		
		if (searchIndex!=null)
			serarchIndexName=searchIndex;
		else 
		{
			if (cms.getRequestContext().currentProject().isOnlineProject())
				serarchIndexName= tEdicion!=null ? tEdicion.getVideosIndex() : OnlineSerarchIndexName;
			else
				serarchIndexName= tEdicion!=null ? tEdicion.getVideosIndexOffline() : OfflineSerarchIndexName;

		}
		
		TfsAdvancedSearch adSearch = new TfsAdvancedSearch();
		
		CmsResourceFilter resourceFilter = (CmsResourceFilter) parameters.get(TfsVideosListTag.param_resourcefilter);	
		if (resourceFilter == null)
			resourceFilter = CmsResourceFilter.DEFAULT;
		
		Query expirationQuery = null;
		if (resourceFilter.equals(CmsResourceFilter.DEFAULT) 
				|| resourceFilter.equals(CmsResourceFilter.DEFAULT_FILES)
				|| resourceFilter.equals(CmsResourceFilter.DEFAULT_FOLDERS)) {
			
			expirationQuery = getVisibilityQuery();
		}
		
		if(uid!=null && !uid.equals(""))
			adSearch.setLanguageAnalyzer(new WhitespaceAnalyzer());
		
		String query = "";

		List<String> tIds = new ArrayList<String>();
		if (type!=null)
			for (String t : type)
			{
				try {
					tIds.add("" +OpenCms.getResourceManager().getResourceType(t).getTypeId());
				}
				catch (CmsLoaderException e) {}
			}
		
		String[] aTIds = new String[tIds.size()];

		query += getQueryClause(videoformats,"formats",OR_OPERATOR);
		query += getQueryClause(classification,"Classification",AND_OPERATOR);
		query += getQueryClause(tags,"Keywords",OR_OPERATOR);
		query += getCategoryQueryClause(categorias,"Category",OR_OPERATOR);
		
		query += getQueryClause(tIds.toArray(aTIds),"typeId",OR_OPERATOR); //CmsSearchField.FIELD_TYPE
//		query += getQueryClause(autores,"Author",OR_OPERATOR);
		query += getQueryClause(agencias,"Agency",OR_OPERATOR);
		
		if(uid!=null && !uid.equals(""))
			query += " AND Uid:\"" + uid + "\"";

		Date desde = parseDateTime(from);
		Date hasta = parseDateTime(to);
		
		//SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		
		if (desde!=null)
			from = DateTools.dateToString(desde, DateTools.Resolution.MILLISECOND); // sdf.format(desde);
		if (hasta!=null)
			to = DateTools.dateToString(hasta, DateTools.Resolution.MILLISECOND); // sdf.format(hasta);
		else
			if (from!=null) to = DateTools.dateToString(new Date(), DateTools.Resolution.MILLISECOND);
		
		query += getRangeQueryClause(from,to,CmsSearchField.FIELD_DATE_LASTMODIFIED);
		
		if (filter!=null)
			query += " AND ( Title:(" + filter + ") OR Description:(" + filter + ") OR name:(" + filter + "))";

		if (advancedFilter!=null)
			query += " AND " + advancedFilter;
		
		if (onnews!=null && onnews.toLowerCase().trim().equals("true"))
			query += " AND NewsCount:[1 TO 9999999]"; 

		if (query.equals(""))
		{
			desde = parseDateTime("19400101");
			hasta = parseDateTime("0d");
						
			
			query += getRangeQueryClause(
					DateTools.dateToString(desde, DateTools.Resolution.MILLISECOND)
					,DateTools.dateToString(hasta, DateTools.Resolution.MILLISECOND)
					,CmsSearchField.FIELD_DATE_LASTMODIFIED);
		}

		query = query.replaceFirst(" AND ", "");
		
		LOG.debug(this.getClass().getName() + "- index:" + serarchIndexName + " - query: '" + query + "'");

		CmsSearchIndex idx = OpenCms.getSearchManager().getIndex(serarchIndexName);
		
		if (idx==null) {
			LOG.error("Error indice no existe " + serarchIndexName);
			return imagenes;
		}
		
		CmsSearchFieldConfiguration fieldConf = idx.getFieldConfiguration();

		adSearch.init(cms,fieldConf);
		adSearch.setMatchesPerPage(size);
		adSearch.setQuery(query);
		
		if (expirationQuery!=null)
			adSearch.setExtraQuery(expirationQuery);
		
		adSearch.setMaxResults(size*page+1);	
		adSearch.setPage(page);
		adSearch.setIndex(serarchIndexName);

		if (searchInHistory!=null && searchInHistory.equals(Boolean.TRUE)) {
			for (String secIdxName : OpenCms.getSearchManager().getIndexNames()) {
				if (secIdxName.startsWith(serarchIndexName + "_HISTORICAL"))
						adSearch.setSecundaryIndex(secIdxName);
			}
		}
		
		adSearch.setResFilter(resourceFilter);
		
		String order = (String)parameters.get(TfsVideosListTag.param_order);
		
		adSearch.setSortOrder(new Sort(getOrder(order)));
		
		List<CmsSearchResult> resultados = adSearch.getSearchResult();
		
		if (resultados!=null)
			for (CmsSearchResult resultado : resultados)
			{
	 			String path = cms.getRequestContext().removeSiteRoot(resultado.getPath());
	 			try {
	 				imagenes.add(cms.readResource(path,CmsResourceFilter.ALL));
				
	 			} catch (CmsException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		
		return imagenes;
	}

	protected SortField[]  getOrder(String order) {
		
		List<SortField> sortBy = new ArrayList<SortField>();
		
		if (order!=null && order.trim().length()>0) {
			String[] parts = order.split(",");
			for (String part : parts)
			{
				part = part.toLowerCase();
			
				boolean ascending = (part.contains(" asc"));
				boolean descending = (part.contains(" desc"));
			
				part = part.replace(" asc", "");
				part = part.replace(" desc", "");
	
				part = part.trim();
						
				if (supportedOrders.contains(new OrderDirective(part)))
				{
					OrderDirective od = getOrderDirective(part);
					if (ascending)
						od.setAscending(true);
					if (descending)
						od.setAscending(false);
						
					SortField campoOrden = null;		
					if (od.getType().equals(OrderDirective.TYPE_INTEGER) || od.getType().equals(OrderDirective.TYPE_DATE) || od.getType().equals(OrderDirective.TYPE_LONG)) {
						campoOrden = new SortedNumericSortField(od.getLuceneName()
								,SortField.Type.LONG,!od.isAscending(),
								od.isAscending() ?
								SortedNumericSelector.Type.MAX
								:
								SortedNumericSelector.Type.MIN
						);
				
						campoOrden.setMissingValue(
							!od.isAscending() ?
									Long.MIN_VALUE
									:
									Long.MAX_VALUE
							);
					}
					else {
						campoOrden = new SortedSetSortField(od.getLuceneName(),!od.isAscending());
						
						campoOrden.setMissingValue(
								!od.isAscending() ? 
										SortField.STRING_FIRST
										:
										SortField.STRING_LAST
								);
					}
					
					sortBy.add(campoOrden);
				}
			}
		}
		sortBy.add(SortField.FIELD_SCORE);
		
		return sortBy.toArray(new SortField[sortBy.size()]);
	}
	
	public static OrderDirective getOrderDirective(String name)
	{
		int pos = supportedOrders.indexOf(new OrderDirective(name));
		if (pos>=0)
		{
			OrderDirective od = supportedOrders.get(pos);
			return new OrderDirective(od.getName(),od.getPropertyName(), od.getLuceneName(), od.getContentName(), od.getType(), od.isAscending());
		}
		return null;
	}
	
	protected String getQueryClause(String[] values,String categoryName, String operator)
	{
		String clauseLucene = "";
		if (values!=null && values.length > 0)
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
						itemValue += part.replaceAll("[-_]", "") + " ";
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

	protected String getRangeQueryClause(String from, String to, String categoryName)
	{
		if (from!=null || to!=null)
			return " AND " + categoryName + ":[" + from + " TO " + to + "]";

		return "";
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

	private Query legacyVideoWithNoVisibilityFields() {
		// Condicion: Aquellos video que no tienen los campos de expiracion y disponibilidad
		Query matchAll = new MatchAllDocsQuery();
		
		String from	=	DateTools.dateToString(
                new Date(0),
                DateTools.Resolution.SECOND);
		
		String to	=	DateTools.dateToString(
                new Date(Long.MAX_VALUE),
                DateTools.Resolution.SECOND);
		
		TermRangeQuery AllwithExpiredValue = TermRangeQuery.newStringRange(CmsSearchField.FIELD_DATE_EXPIRES_LOOKUP,from,to,true,true);
		TermRangeQuery AllwithReleasedValue = TermRangeQuery.newStringRange(CmsSearchField.FIELD_DATE_RELEASED_LOOKUP,from,to,true,true);
		
		
		BooleanQuery.Builder allWithNoVisibilityRange = new BooleanQuery.Builder();
		allWithNoVisibilityRange.add(matchAll,BooleanClause.Occur.MUST);
		allWithNoVisibilityRange.add(AllwithExpiredValue,BooleanClause.Occur.MUST_NOT);
		allWithNoVisibilityRange.add(AllwithReleasedValue,BooleanClause.Occur.MUST_NOT);
		
		return allWithNoVisibilityRange.build();
		
	}
	
	private Query eventInVisibilityWindow() {
		
		String expiresFrom	=	DateTools.dateToString(
                new Date(),
                DateTools.Resolution.SECOND);
		
		String expiresTo	=	DateTools.dateToString(
                new Date(Long.MAX_VALUE),
                DateTools.Resolution.SECOND);
		
		TermRangeQuery expiredLookupRange = TermRangeQuery.newStringRange(CmsSearchField.FIELD_DATE_EXPIRES_LOOKUP,expiresFrom,expiresTo,true,true);
		//query += getRangeQueryClause(expiresFrom, expiresTo, CmsSearchField.FIELD_DATE_EXPIRES_LOOKUP);
		
		String releasedFrom	=	DateTools.dateToString(
                new Date(0),
                DateTools.Resolution.SECOND);
		
		String releasedTo	=	DateTools.dateToString(
                new Date(),
                DateTools.Resolution.SECOND);
		
		TermRangeQuery releasedLookupRange = TermRangeQuery.newStringRange(CmsSearchField.FIELD_DATE_RELEASED_LOOKUP,releasedFrom,releasedTo,true,true);
		
		//query += getRangeQueryClause(releasedFrom, releasedTo, CmsSearchField.FIELD_DATE_RELEASED_LOOKUP);
		BooleanQuery.Builder inRange = new BooleanQuery.Builder();
		inRange.add(releasedLookupRange,BooleanClause.Occur.MUST);
		inRange.add(expiredLookupRange,BooleanClause.Occur.MUST);
		
		return inRange.build();
		
	}
	
	private Query getVisibilityQuery() {
		
		BooleanQuery.Builder inRangeOrNoRestreined = new BooleanQuery.Builder();
		inRangeOrNoRestreined.add(legacyVideoWithNoVisibilityFields(),BooleanClause.Occur.SHOULD);
		inRangeOrNoRestreined.add(eventInVisibilityWindow(),BooleanClause.Occur.SHOULD);
		
		return inRangeOrNoRestreined.build();
	}
	
}
