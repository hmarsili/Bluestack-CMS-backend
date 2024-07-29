package com.tfsla.diario.usersCollector;

import java.rmi.RemoteException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsUUID;

import com.tfsla.diario.friendlyTags.TfsUserListTag;
import com.tfsla.diario.usersCollector.order.OrderDirective;
import com.tfsla.diario.usersCollector.order.ResultOrderManager;
import com.tfsla.rankUsers.model.TfsUserRankResults;
import com.tfsla.rankUsers.service.RankService;
import com.tfsla.statistics.model.TfsHitUser;
import com.tfsla.statistics.model.TfsUserStatsOptions;

public class RankingUsersCollector extends A_UsersCollector {

	private static final Log LOG = CmsLog.getLog(RankingUsersCollector.class);

	public RankingUsersCollector()
	{
		supportedOrders.add(OrderDirective.ORDER_BY_MOSTGENERALRANK);
		supportedOrders.add(OrderDirective.ORDER_BY_MOSTRECEIVEDVIEWS);
		supportedOrders.add(OrderDirective.ORDER_BY_MOSTPUBLICATEDNEWS);
		supportedOrders.add(OrderDirective.ORDER_BY_MOSTRECEIVEDRECOMENDATIONS);
		supportedOrders.add(OrderDirective.ORDER_BY_MOSTRECEIVEDCOMMENTS);
		supportedOrders.add(OrderDirective.ORDER_BY_MOSTMAKEDCOMMENTS);
		supportedOrders.add(OrderDirective.ORDER_BY_MOSTDECLINEDCOMMENTS);
		supportedOrders.add(OrderDirective.ORDER_BY_MOSTCOUNTVALORATIONS);
		supportedOrders.add(OrderDirective.ORDER_BY_MOSTAVERAGEVALORATIONS);
		supportedOrders.add(OrderDirective.ORDER_BY_MOSTPOSITIVEVALORATIONS);
		supportedOrders.add(OrderDirective.ORDER_BY_MOSTENEGATIVEVALORATIONS);
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

		String order = (String)parameters.get(TfsUserListTag.param_order);
		
		if (this.paramValueIsMultivalued(order))
			return false;
		
		if (!this.canOrder(order))
			return false;
		
		return true;
	}

