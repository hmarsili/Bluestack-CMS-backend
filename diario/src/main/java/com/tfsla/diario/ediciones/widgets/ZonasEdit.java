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

import com.tfsla.diario.ediciones.data.PageDAO;
import com.tfsla.diario.ediciones.model.Page;
import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.model.Zona;
import com.tfsla.diario.ediciones.services.TipoEdicionService;
import com.tfsla.diario.ediciones.services.ZonasService;
import com.tfsla.diario.ediciones.services.openCmsService;
import com.tfsla.opencms.widgets.CmsColorpickerWidgetTFS;

//REMINDER: Para perfil comentar la siguiente linea y descomentar la que sigue:
import com.tfsla.opencmsdev.module.pages.PageConfiguration;
//import com.tfsla.perfil.module.pages.PageConfiguration;

//import com.tfsla.planilla.herramientas.ZonasPlanilla;

/**
 * @author Victor Podberezski
 * @version 1.0
 */
public class ZonasEdit extends CmsWidgetDialog {

	Zona zona;

	public static final String PARAM_ZONA_KEY = "zonaId";

	private static final String KEY_PREFIX = "zona";

	private static final String[] PAGES = { "page1" };

	public ZonasEdit(PageContext context, HttpServletRequest req,
			HttpServletResponse res) {
		super(context, req, res);
	}

	private boolean ConstraintsOk() {
		List<Exception> errors = new ArrayList<Exception>();

		if (zona.getOrder() == 0) {
			CmsIllegalArgumentException err = new CmsIllegalArgumentException(
					Messages.get().container(Messages.ERR_ORDER_VALIDATION_0));
			errors.add(err);

		}

		if (zona.getName().equals("")) {
			CmsIllegalArgumentException err = new CmsIllegalArgumentException(
					Messages.get().container(Messages.ERR_NAME_VALIDATION_0));
			errors.add(err);

		}
		if (zona.getColor().equals("")) {
			CmsIllegalArgumentException err = new CmsIllegalArgumentException(
					Messages.get().container(Messages.ERR_COLOR_VALIDATION_0));
			errors.add(err);

		}

		if (!zona.getName().toLowerCase().equals(zona.getName())
				|| zona.getName().matches(".*[áéíóúÁÉÍÓÚ].*")) {
			CmsIllegalArgumentException err = new CmsIllegalArgumentException(
					Messages.get()
							.container(Messages.ERR_NAMECASE_VALIDATION_0));
			errors.add(err);
		}

		if (errors.size() > 0)
			this.setCommitErrors(errors);

		return (errors.size() == 0);

	}

	@Override
	public void actionCommit() throws IOException, ServletException {
		// TODO Auto-generated method stub
		ZonasService zService = new ZonasService();

		List<Exception> errors = new ArrayList<Exception>();

		if (ConstraintsOk()) {
			if (zona.isNew()) {
				zService.crearZona(zona, getCms());
				if (zService.HasError()) {
					CmsIllegalArgumentException err = new CmsIllegalArgumentException(
							Messages.get().container(
									zService.getErrorDescription()));
					errors.add(err);
					this.setCommitErrors(errors);
				} else {
					PageConfiguration.getInstance().reload();
					//ZonasPlanilla.getInstance().reload();
				}
			} else {
				zService.actualizarZona(zona);
				if (zService.HasError())
					throw new RuntimeException(zService.getErrorDescription());
				else {
					PageConfiguration.getInstance().reload();
					//ZonasPlanilla.getInstance().reload();
				}
			}
		}
	}

	@Override
	protected void defineWidgets() throws RuntimeException {
		setKeyPrefix(KEY_PREFIX);

		initZona();

		if (zona.isNew()) {
			addWidget(newInput("name", "Nombre"));
		}
		addWidget(newInput("description", "Descripcion"));
		addWidget(newColorPicker("color", "Color"));
		addWidget(newInput("order", "Orden"));
		addWidget(newInput("visibility", "Visibilidad"));

		if (zona.isNew()) {
			List<CmsSelectWidgetOption> options = null;

			options = getPageComboOptions();
			addWidget(newCombo("idPage", "Pagina", options));

			options = getTipoEdicionesComboOptions();
			addWidget(newCombo("idTipoEdicion", "Publicacion", options));
		}

		addWidget(newInput("sizeDefault", "Cantidad por defecto"));

		List<CmsSelectWidgetOption> options = null;
		options = getOrderListComboOptions();
		addWidget(newCombo("orderDefault", "Orden por defecto", options));
	}

