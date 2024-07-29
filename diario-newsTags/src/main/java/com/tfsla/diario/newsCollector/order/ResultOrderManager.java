package com.tfsla.diario.newsCollector.order;

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.lucene.document.SortedNumericDocValuesField;
import org.apache.lucene.document.SortedSetDocValuesField;
import org.apache.lucene.util.BytesRef;
import org.opencms.main.CmsLog;
import org.opencms.search.fields.CmsSearchField;
import org.opencms.search.fields.CmsSearchFieldConfiguration;

import com.tfsla.diario.newsCollector.order.OrderDirective;

public class ResultOrderManager {
	
	protected static final Log LOG = CmsLog.getLog(ResultOrderManager.class);
	
	public static List<OrderDirective> getOrderConfiguration(String order)
	{
		return getOrderConfiguration(order, false);
	}

	public static List<OrderDirective> getOrderConfiguration(String order, boolean allowCustomOrders, CmsSearchFieldConfiguration fieldConf) {
List<OrderDirective> orderBy = new ArrayList<OrderDirective>();
		
		if (order==null)
			return orderBy;
		
		String[] parts = order.split(",");
		for (String part : parts)
		{
			part = part.toLowerCase();
			
			boolean ascending = (part.contains(" asc"));
			boolean descending = (part.contains(" desc"));
			
			part = part.replace(" asc", "");
			part = part.replace(" desc", "");

			part = part.trim();
			
			if (OrderDirective.isValidOrder(part))
			{
				OrderDirective od = OrderDirective.getOrder(part);
				if (ascending)
					od.setAscending(true);
				if (descending)
					od.setAscending(false);
				orderBy.add(od);
			}
			//order "custom"
			else if (allowCustomOrders){
				String luceneName = "";
				String partLow = part.toLowerCase();
				
				int idxfinal = partLow.length();
				int idx = partLow.indexOf(" as ");
				if (idx!=-1 && idx <= idxfinal)
					idxfinal = idx;
				idx = partLow.indexOf(" asc");
				if (idx!=-1 && idx <= idxfinal)
					idxfinal = idx;
				idx = partLow.indexOf(" desc");
				if (idx!=-1 && idx <= idxfinal)
					idxfinal = idx;
				
				luceneName = part.substring(0, idxfinal).trim();
				
				String dataType = "";
				//String[] type = partLow.split(" as ");
				//String dataType = OrderDirective.TYPE_STRING;
				//if (type.length >1)
				//	dataType = OrderDirective.getDataType(type[1].toLowerCase());
				//else if (fieldConf!=null) {
					CmsSearchField field = fieldConf.getField(luceneName);
					if (field!=null) {
						String idxConfFieldType = field.getType();
						if (idxConfFieldType.equals(CmsSearchField.FIELD_TYPE_STRING)) {
							dataType = OrderDirective.TYPE_STRING;
            			 } 
            			else if (idxConfFieldType.equals(CmsSearchField.FIELD_TYPE_NUMERIC)) {
            				dataType = OrderDirective.TYPE_LONG;
            			}
            			else if (idxConfFieldType.equals(CmsSearchField.FIELD_TYPE_FLOAT)) {
            				dataType = OrderDirective.TYPE_FLOAT;
            			}
					}
					
				//}
				LOG.debug("ordenamiento 'custom': " + luceneName + "(" + dataType + ") - asc: " + ascending);
				OrderDirective od = new OrderDirective("custom","",luceneName,"",dataType,ascending);
				orderBy.add(od);
			}
		}
		
		
		return orderBy;

	}
	
	public static List<OrderDirective> getOrderConfiguration(String order, boolean allowCustomOrders)
	{
		return  getOrderConfiguration(order, allowCustomOrders, null);
	}
}
