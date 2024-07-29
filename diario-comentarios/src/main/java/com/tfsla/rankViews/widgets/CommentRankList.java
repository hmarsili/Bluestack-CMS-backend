package com.tfsla.rankViews.widgets;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.list.A_CmsListDialog;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListOrderEnum;

import com.tfsla.diario.comentarios.widgets.Messages;
import com.tfsla.diario.ediciones.data.SeccionDAO;
import com.tfsla.diario.ediciones.model.Edicion;
import com.tfsla.diario.ediciones.model.Seccion;
import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.EdicionService;
import com.tfsla.diario.ediciones.services.TipoEdicionService;
import com.tfsla.opencmsdev.formatters.CommentTextFormatter;
import com.tfsla.rankViews.model.TfsRankResults;
import com.tfsla.rankViews.service.RankService;
import com.tfsla.rankViews.service.TfsRankXmlPageDataCollector;
import com.tfsla.statistics.model.TfsHitPage;
import com.tfsla.statistics.model.TfsStatisticsOptions;
import com.tfsla.statistics.service.DataCollectorManager;
import com.tfsla.statistics.service.I_statisticsDataCollector;

public class CommentRankList  extends A_CmsListDialog {

	public static final String LIST_ID = "lp";
	protected static final String CAMPO_ORDEN = "visitas";

	
	protected final String FECHACREACION = "FECHACREACION";
	protected final String FECHAMODIFICACION = "FECHAMODIFICACION";
	protected final String FECHAULTIMAMODIFICACION = "FECHAULTIMAMODIFICACION";
	protected final String FECHANO = "FECHANO";

	protected int tipoEdicion = -1;
	protected int edicion = 0;

	protected String seccion = null;
	protected String[] tags = null;
	
	protected int number = 20;
	protected int pagina = 1;
	protected int horas = 24;

	
	protected int horasNoticia = 24;
	
	protected String campo = FECHANO;

	int rankMode = TfsStatisticsOptions.RANK_RECOMENDACION;

	public CommentRankList(CmsJspActionElement jsp) {
		super( jsp, LIST_ID, Messages.get().container(Messages.GUI_COMENTARIOS_LIST_NAME_0), CAMPO_ORDEN,
			CmsListOrderEnum.ORDER_DESCENDING, null);
	}

	public CommentRankList(CmsJspActionElement jsp, String list_id, CmsMessageContainer container, String campo_orden, CmsListOrderEnum order_descending, String object) {
		super(jsp, list_id, container, campo_orden, order_descending, object);
	}


