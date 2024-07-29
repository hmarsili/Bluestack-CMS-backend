package com.tfsla.diario.ediciones.jsp;

import java.util.List;
//import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.opencms.file.CmsProperty;
import org.opencms.file.CmsRequestContext;
import org.opencms.file.CmsResource;
import org.opencms.jsp.CmsJspActionElement;
//import org.opencms.xml.content.CmsXmlContent;
//import org.opencms.xml.content.CmsXmlContentFactory;

import com.tfsla.diario.ediciones.model.Edicion;
import com.tfsla.diario.ediciones.model.Seccion;
import com.tfsla.diario.ediciones.services.EdicionService;
import com.tfsla.diario.ediciones.services.SeccionesService;

/**
 * Administra los datos de las notas de una edicion impresa.
 * @author Victor Podberezski.
 *
 */
public class NoticiaImpresa extends CmsJspActionElement {

	Edicion edicion=null;
	Seccion seccion=null;

	public NoticiaImpresa(PageContext page, HttpServletRequest request, HttpServletResponse response) {
		super(page, request, response);

		CmsRequestContext reqContext = getCmsObject().getRequestContext();

		EdicionService eService = new EdicionService();


		try {
			// Obtengo la edicion Impresa correspondiente
			edicion = eService.obtenerEdicionImpresa(this.getCmsObject(), reqContext.getUri());

			CmsResource resource = this.getCmsObject().readResource(this.getCmsObject().getRequestContext().removeSiteRoot(reqContext.getUri()));

			List properties = this.getCmsObject().readPropertyObjects(resource, true);
			String strSeccion = CmsProperty.get("seccion", properties).getValue();
			if (strSeccion == null)
				strSeccion="";

			SeccionesService sService = new SeccionesService();
			seccion = sService.obtenerSeccion(strSeccion,edicion.getTipo());

		} catch (Exception e) {
			e.printStackTrace();
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
	 * Obtiene la informacion de la seccion a la que pertenece la home.
	 * @return Seccion
	 */
	public Seccion getSeccion() {
		return seccion;
	}

}