	@Override
	public List<CmsUser> collectUsers(Map<String, Object> parameters, CmsObject cms) {	
		List<CmsUser> users = new ArrayList<CmsUser>();

		TfsUserStatsOptions options = new TfsUserStatsOptions();

		String order = (String)parameters.get(TfsUserListTag.param_order);

		List<OrderDirective> orderby = ResultOrderManager.getOrderConfiguration(order);
		
		for (OrderDirective od : orderby)		
		{
			
			if (od.equals(OrderDirective.ORDER_BY_MOSTRECEIVEDVIEWS)) {
		        options.setRankMode(TfsUserStatsOptions.RANK_VISITASRECIBIDAS);
		        options.setShowVisitasRecibidas(true);
			}
			else if (od.equals(OrderDirective.ORDER_BY_MOSTPUBLICATEDNEWS)) {
		        options.setRankMode(TfsUserStatsOptions.RANK_NOTASPUBLICADAS);
		        options.setShowNotasPublicadas(true);
			}
			else if (od.equals(OrderDirective.ORDER_BY_MOSTRECEIVEDCOMMENTS)) {
		        options.setRankMode(TfsUserStatsOptions.RANK_COMENTARIOSRECIBIDOS);
		        options.setShowComentariosRecibidos(true);
			}
			else if (od.equals(OrderDirective.ORDER_BY_MOSTRECEIVEDRECOMENDATIONS)) {
		        options.setRankMode(TfsUserStatsOptions.RANK_RECOMENDACIONESRECIBIDAS);
		        options.setShowRecomendacionesRecibidas(true);
		        //options.setShowCantidadValoracion(true);
			}
			else if (od.equals(OrderDirective.ORDER_BY_MOSTMAKEDCOMMENTS)) {
		        options.setRankMode(TfsUserStatsOptions.RANK_COMENTARIOSREALIZADOS);
		        options.setShowComentariosRealizados(true);
			}
			else if (od.equals(OrderDirective.ORDER_BY_MOSTDECLINEDCOMMENTS)) {
		        options.setRankMode(TfsUserStatsOptions.RANK_COMENTARIOSRECHAZADOS);
		        options.setShowComentariosRechazados(true);
			}
			else if (od.equals(OrderDirective.ORDER_BY_MOSTCOUNTVALORATIONS)) {
		        options.setRankMode(TfsUserStatsOptions.RANK_VALORACIONES_CANTIDAD);
		        options.setShowCantidadValoraciones(true);
		    }
			else if (od.equals(OrderDirective.ORDER_BY_MOSTAVERAGEVALORATIONS)) {
		        options.setRankMode(TfsUserStatsOptions.RANK_VALORACIONES_PROMEDIO);
		        options.setShowCantidadValoraciones(true);
		       // no hay una option del tipo? -->options.setShowValoracionesPromedio(true); 
			}
			else if (od.equals(OrderDirective.ORDER_BY_MOSTPOSITIVEVALORATIONS)) {
		        options.setRankMode(TfsUserStatsOptions.RANK_VALORACIONES_POSITIVO);
		        options.setShowCantidadValoraciones(true);
		      //  options.setShowValoracion(true);
		       // options.setShowCantidadValoracion(true);
			}
			else if (od.equals(OrderDirective.ORDER_BY_MOSTENEGATIVEVALORATIONS)) {
		        options.setRankMode(TfsUserStatsOptions.RANK_VALORACIONES_NEGATIVO);
		       // options.setShowValoracion(true);
		        options.setShowCantidadValoraciones(true);
		       
			}
			else if (od.equals(OrderDirective.ORDER_BY_MOSTGENERALRANK)) {
		        options.setRankMode(TfsUserStatsOptions.RANK_GENERAL);
		        options.setShowGeneralRank(true);
			}
			else if (od.equals(OrderDirective.ORDER_BY_MOSTCUSTOM1)) {
		        options.setRankMode(TfsUserStatsOptions.RANK_CUSTOM1);
		        options.setShowCustom1(true);
			}
			else if (od.equals(OrderDirective.ORDER_BY_MOSTCUSTOM2)) {
		        options.setRankMode(TfsUserStatsOptions.RANK_CUSTOM2);
		        options.setShowCustom2(true);
		    }			
			else if (od.equals(OrderDirective.ORDER_BY_MOSTCUSTOM3)) {
		        options.setRankMode(TfsUserStatsOptions.RANK_CUSTOM3);
		        options.setShowCustom3(true);
		    }
			else if (od.equals(OrderDirective.ORDER_BY_MOSTCUSTOM4)) {
		        options.setRankMode(TfsUserStatsOptions.RANK_CUSTOM4);
		        options.setShowCustom4(true);
			}
			else if (od.equals(OrderDirective.ORDER_BY_MOSTCUSTOM5)) {
		        options.setRankMode(TfsUserStatsOptions.RANK_CUSTOM5);
		        options.setShowCustom5(true);
			}
			else if (od.equals(OrderDirective.ORDER_BY_MOSTCUSTOM6)) {
		        options.setRankMode(TfsUserStatsOptions.RANK_CUSTOM6);
		        options.setShowCustom6(true);
			}
			else if (od.equals(OrderDirective.ORDER_BY_MOSTCUSTOM7)) {
		        options.setRankMode(TfsUserStatsOptions.RANK_CUSTOM7);
		        options.setShowCustom7(true);
			}
			else if (od.equals(OrderDirective.ORDER_BY_MOSTCUSTOM8)) {
		        options.setRankMode(TfsUserStatsOptions.RANK_CUSTOM8);
		        options.setShowCustom8(true);
			}
			else if (od.equals(OrderDirective.ORDER_BY_MOSTCUSTOM9)) {
		        options.setRankMode(TfsUserStatsOptions.RANK_CUSTOM9);
		        options.setShowCustom9(true);
		    }
			else if (od.equals(OrderDirective.ORDER_BY_MOSTCUSTOM10)) {
		        options.setRankMode(TfsUserStatsOptions.RANK_CUSTOM10);
		        options.setShowCustom10(true);
		    }			
			
			
			
			String[] grupo=getValues((String)parameters.get(TfsUserListTag.param_group));
			
			 if (grupo!=null && grupo.length>0)
		        	options.setGrupos(grupo);
		        else
		        	options.setGrupos(new String[0]);
			 String ou = (String)parameters.get(TfsUserListTag.param_ou);
			 if (ou!=null) {
					if (ou.toLowerCase().trim().equals("root")||ou.toLowerCase().trim().equals("/")||ou.toLowerCase().trim().equals("")){
						ou="";
					}
					else if (ou.toLowerCase().trim().equals("webuser")||ou.toLowerCase().trim().equals("/webuser/")){
						ou="webUser/";
					}
					else {
						if(!ou.trim().endsWith("/")){
							ou=ou+"/";
						}
						if(ou.trim().startsWith("/")){
							ou=ou.substring(1);
						}
			 		}
					options.setOu(ou);
				}
			String from = (String)parameters.get(TfsUserListTag.param_from);
			String to = (String)parameters.get(TfsUserListTag.param_to);
			
			if (from!=null)
			{					
				options.setFrom(parseDateTime(from));// parseDateTime(from));
			}
			if (to!=null)
			{	
				options.setTo(parseDateTime(to));
				if (from!=null)
					options.setTo(new Date());
			}
			int size = Integer.MAX_VALUE;
			if (parameters.get("size")!=null)
				if (!(parameters.get(TfsUserListTag.param_size).equals(0))){ 
					size = (Integer)parameters.get(TfsUserListTag.param_size);
					} else{
					//  si no esta definido traera el maximo valor
							size=Integer.MAX_VALUE;
							}
					
				
			
			int page = (Integer)parameters.get(TfsUserListTag.param_page);
			
	        options.setPage(page);
	        options.setCount(size);

	        RankService rank = new RankService();
	       
	        TfsUserRankResults result=null;
			try {
				result = rank.getStatistics(options);
			} catch (RemoteException e1) {
				LOG.error(e1);
			}
			//TfsRankResults result = rank.getStatistics(options);
        
			if (result!=null && result.getRank()!=null)
			{
				for (TfsHitUser masRankeado : result.getRank()) {
		
					try
					{
						CmsUser user =    cms.readUser(CmsUUID.valueOf(masRankeado.getUsuario())) ;
						
						
							users.add(user);
					}
					catch (Exception e) {
						LOG.error(e);
					}
				}
			}

				
		}
        
		return users;
	}
	
