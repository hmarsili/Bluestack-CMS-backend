package com.tfsla.diario.triviasCollector;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.logging.Log;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.SortedNumericSelector;
import org.apache.lucene.search.SortedNumericSortField;
import org.apache.lucene.search.SortedSetSortField;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsLog;
import org.opencms.search.CmsSearchResult;
import org.opencms.search.TfsAdvancedSearch;

import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.TipoEdicionService;
import com.tfsla.diario.triviasCollector.order.OrderDirective;
import com.tfsla.diario.triviasCollector.order.ResultOrderManager;
import com.tfsla.diario.friendlyTags.TfsEventostListTag;
import com.tfsla.diario.friendlyTags.TfsTriviasListTag;


public class LuceneTriviaCollector extends A_TriviasCollector {

protected static final Log LOG = CmsLog.getLog(LuceneTriviaCollector.class);
	
	final static String OnlineSerarchIndexName = "TRIVIAS_ONLINE";
	final static String OfflineSerarchIndexName = "TRIVIAS_OFFLINE";

	final static String OR_OPERATOR = " OR ";
	final static String AND_OPERATOR = " AND ";
	
	String searchIndexName = OnlineSerarchIndexName;

	public LuceneTriviaCollector(){
		supportedOrders.add(OrderDirective.ORDER_BY_STATUS);
		supportedOrders.add(OrderDirective.ORDER_BY_CATEGORY);
		supportedOrders.add(OrderDirective.ORDER_BY_CLOSEDATE);
		supportedOrders.add(OrderDirective.ORDER_BY_STARTDATE);
		supportedOrders.add(OrderDirective.ORDER_BY_MODIFICATIONDATE);
	}

	@Override
	public boolean canCollect(Map<String, Object> parameters) {
		return true;
	}

