package com.tfsla.diario.usersCollector;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.i18n.CmsMessages;
import org.opencms.i18n.CmsMultiMessages;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.widgets.CmsCalendarWidget;

import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.TipoEdicionService;
import com.tfsla.diario.friendlyTags.TfsUserListTag;
import com.tfsla.diario.newsCollector.comparators.CompositeComparator;
import com.tfsla.diario.usersCollector.comparators.PropertyComparator;
import com.tfsla.diario.usersCollector.order.OrderDirective;
import com.tfsla.diario.usersCollector.order.ResultOrderManager;
import com.tfsla.opencms.webusers.SearchUsers;
import com.tfsla.opencms.webusers.params.A_Param;
import com.tfsla.opencms.webusers.params.FilterParam;
import com.tfsla.opencms.webusers.params.GroupParam;
import com.tfsla.opencms.webusers.params.RangePrimaryParams;
import com.tfsla.opencms.webusers.params.SimpleParams;
import com.tfsla.opencms.webusers.params.SimpleSecundaryParams;
public class DataUsersCollector extends A_UsersCollector {

	
	private static final Log LOG = CmsLog.getLog(DataUsersCollector.class);

	public DataUsersCollector()
	{
		supportedOrders.add(OrderDirective.ORDER_BY_NAME);
		supportedOrders.add(OrderDirective.ORDER_BY_EMAIL);
		//supportedOrders.add(OrderDirective.ORDER_BY_GROUP);
		supportedOrders.add(OrderDirective.ORDER_BY_OU);
	
		
	}

	@Override
	public boolean canCollect(Map<String, Object> parameters) {
		return true;
	}
	@Override
	protected List<CmsUser> sortUsers(List<CmsUser> users, Map<String, Object> parameters, CmsObject cms) {
		String order = (String)parameters.get(TfsUserListTag.param_order);
				
		CompositeComparator<CmsUser> comp = new CompositeComparator<CmsUser>();
		
		List<OrderDirective> orderby = ResultOrderManager.getOrderConfiguration(order);
		
		for (OrderDirective od : orderby)		
		{
							
			comp.addComparator(new PropertyComparator(cms,od.getPropertyName(),od.getType(),od.isAscending()));	
		}
		Collections.sort(users, comp);
		
		return users;
	}

	@Override
	public List<CmsUser> collectUsers(Map<String, Object> parameters, CmsObject cms) {
		return this.collectUsers(parameters, cms, false);
	}
	
	public List<CmsUser> collectUsers(Map<String, Object> parameters, CmsObject cms, Boolean andFilter) {
		 CmsMultiMessages m_messages = new CmsMultiMessages(cms.getRequestContext().getLocale());
		 CmsMessages messages = OpenCms.getWorkplaceManager().getMessages(cms.getRequestContext().getLocale());
	
		 m_messages.addMessages(messages);
		 DateFormat df = new SimpleDateFormat(CmsCalendarWidget.getCalendarJavaDateFormat(m_messages.key(org.opencms.workplace.Messages.GUI_CALENDAR_DATE_FORMAT_0)));
		 String siteName = OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot();
		 //cms.readProject(siteName);

		 TipoEdicionService tService = new TipoEdicionService();
		 TipoEdicion currentPublication=null;
		 try {
			 currentPublication = tService.obtenerEdicionOnlineRoot(siteName.substring(siteName.lastIndexOf("/")+1));
		 } catch (Exception e) {
			 LOG.error("al intentar traer la publicacion actual "+e.getCause());
		 }
		 String publication = "" + (currentPublication!=null?currentPublication.getId():"");
		 String moduleConfigName = "webusers";
		 CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
		
		 List<CmsUser> users = new ArrayList<CmsUser>();
		 List<A_Param> params = new ArrayList<A_Param>();
		
		 String grupo =(String) parameters.get(TfsUserListTag.param_group);
		 
		 if (grupo!=null) {
				String[] grupos=getValues(grupo);
				GroupParam groups = new GroupParam();
				for(String gp :grupos) {
					
					//obtener el id de grupo by name
					try {
						CmsGroup cgrp=cms.readGroup(gp);
						groups.addValue(cgrp.getId().getStringValue());
						
					} catch (CmsException e) {
						LOG.error("Error al tratar de obtener el id de grupo "+e.getMessage());
					}
				}
				if (groups.getValues().size()>0){
					params.add(groups);
				}
		}

		String ou = (String)parameters.get(TfsUserListTag.param_ou);
		if (ou!=null) {
			if (ou.toLowerCase().trim().equals("root")||ou.toLowerCase().trim().equals("/")){
				ou="/";
			}
			if (ou.toLowerCase().trim().equals("webuser")||ou.toLowerCase().trim().equals("/webuser/")){
				ou="/webUser/";
			}
			if(!ou.trim().endsWith("/")){
				ou=ou+"/";
			}
			if(!ou.trim().startsWith("/")){
				ou="/"+ou;
			}
			SimpleParams ous = new SimpleParams();
			ous.setValue(ou);
			ous.setName("USER_OU");
			params.add(ous);
		}

		String state = (String)parameters.get(TfsUserListTag.param_state);
		
		if (state!=null) {
			state=state.toLowerCase().trim();
			SimpleSecundaryParams pending = new SimpleSecundaryParams();
			pending.setName("USER_PENDING"); 
			SimpleParams enabled = new SimpleParams();
			enabled.setName("USER_FLAGS & 1");
			
			if (state.equals("active")) {
				pending.setValue("false");
				params.add(pending);	
				enabled.setValue(0);
				params.add(enabled);
			}
			else if (state.equals("block")) {
				enabled.setValue(1);
				params.add(enabled);
			}
			else if (state.equals("pending")) {
				pending.setValue("true");
				params.add(pending); 
				enabled.setValue(0);
				params.add(enabled);
			}
		}

		String desde =(String) parameters.get(TfsUserListTag.param_from);
		String hasta = (String)parameters.get(TfsUserListTag.param_to);
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
			
			RangePrimaryParams rangeCreated = new RangePrimaryParams();
			rangeCreated.setName("USER_DATECREATED");

			rangeCreated.setStartValue(from.getTime());
			rangeCreated.setEndValue(to.getTime());
			
			params.add(rangeCreated);
		}

