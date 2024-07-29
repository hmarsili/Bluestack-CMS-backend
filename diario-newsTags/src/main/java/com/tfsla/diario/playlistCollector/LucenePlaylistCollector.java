package com.tfsla.diario.playlistCollector;

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
import com.tfsla.diario.friendlyTags.TfsPlaylistListTag;
import com.tfsla.diario.friendlyTags.TfsVODListTag;
import com.tfsla.diario.playlistCollector.order.OrderDirective;
import com.tfsla.diario.playlistCollector.order.ResultOrderManager;


public class LucenePlaylistCollector extends A_playlistCollector {

protected static final Log LOG = CmsLog.getLog(LucenePlaylistCollector.class);
	
	final static String OnlineSerarchIndexName = "Generic1_PLAYLIST_ONLINE";
	final static String OfflineSerarchIndexName = "Generic1_PLAYLIST_OFFLINE";

	final static String OR_OPERATOR = " OR ";
	final static String AND_OPERATOR = " AND ";
	
	String serarchIndexName = OnlineSerarchIndexName;

	public LucenePlaylistCollector(){
		supportedOrders.add(OrderDirective.ORDER_BY_CATEGORY);
		supportedOrders.add(OrderDirective.ORDER_BY_CREATIONDATE);
		supportedOrders.add(OrderDirective.ORDER_BY_MODIFICATIONDATE);
		supportedOrders.add(OrderDirective.ORDER_BY_PRIORITY_ZONE);
		supportedOrders.add(OrderDirective.ORDER_BY_ZONE);

	}
	
	@Override
	public boolean canCollect(Map<String, Object> parameters) {
		
		return true;
	}

	@Override
	public List<String> collectPlaylist(Map<String, Object> parameters, CmsObject cms) {
		
		List<String> eventos = new ArrayList<String>();
		
		int size = Integer.MAX_VALUE;
		int page = 1;
		if (parameters.get("size")!=null)
			size = (Integer)parameters.get(TfsPlaylistListTag.param_size);
		if (parameters.get(TfsPlaylistListTag.param_page)!=null)
			page = (Integer)parameters.get(TfsPlaylistListTag.param_page);
		
	
		String[] categorias = getValues((String)parameters.get(TfsPlaylistListTag.param_category));
		String[] tags = getValues((String)parameters.get(TfsPlaylistListTag.param_tags));
		String[] personas = getValues((String)parameters.get(TfsPlaylistListTag.param_persons));
		
		String[] zonas = getValues((String)parameters.get(TfsVODListTag.param_zone));
		
		String searchIndex = (String)parameters.get(TfsPlaylistListTag.param_searchIndex);
		String advancedFilter = (String)parameters.get(TfsPlaylistListTag.param_advancedFilter);
		
		String publication = (String)parameters.get(TfsPlaylistListTag.param_publication);


		String title = (String)parameters.get(TfsPlaylistListTag.param_titulo);
		String longDescription = (String)parameters.get(TfsPlaylistListTag.param_cuerpo);
		String automatica = (String)parameters.get(TfsPlaylistListTag.param_automatica);
		
		
		String fromDate = (String)parameters.get(TfsPlaylistListTag.param_from);
		String toDate = (String)parameters.get(TfsPlaylistListTag.param_to);
		
		Boolean showtemporal = (Boolean)parameters.get(TfsPlaylistListTag.param_showtemporal);
		
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

		query += getCategoryQueryClause(categorias,"categoria",OR_OPERATOR);
		query += getQueryClause(zonas,"homezone",OR_OPERATOR);
		
		query += getQueryClause(tags,"keywords",OR_OPERATOR);
		query += getQueryClause(personas,"people",OR_OPERATOR);
		
		query += getQueryClause(title,"titulo",OR_OPERATOR);
		query += getQueryClause(longDescription,"cuerpo",OR_OPERATOR);
		query += getQueryClause(automatica,"cuerpo",OR_OPERATOR);
		
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
				serarchIndexName= tEdicion!=null ? tEdicion.getPlaylistIndex() : OnlineSerarchIndexName;
			else
				serarchIndexName= tEdicion!=null ? tEdicion.getPlaylistIndexOffline() : OfflineSerarchIndexName;
		}

		LOG.debug(this.getClass().getName() + "- index:" + serarchIndexName + " - query: '" + query + "'");
		adSearch.init(cms);
		adSearch.setMatchesPerPage(size);
		adSearch.setQuery(query);
		adSearch.setMaxResults(size*page+1);	
		adSearch.setPage(page);
		adSearch.setIndex(serarchIndexName);
		adSearch.setResFilter(CmsResourceFilter.ALL);
		
		String order = (String)parameters.get(TfsPlaylistListTag.param_order);
		
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

	


}
