package com.tfsla.diario.ediciones.widgets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.PageContext;

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.list.A_CmsListDialog;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListOrderEnum;

//REMINDER: Para perfil comentar la siguiente linea y descomentar la que sigue:
import com.tfsla.diario.ediciones.data.PageDAO;
import com.tfsla.diario.ediciones.data.ZoneDAO;
import com.tfsla.diario.ediciones.model.Page;
import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.model.Zona;
import com.tfsla.diario.ediciones.services.TipoEdicionService;
import com.tfsla.diario.ediciones.services.ZonasService;
import com.tfsla.diario.ediciones.services.openCmsService;
import com.tfsla.opencmsdev.module.pages.PageConfiguration;
//import com.tfsla.perfil.module.pages.PageConfiguration;

//import com.tfsla.planilla.herramientas.ZonasPlanilla;

public class ZonasList extends A_CmsListDialog {

	public static final String LIST_ID = "lp";
	private static final String CAMPO_ORDEN = "IdentificadorZona";

	public static final String PATH_BUTTONS = "tools/Ediciones/buttons/";

	private static final String LIST_COLUMN_EDITAR = "Editar";
	private static final String LIST_ACTION_EDITAR = "EditarAction";

	private static final String LIST_COLUMN_BORRAR = "Borrar";
	private static final String LIST_ACTION_BORRAR = "BorrarAction";

	private String tipoEdicion = null;
	private String pagina = null;

	public ZonasList(CmsJspActionElement jsp) {
		super( jsp, LIST_ID, Messages.get().container(Messages.GUI_ZONAS_LIST_NAME_0), CAMPO_ORDEN,
			CmsListOrderEnum.ORDER_DESCENDING, null);
	}

	public ZonasList(PageContext context, HttpServletRequest req, HttpServletResponse res) {
		this(new CmsJspActionElement(context, req, res));

		getParameters(req);
	}

	@Override
	public void executeListMultiActions() throws IOException, ServletException,
			CmsRuntimeException {
		// TODO Auto-generated method stub

	}

	@Override
	public void executeListSingleActions() throws IOException,
		ServletException, CmsRuntimeException {

		Map<String, String> params = new HashMap<String, String>();

		String strZonaId = getSelectedItem().getId();

		params.put("zonaId",strZonaId);
		params.put(PARAM_ACTION,DIALOG_INITIAL);

		if (getParamListAction().equals(LIST_ACTION_EDITAR)) {
			// forward a la pagina de edicion
			getToolManager().jspForwardTool(this, "/Zonas/zonaEdicion", params);
		}
		if (getParamListAction().equals(LIST_ACTION_BORRAR)) {
			ZonasService zService = new ZonasService();
			zService.borrarZona(Integer.parseInt(strZonaId),getCms());
			if (zService.HasError()) {
				throw new RuntimeException(zService.getErrorDescription());
			}
			else {
				PageConfiguration.getInstance().reload();
//				ZonasPlanilla.getInstance().reload();
				super.refreshList();
			}
		}

		listSave();
	}