	public CommentRankList(PageContext context, HttpServletRequest req, HttpServletResponse res) {
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

	private String addFilters()
	{
		String filterHTML="";
		filterHTML+=this.dialogBlock(CmsWorkplace.HTML_START, "Filtros", false);

		filterHTML+="<script src=\"" + getJsp().link("/system/modules/com.tfsla.opencmsdev/resources/js/tipoEdicionSelectors.js") + "\" type=\"text/javascript\"></script>\n";

		filterHTML+="<script type=\"text/javascript\">\n";
		filterHTML+="function filtrar()\n";
		filterHTML+="{\n";
		filterHTML+="	theForm = document.forms[\"lp-form\"];\n";
		filterHTML+="	theForm.submit();\n";
		filterHTML+="}\n";

		filterHTML+="</script>\n";


		filterHTML+="<table class='toolsArea' width='100%' cellspacing='0' cellpadding='0' border='0'>\n";
		filterHTML+="<tr><td>\n";


		String siteName = getSiteName(this.getCms());
		

		filterHTML+="<table border='0' width='100%'><tr><td>";

		TipoEdicionService tService = new TipoEdicionService();

		filterHTML+="Publicacion:";
		filterHTML+="<select id=\"tipoEdicion\" name=\"tipoEdicion\" onChange=\"getEdicionesData();getSeccionesData()\" >\n";
		filterHTML+="	<option value=\"-1\" " + (tipoEdicion == -1 ? "selected " : " ") + ">Todos los Tipos de Ediciones</option>\n";
		for (Iterator<TipoEdicion> iter = tService.obtenerTipoEdiciones(siteName).iterator(); iter.hasNext();) {
			TipoEdicion tEdicion = (TipoEdicion) iter.next();
			String selected = (tipoEdicion == tEdicion.getId()) ? "selected" : "";
			filterHTML+="	<option value=\"" + tEdicion.getId() + "\" " + selected + ">" + tEdicion.getDescripcion() + "</option>\n";

		}
		filterHTML+="</select>\n";

		filterHTML+="</td><td>\n";
		EdicionService eService = new EdicionService();

		filterHTML+="Edicion:";
		filterHTML+="<select name=\"edicion\" id=\"edicion\" onChange=\"setEdicion();\">\n";
		filterHTML+="	<option value=\"0\" " + (edicion == 0 ? "selected " : " ") + ">Todas las Ediciones</option>\n";
		for (Iterator<Edicion> iter = eService.obtenerEdiciones(tipoEdicion).iterator(); iter.hasNext();) {
			Edicion ed = (Edicion) iter.next();
			String selected = (edicion == ed.getNumero()) ? "selected" : "";
			filterHTML+="	<option value=\"" + ed.getNumero() + "\" " + selected + ">" + ed.getNumero() + "</option>\n";
		}
		filterHTML+="</select>\n";
		filterHTML+="</td>\n";

		filterHTML+="<td>\n";
		filterHTML+="Seccion:";
		filterHTML+="<select name=\"seccion\" id=\"seccion\">\n";
		filterHTML+="	<option value=\"\">Todas las Secciones</option>\n";
		try {
			SeccionDAO sDAO = new SeccionDAO();
			for (Iterator<Seccion> iter = sDAO.getSeccionesByTipoEdicionId(tipoEdicion).iterator(); iter.hasNext();) {
				Seccion sec = (Seccion) iter.next();
				String selected = (seccion!=null && seccion.equals(sec.getName())) ? "selected" : "";
				filterHTML+="	<option value=\"" + sec.getName() + "\"" + selected + ">" + sec.getDescription() + "</option>\n";
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		filterHTML+="</select>\n";

		filterHTML+="</td></tr><tr><td>\n";

		filterHTML+="Antiguedad de las visitas (Horas):";
		filterHTML+="<select name=\"horas\">\n";
		for (int j=168;j>=1;j-=1)
		{
			String selected = (("" +horas).equals("" + j)) ? "selected" : "";
			filterHTML+="	<option value=\"" + j + "\" " + selected + ">" + j + "</option>\n";
		}
		filterHTML+="</select>\n";

		filterHTML+="</td><td>\n";
		filterHTML+="Antiguedad de las notas (Horas):";
		filterHTML+="<select name=\"horasNoticia\">\n";
		for (int j=168;j>=1;j-=1)
		{
			String selected = (("" +horasNoticia).equals("" + j)) ? "selected" : "";
			filterHTML+="	<option value=\"" + j + "\" " + selected + ">" + j + "</option>\n";
		}

		
		filterHTML+="</select>\n";
		filterHTML+="<select name=\"fecha\">\n";
		filterHTML+="	<option value=\"" + FECHANO +"\" " + (campo.equals(FECHANO) ? "selected" : "") + " >Sin Restriccion de Fecha</option>\n";
		filterHTML+="	<option value=\"" + FECHAULTIMAMODIFICACION +"\" " + (campo.equals(FECHAULTIMAMODIFICACION) ? "selected" : "") + " >Fecha Ultima Modificacion</option>\n";
		filterHTML+="	<option value=\"" + FECHAMODIFICACION +"\" " + (campo.equals(FECHAMODIFICACION) ? "selected" : "") + " >Fecha Modificacion</option>\n";
		filterHTML+="	<option value=\"" + FECHACREACION +"\" " + (campo.equals(FECHACREACION) ? "selected" : "") + " >Fecha Creacion</option>\n";
		filterHTML+="</select>\n";

		filterHTML+="</td><td>\n";

		filterHTML+="Cantidad:";
		filterHTML+="<select name=\"cantidad\">\n";
		for (int j=20;j<=100;j+=20)
		{
			String selected = (("" +number).equals("" + j)) ? "selected" : "";
			filterHTML+="	<option value=\"" + j + "\" " + selected + ">" + j + "</option>\n";
		}
		filterHTML+="</select>\n";

		filterHTML+="Pagina:";
		filterHTML+="<select name=\"pagina\">\n";
		for (int j=168;j>=1;j-=1)
		{
			String selected = (("" +pagina).equals("" + j)) ? "selected" : "";
			filterHTML+="	<option value=\"" + j + "\" " + selected + ">" + j + "</option>\n";
		}
		filterHTML+="</select>\n";

		filterHTML+="</td></tr>\n";

		
		filterHTML+="<tr><td>\n";
		
		filterHTML+="Tags:";
		filterHTML+="<input type=\"text\" name=\"tags\" id=\"tags\">\n";

		filterHTML+="</td>\n";

		
		filterHTML+="<td>\n";

		filterHTML+="Ranking:";
		filterHTML+="<select name=\"modo\" id=\"modo\">\n";
		filterHTML+="	<option value=\"" + TfsStatisticsOptions.RANK_RECOMENDACION + "\" " + (rankMode == TfsStatisticsOptions.RANK_RECOMENDACION ? "selected " : " ") + ">Recomendaciones</option>\n";

		filterHTML+="	<option value=\"" + TfsStatisticsOptions.RANK_VALORACIONES_PROMEDIO + "\" " + (rankMode == TfsStatisticsOptions.RANK_VALORACIONES_PROMEDIO ? "selected " : " ") + ">Valoraciones Promedio</option>\n";
		filterHTML+="	<option value=\"" + TfsStatisticsOptions.RANK_VALORACIONES_CANTIDAD + "\" " + (rankMode == TfsStatisticsOptions.RANK_VALORACIONES_CANTIDAD ? "selected " : " ") + ">Valoraciones Totales</option>\n";
		filterHTML+="	<option value=\"" + TfsStatisticsOptions.RANK_VALORACIONES_POSITIVO + "\" " + (rankMode == TfsStatisticsOptions.RANK_VALORACIONES_POSITIVO ? "selected " : " ") + ">Valoraciones Positivas</option>\n";
		filterHTML+="	<option value=\"" + TfsStatisticsOptions.RANK_VALORACIONES_NEGATIVO + "\" " + (rankMode == TfsStatisticsOptions.RANK_VALORACIONES_NEGATIVO ? "selected " : " ") + ">Valoraciones Negativas</option>\n";

		filterHTML+="	<option value=\"" + TfsStatisticsOptions.RANK_GENERAL + "\" " + (rankMode == TfsStatisticsOptions.RANK_GENERAL ? "selected " : " ") + ">Ranking General</option>\n";

		filterHTML+="	<option value=\"" + TfsStatisticsOptions.RANK_CUSTOM1 + "\" " + (rankMode == TfsStatisticsOptions.RANK_CUSTOM1 ? "selected " : " ") + ">Custom 1</option>\n";
		filterHTML+="	<option value=\"" + TfsStatisticsOptions.RANK_CUSTOM2 + "\" " + (rankMode == TfsStatisticsOptions.RANK_CUSTOM2 ? "selected " : " ") + ">Custom 2</option>\n";
		filterHTML+="	<option value=\"" + TfsStatisticsOptions.RANK_CUSTOM3 + "\" " + (rankMode == TfsStatisticsOptions.RANK_CUSTOM3 ? "selected " : " ") + ">Custom 3</option>\n";
		filterHTML+="	<option value=\"" + TfsStatisticsOptions.RANK_CUSTOM4 + "\" " + (rankMode == TfsStatisticsOptions.RANK_CUSTOM4 ? "selected " : " ") + ">Custom 4</option>\n";
		filterHTML+="	<option value=\"" + TfsStatisticsOptions.RANK_CUSTOM5 + "\" " + (rankMode == TfsStatisticsOptions.RANK_CUSTOM5 ? "selected " : " ") + ">Custom 5</option>\n";
		filterHTML+="	<option value=\"" + TfsStatisticsOptions.RANK_CUSTOM6 + "\" " + (rankMode == TfsStatisticsOptions.RANK_CUSTOM6 ? "selected " : " ") + ">Custom 6</option>\n";
		filterHTML+="	<option value=\"" + TfsStatisticsOptions.RANK_CUSTOM7 + "\" " + (rankMode == TfsStatisticsOptions.RANK_CUSTOM7 ? "selected " : " ") + ">Custom 7</option>\n";
		filterHTML+="	<option value=\"" + TfsStatisticsOptions.RANK_CUSTOM8 + "\" " + (rankMode == TfsStatisticsOptions.RANK_CUSTOM8 ? "selected " : " ") + ">Custom 8</option>\n";
		filterHTML+="	<option value=\"" + TfsStatisticsOptions.RANK_CUSTOM9 + "\" " + (rankMode == TfsStatisticsOptions.RANK_CUSTOM9 ? "selected " : " ") + ">Custom 9</option>\n";
		filterHTML+="	<option value=\"" + TfsStatisticsOptions.RANK_CUSTOM10 + "\" " + (rankMode == TfsStatisticsOptions.RANK_CUSTOM10 ? "selected " : " ") + ">Custom 10</option>\n";

		filterHTML+="</select>\n";
		filterHTML+="</td></tr>\n";


		filterHTML+="<p>&nbsp;<br><center><table width='100' cellpadding='1' cellspacing='0' class='list'>\n";
		filterHTML+="<tr>\n";
		filterHTML+="<th>\n";
		filterHTML+="	<div class='screenTitle'>\n";
		filterHTML+="	<span class=\"link\" onClick=\"filtrar();\"><a href=\'#\'>Filtrar</a></span><div class='help' id='helplpFiltrar' onMouseOver=\"sMH('lpFiltrar');\" onMouseOut=\"hMH('lpFiltrar');\">Pulse aquí para filtrar los resultados.</div>\n";
		filterHTML+="	</div>\n";
		filterHTML+="</th>\n";
		filterHTML+="</tr>\n";
		filterHTML+="</table></center>\n";


		filterHTML+=this.dialogBlock(CmsWorkplace.HTML_END, "Filtros", false);

		return filterHTML;
	}


	@Override
	public void executeListMultiActions() throws IOException, ServletException, CmsRuntimeException {
		// TODO Auto-generated method stub

	}

	@Override
	public void executeListSingleActions() throws IOException, ServletException, CmsRuntimeException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void fillDetails(String arg0) {
		// TODO Auto-generated method stub

	}


	protected void getParameters(HttpServletRequest req)
	{

		if (req.getParameter("cantidad")!=null)
		{

			if (req.getParameter("modo")!=null)
				rankMode = Integer.parseInt(req.getParameter("modo"));

			String strEdicion = req.getParameter("edicion");
			if (strEdicion !=null)
				edicion = Integer.parseInt(strEdicion);

			String strTipoEdicion = req.getParameter("tipoEdicion");
			if (strTipoEdicion !=null)
				tipoEdicion = Integer.parseInt(strTipoEdicion);

			seccion = req.getParameter("seccion");
			if ((seccion != null && seccion.length() ==0) || seccion=="")
				seccion = null;

			String cantidad = req.getParameter("cantidad");
			if (cantidad !=null)
				number = Integer.parseInt(cantidad);

			String page = req.getParameter("pagina");
			if (page !=null)
				pagina = Integer.parseInt(page);
			
			String hourNews = req.getParameter("horasNoticia");
			if (hourNews !=null)
				horasNoticia = Integer.parseInt(hourNews);

			campo = req.getParameter("fecha");

			String sHoras = req.getParameter("horas");
			if (cantidad !=null)
				horas = Integer.parseInt(sHoras);

			

			String tagsList = req.getParameter("tags");
			if (tagsList!=null && tagsList.trim().length()>0)
			{
				tags = tagsList.split(",");
			}
			
		}

	}

	
	protected void addColumn(CmsListMetadata metadata, String columnID, String i18nKey) {
		CmsListColumnDefinition cmsListColumnDefinition = new CmsListColumnDefinition(columnID);
		cmsListColumnDefinition.setName(Messages.get().container(i18nKey));
		cmsListColumnDefinition.setTextWrapping(true);
		cmsListColumnDefinition.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
		metadata.addColumn(cmsListColumnDefinition);
	}

	protected String getSiteName(CmsObject cms)
	{
		
		String siteName = OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot();
		siteName = siteName.replace("/sites/", "");
		return siteName;
	}

	@Override
	protected void setIndependentActions(CmsListMetadata arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void setMultiActions(CmsListMetadata arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void setColumns(CmsListMetadata metadata) {
		
		CmsListColumnDefinition textColumn = addColumn(metadata, "texto",
				Messages.COMMENTS_LIST_TEXTO_COLUMN, "20%");
		textColumn.setFormatter(new CommentTextFormatter("/system/workplace/admin/admcomments/textdetail.html"));

		addColumn(metadata, "url", Messages.COMENTARIOS_LIST_URL_NOTICIA_COLUMN);

		addColumn(metadata, "Titulo", Messages.COMENTARIOS_LIST_TITULO_NOTICIA_COLUMN);
		addColumn(metadata, "seccion", Messages.COMENTARIOS_LIST_SECCION_NOTICIA_COLUMN);
		addColumn(metadata, "fechaUltimaModificacion", Messages.COMENTARIOS_LIST_FECHA_NOTICIA_COLUMN);
		addColumn(metadata, "valor", Messages.COMENTARIOS_LIST_CANTIDAD_NOTICIA_COLUMN);		
	}

	protected TfsStatisticsOptions getOptions()
	{
        TfsStatisticsOptions options = new TfsStatisticsOptions();

        if (edicion!=0)
        	options.setEdicion(edicion);
        if (tipoEdicion!=-1)
        	options.setTipoEdicion(tipoEdicion);
        if (seccion!=null)
        	options.setSeccion(seccion);
        
        options.setSitio(getSiteName(this.getCms()));


        Calendar fromRecurso = new GregorianCalendar();
        fromRecurso.add(Calendar.HOUR, -1 * horasNoticia);
        
        Calendar to = new GregorianCalendar();

        if (!campo.equals(FECHANO))
        { 
        	options.setCampoFechaRecurso(campo);
            options.setFromDateRecurso(fromRecurso.getTime());
            options.setToDateRecurso(to.getTime());
            
        }
        
        
        if (tags==null)
        	tags = new String[0];
        
        options.setTags( tags);
        
        try {
			options.setTipoContenido("" + OpenCms.getResourceManager().getResourceType("comentario").getTypeId());
		} catch (CmsLoaderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        options.setCount(number);
        options.setPage(pagina);


        Calendar from = new GregorianCalendar();
        from.add(Calendar.HOUR, -1 * horas);

        options.setFrom(from.getTime());
        options.setTo(to.getTime());
        
        options.setShowComentarios(false);
        options.setShowGeneralRank(false);
        options.setShowHits(false);
        options.setShowRecomendacion(false);
        options.setShowValoracion(false);
        options.setShowCantidadValoracion(false);
        options.setShowCustom1(false);
        options.setShowCustom2(false);
        options.setShowCustom3(false);
        options.setShowCustom4(false);
        options.setShowCustom5(false);
        options.setShowCustom6(false);
        options.setShowCustom7(false);
        options.setShowCustom8(false);
        options.setShowCustom9(false);
        options.setShowCustom10(false);

		switch (rankMode)
		{
		case TfsStatisticsOptions.RANK_VALORACIONES_PROMEDIO:
		case TfsStatisticsOptions.RANK_VALORACIONES_CANTIDAD:
		case TfsStatisticsOptions.RANK_VALORACIONES_POSITIVO:
		case TfsStatisticsOptions.RANK_VALORACIONES_NEGATIVO:
			options.setShowValoracion(true);
			break;
		case TfsStatisticsOptions.RANK_COMENTARIOS:
			options.setShowComentarios(true);
			break;
		case TfsStatisticsOptions.RANK_RECOMENDACION:
			options.setShowRecomendacion(true);
			break;
		case TfsStatisticsOptions.RANK_HITS:
			options.setShowHits(true);
			break;
		case TfsStatisticsOptions.RANK_GENERAL:
			options.setShowGeneralRank(true);
			break;
		case TfsStatisticsOptions.RANK_CUSTOM1:
			options.setShowCustom1(true);
			break;
		case TfsStatisticsOptions.RANK_CUSTOM2:
			options.setShowCustom2(true);
			break;
		case TfsStatisticsOptions.RANK_CUSTOM3:
			options.setShowCustom3(true);
			break;
		case TfsStatisticsOptions.RANK_CUSTOM4:
			options.setShowCustom4(true);
			break;
		case TfsStatisticsOptions.RANK_CUSTOM5:
			options.setShowCustom5(true);
			break;
		case TfsStatisticsOptions.RANK_CUSTOM6:
			options.setShowCustom6(true);
			break;
		case TfsStatisticsOptions.RANK_CUSTOM7:
			options.setShowCustom7(true);
			break;
		case TfsStatisticsOptions.RANK_CUSTOM8:
			options.setShowCustom8(true);
			break;
		case TfsStatisticsOptions.RANK_CUSTOM9:
			options.setShowCustom9(true);
			break;
		case TfsStatisticsOptions.RANK_CUSTOM10:
			options.setShowCustom10(true);
			break;
		}

        options.setRankMode(rankMode);
        
        return options;
	}
	
	@Override
	protected List getListItems() throws CmsException {

		TfsStatisticsOptions options = getOptions();
		
		List<CmsListItem> results = new ArrayList<CmsListItem>();

			RankService rService = new RankService();
			TfsRankResults res= rService.getStatistics(getCms(), options);

			if (res!=null && res.getRank()!=null)
			{
				for (TfsHitPage masVisitado : res.getRank()) {
					try {
						addItem(results,masVisitado);
					}
					catch (Exception e){
						e.printStackTrace();
					}
				}
			}

		return results;

	}

	protected void addItem(List<CmsListItem> results, TfsHitPage masVisitado) throws CmsException
	{
		String url = getCms().getRequestContext().removeSiteRoot(masVisitado.getURL());

		String urlElemento = masVisitado.getURL();

		String seccion = "";
		String lFecha ="";
		String titulo = "";
		String tipo = "";
		String id = "";
		String text = "";
		
		I_statisticsDataCollector collector = DataCollectorManager.getInstance().getDataCollector(masVisitado.getTipoContenido());

		if (collector==null)
			DataCollectorManager.getInstance().getDataCollector(TfsRankXmlPageDataCollector.class);

		tipo = collector.getContentName();

		try {
			titulo = collector.getValue(getCms(), url, "Title");
			seccion = collector.getValue(getCms(), url, "seccion");
			lFecha = collector.getDateValue(getMessages(), getCms(), url, "ultimaModificacion");
			urlElemento = collector.getValue(getCms(), url, "url");
			text = collector.getValue(getCms(), url, "text");
		} catch (Exception e) {
			e.printStackTrace();
		}

		
		CmsListItem listItem = this.getList().newItem(masVisitado.getURL());

		listItem.set("Titulo",(titulo == null ? "" : titulo));
		
		text = text.replace("<", "[");
		text = text.replace(">", "]");
		text = text.replace("&lt;", "[");
		text = text.replace("&gt;", "]");
		text = text.replace("'", "&uml;");
		text = text.replace("\"", "&uml;");
		
		listItem.set("texto", text);

		listItem.set("url",urlElemento);

		switch (rankMode)
		{
		case TfsStatisticsOptions.RANK_VALORACIONES_PROMEDIO:
			listItem.set("valor",  Double.toString(((double)masVisitado.getValoracion()) /100000) );
			break;
		case TfsStatisticsOptions.RANK_VALORACIONES_CANTIDAD:
		case TfsStatisticsOptions.RANK_VALORACIONES_POSITIVO:
		case TfsStatisticsOptions.RANK_VALORACIONES_NEGATIVO:
			listItem.set("valor",masVisitado.getValoracion());
			break;
		case TfsStatisticsOptions.RANK_RECOMENDACION:
			listItem.set("valor",masVisitado.getRecomendacion());
			break;
		case TfsStatisticsOptions.RANK_HITS:
			listItem.set("valor",masVisitado.getCantidad());
			break;
		case TfsStatisticsOptions.RANK_GENERAL:
			listItem.set("valor",masVisitado.getGeneralRank());
			break;
		case TfsStatisticsOptions.RANK_CUSTOM1:
			listItem.set("valor",masVisitado.getCustom1());
			break;
		case TfsStatisticsOptions.RANK_CUSTOM2:
			listItem.set("valor",masVisitado.getCustom2());
			break;
		case TfsStatisticsOptions.RANK_CUSTOM3:
			listItem.set("valor",masVisitado.getCustom3());
			break;
		case TfsStatisticsOptions.RANK_CUSTOM4:
			listItem.set("valor",masVisitado.getCustom4());
			break;
		case TfsStatisticsOptions.RANK_CUSTOM5:
			listItem.set("valor",masVisitado.getCustom5());
			break;
		case TfsStatisticsOptions.RANK_CUSTOM6:
			listItem.set("valor",masVisitado.getCustom6());
			break;
		case TfsStatisticsOptions.RANK_CUSTOM7:
			listItem.set("valor",masVisitado.getCustom7());
			break;
		case TfsStatisticsOptions.RANK_CUSTOM8:
			listItem.set("valor",masVisitado.getCustom8());
			break;
		case TfsStatisticsOptions.RANK_CUSTOM9:
			listItem.set("valor",masVisitado.getCustom9());
			break;
		case TfsStatisticsOptions.RANK_CUSTOM10:
			listItem.set("valor",masVisitado.getCustom10());
			break;

			
			
		}

		listItem.set("seccion",seccion);
		listItem.set("fechaUltimaModificacion",lFecha);
		
		results.add(listItem);
	}


	protected Map<String, List<String>> parseParams(String url) throws UnsupportedEncodingException {
		Map<String, List<String>> params = new HashMap<String, List<String>>(); 
		String[] urlParts = url.split("\\?"); 
		if (urlParts.length > 1) {
			String query = urlParts[1];     
			for (String param : query.split("&")) 
			{         
				String[] pair = param.split("=");
				String key = URLDecoder.decode(pair[0], "UTF-8");
				String value = URLDecoder.decode(pair[1], "UTF-8");
				List<String> values = params.get(key);
				if (values == null) {
					values = new ArrayList<String>();
					params.put(key, values);
				}
				values.add(value);
			} 
		}
		return params;
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

}
