package com.tfsla.diario.personsCollector;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.opencms.file.CmsObject;

import com.tfsla.diario.newsCollector.comparators.CompositeComparator;
import com.tfsla.diario.newsCollector.A_Collector;
import com.tfsla.diario.terminos.model.Persons;
import com.tfsla.diario.personsCollector.order.OrderDirective;
import com.tfsla.diario.personsCollector.comparators.PropertyComparator;
import com.tfsla.diario.personsCollector.order.ResultOrderManager;

public abstract class A_PersonsCollector extends A_Collector{
protected List<OrderDirective> supportedOrders = new ArrayList<OrderDirective>();
	
	public abstract boolean canCollect(Map<String,Object> parameters);
	
	public abstract List<Persons> collectPersons(Map<String,Object> parameters , CmsObject cms);
	
	protected List<Persons> sortPersons(List<Persons> persons, Map<String, Object> parameters, CmsObject cms) {
		String order = (String)parameters.get("order");
		CompositeComparator<Persons> comp = new CompositeComparator<Persons>();
		
		List<OrderDirective> orderby = ResultOrderManager.getOrderConfiguration(order);
		
		for (OrderDirective od : orderby)		
			comp.addComparator(new PropertyComparator(cms,od.getPropertyName(),od.getType(),od.isAscending()));
		
		Collections.sort(persons, comp);
		
		return persons;
	
	}

	public boolean canOrder(String orderBy) {
		List<OrderDirective> ods = ResultOrderManager.getOrderConfiguration(orderBy);
	
		for (OrderDirective od : ods )
		{
			if (!supportedOrders.contains(od))
				return false;
		}
		return true;
	}

}
