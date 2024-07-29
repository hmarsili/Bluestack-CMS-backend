package com.tfsla.diario.imageCollector;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.logging.Log;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.SortedNumericSelector;
import org.apache.lucene.search.SortedNumericSortField;
import org.apache.lucene.search.SortedSetSortField;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
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
import com.tfsla.diario.friendlyTags.TfsImagenesListTag;
import com.tfsla.diario.friendlyTags.TfsNoticiasListTag;
import com.tfsla.diario.newsCollector.A_Collector;
import com.tfsla.diario.newsCollector.order.OrderDirective;

public class LuceneImageCollector extends A_Collector {

    protected static final Log LOG = CmsLog.getLog(LuceneImageCollector.class);
 	
	static protected List<OrderDirective> supportedOrders = new ArrayList<OrderDirective>();
	
	public static final OrderDirective ORDER_BY_TAG  = new  OrderDirective("tag","","Keywords","",OrderDirective.TYPE_STRING,true);
	public static final OrderDirective ORDER_BY_AUTHOR  = new  OrderDirective("author","","Author","",OrderDirective.TYPE_STRING,true);
	public static final OrderDirective ORDER_BY_AGENCY  = new  OrderDirective("agency","","Agency","",OrderDirective.TYPE_STRING,true);
	public static final OrderDirective ORDER_BY_CREATIONDATE  = new  OrderDirective("creation-date","",CmsSearchField.FIELD_DATE_CREATED,"",OrderDirective.TYPE_STRING,false);
	public static final OrderDirective ORDER_BY_USERMODIFICATIONDATE  = new  OrderDirective("user-modification-date","",CmsSearchField.FIELD_DATE_LASTMODIFIED,"",OrderDirective.TYPE_STRING,false);
	public static final OrderDirective ORDER_BY_MODIFICATIONDATE  = new  OrderDirective("modification-date","",CmsSearchField.FIELD_DATE_LASTMODIFIED,"",OrderDirective.TYPE_STRING,false);

	{
		supportedOrders.add(ORDER_BY_TAG);
		supportedOrders.add(ORDER_BY_AUTHOR);
		supportedOrders.add(ORDER_BY_AGENCY);
		supportedOrders.add(ORDER_BY_CREATIONDATE);
		supportedOrders.add(ORDER_BY_USERMODIFICATIONDATE);
		supportedOrders.add(ORDER_BY_MODIFICATIONDATE);
	}

	final static String OnlineSerarchIndexName = "IMAGENES_ONLINE";
	final static String OfflineSerarchIndexName = "IMAGENES_OFFLINE";

	final static String OR_OPERATOR = " OR ";
	final static String AND_OPERATOR = " AND ";
	
	String serarchIndexName = OnlineSerarchIndexName;
	