		List<String> additionalInfo = 	config.getParamList(siteName, publication, moduleConfigName, "additionalInfo");
		
		final Iterator <Entry<String, Object>> it = parameters.entrySet().iterator();
		Entry<String, Object> entry= null;
		Object valor=null;
		String clave=null;
		while (it.hasNext()) {
			entry= it.next();
			clave = entry.getKey(); //clave
			valor = entry.getValue(); //valor
			
			if (clave.contains("param_userinfo_")) {
				SimpleSecundaryParams addParam = new SimpleSecundaryParams();
				addParam.setName(config.getItemGroupParam(siteName, publication, moduleConfigName, clave.substring(15), "entryname",clave.substring(15)));//(clave.substring(15)));
				String type = config.getItemGroupParam(siteName, publication, moduleConfigName, clave.substring(15), "type","string");//clave.substring(15));
				
				if (type.equals("string")) {
					addParam.setOperator("LIKE");
					valor =  "%" + valor + "%";
				} 
				
				addParam.setValue(valor.toString());
				params.add(addParam);
			}
		}
	
		String filter = (String)parameters.get(TfsUserListTag.param_filter);
		if (filter!=null) {
			FilterParam filterParam = new FilterParam();
			
			//nombre			
			SimpleParams filterName = new SimpleParams();
			filterName.setName("USER_NAME");
			filterName.setOperator("LIKE");
			filterName.setValue("%" + filter + "%");
			filterParam.addFilter(filterName);	
			
			//email
			SimpleParams filterEmail = new SimpleParams();
			filterEmail.setName("USER_EMAIL");
			filterEmail.setOperator("LIKE");
			filterEmail.setValue("%" + filter + "%");
			filterParam.addFilter(filterEmail);	
				
			//apodo
			SimpleSecundaryParams filterApodo = new SimpleSecundaryParams();
			filterApodo.setName("APODO");
				
			String type = config.getItemGroupParam(siteName, publication, moduleConfigName, "apodo", "type","string");
			
			if (type.equals("string")) {
				filterApodo.setOperator("LIKE");
				filter =  "%" + filter + "%";
			}
			filterApodo.setValue( filter);
			filterParam.addFilter(filterApodo);
			params.add(filterParam);
		}
		
		String order = (String)parameters.get(TfsUserListTag.param_order);
		List<OrderDirective> orderby = ResultOrderManager.getOrderConfiguration(order);
		String orderlist="";

		for(OrderDirective od : orderby) {
			
			if (additionalInfo.contains(od.getName())) {
				orderlist+= "DATA_VALUE " +(od.isAscending()?"ASC":"DESC") +",";
				SimpleParams filterExtra = new SimpleParams();
				filterExtra.setName("DATA_KEY");
				filterExtra.setValue(od.getPropertyName());
				params.add(filterExtra);
			} else {
				orderlist+=od.getPropertyName()+" " +(od.isAscending()?"ASC":"DESC") +",";
			}
		}
		if (orderlist.endsWith(",")) {
			orderlist=orderlist.substring(0, orderlist.length()-1);
		}
		
		//List<String> resultados=SearchUsers.getInstance(cms).Search(cms, ou, onlyActivos, filter, "", atributosadicionales, valoresadicionales, size, page);
		//caso en que todos los parametros son null, no hay campos aidicionales y definir el size por defecto
		int size = Integer.MAX_VALUE;
		if (parameters.get(TfsUserListTag.param_size)!=null) {
			if (!(parameters.get(TfsUserListTag.param_size).equals(0))) { 
				size = (Integer)parameters.get(TfsUserListTag.param_size);
			} else {
				if(params.isEmpty()&&(orderlist.equals(""))){
					size=0;
				}
			}
		}
		int page = (Integer)parameters.get(TfsUserListTag.param_page);
		users = SearchUsers.getInstance(cms).SearchPlus(cms, params, size, page, orderlist, andFilter);	
	
		return  users;
	}
}
