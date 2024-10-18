package com.tfsla.diario.ediciones.widgets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.PageContext;

import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.CmsInputWidget;
import org.opencms.widgets.CmsSelectWidget;
import org.opencms.widgets.CmsSelectWidgetOption;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWidgetDialog;
import org.opencms.workplace.CmsWidgetDialogParameter;
import org.opencms.workplace.CmsWorkplaceSettings;


//REMINDER: Para perfil comentar la siguiente linea y descomentar la que sigue:
import com.tfsla.diario.ediciones.model.Seccion;
import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.SeccionesService;
import com.tfsla.diario.ediciones.services.TipoEdicionService;
import com.tfsla.diario.ediciones.services.openCmsService;
import com.tfsla.opencmsdev.module.pages.PageConfiguration;
//import com.tfsla.perfil.module.pages.PageConfiguration;

//import com.tfsla.planilla.herramientas.PlanillaEditoresHelper;

public class SeccionesEdit extends CmsWidgetDialog {

	Seccion seccion;

	public static final String PARAM_SECCION_KEY = "seccionId";

	private static final String KEY_PREFIX = "seccion";

	private static final String[] PAGES = { "page1" };

	public SeccionesEdit(PageContext context, HttpServletRequest req, HttpServletResponse res) {
		super(context, req, res);
	}

	@Override
	public void actionCommit() throws IOException, ServletException {
		// TODO Auto-generated method stub
		SeccionesService sService = new SeccionesService();

		List<Exception> errors = new ArrayList<Exception>();

		if (ConstraintsOk())
		{
			if (seccion.isNew())
			{
				sService.crearSeccion(seccion,getCms());
				if (sService.HasError())
				{
					CmsIllegalArgumentException err = new CmsIllegalArgumentException(
							Messages.get().container(
									sService.getErrorDescription()
									)
							);
					errors.add(err);
					this.setCommitErrors(errors);
				}
				else {
					PageConfiguration.getInstance().reload();
//					PlanillaEditoresHelper.reload();

				}
			}
			else
			{
				sService.actualizarSeccion(seccion);
				if (sService.HasError()) {
					throw new RuntimeException(sService.getErrorDescription());
				}
				else {
					PageConfiguration.getInstance().reload();
//					PlanillaEditoresHelper.reload();
				}
			}
		}
	}

	private boolean ConstraintsOk() {
		List<Exception> errors = new ArrayList<Exception>();

		if (seccion.getName().equals(""))
		{
			CmsIllegalArgumentException err = new CmsIllegalArgumentException(
					Messages.get().container(
							Messages.ERR_NAME_VALIDATION_0
							)
					);
			errors.add(err);

		}

		if (!seccion.getName().toLowerCase().equals(seccion.getName()) || seccion.getName().matches(".*[áéíóúÁÉÍÓÚ].*"))
		{
			CmsIllegalArgumentException err = new CmsIllegalArgumentException(
					Messages.get().container(
							Messages.ERR_NAMECASE_VALIDATION_0
							)
					);
			errors.add(err);
		}

		if (seccion.getPage().equals(""))
		{
			CmsIllegalArgumentException err = new CmsIllegalArgumentException(
					Messages.get().container(
							Messages.ERR_PAGE_VALIDATION_0
							)
					);
			errors.add(err);

		}

		if (seccion.getDescription().equals(""))
		{
			CmsIllegalArgumentException err = new CmsIllegalArgumentException(
					Messages.get().container(
							Messages.ERR_DESCRIPTION_VALIDATION_0
							)
					);
			errors.add(err);

		}

		if (errors.size()>0)
			this.setCommitErrors(errors);

		return (errors.size() == 0);
	}


	@Override
	protected void defineWidgets() {
		setKeyPrefix(KEY_PREFIX);

		initSeccion();
		if (seccion.isNew())
		{
			addWidget(newInput("name","Nombre"));

			List<CmsSelectWidgetOption> options=null;
			options = getTipoEdicionesComboOptions();
			addWidget(newCombo("idTipoEdicion","Publicacion",options));
		}
		addWidget(newInput("page","Pagina"));
		addWidget(newInput("description","Descripcion"));
		addWidget(newInput("order","Orden"));
		addWidget(newInput("visibility","Visible"));

		//List<CmsSelectWidgetOption> twitterOptions=null;
		//twitterOptions = getTipoTwitterAccountComboOptions();
		//addWidget(newCombo("twitterAccount","Cuenta Twitter",twitterOptions));
		
		//List<CmsSelectWidgetOption> facebookOptions=null;
		//facebookOptions = getTipoFacebookAccountComboOptions();
		//addWidget(newCombo("facebookAccount","Cuenta facebookAccount",facebookOptions));
		
		//List<CmsSelectWidgetOption> facebookPageOptions=null;
		//facebookPageOptions = getTipoFacebookPageAccountComboOptions();
		//addWidget(newCombo("facebookPageAccount","Cuenta facebookPageAccount",facebookPageOptions));

	}


	private CmsWidgetDialogParameter newCombo(String propertyName, String title, List<CmsSelectWidgetOption> options) {
		return new CmsWidgetDialogParameter(this.seccion, propertyName, title, PAGES[0], new CmsSelectWidget(
				options));
	}

