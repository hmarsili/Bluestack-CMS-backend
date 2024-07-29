package com.tfsla.diario.newsCollector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsException;
import org.opencms.xml.A_CmsXmlDocument;
import org.opencms.xml.content.CmsXmlContentFactory;

import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.NoticiasService;
import com.tfsla.diario.ediciones.services.TipoEdicionService;
import com.tfsla.diario.friendlyTags.TfsNoticiasListTag;
import com.tfsla.diario.newsCollector.order.OrderDirective;

public class EdicionImpresaNewsCollector extends A_NewsCollector {

	public EdicionImpresaNewsCollector()
	{
		supportedOrders.add(OrderDirective.ORDER_BY_CREATIONDATE);
		supportedOrders.add(OrderDirective.ORDER_BY_MODIFICATIONDATE);
		supportedOrders.add(OrderDirective.ORDER_BY_SECTION);
		supportedOrders.add(OrderDirective.ORDER_BY_PRIORITY);
		supportedOrders.add(OrderDirective.ORDER_BY_USERMODIFICATIONDATE);
		supportedOrders.add(OrderDirective.ORDER_BY_ZONE);
		
	}
	
	@Override
	public boolean canCollect(Map<String, Object> parameters) {
		
		if (paramValueIsEmpty(parameters.get(TfsNoticiasListTag.param_section)))
			return false;

		if (paramValueIsEmpty(parameters.get(TfsNoticiasListTag.param_edition)))
			return false;
		if (paramValueIsMultivalued((String)parameters.get(TfsNoticiasListTag.param_edition)))
			return false;
		
		if (paramValueIsEmpty(parameters.get(TfsNoticiasListTag.param_publication)))
			return false;
		if (paramValueIsMultivalued((String)parameters.get(TfsNoticiasListTag.param_publication)))
			return false;

		
		int numParams = 3;

		//Solo usar si no se busca en paginas principales
		if (!paramValueIsEmpty(parameters.get(TfsNoticiasListTag.param_onmainpage)))
		{
			String[] values = getValues((String)parameters.get(TfsNoticiasListTag.param_onmainpage));
			for (String value : values)
				if (!value.trim().equals("anywhere"))
					return false;
			numParams++;
		}

		if (parameters.get(TfsNoticiasListTag.param_filter)!=null)
			numParams++;

		if (parameters.get(TfsNoticiasListTag.param_size)!=null)
			numParams++;
		
		if (parameters.get(TfsNoticiasListTag.param_page)!=null)
			numParams++;
		
		if (parameters.get(TfsNoticiasListTag.param_order)!=null)
			numParams++;

		if ((Integer)parameters.get(TfsNoticiasListTag.param_numberOfParamters)>numParams)
			return false;
		
		return true;
	}

	@Override
	public List<CmsResource> collectNews(Map<String, Object> parameters, CmsObject cms) {
		
		String seccion = (String)parameters.get(TfsNoticiasListTag.param_section);
		String filtro =  (String)parameters.get(TfsNoticiasListTag.param_filter);
		int tipoEdicion = Integer.parseInt((String)parameters.get(TfsNoticiasListTag.param_publication));

		CmsResourceFilter resourceFilter = (CmsResourceFilter) parameters.get(TfsNoticiasListTag.param_resourcefilter);	
		if (resourceFilter == null)
			resourceFilter = CmsResourceFilter.DEFAULT;
		
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
		
		NoticiasService nService = new NoticiasService();

		List<String> urls=new ArrayList<String>();

		try {
		if (seccion.equals("all"))
				urls=nService.obtenerPathNoticias(tipoEdicion, numeroEdicion);
		else
		{
			String[] secciones = getValues((String)parameters.get(TfsNoticiasListTag.param_section));
			for (String sec : secciones)
			{
				urls.addAll(nService.obtenerPathNoticias(tipoEdicion, numeroEdicion,sec));
			}
			
		}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		List<CmsResource> resources = new ArrayList<CmsResource>();

		for (Iterator iter = urls.iterator(); iter.hasNext();) {
			String url = (String) iter.next();

			CmsResource res;
			try {
				res = cms.readResource(cms.getRequestContext().removeSiteRoot(url),resourceFilter);
				if (!res.getRootPath().contains("~")) {
					if (filtro!=null)
					{
						CmsFile file = cms.readFile(res);
						A_CmsXmlDocument xmlContent = CmsXmlContentFactory.unmarshal(cms, file);
	
						String titulo = xmlContent.getStringValue(cms, "titulo", cms.getRequestContext().getLocale());
						if (titulo == null)
							titulo="";
	
						if (titulo.toLowerCase().contains(filtro.toLowerCase()))
							resources.add(res);
					}
					else {
						resources.add(res);
					}
				}
			
			} catch (CmsException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
}
