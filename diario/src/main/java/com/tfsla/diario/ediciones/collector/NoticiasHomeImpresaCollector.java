package com.tfsla.diario.ediciones.collector;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.opencms.file.CmsDataAccessException;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.collectors.A_CmsResourceCollector;
import org.opencms.main.CmsException;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.I_CmsXmlContentValue;

import com.tfsla.diario.ediciones.exceptions.UndefinedTipoEdicion;
import com.tfsla.diario.ediciones.model.Edicion;
import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.EdicionService;
import com.tfsla.diario.ediciones.services.TipoEdicionService;

public class NoticiasHomeImpresaCollector extends A_CmsResourceCollector {

	private int numeroEdicion=-1;
	private int tipoEdicion=-1;
	private String zona=null;
	private String seccion=null;

	public List getCollectorNames() {
		List<String> nombres = new ArrayList<String>();
		nombres.add("NoticiasHomeImpresa");
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

		EdicionService eService = new EdicionService();
		TipoEdicionService tService = new TipoEdicionService();

		Edicion edicion = eService.obtenerEdicion(tipoEdicion, numeroEdicion);
		TipoEdicion tEdicion = tService.obtenerTipoEdicion(tipoEdicion);

		edicion.setTipoEdicion(tEdicion);

		String path="";
		try {
			path = edicion.getbaseURL();
		} catch (UndefinedTipoEdicion e) {
			e.printStackTrace();
		}
		path += "homes/" + seccion + ".xml";


		path = cms.getRequestContext().removeSiteRoot(path);
		CmsFile contentFile = cms.readFile(path);
		CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, contentFile);

		int positionZona = findZonaPosition(content,cms);

		return getNoticias(content,cms,positionZona);
	}

	private List<CmsResource> getNoticias(CmsXmlContent content, CmsObject cms, int positionZona) throws CmsException
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
					resources.add(res);
				}
				else
				{
					urlNoticia = null;
				}
			}
		}
		return resources;
	}

	private int findZonaPosition(CmsXmlContent content, CmsObject cms)
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

	private void parseParam(String params,CmsObject cms) throws RuntimeException, CmsException
	{
		String[] values = params.split("\\|");
		int i;
		seccion=null;
		for (i=0; i<values.length;i++)
		{
			String[] param = values[i].split(":");
			if (param[0].equalsIgnoreCase("seccion"))
			{
				seccion = param[1];
			}
			else if (param[0].equalsIgnoreCase("zona"))
			{
				zona = param[1];
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
