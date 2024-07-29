package com.tfsla.diario.facebook;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsRuntimeException;

import org.opencms.workplace.list.A_CmsListDialog;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListOrderEnum;


public class FacebookPublishersListWidget extends A_CmsListDialog {

	public static final String LIST_ID = "lp";
	public static final String LIST_COLUMN_NAME = "cn";
	public static final String PATH_BUTTONS = "tools/Facebook/buttons/";

	private static final String CAMPO_ORDEN = "name";

	//private static final String LIST_COLUMN_EDITAR = "Editar";
	//private static final String LIST_ACTION_EDITAR = "EditarAction";

	private static final String LIST_COLUMN_BORRAR = "Borrar";
	private static final String LIST_ACTION_BORRAR = "BorrarAction";

	public FacebookPublishersListWidget(CmsJspActionElement jsp) {
		super( jsp, LIST_ID, Messages.get().container(Messages.GUI_FACEBOOK_LIST_NAME_0), CAMPO_ORDEN,
			CmsListOrderEnum.ORDER_DESCENDING, null);
	}

	public FacebookPublishersListWidget(PageContext context, HttpServletRequest req, HttpServletResponse res) {
		this(new CmsJspActionElement(context, req, res));

		//getParameters(req);
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

		String strname = getSelectedItem().getId();

		params.put("strname",strname);
		params.put(PARAM_ACTION,DIALOG_INITIAL);

		//if (getParamListAction().equals(LIST_ACTION_EDITAR)) {
			// forward a la pagina de edicion
		//	getToolManager().jspForwardTool(this, "/Secciones/seccionEdicion", params);
		//}
		if (getParamListAction().equals(LIST_ACTION_BORRAR)) {
			FacebookAccountPublisher account = new FacebookAccountPublisher();
			account.setName(strname);
			FacebookService.getInstance().removePublishers(this.getCms(),account);
			
			super.refreshList();

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
		
		List<FacebookAccountPublisher> accounts = FacebookService.getInstance().getPublishers(this.getCms());

		for (FacebookAccountPublisher account : accounts) {

			CmsListItem listItem = this.getList().newItem("" + account.getName());

			listItem.set("name",account.getName());
			listItem.set("key",account.getKey());
			listItem.set("secret",account.getSecret());
			results.add(listItem);

		}

		return results;

	}

	@Override
	protected void setColumns(CmsListMetadata metadata) {
		//addColumn(metadata, "IdentificadorSeccion", Messages.SECCIONES_LIST_IDENTIFICADOR_SECCION_COLUMN);
		addColumn(metadata, "name", Messages.FACEBOOK_LIST_NAME_PUBLISHER_COLUMN);
		addColumn(metadata, "key", Messages.FACEBOOK_LIST_KEY_PUBLISHER_COLUMN);
		addColumn(metadata, "secret", Messages.FACEBOOK_LIST_SECRET_PUBLISHER_COLUMN);

		//CmsListColumnDefinition editarColumn = addActionColumn(LIST_COLUMN_EDITAR,
		//	Messages.GUI_SECCIONES_LIST_EDITAR_COLUMN_0);

		//CmsListDirectAction editarAction = this.addDirectAction(new CmsListDirectAction(LIST_ACTION_EDITAR),
		//	Messages.GUI_SECCIONES_LIST_ACTION_PUBLICAR_NAME_0, Messages.GUI_SECCIONES_LIST_ACTION_EDITAR_HELP_0,
		//	"xmledit.png", Messages.GUI_SECCIONES_LIST_ACTION_EDITAR_CONF_0);

		//editarColumn.addDirectAction(editarAction);
		//metadata.addColumn(editarColumn);

		CmsListColumnDefinition borrarColumn = addActionColumn(LIST_COLUMN_BORRAR,
				Messages.GUI_FACEBOOK_LIST_BORRAR_COLUMN_0);

		CmsListDirectAction borrarAction = this.addDirectAction(new CmsListDirectAction(LIST_ACTION_BORRAR),
				Messages.GUI_FACEBOOK_LIST_ACTION_BORRAR_NAME_0, Messages.GUI_FACEBOOK_LIST_ACTION_BORRAR_HELP_0,
				"cerrar.png", Messages.GUI_FACEBOOK_LIST_ACTION_BORRAR_CONF_0);

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
		// actionCol.setHelpText(Messages.get().container(Messages.GUI_PROJECTS_LIST_COLS_FILES_HELP_0));
		actionCol.setWidth("20");
		actionCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
		actionCol.setSorteable(false);
		return actionCol;
	}



}
