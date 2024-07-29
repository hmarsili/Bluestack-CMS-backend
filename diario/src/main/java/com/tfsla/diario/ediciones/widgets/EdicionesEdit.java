package com.tfsla.diario.ediciones.widgets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.CmsCalendarWidget;
import org.opencms.widgets.CmsCheckboxWidget;
import org.opencms.widgets.CmsImageGalleryWidget;
import org.opencms.widgets.CmsInputWidget;
import org.opencms.widgets.CmsSelectWidget;
import org.opencms.widgets.CmsSelectWidgetOption;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWidgetDialog;
import org.opencms.workplace.CmsWidgetDialogParameter;
import org.opencms.workplace.CmsWorkplaceSettings;


import com.tfsla.diario.ediciones.model.Edicion;
import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.EdicionService;
import com.tfsla.diario.ediciones.services.TipoEdicionService;
import com.tfsla.diario.ediciones.services.openCmsService;

/**
 * @author Victor Podberezski
 * @version 1.0
 */
public class EdicionesEdit extends CmsWidgetDialog {


	Edicion edicion;

	public static final String PARAM_EDICION_KEY = "edicionID";

	private static final String KEY_PREFIX = "edicion";

	private static final String[] PAGES = { "page1" };

	public EdicionesEdit(PageContext context, HttpServletRequest req, HttpServletResponse res) {
		super(context, req, res);
	}

	private boolean ConstraintsOk()
	{
		List<Exception> errors = new ArrayList<Exception>();

		if (edicion.getFechaEdicion()==null)
		{
			CmsIllegalArgumentException err = new CmsIllegalArgumentException(
					Messages.get().container(
							Messages.ERR_FECHA_EDICION_VALIDATION_0
							)
					);
			errors.add(err);

		}


		if (errors.size()>0)
			this.setCommitErrors(errors);

		return (errors.size() == 0);

	}

	@Override
	public void actionCommit() throws IOException, ServletException {
		// TODO Auto-generated method stub
		EdicionService eService = new EdicionService();

		List<Exception> errors = new ArrayList<Exception>();

		if (ConstraintsOk())
		{
			if (edicion.isNuevaEdicion())
			{

				java.util.Date date = new java.util.Date();
				date.setTime(Long.parseLong(edicion.getFechaEdicion()));
				edicion.setFecha(date);

				if (edicion.getFechaPublicacion()!="") {
					java.util.Date datePub = new java.util.Date();
					datePub.setTime(Long.parseLong(edicion.getFechaPublicacion()));
					edicion.setPublicacion(datePub);
				}
				else
					edicion.setPublicacion(null);

				eService.crearEdicion(edicion,this.getCms());
				if (eService.HasError())
				{
					CmsIllegalArgumentException err = new CmsIllegalArgumentException(
							Messages.get().container(
									eService.getErrorDescription()
									)
							);
					errors.add(err);
					this.setCommitErrors(errors);
				}
			}
			else
			{
				if (edicion.getFechaPublicacion()!="") {
					java.util.Date datePub = new java.util.Date();
					datePub.setTime(Long.parseLong(edicion.getFechaPublicacion()));
					edicion.setPublicacion(datePub);
				}
				else
					edicion.setPublicacion(null);


				eService.actualizarEdicion(edicion);
				if (eService.HasError())
					throw new RuntimeException(eService.getErrorDescription());
			}
		}
	}

	@Override
	protected void defineWidgets() throws RuntimeException {
		setKeyPrefix(KEY_PREFIX);

		initEdicion();

		addWidget(newDateBox("fechaEdicion","Fecha Edicion"));
		addWidget(newInput("tituloTapa","Titulo tapa"));
		addWidget(newGallery("portada", "Portada"));
		addWidget(newGallery("logo", "Logo"));
		addWidget(newDateBox("fechaPublicacion","Fecha publicacion diferida"));

		if (edicion.isNuevaEdicion()) {

			addWidget(newInput("numero","Numero"));

			addWidget(newCheckBox("autoNumerico","Numero de edicion automatico"));

			List<CmsSelectWidgetOption> options=null;
			options = getTipoEdicionesComboOptions();
			addWidget(newCombo("tipo","Publicacion",options));
		}
	}

