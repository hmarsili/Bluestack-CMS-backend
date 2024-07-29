package com.tfsla.diario.ediciones.collector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.opencms.file.CmsDataAccessException;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.collectors.A_CmsResourceCollector;
import org.opencms.main.CmsException;
import org.opencms.xml.A_CmsXmlDocument;
import org.opencms.xml.content.CmsXmlContentFactory;

import com.tfsla.diario.ediciones.services.NoticiasService;

/**
 * Collector que obtiene las noticias disponibles de una edicion de una publicacion impresa.<br>
 * Se invoca bajo el nombre "NoticiasImpresas".<p>
 * Parametros posibles:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;- tipoEdicion : identificador de publicacion (Integer)<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;- numeroEdicion : numero de edicion de la publicacion (Integer)<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;- seccion : nombre de seccion a la que tienen que pertenecer las noticias (index para mostrar todas)<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;- filtro : (opcional) palabra clave para filtrar por el titulo.
 * @author Victor Podberezski
 *
 */
public class NoticiasImpresasCollector extends A_CmsResourceCollector {

	private int numeroEdicion=-1;
	private int tipoEdicion=-1;
	private String seccion=null;
	private String filtro=null;

	public List getCollectorNames() {
		List<String> nombres = new ArrayList<String>();
		nombres.add("NoticiasImpresas");
		return nombres;
	}

	public String getCreateLink(CmsObject cms, String collectorName,
			String param) throws CmsException, CmsDataAccessException {
		return getCreateInFolder(cms, param);
	}

	public String getCreateParam(CmsObject cms, String collectorName, String param) throws CmsDataAccessException {
		return param;
	}

	public List getResults(CmsObject cms, String collectorName, String param)
			throws CmsDataAccessException, CmsException {

		parseParam(param, cms);

		NoticiasService nService = new NoticiasService();

		List<String> urls=null;
		List<CmsResource> resources = new ArrayList<CmsResource>();

		try {
		if (seccion.equals("index"))

				urls=nService.obtenerPathNoticias(tipoEdicion, numeroEdicion);
		else
			urls=nService.obtenerPathNoticias(tipoEdicion, numeroEdicion,seccion);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return resources;
		}

		for (Iterator iter = urls.iterator(); iter.hasNext();) {
			String url = (String) iter.next();

			CmsResource res = cms.readResource(cms.getRequestContext().removeSiteRoot(url));
			if (!res.getRootPath().contains("~")) {
				if (filtro!=null)
				{
					CmsFile file = cms.readFile(res);
					A_CmsXmlDocument xmlContent = CmsXmlContentFactory.unmarshal(cms, file);

					String titulo = xmlContent.getStringValue(cms, "titulo", cms.getRequestContext().getLocale());
					if (titulo == null)
						titulo="";

					if (titulo!=null && filtro!=null && titulo.toLowerCase().contains(filtro.toLowerCase()))
						resources.add(res);
				}
				else {
					resources.add(res);
				}
			}
		}

		return resources;

	}

	private void parseParam(String params,CmsObject cms) throws RuntimeException, CmsException
	{
		String[] values = params.split("\\|");
		int i;
		seccion=null;
		filtro=null;
		for (i=0; i<values.length;i++)
		{
			String[] param = values[i].split(":");
			if (param[0].equalsIgnoreCase("seccion"))
			{
				seccion = param[1];
			}
			if (param[0].equalsIgnoreCase("filtro"))
			{
				filtro = param[1];
			}
			else if (param[0].equalsIgnoreCase("tipoEdicion"))
			{
				tipoEdicion = Integer.parseInt(param[1]);
			}
			else if (param[0].equalsIgnoreCase("numeroEdicion"))
			{
				numeroEdicion = Integer.parseInt(param[1]);
			}

		}
	}

}
