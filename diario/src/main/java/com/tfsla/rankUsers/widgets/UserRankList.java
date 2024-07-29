package com.tfsla.rankUsers.widgets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.site.CmsSite;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.list.A_CmsListDialog;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListOrderEnum;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import com.tfsla.rankUsers.model.TfsUserRankResults;
import com.tfsla.rankUsers.service.RankService;
import com.tfsla.rankUsers.widgets.Messages;
import com.tfsla.statistics.model.TfsHitUser;
import com.tfsla.statistics.model.TfsUserStatsOptions;
import org.opencms.util.CmsUUID;

public class UserRankList extends A_CmsListDialog {

	
	//TODO:Agregar OU
	
	public static final String LIST_ID = "lp";
	protected static final String CAMPO_ORDEN = "ranking";

	String grupo=null;
	String ou=null;
	protected int horas = 24;
	protected int number = 20;
	protected int page = 1;
	
	int rankMode = TfsUserStatsOptions.RANK_VISITASRECIBIDAS; //General RANK
	
	public UserRankList(CmsJspActionElement jsp) {
		super( jsp, LIST_ID, Messages.get().container(Messages.GUI_RNK_USUARIOS_LIST_NAME_0), CAMPO_ORDEN,
			CmsListOrderEnum.ORDER_DESCENDING, null);
	}

	public UserRankList(CmsJspActionElement jsp, String list_id, CmsMessageContainer container, String campo_orden, CmsListOrderEnum order_descending, String object) {
		super(jsp, list_id, container, campo_orden, order_descending, object);
	}