	private String addFilters()
	{
		String filterHTML="";
		filterHTML+=this.dialogBlock(CmsWorkplace.HTML_START, "Filtros", false);

		filterHTML+="<script type=\"text/javascript\">\n";

		filterHTML+="function filtrar()\n";
		filterHTML+="{\n";
		filterHTML+="	theForm = document.forms[\"lp-form\"];\n";
		filterHTML+="	theForm.submit();\n";
		filterHTML+="}\n";
		filterHTML+="</script>\n";

		filterHTML+="<table class='toolsArea' width='100%' cellspacing='0' cellpadding='0' border='0'>\n";
		filterHTML+="<tr><td>\n";


		filterHTML+="Publicacion:";
		filterHTML+="<select name=\"tipoEdicion\" >\n";

		TipoEdicionService tService = new TipoEdicionService();

		String proyecto = openCmsService.getCurrentSite(this.getCms());

		for (java.util.Iterator iter = tService.obtenerTipoEdiciones(proyecto).iterator(); iter.hasNext();) {
			TipoEdicion tEdicion = (TipoEdicion) iter.next();
			String selected = (tipoEdicion!=null && tipoEdicion.equals("" + tEdicion.getId())) ? "selected" : "";
			filterHTML+="	<option value=\"" + tEdicion.getId() + "\"" + selected + ">" + tEdicion.getDescripcion() + "</option>\n";

		}
		filterHTML+="</select>\n";

		filterHTML+="Pagina:";
		filterHTML+="<select name=\"pagina\" >\n";

		PageDAO pDAO = new PageDAO();

		try {

			for (java.util.Iterator iter = pDAO.getPages().iterator(); iter.hasNext();) {
				Page page = (Page) iter.next();
				String selected = (tipoEdicion!=null && pagina.equals("" + page.getIdPage())) ? "selected" : "";
				filterHTML+="	<option value=\"" + page.getIdPage() + "\"" + selected + ">" + page.getPageName() + "</option>\n";

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		filterHTML+="</select>\n";



		filterHTML+="<p>&nbsp;<br><center><table width='100' cellpadding='1' cellspacing='0' class='list'>\n";
		filterHTML+="<tr>\n";
		filterHTML+="<th>\n";
		filterHTML+="	<div class='screenTitle'>\n";
		filterHTML+="	<span class=\"link\" onClick=\"filtrar();\"><a href=\'#\'>Filtrar</a></span><div class='help' id='helplpFiltrar' onMouseOver=\"sMH('lpFiltrar');\" onMouseOut=\"hMH('lpFiltrar');\">Pulse aquí para ver la ediciones.</div>\n";
		filterHTML+="	</div>\n";
		filterHTML+="</th>\n";
		filterHTML+="</tr>\n";
		filterHTML+="</table></center>\n";


		filterHTML+=this.dialogBlock(CmsWorkplace.HTML_END, "Filtros", false);

		return filterHTML;
	}

	@Override
	protected String defaultActionHtmlContent() {
		String def = super.defaultActionHtmlContent();

		int posStart = def.indexOf("</form>");
		String newValue = def.substring(0,posStart) + addFilters() + def.substring(posStart);
		return newValue;
	}

	private void getParameters(HttpServletRequest req)
	{

		if (req.getParameter("tipoEdicion")!=null)
		{
			tipoEdicion = req.getParameter("tipoEdicion");
			pagina = req.getParameter("pagina");
		}
		if (tipoEdicion==null || tipoEdicion.length()==0)
		{
			TipoEdicionService tService = new TipoEdicionService();
			String proyecto = openCmsService.getCurrentSite(this.getCms());
			List<TipoEdicion> tEdiciones = tService.obtenerTipoEdiciones(proyecto);
			if (tEdiciones.size()>0)
			{
				TipoEdicion tEdicion = tEdiciones.get(0);
				tipoEdicion = "" + tEdicion.getId();
			}
			pagina="1";
		}

	}

	@Override
	protected void fillDetails(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	protected List getListItems() throws CmsException {
		List<CmsListItem> results = new ArrayList<CmsListItem>();
		ZoneDAO zoneDAO = new ZoneDAO();

		List<Zona> zonas=null;

		if (tipoEdicion!=null)
		{
			try {
				zonas = zoneDAO.getZonas(Integer.parseInt(tipoEdicion), Integer.parseInt(pagina));
			} catch (Exception e) {
				throw new CmsException(null);
			}

			for (Zona zona : zonas) {

				CmsListItem listItem = this.getList().newItem("" + zona.getIdZone());

				listItem.set("NombreZona",zona.getName());
				listItem.set("DescripcionZona",zona.getDescription());
				listItem.set("ColoZona",zona.getColor());
				listItem.set("OrderZona",zona.getOrder());
				listItem.set("visibilidadZona",zona.getVisibility());


				results.add(listItem);

			}
		}
		return results;

	}

	@Override
	protected void setColumns(CmsListMetadata metadata) {

		addColumn(metadata, "NombreZona", Messages.ZONAS_LIST_NOMBRE_COLUMN);
		addColumn(metadata, "DescripcionZona", Messages.ZONAS_LIST_DESCIPCION_COLUMN);
		addColumn(metadata, "ColoZona", Messages.ZONAS_LIST_COLOR_COLUMN);
		addColumn(metadata, "OrderZona", Messages.ZONAS_LIST_ORDEN_COLUMN);
		addColumn(metadata, "visibilidadZona", Messages.ZONAS_LIST_VISIBILIDAD_COLUMN);

		CmsListColumnDefinition editarColumn = addActionColumn(LIST_COLUMN_EDITAR,
			Messages.GUI_ZONAS_LIST_EDITAR_COLUMN_0);

		CmsListDirectAction editarAction = this.addDirectAction(new CmsListDirectAction(LIST_ACTION_EDITAR),
			Messages.GUI_ZONAS_LIST_ACTION_PUBLICAR_NAME_0, Messages.GUI_ZONAS_LIST_ACTION_EDITAR_HELP_0,
			"xmledit.png", Messages.GUI_ZONAS_LIST_ACTION_EDITAR_CONF_0);

		editarColumn.addDirectAction(editarAction);
		metadata.addColumn(editarColumn);

		CmsListColumnDefinition borrarColumn = addActionColumn(LIST_COLUMN_BORRAR,
				Messages.GUI_ZONAS_LIST_BORRAR_COLUMN_0);

		CmsListDirectAction borrarAction = this.addDirectAction(new CmsListDirectAction(LIST_ACTION_BORRAR),
				Messages.GUI_ZONAS_LIST_ACTION_BORRAR_NAME_0, Messages.GUI_ZONAS_LIST_ACTION_BORRAR_HELP_0,
				"cerrar.png", Messages.GUI_ZONAS_LIST_ACTION_BORRAR_CONF_0);

		borrarColumn.addDirectAction(borrarAction);
		metadata.addColumn(borrarColumn);

	}

	@Override
	protected void setIndependentActions(CmsListMetadata arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void setMultiActions(CmsListMetadata arg0) {
		// TODO Auto-generated method stub

	}

	private void addColumn(CmsListMetadata metadata, String columnID, String i18nKey) {
		CmsListColumnDefinition cmsListColumnDefinition = new CmsListColumnDefinition(columnID);
		cmsListColumnDefinition.setName(Messages.get().container(i18nKey));
		cmsListColumnDefinition.setTextWrapping(true);
		cmsListColumnDefinition.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
		metadata.addColumn(cmsListColumnDefinition);
	}

	private CmsListDirectAction addDirectAction(CmsListDirectAction directAction, String nameI18n, String helpI18n,
			String iconName, String confirmationMessage) {
		directAction.setName(Messages.get().container(nameI18n));
		directAction.setHelpText(Messages.get().container(helpI18n));
		directAction.setIconPath(PATH_BUTTONS + iconName);
		directAction.setConfirmationMessage(confirmationMessage == null ? null : Messages.get().container(
			confirmationMessage));

		return directAction;
	}

	private CmsListColumnDefinition addActionColumn(String columnID, String i18n) {
		CmsListColumnDefinition actionCol = new CmsListColumnDefinition(columnID);
		actionCol.setName(Messages.get().container(i18n));
		actionCol.setWidth("20");
		actionCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
		actionCol.setSorteable(false);
		return actionCol;
	}


}
