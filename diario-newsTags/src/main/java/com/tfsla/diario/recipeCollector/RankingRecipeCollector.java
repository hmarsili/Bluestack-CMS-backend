package com.tfsla.diario.recipeCollector;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.opencms.db.CmsResourceState;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import com.tfsla.diario.friendlyTags.TfsRecipeListTag;
import com.tfsla.diario.recipeCollector.order.OrderDirective;
import com.tfsla.diario.recipeCollector.order.ResultOrderManager;
import com.tfsla.planilla.herramientas.PlanillaFormConstants;
import com.tfsla.rankViews.model.TfsRankResults;
import com.tfsla.rankViews.service.RankService;
import com.tfsla.rankViews.service.TfsTokenHelper;
import com.tfsla.statistics.model.TfsHitPage;
import com.tfsla.statistics.model.TfsStatisticsOptions;

public class RankingRecipeCollector extends A_RecipeCollector {

	private static final Log LOG = CmsLog.getLog(RankingRecipeCollector.class);

	public RankingRecipeCollector()
	{
		supportedOrders.add(OrderDirective.ORDER_BY_MOSTCOMMENTED);
		supportedOrders.add(OrderDirective.ORDER_BY_MOSTREAD);
		supportedOrders.add(OrderDirective.ORDER_BY_MOSTRECOMMENDED);
		supportedOrders.add(OrderDirective.ORDER_BY_MOSTCOUNTERVALUED);
		supportedOrders.add(OrderDirective.ORDER_BY_MOSTNEGATIVEVALUED);
		supportedOrders.add(OrderDirective.ORDER_BY_MOSTPOSITIVEVALUED);
		supportedOrders.add(OrderDirective.ORDER_BY_MOSTVALUED);
		
		supportedOrders.add(OrderDirective.ORDER_BY_MOSTGENERALRANK);
		supportedOrders.add(OrderDirective.ORDER_BY_MOSTCUSTOM1);
		supportedOrders.add(OrderDirective.ORDER_BY_MOSTCUSTOM2);
		supportedOrders.add(OrderDirective.ORDER_BY_MOSTCUSTOM3);
		supportedOrders.add(OrderDirective.ORDER_BY_MOSTCUSTOM4);
		supportedOrders.add(OrderDirective.ORDER_BY_MOSTCUSTOM5);
		supportedOrders.add(OrderDirective.ORDER_BY_MOSTCUSTOM6);
		supportedOrders.add(OrderDirective.ORDER_BY_MOSTCUSTOM7);
		supportedOrders.add(OrderDirective.ORDER_BY_MOSTCUSTOM8);
		supportedOrders.add(OrderDirective.ORDER_BY_MOSTCUSTOM9);
		supportedOrders.add(OrderDirective.ORDER_BY_MOSTCUSTOM10);
	}
	
	@Override
	public boolean canCollect(Map<String, Object> parameters) {

		String order = (String)parameters.get(TfsRecipeListTag.param_order);
		
		if (this.paramValueIsMultivalued(order))
			return false;
		
		if (!this.canOrder(order))
			return false;
		
		return true;
	}