	/*
	private List<CmsSelectWidgetOption> getTipoTwitterAccountComboOptions() throws RuntimeException
	{
		List<CmsSelectWidgetOption> widgetOptions = new ArrayList<CmsSelectWidgetOption>();


		try {
			List<TwitterAccountPublisher> accounts = TwitterService.getInstance().getPublishers(this.getCms());
			CmsSelectWidgetOption option = new CmsSelectWidgetOption("",(this.seccion.getTwitterAccount()==null || this.seccion.getTwitterAccount().equals("")),"","");
			widgetOptions.add(option);

			for (Iterator it = accounts.iterator(); it.hasNext();) {
				TwitterAccountPublisher account = (TwitterAccountPublisher) it.next();
				option = new CmsSelectWidgetOption(account.getName(),(this.seccion.getTwitterAccount()!=null && this.seccion.getTwitterAccount().equals(account.getName())),account.getName(),account.getName());
				widgetOptions.add(option);
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new RuntimeException(e);
		}
		return widgetOptions;
	}
*/
	
	/*
	private List<CmsSelectWidgetOption> getTipoFacebookAccountComboOptions() throws RuntimeException
	{
		List<CmsSelectWidgetOption> widgetOptions = new ArrayList<CmsSelectWidgetOption>();


		try {
			List<FacebookAccountPublisher> accounts = FacebookService.getInstance().getPublishers(this.getCms());
			CmsSelectWidgetOption option = new CmsSelectWidgetOption("",(this.seccion.getFacebookAccount()==null || this.seccion.getFacebookAccount().equals("")),"","");
			widgetOptions.add(option);

			for (Iterator it = accounts.iterator(); it.hasNext();) {
				FacebookAccountPublisher account = (FacebookAccountPublisher) it.next();
				option = new CmsSelectWidgetOption(account.getName(),(this.seccion.getFacebookAccount()!=null && this.seccion.getFacebookAccount().equals(account.getName())),account.getName(),account.getName());
				widgetOptions.add(option);
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new RuntimeException(e);
		}
		return widgetOptions;
	}
	*/
	
	/*
	private List<CmsSelectWidgetOption> getTipoFacebookPageAccountComboOptions() throws RuntimeException
	{
		List<CmsSelectWidgetOption> widgetOptions = new ArrayList<CmsSelectWidgetOption>();


		try {
			List<FacebookPageAccountPublisher> accounts = FacebookPageService.getInstance().getPagePublishers(this.getCms());
			CmsSelectWidgetOption option = new CmsSelectWidgetOption("",(this.seccion.getFacebookPageAccount()==null || this.seccion.getFacebookPageAccount().equals("")),"","");
			widgetOptions.add(option);

			for (Iterator it = accounts.iterator(); it.hasNext();) {
				FacebookPageAccountPublisher account = (FacebookPageAccountPublisher) it.next();
				option = new CmsSelectWidgetOption(account.getName(),(this.seccion.getFacebookPageAccount()!=null && this.seccion.getFacebookPageAccount().equals(account.getName())),account.getName(),account.getName());
				widgetOptions.add(option);
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new RuntimeException(e);
		}
		return widgetOptions;
	}
	*/
	
	private List<CmsSelectWidgetOption> getTipoEdicionesComboOptions() throws RuntimeException
	{
		List<CmsSelectWidgetOption> widgetOptions = new ArrayList<CmsSelectWidgetOption>();

		TipoEdicionService tDAO = new TipoEdicionService();

		String proyecto = openCmsService.getCurrentSite(this.getCms());

		try {
			List<TipoEdicion> tEdiciones = tDAO.obtenerTipoEdiciones(proyecto);

			for (Iterator it = tEdiciones.iterator(); it.hasNext();) {
				TipoEdicion tEdicion = (TipoEdicion) it.next();
				CmsSelectWidgetOption option = new CmsSelectWidgetOption("" + tEdicion.getId(),(this.seccion.getIdTipoEdicion()==tEdicion.getId()),tEdicion.getDescripcion(),tEdicion.getDescripcion());
				widgetOptions.add(option);
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new RuntimeException(e);
		}
		return widgetOptions;
	}


	private CmsWidgetDialogParameter newInput(String propertyName, String title) {
		return new CmsWidgetDialogParameter(this.seccion, propertyName,title, PAGES[0], new CmsInputWidget());
	}

	@Override
	protected String[] getPageArray() {
		return PAGES;
	}

	@Override
	protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {
		// initialize parameters and dialog actions in super implementation
		super.initWorkplaceRequestValues(settings, request);

		// save the current state of the seccion (may be changed because of the
		// widget values)
		this.setDialogObject(this.seccion);
	}

	private void initSeccion() {
		if (this.isInitialCall()) {
			String strSeccionId = this.getSeccionIdFromRequest();
			// edit an existing encuesta, get the encuesta object from db
			if (strSeccionId == null) {
				this.seccion = new Seccion();
			}
			else {
				SeccionesService sService = new SeccionesService();
				int seccionId = Integer.parseInt(strSeccionId);
				this.seccion = sService.obtenerSeccion(seccionId);
			}
		}
		else {
			// this is not the initial call, get the project object from
			// session
			this.seccion = (Seccion) this.getDialogObject();
		}
	}



	private String getSeccionIdFromRequest() {
		return this.getJsp().getRequest().getParameter(PARAM_SECCION_KEY);
	}

	private boolean isInitialCall() {
		return CmsStringUtil.isEmpty(getParamAction()) || CmsDialog.DIALOG_INITIAL.equals(getParamAction());
	}

}
