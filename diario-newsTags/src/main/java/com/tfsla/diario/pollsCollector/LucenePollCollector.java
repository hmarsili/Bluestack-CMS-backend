package com.tfsla.diario.pollsCollector;

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
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.search.CmsSearchIndex;
import org.opencms.search.CmsSearchResult;
import org.opencms.search.TfsAdvancedSearch;
import org.opencms.search.fields.CmsSearchFieldConfiguration;

import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.TipoEdicionService;
import com.tfsla.diario.friendlyTags.TfsEncuestasBoxTag;
import com.tfsla.diario.pollsCollector.order.OrderDirective;
import com.tfsla.diario.pollsCollector.order.ResultOrderManager;
import com.tfsla.diario.pollsCollector.A_PollsCollector;
import com.tfsla.opencmsdev.encuestas.Encuesta;

public class LucenePollCollector extends A_PollsCollector {
	
	protected static final Log LOG = CmsLog.getLog(LucenePollCollector.class);
	
	final static String OnlineSerarchIndexName = "ENCUESTAS_ONLINE";
	final static String OfflineSerarchIndexName = "ENCUESTAS_OFFLINE";

	final static String OR_OPERATOR = " OR ";
	final static String AND_OPERATOR = " AND ";
	
	String serarchIndexName = OnlineSerarchIndexName;

	public LucenePollCollector()
	{
		supportedOrders.add(OrderDirective.ORDER_BY_GROUP);
		supportedOrders.add(OrderDirective.ORDER_BY_STATUS);
		supportedOrders.add(OrderDirective.ORDER_BY_CATEGORY);
		supportedOrders.add(OrderDirective.ORDER_BY_CLOSEDATE);
		supportedOrders.add(OrderDirective.ORDER_BY_CREATIONDATE);
		supportedOrders.add(OrderDirective.ORDER_BY_PUBLICATIONDATE);
		supportedOrders.add(OrderDirective.ORDER_BY_EXPIRATIONDATE);
		
	}
	
	@Override
	public boolean canCollect(Map<String, Object> parameters) {
		
		return true;
	}

