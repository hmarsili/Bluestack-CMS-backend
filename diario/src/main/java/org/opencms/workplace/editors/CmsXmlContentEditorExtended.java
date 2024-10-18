package org.opencms.workplace.editors;


import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.jsp.JspException;

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.workplace.CmsWorkplaceSettings;

import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.model.TipoPublicacion;
import com.tfsla.diario.ediciones.services.TipoEdicionService;

public class CmsXmlContentEditorExtended extends CmsXmlContentEditor {

	public static final String EDITOR_ACTION_SAVENEW = "saveNew";
	public static final int ACTION_SAVENEW = 160;

	public static final String PARAM_NEW_LINK = "newlink";

	protected boolean isSaveNew = false;

	public CmsXmlContentEditorExtended(CmsJspActionElement jsp) {
		super(jsp);
	}

	@Override
	protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {
		// TODO Auto-generated method stub
		super.initWorkplaceRequestValues(settings, request);
		if (isSaveNew) {
            setAction(ACTION_SAVENEW);
            setParamAction("saveNew");
		}
	}

	public String getParamNewLinkExt() throws Exception
	{
		String newLink = super.getParamNewLink();
		if (newLink==null)
		{
			TipoEdicionService tService = new TipoEdicionService();

			TipoEdicion tEdicion = tService.obtenerTipoEdicion(this.getCms(), getParamResource());

			String resource = this.getParamResource();

			String collector = "allInFolder";
			String fileName = resource.substring(0,resource.lastIndexOf("_")+1) + "${number}.html";

			if (tEdicion!=null && !TipoPublicacion.getTipoPublicacionByCode(tEdicion.getTipoPublicacion()).equals(TipoPublicacion.EDICION_IMPRESA))
			{
				collector = "Contenidos";
				fileName="/contenidos/noticia_${number}.html";
			}

			newLink = collector + "|" + fileName + "|50";
		}

		return newLink;
	}

	public void actionSaveNew() throws JspException, IOException, ServletException
	{
		actionSave();
		actionClear(false);
		//actionExit();
	}

	@Override
	public void fillParamValues(HttpServletRequest arg0) {
		super.fillParamValues(arg0);
		isSaveNew = (EDITOR_ACTION_SAVENEW.equals(getParamAction()));
		if (EDITOR_ACTION_SAVENEW.equals(getParamAction())) {
            setAction(ACTION_SAVE);
            setParamAction("save");
		}
	}

}