	@Override
	public List<String> collectTrivias(Map<String, Object> parameters, CmsObject cms) {
		
		List<String> trivias = new ArrayList<String>();
		
		int size = Integer.MAX_VALUE;
		int page = 1;
		if (parameters.get("size")!=null)
			size = (Integer)parameters.get(TfsTriviasListTag.param_size);
		if (parameters.get(TfsTriviasListTag.param_page)!=null)
			page = (Integer)parameters.get(TfsTriviasListTag.param_page);
		
		String searchIndex = (String)parameters.get(TfsTriviasListTag.param_searchIndex);
		String advancedFilter = (String)parameters.get(TfsTriviasListTag.param_advancedFilter);
		
		String publication = (String)parameters.get(TfsTriviasListTag.param_publication);

		String[] categories = getValues((String)parameters.get(TfsTriviasListTag.param_category));
		String[] tags = getValues((String)parameters.get(TfsTriviasListTag.param_tags));
		
		String fromStartDate = (String)parameters.get(TfsTriviasListTag.param_fromStartDate);
		String toStartDate = (String)parameters.get(TfsTriviasListTag.param_toStartDate);
		String fromCloseDate = (String)parameters.get(TfsTriviasListTag.param_fromCloseDate);
		String toCloseDate = (String)parameters.get(TfsTriviasListTag.param_toCloseDate);
		
		String status = (String)parameters.get(TfsTriviasListTag.param_status);
		
		String title = (String)parameters.get(TfsTriviasListTag.param_title);
		String description = (String)parameters.get(TfsTriviasListTag.param_description);
		String author = (String)parameters.get(TfsTriviasListTag.param_author);
		
		Boolean multipleGame = (Boolean)parameters.get(TfsTriviasListTag.param_multipleGame);
		Boolean storeResults = (Boolean)parameters.get(TfsTriviasListTag.param_storeResults);
		Boolean registeredUser = (Boolean)parameters.get(TfsTriviasListTag.param_registeredUser);
		String resultType = (String)parameters.get(TfsTriviasListTag.param_resultType);
		
		Boolean showtemporal = (Boolean)parameters.get(TfsEventostListTag.param_showtemporal);
		
		TipoEdicionService tEService = new TipoEdicionService();	
		TipoEdicion tEdicion = null;
		if (publication!=null) {
			try {
				
				if (publication.toLowerCase().trim().equals("current"))
					tEdicion = tEService.obtenerTipoEdicion(cms,cms.getRequestContext().getUri());
				else {
					Scanner scanner = new Scanner(publication);   
					if (scanner.hasNextInt())
						tEdicion = tEService.obtenerTipoEdicion(scanner.nextInt()); 
					else
						tEdicion = tEService.obtenerTipoEdicion(cms,cms.getRequestContext().getUri());
					scanner.close();

				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		TfsAdvancedSearch adSearch = new TfsAdvancedSearch();
		
		String query = "";
		
		query += getCategoryQueryClause(categories,"categories",OR_OPERATOR);
		query += getQueryClause(tags,"keywords",OR_OPERATOR);
		query += getQueryClause(status,"status",OR_OPERATOR);
		query += getQueryClause(title,"title",OR_OPERATOR);
		query += getQueryClause(description,"description",OR_OPERATOR);
		query += getQueryClause(author,"author",OR_OPERATOR);
		query += getQueryClause(resultType,"resultsType",OR_OPERATOR);
		
		query = fromToDateExtractor(fromStartDate, toStartDate, query, "startDate");
		query = fromToDateExtractor(fromCloseDate, toCloseDate, query, "closeDate");
		
		if( multipleGame!=null && multipleGame.equals(Boolean.FALSE))
			query += " AND ( multipleGame:false)";
		else if( multipleGame!=null && multipleGame.equals(Boolean.TRUE))
			query += " AND ( multipleGame:true)";
		
		if( storeResults!=null && storeResults.equals(Boolean.FALSE))
			query += " AND ( storeResults:false)";
		else if( storeResults!=null && storeResults.equals(Boolean.TRUE))
			query += " AND ( storeResults:true)";
		
		if( registeredUser!=null && registeredUser.equals(Boolean.FALSE))
			query += " AND ( registeredUser:false)";
		else if( registeredUser!=null && registeredUser.equals(Boolean.TRUE))
			query += " AND ( registeredUser:true)";
		
		
		if (advancedFilter!=null)
			query += " AND " + advancedFilter;
		
		if (!cms.getRequestContext().currentProject().isOnlineProject() && (showtemporal==null || showtemporal.equals(Boolean.FALSE))) {
			query += " AND ( temporal:false)";
		}
		
		if (query.equals(""))
			query = fromToDateExtractor("19400101", null, query, "startDate");

		query = query.replaceFirst(" AND ", "");
		
		if (searchIndex!=null)
			searchIndexName=searchIndex;
		else {		
			if (publication==null){
				try {
					tEdicion = tEService.obtenerTipoEdicion(cms,cms.getRequestContext().getUri());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			if (cms.getRequestContext().currentProject().isOnlineProject())
				searchIndexName= tEdicion!=null ? tEdicion.getTriviasIndex() : OnlineSerarchIndexName;
			else
				searchIndexName= tEdicion!=null ? tEdicion.getTriviasIndexOffline() : OfflineSerarchIndexName;
		}

		LOG.debug(this.getClass().getName() + "- index:" + searchIndexName + " - query: '" + query + "'");
		adSearch.init(cms);
		adSearch.setMatchesPerPage(size);
		adSearch.setQuery(query);
		adSearch.setMaxResults(size*page+1);	
		adSearch.setPage(page);
		adSearch.setIndex(searchIndexName);
		adSearch.setResFilter(CmsResourceFilter.ALL);
		
		String order = (String)parameters.get(TfsTriviasListTag.param_order);
		
		List<OrderDirective> orderby = ResultOrderManager.getOrderConfiguration(order);
		
		SortField[] camposOrden = null;
		if (orderby.size()>0)
		{
			camposOrden = new SortField[orderby.size()+1];
			int j=0;
			for (OrderDirective od : orderby)		
			{
				if (od.getType().equals(OrderDirective.TYPE_INTEGER) || od.getType().equals(OrderDirective.TYPE_DATE) || od.getType().equals(OrderDirective.TYPE_LONG)) {
					camposOrden[j] = new SortedNumericSortField(od.getLuceneName()
							,SortField.Type.LONG,!od.isAscending(),
							od.isAscending() ?
							SortedNumericSelector.Type.MAX
							:
							SortedNumericSelector.Type.MIN
					);
			
					camposOrden[j].setMissingValue(
						!od.isAscending() ?
								Long.MIN_VALUE
								:
								Long.MAX_VALUE
						);
				}
				else
					camposOrden[j] = new SortedSetSortField(od.getLuceneName(),!od.isAscending());				
				j++;
			}
			camposOrden[j] = SortField.FIELD_SCORE;
			
		}
		else
			camposOrden = new SortField[] {	
					SortField.FIELD_SCORE
			};
		
		
		adSearch.setSortOrder(new Sort(camposOrden));

		List<CmsSearchResult> resultados = adSearch.getSearchResult();
		
		if (resultados!=null)
			for (CmsSearchResult resultado : resultados)
			{
	 			String path = cms.getRequestContext().removeSiteRoot(resultado.getPath());	 			
	 			trivias.add(path);
			}
		return trivias;
	}

	private String fromToDateExtractor(String fromDate, String toDate,String query, String luceneElemName) {
		Date desde = parseDateTime(fromDate);
		Date hasta = parseDateTime(toDate);
				
		if (desde!=null)
			fromDate = "" + desde.getTime(); // sdf.format(desde);
		if (hasta!=null)
			toDate = "" + hasta.getTime(); // sdf.format(hasta);
		
		query += getRangeQueryClause(fromDate,toDate,luceneElemName);
		return query;
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
	

	protected String getQueryClause(String[] values,String categoryName, String operator){
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
	
	protected String getCategoryQueryClause(String[] values,String categoryName, String operator) {
		String clauseLucene = "";
		if (values!=null) {
			clauseLucene = categoryName + ":(";
			for (String value : values) {
				if (value.charAt(0)=='+') {
					value = value.substring(1);
					operator = AND_OPERATOR;
    			}
				
				String itemValue = "\"";
				for (String part : value.split("/")) {
					if (part.trim().length()>0) {
						itemValue += part.replaceAll("[-_]", "") + " ";
					}
				}
				if (!itemValue.equals("\"")) {
					clauseLucene += " " + itemValue + "\"" + operator;
				}
			}
			clauseLucene = clauseLucene.substring(0,clauseLucene.length()-operator.length());
			clauseLucene +=")";
			clauseLucene = " AND " + clauseLucene;
		}
		
		return clauseLucene;
	}



}
