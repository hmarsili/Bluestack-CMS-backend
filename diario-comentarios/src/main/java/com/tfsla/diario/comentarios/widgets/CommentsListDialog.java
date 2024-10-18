package com.tfsla.diario.comentarios.widgets;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.PageContext;

import org.opencms.file.CmsUser;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.list.A_CmsListDialog;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListColumnDefinitionCheck;
import org.opencms.workplace.list.CmsListDateFormatter;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListOrderEnum;
import org.opencms.workplace.list.CmsListSearchAction;

import com.tfsla.diario.comentarios.model.Comment;
import com.tfsla.diario.comentarios.services.CommentsModule;
import com.tfsla.opencmsdev.formatters.CmsListLinkParserFormatter;
import com.tfsla.opencmsdev.formatters.CmsListTitleFormatter;
import com.tfsla.opencmsdev.formatters.CommentTextFormatter;
import com.tfsla.opencmsdev.listActions.ListIndependentAccion;

public class CommentsListDialog extends A_CmsListDialog {

	private static final String MODULE_PATH = "/admcomments";

	public static final String LIST_ID = "lp";
	public static final String LIST_COLUMN_NAME = "cn";
	public static final String PATH_BUTTONS = "tools/comentarios/";

	private static final String FECHA = "fecha";
	private static final String ID = "id";
	private static final String USUARIO_AUTOR = "UsuarioAutor";
	private static final String TEXTO = "Texto";
	private static final String CANT_REPORTES = "CantReportes";
	private static final String NOTICIA_URL = "NoticiaURL";
	private static final String REMOTE_IP = "RemoteIp";
	private static final String TITULO_NOTICIA = "TituloNoticia";
	private static final String SECCION_NOTICIA = "SeccionNoticia";

	private static final String ESTADO = "Estado";

	private static final String LIST_COLUMN_ELIMINAR = "Eliminar";
	private static final String LIST_ACTION_ELIMINAR = "EliminarAction";

	private static final String LIST_COLUMN_RECHAZAR = "Rechazar";
	private static final String LIST_ACTION_RECHAZAR = "RechazarAction";

	private static final String LIST_ACTION_ACEPTAR = "AceptarAction";
	private static final String LIST_COLUMN_ACEPTAR = "Aceptar";

	private static final String PARAM_COMMENT_ID = "commentId";

	private static final String TOGGLE_VER_HISTORICO = "verHistorico";
	private static final String TOGGLE_VER_RECIENTES = "verRecientes";
	private static final String TOGGLE_VER_PENDIENTES = "verPendientes";
	private static final String TOGGLE_VER_TODOS = "verTodos";
	private static final String TOGGLE_VER_REVISION = "verEnRevision";

	// filtro de pendientes/todos
	private static final String SHOW_ONLY_PENDING_COMMENTS_SESSION_ATTRIBUTE = "showOnlyPendingComments";
	
	private static final String SHOW_ONLY_PREMODERATED_COMMENTS_SESSION_ATTRIBUTE = "showOnlyPreModeratedComments";

	// filtro de comentarios nuevos/comentarios historicos
	private static final String SHOW_HISTORIC_COMMENTS_SESSION_ATTRIBUTE = "showHistoricComments";

	private static final String ACEPTAR_ACTION = "A";
	private static final String RECHAZAR_ACTION = "R";
	private static final String ELIMINAR_ACTION = "E";

	// ******************************
	// ** Constructores
	// ******************************
	public CommentsListDialog(CmsJspActionElement jsp) {
		super(jsp, LIST_ID, Messages.get().container(Messages.GUI_COMMENTS_LIST_NAME_0), CANT_REPORTES,
				CmsListOrderEnum.ORDER_DESCENDING, null);
		this.getList().setMaxItemsPerPage(CommentsModule.getInstance(getCms()).getAdminPageSize());

		// marcamos la metadata como volatil para poder usar el reload de la configuracion en caliente
		// podria optimizarse guardando la metadata en otro lugar para invalidarse cuando se recarga la
		// configuracion
		this.getList().getMetadata().setVolatile(true);
	}

	public CommentsListDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {
		this(new CmsJspActionElement(context, req, res));
		getParameters(req);
	}

	// ******************************
	// ** list dialog methods
	// ******************************
	@Override
	public void displayDialog() throws JspException, IOException, ServletException {
		
		super.displayDialog();
		
	}

