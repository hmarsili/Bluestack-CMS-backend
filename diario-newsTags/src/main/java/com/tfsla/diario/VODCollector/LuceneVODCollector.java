package com.tfsla.diario.VODCollector;

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
import org.opencms.main.OpenCms;
import org.opencms.search.CmsSearchIndex;
import org.opencms.search.CmsSearchResult;
import org.opencms.search.TfsAdvancedSearch;
import org.opencms.search.fields.CmsSearchField;
import org.opencms.search.fields.CmsSearchFieldConfiguration;

import com.tfsla.diario.VODCollector.order.OrderDirective;
import com.tfsla.diario.VODCollector.order.ResultOrderManager;
import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.TipoEdicionService;
import com.tfsla.diario.friendlyTags.TfsNoticiasListTag;
import com.tfsla.diario.friendlyTags.TfsVODListTag;




public class LuceneVODCollector extends A_VODCollector {

	protected static final Log LOG = CmsLog.getLog(LuceneVODCollector.class);
		
		final static String OnlineSerarchIndexName = "VOD_ONLINE";
		final static String OfflineSerarchIndexName = "VOD_OFFLINE";

		final static String OR_OPERATOR = " OR ";
		final static String AND_OPERATOR = " AND ";
		
		String serarchIndexName = OnlineSerarchIndexName;

		public LuceneVODCollector(){
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
			
			List<String> eventos = new ArrayList<String>();
			
			int size = Integer.MAX_VALUE;
			int page = 1;
			if (parameters.get("size")!=null)
				size = (Integer)parameters.get(TfsVODListTag.param_size);
			if (parameters.get(TfsVODListTag.param_page)!=null)
				page = (Integer)parameters.get(TfsVODListTag.param_page);
			
		
			String[] categorias = getValues((String)parameters.get(TfsVODListTag.param_category));
			String[] tags = getValues((String)parameters.get(TfsVODListTag.param_tags));
			String[] personas = getValues((String)parameters.get(TfsVODListTag.param_personas));
			
			String[] zonas = getValues((String)parameters.get(TfsVODListTag.param_zone));
			
			
			String searchIndex = (String)parameters.get(TfsVODListTag.param_searchIndex);
			String advancedFilter = (String)parameters.get(TfsVODListTag.param_advancedFilter);
			
			String publication = (String)parameters.get(TfsVODListTag.param_publication);


			String title = (String)parameters.get(TfsVODListTag.param_title);
			String longDescription = (String)parameters.get(TfsVODListTag.param_longDescription);
			
			
			String fromDate = (String)parameters.get(TfsVODListTag.param_fromDate);
			String toDate = (String)parameters.get(TfsVODListTag.param_toDate);
			
			String type = (String)parameters.get(TfsVODListTag.param_type);
			String orderVOD = (String)parameters.get(TfsVODListTag.param_orderVOD);
			String classificacion = (String)parameters.get(TfsVODListTag.param_classification);
			
			
			Boolean showtemporal = (Boolean)parameters.get(TfsVODListTag.param_showtemporal);
			
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
			query += getQueryClause(orderVOD,"orden",OR_OPERATOR);
			query += getQueryClause(classificacion,"calificacion",OR_OPERATOR);
			
			
			query = fromToDateExtractor(fromDate, toDate, query, "ultimaModificacion");
			
			
			if (advancedFilter!=null)
				query += " AND " + advancedFilter;
			
			if (!query.contains("titulo")){
				//query += " AND ( (titulo: [0 TO 9]) OR (titulo:[a TO z])) ";
				query += " AND ( Title:[* TO *])";
				
			}
			
			if (!cms.getRequestContext().currentProject().isOnlineProject() && (showtemporal==null || showtemporal.equals(Boolean.FALSE))) {
				query += " AND ( temporal:false)";
			}
			
			
			
			if (query.equals(""))
				query = fromToDateExtractor("19400101", null, query, "ultimaModificacion");
			
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
					serarchIndexName= tEdicion!=null ? tEdicion.getVodIndexOnline() : OnlineSerarchIndexName;
				else
					serarchIndexName= tEdicion!=null ? tEdicion.getVodIndexOffline() : OfflineSerarchIndexName;
			}

			LOG.debug(this.getClass().getName() + "- index:" + serarchIndexName + " - query: '" + query + "'");
			
			CmsSearchIndex idx = OpenCms.getSearchManager().getIndex(serarchIndexName);
			
			if (idx==null) {
				LOG.error("Error indice no existe " + serarchIndexName);
				return eventos;
			}
			
			CmsSearchFieldConfiguration fieldConf = idx.getFieldConfiguration();
			
			adSearch.init(cms,fieldConf);
			adSearch.setMatchesPerPage(size);
			adSearch.setQuery(query);
			adSearch.setMaxResults(size*page+1);	
			adSearch.setPage(page);
			adSearch.setIndex(serarchIndexName);
			adSearch.setResFilter(CmsResourceFilter.ALL);
			
			
			String order = (String)parameters.get(TfsVODListTag.param_order);
			
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
		 			eventos.add(path);
				}
			return eventos;
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

