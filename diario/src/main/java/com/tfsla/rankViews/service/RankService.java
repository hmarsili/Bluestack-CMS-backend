package com.tfsla.rankViews.service;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.opencms.configuration.I_HitCounterService;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.site.CmsSite;
import org.opencms.util.CmsRequestUtil;

import com.tfsla.rankViews.model.TfsRankResults;
import com.tfsla.statistics.SoapConfig;
import com.tfsla.statistics.model.TfsHitPage;
import com.tfsla.statistics.model.TfsKeyValue;
import com.tfsla.statistics.model.TfsStatisticsOptions;
import com.tfsla.statistics.service.CachedHitsList;
import com.tfsla.statistics.service.DataCollectorManager;
import com.tfsla.statistics.service.I_statisticsDataCollector;

public class RankService extends TfsRankingService implements I_HitCounterService {


	private static final Log LOG = CmsLog.getLog(RankService.class);


	public void countHitView(String url,CmsObject cms, HttpSession session)
	{
		CmsResource res=null;
		try {
			res = cms.readResource(url);
		} catch (CmsException e) {
			LOG.error("Atencion! Error al obtener el recurso.",e);
		}

		countHitView(res,cms,session);
	}
	
	public void countHitView(CmsResource res,CmsObject cms, HttpSession session)
	{

		CmsUser user = cms.getRequestContext().currentUser();
		 

		String sitePath = getSiteName(cms);
		String siteName = sitePath.replaceFirst("/sites/", "");
	
		TfsHitPage page = new TfsHitPage();
		page.setCantidad(1);
		page.setComentarios(0);
		page.setRecomendacion(0);
		page.setValoracion(0);

		page.setURL(cms.getRequestContext().removeSiteRoot(res.getRootPath()));
		page.setSitio(siteName);
		page.setTipoContenido("" + res.getTypeId());
		String sessionId = session.getId();
		
		//page.setAutor(res.getUserCreated().getStringValue());
		
		TfsKeyValue[] values=null;
        DataCollectorManager dataCollectorManager = DataCollectorManager.getInstance();
		for (Iterator<I_statisticsDataCollector> it = dataCollectorManager.getDataCollectors().iterator();it.hasNext();)
		{
			I_statisticsDataCollector dCollector = (I_statisticsDataCollector) it.next(); 
			TfsKeyValue[] valuesCollector=null;
			try {
				valuesCollector = dCollector.collect(cms, res, user, sessionId, page);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			values = (TfsKeyValue[]) ArrayUtils.addAll(values, valuesCollector); 

		}
	
		//LOG.debug("tags de " + res.getRootPath() + ": " + (values!=null ? Arrays.toString(values) : ""));
		page.setValues(values);
		try {
			
			if (SoapConfig.getInstance().isUseCachedViewHits())
				CachedHitsList.getInstance().addHit(page);
			else
			{
				this.addHit(page);

			}
		} catch (RemoteException e) {
			LOG.fatal("Atencion! Error al intentar actualizar las estadisticas.",e);
		}
		        

	}

	public void countHitView(TfsHitPage page)
	{
		
		try {
			
			if (SoapConfig.getInstance().isUseCachedViewHits())
				CachedHitsList.getInstance().addHit(page);
			else
			{
				this.addHit(page);
			}
		} catch (RemoteException e) {
			LOG.fatal("Atencion! Error al intentar actualizar las estadisticas.",e);
		}
		        

	}

	public TfsRankResults getStatistics(CmsObject cms,TfsStatisticsOptions options)
	{
		Date from = roundDate(options.getFrom(), "m");
		Date to = roundDate(options.getTo(), "m");

		if (from!=null)
			options.setFrom(from);
		if (to!=null)
			options.setTo(to);

		if (options.getSitio()==null)
			options.setSitio(getSiteName(cms));
		else if (options.getSitio().trim().equals("*"))
			options.setSitio(null);

        try {

        	return this.getRankingStatistics(options);
		} catch (RemoteException e) {
			LOG.fatal("Error al obtener las estadisticas.",e);
		}
        return null;
	}
	
	public void addRecomendation(CmsResource res,CmsObject cms, HttpSession session)
	{
		TfsHitPage page = fillTfsHitPage(res, cms, session);

		page.setRecomendacion(1);

        try {
        	
        	this.addRecomendation(page);

		} catch (RemoteException e) {
			LOG.fatal("Error al agregar la recomendacion.",e);
		}
        
	}

	public void addComentario(CmsResource res,CmsObject cms, HttpSession session)
	{
		TfsHitPage page = fillTfsHitPage(res, cms, session);

		page.setComentarios(1);

        try {
        	this.addComentario(page);

		} catch (RemoteException e) {
			LOG.fatal("Error al agregar el comentario.",e);
		}
        
	}

	public void addValoracion(String url,CmsObject cms, HttpSession session, int valor)
	{
		
		//if (!url.contains("?")) {
			CmsResource res = null;
			try {
				res = cms.readResource(url);
				addValoracion(res,cms, session, valor);

			} catch (CmsException e1) {
				e1.printStackTrace();
			}
		//}
/*		else
		{
			I_statisticsDataCollector dCollector = DataCollectorManager.getInstance().getDataCollector(TfsRankComentarioDataCollector.class);

			String urlNoParams = url.substring(0, url.indexOf("?"));
			
			
			CmsResource res = null;
			try {
				res = cms.readResource(urlNoParams);

				
				if (res.getTypeId()==Integer.parseInt(dCollector.getContentType()))
				{
				
					Map<String, List<String>> params = parseParams(url);
					String cId = params.get("cId").get(0);

					addValoracionComentario(url,res, cId, cms, session, valor);
				}
				else
				{
					addValoracion(res,cms, session, valor);					
				}
			} catch (CmsException e1) {
				LOG.error("Error al agregar la valoración.",e1);
			} catch (UnsupportedEncodingException e) {
				LOG.error("Error al agregar la valoración.",e);
			}

			
		}
		*/
	}

	public void addValoracion(CmsResource res,CmsObject cms, HttpSession session, int valor)
	{
		TfsHitPage page = fillTfsHitPage(res, cms, session);

		page.setValoracion(valor);		

        try {
        	this.addValoracion(page);
        } catch (RemoteException e) {
			LOG.fatal("Error al agregar la valoración.",e);
		}
        
	}

	public void removeResourceFromStatistics(CmsResource res,CmsObject cms)
	{
		
        try {
            
			
			TfsStatisticsOptions options = new TfsStatisticsOptions();
			
			if (options.getSitio()==null)
				options.setSitio(getSiteName(cms));
			else if (options.getSitio().trim().equals("*"))
				options.setSitio(null);

			options.setUrl(cms.getRequestContext().removeSiteRoot(res.getRootPath()));
			
			this.removeResourceFromStatistics(options);
			
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private TfsHitPage fillTfsHitPage(CmsResource res, CmsObject cms, HttpSession session) {
		String siteName = getSiteName(cms);

		TfsHitPage page = new TfsHitPage();
		page.setURL(cms.getRequestContext().removeSiteRoot(res.getRootPath()));
		page.setSitio(siteName);
		page.setTipoContenido("" + res.getTypeId());
		
		page.setAutor(res.getUserCreated().getStringValue());

		
		collectInformation(cms,res,session,page);
		return page;
	}

	
	protected void collectInformation(CmsObject cms, CmsResource res, HttpSession session, TfsHitPage page)
	{

	    CmsUser user = cms.getRequestContext().currentUser();

	    
		TfsKeyValue[] values=null;
        DataCollectorManager dataCollectorManager = DataCollectorManager.getInstance();
		for (Iterator<I_statisticsDataCollector> it = dataCollectorManager.getDataCollectors().iterator();it.hasNext();)
		{
			I_statisticsDataCollector dCollector = (I_statisticsDataCollector) it.next(); 
			TfsKeyValue[] valuesCollector=null;
			try {
				String sessionId = "unknown";
				if (session!=null)
					
					sessionId = session.getId();
				valuesCollector = dCollector.collect(cms, res, user, sessionId, page);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			values = (TfsKeyValue[]) ArrayUtils.addAll(values, valuesCollector); 

		}
				
		page.setValues(values);

	}
	
	protected String getSiteName(CmsObject cms)
	{
		CmsSite site = OpenCms.getSiteManager().getCurrentSite(cms);
		String siteName = site.getSiteRoot(); 
		siteName = siteName.replace("/sites/", "");
		return siteName;
	}

    
	public void impedirNuevaValoracion(String resourceURL, CmsJspActionElement jsp, int valor)
	{
		CmsRequestUtil.setCookieValue(jsp, resourceURL, "" + valor);
	}

	public boolean puedeValorar(String resourceURL, CmsJspActionElement jsp)
	{
		return (CmsRequestUtil.getCookieValue(jsp, resourceURL)==null);
	}


	protected Map<String, List<String>> parseParams(String url) throws UnsupportedEncodingException
	{
		Map<String, List<String>> params = new HashMap<String, List<String>>(); 
		String[] urlParts = url.split("\\?"); 
		if (urlParts.length > 1) {
			String query = urlParts[1];     
			for (String param : query.split("&")) 
			{         
				String[] pair = param.split("=");
				String key = URLDecoder.decode(pair[0], "UTF-8");
				String value = URLDecoder.decode(pair[1], "UTF-8");
				List<String> values = params.get(key);
				if (values == null) {
					values = new ArrayList<String>();
					params.put(key, values);
				}
				values.add(value);
			} 
		}
		return params;
	}
	
	protected Date roundDate(Date date, String accuracy)
	{
		if (date==null)
			return null;
		
		long milliseconds = 0;
		if (accuracy==null || accuracy.trim().equals("s"))
		{
			milliseconds=1000;
		}
		else if (accuracy==null || accuracy.trim().equals("m"))
		{
			milliseconds=1000*60;
		}
		else if (accuracy==null || accuracy.trim().equals("h"))
		{
			milliseconds=1000*60*60;
		}
		else if (accuracy==null || accuracy.trim().equals("d"))
		{
			milliseconds=1000*60*60*24;
		}
		
		long currentTime = date.getTime(); 
		long roundedDate = (currentTime / milliseconds) * milliseconds; 
		
		return new Date(roundedDate);
	}

	public void addHitCustom(CmsResource res,CmsObject cms, HttpSession session, int nro, int value)
	{
		TfsHitPage page = fillTfsHitPage(res, cms, session);
		switch (nro)
		{
		case 1:
			page.setCustom1(value);
			break;
		case 2:
			page.setCustom2(value);
			break;
		case 3:
			page.setCustom3(value);
			break;
		case 4:
			page.setCustom4(value);
			break;
		case 5:
			page.setCustom5(value);
			break;
		case 6:
			page.setCustom6(value);
			break;
		case 7:
			page.setCustom7(value);
			break;
		case 8:
			page.setCustom8(value);
			break;
		case 9:
			page.setCustom9(value);
			break;
		case 10:
			page.setCustom10(value);
			break;
		}
	
        try {
        	
            this.AddCustomEvents(page);

		} catch (RemoteException e) {
			LOG.fatal("Error al intentar registrar el contador custom " + nro + " del usuario", e);
			e.printStackTrace();
		}

	}

	public void putTags(CmsResource resource, CmsObject cms) {
		CmsUser user = cms.getRequestContext().currentUser();
		 

		String sitePath = getSiteName(cms);
		String siteName = sitePath.replaceFirst("/sites/", "");
	
		TfsHitPage page = new TfsHitPage();

		page.setURL(cms.getRequestContext().removeSiteRoot(resource.getRootPath()));
		page.setSitio(siteName);
		page.setTipoContenido("" + resource.getTypeId());
		String sessionId = "";
		
		TfsKeyValue[] values=null;
        DataCollectorManager dataCollectorManager = DataCollectorManager.getInstance();
		for (Iterator<I_statisticsDataCollector> it = dataCollectorManager.getDataCollectors().iterator();it.hasNext();)
		{
			I_statisticsDataCollector dCollector = (I_statisticsDataCollector) it.next(); 
			TfsKeyValue[] valuesCollector=null;
			try {
				valuesCollector = dCollector.collect(cms, resource, user, sessionId, page);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			values = (TfsKeyValue[]) ArrayUtils.addAll(values, valuesCollector); 

		}
	
		page.setValues(values);
		try {
			
			this.putTags(page);
			
		} catch (RemoteException e) {
			LOG.fatal("Atencion! Error al intentar actualizar las estadisticas.",e);
		}
		        

		
	}


}