	private void getParameters(HttpServletRequest req)
	{
		if (req.getParameter("commentAction")!=null)
		{
			String action = req.getParameter("commentAction");

			String[] ids =req.getParameterValues("id");

			if (ids!=null)
			{
				if (action.equals(ACEPTAR_ACTION)) {
					for (String id : ids) {
						CommentsModule.getInstance(getCms()).acceptComment(this.getJsp().getCmsObject(), id);
					}
				}
				else if (action.equals(RECHAZAR_ACTION)) {
					for (String id : ids) {
						CommentsModule.getInstance(getCms()).rejectComment(this.getJsp().getCmsObject(), id);
					}
				}
				else if (action.equals(ELIMINAR_ACTION)) {
					for (String id : ids) {
						CommentsModule.getInstance(getCms()).deleteComment(this.getJsp().getCmsObject(), id);
					}
				}
			}

		}
	}

	@Override
	protected void setSearchAction(CmsListMetadata metadata, String columnId) {
		if (metadata.getSearchAction() == null) {
			CmsListSearchAction action = new CmsListSearchAction(metadata.getColumnDefinition(NOTICIA_URL));
			action.addColumn(metadata.getColumnDefinition(ESTADO));
			action.addColumn(metadata.getColumnDefinition(CANT_REPORTES));
			action.addColumn(metadata.getColumnDefinition(FECHA));
			action.addColumn(metadata.getColumnDefinition(USUARIO_AUTOR));
			action.addColumn(metadata.getColumnDefinition(TEXTO));
			action.addColumn(metadata.getColumnDefinition(REMOTE_IP));
			action.addColumn(metadata.getColumnDefinition(TITULO_NOTICIA));
			action.addColumn(metadata.getColumnDefinition(SECCION_NOTICIA));
			metadata.setSearchAction(action);
		}
	}

	@Override
	public void executeListMultiActions() throws IOException, ServletException, CmsRuntimeException {
	}

	@Override
	public void executeListIndepActions() {

		if (TOGGLE_VER_HISTORICO.equals(this.getParamListAction())) {
			this.setVerHistorico(Boolean.TRUE);
		}
		if (TOGGLE_VER_RECIENTES.equals(this.getParamListAction())) {
			this.setVerHistorico(Boolean.FALSE);
		}

		//if (CommentsModule.getInstance().getModerateComments().booleanValue()) {
			// las acciones de ver pendientes solo tienen sentido si esta prendida la moderacion

			if (TOGGLE_VER_PENDIENTES.equals(this.getParamListAction())) {
				// prender el flag de ver solo los pendientes
				this.setShowPendingCommentsFilter(Boolean.TRUE);
				this.setShowPreModeratedCommentsFilter(Boolean.FALSE); 
			}
			//else if (TOGGLE_VER_TODOS.equals(this.getParamListAction())) {
				// apagar el flag de ver solo pendientes (ver todos)
			//	this.setShowPendingCommentsFilter(Boolean.FALSE);
			//	this.setShowPreModeratedCommentsFilter(Boolean.FALSE); 
			//}
			
		//}
		
		if (TOGGLE_VER_TODOS.equals(this.getParamListAction())) {
			// apagar el flag de ver solo pendientes (ver todos)
			this.setShowPendingCommentsFilter(Boolean.FALSE);
			this.setShowPreModeratedCommentsFilter(Boolean.FALSE); 
		}
		
		if(TOGGLE_VER_REVISION.equals(this.getParamListAction())){
				this.setShowPreModeratedCommentsFilter(Boolean.TRUE); 
		}
		
		super.executeListIndepActions();

		// para forzar que se pida de nuevo la lista con el filtro aplicado
		refreshList();
	}

	// ************************************
	// ** filtro de comentarios pendientes
	// ************************************
	private void setShowPendingCommentsFilter(Boolean value) {
		this.getJsp().getRequest().getSession().setAttribute(SHOW_ONLY_PENDING_COMMENTS_SESSION_ATTRIBUTE,
				value);
	}

	private boolean getShowPendingCommentsFilter() {
		Boolean filterSessionAttribute = (Boolean) this.getJsp().getRequest().getSession().getAttribute(
				SHOW_ONLY_PENDING_COMMENTS_SESSION_ATTRIBUTE);

		// si el atributo no esta en la session (es la primera vez) defaultamos a "ver todos"
		return filterSessionAttribute != null ? filterSessionAttribute.booleanValue() : false;
	}
	
