package com.tfsla.diario.recipeCollector;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.logging.Log;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.SortedNumericSelector;
import org.apache.lucene.search.SortedNumericSortField;
import org.apache.lucene.search.SortedSetSortField;
import org.apache.lucene.search.TermRangeQuery;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.search.CmsSearchIndex;
import org.opencms.search.CmsSearchResult;
import org.opencms.search.TfsAdvancedSearch;
import org.opencms.search.fields.CmsSearchField;
import org.opencms.search.fields.CmsSearchFieldConfiguration;

import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.TipoEdicionService;
import com.tfsla.diario.friendlyTags.TfsNoticiasListTag;
import com.tfsla.diario.friendlyTags.TfsRecipeListTag;
import com.tfsla.diario.recipeCollector.order.OrderDirective;
import com.tfsla.diario.recipeCollector.order.ResultOrderManager;


public class LuceneRecipeCollector extends A_RecipeCollector {

	protected static final Log LOG = CmsLog.getLog(LuceneRecipeCollector.class);
		
		final static String OnlineSerarchIndexName = "RECIPE_ONLINE";
		final static String OfflineSerarchIndexName = "RECIPE_OFFLINE";

		final static String OR_OPERATOR = " OR ";
		final static String AND_OPERATOR = " AND ";
		
		String serarchIndexName = OnlineSerarchIndexName;

