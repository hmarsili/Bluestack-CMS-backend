package com.tfsla.diario.eventCollector;

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
import org.opencms.search.CmsSearchResult;
import org.opencms.search.TfsAdvancedSearch;
import org.opencms.search.fields.CmsSearchField;

import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.TipoEdicionService;
import com.tfsla.diario.eventCollector.order.OrderDirective;
import com.tfsla.diario.eventCollector.order.ResultOrderManager;
import com.tfsla.diario.friendlyTags.TfsEventostListTag;

public class LuceneEventCollector extends A_EventCollector {

	protected static final Log LOG = CmsLog.getLog(LuceneEventCollector.class);

	final static String OnlineSerarchIndexName = "EVENTOS_ONLINE";
	final static String OfflineSerarchIndexName = "EVENTOS_OFFLINE";

	final static String OR_OPERATOR = " OR ";
	final static String AND_OPERATOR = " AND ";

	String serarchIndexName = OnlineSerarchIndexName;

	public LuceneEventCollector() {
		supportedOrders.add(OrderDirective.ORDER_BY_CATEGORY);
		supportedOrders.add(OrderDirective.ORDER_BY_CREATIONDATE);
		supportedOrders.add(OrderDirective.ORDER_BY_DATE_FROM);
		supportedOrders.add(OrderDirective.ORDER_BY_MODIFICATIONDATE);
		supportedOrders.add(OrderDirective.ORDER_BY_PRICE_VALUE);
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
		if (parameters.get("size") != null)
			size = (Integer) parameters.get(TfsEventostListTag.param_size);
		if (parameters.get(TfsEventostListTag.param_page) != null)
			page = (Integer) parameters.get(TfsEventostListTag.param_page);

		String[] categorias = getValues((String) parameters.get(TfsEventostListTag.param_category));
		String[] tags = getValues((String) parameters.get(TfsEventostListTag.param_tags));
		String[] personas = getValues((String) parameters.get(TfsEventostListTag.param_personas));

		String searchIndex = (String) parameters.get(TfsEventostListTag.param_searchIndex);
		String advancedFilter = (String) parameters.get(TfsEventostListTag.param_advancedFilter);

		String publication = (String) parameters.get(TfsEventostListTag.param_publication);

		String title = (String) parameters.get(TfsEventostListTag.param_title);
		String shortDescription = (String) parameters.get(TfsEventostListTag.param_shortDescription);
		String longDescription = (String) parameters.get(TfsEventostListTag.param_longDescription);

		String fromDateModification = (String) parameters.get(TfsEventostListTag.param_fromDateModification);
		String toDateModification = (String) parameters.get(TfsEventostListTag.param_toDateModification);
		String fromDate = (String) parameters.get(TfsEventostListTag.param_fromDate);
		String toDate = (String) parameters.get(TfsEventostListTag.param_toDate);

		String place = (String) parameters.get(TfsEventostListTag.param_place);
		String address = (String) parameters.get(TfsEventostListTag.param_address);
		String country = (String) parameters.get(TfsEventostListTag.param_country);
		String locality = (String) parameters.get(TfsEventostListTag.param_locality);
		String region = (String) parameters.get(TfsEventostListTag.param_region);
		String postalCode = (String) parameters.get(TfsEventostListTag.param_postalCode);

		Boolean showtemporal = (Boolean) parameters.get(TfsEventostListTag.param_showtemporal);

		CmsResourceFilter resourceFilter = (CmsResourceFilter) parameters.get(TfsEventostListTag.param_resourcefilter);	
		if (resourceFilter == null)
			resourceFilter = CmsResourceFilter.DEFAULT;
		
		TipoEdicionService tEService = new TipoEdicionService();
		TipoEdicion tEdicion = null;
		if (publication != null) {
			try {

				if (publication.toLowerCase().trim().equals("current"))
					tEdicion = tEService.obtenerTipoEdicion(cms, cms.getRequestContext().getUri());
				else {
					Scanner scanner = new Scanner(publication);
					if (scanner.hasNextInt())
						tEdicion = tEService.obtenerTipoEdicion(scanner.nextInt());
					else
						tEdicion = tEService.obtenerTipoEdicion(cms, cms.getRequestContext().getUri());
					scanner.close();

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

		query += getQueryClause(categorias, "categoria", OR_OPERATOR);
		query += getQueryClause(tags, "keywords", OR_OPERATOR);
		query += getQueryClause(personas, "people", OR_OPERATOR);

		query += getQueryClause(title, "titulo", OR_OPERATOR);
		query += getQueryClause(shortDescription, "volanta", OR_OPERATOR);
		query += getQueryClause(longDescription, "cuerpo", OR_OPERATOR);

		query += getQueryClause(place, "lugar", OR_OPERATOR);
		query += getQueryClause(country, "pais", OR_OPERATOR);
		query += getQueryClause(address, "direccion", OR_OPERATOR);
		query += getQueryClause(locality, "localidad", OR_OPERATOR);
		query += getQueryClause(region, "region", OR_OPERATOR);
		query += getQueryClause(postalCode, "codigoPostal", OR_OPERATOR);

		query = fromToDateExtractor(fromDateModification, toDateModification, query, "ultimaModificacion");
		query = fromToDateExtractor(fromDate, toDate, query, "fechaDesde");

		if (advancedFilter != null)
			query += " AND " + advancedFilter;

		if (!cms.getRequestContext().currentProject().isOnlineProject()
				&& (showtemporal == null || showtemporal.equals(Boolean.FALSE))) {
			query += " AND ( temporal:false)";
		}

		if (query.equals(""))
			query = fromToDateExtractor("19400101", "0d", query, "ultimaModificacion");

		query = query.replaceFirst(" AND ", "");

		if (searchIndex != null)
			serarchIndexName = searchIndex;
		else {
			if (publication == null) {
				try {
					tEdicion = tEService.obtenerTipoEdicion(cms, cms.getRequestContext().getUri());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			if (cms.getRequestContext().currentProject().isOnlineProject())
				serarchIndexName = tEdicion != null ? tEdicion.getEventosIndex() : OnlineSerarchIndexName;
			else
				serarchIndexName = tEdicion != null ? tEdicion.getEventosIndexOffline() : OfflineSerarchIndexName;
		}

		LOG.debug(this.getClass().getName() + "- index:" + serarchIndexName + " - query: '" + query + "'");
		
		adSearch.init(cms);
		adSearch.setMatchesPerPage(size);
		adSearch.setQuery(query);
		
		if (expirationQuery!=null)
			adSearch.setExtraQuery(expirationQuery);
		
		adSearch.setMaxResults(size * page + 1);
		adSearch.setPage(page);
		adSearch.setIndex(serarchIndexName);		
		adSearch.setResFilter(resourceFilter);
		
		String order = (String) parameters.get(TfsEventostListTag.param_order);

		List<OrderDirective> orderby = ResultOrderManager.getOrderConfiguration(order);

		SortField[] camposOrden = null;
		if (orderby.size() > 0) {
			camposOrden = new SortField[orderby.size() + 1];
			int j = 0;
			for (OrderDirective od : orderby) {
				if (od.getType().equals(OrderDirective.TYPE_INTEGER) || od.getType().equals(OrderDirective.TYPE_DATE)
						|| od.getType().equals(OrderDirective.TYPE_LONG)) {
					camposOrden[j] = new SortedNumericSortField(od.getLuceneName(), SortField.Type.LONG,
							!od.isAscending(),
							od.isAscending() ? SortedNumericSelector.Type.MAX : SortedNumericSelector.Type.MIN);

					camposOrden[j].setMissingValue(!od.isAscending() ? Long.MIN_VALUE : Long.MAX_VALUE);
				} else
					camposOrden[j] = new SortedSetSortField(od.getLuceneName(), !od.isAscending());
				j++;
			}
			camposOrden[j] = SortField.FIELD_SCORE;

		} else
			camposOrden = new SortField[] { SortField.FIELD_SCORE };

		adSearch.setSortOrder(new Sort(camposOrden));

		List<CmsSearchResult> resultados = adSearch.getSearchResult();

		if (resultados != null)
			for (CmsSearchResult resultado : resultados) {
				String path = cms.getRequestContext().removeSiteRoot(resultado.getPath());
				eventos.add(path);
			}
		return eventos;
	}

	private String fromToDateExtractor(String fromDate, String toDate, String query, String luceneElemName) {
		Date desde = parseDateTime(fromDate);
		Date hasta = parseDateTime(toDate);

		if (desde != null)
			fromDate = "" + desde.getTime(); // sdf.format(desde);
		if (hasta != null)
			toDate = "" + hasta.getTime(); // sdf.format(hasta);
		// else
		// if (fromDate!=null) toDate = "" + new Date().getTime();

		query += getRangeQueryClause(fromDate, toDate, luceneElemName);
		return query;
	}

	protected String getQueryClause(String value, String categoryName, String operator) {
		String clauseLucene = "";
		if (value != null) {
			clauseLucene = categoryName + ":(";
			clauseLucene += " " + value + operator;
			clauseLucene = clauseLucene.substring(0, clauseLucene.length() - operator.length());
			clauseLucene += ")";
			clauseLucene = " AND " + clauseLucene;
		}

		return clauseLucene;
	}

	protected String getQueryClause(String[] values, String categoryName, String operator) {
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

	protected String getRangeQueryClause(String from, String to, String categoryName) {
		if (from != null || to != null)
			return " AND " + categoryName + ":[" + from + " TO " + to + "]";

		return "";
	}
	
	private Query legacyEventWithNoVisibilityFields() {
		// Condicion: Aquellas eventos que no tienen los campos de expiracion y disponibilidad
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
		inRangeOrNoRestreined.add(legacyEventWithNoVisibilityFields(),BooleanClause.Occur.SHOULD);
		inRangeOrNoRestreined.add(eventInVisibilityWindow(),BooleanClause.Occur.SHOULD);

		return inRangeOrNoRestreined.build();
	}

}
