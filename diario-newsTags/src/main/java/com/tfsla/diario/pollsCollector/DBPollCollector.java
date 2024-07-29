package com.tfsla.diario.pollsCollector;

//TODO: Agregar sitio y publicacion.

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.opencms.file.CmsObject;

import com.tfsla.diario.ediciones.services.openCmsService;
import com.tfsla.diario.friendlyTags.TfsEncuestasBoxTag;
import com.tfsla.diario.pollsCollector.order.OrderDirective;
import com.tfsla.diario.pollsCollector.order.ResultOrderManager;
import com.tfsla.diario.pollsCollector.A_PollsCollector;
import com.tfsla.opencmsdev.encuestas.Encuesta;
import com.tfsla.opencmsdev.encuestas.ModuloEncuestas;

public class DBPollCollector extends A_PollsCollector {

	public DBPollCollector()
	{
		supportedOrders.add(OrderDirective.ORDER_BY_GROUP);
		supportedOrders.add(OrderDirective.ORDER_BY_STATUS);
		supportedOrders.add(OrderDirective.ORDER_BY_CLOSEDATE);
		
	}
	@Override
	public boolean canCollect(Map<String, Object> parameters) {
		int numParams = 0;
	
		if (parameters.get(TfsEncuestasBoxTag.param_publication)!=null)
			numParams++;

		if (parameters.get(TfsEncuestasBoxTag.param_state)!=null)
			numParams++;

		if (parameters.get(TfsEncuestasBoxTag.param_group)!=null)
			numParams++;

		if (parameters.get(TfsEncuestasBoxTag.param_size)!=null)
			numParams++;
		
		if (parameters.get(TfsEncuestasBoxTag.param_order)!=null)
			numParams++;
		
		if ((Integer)parameters.get(TfsEncuestasBoxTag.param_numberOfParamters)>numParams)
			return false;
		
		return true;
	}

	@Override
	public List<String> collectPolls(Map<String, Object> parameters,
			CmsObject cms) {
		
		List<String> encuestas = new ArrayList<String>();
		
		int size = Integer.MAX_VALUE;
		if (parameters.get("size")!=null)
			size = (Integer)parameters.get(TfsEncuestasBoxTag.param_size);

		String group = (String)parameters.get(TfsEncuestasBoxTag.param_group);
		
		String state = (String)parameters.get(TfsEncuestasBoxTag.param_state);

		String order = (String)parameters.get(TfsEncuestasBoxTag.param_order);

		String publications = (String)parameters.get(TfsEncuestasBoxTag.param_publication);

		String sitio = openCmsService.getCurrentSite(cms);
		
		String orden = null;		
		List<OrderDirective> orderby = ResultOrderManager.getOrderConfiguration(order);
		if (orderby!=null && orderby.size()>0) {
			orden = "";
			for (OrderDirective od : orderby)
				orden += ", " + od.getPropertyName() + " " + (od.isAscending() ? "ASC " : "DESC ") ;
					
					orden = orden.replaceFirst(", ", "");		
		} 	

		
		if (state!=null) {	    	
    		state = state.replace("active", Encuesta.ACTIVA);
    		state = state.replace("closed", Encuesta.CERRADA);
    		state = state.replace("unpublished", Encuesta.DESPUBLICADA);
    		state = state.replace("inactive", Encuesta.INACTIVA);	
		}
		
    	encuestas = (List<String>)ModuloEncuestas.getEncuestas(cms, sitio, publications, group, state, "" + size, orden);
		
		return encuestas;
	}

}