	@Override
	public List<String> collectRecipe(Map<String, Object> parameters, CmsObject cms) {
		LOG.debug("ingresando  a collect recipe");
		
		List<String> resources = new ArrayList<String>();

        TfsStatisticsOptions options = new TfsStatisticsOptions();

		String order = (String)parameters.get(TfsRecipeListTag.param_order);

		List<OrderDirective> orderby = ResultOrderManager.getOrderConfiguration(order);
		
		for (OrderDirective od : orderby)		
		{
			if (od.equals(OrderDirective.ORDER_BY_MOSTCOMMENTED)) {
		        options.setRankMode(TfsStatisticsOptions.RANK_COMENTARIOS);
		        options.setShowComentarios(true);
			}
			else if (od.equals(OrderDirective.ORDER_BY_MOSTREAD)) {
		        options.setRankMode(TfsStatisticsOptions.RANK_HITS);
		        options.setShowHits(true);
			}
			else if (od.equals(OrderDirective.ORDER_BY_MOSTRECOMMENDED)) {
		        options.setRankMode(TfsStatisticsOptions.RANK_RECOMENDACION);
		        options.setShowRecomendacion(true);
			}
			else if (od.equals(OrderDirective.ORDER_BY_MOSTVALUED)) {
		        options.setRankMode(TfsStatisticsOptions.RANK_VALORACIONES_PROMEDIO);
		        options.setShowValoracion(true);
		        options.setShowCantidadValoracion(true);
			}
			else if (od.equals(OrderDirective.ORDER_BY_MOSTPOSITIVEVALUED)) {
		        options.setRankMode(TfsStatisticsOptions.RANK_VALORACIONES_POSITIVO);
		        options.setShowValoracion(true);
		        options.setShowCantidadValoracion(true);
			}
			else if (od.equals(OrderDirective.ORDER_BY_MOSTNEGATIVEVALUED)) {
		        options.setRankMode(TfsStatisticsOptions.RANK_VALORACIONES_NEGATIVO);
		        options.setShowValoracion(true);
		        options.setShowCantidadValoracion(true);
			}
			else if (od.equals(OrderDirective.ORDER_BY_MOSTCOUNTERVALUED)) {
		        options.setRankMode(TfsStatisticsOptions.RANK_VALORACIONES_CANTIDAD);
		        options.setShowValoracion(true);
		        options.setShowCantidadValoracion(true);
			}
			else if (od.equals(OrderDirective.ORDER_BY_MOSTGENERALRANK)) {
		        options.setRankMode(TfsStatisticsOptions.RANK_GENERAL);
		        options.setShowGeneralRank(true);
			}
			else if (od.equals(OrderDirective.ORDER_BY_MOSTCUSTOM1)) {
		        options.setRankMode(TfsStatisticsOptions.RANK_CUSTOM1);
		        options.setShowCustom1(true);
			}
			else if (od.equals(OrderDirective.ORDER_BY_MOSTCUSTOM2)) {
		        options.setRankMode(TfsStatisticsOptions.RANK_CUSTOM2);
		        options.setShowCustom2(true);
		    }			
			else if (od.equals(OrderDirective.ORDER_BY_MOSTCUSTOM3)) {
		        options.setRankMode(TfsStatisticsOptions.RANK_CUSTOM3);
		        options.setShowCustom3(true);
		    }
			else if (od.equals(OrderDirective.ORDER_BY_MOSTCUSTOM4)) {
		        options.setRankMode(TfsStatisticsOptions.RANK_CUSTOM4);
		        options.setShowCustom4(true);
			}
			else if (od.equals(OrderDirective.ORDER_BY_MOSTCUSTOM5)) {
		        options.setRankMode(TfsStatisticsOptions.RANK_CUSTOM5);
		        options.setShowCustom5(true);
			}
			else if (od.equals(OrderDirective.ORDER_BY_MOSTCUSTOM6)) {
		        options.setRankMode(TfsStatisticsOptions.RANK_CUSTOM6);
		        options.setShowCustom6(true);
			}
			else if (od.equals(OrderDirective.ORDER_BY_MOSTCUSTOM7)) {
		        options.setRankMode(TfsStatisticsOptions.RANK_CUSTOM7);
		        options.setShowCustom7(true);
			}
			else if (od.equals(OrderDirective.ORDER_BY_MOSTCUSTOM8)) {
		        options.setRankMode(TfsStatisticsOptions.RANK_CUSTOM8);
		        options.setShowCustom8(true);
			}
			else if (od.equals(OrderDirective.ORDER_BY_MOSTCUSTOM9)) {
		        options.setRankMode(TfsStatisticsOptions.RANK_CUSTOM9);
		        options.setShowCustom9(true);
		    }
			else if (od.equals(OrderDirective.ORDER_BY_MOSTCUSTOM10)) {
		        options.setRankMode(TfsStatisticsOptions.RANK_CUSTOM10);
		        options.setShowCustom10(true);
		    }			
			int size = Integer.MAX_VALUE;
			if (parameters.get("size")!=null)
				size = (Integer)parameters.get(TfsRecipeListTag.param_size);
			
			int page = (Integer)parameters.get(TfsRecipeListTag.param_page);
			
			String[] categorias = getValues((String)parameters.get(TfsRecipeListTag.param_category));
			String[] autores = getValues((String)parameters.get(TfsRecipeListTag.param_author));
			String[] tipoEdicion = getValues((String)parameters.get(TfsRecipeListTag.param_publication));
			String[] edicion = getValues((String)parameters.get(TfsRecipeListTag.param_edition));
			
			String[] tags = getValues((String)parameters.get(TfsRecipeListTag.param_tags));
			
			String from = (String)parameters.get(TfsRecipeListTag.param_fromDate);
			String to = (String)parameters.get(TfsRecipeListTag.param_toDate);

			String[] ingrediente = getValues((String)parameters.get(TfsRecipeListTag.param_ingrediente));
			String[] tipoCocina = getValues((String)parameters.get(TfsRecipeListTag.param_tipoCocina));
			String[]  tipoCoccion = getValues((String)parameters.get(TfsRecipeListTag.param_tipoCoccion));
			
			//String age = (String)parameters.get(TfsRecipeListTag.param_age);

			CmsResourceFilter resourceFilter = (CmsResourceFilter) parameters.get(TfsRecipeListTag.param_resourcefilter);	
			if (resourceFilter == null)
				resourceFilter = CmsResourceFilter.DEFAULT;

			/*if (age!=null) {
				options.setFrom(parseDateTime(age));
			}*/
			
			if (from!=null)
			{	
				options.setCampoFechaRecurso("FECHAULTIMAMODIFICACION");
				//int horas = parseToHours(from);
				//options.setHoras(horas);
				
				options.setFromDateRecurso(parseDateTime(from));
				options.setToDateRecurso(new Date());
			}
			if (to!=null)
			{	
				options.setTo(new Date());
				//options.setTo(parseDateTime(to));
			}			

			if (edicion!=null) {
	        	options.setEdicion(Integer.parseInt(edicion[0]));
	        }
	        if (tipoEdicion!=null)
	        	options.setTipoEdicion(Integer.parseInt(tipoEdicion[0]));

	        	        
	        try {
				options.setTipoContenido("" + OpenCms.getResourceManager().getResourceType("receta").getTypeId());
	        } catch (CmsLoaderException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        
	        /*if (autores!=null) {
	        	try {
					options.setAutor(cms.readUser(autores[0]).getId().getStringValue());
				} catch (CmsException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        }*/
	        
	        String[] tags_categories = new String[ (tags!=null ? tags.length : 0 ) + (categorias!=null ? categorias.length : 0 ) +
	                                               (ingrediente!=null ? ingrediente.length : 0 ) +  (tipoCocina!=null ? tipoCocina.length : 0 )
	                                               +  (tipoCoccion!=null ? tipoCoccion.length : 0 )];
	        
	        int pos =0;
	        if (tags!=null)
	        	for (int j=0;j<tags.length;j++,pos++)
	        		tags_categories[pos] = TfsTokenHelper.convert(tags[j]);
	        if (categorias!=null)
	        	for (int j=0;j<categorias.length;j++,pos++)
	        		tags_categories[pos] = categorias[j];
	        if (ingrediente!=null)
	        	for (int j=0;j<ingrediente.length;j++,pos++)
	        		tags_categories[pos] = "ingr_" + ingrediente[j];
	        if (tipoCoccion!=null)
	        	for (int j=0;j<tipoCoccion.length;j++,pos++)
	        		tags_categories[pos] = "cooking_" + tipoCoccion[j];
	        if (tipoCocina!=null)
	        	for (int j=0;j<tipoCocina.length;j++,pos++)
	        		tags_categories[pos] = "cuisine_" + tipoCocina[j];
	    	LOG.debug( "primer elemento" +(tags_categories.length >0? tags_categories[0]:"no hay"));

	        options.setTags(tags_categories);
	        options.setPage(page);
	        options.setCount(size);

	    	LOG.debug("opciones configuradas / pasa a buscar las estadisticas");
	    	
	        
	        RankService rank = new RankService();
	        
	        TfsRankResults result = rank.getStatistics(cms,options);
	        
	        String[] estados = getValues((String)parameters.get(TfsRecipeListTag.param_state));
			if (estados==null || estados[0].trim().length()==0)
				estados = new String[] {PlanillaFormConstants.PUBLICADA_VALUE};
			
	        if (result!=null && result.getRank()!=null){
	        	 LOG.debug(result.getRank());
	  	       
	        	for (TfsHitPage masVisitado : result.getRank()) {
		
					try {
						CmsResource res = cms.readResource(cms.getRequestContext().removeSiteRoot(masVisitado.getURL()),resourceFilter);
						
						CmsResourceState estado = res.getState();
						String estadoStr = estado.toString();
						
						String resourceState = "";
						if (estadoStr.equals("0"))
							resourceState="publicada";
						
						/*CmsProperty prop = cms.readPropertyObject(res, "state", false);
						String resourceState = "";
						if (prop!=null)
							resourceState = prop.getValue("");
						LOG.debug("estado del recurso " + resourceState);
				  	    */   
						
						LOG.debug("estado del recurso " + resourceState);
						if (CmsResourceTypeXmlContent.isXmlContent(res) && ArrayUtils.contains(estados, resourceState)) {
							
							LOG.debug("recurso  " + cms.getRequestContext().removeSiteRoot(res.getRootPath()));
							resources.add(cms.getRequestContext().removeSiteRoot(res.getRootPath()));
						}
					}
					catch (Exception e) {
						LOG.error(e);
					}
				}
			}
		}
        
		return resources;
	}
}
