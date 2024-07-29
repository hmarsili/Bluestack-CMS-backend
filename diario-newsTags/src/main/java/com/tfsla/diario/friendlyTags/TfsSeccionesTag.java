package com.tfsla.diario.friendlyTags;

import java.util.List;
import java.util.Scanner;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.opencms.file.CmsObject;
import org.opencms.flex.CmsFlexController;
import org.opencms.main.OpenCms;

import com.tfsla.diario.ediciones.data.SeccionDAO;
import com.tfsla.diario.ediciones.model.Edicion;
import com.tfsla.diario.ediciones.model.Seccion;
import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.model.TipoPublicacion;
import com.tfsla.diario.ediciones.services.EdicionService;
import com.tfsla.diario.ediciones.services.SeccionesService;
import com.tfsla.diario.ediciones.services.TipoEdicionService;
import com.tfsla.diario.utils.TfsXmlContentNameProvider;



public class TfsSeccionesTag extends BodyTagSupport implements I_TfsCollectionListTag {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4673878815235747767L;
	private String publication=null;
	private String edition= null;
	
	CmsObject cms = null;
	
	private List<Seccion> secciones=null;
	private int index = -1;
	
	public int doStartTag() throws JspException {
		
		index = -1;
		
	    cms = CmsFlexController.getCmsObject(pageContext.getRequest());

	    getSecciones();
	    
	    return (hasMoreContent() ? EVAL_BODY_INCLUDE : SKIP_BODY );
	}

	protected void getSecciones() {

		TipoEdicionService tEService = new TipoEdicionService();
		TipoEdicion tEdicion = null;
		
		EdicionService eService = new EdicionService();
		Edicion edicion = null;
		try {

			if (publication==null || publication.equals("current"))
					tEdicion = tEService.obtenerTipoEdicion(cms, cms.getRequestContext().getUri());
			else
			{
				Scanner scanner = new Scanner(publication);   
				if (scanner.hasNextInt())   
					tEdicion = tEService.obtenerTipoEdicion(scanner.nextInt()); 
				else
					tEdicion = tEService.obtenerTipoEdicion(cms, cms.getRequestContext().getUri());
			}

			if (tEdicion.getTipoPublicacion().equals(TipoPublicacion.EDICION_IMPRESA))
			{
				if (edition==null || edition.equals("current"))
					edicion = eService.obtenerEdicionImpresa(cms, cms.getRequestContext().getUri());
				else if (edition.equals("active"))
					edicion = eService.obtenerEdicion(tEdicion.getId() , tEdicion.getEdicionActiva());
				else {
						Scanner scanner = new Scanner(publication);   
						if (scanner.hasNextInt())   
							edicion = eService.obtenerEdicion(tEdicion.getId() ,scanner.nextInt()); 
						else
							edicion = eService.obtenerEdicionImpresa(cms, cms.getRequestContext().getUri());
				}
				
				SeccionesService sService = new SeccionesService();
				secciones = sService.obtenerSeccionesDeEdicionImpresa(cms, tEdicion.getId(), edicion.getNumero());
			}
			else {
				SeccionDAO seccionDAO = new SeccionDAO();
				secciones = seccionDAO.getSeccionesByTipoEdicionId(tEdicion.getId());
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public int getIndex() {
		return index;
	}

	public boolean isLast() {
		if (secciones==null)
			return true;
		
		return (index==secciones.size()-1);
	}

	@Override
	public int doEndTag() throws JspException {
			
		if (OpenCms.getSystemInfo().getServletContainerSettings().isReleaseTagsAfterEnd()) {
			release();
		}
		return super.doEndTag();
	}
	
	@Override
	public int doAfterBody() throws JspException {
		return (hasMoreContent() ? EVAL_BODY_AGAIN : SKIP_BODY );
	}
	
	public boolean hasMoreContent() throws JspException
	{
		
		index++;
				
		return (index<secciones.size());
	}
	
	public String getCollectionValue(String name) throws JspTagException {
		
		if (name.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.section")))
			return secciones.get(index).getName();
		
		if (name.equals(TfsXmlContentNameProvider.getInstance().getTagName("section.description")))
			return secciones.get(index).getDescription();

		if (name.equals(TfsXmlContentNameProvider.getInstance().getTagName("section.page")))
			return secciones.get(index).getPage();
		
		return "";
	}

	public String getCollectionIndexValue(String name, int index)
			throws JspTagException {
		return null;
	}

	public int getCollectionIndexSize(String name, boolean isCollectionPart) {
		// TODO Auto-generated method stub
		return 0;
	}
	public Seccion getCurrentSection()
	{
		return secciones.get(index);
	}
	
	public String getPublication() {
		return publication;
	}

	public void setPublication(String publication) {
		this.publication = publication;
	}

	public String getEdition() {
		return edition;
	}

	public void setEdition(String edition) {
		this.edition = edition;
	}

	public String getCollectionPathName() throws JspTagException {
		// TODO Auto-generated method stub
		return "";
	}
}