	private void setShowPreModeratedCommentsFilter(Boolean value) {
		this.getJsp().getRequest().getSession().setAttribute(SHOW_ONLY_PREMODERATED_COMMENTS_SESSION_ATTRIBUTE,
				value);
	}

	private boolean getShowPreModeratedCommentsFilter() {
		Boolean filterPreModeratedSessionAttribute = (Boolean) this.getJsp().getRequest().getSession().getAttribute(
				SHOW_ONLY_PREMODERATED_COMMENTS_SESSION_ATTRIBUTE);

		// si el atributo no esta en la session (es la primera vez) defaultamos a "ver todos"
		return filterPreModeratedSessionAttribute != null ? filterPreModeratedSessionAttribute.booleanValue() : false;
	}

	private void setVerHistorico(Boolean value) {
		this.getJsp().getRequest().getSession().setAttribute(SHOW_HISTORIC_COMMENTS_SESSION_ATTRIBUTE, value);
	}

	private boolean getVerHistoricoFilter() {
		Boolean historicCommentsSessionAttribute = (Boolean) this.getJsp().getRequest().getSession()
				.getAttribute(SHOW_HISTORIC_COMMENTS_SESSION_ATTRIBUTE);

		// si el atributo no esta en la session defaulteamos a "ver solo los nuevos"
		return historicCommentsSessionAttribute != null ? historicCommentsSessionAttribute.booleanValue()
				: false;
	}

	@Override
	public void executeListSingleActions() throws IOException, ServletException, CmsRuntimeException {
		Map<String, String> params = new HashMap<String, String>();

		String commentId = getSelectedItem().getId();
		// set action parameter to initial dialog call
		params.put(CmsDialog.PARAM_ACTION, CmsDialog.DIALOG_INITIAL);
		params.put(PARAM_COMMENT_ID, commentId);

		if (getParamListAction().equals(LIST_ACTION_ELIMINAR)) {
			CommentsModule.getInstance(getCms()).deleteComment(this.getJsp().getCmsObject(), commentId);
			getToolManager().jspForwardTool(this, MODULE_PATH, params);
		}
		else if (getParamListAction().equals(LIST_ACTION_ACEPTAR)) {
			CommentsModule.getInstance(getCms()).acceptComment(this.getJsp().getCmsObject(), commentId);
			getToolManager().jspForwardTool(this, MODULE_PATH, params);
		}
		else if (getParamListAction().equals(LIST_ACTION_RECHAZAR)) {
			CommentsModule.getInstance(getCms()).rejectComment(this.getJsp().getCmsObject(), commentId);
			getToolManager().jspForwardTool(this, MODULE_PATH, params);
		}
		else {
			this.throwListUnsupportedActionException();
		}

		listSave();
	}

	@Override
	protected void fillDetails(String detailId) {
	}

	@Override
	protected List getListItems() throws CmsException {
		List<CmsListItem> results = new ArrayList<CmsListItem>();
		boolean verHistoricoFilter = this.getVerHistoricoFilter();

		List<Comment> comments = CommentsModule.getInstance(getCms()).getAllComments(this.getCms(),
				verHistoricoFilter);

		for (Iterator<Comment> it = comments.iterator(); it.hasNext();) {
			Comment comment = it.next();
			
			boolean verPendientes = this.getShowPendingCommentsFilter();
			boolean verPreModerados = this.getShowPreModeratedCommentsFilter();

			//if (CommentsModule.getInstance().getModerateComments().booleanValue()) {
				
				if (verPendientes && !verPreModerados) {
					// poner solo los pendientes en la lista
					if (Comment.PENDIENTE_STATE.equals(comment.getState())) {
						newItem(results, comment);
					}
				}else if (!verPendientes && !verPreModerados) {
					// poner todos
					newItem(results, comment);
				}else if (verPreModerados){
					if (Comment.REVISION_STATE.equals(comment.getState())) {
						newItem(results, comment);
					}
				}else{
					
					newItem(results, comment);
					
				}
					
			//}
			//else {
				
				//if (verPreModerados){
					//if (Comment.REVISION_STATE.equals(comment.getState())) {
					//	newItem(results, comment);
				//	}
				//}else{
                    // si la moderacion no esta activa, tambien poner todos
					//newItem(results, comment);
					
				//}
			//}

		}

		return results;
	}