	private CmsWidgetDialogParameter newInput(String propertyName, String title) {
		return new CmsWidgetDialogParameter(this.edicion, propertyName,title, PAGES[0], new CmsInputWidget());
	}

	private CmsWidgetDialogParameter newCombo(String propertyName, String title, List<CmsSelectWidgetOption> options) {
		return new CmsWidgetDialogParameter(this.edicion, propertyName, title, PAGES[0], new CmsSelectWidget(
				options));
	}

	private CmsWidgetDialogParameter newGallery(String propertyName, String title) {
		return new CmsWidgetDialogParameter(this.edicion, propertyName, title, PAGES[0], new CmsImageGalleryWidget());
	}

	private CmsWidgetDialogParameter newDateBox(String propertyName, String title) {
		return new CmsWidgetDialogParameter(this.edicion, propertyName, title, PAGES[0],
				new CmsCalendarWidget(), 1, 1);
	}

	private CmsWidgetDialogParameter newCheckBox(String propertyName,String title) {
		return new CmsWidgetDialogParameter(this.edicion, propertyName, title, PAGES[0], new CmsCheckboxWidget());
	}


	private List<CmsSelectWidgetOption> getTipoEdicionesComboOptions() throws RuntimeException
	{
		List<CmsSelectWidgetOption> widgetOptions = new ArrayList<CmsSelectWidgetOption>();

		TipoEdicionService tDAO = new TipoEdicionService();

		String proyecto = openCmsService.getCurrentSite(this.getCms());

		try {
			List<TipoEdicion> tEdiciones = tDAO.obtenerTipoEdicionesImpresas(proyecto);

			for (Iterator it = tEdiciones.iterator(); it.hasNext();) {
				TipoEdicion tEdicion = (TipoEdicion) it.next();
				CmsSelectWidgetOption option = new CmsSelectWidgetOption("" + tEdicion.getId(),(this.edicion.getTipo()==tEdicion.getId()),tEdicion.getDescripcion(),tEdicion.getDescripcion());
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
	protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {
		// initialize parameters and dialog actions in super implementation
		super.initWorkplaceRequestValues(settings, request);

		// save the current state of the seccion (may be changed because of the
		// widget values)
		this.setDialogObject(this.edicion);
	}

	private void initEdicion() {
		if (this.isInitialCall()) {
			String strEdicion = this.getEdicionIdFromRequest();
			// edit an existing encuesta, get the encuesta object from db
			if (strEdicion == null) {
				this.edicion = new Edicion();
				this.edicion.setNuevaEdicion(true);
			}
			else {

				EdicionService eService = new EdicionService();

				String[] edicionID = strEdicion.split(":");
				int tipo = Integer.parseInt(edicionID[0]);
				int numero = Integer.parseInt(edicionID[1]);
				this.edicion = eService.obtenerEdicion(tipo, numero);

				this.edicion.setFechaEdicion("" + this.edicion.getFecha().getTime());

				if (this.edicion.getPublicacion()!=null)
					this.edicion.setFechaPublicacion("" + this.edicion.getPublicacion().getTime());
				else
					this.edicion.setFechaPublicacion("");
				this.edicion.setNuevaEdicion(false);
			}
		}
		else {
			// this is not the initial call, get the project object from
			// session
			this.edicion = (Edicion) this.getDialogObject();
		}
	}

	private String getEdicionIdFromRequest() {
		return this.getJsp().getRequest().getParameter(PARAM_EDICION_KEY);
	}

	private boolean isInitialCall() {
		return CmsStringUtil.isEmpty(getParamAction()) || CmsDialog.DIALOG_INITIAL.equals(getParamAction());
	}

}
