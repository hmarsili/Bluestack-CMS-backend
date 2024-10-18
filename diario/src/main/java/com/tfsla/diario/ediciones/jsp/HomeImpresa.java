package com.tfsla.diario.ediciones.jsp;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.PageContext;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsRequestContext;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
//import org.opencms.site.CmsSiteManager;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.I_CmsXmlContentValue;

import com.tfsla.diario.ediciones.exceptions.UndefinedTipoEdicion;
import com.tfsla.diario.ediciones.model.Edicion;
import com.tfsla.diario.ediciones.model.Seccion;
import com.tfsla.diario.ediciones.services.EdicionService;
import com.tfsla.diario.ediciones.services.SeccionesService;

/**
 * Administra los datos de las paginas principales de una edicion impresa.
 * @author Victor Podberezski.
 *
 */
public class HomeImpresa extends CmsJspActionElement {

	protected Edicion edicion=null;
	protected Seccion seccion=null;

	protected boolean isIndexHome;

	protected List<Seccion> secciones=null;

	public HomeImpresa() {
		super();
	}

	public HomeImpresa(PageContext page, HttpServletRequest request, HttpServletResponse response) {
		super(page, request, response);

		EdicionService eService = new EdicionService();
		CmsRequestContext reqContext = getCmsObject().getRequestContext();
		String SeccionPage = reqContext.getUri().substring(reqContext.getUri().lastIndexOf("/") +1);

		String SeccionPageNoHTML = SeccionPage.replace(".html","");

		try {
			// Obtengo la edicion Impresa correspondiente
			edicion = eService.obtenerEdicionImpresa(this.getCmsObject(), reqContext.getUri());

			// Determino si es la pagina inicial o la de una seccion
			if (SeccionPage.toLowerCase().equals("index"))
				isIndexHome = true;
			else
			{
				// Otengo los datos de la seccion.
				isIndexHome = false;
				SeccionesService sService = new SeccionesService();
				seccion = sService.obtenerSeccionPorPagina(SeccionPage,edicion.getTipo());
				
				if (seccion==null)
					seccion = sService.obtenerSeccionPorPagina(SeccionPageNoHTML,edicion.getTipo());
					
			}

			// Obtengo las secciones disponibles.
			getSecciones();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void getSecciones() throws CmsException, UndefinedTipoEdicion
	{
		SeccionesService sService = new SeccionesService();

		String fileName= edicion.getbaseURL() + "homes/secciones.xml";
		fileName = fileName.replace(OpenCms.getSiteManager().getCurrentSite(getCmsObject()).getTitle(), "/");
		CmsFile contentFile = this.getCmsObject().readFile(fileName);
		CmsXmlContent content = CmsXmlContentFactory.unmarshal(this.getCmsObject(), contentFile);

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
				seccionName = value.getPlainText(getCmsObject());
				Seccion seccion = sService.obtenerSeccion(seccionName, edicion.getTipo());
				secciones.add(seccion);
			}
			else
			{
				seccionName = null;
			}
		}
	}

	/**
	 * Retorna la informacion de la edicion.
	 * @return Edicion
	 */
	public Edicion getEdicion() {
		return edicion;
	}

	/**
	 * @return True si la pagina es la home principal.
	 */
	public boolean isIndexHome() {
		return isIndexHome;
	}

	/**
	 * Obtiene la informacion de la seccion a la que pertenece la home.
	 * @return Seccion
	 */
	public Seccion getSeccion() {
		return seccion;
	}

	/**
	 * Obtiene la informacion de todas las secciones disponibles en la edicion.
	 * @return Lista de Secciones
	 */
	public List<Seccion> getAllSecciones()
	{
		return secciones;
	}

}