	private void newItem(List<CmsListItem> results, Comment comment) {
		CmsListItem listItem = this.getList().newItem(comment.getId() + "");
		listItem.set(ESTADO, comment.getState());
		listItem.set(FECHA, comment.getDate());
		String user = comment.getUser();

		String userDetails = "";
		try {
			CmsUser cmsUser = this.getCms().readUser(user);
			userDetails = cmsUser.getFirstname() + " " + cmsUser.getLastname() + " mail: "
					+ cmsUser.getEmail() + " desc: " + cmsUser.getDescription();
		}
		catch (CmsException e) {
			userDetails = "user details unavailable";
		}

		listItem.set(ID, comment.getId());
		listItem.set(USUARIO_AUTOR, user + "|" + userDetails);
		
		String text = comment.getText();
		text = text.replace("<", "[");
		text = text.replace(">", "]");
		text = text.replace("&lt;", "[");
		text = text.replace("&gt;", "]");
		text = text.replace("'", "&uml;");
		text = text.replace("\"", "&uml;");
		
		listItem.set(TEXTO, text);
		listItem.set(CANT_REPORTES, comment.getCantReports());
		listItem.set(NOTICIA_URL, comment.getNoticiaURL());

		// seteamos tambien la URL para poder hacer que el titulo sea clickeable
		listItem.set(TITULO_NOTICIA, comment.getTituloNoticia(this.getCms()) + "|" + comment.getNoticiaURL());
		listItem.set(SECCION_NOTICIA, comment.getSeccionNoticia(this.getCms()));
		listItem.set(REMOTE_IP, comment.getRemoteIP());

		results.add(listItem);
	}

	@Override
	protected void setColumns(CmsListMetadata metadata) {

		this.addCheckBoxColumn(metadata, ID, Messages.COMMENTS_LIST_CHECK_COLUMN, "10");

		this.addColumn(metadata, ESTADO, Messages.COMMENTS_LIST_ESTADO_COLUMN, "10%");

		CmsListColumnDefinition textColumn = this.addColumn(metadata, TEXTO,
				Messages.COMMENTS_LIST_TEXTO_COLUMN, "20%");
		textColumn.setFormatter(new CommentTextFormatter("/system/workplace/admin/admcomments/textdetail.html"));


		CmsListColumnDefinition userColumn = this.addColumn(metadata, USUARIO_AUTOR,
				Messages.COMMENTS_LIST_USUARIO_AUTOR_COLUMN);
		userColumn.setFormatter(new CmsListTitleFormatter());

		this.addDateColumn(metadata, FECHA, Messages.COMMENTS_LIST_FECHA_COLUMN, "15%");
		this.addColumn(metadata, CANT_REPORTES, Messages.COMMENTS_LIST_CANT_REPORTES_COLUMN, "5%");

		CmsListColumnDefinition noticiaURLColumn = this.addColumn(metadata, NOTICIA_URL,
				Messages.COMMENTS_LIST_NOTICIA_URL_COLUMN, "1%");
		noticiaURLColumn.setVisible(false);
		// noticiaURLColumn.setFormatter(new CmsListLinkFormatter(null));

		CmsListColumnDefinition tituloNoticiaColumn = this.addColumn(metadata, TITULO_NOTICIA,
				Messages.COMMENTS_LIST_TITULO_NOTICIA_COLUMN, "30%");
		tituloNoticiaColumn.setFormatter(new CmsListLinkParserFormatter(null));

		this.addColumn(metadata, SECCION_NOTICIA, Messages.COMMENTS_LIST_SECCION_NOTICIA_COLUMN, "10%");

		this.addColumn(metadata, REMOTE_IP, Messages.COMMENTS_LIST_REMOTE_IP_COLUMN, "10%");

		//if (CommentsModule.getInstance().getModerateComments()) {
			// las acciones de activar y rechazar solo tienen sentido si la modalidad moderada esta habilitada
			CmsListColumnDefinition activarColumn = addActionColumn(LIST_COLUMN_ACEPTAR,
					Messages.GUI_COMMENTS_LIST_ACEPTAR_COLUMN_0);

			CmsListDirectAction activarAction = this.addDirectAction(new CmsListDirectAction(
					LIST_ACTION_ACEPTAR), Messages.GUI_COMMENTS_LIST_ACTION_ACEPTAR_NAME_0,
					Messages.GUI_COMMENTS_LIST_ACTION_ACEPTAR_HELP_0, ICON_ACTIVE,
					Messages.GUI_COMMENTS_LIST_ACTION_ACEPTAR_CONF_0);

			activarColumn.addDirectAction(activarAction);
			metadata.addColumn(activarColumn);
		//}

		// la accion de rechazar se muestra siempre
		CmsListColumnDefinition rechazarColumn = addActionColumn(LIST_COLUMN_RECHAZAR,
				Messages.GUI_COMMENTS_LIST_RECHAZAR_COLUMN_0);

		CmsListDirectAction rechazarAction = this.addDirectAction(new CmsListDirectAction(
				LIST_ACTION_RECHAZAR), Messages.GUI_COMMENTS_LIST_ACTION_RECHAZAR_NAME_0,
				Messages.GUI_COMMENTS_LIST_ACTION_RECHAZAR_HELP_0, ICON_MINUS,
				Messages.GUI_COMMENTS_LIST_ACTION_RECHAZAR_CONF_0);

		rechazarColumn.addDirectAction(rechazarAction);
		metadata.addColumn(rechazarColumn);

		CmsListColumnDefinition eliminarColumn = addActionColumn(LIST_COLUMN_ELIMINAR,
				Messages.GUI_COMMENTS_LIST_ELIMINAR_COLUMN_0);

		CmsListDirectAction eliminarAction = this.addDirectAction(new CmsListDirectAction(
				LIST_ACTION_ELIMINAR), Messages.GUI_COMMENTS_LIST_ACTION_ELIMINAR_NAME_0,
				Messages.GUI_COMMENTS_LIST_ACTION_ELIMINAR_HELP_0, ICON_DELETE,
				Messages.GUI_COMMENTS_LIST_ACTION_ELIMINAR_CONF_0);

		eliminarColumn.addDirectAction(eliminarAction);
		metadata.addColumn(eliminarColumn);
	}

