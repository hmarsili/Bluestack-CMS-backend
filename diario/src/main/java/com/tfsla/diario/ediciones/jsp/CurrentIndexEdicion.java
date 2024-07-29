package com.tfsla.diario.ediciones.jsp;



import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.opencms.file.CmsRequestContext;
import org.opencms.jsp.CmsJspActionElement;

import com.tfsla.diario.ediciones.data.TipoEdicionesDAO;
import com.tfsla.diario.ediciones.model.Edicion;
import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.EdicionService;
import com.tfsla.diario.ediciones.services.OpenCmsBaseService;


/**
 * Redirecciona a la pagina de la home de la edicion activa de una publicacion impresa.
 * @author Victor Podberezski
 *
 */
public class CurrentIndexEdicion extends CmsJspActionElement {

	public CurrentIndexEdicion(PageContext page, HttpServletRequest request, HttpServletResponse response) {
		super(page, request, response);

		CmsRequestContext reqContext = getCmsObject().getRequestContext();
		String tipoEdicionStr = reqContext.getUri().substring(0,reqContext.getUri().lastIndexOf("/")).replaceAll("/","");

		TipoEdicionesDAO tDAO = new TipoEdicionesDAO();
		TipoEdicion tipoEdicion = null;
		try {
			String proyecto = OpenCmsBaseService.getCurrentSite(getCmsObject());
			tipoEdicion = tDAO.getTipoEdicion(tipoEdicionStr,proyecto);
			String path="";
			if (tipoEdicion.getEdicionActiva()!=0)
			{
				EdicionService eService = new EdicionService();

				Edicion edicionActiva = eService.obtenerEdicion(tipoEdicion.getId(), tipoEdicion.getEdicionActiva());
				edicionActiva.setTipoEdicion(tipoEdicion);
				path=tipoEdicion.getNombre() + "/" + edicionActiva.getbaseURL().replace(tipoEdicion.getBaseURL(), "");
			}
				response.sendRedirect("/" + path + "index.html" );
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
