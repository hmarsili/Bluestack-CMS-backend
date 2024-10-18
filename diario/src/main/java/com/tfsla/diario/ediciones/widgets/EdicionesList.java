package com.tfsla.diario.ediciones.widgets;

import java.io.IOException;
import java.text.SimpleDateFormat;
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
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.list.A_CmsListDialog;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListColumnDefinitionExclusiveActions;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListOrderEnum;

import com.tfsla.diario.ediciones.data.TipoEdicionesDAO;
import com.tfsla.diario.ediciones.exceptions.UndefinedTipoEdicion;
import com.tfsla.diario.ediciones.model.Edicion;
import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.EdicionService;
import com.tfsla.diario.ediciones.services.TipoEdicionService;
import com.tfsla.diario.ediciones.services.openCmsService;

public class EdicionesList extends A_CmsListDialog {


	public static final String LIST_ID = "lp";
	private static final String CAMPO_ORDEN = "numero";

	public static final String PATH_BUTTONS = "tools/Ediciones/buttons/";

	private static final String LIST_COLUMN_EDITAR = "Editar";
	private static final String LIST_ACTION_EDITAR = "EditarAction";

	private static final String LIST_COLUMN_BORRAR = "Borrar";
	private static final String LIST_ACTION_BORRAR = "BorrarAction";

	private static final String LIST_COLUMN_PUBLICAR = "Publicar";
	private static final String LIST_ACTION_PUBLICAR = "PublicarAction";

	private static final String LIST_COLUMN_ACTIVAR = "Activar";
	private static final String LIST_ACTION_ACTIVAR = "ActivarAction";
	private static final String LIST_ACTION_DESACTIVAR = "DesactivarAction";
	private String tipoEdicion = null;


	public EdicionesList(CmsJspActionElement jsp) {
		super( jsp, LIST_ID, Messages.get().container(Messages.GUI_EDICION_LIST_NAME_0), CAMPO_ORDEN,
			CmsListOrderEnum.ORDER_DESCENDING, null);
	}

	public EdicionesList(PageContext context, HttpServletRequest req, HttpServletResponse res) {
		this(new CmsJspActionElement(context, req, res));

		getParameters(req);
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

		String proyecto = openCmsService.getCurrentSite(this.getCms());
		
		TipoEdicionService tService = new TipoEdicionService();

		for (java.util.Iterator iter = tService.obtenerTipoEdicionesImpresas(proyecto).iterator(); iter.hasNext();) {
			TipoEdicion tEdicion = (TipoEdicion) iter.next();
			String selected = (tipoEdicion!=null && tipoEdicion.equals("" + tEdicion.getId())) ? "selected" : "";
			filterHTML+="	<option value=\"" + tEdicion.getId() + "\"" + selected + ">" + tEdicion.getDescripcion() + "</option>\n";

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

		}
		
		if (tipoEdicion==null || tipoEdicion.length()==0)
		{
			TipoEdicionService tService = new TipoEdicionService();
			String proyecto = openCmsService.getCurrentSite(this.getCms());
			//List<TipoEdicion> tEdiciones = tService.obtenerTipoEdiciones(proyecto);
			 
			List<TipoEdicion> tEdiciones = tService.obtenerTipoEdicionesImpresas(proyecto);
			
			if (tEdiciones.size()>0)
			{
				TipoEdicion tEdicion = tEdiciones.get(0);
				tipoEdicion = "" + tEdicion.getId();
			}
		}

	}

	@Override
	public void executeListMultiActions() throws IOException, ServletException, CmsRuntimeException {
		// TODO Auto-generated method stub
	}

