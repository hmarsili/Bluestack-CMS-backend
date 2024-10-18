package com.tfsla.diario.ediciones.widgets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.PageContext;

import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsCategoryService;
import org.opencms.workplace.list.A_CmsListDialog;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListOrderEnum;

public class SubCategoriasList extends A_CmsListDialog {

	private static final String LIST_COLUMN_EDITAR = "Editar";
	private static final String LIST_ACTION_EDITAR = "EditarAction";

	private static final String LIST_COLUMN_BORRAR = "Borrar";
	private static final String LIST_ACTION_BORRAR = "BorrarAction";

	public static final String LIST_ID = "lp";

	public static final String PATH_BUTTONS = "tools/Ediciones/buttons/";

	private static final String CAMPO_ORDEN = "categoria";

	public SubCategoriasList(CmsJspActionElement jsp) {
		super( jsp, LIST_ID, Messages.get().container(Messages.GUI_SUBCATEGORIA_LIST_NAME_0), CAMPO_ORDEN,
			CmsListOrderEnum.ORDER_DESCENDING, null);
	}

	public SubCategoriasList(PageContext context, HttpServletRequest req, HttpServletResponse res) {
		this(new CmsJspActionElement(context, req, res));

	}


	@Override
	public void executeListMultiActions() throws IOException, ServletException, CmsRuntimeException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void executeListSingleActions() throws IOException, ServletException, CmsRuntimeException {
		
		Map<String, String> params = new HashMap<String, String>();

		String category = getSelectedItem().getId();

		params.put("pathCategoria",category);
		params.put(PARAM_ACTION,DIALOG_INITIAL);

		if (getParamListAction().equals(LIST_ACTION_EDITAR)) {
			// forward a la pagina de edicion
			getToolManager().jspForwardTool(this, "/Categorias/categoriaEdicion", params);
		}
		if (getParamListAction().equals(LIST_ACTION_BORRAR)) {

			CmsCategoryService cService = CmsCategoryService.getInstance();

			String categoria = category;
			
			try {
				
				cService.deleteCategory(this.getCms(),categoria ,null);
				
				this.getCms().lockResource(categoria);
				this.getCms().deleteResource(categoria, CmsResource.DELETE_REMOVE_SIBLINGS);
				this.getCms().unlockResource(categoria);
				
				OpenCms.getPublishManager().publishResource(this.getCms(), categoria);

			} catch (CmsException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
				//borrar categoria.
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
		List<CmsResource> files = this.getCms().getSubFolders("/SubSecciones/");
		
		for (int j=0;j<files.size();j++)
		{
			CmsResource file = files.get(j);
			
			files.addAll(this.getCms().getSubFolders(this.getCms().getRequestContext().removeSiteRoot(file.getRootPath())));
		}
	
		for (Iterator it = files.iterator();it.hasNext();)
		{
			CmsResource file = (CmsResource) it.next();
			String path = this.getCms().getRequestContext().removeSiteRoot(file.getRootPath());
			CmsListItem listItem = this.getList().newItem(path);

			String categoria = path.replace("/SubSecciones/", "");
			categoria = categoria.substring(0,categoria.length()-1);
			
			CmsProperty descripcion = this.getCms().readPropertyObject(file, "Title", false);
			
			listItem.set("categoria",categoria);
			listItem.set("descripcion",descripcion.getValue());
			results.add(listItem);
		}
		
		return results;
	}

	@Override
	protected void setColumns(CmsListMetadata metadata) {
		addColumn(metadata, "categoria", Messages.SUBCATEGORIA_LIST_CATEGORIA_COLUMN);
		addColumn(metadata, "descripcion", Messages.SUBCATEGORIA_LIST_DESCRIPCION_COLUMN);

		CmsListColumnDefinition editarColumn = addActionColumn(LIST_COLUMN_EDITAR,
				Messages.GUI_SUBCATEGORIA_LIST_EDITAR_COLUMN_0);

		CmsListDirectAction editarAction = this.addDirectAction(new CmsListDirectAction(LIST_ACTION_EDITAR),
				Messages.GUI_SUBCATEGORIA_LIST_ACTION_EDITAR_NAME_0, Messages.GUI_SUBCATEGORIA_LIST_ACTION_EDITAR_HELP_0,
				"xmledit.png", Messages.GUI_SUBCATEGORIA_LIST_ACTION_EDITAR_CONF_0);

			editarColumn.addDirectAction(editarAction);
			metadata.addColumn(editarColumn);

			CmsListColumnDefinition borrarColumn = addActionColumn(LIST_COLUMN_BORRAR,
					Messages.GUI_SUBCATEGORIA_LIST_BORRAR_COLUMN_0);

			CmsListDirectAction borrarAction = this.addDirectAction(new CmsListDirectAction(LIST_ACTION_BORRAR),
					Messages.GUI_SUBCATEGORIA_LIST_ACTION_BORRAR_NAME_0, Messages.GUI_SUBCATEGORIA_LIST_ACTION_BORRAR_HELP_0,
					"cerrar.png", Messages.GUI_SUBCATEGORIA_LIST_ACTION_BORRAR_CONF_0);

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

	private CmsListColumnDefinition addActionColumn(String columnID, String i18n) {
		CmsListColumnDefinition actionCol = new CmsListColumnDefinition(columnID);
		actionCol.setName(Messages.get().container(i18n));
		// actionCol.setHelpText(Messages.get().container(Messages.GUI_PROJECTS_LIST_COLS_FILES_HELP_0));
		actionCol.setWidth("20");
		actionCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
		actionCol.setSorteable(false);
		return actionCol;
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

}
