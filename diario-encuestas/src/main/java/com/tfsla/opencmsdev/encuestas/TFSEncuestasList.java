package com.tfsla.opencmsdev.encuestas;

import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsResourceFilter;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.list.A_CmsListDialog;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDateFormatter;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListIndependentAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListItemDetails;
import org.opencms.workplace.list.CmsListItemDetailsFormatter;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListOrderEnum;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.TipoEdicionService;
import com.tfsla.diario.ediciones.services.openCmsService;

public class TFSEncuestasList extends A_CmsListDialog {

	/**
	 * Es una CmsLisDirectAction que se configura con una lista de strings. Esos strings representan estados
	 * de una encuesta, la accion estara visible solo si el campo "Estado" de la encuesta actual es igual a
	 * alguno de los items del array.
	 * 
	 * @author jpicasso
	 */
	private final class TFSEncuestaDirectAction extends CmsListDirectAction {
		private String[] estadosEncuesta;

		private TFSEncuestaDirectAction(String id, String[] estadosEncuesta) {
			super(id);
			this.estadosEncuesta = estadosEncuesta;
		}

		@Override
		public boolean isVisible() {
			if (getItem() != null) {
				String estado = (String) getItem().get("Estado");

				for (int i = 0; i < this.estadosEncuesta.length; i++) {
					if (this.estadosEncuesta[i].equals(estado)) {
						return true;
					}
				}
				return false;
			}
			return super.isEnabled();
		}
	}

	private static final String PREGUNTA = "Pregunta";
	public static final String LIST_ID = "lp";
	public static final String LIST_COLUMN_NAME = "cn";
	private static final String ESTADO = "Estado";
	private static final String GRUPO = "Grupo";
	private static final String FECHA_PUBLICACION = "FechaPublicacion";
	private static final String USUARIO_PUBLICADOR = "UsuarioPublicador";
	private static final String FECHA_CREACION = "FechaCreacion";
	public static final String PATH_BUTTONS = "tools/encuestas/buttons/";
	private static final String LIST_COLUMN_MODIFICAR = "Modificar";
	private static final String LIST_ACTION_MODIFICAR = "ModificarAction";
	private static final String LIST_COLUMN_PUBLICAR = "Publicar";
	private static final String LIST_ACTION_PUBLICAR = "PublicarAction";
	private static final String LIST_COLUMN_CERRAR = "Cerrar";
	private static final String LIST_ACTION_CERRAR = "CerrarAction";

	private static final String LIST_ACTION_ACTIVAR = "ActivarAction";
	private static final String LIST_COLUMN_ACTIVAR = "Activar";
	private static final String TOGGLE_VER_RESULTADOS = "verResultados";
	private static final String TOGGLE_OCULTAR_RESULTADOS = "OcultarResultados";
	private static final String SHOW_RESULTADOS_ENCUESTA_SESSION_ATTRIBUTE = "showResultsEncuestas";
	private static final String LIST_DETAIL_ENCUESTA = "lde";
	private static final String LIST_COLUMN_DETAIL_ENCUESTA = "Detalles Encuesta";

	private String tipoEdicion = null;

	public TFSEncuestasList(CmsJspActionElement jsp) {
		super(jsp, LIST_ID, Messages.get().container(Messages.GUI_ENCUESTAS_LIST_NAME_0), FECHA_CREACION,
			CmsListOrderEnum.ORDER_DESCENDING, null);
		
		this.getList().setMaxItemsPerPage(GetEncuestasProperties.getInstance(this.getCms()).getPageSizeAdmin());
	}

	public TFSEncuestasList(PageContext context, HttpServletRequest req, HttpServletResponse res) {		
		this(new CmsJspActionElement(context, req, res));
		getParameters(req);

	}

	@Override
	protected String defaultActionHtmlContent() {
		String def = super.defaultActionHtmlContent();

		int posStart = def.indexOf("</form>");
		String newValue = def.substring(0,posStart) + addFilters() + def.substring(posStart);
		return newValue;
	}
	
	@Override
	public void executeListMultiActions() throws IOException, ServletException, CmsRuntimeException {
	}