	@Override
	public void executeListSingleActions() throws IOException,
		ServletException, CmsRuntimeException {

		Map<String, String> params = new HashMap<String, String>();

		String strEdicionId = getSelectedItem().getId();


		params.put("edicionID",strEdicionId);
		params.put(PARAM_ACTION,DIALOG_INITIAL);

		if (getParamListAction().equals(LIST_ACTION_EDITAR)) {
			// forward a la pagina de edicion
			getToolManager().jspForwardTool(this, "/Ediciones/edicionEdicion", params);
		}
		if (getParamListAction().equals(LIST_ACTION_BORRAR)) {
			String[] edicionID = strEdicionId.split(":");
			int tipo = Integer.parseInt(edicionID[0]);
			int numero = Integer.parseInt(edicionID[1]);

			Edicion edicion = new Edicion();
			edicion.setTipo(tipo);
			edicion.setNumero(numero);
			EdicionService eServices = new EdicionService();
			eServices.borrarEdicion(edicion, this.getCms());
			if (eServices.HasError())
				throw new RuntimeException(eServices.getErrorDescription());
			else {
				super.refreshList();
			}

		}
		if (getParamListAction().equals(LIST_ACTION_PUBLICAR)) {
			String[] edicionID = strEdicionId.split(":");
			int tipo = Integer.parseInt(edicionID[0]);
			int numero = Integer.parseInt(edicionID[1]);

			EdicionService eServices = new EdicionService();
			try {
				eServices.publicarEdicion(tipo, numero, this.getCms());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (eServices.HasError())
				throw new RuntimeException(eServices.getErrorDescription());
			else {
				super.refreshList();
			}

		}
		if (getParamListAction().equals(LIST_ACTION_ACTIVAR)) {
			String[] edicionID = strEdicionId.split(":");
			int tipo = Integer.parseInt(edicionID[0]);
			int numero = Integer.parseInt(edicionID[1]);

			TipoEdicionService tServices = new TipoEdicionService();
			try {
				tServices.establecerEdicionActiva(tipo, numero);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (tServices.HasError())
				throw new RuntimeException(tServices.getErrorDescription());
			else {
				super.refreshList();
			}

		}


		listSave();

	}

	@Override
	protected void fillDetails(String arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	protected List getListItems() throws CmsException {
		List<CmsListItem> results = new ArrayList<CmsListItem>();
		EdicionService eService = new EdicionService();

		List<Edicion> ediciones =  null;

		if (tipoEdicion!=null)
		{
			try {
				ediciones = eService.obtenerEdiciones(Integer.parseInt(tipoEdicion));

			} catch (Exception e) {
				e.printStackTrace();
				return results;
			}

			TipoEdicionesDAO tDAO = new TipoEdicionesDAO();

			try {
				TipoEdicion tEdicion = tDAO.getTipoEdicion(Integer.parseInt(tipoEdicion));
				for (Edicion edicion : ediciones) {

					edicion.setTipoEdicion(tEdicion);

					CmsListItem listItem = this.getList().newItem("" + edicion.getTipo() + ":" + edicion.getNumero());

					listItem.set("Edicion",edicion.getNumero());

					SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
					listItem.set("Fecha",sdf.format(edicion.getFecha()));
					listItem.set("Url",edicion.getbaseURL());
					listItem.set("EdicionActiva", tEdicion.getEdicionActiva());

					results.add(listItem);
				}
			} catch (CmsIllegalArgumentException e) {
				e.printStackTrace();
			} catch (UndefinedTipoEdicion e) {

				e.printStackTrace();
			} catch (NumberFormatException e) {

				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}


		return results;

	}


	@Override
	protected void setColumns(CmsListMetadata metadata) {
		addColumn(metadata, "Edicion", Messages.EDICION_LIST_EDICION_EDICION_COLUMN);
		addColumn(metadata, "Fecha", Messages.EDICION_LIST_FECHA_EDICION_COLUMN);
		addColumn(metadata, "Url", Messages.EDICION_LIST_URL_EDICION_COLUMN);
		addColumn(metadata, "EdicionActiva", Messages.EDICION_LIST_EDICION_EDICION_COLUMN,false);

		CmsListDirectAction activarAction = this.addDirectAction(new CmsListDirectAction(LIST_ACTION_ACTIVAR),
				Messages.GUI_EDICIONES_LIST_ACTION_ACTIVAR_NAME_0, Messages.GUI_EDICIONES_LIST_ACTION_ACTIVAR_HELP_0,
				"redButton.png", Messages.GUI_EDICIONES_LIST_ACTION_ACTIVAR_CONF_0);

		CmsListColumnDefinitionExclusiveActions activarColumn = addExclusiveActionColumn(LIST_COLUMN_ACTIVAR,
				Messages.GUI_EDICIONES_LIST_ACTIVAR_COLUMN_0);
		activarColumn.setOnAction(activarAction);
		activarColumn.setSubItemName("Edicion");
		activarColumn.setCompareSubItemName("EdicionActiva");

		CmsListDirectAction desaActivarAction = this.addDirectAction(new CmsListDirectAction(LIST_ACTION_DESACTIVAR),
				Messages.GUI_EDICIONES_LIST_ACTION_DESACTIVAR_NAME_0, Messages.GUI_EDICIONES_LIST_ACTION_DESACTIVAR_HELP_0,
				"greenButton.png", Messages.GUI_EDICIONES_LIST_ACTION_DESACTIVAR_CONF_0);

		activarColumn.setOffAction(desaActivarAction);
		metadata.addColumn(activarColumn);

		CmsListColumnDefinition editarColumn = addActionColumn(LIST_COLUMN_EDITAR,
				Messages.GUI_EDICIONES_LIST_EDITAR_COLUMN_0);

			CmsListDirectAction editarAction = this.addDirectAction(new CmsListDirectAction(LIST_ACTION_EDITAR),
				Messages.GUI_EDICIONES_LIST_ACTION_EDITAR_NAME_0, Messages.GUI_EDICIONES_LIST_ACTION_EDITAR_HELP_0,
				"xmledit.png", Messages.GUI_EDICIONES_LIST_ACTION_EDITAR_CONF_0);

			editarColumn.addDirectAction(editarAction);
			metadata.addColumn(editarColumn);

			CmsListColumnDefinition borrarColumn = addActionColumn(LIST_COLUMN_BORRAR,
					Messages.GUI_EDICIONES_LIST_BORRAR_COLUMN_0);

			CmsListDirectAction borrarAction = this.addDirectAction(new CmsListDirectAction(LIST_ACTION_BORRAR),
					Messages.GUI_EDICIONES_LIST_ACTION_BORRAR_NAME_0, Messages.GUI_EDICIONES_LIST_ACTION_BORRAR_HELP_0,
					"cerrar.png", Messages.GUI_EDICIONES_LIST_ACTION_BORRAR_CONF_0);

			borrarColumn.addDirectAction(borrarAction);
			metadata.addColumn(borrarColumn);

			CmsListColumnDefinition publicarColumn = addActionColumn(LIST_COLUMN_PUBLICAR,
					Messages.GUI_EDICIONES_LIST_PUBLICAR_COLUMN_0);

			CmsListDirectAction publicarAction = this.addDirectAction(new CmsListDirectAction(LIST_ACTION_PUBLICAR),
					Messages.GUI_EDICIONES_LIST_ACTION_PUBLICAR_NAME_0, Messages.GUI_EDICIONES_LIST_ACTION_PUBLICAR_HELP_0,
					"publish.png", Messages.GUI_EDICIONES_LIST_ACTION_PUBLICAR_CONF_0);

			publicarColumn.addDirectAction(publicarAction);
			metadata.addColumn(publicarColumn);

	}

	private void addColumn(CmsListMetadata metadata, String columnID, String i18nKey) {
		addColumn(metadata, columnID, i18nKey, true);
	}

	private void addColumn(CmsListMetadata metadata, String columnID, String i18nKey, boolean visible) {
		CmsListColumnDefinition cmsListColumnDefinition = new CmsListColumnDefinition(columnID);
		cmsListColumnDefinition.setName(Messages.get().container(i18nKey));
		cmsListColumnDefinition.setTextWrapping(true);
		cmsListColumnDefinition.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
		cmsListColumnDefinition.setVisible(visible);
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

	private CmsListColumnDefinitionExclusiveActions addExclusiveActionColumn(String columnID, String i18n) {
		CmsListColumnDefinitionExclusiveActions actionCol = new CmsListColumnDefinitionExclusiveActions(columnID);
		actionCol.setName(Messages.get().container(i18n));
		//actionCol.setHelpText(Messages.get().container(Messages.GUI_PROJECTS_LIST_COLS_FILES_HELP_0));
		actionCol.setWidth("20");
		actionCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
		actionCol.setSorteable(false);
		return actionCol;
	}

	private CmsListColumnDefinition addActionColumn(String columnID, String i18n) {
		CmsListColumnDefinition actionCol = new CmsListColumnDefinition(columnID);
		actionCol.setName(Messages.get().container(i18n));
		// actionCol.setHelpText(Messages.get().container(Messages.GUI_PROJECTS_LIST_COLS_FILES_HELP_0));
		actionCol.setWidth("20");
		actionCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
		actionCol.setSorteable(false);
		return actionCol;
	}


	@Override
	protected void setIndependentActions(CmsListMetadata arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void setMultiActions(CmsListMetadata arg0) {
		// TODO Auto-generated method stub

	}


	public String getTipoEdicion() {
		return tipoEdicion;
	}

	public void setTipoEdicion(String tipoEdicion) {
		this.tipoEdicion = tipoEdicion;
	}

}