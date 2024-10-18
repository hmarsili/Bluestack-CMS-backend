package com.tfsla.diario.friendlyTags;

import jakarta.servlet.jsp.JspException;

import org.opencms.file.CmsObject;
import org.opencms.flex.CmsFlexController;

import com.tfsla.diario.ediciones.services.SeccionesService;
import com.tfsla.diario.ediciones.services.TipoEdicionService;
import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.model.Seccion;
import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class TfsSectionDescriptionTag  extends A_TfsNoticiaCollectionValue {

    @Override
    public int doStartTag() throws JspException {

	    CmsObject cms = CmsFlexController.getCmsObject(pageContext.getRequest());

		 I_TfsCollectionListTag collection = getCurrentCollection();
		 String sectionDesc = TfsXmlContentNameProvider.getInstance().getTagName("section.description");
		 
		 String sectionDescription = null;
		 if (sectionDesc!=null)
			 sectionDescription = collection.getCollectionValue(sectionDesc); //seccion

		 if (sectionDescription!=null && !sectionDescription.equals(""))
			 printContent(sectionDescription);
		 else
		 {
			String seccionName = collection.getCollectionValue(TfsXmlContentNameProvider.getInstance().getTagName("news.section")); //seccion
	
			I_TfsNoticia noticia = getCurrentNews() ;
			
			SeccionesService sService = new SeccionesService();
	        
	        TipoEdicionService tService =  new TipoEdicionService();
	        
	        TipoEdicion tEdicion=null;        
	        
			try {
				tEdicion = tService.obtenerTipoEdicion(cms, cms.getSitePath(noticia.getXmlDocument().getFile()));
			} catch (Exception e) {
				LOG.error("Publication not found", e);
				return SKIP_BODY;
			}
	        Seccion seccion = sService.obtenerSeccion(seccionName, tEdicion.getId());
	        
	        if (seccion!=null)
	        	printContent(seccion.getDescription());

		 }
        return SKIP_BODY;
    }

}
