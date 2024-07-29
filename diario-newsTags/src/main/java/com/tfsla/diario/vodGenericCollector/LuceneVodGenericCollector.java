package com.tfsla.diario.vodGenericCollector;


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

import com.tfsla.diario.friendlyTags.TfsVodGenericListTag;
import com.tfsla.diario.vodGenericCollector.order.OrderDirective;
import com.tfsla.diario.vodGenericCollector.order.ResultOrderManager;




public class LuceneVodGenericCollector extends A_VodGenericCollector {

	protected static final Log LOG = CmsLog.getLog(LuceneVodGenericCollector.class);
		
		final static String OnlineSerarchIndexName = "VOD_GENERIC_ONLINE";
		final static String OfflineSerarchIndexName = "VOD_GENERIC_OFFLINE";

		final static String OR_OPERATOR = " OR ";
		final static String AND_OPERATOR = " AND ";
		
		String serarchIndexName = OnlineSerarchIndexName;

		public LuceneVodGenericCollector(){
			supportedOrders.add(OrderDirective.ORDER_BY_CATEGORY);
			supportedOrders.add(OrderDirective.ORDER_BY_CREATIONDATE);
			supportedOrders.add(OrderDirective.ORDER_BY_MODIFICATIONDATE);
			supportedOrders.add(OrderDirective.ORDER_BY_ORDER_VALUE);
			supportedOrders.add(OrderDirective.ORDER_BY_PRIORITY_ZONE);
			supportedOrders.add(OrderDirective.ORDER_BY_ZONE);
		}
		
		@Override
		public boolean canCollect(Map<String, Object> parameters) {
			
			return true;
		}

		@Override
		public List<String> collectEvent(Map<String, Object> parameters, CmsObject cms) {
			
			List<String> vodGeneric = new ArrayList<String>();
			
			int size = Integer.MAX_VALUE;
			int page = 1;
			if (parameters.get("size")!=null)
				size = (Integer)parameters.get(TfsVodGenericListTag.param_size);
			if (parameters.get(TfsVodGenericListTag.param_page)!=null)
				page = (Integer)parameters.get(TfsVodGenericListTag.param_page);
			
		
			String[] categorias = getValues((String)parameters.get(TfsVodGenericListTag.param_category));
			String[] tags = getValues((String)parameters.get(TfsVodGenericListTag.param_tags));
			String[] personas = getValues((String)parameters.get(TfsVodGenericListTag.param_personas));
			
			String[] zonas = getValues((String)parameters.get(TfsVodGenericListTag.param_zone));
			
			
			String searchIndex = (String)parameters.get(TfsVodGenericListTag.param_searchIndex);
			String advancedFilter = (String)parameters.get(TfsVodGenericListTag.param_advancedFilter);
			
			String publication = (String)parameters.get(TfsVodGenericListTag.param_publication);


			String title = (String)parameters.get(TfsVodGenericListTag.param_title);
			String longDescription = (String)parameters.get(TfsVodGenericListTag.param_longDescription);
			
			
			String fromDate = (String)parameters.get(TfsVodGenericListTag.param_fromDate);
			String toDate = (String)parameters.get(TfsVodGenericListTag.param_toDate);
			
			String type = (String)parameters.get(TfsVodGenericListTag.param_type);
			String classificacion = (String)parameters.get(TfsVodGenericListTag.param_classification);
			
			
			Boolean showtemporal = (Boolean)parameters.get(TfsVodGenericListTag.param_showtemporal);
			
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

					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			
			TfsAdvancedSearch adSearch = new TfsAdvancedSearch();
			
			
			String query = "";
			
			query += getCategoryQueryClause(categorias,"categoria",OR_OPERATOR);
			query += getQueryClause(zonas,"homezone",OR_OPERATOR);
			
			query += getQueryClause(tags,"keywords",OR_OPERATOR);
			query += getQueryClause(personas,"people",OR_OPERATOR);
			
			query += getQueryClause(title,"titulo",OR_OPERATOR);
			query += getQueryClause(longDescription,"cuerpo",OR_OPERATOR);
			query += getQueryClause(type,"type",OR_OPERATOR);
			query += getQueryClause(classificacion,"calificacion",OR_OPERATOR);
			
			
			query = fromToDateExtractor(fromDate, toDate, query, "ultimaModificacion");
			
			
			if (advancedFilter!=null)
				query += " AND " + advancedFilter;
			
			if (!cms.getRequestContext().currentProject().isOnlineProject() && (showtemporal==null || showtemporal.equals(Boolean.FALSE))) {
				query += " AND ( temporal:false)";
			}
			
			
			
			if (query.equals(""))
				query = fromToDateExtractor("19400101", "0d", query, "ultimaModificacion");
			
			query = query.replaceFirst(" AND ", "");
			
			if (searchIndex!=null)
				serarchIndexName=searchIndex;
			else {		
				if (publication==null){
					try {
						tEdicion = tEService.obtenerTipoEdicion(cms,cms.getRequestContext().getUri());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				if (cms.getRequestContext().currentProject().isOnlineProject())
					serarchIndexName= tEdicion!=null ? tEdicion.getVodGenericIndexOnline() : OnlineSerarchIndexName;
				else
					serarchIndexName= tEdicion!=null ? tEdicion.getVodGenericIndexOffline() : OfflineSerarchIndexName;
			}

			LOG.debug(this.getClass().getName() + "- index:" + serarchIndexName + " - query: '" + query + "'");
			adSearch.init(cms);
			adSearch.setMatchesPerPage(size);
			adSearch.setQuery(query);
			adSearch.setMaxResults(size*page+1);	
			adSearch.setPage(page);
			adSearch.setIndex(serarchIndexName);
			adSearch.setResFilter(CmsResourceFilter.ALL);
			
			
			String order = (String)parameters.get(TfsVodGenericListTag.param_order);
			
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
		 			vodGeneric.add(path);
				}
			return vodGeneric;
		}

		private String fromToDateExtractor(String fromDate, String toDate,
				String query, String luceneElemName) {
			Date desde = parseDateTime(fromDate);
			Date hasta = parseDateTime(toDate);
					
			if (desde!=null)
				fromDate = "" + desde.getTime(); // sdf.format(desde);
			if (hasta!=null)
				toDate = "" + hasta.getTime(); // sdf.format(hasta);
			//else
			//	if (fromDate!=null) toDate = "" + new Date().getTime();
			
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

