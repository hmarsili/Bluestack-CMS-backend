package com.tfsla.diario.admin.jobs;

//TODO: Terminar. Esta clase esta sin terminar.
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.OpenCmsBaseService;
import com.tfsla.diario.ediciones.services.TipoEdicionService;
import com.tfsla.diario.friendlyTags.TfsNoticiasListTag;
import com.tfsla.diario.newsCollector.LuceneNewsCollector;

import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.report.CmsLogReport;
import org.opencms.scheduler.I_CmsScheduledJob;
import org.opencms.xml.CmsXmlEntityResolver;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.I_CmsXmlContentValue;

public class QuitarNoticiasViejasDeHomeJob implements I_CmsScheduledJob {

	private String resultados = "";

	public String launch(CmsObject cms, Map parameters) throws Exception {
		resultados = "";
		
		List<CmsFile> resourcesToPublish = new ArrayList<CmsFile>();
		
		String publicacion = (String) parameters.get("publicacion");
		String zone = (String) parameters.get("zone");
		String where = (String) parameters.get("where");
		String to = (String) parameters.get("to");
		String _size = (String) parameters.get("size");

		Date hasta = parseDateTime(to);
		Date desde = parseDateTime("19000101");
			
		String zoneABuscar = "pub.online";
		
		if (where!=null) {
			if (where.equals("home"))
				zoneABuscar +=".zonahome";

			if (where.equals("section"))
				zoneABuscar +=".zonaseccion";
			
			if (zone!=null)
				zoneABuscar += "." + zone ;
		}
		
		String advancedfilter = 
			"hightraffic:(" + zoneABuscar + ")" + 
			" AND ultimaModificacion:[" + desde.getTime() + " TO " + hasta.getTime() + "]";
			
		int size = Integer.parseInt(_size);
		
		String searchindex= getHighTrafficLuceneIndexForSite(cms.getRequestContext().getSiteRoot(),publicacion,false);
		LuceneNewsCollector newsCollector = new LuceneNewsCollector();
		Map<String,Object> collectorParams = new HashMap<String,Object>();
		
		collectorParams.put(TfsNoticiasListTag.param_size,size);
		collectorParams.put(TfsNoticiasListTag.param_advancedFilter,advancedfilter);
		collectorParams.put(TfsNoticiasListTag.param_searchIndex,searchindex);
		collectorParams.put(TfsNoticiasListTag.param_numberOfParamters,3);

		List<CmsResource> noticias = newsCollector.collectNews(collectorParams, cms);
		
		for (CmsResource noticia : noticias) {
		
			CmsFile file = cms.readFile(noticia);
		CmsXmlContent xmlContent = CmsXmlContentFactory.unmarshal(cms, file);
		
		List locales = xmlContent.getLocales();
		if (locales.size() == 0) {
			locales = OpenCms.getLocaleManager().getDefaultLocales(cms,cms.getSitePath(file ));
		}

		Locale locale = OpenCms.getLocaleManager().getBestMatchingLocale(CmsLocaleManager.getLocale(""),
		OpenCms.getLocaleManager().getDefaultLocales(cms, cms.getSitePath(file )),locales);

		
			try {
		            xmlContent.validateXmlStructure(new CmsXmlEntityResolver(cms));
		        } catch (CmsXmlException eXml) {
		            		// validation failed, check the settings for handling the correction
		           		xmlContent.setAutoCorrectionEnabled(true);
		           		xmlContent.correctXmlStructure(cms);
		      
		        
		        }
	
			//TODO: quitar tambien de notas en zonas por multipublicacion y chequear si son notas compartidas no quitarlas de zonas no pedidas
			//TODO: no quitar si la noticia no es de la publicacion a purgar
			if (where==null) {
				xmlContent.getValue("zonahome",locale).setStringValue(cms, "no_mostrar");
				xmlContent.getValue("zonaseccion",locale).setStringValue(cms, "no_mostrar");
			}
			else if (where.equals("home"))
				xmlContent.getValue("zonahome",locale).setStringValue(cms, "no_mostrar");
			else if (where.equals("section"))
				xmlContent.getValue("zonaseccion",locale).setStringValue(cms, "no_mostrar");
			
			List<I_CmsXmlContentValue> values = xmlContent.getValues("publicaciones", locale);
			for (I_CmsXmlContentValue value : values) {
				String preffix = "publicaciones["  + (value.getIndex()+1) + "]/";
				
				String publicacionPortada = xmlContent.getStringValue(cms, preffix + "publicacion", locale);
			
				String proyecto = OpenCmsBaseService.getCurrentSite(cms);
				TipoEdicionService tService =  new TipoEdicionService();
				TipoEdicion tEdicion=tService.obtenerTipoEdicion(publicacionPortada,proyecto);
				
				if (tEdicion!= null) {
					String zonahome = xmlContent.getStringValue(cms, preffix + "zonahome", locale);
					String prioridadhome = xmlContent.getStringValue(cms, preffix + "prioridadhome", locale);
					String seccion = xmlContent.getStringValue(cms, preffix + "seccion", locale);
					String zonaseccion = xmlContent.getStringValue(cms, preffix + "zonaseccion", locale);
					String prioridadseccion = xmlContent.getStringValue(cms, preffix + "prioridadseccion", locale);
				}
			}
	
			file.setContents(xmlContent.marshal());
	
			cms.writeFile(file);
	
			if(!cms.getLock(file).isUnlocked())
				cms.unlockResource(cms.getSitePath(file));
			
			resourcesToPublish.add(cms.readFile(cms.getSitePath(file)));
		
		}
		
		publicarCambios(cms,resourcesToPublish);
		
		return resultados;
	}

	private String getHighTrafficLuceneIndexForSite(String siteName, String publicationName, boolean online)
	{
    	String module = "newsTags";
 		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();

		String paramName = (online ? "online" : "offline") + "luceneNewsIndexForSite";

		return config.getParam(siteName, publicationName, module, paramName, "");

	}

	public void publicarCambios(CmsObject cmsObject, List<CmsFile> resourcesToPublish) throws CmsException {
		resultados += "Publicando las noticias quitadas de la home.\n";
		
		OpenCms.getPublishManager().publishProject(cmsObject,
			new CmsLogReport(Locale.getDefault(), this.getClass()),
			OpenCms.getPublishManager().getPublishList(cmsObject,resourcesToPublish, false));
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