	protected Date parseDateTime(String value) {
		if (value==null)
			return null;
		
		if (value.matches("\\d{8}"))
		{
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			try {
				return sdf.parse(value);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	
		if (value.matches("\\d{4}-\\d{2}-\\d{2}")){
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			try {
				return sdf.parse(value);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (value.matches("\\d{4}/\\d{2}/\\d{2}")){
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
			try {
				return sdf.parse(value);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if (value.matches("\\d{2}/\\d{2}/\\d{4}")){
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
			try {
				return sdf.parse(value);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if (value.matches("\\d{2}-\\d{2}-\\d{4}")){
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
			try {
				return sdf.parse(value);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (value.matches("\\d{8}\\s\\d{4}"))
		{
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd hhmm");
			try {
				return sdf.parse(value);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	
		if (value.matches("\\d+h"))
		{
			value = value.replace("h", "");
			int hours = Integer.parseInt(value);
			
			Calendar cal = new GregorianCalendar();
			cal.add(Calendar.HOUR, -1* hours);
			return cal.getTime();
		}
	
		if (value.matches("\\d+d"))
		{
			value = value.replace("d", "");
			int days = Integer.parseInt(value);
			
			Calendar cal = new GregorianCalendar();
			cal.add(Calendar.DAY_OF_YEAR, -1* days);
			return cal.getTime();
		}
	
		if (value.matches("\\d+M"))
		{
			value = value.replace("M", "");
			int month = Integer.parseInt(value);
			
			Calendar cal = new GregorianCalendar();
			cal.add(Calendar.MONTH, -1* month);
			return cal.getTime();
		}
	
		if (value.matches("\\d+y"))
		{
			value = value.replace("y", "");
			int year = Integer.parseInt(value);
			
			Calendar cal = new GregorianCalendar();
			cal.add(Calendar.YEAR, -1* year);
			return cal.getTime();
		}
	
		return null;
	}


}