		public LuceneRecipeCollector(){
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
		public List<String> collectRecipe(Map<String, Object> parameters, CmsObject cms) {
			
			List<String> recetas = new ArrayList<String>();
			
			int size = Integer.MAX_VALUE;
			int page = 1;
			if (parameters.get("size")!=null)
				size = (Integer)parameters.get(TfsRecipeListTag.param_size);
			if (parameters.get(TfsRecipeListTag.param_page)!=null)
				page = (Integer)parameters.get(TfsRecipeListTag.param_page);
			
		
			String[] categorias = getValues((String)parameters.get(TfsRecipeListTag.param_category));
			String[] tags = getValues((String)parameters.get(TfsRecipeListTag.param_tags));
			//String[] personas = getValues((String)parameters.get(TfsRecipeListTag.param_personas));
			
			String[] autores = getValues((String)parameters.get(TfsRecipeListTag.param_author));
			
			String[] zonas = getValues((String)parameters.get(TfsRecipeListTag.param_zone));
			String[] edicion = getValues((String)parameters.get(TfsNoticiasListTag.param_edition));
			String[] estados = getValues((String)parameters.get(TfsNoticiasListTag.param_state));
			
			
			String searchIndex = (String)parameters.get(TfsRecipeListTag.param_searchIndex);
			String advancedFilter = (String)parameters.get(TfsRecipeListTag.param_advancedFilter);
			
			String publication = (String)parameters.get(TfsRecipeListTag.param_publication);


			String title = (String)parameters.get(TfsRecipeListTag.param_title);
			String longDescription = (String)parameters.get(TfsRecipeListTag.param_longDescription);
			
			String fromDate = (String)parameters.get(TfsRecipeListTag.param_fromDate);
			String toDate = (String)parameters.get(TfsRecipeListTag.param_toDate);
			
			//tiemoPreparacion
			String fromPrep = (String)parameters.get(TfsRecipeListTag.param_fromPrep);
			String toPrep = (String)parameters.get(TfsRecipeListTag.param_toPrep);
			//TiempoCoccion
			String fromCocc = (String)parameters.get(TfsRecipeListTag.param_fromCocc);
			String toCocc = (String)parameters.get(TfsRecipeListTag.param_toCocc);
			
			//TiempoCoccion
			String fromCalories= (String)parameters.get(TfsRecipeListTag.param_fromCalories);
			String toCalories = (String)parameters.get(TfsRecipeListTag.param_toCalories);
			
			//TiempoCoccTotalion
			String fromCoccTotal = (String)parameters.get(TfsRecipeListTag.param_fromCoccTotal);
			String toCoccTotal = (String)parameters.get(TfsRecipeListTag.param_toCoccTotal);

			String type = (String)parameters.get(TfsRecipeListTag.param_type);
			String ingrediente = (String)parameters.get(TfsRecipeListTag.param_ingrediente);
			String tipoCoccion = (String)parameters.get(TfsRecipeListTag.param_tipoCoccion);
			String tipoCocina = (String)parameters.get(TfsRecipeListTag.param_tipoCocina);
			String dificultad = (String)parameters.get(TfsRecipeListTag.param_dificultad);
			
			CmsResourceFilter resourceFilter = (CmsResourceFilter) parameters.get(TfsRecipeListTag.param_resourcefilter);	
			if (resourceFilter == null)
				resourceFilter = CmsResourceFilter.DEFAULT;
			
			Boolean showtemporal = (Boolean)parameters.get(TfsRecipeListTag.param_showtemporal);
			
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
			
			Query expirationQuery = null;
			if (resourceFilter.equals(CmsResourceFilter.DEFAULT) 
					|| resourceFilter.equals(CmsResourceFilter.DEFAULT_FILES)
					|| resourceFilter.equals(CmsResourceFilter.DEFAULT_FOLDERS)) {

				expirationQuery = getVisibilityQuery();
			}
			
			String query = "";
			
			query += getCategoryQueryClause(categorias,"categoria",OR_OPERATOR);
			query += getQueryClause(zonas,"homezone",OR_OPERATOR);
			
			query += getQueryClause(tags,"keywords",OR_OPERATOR);
			//query += getQueryClause(personas,"people",OR_OPERATOR);
			query += getQueryClause(autores,"internalUser",OR_OPERATOR);
			
			query += getQueryClause(estados,"estado",OR_OPERATOR);
			
			query += getQueryClause(title,"titulo",OR_OPERATOR);
			query += getQueryClause(longDescription,"cuerpo",OR_OPERATOR);
			query += getQueryClause(type,"type",OR_OPERATOR);
			query += getQueryClause(tipoCocina,"tipoCocina",OR_OPERATOR);
			query += getQueryClause(tipoCoccion,"tipoCoccion",OR_OPERATOR);
			query += getQueryClause(ingrediente,"ingredient",OR_OPERATOR);
			query += getQueryClause(dificultad,"dificultad",OR_OPERATOR);
			
			if (edicion!=null && edicion.length==1 && edicion[0].equals("current")) {
				if (tEdicion!=null) {
					query += " AND ( tipoEdicion:" + tEdicion.getId() + " AND edicion:" + tEdicion.getEdicionActiva() + ")";
				}
			} else {
				query += getQueryClause(edicion,"edicion",OR_OPERATOR);
			}
			
			query += fromToDateExtractor(fromDate, toDate, query, "ultimaModificacion");
			
			query += getRangeQueryClause(fromPrep, toPrep,  "tiempoPreparacion");
			query += getRangeQueryClause(fromCocc, toCocc, "tiempoCoccion");
			query += getRangeQueryClause(fromCoccTotal, toCoccTotal, "tiempoCoccionTotal");
			query += getRangeQueryClause(fromCalories, toCalories, "calories");
	
			
			if (advancedFilter!=null)
				query += " AND " + advancedFilter;
			

			if (!query.contains("titulo"))
				query += " AND ( titulo:[* TO *])";
			
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
					serarchIndexName= tEdicion!=null ? tEdicion.getRecetaIndexOnline(): OnlineSerarchIndexName;
				else
					serarchIndexName= tEdicion!=null ? tEdicion.getRecetaIndexOffline() : OfflineSerarchIndexName;
			}

			CmsSearchIndex idx = OpenCms.getSearchManager().getIndex(serarchIndexName);
			
			if (idx==null) {
				LOG.error("Error indice no existe " + serarchIndexName);
				return recetas;
			}
			CmsSearchFieldConfiguration fieldConf = idx.getFieldConfiguration();
			
			LOG.debug("fieldConfiguration: " + (fieldConf!=null? fieldConf.getName() : "no encontrado"));
			
			LOG.debug(this.getClass().getName() + "- index:" + serarchIndexName + " - query: '" + query + "'");
			adSearch.init(cms,fieldConf);
			adSearch.setMatchesPerPage(size);
			adSearch.setQuery(query);
			
			if (expirationQuery!=null)
				adSearch.setExtraQuery(expirationQuery);
			
			adSearch.setMaxResults(size*page+1);	
			adSearch.setPage(page);
			adSearch.setIndex(serarchIndexName);
			adSearch.setResFilter(CmsResourceFilter.ALL);
			
			
			String order = (String) parameters.get(TfsRecipeListTag.param_order);
			
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
		 			recetas.add(path);
				}
			return recetas;
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
		
		private Query legacyNewsWithNoVisibilityFields() {
			// Condicion: Aquellas noticias que no tienen los campos de expiracion y disponibilidad
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
			
			
			//--------------
			
			return allWithNoVisibilityRange.build();

		}
		
		private Query newsInVisibilityWindow() {
			
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
			inRangeOrNoRestreined.add(legacyNewsWithNoVisibilityFields(),BooleanClause.Occur.SHOULD);
			inRangeOrNoRestreined.add(newsInVisibilityWindow(),BooleanClause.Occur.SHOULD);

			return inRangeOrNoRestreined.build();
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