	@Override
	public List<String> collectPolls(Map<String, Object> parameters,
			CmsObject cms) {
		
		List<String> encuestas = new ArrayList<String>();
		
		int size = Integer.MAX_VALUE;
		int page = 1;
		if (parameters.get("size")!=null)
			size = (Integer)parameters.get(TfsEncuestasBoxTag.param_size);
		if (parameters.get(TfsEncuestasBoxTag.param_page)!=null)
			page = (Integer)parameters.get(TfsEncuestasBoxTag.param_page);
		
		String[] categorias = getValues((String)parameters.get(TfsEncuestasBoxTag.param_category));
		String[] tags = getValues((String)parameters.get(TfsEncuestasBoxTag.param_tags));
		String[] group = getValues((String)parameters.get(TfsEncuestasBoxTag.param_group));
		
		String[] state = getValues((String)parameters.get(TfsEncuestasBoxTag.param_state));
		
		String searchIndex = (String)parameters.get(TfsEncuestasBoxTag.param_searchIndex);
		String advancedFilter = (String)parameters.get(TfsEncuestasBoxTag.param_advancedFilter);
		
		String publication = (String)parameters.get(TfsEncuestasBoxTag.param_publication);

		String fromDateCreation = (String)parameters.get(TfsEncuestasBoxTag.param_fromDateCreation);
		String toDateCreation = (String)parameters.get(TfsEncuestasBoxTag.param_toDateCreation);
		String fromDateDeadline = (String)parameters.get(TfsEncuestasBoxTag.param_fromDateDeadline);
		String toDateDeadline = (String)parameters.get(TfsEncuestasBoxTag.param_toDateDeadline);
		String fromDateExpiration = (String)parameters.get(TfsEncuestasBoxTag.param_fromDateExpiration);
		String toDateExpiration = (String)parameters.get(TfsEncuestasBoxTag.param_toDateExpiration);
		String fromDatePublication = (String)parameters.get(TfsEncuestasBoxTag.param_fromDatePublication);
		String toDatePublication = (String)parameters.get(TfsEncuestasBoxTag.param_toDatePublication);
		Boolean showtemporal = (Boolean)parameters.get(TfsEncuestasBoxTag.param_showtemporal);
		
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

		//query += getQueryClause(categorias,"categorias",OR_OPERATOR);
		query += getCategoryQueryClause(categorias,"categorias",OR_OPERATOR);
		
		query += getQueryClause(tags,"tags",OR_OPERATOR);
		query += getQueryClause(group,"grupo",OR_OPERATOR);
		query += getStateQueryClause(state,"estado",OR_OPERATOR);
		
		query = fromToDateExtractor(fromDateCreation, toDateCreation, query, "fechaCreacion");
		query = fromToDateExtractor(fromDatePublication, toDatePublication, query, "fechaPublicacion");
		query = fromToDateExtractor(fromDateExpiration, toDateExpiration, query, "fechaExpiracion");
		query = fromToDateExtractor(fromDateDeadline, toDateDeadline, query, "fechaCierre");

		if (advancedFilter!=null)
			query += " AND " + advancedFilter;
		
		if (!cms.getRequestContext().currentProject().isOnlineProject() && (showtemporal==null || showtemporal.equals(Boolean.FALSE))) {
			query += " AND ( temporal:false)";
		}
		
		if (query.equals(""))
			query = fromToDateExtractor("19400101", "0d", query, "fechaCreacion");
		
		query = query.replaceFirst(" AND ", "");
		
		if (searchIndex!=null)
			serarchIndexName=searchIndex;
		else 
		{		
			if (publication==null)
			{
				try {
					tEdicion = tEService.obtenerTipoEdicion(cms,cms.getRequestContext().getUri());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			if (cms.getRequestContext().currentProject().isOnlineProject())
				serarchIndexName= tEdicion!=null ? tEdicion.getEncuestasIndex() : OnlineSerarchIndexName;
			else
				serarchIndexName= tEdicion!=null ? tEdicion.getEncuestasIndexOffline() : OfflineSerarchIndexName;
		}

		LOG.debug(this.getClass().getName() + "- index:" + serarchIndexName + " - query: '" + query + "'");
		
		CmsSearchIndex idx = OpenCms.getSearchManager().getIndex(serarchIndexName);
		
		if (idx==null) {
			LOG.error("Error indice no existe " + serarchIndexName);
			return encuestas;
		}
		
		CmsSearchFieldConfiguration fieldConf = idx.getFieldConfiguration();

		adSearch.init(cms,fieldConf);
		adSearch.setMatchesPerPage(size);
		adSearch.setQuery(query);
		adSearch.setMaxResults(size*page+1);	
		adSearch.setPage(page);
		adSearch.setIndex(serarchIndexName);
		
		String order = (String)parameters.get(TfsEncuestasBoxTag.param_order);
		
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
	 			encuestas.add(path);
			}
		

		return encuestas;
	}

	private String fromToDateExtractor(String fromDate, String toDate,
			String query, String luceneElemName) {
		Date desde = parseDateTime(fromDate);
		Date hasta = parseDateTime(toDate);
				
		if (desde!=null)
			fromDate = "" + desde.getTime(); // sdf.format(desde);
		if (hasta!=null)
			toDate = "" + hasta.getTime(); // sdf.format(hasta);
		else
			if (fromDate!=null) toDate = "" + new Date().getTime();
		
		query += getRangeQueryClause(fromDate,toDate,luceneElemName);
		return query;
	}

	protected String getStateQueryClause(String[] values,String categoryName, String operator)
	{
		String clauseLucene = "";
		if (values!=null)
		{
			clauseLucene = categoryName + ":(";
			for (String value : values) {
				
				value = value.replace("active", Encuesta.ACTIVA);
				value = value.replace("closed", Encuesta.CERRADA);
				value = value.replace("unpublished", Encuesta.DESPUBLICADA);
				value = value.replace("inactive", Encuesta.INACTIVA);	

				clauseLucene += " " + value + operator ;
			}
			clauseLucene = clauseLucene.substring(0,clauseLucene.length()-operator.length());
			clauseLucene +=")";
			
			clauseLucene = " AND " + clauseLucene;
		}
		
		return clauseLucene;
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



}
