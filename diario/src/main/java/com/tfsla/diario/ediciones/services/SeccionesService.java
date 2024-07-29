package com.tfsla.diario.ediciones.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.I_CmsXmlContentValue;

import com.tfsla.diario.ediciones.data.SeccionDAO;
import com.tfsla.diario.ediciones.exceptions.UndefinedTipoEdicion;
import com.tfsla.diario.ediciones.model.Edicion;
import com.tfsla.diario.ediciones.model.Seccion;
import com.tfsla.diario.ediciones.model.TipoEdicion;

public class SeccionesService extends baseService {

	public void crearSeccion(Seccion s,CmsObject obj)
	{
		SeccionDAO sDAO = new SeccionDAO();
		try {
			sDAO.insertSeccion(s);

			hasError = false;
		} catch (Exception e) {
			if (e.getMessage().contains("Duplicate entry"))
			{
				errorDescription = "ERR_DUPLICATE_SECCION_0";
				hasError = true;
			}
			else
				throw new RuntimeException(e);
		}
	}

	public void borrarSeccion(int seccionId,CmsObject obj)
	{
		SeccionDAO seccionDAO = new SeccionDAO();
		NoticiasService nService = new NoticiasService();
		SeccionDAO sDAO = new SeccionDAO();
		try {

			Seccion seccion = seccionDAO.getSeccion(seccionId);

			TipoEdicionService tService = new TipoEdicionService();
			TipoEdicion tEdicion = tService.obtenerTipoEdicion(seccion.getIdTipoEdicion());

			if (!nService.existenNoticiasEnSeccion(obj,seccion.getName(),tEdicion))
			{
				sDAO.deleteSeccion(seccionId);

				SeccionCache.getInstance().reset();
				hasError = false;
			}
			else {
				hasError = true;
				errorDescription = "Existen noticias cargadas en la seccion";
			}
		} catch (Exception e) {
			errorDescription = e.getMessage();
			hasError = true;
		}
	}

	public void actualizarSeccion(Seccion s)
	{
		SeccionDAO sDAO = new SeccionDAO();
		try {
			sDAO.updateSeccion(s);
			
			SeccionCache.getInstance().reset();
			hasError = false;
		} catch (Exception e) {
			errorDescription = e.getMessage();
			hasError = true;
		}
	}

	public Seccion obtenerSeccion(int seccionId)
	{
		
		Seccion s = SeccionCache.getInstance().getSeccion(seccionId);
		if (s!=null)
			return s;

		SeccionDAO sDAO = new SeccionDAO();
		try {
			s = sDAO.getSeccion(seccionId);
			SeccionCache.getInstance().putSeccion(s);
			hasError = false;
		} catch (Exception e) {
			errorDescription = e.getMessage();
			hasError = true;
		}
		
		
		return s;
	}

	public Seccion obtenerSeccion(String nombre, int idTipoEdicion)
	{
		Seccion s = SeccionCache.getInstance().getSeccion(idTipoEdicion,nombre);
		if (s!=null)
			return s;
		
		SeccionDAO sDAO = new SeccionDAO();
		try {
			s = sDAO.getSeccion(nombre,idTipoEdicion);
			SeccionCache.getInstance().putSeccion(s);
			hasError = false;
		} catch (Exception e) {
			errorDescription = e.getMessage();
			hasError = true;
		}
		return s;
	}

	public Seccion obtenerSeccionPorPagina(String pagina, int idTipoEdicion)
	{
		Seccion s = SeccionCache.getInstance().getSeccionByPage(idTipoEdicion,pagina);
		if (s!=null)
			return s;

		SeccionDAO sDAO = new SeccionDAO();
		try {
			s = sDAO.getSeccionByPage(pagina,idTipoEdicion);
			SeccionCache.getInstance().putSeccion(s);
			hasError = false;
		} catch (Exception e) {
			errorDescription = e.getMessage();
			hasError = true;
		}
		return s;
	}

	public List<Seccion> obtenerSeccionesDeEdicionImpresa(CmsObject cmsObject, int idTipoEdicion, int nroEdicion) throws UndefinedTipoEdicion, CmsException
	{
		List<Seccion> secciones=null;

		EdicionService eService = new EdicionService();
		Edicion edicion = eService.obtenerEdicion(idTipoEdicion, nroEdicion);
		
		String fileName= edicion.getbaseURL() + "homes/secciones.xml";
		fileName = fileName.replace(OpenCms.getSiteManager().getCurrentSite(cmsObject).getTitle(), "/");
		CmsFile contentFile = cmsObject.readFile(fileName);
		CmsXmlContent content = CmsXmlContentFactory.unmarshal(cmsObject, contentFile);

		String seccionName = "";
		int x=0;

		secciones = new ArrayList<Seccion>();
		while(seccionName!=null)
		{
			x++;

			String name = "seccion["+x+"]";
			I_CmsXmlContentValue value = content.getValue(name, Locale.ENGLISH);
			if(value!=null)
			{
				seccionName = value.getPlainText(cmsObject);
				Seccion seccion = obtenerSeccion(seccionName, edicion.getTipo());
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
