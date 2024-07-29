package com.tfsla.diario.newsCollector;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;

import org.apache.commons.logging.Log;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
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
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.search.CmsSearchIndex;
import org.opencms.search.CmsSearchResult;
import org.opencms.search.SpanishAnalyzer;
import org.opencms.search.TfsAdvancedSearch;
import org.opencms.search.fields.CmsSearchField;
import org.opencms.search.fields.CmsSearchFieldConfiguration;
import org.opencms.util.CmsUUID;

import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.TipoEdicionService;
import com.tfsla.diario.ediciones.services.openCmsService;
import com.tfsla.diario.friendlyTags.TfsNoticiasListTag;
import com.tfsla.diario.newsCollector.order.OrderDirective;
import com.tfsla.diario.newsCollector.order.ResultOrderManager;
import com.tfsla.diario.utils.SeparateLogger;
import com.tfsla.opencms.search.documents.NoticiacontentExtrator;

public class LuceneNewsCollector extends A_NewsCollector {

    final static Log LOG = CmsLog.getLog(LuceneNewsCollector.class);
	final static String OnlineSerarchIndexName = "DIARIO_CONTENIDOS_ONLINE";
	final static String OfflineSerarchIndexName = "DIARIO_CONTENIDOS_OFFLINE";
	final static String OR_OPERATOR = " OR ";
	final static String AND_OPERATOR = " AND ";
	
	String serarchIndexName = OnlineSerarchIndexName;

	public LuceneNewsCollector() {
		supportedOrders.add(OrderDirective.ORDER_BY_RELEVANCE);
		supportedOrders.add(OrderDirective.ORDER_BY_SECTION);
		supportedOrders.add(OrderDirective.ORDER_BY_USERMODIFICATIONDATE);
		supportedOrders.add(OrderDirective.ORDER_BY_MODIFICATIONDATE);
		supportedOrders.add(OrderDirective.ORDER_BY_CREATIONDATE);
		supportedOrders.add(OrderDirective.ORDER_BY_ZONE);
		supportedOrders.add(OrderDirective.ORDER_BY_CATEGORY);
		supportedOrders.add(OrderDirective.ORDER_BY_TAG);
		supportedOrders.add(OrderDirective.ORDER_BY_AUTHOR);
		supportedOrders.add(OrderDirective.ORDER_BY_PUBLICATION);
		supportedOrders.add(OrderDirective.ORDER_BY_EDITION);
	}

	@Override
	public boolean canCollect(Map<String, Object> parameters) {
		return true;
	}

	int size;
	int page;
	String[] secciones;
	String[] categorias;
	String[] autores;
	String[] newscreator;
	String[] grupos;
	String[] tags;
	String[] edicion;
	String[] zonas;
	String[] estados;
	String[] newstype;
	String[] personas;		
	String paginas;
	String[] publications;
	Boolean showtemporal;
	Boolean searchInHistory;
	Boolean logquery;
	String from;
	String to;
	String searchIndex;
	String filter;
	String advancedFilter;
	CmsResourceFilter resourceFilter;
	String exactpage;
	