	private List<CmsSelectWidgetOption> getOrderListComboOptions() {
		List<CmsSelectWidgetOption> widgetOptions = new ArrayList<CmsSelectWidgetOption>();
		
		widgetOptions.add(new CmsSelectWidgetOption("priority",
				(this.zona.getOrderDefault().equals("priority")), "priority", "priority"));
		
		widgetOptions.add(new CmsSelectWidgetOption("priority-home",
				(this.zona.getOrderDefault().equals("priority-home")), "priority-home", "priority-home"));
		
		widgetOptions.add(new CmsSelectWidgetOption("priority-section",
				(this.zona.getOrderDefault().equals("priority-section")), "priority-section", "priority-section"));
		
		widgetOptions.add(new CmsSelectWidgetOption("creation-date",
				(this.zona.getOrderDefault().equals("creation-date")), "creation-date", "creation-date"));
		
		widgetOptions.add(new CmsSelectWidgetOption("user-modification-date",
				(this.zona.getOrderDefault().equals("user-modification-date")), "user-modification-date", "user-modification-date"));
		
		widgetOptions.add(new CmsSelectWidgetOption("modification-date",
				(this.zona.getOrderDefault().equals("modification-date")), "modification-date", "modification-date"));

		widgetOptions.add(new CmsSelectWidgetOption("most-read",
				(this.zona.getOrderDefault().equals("most-read")), "most-read", "most-read"));

		widgetOptions.add(new CmsSelectWidgetOption("most-commented",
				(this.zona.getOrderDefault().equals("most-commented")), "most-commented", "most-commented"));

		widgetOptions.add(new CmsSelectWidgetOption("most-recommended",
				(this.zona.getOrderDefault().equals("most-recommended")), "most-recommended", "most-recommended"));

		widgetOptions.add(new CmsSelectWidgetOption("most-valued",
				(this.zona.getOrderDefault().equals("most-valued")), "most-valued", "most-valued"));

		widgetOptions.add(new CmsSelectWidgetOption("most-positive-evaluations",
				(this.zona.getOrderDefault().equals("most-positive-evaluations")), "most-positive-evaluations", "most-positive-evaluations"));
		
		widgetOptions.add(new CmsSelectWidgetOption("most-negative-evaluations",
				(this.zona.getOrderDefault().equals("most-negative-evaluations")), "most-negative-evaluations", "most-negative-evaluations"));
		
		widgetOptions.add(new CmsSelectWidgetOption("most-number-of-evaluations",
				(this.zona.getOrderDefault().equals("most-number-of-evaluations")), "most-number-of-evaluations", "most-number-of-evaluations"));
		
		widgetOptions.add(new CmsSelectWidgetOption("category",
				(this.zona.getOrderDefault().equals("category")), "category", "category"));
		
		widgetOptions.add(new CmsSelectWidgetOption("tag",
				(this.zona.getOrderDefault().equals("tag")), "tag", "tag"));
		
		widgetOptions.add(new CmsSelectWidgetOption("author",
				(this.zona.getOrderDefault().equals("author")), "author", "author"));
		

		return widgetOptions;

	}

	private List<CmsSelectWidgetOption> getTipoEdicionesComboOptions()
			throws RuntimeException {
		List<CmsSelectWidgetOption> widgetOptions = new ArrayList<CmsSelectWidgetOption>();

		TipoEdicionService tDAO = new TipoEdicionService();

		String proyecto = openCmsService.getCurrentSite(this.getCms());

		try {
			List<TipoEdicion> tEdiciones = tDAO.obtenerTipoEdiciones(proyecto);

			for (Iterator it = tEdiciones.iterator(); it.hasNext();) {
				TipoEdicion tEdicion = (TipoEdicion) it.next();
				CmsSelectWidgetOption option = new CmsSelectWidgetOption(""
						+ tEdicion.getId(),
						(this.zona.getIdTipoEdicion() == tEdicion.getId()),
						tEdicion.getDescripcion(), tEdicion.getDescripcion());
				widgetOptions.add(option);
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new RuntimeException(e);
		}
		return widgetOptions;
	}

	private CmsWidgetDialogParameter newCombo(String propertyName,
			String title, List<CmsSelectWidgetOption> options) {
		return new CmsWidgetDialogParameter(this.zona, propertyName, title,
				PAGES[0], new CmsSelectWidget(options));
	}

	private CmsWidgetDialogParameter newInput(String propertyName, String title) {
		return new CmsWidgetDialogParameter(this.zona, propertyName, title,
				PAGES[0], new CmsInputWidget());
	}

	private CmsWidgetDialogParameter newColorPicker(String propertyName,
			String title) {
		return new CmsWidgetDialogParameter(this.zona, propertyName, title,
				PAGES[0], new CmsColorpickerWidgetTFS());
	}

	private List<CmsSelectWidgetOption> getPageComboOptions()
			throws RuntimeException {
		List<CmsSelectWidgetOption> widgetOptions = new ArrayList<CmsSelectWidgetOption>();

		PageDAO pDAO = new PageDAO();

		try {

			List<Page> paginas;
			paginas = pDAO.getPages();

			for (Iterator it = paginas.iterator(); it.hasNext();) {
				Page page = (Page) it.next();
				CmsSelectWidgetOption option = new CmsSelectWidgetOption(""
						+ page.getIdPage(),
						(this.zona.getIdPage() == page.getIdPage()),
						page.getPageName(), page.getPageName());
				widgetOptions.add(option);
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new RuntimeException(e);
		}
		return widgetOptions;
	}

	@Override
	protected String[] getPageArray() {
		return PAGES;
	}

	@Override
	protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings,
			HttpServletRequest request) {
		// initialize parameters and dialog actions in super implementation
		super.initWorkplaceRequestValues(settings, request);

		// save the current state of the seccion (may be changed because of the
		// widget values)
		this.setDialogObject(this.zona);
	}

	private void initZona() {
		if (this.isInitialCall()) {
			String strZonaId = this.getZonaIdFromRequest();
			// edit an existing encuesta, get the encuesta object from db
			if (strZonaId == null) {
				this.zona = new Zona();
			} else {
				ZonasService zService = new ZonasService();
				int zonaId = Integer.parseInt(strZonaId);
				this.zona = zService.obtenerZona(zonaId);
			}
		} else {
			// this is not the initial call, get the project object from
			// session
			this.zona = (Zona) this.getDialogObject();
		}
	}

	private String getZonaIdFromRequest() {
		return this.getJsp().getRequest().getParameter(PARAM_ZONA_KEY);
	}

	private boolean isInitialCall() {
		return CmsStringUtil.isEmpty(getParamAction())
				|| CmsDialog.DIALOG_INITIAL.equals(getParamAction());
	}

}
