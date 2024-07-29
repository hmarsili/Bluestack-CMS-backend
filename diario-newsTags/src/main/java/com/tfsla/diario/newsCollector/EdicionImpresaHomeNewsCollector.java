package com.tfsla.diario.newsCollector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.I_CmsXmlContentValue;

import com.tfsla.diario.ediciones.exceptions.UndefinedTipoEdicion;
import com.tfsla.diario.ediciones.model.Edicion;
import com.tfsla.diario.ediciones.model.Seccion;
import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.EdicionService;
import com.tfsla.diario.ediciones.services.SeccionesService;
import com.tfsla.diario.ediciones.services.TipoEdicionService;
import com.tfsla.diario.friendlyTags.TfsNoticiasListTag;
import com.tfsla.diario.newsCollector.comparators.CompositeComparator;
import com.tfsla.diario.newsCollector.comparators.MapComparator;
import com.tfsla.diario.newsCollector.comparators.PropertyComparator;
import com.tfsla.diario.newsCollector.order.OrderDirective;
import com.tfsla.diario.newsCollector.order.ResultOrderManager;

public class EdicionImpresaHomeNewsCollector  extends A_NewsCollector  {

	public EdicionImpresaHomeNewsCollector()
	{
		supportedOrders.add(OrderDirective.ORDER_BY_CREATIONDATE);
		supportedOrders.add(OrderDirective.ORDER_BY_MODIFICATIONDATE);
		supportedOrders.add(OrderDirective.ORDER_BY_SECTION);
		supportedOrders.add(OrderDirective.ORDER_BY_PRIORITY);
		supportedOrders.add(OrderDirective.ORDER_BY_USERMODIFICATIONDATE);
		supportedOrders.add(OrderDirective.ORDER_BY_ZONE);
		
	}

	CmsResourceFilter resourceFilter = null;
	
	Map<CmsResource,Integer> orderPr = new HashMap<CmsResource, Integer>();

	@Override
	public boolean canCollect(Map<String, Object> parameters) {

		if (paramValueIsEmpty(parameters.get(TfsNoticiasListTag.param_edition)))
			return false;
		if (paramValueIsMultivalued((String)parameters.get(TfsNoticiasListTag.param_edition)))
			return false;
		
		if (paramValueIsEmpty(parameters.get(TfsNoticiasListTag.param_publication)))
			return false;
		if (paramValueIsMultivalued((String)parameters.get(TfsNoticiasListTag.param_publication)))
			return false;

		if (paramValueIsEmpty(parameters.get(TfsNoticiasListTag.param_onmainpage)))
			return false;
		
		//Solo aplicar este collector si se debe tomar noticias de alguna homa de la edicion impresa (home o section).
		String[] values = getValues((String)parameters.get(TfsNoticiasListTag.param_onmainpage));
		if (values==null) return false;
		
		for (String value : values)
			if (value.trim().equals("anywhere") || value.trim().equals("none"))
				return false;
		
		int numParams = 3;

		if (parameters.get(TfsNoticiasListTag.param_size)!=null)
			numParams++;

		if (parameters.get(TfsNoticiasListTag.param_page)!=null)
			numParams++;
		
		if (parameters.get(TfsNoticiasListTag.param_order)!=null)
			numParams++;

		if (parameters.get(TfsNoticiasListTag.param_section)!=null)
			numParams++;

		if (parameters.get(TfsNoticiasListTag.param_zone)!=null)
			numParams++;

		if ((Integer)parameters.get(TfsNoticiasListTag.param_numberOfParamters)>numParams)
			return false;
		
		return true;

	}