	private CmsListColumnDefinition addColumn(CmsListMetadata metadata, String columnID, String i18n,
			String width) {
		CmsListColumnDefinition cmsListColumnDefinition = new CmsListColumnDefinition(columnID);
		
		cmsListColumnDefinition.setName(Messages.get().container(i18n));
		cmsListColumnDefinition.setWidth(width);
		cmsListColumnDefinition.setTextWrapping(true);
		cmsListColumnDefinition.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);

		metadata.addColumn(cmsListColumnDefinition);

		return cmsListColumnDefinition;
	}

	private CmsListColumnDefinition addCheckBoxColumn(CmsListMetadata metadata, String columnID, String i18n,
			String width) {
		CmsListColumnDefinitionCheck cmsListColumnDefinition = new CmsListColumnDefinitionCheck(columnID);
		cmsListColumnDefinition.setName(Messages.get().container(i18n));
		cmsListColumnDefinition.setWidth(width);
		cmsListColumnDefinition.setTextWrapping(true);
		cmsListColumnDefinition.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);

		metadata.addColumn(cmsListColumnDefinition);

		return cmsListColumnDefinition;
	}

	private CmsListDirectAction addDirectAction(CmsListDirectAction directAction, String nameI18n,
			String helpI18n, String iconName, String confirmationMessage) {
		directAction.setName(Messages.get().container(nameI18n));
		directAction.setHelpText(Messages.get().container(helpI18n));
		directAction.setIconPath(iconName);
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

	@Override
	protected void setIndependentActions(CmsListMetadata metadata) {
		ListIndependentAccion listIndependentAccion = new ListIndependentAccion(TOGGLE_VER_HISTORICO,
				"Ver Historico", "Ver todos los comentarios historicos", "commons/warning.png");
		listIndependentAccion.setConfirmationMessage(Messages.get().container(
				Messages.GUI_COMMENTS_LIST_ACTION_VER_HISTORICO_CONF_0));
		metadata.addIndependentAction(listIndependentAccion);

		metadata.addIndependentAction(new ListIndependentAccion(TOGGLE_VER_RECIENTES, "Ver solo ultimos "
				+ CommentsModule.getInstance(getCms()).getCantDiasMostrables() + " dias",
				"Ver solo los comentarios recientes (default)", "list/csv.png"));

		//if (CommentsModule.getInstance().getModerateComments().booleanValue()) {
			metadata.addIndependentAction(new ListIndependentAccion(TOGGLE_VER_PENDIENTES, "Ver Pendientes",
					"Ver comentarios en estado pendientes", "list/csv.png"));
			
			//metadata.addIndependentAction(new ListIndependentAccion(TOGGLE_VER_TODOS, "Ver Todos",
			//		"Ver comentarios sin filtrar por estado", "list/csv.png"));
		//}
		
		metadata.addIndependentAction(new ListIndependentAccion(TOGGLE_VER_REVISION, "Ver pre-moderados",
				"Ver comentarios en estado de revisi&oacute;n", "list/csv.png"));

		metadata.addIndependentAction(new ListIndependentAccion(TOGGLE_VER_TODOS, "Ver Todos",
				"Ver comentarios sin filtrar por estado", "list/csv.png"));
	}

	@Override
	protected String defaultActionHtmlContent() {
		String def = super.defaultActionHtmlContent();

		int posStart = def.indexOf("</form>");
		String newValue = def.substring(0,posStart) + addFilters() + def.substring(posStart);
		return newValue;
	}
	
	private String addFilters()
	{
		String filterHTML="";
		filterHTML+=this.dialogBlock(CmsWorkplace.HTML_START, "Acciones", false);

		filterHTML+="<script type=\"text/javascript\">\n";

		filterHTML+="function doAction(value)\n";
		filterHTML+="{\n";
		filterHTML+="	var element = document.getElementById(\"commentAction\");\n";
		filterHTML+="	element.value= value;\n";
		filterHTML+="	theForm = document.forms[\"lp-form\"];\n";
		filterHTML+="	theForm.submit();\n";
		filterHTML+="	return false;";
		filterHTML+="}\n";
		filterHTML+="</script>\n";

		filterHTML+="<input type=\"hidden\" id=\"commentAction\" name=\"commentAction\" value=\"\">\n";

		filterHTML+="<a class='link' onclick=\"return doAction('" + ACEPTAR_ACTION + "');\" href=\"#\"><img boder=\"0\" alt='Aceptar' title='Aceptar' src=\"/system/workplace/resources/" +
		ICON_ACTIVE + "\">Aceptar Comentarios</a>&nbsp;&nbsp;\n";

		filterHTML+="<a class='link' onclick=\"return doAction('" + RECHAZAR_ACTION + "');\" href=\"#\"><img boder=\"0\" alt='Rechazar' title='Rechazar' src=\"/system/workplace/resources/" +
		ICON_MINUS + "\">Rechazar Comentarios</a>&nbsp;&nbsp;\n";

		filterHTML+="<a class='link' onclick=\"return doAction('" + ELIMINAR_ACTION + "');\" href=\"#\"><img boder=\"0\" alt='Eliminar' title='Eliminar' src=\"/system/workplace/resources/" +
		ICON_DELETE + "\">Eliminar Comentarios</a>\n";

		filterHTML+=this.dialogBlock(CmsWorkplace.HTML_END, "Acciones", false);

		return filterHTML;
	}


	@Override
	protected void setMultiActions(CmsListMetadata metadata) {
	}

	private void addDateColumn(CmsListMetadata metadata, String columnID, String i18nKey, String width) {
		CmsListColumnDefinition cmsListColumnDefinition = new CmsListColumnDefinition(columnID);
		cmsListColumnDefinition.setName(Messages.get().container(i18nKey));
		cmsListColumnDefinition.setFormatter(new CmsListDateFormatter(DateFormat.SHORT, DateFormat.SHORT));
		cmsListColumnDefinition.setWidth(width);
		cmsListColumnDefinition.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
		metadata.addColumn(cmsListColumnDefinition);
	}

	private CmsListColumnDefinition addColumn(CmsListMetadata metadata, String columnID, String i18nKey) {
		CmsListColumnDefinition cmsListColumnDefinition = new CmsListColumnDefinition(columnID);
		cmsListColumnDefinition.setName(Messages.get().container(i18nKey));
		cmsListColumnDefinition.setTextWrapping(true);
		cmsListColumnDefinition.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
		metadata.addColumn(cmsListColumnDefinition);

		return cmsListColumnDefinition;
	}
}