	public List<CmsResource> collectImages(Map<String, Object> parameters, CmsObject cms) {
		
		List<CmsResource> imagenes = new ArrayList<CmsResource>();
		
		int size = Integer.MAX_VALUE;
		if (parameters.get("size")!=null)
			size = (Integer)parameters.get(TfsImagenesListTag.param_size);

		int page = (Integer)parameters.get(TfsImagenesListTag.param_page);

		String[] autores = getValues((String)parameters.get(TfsImagenesListTag.param_author));
		String[] tags = getValues((String)parameters.get(TfsImagenesListTag.param_tags));
		String[] agencias = getValues((String)parameters.get(TfsImagenesListTag.param_agency));
		
		String[] categorias = getValues((String)parameters.get(TfsImagenesListTag.param_category));

		Boolean searchInHistory = (Boolean)parameters.get(TfsImagenesListTag.param_searchinhistory);
		
		String from = (String)parameters.get(TfsImagenesListTag.param_from);
		String to = (String)parameters.get(TfsImagenesListTag.param_to);
		
		String searchIndex = (String)parameters.get(TfsImagenesListTag.param_searchIndex);
		Boolean includeCropped = (Boolean)parameters.get(TfsImagenesListTag.param_includeCropped);
		String originalImage = (String)parameters.get(TfsImagenesListTag.param_originalImage);
		String filter = (String)parameters.get(TfsImagenesListTag.param_filter);
		String advancedFilter = (String)parameters.get(TfsImagenesListTag.param_advancedFilter);

		String onnews = (String)parameters.get(TfsImagenesListTag.param_onnews);
		String publication = (String)parameters.get(TfsImagenesListTag.param_publication);
		
		TipoEdicion tEdicion = getTipoEdicion(cms, publication);
		
		if (searchIndex!=null)
			serarchIndexName=searchIndex;
		else 
		{
			if (cms.getRequestContext().currentProject().isOnlineProject())
				serarchIndexName= tEdicion!=null ? tEdicion.getImagenesIndex() : OnlineSerarchIndexName;
			else
				serarchIndexName= tEdicion!=null ? tEdicion.getImagenesIndexOffline() : OfflineSerarchIndexName;

		}

		
		TfsAdvancedSearch adSearch = new TfsAdvancedSearch();
		
		String query = "";

		query += getQueryClause(tags,"Keywords",OR_OPERATOR);
		query += getCategoryQueryClause(categorias,"Category",OR_OPERATOR);

		query += getQueryClause(autores,"Author",OR_OPERATOR);
		query += getQueryClause(agencias,"Agency",OR_OPERATOR);
		
		if (includeCropped != null && !includeCropped)
			query += getQueryClause( "*:* !true","isCrop",OR_OPERATOR);
		query += getQueryClause(originalImage==null? null:'"'+originalImage+'"',"originalImage",OR_OPERATOR);
		
		Date desde = parseDateTime(from);
		Date hasta = parseDateTime(to);
		
		//SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		
		if (desde!=null)
			from = DateTools.dateToString(desde, DateTools.Resolution.MILLISECOND); // sdf.format(desde);
		if (hasta!=null)
			to = DateTools.dateToString(hasta, DateTools.Resolution.MILLISECOND); // sdf.format(hasta);
		else
			if (from!=null) to = DateTools.dateToString(new Date(), DateTools.Resolution.MILLISECOND);
		
		//query += getRangeQueryClause(from,to,CmsSearchField.FIELD_DATE_LASTMODIFIED);
		query += getRangeQueryClause(from,to,CmsSearchField.FIELD_DATE_CREATED);
		
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
					,CmsSearchField.FIELD_DATE_CREATED);
		}
		
		query = query.replaceFirst(" AND ", "");
		
//		adSearch.init(cms);
//		adSearch.setQuery(query);
//		adSearch.setMaxResults(size);	
//		adSearch.setPage(page);
//		adSearch.setIndex(serarchIndexName);

		LOG.debug(this.getClass().getName() + "- index:" + serarchIndexName + " - query: '" + query + "'");
		
		CmsSearchIndex idx = OpenCms.getSearchManager().getIndex(serarchIndexName);
		CmsSearchFieldConfiguration fieldConf = idx.getFieldConfiguration();

		adSearch.init(cms,fieldConf);
		adSearch.setMatchesPerPage(size);
		adSearch.setQuery(query);
		adSearch.setMaxResults(size*page+1);	
		adSearch.setPage(page);
		adSearch.setIndex(serarchIndexName);
		
		if (searchInHistory!=null && searchInHistory.equals(Boolean.TRUE)) {
			for (String secIdxName : OpenCms.getSearchManager().getIndexNames()) {
				if (secIdxName.startsWith(serarchIndexName + "_HISTORICAL"))
						adSearch.setSecundaryIndex(secIdxName);
			}
		}
		
		String order = (String)parameters.get(TfsImagenesListTag.param_order);
		
		adSearch.setSortOrder(new Sort(getOrder(order)));
		
		List<CmsSearchResult> resultados = adSearch.getSearchResult();
		
		if (resultados!=null)
			for (CmsSearchResult resultado : resultados)
			{
	 			String path = cms.getRequestContext().removeSiteRoot(resultado.getPath());
	 			try {
	 				imagenes.add(cms.readResource(path));
				
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
	
	protected String getRangeQueryClause(String from, String to, String categoryName)
	{
		if (from!=null || to!=null)
			return " AND " + categoryName + ":[" + from + " TO " + to + "]";

		return "";
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

	protected String getQueryClause(String value,String categoryName, String operator) {
		String clauseLucene = "";
		if (value!=null) {
			clauseLucene = categoryName + ":(";
			clauseLucene += " " + value + operator ;
			clauseLucene = clauseLucene.substring(0,clauseLucene.length()-operator.length());
			clauseLucene +=")";
			clauseLucene = " AND " + clauseLucene;
		}
		
		return clauseLucene;
	}
}