	@Override
	public List<CmsResource> collectNews(Map<String, Object> parameters, CmsObject cms) {
		
		List<CmsResource> resources = new ArrayList<CmsResource>();

		String[] secciones = getValues((String)parameters.get(TfsNoticiasListTag.param_section));
		String[] zonas = getValues((String)parameters.get(TfsNoticiasListTag.param_zone));
		String[] paginas = getValues((String)parameters.get(TfsNoticiasListTag.param_onmainpage));

		resourceFilter = (CmsResourceFilter) parameters.get(TfsNoticiasListTag.param_resourcefilter);	
		if (resourceFilter == null)
			resourceFilter = CmsResourceFilter.DEFAULT;
		
		int tipoEdicion = Integer.parseInt((String)parameters.get(TfsNoticiasListTag.param_publication));

		TipoEdicionService tService = new TipoEdicionService();
		TipoEdicion tEdicion = tService.obtenerTipoEdicion(tipoEdicion);

		String sEdicion = (String)parameters.get(TfsNoticiasListTag.param_edition);
		int numeroEdicion = tEdicion.getEdicionActiva();
		
		if (!sEdicion.trim().equals("current"))
			numeroEdicion = Integer.parseInt((String)parameters.get(TfsNoticiasListTag.param_edition));
		
		int size = Integer.MAX_VALUE;
		
		if (parameters.get(TfsNoticiasListTag.param_size)!=null)
			size = (Integer)parameters.get(TfsNoticiasListTag.param_size);
		
		int page = (Integer)parameters.get(TfsNoticiasListTag.param_page);

		EdicionService eService = new EdicionService();

		Edicion edicion = eService.obtenerEdicion(tipoEdicion, numeroEdicion);

		if (edicion==null)
			return resources;

		if (tEdicion==null)
			return resources;

		edicion.setTipoEdicion(tEdicion);

		String basePath="";
		try {
			basePath = edicion.getbaseURL();
		} catch (UndefinedTipoEdicion e) {
			e.printStackTrace();
		}
		
		for (String pagina : paginas)
		{
			if (pagina.equals("home"))
			{
				String path = basePath + "homes/index.xml";
				List<CmsResource> noticias = getNoticias(cms, path, zonas, secciones);
				
				resources.addAll(noticias);
			}
			else if (pagina.equals("section"))
			{
				if (secciones!=null)
					for (String seccion: secciones)
					{
						if (seccion.equals("all"))
						{
							try {
								List<Seccion> seccionesEdicion = getSecciones(cms, edicion);
							
								for (Seccion sec : seccionesEdicion)
								{
									String path = basePath + "homes/" + sec.getName() + ".xml";
									List<CmsResource> noticias = getNoticias(cms, path, zonas,null);
									resources.addAll(noticias);

								}
							} catch (CmsException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (UndefinedTipoEdicion e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						else {
							String path = basePath + "homes/" + seccion + ".xml";
							List<CmsResource> noticias = getNoticias(cms, path, zonas,null);
							resources.addAll(noticias);
						}
					}
			}				
		}


		resources = sortNews(resources,parameters,cms);
		
		//pagino los resultados.
		int fromNews = (page-1)*size;
		int toNews = page*size - 1;
		
		if (fromNews>=resources.size())
			return Collections.emptyList();
		
		if (toNews>=resources.size())
			toNews = resources.size()-1;

		return resources.subList(fromNews, toNews+1);

		//return (size >= resources.size()) ? resources : resources.subList(0, size);


	}

	protected  List<CmsResource> getNoticias(CmsObject cms, String path, String[] zonas, String[] secciones)
	{
		List<CmsResource> resources = new ArrayList<CmsResource>();
		
		path = cms.getRequestContext().removeSiteRoot(path);
		try {
			CmsFile contentFile = cms.readFile(path,resourceFilter);
			CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, contentFile);

			if (zonas==null)
				resources.addAll(getAllNoticias(content, cms,secciones));
			else
				for (String zona: zonas)
				{
					if (!zona.equals("all"))
					{
						int positionZona = findZonaPosition(content,cms, zona);
						resources.addAll(getNoticias(content,cms,positionZona,secciones));
					}
					else
						resources.addAll(getAllNoticias(content, cms,secciones));
			}
			
		} catch (CmsException e) {
			e.printStackTrace();
		}
		
		return resources;
	}
	
	@Override
	protected List<CmsResource> sortNews(List<CmsResource> resources, Map<String, Object> parameters, CmsObject cms) {
		String order = (String)parameters.get(TfsNoticiasListTag.param_order);
		CompositeComparator<CmsResource> comp = new CompositeComparator<CmsResource>();
		
		List<OrderDirective> orderby = ResultOrderManager.getOrderConfiguration(order);
		
		for (OrderDirective od : orderby)		
		{
			if (od.equals(OrderDirective.ORDER_BY_PRIORITY))
				comp.addComparator(new MapComparator<Integer>(od.isAscending(),orderPr,MapComparator.INTEGER_COMPARATOR));
			else				
				comp.addComparator(new PropertyComparator(cms,od.getPropertyName(),od.getType(),od.isAscending()));
		}
		Collections.sort(resources, comp);
		
		return resources;

	}


	private List<CmsResource> getNoticias(CmsXmlContent content, CmsObject cms, int positionZona, String[] secciones) throws CmsException
	{
		List<CmsResource> resources = new ArrayList<CmsResource>();

		if (positionZona>0)
		{
			int nroNoticia = 0;
			String urlNoticia = "";
			while(urlNoticia!=null)
			{
				nroNoticia++;
				String name ="zona[" + positionZona + "]/noticia[" + nroNoticia + "]";

				I_CmsXmlContentValue value = content.getValue(name, Locale.ENGLISH);
				if(value!=null)
				{
					urlNoticia = value.getStringValue(cms);

					CmsResource res = cms.readResource(cms.getRequestContext().removeSiteRoot(urlNoticia));
					
					if (secciones!=null)
					{
						CmsProperty resSec = cms.readPropertyObject(res, "section", false);
						if (resSec!=null && resSec.getValue()!=null)
							for (String seccion: secciones)
							{
								if (resSec.getValue().equals(seccion))
								{
									resources.add(res);
									orderPr.put(res, nroNoticia);
									break;
								}
							}
					}
					else
					{
						resources.add(res);
						orderPr.put(res, nroNoticia);
					}
				}
				else
				{
					urlNoticia = null;
				}
			}
		}
		return resources;
	}

	private List<CmsResource> getAllNoticias(CmsXmlContent content, CmsObject cms, String[] secciones) throws CmsException
	{
		List<CmsResource> resources = new ArrayList<CmsResource>();

		int maxZonas = content.getIndexCount("zona", Locale.ENGLISH);
		
		int positionZona=0;
		
		while (positionZona<maxZonas)
		{
			int nroNoticia = 0;
			String urlNoticia = "";
			while(urlNoticia!=null)
			{
				nroNoticia++;
				String name ="zona[" + (positionZona+1) + "]/noticia[" + nroNoticia + "]";

				I_CmsXmlContentValue value = content.getValue(name, Locale.ENGLISH);
				if(value!=null)
				{
					urlNoticia = value.getStringValue(cms);

					CmsResource res = cms.readResource(cms.getRequestContext().removeSiteRoot(urlNoticia),resourceFilter);
					
					if (secciones!=null)
					{
						CmsProperty resSec = cms.readPropertyObject(res, "section", false);
						if (resSec!=null && resSec.getValue()!=null)
							for (String seccion: secciones)
							{
								if (resSec.getValue().equals(seccion))
								{
									resources.add(res);
									orderPr.put(res, nroNoticia);
									break;
								}
							}
					}
					else
					{
						resources.add(res);
						orderPr.put(res, nroNoticia);
					}
				}
				else
				{
					urlNoticia = null;
				}
			}
			positionZona++;
		}
		return resources;
	}

	private int findZonaPosition(CmsXmlContent content, CmsObject cms, String zona)
	{
		int x=0;
		boolean endProcess=false;

		while(endProcess==false)
		{
			x++;
			String name = "zona["+x+"]/nombre[1]";
			I_CmsXmlContentValue value = content.getValue(name, Locale.ENGLISH);
			if(value!=null)
			{
				String zonaName = value.getPlainText(cms);
				if (zonaName.equals(zona))
					endProcess = true;

			}
			else {
				endProcess = true;
				x=0;
			}
		}

		return x;
	}

	private List<Seccion> getSecciones(CmsObject cms, Edicion edicion) throws CmsException, UndefinedTipoEdicion
	{
		SeccionesService sService = new SeccionesService();

		String fileName= edicion.getbaseURL() + "homes/secciones.xml";
		fileName = fileName.replace(OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot(), "/");
		CmsFile contentFile = cms.readFile(fileName);
		CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, contentFile);

		String seccionName = "";
		int x=0;

		List<Seccion> secciones = new ArrayList<Seccion>();
		while(seccionName!=null)
		{
			x++;

			String name = "seccion["+x+"]";
			I_CmsXmlContentValue value = content.getValue(name, Locale.ENGLISH);
			if(value!=null)
			{
				seccionName = value.getPlainText(cms);
				Seccion seccion = sService.obtenerSeccion(seccionName, edicion.getTipo());
				secciones.add(seccion);
			}
			else
			{
				seccionName = null;
			}
		}
		
		return secciones;
	}

}
