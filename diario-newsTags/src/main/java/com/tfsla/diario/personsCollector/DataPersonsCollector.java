package com.tfsla.diario.personsCollector;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsLog;

import com.tfsla.diario.friendlyTags.TfsPersonsListTag;
import com.tfsla.diario.newsCollector.comparators.CompositeComparator;
import com.tfsla.diario.personsCollector.comparators.PropertyComparator;
import com.tfsla.diario.personsCollector.order.OrderDirective;
import com.tfsla.diario.personsCollector.order.ResultOrderManager;
import com.tfsla.diario.terminos.data.PersonsDAO;
import com.tfsla.diario.terminos.model.Persons;

public class DataPersonsCollector extends A_PersonsCollector {

	
	private static final Log LOG = CmsLog.getLog(DataPersonsCollector.class);

	public DataPersonsCollector()
	{
		supportedOrders.add(OrderDirective.ORDER_BY_NAME);
		supportedOrders.add(OrderDirective.ORDER_BY_EMAIL);
		supportedOrders.add(OrderDirective.ORDER_BY_NICKNAME);
	
		
	}

	@Override
	public boolean canCollect(Map<String, Object> parameters) {
		return true;
	}
	@Override
	protected List<Persons> sortPersons(List<Persons> persons, Map<String, Object> parameters, CmsObject cms) {
		String order = (String)parameters.get(TfsPersonsListTag.param_order);
				
		CompositeComparator<Persons> comp = new CompositeComparator<Persons>();
		
		List<OrderDirective> orderby = ResultOrderManager.getOrderConfiguration(order);
		
		for (OrderDirective od : orderby)		
		{
							
			comp.addComparator(new PropertyComparator(cms,od.getPropertyName(),od.getType(),od.isAscending()));	
		}
		Collections.sort(persons, comp);
		
		return persons;
	}

	@Override
	public List<Persons> collectPersons(Map<String, Object> parameters, CmsObject cms) {
		PersonsDAO peopledao= new PersonsDAO();
		
		List<Persons> persons = new ArrayList<Persons>();
	//	List<A_Param> params = new ArrayList<A_Param>();
		
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		
		String tipo =(String) parameters.get(TfsPersonsListTag.param_tipo);
		if (tipo!=null) {
			
				//String[] tipos=getValues(tipo);
				
		}else{
			tipo="";
		}

	
		String state = (String)parameters.get(TfsPersonsListTag.param_state);
		
		if (state!=null) {
			state=state.toLowerCase().trim();
			
			
			
		}

		String desde =(String) parameters.get(TfsPersonsListTag.param_from);
		String hasta = (String)parameters.get(TfsPersonsListTag.param_to);
		if (desde!=null && hasta!=null) {
			Date from;
			Date to;
			try {
				from = df.parse(desde);
			}
			catch (ParseException ex) {
				from = new Date(0);
			}
			try {
				to = df.parse(hasta);
			}
			catch (ParseException ex) {
				to = new Date();
			}
			
		}



	
		String filter = (String)parameters.get(TfsPersonsListTag.param_filter);
		if (filter!=null) {
			
				
			
		}else{
			filter="";
		}
		
		
		
		String order = (String)parameters.get(TfsPersonsListTag.param_order);
		
		List<OrderDirective> orderby = ResultOrderManager.getOrderConfiguration(order);
		
		
		String orderlist="";
		for(OrderDirective od:orderby){
			
			
				orderlist+=od.getPropertyName()+" " +(od.isAscending()?"ASC":"DESC") +",";
			
		}
		if (orderlist.endsWith(",")){
			orderlist=orderlist.substring(0, orderlist.length()-1);
		}
		if (order==null){
			order="";
		}
		
		int size = Integer.MAX_VALUE;
		if (parameters.get(TfsPersonsListTag.param_size)!=null){
			if (!(parameters.get(TfsPersonsListTag.param_size).equals(0))){ 
			size = (Integer)parameters.get(TfsPersonsListTag.param_size);
			} else{
				if((orderlist.equals(""))){
					size=0;
				}
			}
		}
		int page = (Integer)parameters.get(TfsPersonsListTag.param_page)!=null?(Integer)parameters.get(TfsPersonsListTag.param_page):0;
		/*String fecha = request.getParameter("fecha")!=null?request.getParameter("fecha"):"";
		String type = request.getParameter("type")!=null?request.getParameter("type"):"";
		String order = request.getParameter("order")!=null?request.getParameter("order"):"";
		
		String nacionalidad = request.getParameter("nacionalidad")!=null?request.getParameter("nacionalidad"):"";
		Integer cantidad =request.getParameter("cantidad")!=null?new Integer(request.getParameter("cantidad")):0;*/
		String nacionalidad="";
		String fechacumple="";
		Integer estado =state!=null?new Integer(state):new Integer(0);
			try {
				persons=peopledao.getPersonas(size,order,filter,fechacumple,tipo,nacionalidad,estado.intValue());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				LOG.error("Error al buscar personas- TfsPersonListTag"+e.getStackTrace());
			}
		return  persons;
	}
	
	
}