	public UserRankList(PageContext context, HttpServletRequest req, HttpServletResponse res) {
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

	@SuppressWarnings("unchecked")
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

		
		//Agregar Grupos.
		filterHTML+="Modo Ranking:";
		filterHTML+="<select name=\"rMode\">\n";
		filterHTML+="	<option value=\"" + TfsUserStatsOptions.RANK_GENERAL + "\" " + (rankMode==TfsUserStatsOptions.RANK_GENERAL ?  " selected " : "") + ">General</option>\n";
		filterHTML+="	<option value=\"" + TfsUserStatsOptions.RANK_VISITASRECIBIDAS + "\" " + (rankMode==TfsUserStatsOptions.RANK_VISITASRECIBIDAS ?  " selected " : "") + ">Visitas recibidas</option>\n";
		filterHTML+="	<option value=\"" + TfsUserStatsOptions.RANK_NOTASPUBLICADAS + "\" " + (rankMode==TfsUserStatsOptions.RANK_NOTASPUBLICADAS ?  " selected " : "") + ">Notas Publicadas</option>\n";
		filterHTML+="	<option value=\"" + TfsUserStatsOptions.RANK_RECOMENDACIONESRECIBIDAS + "\" " + (rankMode==TfsUserStatsOptions.RANK_RECOMENDACIONESRECIBIDAS ?  " selected " : "") + ">Recomendaciones recibidas</option>\n";
		filterHTML+="	<option value=\"" + TfsUserStatsOptions.RANK_COMENTARIOSRECIBIDOS + "\" " + (rankMode==TfsUserStatsOptions.RANK_COMENTARIOSRECIBIDOS ?  " selected " : "") + ">Comentarios recibidos</option>\n";
		filterHTML+="	<option value=\"" + TfsUserStatsOptions.RANK_COMENTARIOSREALIZADOS + "\" " + (rankMode==TfsUserStatsOptions.RANK_COMENTARIOSREALIZADOS ?  " selected " : "") + ">Comentarios realizados</option>\n";
		filterHTML+="	<option value=\"" + TfsUserStatsOptions.RANK_COMENTARIOSRECHAZADOS + "\" " + (rankMode==TfsUserStatsOptions.RANK_COMENTARIOSRECHAZADOS ?  " selected " : "") + ">Comentarios rechazados</option>\n";
		filterHTML+="	<option value=\"" + TfsUserStatsOptions.RANK_VALORACIONES_CANTIDAD + "\" " + (rankMode==TfsUserStatsOptions.RANK_VALORACIONES_CANTIDAD ?  " selected " : "") + ">Cantidad de valoraciones</option>\n";
		filterHTML+="	<option value=\"" + TfsUserStatsOptions.RANK_VALORACIONES_PROMEDIO + "\" " + (rankMode==TfsUserStatsOptions.RANK_VALORACIONES_PROMEDIO ?  " selected " : "") + ">Promedio valoraciones</option>\n";
		filterHTML+="	<option value=\"" + TfsUserStatsOptions.RANK_VALORACIONES_POSITIVO + "\" " + (rankMode==TfsUserStatsOptions.RANK_VALORACIONES_POSITIVO ?  " selected " : "") + ">Valoraciones positivas</option>\n";
		filterHTML+="	<option value=\"" + TfsUserStatsOptions.RANK_VALORACIONES_NEGATIVO + "\" " + (rankMode==TfsUserStatsOptions.RANK_VALORACIONES_NEGATIVO ?  " selected " : "") + ">Valoraciones negativas</option>\n";
		filterHTML+="	<option value=\"" + TfsUserStatsOptions.RANK_CUSTOM1 + "\" " + (rankMode==TfsUserStatsOptions.RANK_CUSTOM1 ?  " selected " : "") + ">Custom 1</option>\n";
		filterHTML+="	<option value=\"" + TfsUserStatsOptions.RANK_CUSTOM2 + "\" " + (rankMode==TfsUserStatsOptions.RANK_CUSTOM2 ?  " selected " : "") + ">Custom 2</option>\n";
		filterHTML+="	<option value=\"" + TfsUserStatsOptions.RANK_CUSTOM3 + "\" " + (rankMode==TfsUserStatsOptions.RANK_CUSTOM3 ?  " selected " : "") + ">Custom 3</option>\n";
		filterHTML+="	<option value=\"" + TfsUserStatsOptions.RANK_CUSTOM4 + "\" " + (rankMode==TfsUserStatsOptions.RANK_CUSTOM4 ?  " selected " : "") + ">Custom 4</option>\n";
		filterHTML+="	<option value=\"" + TfsUserStatsOptions.RANK_CUSTOM5 + "\" " + (rankMode==TfsUserStatsOptions.RANK_CUSTOM5 ?  " selected " : "") + ">Custom 5</option>\n";
		filterHTML+="	<option value=\"" + TfsUserStatsOptions.RANK_CUSTOM6 + "\" " + (rankMode==TfsUserStatsOptions.RANK_CUSTOM6 ?  " selected " : "") + ">Custom 6</option>\n";
		filterHTML+="	<option value=\"" + TfsUserStatsOptions.RANK_CUSTOM7 + "\" " + (rankMode==TfsUserStatsOptions.RANK_CUSTOM7 ?  " selected " : "") + ">Custom 7</option>\n";
		filterHTML+="	<option value=\"" + TfsUserStatsOptions.RANK_CUSTOM8 + "\" " + (rankMode==TfsUserStatsOptions.RANK_CUSTOM8 ?  " selected " : "") + ">Custom 8</option>\n";
		filterHTML+="	<option value=\"" + TfsUserStatsOptions.RANK_CUSTOM9 + "\" " + (rankMode==TfsUserStatsOptions.RANK_CUSTOM9 ?  " selected " : "") + ">Custom 9</option>\n";
		filterHTML+="	<option value=\"" + TfsUserStatsOptions.RANK_CUSTOM10 + "\" " + (rankMode==TfsUserStatsOptions.RANK_CUSTOM10 ?  " selected " : "") + ">Custom 10</option>\n";
		
		filterHTML+="</select>\n";
		filterHTML+="</td><td>\n";

		List<CmsOrganizationalUnit> ous = null;
		try {
			ous = OpenCms.getOrgUnitManager().getOrganizationalUnits(getCms(), "", true);
		} catch (CmsException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		filterHTML+="OU:";
		filterHTML+="<select name=\"ou\">\n";
		filterHTML+="	<option value=\"\" >Todos</option>\n";
		
		if (ous!=null)
			for (CmsOrganizationalUnit cmsOu : ous)
			{
				filterHTML+="	<option value=\"" + cmsOu.getSimpleName() + "\" " + (ou!=null && ou.equals(cmsOu.getSimpleName()) ? " selected " : "" )+ ">" + cmsOu.getDescription() + "</option>\n";
				
			}
		
		filterHTML+="</select>\n";
		
		
		List<CmsGroup> groups = null;
		try {
			groups = OpenCms.getOrgUnitManager().getGroups(getCms(), "", true);
		} catch (CmsException e) {
			e.printStackTrace();
		}
		
		filterHTML+="Grupo:";
		filterHTML+="<select name=\"grupo\">\n";
		filterHTML+="	<option value=\"\" >Todos</option>\n";
		
		if (groups!=null)
			for (CmsGroup cmsGrupo : groups)
			{
				filterHTML+="	<option value=\"" + cmsGrupo.getId().getStringValue() + "\" " + (grupo!=null && grupo.equals(cmsGrupo.getId().getStringValue()) ? " selected " : "" )+ ">" + cmsGrupo.getName() + "</option>\n";
				
			}
		
		filterHTML+="</select>\n";
		
		
		filterHTML+="</td></tr><tr><td>\n";

		filterHTML+="Maxima antiguedad de los eventos (Horas):";
		filterHTML+="<select name=\"horas\">\n";
		for (int j=744;j>48;j-=24)
		{
			String selected = (("" +horas).equals("" + j)) ? "selected" : "";
			filterHTML+="	<option value=\"" + j + "\" " + selected + ">" + j + "</option>\n";
		}
		for (int j=48;j>=1;j-=1)
		{
			String selected = (("" +horas).equals("" + j)) ? "selected" : "";
			filterHTML+="	<option value=\"" + j + "\" " + selected + ">" + j + "</option>\n";
		}
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
		for (int j=1;j<=20;j++)
		{
			String selected = (("" +page).equals("" + j)) ? "selected" : "";
			filterHTML+="	<option value=\"" + j + "\" " + selected + ">" + j + "</option>\n";
		}
		filterHTML+="</select>\n";

		filterHTML+="</td></tr>\n";
		filterHTML+="</table>\n";
		
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


	private String getSiteName(CmsObject cms)
	{
		CmsSite site = OpenCms.getSiteManager().getCurrentSite(cms);
		String siteName = site.getSiteRoot(); 
		return siteName.replace("/sites/", "");
	}

	protected void getParameters(HttpServletRequest req)
	{

		if (req.getParameter("cantidad")!=null)
		{
			
			String cantidad = req.getParameter("cantidad");
			if (cantidad !=null)
				number = Integer.parseInt(cantidad);

			String pagina = req.getParameter("pagina");
			if (pagina !=null)
				page = Integer.parseInt(pagina);

			String sHoras = req.getParameter("horas");
			if (sHoras !=null)
				horas = Integer.parseInt(sHoras);

			String sModRanking = req.getParameter("rMode");
			if (sModRanking !=null)
				rankMode = Integer.parseInt(sModRanking);
			
			grupo = req.getParameter("grupo");
			ou = req.getParameter("ou");
			
		}
	}
	
	protected void addColumn(CmsListMetadata metadata, String columnID, String i18nKey) {
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


	protected TfsUserStatsOptions getOptions()
	{
		TfsUserStatsOptions options = new TfsUserStatsOptions();

        
        options.setSitio(getSiteName(this.getCms()));

        options.setRankMode(rankMode);
        
        options.setCount(number);

        Calendar from = new GregorianCalendar();
        from.add(Calendar.HOUR, -1 * horas);
        
        Calendar to = new GregorianCalendar();
        
        if (grupo!=null && !grupo.trim().equals(""))
        	options.setGrupos(new String[] {grupo});
        else
        	options.setGrupos(new String[0]);
        
        if (ou!=null && !ou.trim().equals(""))
        	options.setOu(ou);

        options.setFrom(from.getTime());
        options.setTo(to.getTime());
        
        //options.setRankMode(rankMode);

        options.setShowCantidadValoraciones(false);
        options.setShowComentariosRealizados(false);
        options.setShowComentariosRechazados(false);
        options.setShowComentariosRecibidos(false);
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
        options.setShowGeneralRank(false);
        options.setShowNotasPublicadas(false);
        options.setShowRecomendacionesRecibidas(false);
        options.setShowValoracionesRecibidas(false);
        options.setShowVisitasRecibidas(false);
        
		switch (rankMode) {
		case TfsUserStatsOptions.RANK_GENERAL:
			options.setShowGeneralRank(true);
			break;
		case TfsUserStatsOptions.RANK_VISITASRECIBIDAS:
			options.setShowVisitasRecibidas(true);
			break;
		case TfsUserStatsOptions.RANK_NOTASPUBLICADAS:
			options.setShowNotasPublicadas(true);
				break;
		case TfsUserStatsOptions.RANK_RECOMENDACIONESRECIBIDAS:
			options.setShowRecomendacionesRecibidas(true);
			break;
		case TfsUserStatsOptions.RANK_COMENTARIOSRECIBIDOS:
			options.setShowComentariosRecibidos(true);
			break;
		case TfsUserStatsOptions.RANK_COMENTARIOSREALIZADOS:
			options.setShowComentariosRealizados(true);
			break;
		case TfsUserStatsOptions.RANK_COMENTARIOSRECHAZADOS:
			options.setShowComentariosRechazados(true);
			break;
		case TfsUserStatsOptions.RANK_VALORACIONES_CANTIDAD:
			options.setShowCantidadValoraciones(true);
			break;
		case TfsUserStatsOptions.RANK_VALORACIONES_PROMEDIO:
		case TfsUserStatsOptions.RANK_VALORACIONES_POSITIVO:
		case TfsUserStatsOptions.RANK_VALORACIONES_NEGATIVO:
			options.setShowValoracionesRecibidas(true);
			break;
		case TfsUserStatsOptions.RANK_CUSTOM1:
			options.setShowCustom1(true);
			break;
		case TfsUserStatsOptions.RANK_CUSTOM2:
			options.setShowCustom2(true);
			break;
		case TfsUserStatsOptions.RANK_CUSTOM3:
			options.setShowCustom3(true);
			break;
		case TfsUserStatsOptions.RANK_CUSTOM4:
			options.setShowCustom4(true);
			break;
		case TfsUserStatsOptions.RANK_CUSTOM5:
			options.setShowCustom5(true);
			break;
		case TfsUserStatsOptions.RANK_CUSTOM6:
			options.setShowCustom6(true);
			break;
		case TfsUserStatsOptions.RANK_CUSTOM7:
			options.setShowCustom7(true);
			break;
		case TfsUserStatsOptions.RANK_CUSTOM8:
			options.setShowCustom8(true);
			break;
		case TfsUserStatsOptions.RANK_CUSTOM9:
			options.setShowCustom9(true);
			break;
		case TfsUserStatsOptions.RANK_CUSTOM10:
			options.setShowCustom10(true);
			break;
		default:
			break;
		}

        return options;
	}

	@Override
	protected List getListItems() throws CmsException {

		TfsUserStatsOptions options = getOptions();
		
		List<CmsListItem> results = new ArrayList<CmsListItem>();

		try {

			RankService rService = new RankService();
			TfsUserRankResults res = rService.getStatistics(options);
			if (res.getRank()!=null)
			{
				for (TfsHitUser masVisitado : res.getRank()) {
					try {
						addItem(results,masVisitado);
					}
					catch (Exception e){
						e.printStackTrace();
					}
				}
			}

		} catch (RemoteException e1) {
			e1.printStackTrace();
		}

		return results;

	}

	@Override
	protected void setColumns(CmsListMetadata metadata) {
		addColumn(metadata, "usuario", Messages.RNK_USUARIOS_LIST_USUARIO_COLUMN);
		addColumn(metadata, "ou", Messages.RNK_USUARIOS_LIST_UO_COLUMN);
		addColumn(metadata, "ranking", Messages.RNK_USUARIOS_LIST_RANKING_COLUMN);
		
	}

	protected void addItem(List<CmsListItem> results, TfsHitUser user) throws CmsException
	{

		//CmsGroup cmsGroup = getCms().readGroup(user.getGrupo());
		CmsUser cmsUser = getCms().readUser(new CmsUUID(user.getUsuario()));
		
		CmsListItem listItem = this.getList().newItem(user.getUsuario());

		listItem.set("usuario",cmsUser.getFullName());
		listItem.set("ou",cmsUser.getOuFqn());

//		listItem.set("grupo",cmsGroup.getName());

		switch (rankMode) {
		case TfsUserStatsOptions.RANK_GENERAL:
			listItem.set("ranking",user.getGeneralRank());
			break;
		case TfsUserStatsOptions.RANK_VISITASRECIBIDAS:
			listItem.set("ranking",user.getVisitasRecibidas());
				break;
		case TfsUserStatsOptions.RANK_NOTASPUBLICADAS:
			listItem.set("ranking",user.getNotasPublicadas());
				break;
		case TfsUserStatsOptions.RANK_RECOMENDACIONESRECIBIDAS:
			listItem.set("ranking",user.getRecomendacionesRecibidas());
				break;
		case TfsUserStatsOptions.RANK_COMENTARIOSRECIBIDOS:
			listItem.set("ranking",user.getComentariosRecibidos());
				break;
		case TfsUserStatsOptions.RANK_COMENTARIOSREALIZADOS:
			listItem.set("ranking",user.getComentariosRealizados());
				break;
		case TfsUserStatsOptions.RANK_COMENTARIOSRECHAZADOS:
			listItem.set("ranking",user.getComentariosRechazados());
				break;
		case TfsUserStatsOptions.RANK_VALORACIONES_CANTIDAD:
			listItem.set("ranking",user.getCantidadValoraciones());
				break;
		case TfsUserStatsOptions.RANK_VALORACIONES_PROMEDIO:
			listItem.set("ranking",  Double.toString(((double)user.getValoracionesRecibidas()) /100000) );
			break;
		case TfsUserStatsOptions.RANK_VALORACIONES_POSITIVO:
		case TfsUserStatsOptions.RANK_VALORACIONES_NEGATIVO:
			listItem.set("ranking",user.getValoracionesRecibidas());
				break;
		case TfsUserStatsOptions.RANK_CUSTOM1:
			listItem.set("ranking",user.getCustom1());
				break;
		case TfsUserStatsOptions.RANK_CUSTOM2:
			listItem.set("ranking",user.getCustom2());
				break;
		case TfsUserStatsOptions.RANK_CUSTOM3:
			listItem.set("ranking",user.getCustom3());
				break;
		case TfsUserStatsOptions.RANK_CUSTOM4:
			listItem.set("ranking",user.getCustom4());
				break;
		case TfsUserStatsOptions.RANK_CUSTOM5:
			listItem.set("ranking",user.getCustom5());
			break;
		case TfsUserStatsOptions.RANK_CUSTOM6:
			listItem.set("ranking",user.getCustom6());
				break;
		case TfsUserStatsOptions.RANK_CUSTOM7:
			listItem.set("ranking",user.getCustom7());
				break;
		case TfsUserStatsOptions.RANK_CUSTOM8:
			listItem.set("ranking",user.getCustom8());
				break;
		case TfsUserStatsOptions.RANK_CUSTOM9:
			listItem.set("ranking",user.getCustom9());
				break;
		case TfsUserStatsOptions.RANK_CUSTOM10:
			listItem.set("ranking",user.getCustom10());
			break;

		default:
			break;
		}
		
		results.add(listItem);
	}

}