	@Override
	public void executeListSingleActions() throws IOException, ServletException, CmsRuntimeException {
		if (this.getCms().getRequestContext().currentProject().isOnlineProject()) {
			throw new RuntimeException("No se pueden modificar encuestas desde el Online. Debe ir al Offline.");
		}

		Map<String, String> params = new HashMap<String, String>();

		String encuestaURL = getSelectedItem().getId();
		// set action parameter to initial dialog call
		params.put(CmsDialog.PARAM_ACTION, CmsDialog.DIALOG_INITIAL);
		params.put(TFSEditEncuestaDialog.PARAM_ENCUESTA_URL, encuestaURL);

		if (getParamListAction().equals(LIST_ACTION_MODIFICAR)) {
			// forward a la pagina de edicion
			getToolManager().jspForwardTool(this, "/admencuestas/editaencuesta", params);
		}
		else if (getParamListAction().equals(LIST_ACTION_PUBLICAR)) {
			ModuloEncuestas.publicarEncuesta(this.getJsp(), encuestaURL);
			getToolManager().jspForwardTool(this, "/admencuestas", params);
		}
		else if (getParamListAction().equals(LIST_ACTION_ACTIVAR)) {
			ModuloEncuestas.activarEncuesta(this.getJsp(), encuestaURL, (String) getSelectedItem().get(GRUPO));
			getToolManager().jspForwardTool(this, "/admencuestas", params);
		}
		else if (getParamListAction().equals(LIST_ACTION_CERRAR)) {
			ModuloEncuestas.cerrarEncuesta(this.getJsp(), encuestaURL);
			getToolManager().jspForwardTool(this, "/admencuestas", params);
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
		
		String encuestasPath = "";
		if (tipoEdicion!=null)
			encuestasPath = ModuloEncuestas.getEncuestaPath(getCms(), Integer.parseInt(tipoEdicion));
		else		
			encuestasPath = ModuloEncuestas.ENCUESTAS_PATH;
		
		List<CmsListItem> results = new ArrayList<CmsListItem>();
		
		List cmsFiles = null;
		try {
		cmsFiles = this.getCms().getResourcesInFolder(encuestasPath, CmsResourceFilter.DEFAULT);
		}
		catch (CmsException ex) {
			return results;
		}
		boolean visible = getList().getMetadata().getItemDetailDefinition(LIST_DETAIL_ENCUESTA).isVisible();
		
		for (Iterator it = cmsFiles.iterator(); it.hasNext();) {
			CmsFile file = (CmsFile) it.next();
			if (file.getName().startsWith("~")) {
				// ignore temp files
				continue;
			}
			
			CmsFile readFile = this.getCms().readFile(file);
			CmsXmlContent content = CmsXmlContentFactory.unmarshal(this.getCms(), readFile);

			CmsListItem listItem = this.getList().newItem(this.getCms().getSitePath(file));

			this.setValue(listItem, ESTADO, content, "estado");
			this.setValue(listItem, GRUPO, content, "grupo");
			
			if(!visible){
			    this.setValue(listItem, PREGUNTA, content, "pregunta");
			}else{
				this.setDetail(listItem, PREGUNTA, content, "pregunta");
			}

			
			this.setDateValue(listItem, FECHA_PUBLICACION, content, "fechaPublicacion");
			this.setValue(listItem, USUARIO_PUBLICADOR, content, "usuarioPublicador");
			this.setDateValue(listItem, FECHA_CREACION, content, "fechaCreacion");
			
			
			results.add(listItem);

		}

		return results;
	}

	@Override
	protected void setColumns(CmsListMetadata metadata) {
		this.addColumn(metadata, ESTADO, Messages.ENCUESTAS_LIST_ESTADO_COLUMN, "15%");
		this.addColumn(metadata, GRUPO, Messages.ENCUESTAS_LIST_GRUPO_COLUMN);
		this.addColumn(metadata, PREGUNTA, Messages.ENCUESTAS_LIST_PREGUNTA_COLUMN);
		this.addDateColumn(metadata, FECHA_PUBLICACION, Messages.ENCUESTAS_LIST_FECHA_PUBLICACION_COLUMN, "15%");
		this.addColumn(metadata, USUARIO_PUBLICADOR, Messages.ENCUESTAS_LIST_USUARIO_PUBLICADOR_COLUMN, "15%");
		this.addDateColumn(metadata, FECHA_CREACION, Messages.ENCUESTAS_LIST_FECHA_CREACION_COLUMN, "15%");

		CmsListColumnDefinition modificarColumn = addActionColumn(LIST_COLUMN_MODIFICAR,
			Messages.GUI_ENCUESTAS_LIST_MODIFICAR_COLUMN_0);

		CmsListDirectAction modificarAction = this.addDirectAction(new TFSEncuestaDirectAction(LIST_ACTION_MODIFICAR,
			new String[] { Encuesta.INACTIVA, Encuesta.ACTIVA }), Messages.GUI_ENCUESTAS_LIST_ACTION_MODIFICAR_NAME_0,
			Messages.GUI_ENCUESTAS_LIST_ACTION_MODIFICAR_HELP_0, "xmledit.png", null);

		modificarColumn.addDirectAction(modificarAction);
		metadata.addColumn(modificarColumn);

		CmsListColumnDefinition activarColumn = addActionColumn(LIST_COLUMN_ACTIVAR,
			Messages.GUI_ENCUESTAS_LIST_ACTIVAR_COLUMN_0);

		CmsListDirectAction activarAction = this.addDirectAction(new TFSEncuestaDirectAction(LIST_ACTION_ACTIVAR,
			new String[] { Encuesta.INACTIVA }), Messages.GUI_ENCUESTAS_LIST_ACTION_ACTIVAR_NAME_0,
			Messages.GUI_ENCUESTAS_LIST_ACTION_ACTIVAR_HELP_0, "active.png",
			Messages.GUI_ENCUESTAS_LIST_ACTION_ACTIVAR_CONF_0);

		activarColumn.addDirectAction(activarAction);
		metadata.addColumn(activarColumn);

		CmsListColumnDefinition publicarColumn = addActionColumn(LIST_COLUMN_PUBLICAR,
			Messages.GUI_ENCUESTAS_LIST_PUBLICAR_COLUMN_0);

		CmsListDirectAction publicarAction = this.addDirectAction(new CmsListDirectAction(LIST_ACTION_PUBLICAR),
			Messages.GUI_ENCUESTAS_LIST_ACTION_PUBLICAR_NAME_0, Messages.GUI_ENCUESTAS_LIST_ACTION_PUBLICAR_HELP_0,
			"publish.png", Messages.GUI_ENCUESTAS_LIST_ACTION_PUBLICAR_CONF_0);

		publicarColumn.addDirectAction(publicarAction);
		metadata.addColumn(publicarColumn);

		CmsListColumnDefinition cerrarColumn = addActionColumn(LIST_COLUMN_CERRAR,
			Messages.GUI_ENCUESTAS_LIST_CERRAR_COLUMN_0);

		CmsListDirectAction cerrarAction = this.addDirectAction(new TFSEncuestaDirectAction(LIST_ACTION_CERRAR,
			new String[] { Encuesta.ACTIVA }), Messages.GUI_ENCUESTAS_LIST_ACTION_CERRAR_NAME_0,
			Messages.GUI_ENCUESTAS_LIST_ACTION_CERRAR_HELP_0, "cerrar.png",
			Messages.GUI_ENCUESTAS_LIST_ACTION_CERRAR_CONF_0);

		cerrarColumn.addDirectAction(cerrarAction);
		metadata.addColumn(cerrarColumn);
		
		// create column for resutls  
        CmsListColumnDefinition resutlsCol = new CmsListColumnDefinition(LIST_COLUMN_DETAIL_ENCUESTA);
        resutlsCol.setName(Messages.get().container("Messages.GUI_USERS_LIST_COLS_ORGUNIT_0"));
        resutlsCol.setVisible(false);
        // add it to the list definition
        metadata.addColumn(resutlsCol);
	}

	private void addColumn(CmsListMetadata metadata, String columnID, String i18n, String width) {
		CmsListColumnDefinition cmsListColumnDefinition = new CmsListColumnDefinition(columnID);
		cmsListColumnDefinition.setName(Messages.get().container(i18n));
		cmsListColumnDefinition.setWidth(width);
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

	@Override
    protected void initializeDetail(String detailId) {

      //  super.initializeDetail(detailId);
       // if (detailId.equals(LIST_DETAIL_ENCUESTA)) {
        //    boolean visible = getList().getMetadata().getItemDetailDefinition(LIST_DETAIL_ENCUESTA).isVisible();
          //  getList().getMetadata().getColumnDefinition(LIST_COLUMN_DETAIL_ENCUESTA).setVisible(visible);
           // getList().getMetadata().getColumnDefinition(LIST_COLUMN_DETAIL_ENCUESTA).setPrintable(visible);
       // }

    }
	
	@Override
	protected void setIndependentActions(CmsListMetadata metadata) {
		
//		 add other ou button
        CmsListItemDetails encuestaDetails = new CmsListItemDetails(LIST_DETAIL_ENCUESTA);
        encuestaDetails.setVisible(false);
        encuestaDetails.setHideAction(new CmsListIndependentAction(LIST_DETAIL_ENCUESTA) {

            @Override
            public String getIconPath() {

                return A_CmsListDialog.ICON_DETAILS_HIDE;
            }
            
            @Override
            public boolean isVisible() {

                //return ((A_CmsGroupUsersList)getWp()).hasUsersInOtherOus();
            	return true;
            }
        });
        
        encuestaDetails.setShowAction(new CmsListIndependentAction(LIST_DETAIL_ENCUESTA) {

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#getIconPath()
             */
            @Override
            public String getIconPath() {

                return A_CmsListDialog.ICON_DETAILS_SHOW;
            }

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#isVisible()
             */
            @Override
            public boolean isVisible() {

                //return ((A_CmsGroupUsersList)getWp()).hasUsersInOtherOus();
            	return true;
            }
        });
        encuestaDetails.setShowActionName(Messages.get().container(Messages.TOGGLE_VER_RESULTADOS));
        encuestaDetails.setHideActionName(Messages.get().container(Messages.TOGGLE_OCULTAR_RESULTADOS));
        encuestaDetails.setName(Messages.get().container(Messages.GUI_ENCUESTAS_DETAIL_0));
        encuestaDetails.setFormatter(new CmsListItemDetailsFormatter(Messages.get().container(
            Messages.GUI_ENCUESTAS_DETAIL_0)));
        metadata.addItemDetails(encuestaDetails);
	}

	@Override
	protected void setMultiActions(CmsListMetadata metadata) {
	}

	private void setValue(CmsListItem listItem, String columnID, CmsXmlContent content, String propertyName) {
		listItem.set(columnID, content.getValue(propertyName, CmsLocaleManager.getDefaultLocale()).getStringValue(
			this.getCms()));
	}
	
	private void setDetail(CmsListItem listItem, String columnID, CmsXmlContent content, String propertyName) {
		
		String pregunta = content.getValue(propertyName, CmsLocaleManager.getDefaultLocale()).getStringValue(
				this.getCms());
		String Detalles = pregunta;
		
		CmsFile FileEncuesta = content.getFile();
		String encuestaURL = ModuloEncuestas.getCompletePath(FileEncuesta.getName());
		
		int IdEncuesta = 0;
		
		try {
		    IdEncuesta =  ModuloEncuestas.getEncuestaIDFromOnline(this.getCms(), encuestaURL);
		} catch (Exception e) {

		}
		
		
		if(IdEncuesta > 0){
			String tabla = "";
			
			String respuesta = "";
			int cantRespuestas = Encuesta.MAX_RESPUESTAS;
			DecimalFormat df = new DecimalFormat("#0.0");
			
			ResultadoEncuestaBean resultado = ModuloEncuestas.getResultado(this.getCms(), encuestaURL);
			int total = 0;
			
				for(int i=1; i<=cantRespuestas; i++ ){
					try {
					  respuesta = content.getValue("respuesta["+i+"]", CmsLocaleManager.getDefaultLocale()).getStringValue(
							this.getCms());
					
					  if(!respuesta.equals("")){
						  int nro = i-1;
						  RespuestaEncuestaConVotos respuestaVotos = (RespuestaEncuestaConVotos) resultado.getRespuestas().get(nro);
						  tabla += "<tr><td>"+respuesta+"</td><td> "+df.format(respuestaVotos.getPorcentajeVotos())+"%</td><td> ("+respuestaVotos.getCantVotos()+")</td></tr>";
						  total = total +respuestaVotos.getCantVotos();
					  }
					} catch (Exception e) {

					}
				}

				Detalles += "<br><table CELLPADDING=3 CELLSPACING=3 WIDTH=200 >"+tabla+"<tr><td colpan=3> Total de votos: "+total+"</td><td></table>";
				
				//System.out.println(Detalles);
			

		}else{
			Detalles += "<br><b>Sin resultados</b>";
		}
		
		listItem.set(columnID, Detalles);
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
		
		TipoEdicionService tDAO = new TipoEdicionService();
		
		for (java.util.Iterator iter = tDAO.obtenerTipoEdiciones(proyecto).iterator(); iter.hasNext();) {
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

	private void setDateValue(CmsListItem listItem, String columnID, CmsXmlContent content, String propertyName) {
		String stringValue = content.getValue(propertyName, CmsLocaleManager.getDefaultLocale()).getStringValue(
			this.getCms());

		if ("".equals(stringValue) || "0".equals(stringValue)) {
			listItem.set(columnID, null);
		}
		else {
			Date valueAsDate = new Date(Long.parseLong(stringValue));
			listItem.set(columnID, valueAsDate);
		}

	}

	private void addDateColumn(CmsListMetadata metadata, String columnID, String i18nKey, String width) {
		CmsListColumnDefinition cmsListColumnDefinition = new CmsListColumnDefinition(columnID);
		cmsListColumnDefinition.setName(Messages.get().container(i18nKey));
		cmsListColumnDefinition.setFormatter(new CmsListDateFormatter(DateFormat.SHORT, DateFormat.SHORT));
		cmsListColumnDefinition.setWidth(width);
		cmsListColumnDefinition.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
		metadata.addColumn(cmsListColumnDefinition);
	}

	private void addColumn(CmsListMetadata metadata, String columnID, String i18nKey) {
		CmsListColumnDefinition cmsListColumnDefinition = new CmsListColumnDefinition(columnID);
		cmsListColumnDefinition.setName(Messages.get().container(i18nKey));
		cmsListColumnDefinition.setTextWrapping(true);
		cmsListColumnDefinition.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
		metadata.addColumn(cmsListColumnDefinition);
	}
}