	private void getQueryParameters(Map<String, Object> parameters, CmsObject cms) {
		size = Integer.MAX_VALUE;
		page = 1;
		if (parameters.get(TfsNoticiasListTag.param_size)!=null)
			size = (Integer)parameters.get(TfsNoticiasListTag.param_size);
		if (parameters.get(TfsNoticiasListTag.param_page)!=null)
			page = (Integer)parameters.get(TfsNoticiasListTag.param_page);

		secciones = getValues((String)parameters.get(TfsNoticiasListTag.param_section));
		categorias = getValues((String)parameters.get(TfsNoticiasListTag.param_category));
		autores = getValues((String)parameters.get(TfsNoticiasListTag.param_author));
		newscreator = getValues((String)parameters.get(TfsNoticiasListTag.param_newscreator));
		grupos = getValues((String)parameters.get(TfsNoticiasListTag.param_group));
		tags = getValues((String)parameters.get(TfsNoticiasListTag.param_tags));
		edicion = getValues((String)parameters.get(TfsNoticiasListTag.param_edition));
		zonas = getValues((String)parameters.get(TfsNoticiasListTag.param_zone));
		estados = getValues((String)parameters.get(TfsNoticiasListTag.param_state));
		newstype = getValues((String)parameters.get(TfsNoticiasListTag.param_newstype));
		personas= getValues((String)parameters.get(TfsNoticiasListTag.param_persons));	
				
		paginas = (String)parameters.get(TfsNoticiasListTag.param_onmainpage);
		if (paginas!=null && (paginas.trim().toLowerCase().equals("undefined") || paginas.trim().toLowerCase().equals("null"))) 
			paginas=null;
		
		publications = getValues((String)parameters.get(TfsNoticiasListTag.param_publication));

		showtemporal = (Boolean)parameters.get(TfsNoticiasListTag.param_showtemporal);
		searchInHistory = (Boolean)parameters.get(TfsNoticiasListTag.param_searchinhistory);

		logquery = (Boolean)parameters.get(TfsNoticiasListTag.param_logquery);
		
		from = (String)parameters.get(TfsNoticiasListTag.param_from);
		to = (String)parameters.get(TfsNoticiasListTag.param_to);
		
		searchIndex = (String)parameters.get(TfsNoticiasListTag.param_searchIndex);
		filter = (String)parameters.get(TfsNoticiasListTag.param_filter);
		advancedFilter = (String)parameters.get(TfsNoticiasListTag.param_advancedFilter);
		
		resourceFilter = (CmsResourceFilter) parameters.get(TfsNoticiasListTag.param_resourcefilter);	
		if (resourceFilter == null)
			resourceFilter = CmsResourceFilter.DEFAULT;
		
		exactpage = (String)parameters.get(TfsNoticiasListTag.param_exactpage);
		if (exactpage==null)
			exactpage = "auto";
		
	}
	
