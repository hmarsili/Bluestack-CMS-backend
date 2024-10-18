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
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.workplace.list.A_CmsListDialog;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListOrderEnum;
import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.model.TipoPublicacion;
import com.tfsla.diario.ediciones.services.TipoEdicionService;

public class TipoEdicionList extends A_CmsListDialog {


	public static final String LIST_ID = "lp";
	private static final String CAMPO_ORDEN = "recomendacion";

	public static final String PATH_BUTTONS = "tools/Ediciones/buttons/";

	private static final String LIST_COLUMN_EDITAR = "Editar";
	private static final String LIST_ACTION_EDITAR = "EditarAction";

	private static final String LIST_COLUMN_BORRAR = "Borrar";
	private static final String LIST_ACTION_BORRAR = "BorrarAction";



	public TipoEdicionList(CmsJspActionElement jsp) {
		super( jsp, LIST_ID, Messages.get().container(Messages.GUI_TIPOEDICION_LIST_NAME_0), CAMPO_ORDEN,
			CmsListOrderEnum.ORDER_DESCENDING, null);
	}

	public TipoEdicionList(PageContext context, HttpServletRequest req, HttpServletResponse res) {
		this(new CmsJspActionElement(context, req, res));

	}

	@Override
	public void executeListMultiActions() throws IOException, ServletException, CmsRuntimeException {
		// TODO Auto-generated method stub

	}

	@Override
	public void executeListSingleActions() throws IOException,
		ServletException, CmsRuntimeException {

		Map<String, String> params = new HashMap<String, String>();

		String strTipoEdicionId = getSelectedItem().getId();

		int id = Integer.parseInt(strTipoEdicionId);

		TipoEdicionService tServices = new TipoEdicionService();
		TipoEdicion tipoEdicion = tServices.obtenerTipoEdicion(id);

		params.put("tipoEdicionID",strTipoEdicionId);
		params.put(PARAM_ACTION,DIALOG_INITIAL);

		if (getParamListAction().equals(LIST_ACTION_EDITAR)) {
/*			
			if (!TipoPublicacion.getTipoPublicacionByCode(tipoEdicion.getTipoPublicacion()).equals(TipoPublicacion.EDICION_IMPRESA)) {
				CmsIllegalArgumentException err = new CmsIllegalArgumentException(
						Messages.get().container(
								"Sólo se pueden editar las publicaciones de tipo EDICION IMPRESA"
								)
						);
				throw err;
			}
*/			
			// forward a la pagina de edicion
			getToolManager().jspForwardTool(this, "/Publicaciones/suplementoEdicion", params);
		}
		if (getParamListAction().equals(LIST_ACTION_BORRAR)) {			
			
			tServices.borrarTipoEdicion(tipoEdicion, this.getCms());
			
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
		TipoEdicionService tService = new TipoEdicionService();

		List<TipoEdicion> tipoEdiciones =  null;

		try {
			tipoEdiciones = tService.obtenerTipoEdiciones();
		} catch (Exception e) {
			throw new CmsException(null);
		}

		for (TipoEdicion tEdicion : tipoEdiciones) {


			CmsListItem listItem = this.getList().newItem("" + tEdicion.getId());

			listItem.set("descripcion",tEdicion.getDescripcion());
			listItem.set("nombre",tEdicion.getNombre());
			listItem.set("url",tEdicion.getBaseURL());
			listItem.set("proyecto", tEdicion.getProyecto());
			listItem.set("tipoPublicacion", TipoPublicacion.getTipoPublicacionByCode(tEdicion.getTipoPublicacion()).getDescription());
			listItem.set("edicionActiva",tEdicion.getEdicionActiva());
			results.add(listItem);
		}

		return results;

	}

	@Override
	protected void setColumns(CmsListMetadata metadata) {
		addColumn(metadata, "descripcion", Messages.TIPOEDICION_LIST_DESCRIPCION_COLUMN);
		addColumn(metadata, "nombre", Messages.TIPOEDICION_LIST_NOMBRE_COLUMN);
		addColumn(metadata, "url", Messages.TIPOEDICION_LIST_URL_COLUMN);
		addColumn(metadata, "proyecto", Messages.TIPOEDICION_LIST_PROYECTO_COLUMN);
		addColumn(metadata, "tipoPublicacion", Messages.TIPOEDICION_LIST_TIPOPUBLICACION_COLUMN);
		addColumn(metadata, "edicionActiva", Messages.TIPOEDICION_LIST_EDICIONACTIVA_COLUMN);

		CmsListColumnDefinition editarColumn = addActionColumn(LIST_COLUMN_EDITAR,
				Messages.GUI_TIPOEDICIONES_LIST_EDITAR_COLUMN_0);

			CmsListDirectAction editarAction = this.addDirectAction(new CmsListDirectAction(LIST_ACTION_EDITAR),
				Messages.GUI_TIPOEDICIONES_LIST_ACTION_PUBLICAR_NAME_0, Messages.GUI_TIPOEDICIONES_LIST_ACTION_EDITAR_HELP_0,
				"xmledit.png", Messages.GUI_TIPOEDICIONES_LIST_ACTION_EDITAR_CONF_0);

			editarColumn.addDirectAction(editarAction);
			metadata.addColumn(editarColumn);

			CmsListColumnDefinition borrarColumn = addActionColumn(LIST_COLUMN_BORRAR,
					Messages.GUI_TIPOEDICIONES_LIST_BORRAR_COLUMN_0);

			CmsListDirectAction borrarAction = this.addDirectAction(new CmsListDirectAction(LIST_ACTION_BORRAR),
					Messages.GUI_TIPOEDICIONES_LIST_ACTION_BORRAR_NAME_0, Messages.GUI_TIPOEDICIONES_LIST_ACTION_BORRAR_HELP_0,
					"cerrar.png", Messages.GUI_TIPOEDICIONES_LIST_ACTION_BORRAR_CONF_0);

			borrarColumn.addDirectAction(borrarAction);
			metadata.addColumn(borrarColumn);

	}

	private void addColumn(CmsListMetadata metadata, String columnID, String i18nKey) {
		CmsListColumnDefinition cmsListColumnDefinition = new CmsListColumnDefinition(columnID);
		cmsListColumnDefinition.setName(Messages.get().container(i18nKey));
		cmsListColumnDefinition.setTextWrapping(true);
		cmsListColumnDefinition.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
		metadata.addColumn(cmsListColumnDefinition);
	}

	@Override
	protected void setIndependentActions(CmsListMetadata arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void setMultiActions(CmsListMetadata arg0) {
		// TODO Auto-generated method stub

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
		// actionCol.setHelpText(Messages.get().container(Messages.GUI_PROJECTS_LIST_COLS_FILES_HELP_0));
		actionCol.setWidth("20");
		actionCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
		actionCol.setSorteable(false);
		return actionCol;
	}

}