	private String buildQuery(CmsObject cms, TipoEdicion tEdicion ) {
		String query = "";

		
		if (paginas==null || !paginas.contains("section"))
			query += getQueryClause(secciones,"seccion",OR_OPERATOR);
		
				
		//String categoryClause = getCategoryQueryClause(categorias,"categoria",OR_OPERATOR);
		//String categClause = getCategoryQueryClause(categorias,"category",OR_OPERATOR);
		//if (categoryClause!="")
		//	query += " AND ( " + categoryClause + " OR " + categClause + ") ";
		query += getCategoryQueryClause(categorias,"categoria",OR_OPERATOR);
		query += getQueryClause(autores,"internalUser",OR_OPERATOR);
		query += getQueryClause(newscreator,"newscreator",OR_OPERATOR);	
		query += getQueryClause(tags,"claves",OR_OPERATOR);
		query += getQueryClause(estados,"estado",OR_OPERATOR);
		query += getGroupQueryClause(grupos, "usergroups", OR_OPERATOR,cms);
		query += getQueryClause(newstype,"newstype",OR_OPERATOR);
		query += getQueryClause(personas,"people",OR_OPERATOR);		

		if (edicion!=null && edicion.length==1 && edicion[0].equals("current")) {
			if (tEdicion!=null) {
				query += " AND ( tipoEdicion:" + tEdicion.getId() + " AND edicion:" + tEdicion.getEdicionActiva() + ")";
			}
		} else {
			query += getQueryClause(edicion,"edicion",OR_OPERATOR);
		}
		
		Date desde = parseDateTime(from);
		Date hasta = parseDateTime(to);
		
		//SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		
		if (desde!=null) {
			from = "" + desde.getTime(); // sdf.format(desde);
		}
		if (hasta!=null) {
			to = "" + hasta.getTime(); // sdf.format(hasta);
		} else {
			if (from!=null) to = "" + new Date().getTime();
		}
		
		query += getRangeQueryClause(from,to,"ultimaModificacion");
		query += getHighTrafficQueryClause(paginas, zonas, secciones, tEdicion);
		/*
		if (tEdicion!=null)
			query += " AND ( tipoEdicion:" + tEdicion.getId() + ")"; 
		*/

		if ((filter!=null) && (!(filter.equals("")))) {
			query += " AND ( cuerpo:(" + filter + ") OR titulo:(" + filter + "))";
		}
		if (advancedFilter!=null) {
			query += " AND " + advancedFilter;
		}
		
		if (query.equals("")) {
			desde = parseDateTime("19400101");
			hasta = parseDateTime("0d");
						
			from = "" + desde.getTime(); 
			to = "" + hasta.getTime();
			
			query += getRangeQueryClause(from,to,"ultimaModificacion");
		}
		if (showtemporal==null || showtemporal.equals(Boolean.FALSE)) {
			query += " AND ( temporal:false)";
		} else {
			query += " AND ( temporal:true)";
		}

		if (estados==null) {
			query += " AND NOT ( estado:rechazada)";
		}
		
		return query;
		
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<CmsResource> collectNews(Map<String, Object> parameters, CmsObject cms) {
		List<CmsResource> noticias = new ArrayList<CmsResource>();
		
		
		this.getQueryParameters(parameters, cms);
		
		//uso la publicacion primer cargada como principal
		TipoEdicion tEdicion = getTipoEdicion(cms, (publications!=null && publications.length >0 ? publications[0] : null));
		
		String query = this.buildQuery(cms,tEdicion);
		
		TfsAdvancedSearch adSearch = new TfsAdvancedSearch();
		
		
		Query expirationQuery = null;
		if (resourceFilter.equals(CmsResourceFilter.DEFAULT) 
				|| resourceFilter.equals(CmsResourceFilter.DEFAULT_FILES)
				|| resourceFilter.equals(CmsResourceFilter.DEFAULT_FOLDERS)) {

			expirationQuery = getVisibilityQuery();
		}
		
		
		
		
		
		query = query.replaceFirst(" AND ", "");
		
		if (searchIndex!=null) {
			serarchIndexName=searchIndex;
		} else {
			if (paginas!=null && (paginas.contains("home") || paginas.contains("section"))) {
				serarchIndexName= getHighTrafficLuceneIndexForSite(cms.getRequestContext().getSiteRoot(), tEdicion!=null ? tEdicion.getNombre() : "", cms.getRequestContext().currentProject().isOnlineProject());
			} else if (cms.getRequestContext().currentProject().isOnlineProject()) {
				serarchIndexName= tEdicion!=null ? tEdicion.getNoticiasIndex() : OnlineSerarchIndexName;
			} else {
				serarchIndexName= tEdicion!=null ? tEdicion.getNoticiasIndexOffline() : OfflineSerarchIndexName;
			}
		}
		

		String idLog = String.format("%x", new Date().getTime());
		if (logquery!=null && logquery.equals(Boolean.TRUE)) {
			SeparateLogger.DebugLog("(" + idLog + ") " + this.getClass().getName() + "- index:" + serarchIndexName + " - query: '" + query + "'");
		}

		
		LOG.debug(this.getClass().getName() + "- index:" + serarchIndexName + " - query: '" + query + "'");
		
		CmsSearchIndex idx = OpenCms.getSearchManager().getIndex(serarchIndexName);
		
		if (idx==null) {
			LOG.error("Error indice no existe " + serarchIndexName);
			
			if (logquery!=null && logquery.equals(Boolean.TRUE)) {
				SeparateLogger.ErrorLog("(" + idLog + ") Error indice no existe " + serarchIndexName);
			}

			return noticias;
		}
		
		CmsSearchFieldConfiguration fieldConf = idx.getFieldConfiguration();
		
		
		LOG.debug("fieldConfiguration: " + (fieldConf!=null? fieldConf.getName() : "no encontrado"));
		
		if (logquery!=null && logquery.equals(Boolean.TRUE)) {
			SeparateLogger.DebugLog("(" + idLog + ") fieldConfiguration: " + (fieldConf!=null? fieldConf.getName() : "no encontrado"));
		}

		
		adSearch.init(cms,fieldConf);
		adSearch.setMatchesPerPage(size);
		adSearch.setQuery(query);
		if (expirationQuery!=null)
			adSearch.setExtraQuery(expirationQuery);
		
		
		
		Map<String, Analyzer> analyzerPerField = getHighTrafficAnalyzers(paginas, zonas, secciones, tEdicion);
		if (analyzerPerField.size()>0) {
			Analyzer analyzer = new PerFieldAnalyzerWrapper(
                    new SpanishAnalyzer(), analyzerPerField);
			adSearch.setLanguageAnalyzer(analyzer);
		}
		adSearch.setMaxResults(size*page+1);	
		adSearch.setPage(page);
		adSearch.setIndex(serarchIndexName);
		adSearch.setExactPage(exactpage);
		
		if (searchInHistory!=null && searchInHistory.equals(Boolean.TRUE)) {
			for (String secIdxName : OpenCms.getSearchManager().getIndexNames()) {
				if (secIdxName.startsWith(serarchIndexName + "_HISTORICAL"))
						adSearch.setSecundaryIndex(secIdxName);
			}
		}
		
		if (publications!=null && publications.length>0) {
			for (String publication : publications) {
				if (!publication.equals(publications[0])) {
					TipoEdicion tEdicionSec = getTipoEdicionSecSearch(cms, publication);
					if (tEdicionSec!=null) {
						String serarchIndexSecName = (cms.getRequestContext().currentProject().isOnlineProject() ?  
								tEdicionSec.getNoticiasIndex()
							:
								tEdicionSec.getNoticiasIndexOffline());
						
						adSearch.setSecundaryIndex(serarchIndexSecName);
						
						if (searchInHistory!=null && searchInHistory.equals(Boolean.TRUE)) {
							for (String secIdxName : OpenCms.getSearchManager().getIndexNames()) {
								if (secIdxName.startsWith(serarchIndexSecName + "_HISTORICAL"))
										adSearch.setSecundaryIndex(secIdxName);
							}
						}
					}
				}
			}
		}
		
		adSearch.setResFilter(resourceFilter);

		SortField[] camposOrden = getQueryOrder(parameters, tEdicion, fieldConf);

		adSearch.setSortOrder(new Sort(camposOrden));
		
		LOG.debug(new Sort(camposOrden).toString());

		List<CmsSearchResult> resultados = adSearch.getSearchResult();
		
		boolean _first=true;
		if (resultados!=null) {
			for (CmsSearchResult resultado : resultados) {
				
				if (logquery!=null && logquery.equals(Boolean.TRUE) && _first) {
					SeparateLogger.DebugLog("(" + idLog + ") first result: " + resultado.getPath());
					_first = false;
				}
	 			String path = cms.getRequestContext().removeSiteRoot(resultado.getPath());
	 			try {
	 				noticias.add(cms.readResource(path,resourceFilter));
	 			} catch (CmsException e) {
					LOG.error("Error leyendo la noticia " + path,e);
				}
			}
		}
		
		return noticias;
	}

	private SortField[] getQueryOrder(Map<String, Object> parameters, TipoEdicion tEdicion,
			CmsSearchFieldConfiguration fieldConf) {
		String order = (String)parameters.get(TfsNoticiasListTag.param_order);
		
		List<OrderDirective> orderby = ResultOrderManager.getOrderConfiguration(order,true,fieldConf);
		
		OrderDirective prio = null;
		if (paginas==null || paginas.contains("home")) {
			prio = OrderDirective.ORDER_BY_PRIORITY;
		} else {
			prio = OrderDirective.ORDER_BY_PRIORITY_SECTION;
		}
		
		SortField[] camposOrden = null;
		
		if (orderby.size()>0) {
			camposOrden = new SortField[orderby.size()+1];
			int j=0;
			for (OrderDirective od : orderby) {
				if (od.equals(OrderDirective.ORDER_BY_RELEVANCE))
					camposOrden[j] = SortField.FIELD_SCORE;
				else if (od.equals(OrderDirective.ORDER_BY_PRIORITY)) {
					SortedNumericSortField prioSort = new SortedNumericSortField(
							getPriorityFieldOrder(paginas,zonas,secciones,tEdicion),
							SortField.Type.INT, 
							od.isAscending(),
							od.isAscending() ?
									SortedNumericSelector.Type.MAX
									:
									SortedNumericSelector.Type.MIN
							);
					
					prioSort.setMissingValue(
							od.isAscending() ?
									Integer.MIN_VALUE
									:
									Integer.MAX_VALUE
							);

					camposOrden[j] = prioSort;
				}
				else if (od.equals(OrderDirective.ORDER_BY_ZONE))
					camposOrden[j] = new SortedSetSortField(getZoneFieldOrder(paginas,zonas,secciones,tEdicion),od.isAscending());
				else if (od.getType().equals(OrderDirective.TYPE_INTEGER)) {
					camposOrden[j] = new SortedNumericSortField(od.getLuceneName(),SortField.Type.INT,!od.isAscending(),
							od.isAscending() ?
							SortedNumericSelector.Type.MAX
							:
							SortedNumericSelector.Type.MIN
					);
			
					camposOrden[j].setMissingValue(
						!od.isAscending() ?
								Integer.MIN_VALUE
								:
								Integer.MAX_VALUE
						);
				}

				else if	(od.getType().equals(OrderDirective.TYPE_DATE))
					camposOrden[j] = new SortField(od.getLuceneName(),SortField.Type.LONG,!od.isAscending());
				else if	(od.getType().equals(OrderDirective.TYPE_LONG)) {
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
				else if	(od.getType().equals(OrderDirective.TYPE_FLOAT))
					camposOrden[j] = new SortedNumericSortField(od.getLuceneName(),SortField.Type.FLOAT,!od.isAscending());
				else {
					camposOrden[j] = new SortedSetSortField(od.getLuceneName(),
							!od.isAscending());	
					
					camposOrden[j].setMissingValue(
							!od.isAscending() ? 
									SortField.STRING_FIRST
									:
									SortField.STRING_LAST
							);
				}
				j++;
			}
			camposOrden[j] = SortField.FIELD_SCORE;
		} else {
			camposOrden = new SortField[] {	
				SortField.FIELD_SCORE
			};
		}
		return camposOrden;
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
	
	private String getHighTrafficLuceneIndexForSite(String siteName, String publicationName, boolean online) {
    	String module = "newsTags";
 		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();

		String paramName = (online ? "online" : "offline") + "luceneNewsIndexForSite";
 		LOG.debug(this.getClass().getName() + "- siteName: " + siteName + " publicationName: " + publicationName + " paramName: " + paramName);

		return config.getParam(siteName, publicationName, module, paramName, "");
	}
	
	/**
	 * 
	 * @param paginas
	 * @param zonas
	 * @param secciones
	 * @param tipoEdicion
	 * @return
	 */
	private String getZoneFieldOrder(String paginas, String[] zonas, String[] secciones, TipoEdicion tipoEdicion) {
		/*
		> Si ponen page=home (u otra variante que no no se solo "page=section")
			order: pub_[pub]_zonahome -> [zonahome]
		> Si ponen "page=section"
			- si hay una seccion cargada:
				order: pub_[pub]_seccion_[seccion] -> [zonaseccion]
			- si no hay una seccion o mas de una
				order: pub_[pub]_zonaseccion -> [zonaseccion]
		 */
		boolean hasOneSection = secciones!=null && secciones.length==1;
		String fieldToSort = "";
		if (paginas==null  || !paginas.contains("section")) {
			fieldToSort = "pub" + NoticiacontentExtrator.VALUE_SEPARATOR + NoticiacontentExtrator.escapeValue(tipoEdicion.getNombre()) 
			+ NoticiacontentExtrator.VALUE_SEPARATOR + "zonahome";
		}
		else if (paginas!=null && paginas.contains("section")) {
			if (hasOneSection) {
				fieldToSort = "pub" + NoticiacontentExtrator.VALUE_SEPARATOR + NoticiacontentExtrator.escapeValue(tipoEdicion.getNombre()) 
				+ NoticiacontentExtrator.VALUE_SEPARATOR + "seccion"
				+ NoticiacontentExtrator.VALUE_SEPARATOR + NoticiacontentExtrator.escapeValue(secciones[0]);
			}
			else {
				fieldToSort = "pub" + NoticiacontentExtrator.VALUE_SEPARATOR + NoticiacontentExtrator.escapeValue(tipoEdicion.getNombre()) 
				+ NoticiacontentExtrator.VALUE_SEPARATOR + "zonaseccion";
			}
		}
		
		return fieldToSort;
	}
	
	/** 
	 * @param paginas
	 * @param zonas
	 * @param secciones
	 * @param tipoEdicion
	 * @return
	 */
	private String getPriorityFieldOrder(String paginas, String[] zonas, String[] secciones, TipoEdicion tipoEdicion) {		
		/*
		> Si ponen page=home (u otra variante que no no se solo "page=section")
		- Si no hay zonas (o mas de una zona):
			order: pub_[pub]_zonahome_prio
		- Si hay una zona:
			order: pub_[pub]_zonahome_[zonahome]_prio
	 > Si ponen "page=section"
		- Sin secciones o mas de una:
			+ No hay zonas o mas de una:
			order: pub_[pub]_zonaseccion_prio
			+ Hay 1 zona:
			order: pub_[pub]_zonaseccion_[zonaseccion]_prio
		- Con 1 seccion:
			+ Ho hay Zonas o mas de una:
			order: pub_seccion_[seccion]_prio
			+ Hay 1 zona:
			order: pub_[pub]_seccion_[seccion]_zonaseccion_[zonaseccion]_prio
		*/
		
		boolean hasOneZone = zonas!=null && zonas.length==1;
		boolean hasOneSection = secciones!=null && secciones.length==1;
		String fieldToSort = "";
		if (paginas==null || !paginas.contains("section")) {
			if (!hasOneZone) {
				fieldToSort = "pub" + NoticiacontentExtrator.VALUE_SEPARATOR + NoticiacontentExtrator.escapeValue(tipoEdicion.getNombre()) 
						+ NoticiacontentExtrator.VALUE_SEPARATOR + "zonahome" + NoticiacontentExtrator.VALUE_SEPARATOR + "prio";
			}
			else {
				fieldToSort = "pub" + NoticiacontentExtrator.VALUE_SEPARATOR + NoticiacontentExtrator.escapeValue(tipoEdicion.getNombre()) 
				+ NoticiacontentExtrator.VALUE_SEPARATOR + "zonahome"
				+ NoticiacontentExtrator.VALUE_SEPARATOR + NoticiacontentExtrator.escapeValue(zonas[0]) 
				+ NoticiacontentExtrator.VALUE_SEPARATOR + "prio";
				
			}
		}
		else if (paginas!=null && paginas.contains("section")) {
			if (!hasOneSection && !hasOneZone) {
				fieldToSort = "pub" + NoticiacontentExtrator.VALUE_SEPARATOR + NoticiacontentExtrator.escapeValue(tipoEdicion.getNombre()) 
				+ NoticiacontentExtrator.VALUE_SEPARATOR + "zonaseccion"
				+ NoticiacontentExtrator.VALUE_SEPARATOR + "prio";
			}
			else if (!hasOneSection) { // && hasOneZone
				fieldToSort = "pub" + NoticiacontentExtrator.VALUE_SEPARATOR + NoticiacontentExtrator.escapeValue(tipoEdicion.getNombre()) 
				+ NoticiacontentExtrator.VALUE_SEPARATOR + "zonaseccion"
				+ NoticiacontentExtrator.VALUE_SEPARATOR + NoticiacontentExtrator.escapeValue(zonas[0]) 
				+ NoticiacontentExtrator.VALUE_SEPARATOR + "prio";				
			}
			else if (hasOneSection && !hasOneZone){
				fieldToSort = "pub" + NoticiacontentExtrator.VALUE_SEPARATOR + NoticiacontentExtrator.escapeValue(tipoEdicion.getNombre()) 
				+ NoticiacontentExtrator.VALUE_SEPARATOR + "seccion"
				+ NoticiacontentExtrator.VALUE_SEPARATOR + NoticiacontentExtrator.escapeValue(secciones[0]) 
				+ NoticiacontentExtrator.VALUE_SEPARATOR + "prio";
				
			}
			else { //hasOneSection && hasOneZone
				fieldToSort = "pub" + NoticiacontentExtrator.VALUE_SEPARATOR + NoticiacontentExtrator.escapeValue(tipoEdicion.getNombre()) 
				+ NoticiacontentExtrator.VALUE_SEPARATOR + "seccion"
				+ NoticiacontentExtrator.VALUE_SEPARATOR + NoticiacontentExtrator.escapeValue(secciones[0]) 
				+ NoticiacontentExtrator.VALUE_SEPARATOR + "zonaseccion"
				+ NoticiacontentExtrator.VALUE_SEPARATOR + NoticiacontentExtrator.escapeValue(zonas[0]) 
				+ NoticiacontentExtrator.VALUE_SEPARATOR + "prio";				

			}
		}
		
		return fieldToSort;
	}
	
	
	/*
	 
	 Map<String, Analyzer> analyzerPerField = new HashMap<String, Analyzer>();
        analyzerPerField.put("date", new WhitespaceAnalyzer());
        analyzer = new PerFieldAnalyzerWrapper(
                    new StandardAnalyzer(), analyzerPerField);

	 
	 */
	private  Map<String, Analyzer> getHighTrafficAnalyzers(String paginas, String[] zonas, String[] secciones, TipoEdicion tipoEdicion) {
		 Map<String, Analyzer> analyzerPerField = new HashMap<String, Analyzer>();

		 boolean hasZones = zonas!=null;
			boolean hasSections = secciones!=null;

			String publicationTokenPreffix = "pub" + NoticiacontentExtrator.VALUE_SEPARATOR + NoticiacontentExtrator.escapeValue(tipoEdicion.getNombre());
			
			if (paginas!=null && paginas.contains("home")) {
				if (!hasZones) {
					// pub_[pub]
					analyzerPerField.put(publicationTokenPreffix,new WhitespaceAnalyzer());
				}
				else {
					//pub_[pub]_zonahome
					String zonaHomeTokenPreffix = publicationTokenPreffix + NoticiacontentExtrator.KEY_SEPARATOR + "zonahome";					
					analyzerPerField.put(zonaHomeTokenPreffix,new WhitespaceAnalyzer());
				}
			}
			if (paginas!=null && paginas.contains("section")) {
				if (hasSections) {
					String sectionTokenPreffix = publicationTokenPreffix + NoticiacontentExtrator.KEY_SEPARATOR + "seccion";
					if (!hasZones) {
						analyzerPerField.put(sectionTokenPreffix,new WhitespaceAnalyzer());
					}
					else {
						// pub_[pub]_seccion_[sectionX]
						for (String seccion : secciones) {
							String sectionX = publicationTokenPreffix + NoticiacontentExtrator.KEY_SEPARATOR 
									+ "seccion" + NoticiacontentExtrator.KEY_SEPARATOR +
									NoticiacontentExtrator.escapeValue(seccion);
							analyzerPerField.put(sectionX,new WhitespaceAnalyzer());
							
						}
						
					}	
				} else { //no sections
					if (!hasZones) { //no zone no sections (in any section)
						analyzerPerField.put(publicationTokenPreffix,new WhitespaceAnalyzer());
						//pub_[pub]
					}
					else {
						String zonaSectionTokenPreffix = publicationTokenPreffix + NoticiacontentExtrator.KEY_SEPARATOR + "zonaseccion";
						//pub_[pub]_zonaseccion
						analyzerPerField.put(zonaSectionTokenPreffix,new WhitespaceAnalyzer());				
					}
				}
			}

		 
		 
		 return analyzerPerField;
	}
	
	private String getHighTrafficQueryClause(String paginas, String[] zonas, String[] secciones, TipoEdicion tipoEdicion) {
		boolean hasZones = zonas!=null;
		boolean hasSections = secciones!=null;

		String publicationTokenPreffix = "pub" + NoticiacontentExtrator.VALUE_SEPARATOR + NoticiacontentExtrator.escapeValue(tipoEdicion.getNombre());
		List<String> tokensToSearch = new ArrayList<String>();
		
		if (paginas!=null && paginas.contains("home")) {
			if (!hasZones) {
				// pub_[pub]:(zonahome)
				tokensToSearch.add(publicationTokenPreffix + ":" + "zonahome");
			}
			else {
				//pub_[pub]_zonahome: (zone1 zone2)
				String zonaHomeTokenPreffix = publicationTokenPreffix + NoticiacontentExtrator.KEY_SEPARATOR + "zonahome";
				String zonasHome = zonaHomeTokenPreffix + ":(";
				for (String zona : zonas) {
					zonasHome += NoticiacontentExtrator.escapeValue(zona) + " ";
				}
				zonasHome += ")";
				
				tokensToSearch.add(zonasHome);
			}
		}
		if (paginas!=null && paginas.contains("section")) {
			if (hasSections) {
				String sectionTokenPreffix = publicationTokenPreffix + NoticiacontentExtrator.KEY_SEPARATOR + "seccion";
				if (!hasZones) {
					// pub_[pub]_seccion:(section1 section2)
					String seccionesHome = sectionTokenPreffix + ":(";
					for (String seccion : secciones) {
						seccionesHome += NoticiacontentExtrator.escapeValue(seccion) + " ";
					}
					seccionesHome += ")";
					
					tokensToSearch.add(seccionesHome);
				}
				else {
					// pub_[pub]_seccion_[section1]:(zone1 zone2) OR pub_[pub]_seccion_[section2]:(zone1 zone2)
					for (String seccion : secciones) {
						String sectionX = publicationTokenPreffix + NoticiacontentExtrator.KEY_SEPARATOR 
								+ "seccion" + NoticiacontentExtrator.KEY_SEPARATOR +
								NoticiacontentExtrator.escapeValue(seccion) + ":(";
						for (String zona : zonas) {
							sectionX += NoticiacontentExtrator.escapeValue(zona) + " ";
						}
						sectionX += ")";
						tokensToSearch.add(sectionX);
					}
					
				}	
			} else { //no sections
				if (!hasZones) { //no zone no sections (in any section)
					tokensToSearch.add(publicationTokenPreffix + ":" + "zonaseccion");
					//pub_[pub]:zonaseccion
				}
				else {
					String zonaSectionTokenPreffix = publicationTokenPreffix + NoticiacontentExtrator.KEY_SEPARATOR + "zonaseccion";
					//pub_[pub]_zonaseccion:(zona1 zona2)
					String zonasSection = zonaSectionTokenPreffix + ":(";
					for (String zona : zonas) {
						zonasSection += NoticiacontentExtrator.escapeValue(zona) + " ";
					}
					zonasSection += ")";
					
					tokensToSearch.add(zonasSection);
				}
			}
		}

		if (tokensToSearch.size()==0) {
			return "";
		}
		
		String query = AND_OPERATOR + "((" + tokensToSearch.remove(0);
		for (String part : tokensToSearch)
			query += ")" + OR_OPERATOR + "(" + part;
		
		query += "))";
		return query;
	}

	private TipoEdicion getTipoEdicionSecSearch(CmsObject cms, String publication) {
		TipoEdicionService tEService = new TipoEdicionService();
		TipoEdicion tEdicion = null;
		if (publication!=null) {
			try {
				if (publication.toLowerCase().trim().equals("current")) {
					tEdicion = tEService.obtenerTipoEdicion(cms,cms.getRequestContext().getUri());
				} else {
					Scanner scanner = new Scanner(publication);   
					if (scanner.hasNextInt()) {
						tEdicion = tEService.obtenerTipoEdicion(scanner.nextInt());
					}
					scanner.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return tEdicion;
	}
	
	private TipoEdicion getTipoEdicion(CmsObject cms, String publication) {
		TipoEdicionService tEService = new TipoEdicionService();
		TipoEdicion tEdicion = null;
		if (publication!=null) {
			try {
				if (publication.toLowerCase().trim().equals("current")) {
					tEdicion = tEService.obtenerTipoEdicion(cms,cms.getRequestContext().getUri());
				} else {
					Scanner scanner = new Scanner(publication);   
					if (scanner.hasNextInt()) {
						tEdicion = tEService.obtenerTipoEdicion(scanner.nextInt());
					}
					scanner.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (tEdicion==null) {
			try {
				tEdicion = tEService.obtenerTipoEdicion(cms,cms.getRequestContext().getUri());
			} catch (Exception e1) {
				e1.printStackTrace();
			}			
		}
		if (tEdicion==null) {
			String siteName = openCmsService.getSiteName(cms.getRequestContext().getSiteRoot());
			try {
				tEdicion = tEService.obtenerEdicionOnlineRoot(siteName);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return tEdicion;
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

	protected String getGroupQueryClause(String[] values,String categoryName, String operator, CmsObject cms) {
		String clauseLucene = "";
		if (values!=null) {
			clauseLucene = categoryName + ":(";
			for (String value : values) {
				try {
					CmsGroup group = cms.readGroup(new CmsUUID(value));
					if (group!=null)
						value = group.getName();
				} catch (NumberFormatException e) {
				} catch (CmsException e) { }
				
				clauseLucene += " " + value + operator ;
			}
			clauseLucene = clauseLucene.substring(0,clauseLucene.length()-operator.length());
			clauseLucene +=")";
			clauseLucene = " AND " + clauseLucene;
		}
		
		return clauseLucene;
	}
	
	protected String getQueryClause(String[] values,String categoryName, String operator) {
		String clauseLucene = "";
		if (values!=null) {
			clauseLucene = categoryName + ":(";
			for (String value : values) {
				clauseLucene += " " + value + operator ;
			}
			
			clauseLucene = clauseLucene.substring(0,clauseLucene.length()-operator.length());
			clauseLucene +=")";
			clauseLucene = " AND " + clauseLucene;
		}
		
		return clauseLucene;
	}
	
	protected String getRangeQueryClause(String from, String to, String categoryName) {
		if (from!=null || to!=null) {
			return " AND " + categoryName + ":[" + from + " TO " + to + "]";
		}

		return "";
	}

}