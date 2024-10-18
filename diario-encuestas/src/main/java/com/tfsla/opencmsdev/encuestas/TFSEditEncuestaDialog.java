package com.tfsla.opencmsdev.encuestas;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.PageContext;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.CmsCalendarWidget;
import org.opencms.widgets.CmsCategoryWidget;
import org.opencms.widgets.CmsCheckboxWidget;
import org.opencms.widgets.CmsComboWidget;
import org.opencms.widgets.CmsImageGalleryWidget;
import org.opencms.widgets.CmsInputWidget;
import org.opencms.widgets.CmsSelectWidget;
import org.opencms.widgets.CmsSelectWidgetOption;
import org.opencms.widgets.I_CmsWidget;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWidgetDialog;
import org.opencms.workplace.CmsWidgetDialogParameter;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.TipoEdicionService;
import com.tfsla.diario.ediciones.services.openCmsService;
import com.tfsla.widgets.CmsComboWidgetTfsla;
import com.tfsla.widgets.TfsCalendarWidget;

public class TFSEditEncuestaDialog extends CmsWidgetDialog {

	private static final String KEY_PREFIX = "encuesta";

	private static final String[] PAGES = { "page1" };

	public static final String PARAM_ENCUESTA_URL = "encuestaURL";

	private Encuesta encuesta;

	public TFSEditEncuestaDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {
		super(context, req, res);
	}

	@Override
	public void actionCommit() throws IOException, ServletException {
		// malisimo, pongo esta validacion aca porque el opencms no valida
		// ocurrencias bien o yo estoy loco
		if (this.encuesta.getRespuestas().length < Encuesta.MIN_RESPUESTAS) {
			List errors = new ArrayList();
			errors.add(new CmsIllegalArgumentException(Messages.get()
					.container(
							Messages.ERR_RESPUESTAS_VALIDATION_0,
							new Object[] { new Integer(Encuesta.MIN_RESPUESTAS),
									new Integer(Encuesta.MAX_RESPUESTAS) })));
			this.setCommitErrors(errors);
			return;
		}
		if (this.encuesta.getGrupo()==null  || this.encuesta.getGrupo().length() == 0) {
			List errors = new ArrayList();
			errors.add(new CmsIllegalArgumentException(Messages.get()
					.container(
							Messages.ERR_GRUPO_VALIDATION_0)));
			this.setCommitErrors(errors);
			return;
		}

		if (!this.encuesta.isTransient()) {
			// se trata de una modificacion
			if (this.encuesta.getCantRespuestasOriginal() != this.encuesta.getRespuestas().length) {
				// en la modificacion fue cambiada la cantidad de preguntas, lo cual no esta permitido
				List errors = new ArrayList();
				errors.add(new CmsIllegalArgumentException(Messages.get().container(
						Messages.ERR_RESPUESTAS_CANTIDAD_VALIDATION_0,
						new Object[] { new Long(this.encuesta.getCantRespuestasOriginal()) })));
				this.setCommitErrors(errors);
				return;
			}
		}
		try {
			String encuestaURL = null;
			if (this.encuesta.isTransient()) {
				String encuestaFileURL = "Encuesta" + this.getFechaActualAsLong();
				encuestaURL = ModuloEncuestas.getEncuestaPath(getCms(), Integer.parseInt(this.encuesta.getPublicacion())) + "/" + encuestaFileURL;
				getCms().createResource(encuestaURL, Encuesta.TYPE_ID);
			}
			else {
				encuestaURL = this.encuesta.getEncuestaURL();
			}

			TfsEncuestasContenidoEditor editor = new TfsEncuestasContenidoEditor(this.getJsp(), encuestaURL);
			TfsEncuestasContenidoEditor.configureEditor(editor, this.encuesta);
			editor.save();

			ModuloEncuestas.insertOrUpdateEncuesta(this.getCms(), encuestaURL, this.encuesta);
		}
		catch (Exception e) {
			throw new RuntimeException("Error creando Encuesta", e);
		}
	}

	private String getFechaActualAsLong() {
		return String.valueOf(new Date().getTime());
	}

	@Override
	protected void defineWidgets() {
		// initialize the encuesta object to use for the dialog
		this.initEncuestaObject();
		this.setKeyPrefix(KEY_PREFIX);

		addWidget(newInput("pregunta"));
		if (this.encuesta.isNoPublicada()) {
			// solo se puede modificar el grupo si aun no fue publicada
			addWidget(newCombo("grupo"));
		}

		addWidget(newDateBox("fechaCierre"));
		addWidget(newDateBox("fechaDespublicacion"));
		addWidget(newVfsImageBox("imagenAMostrar"));
		addWidget(newInput("textoAclaratorio"));
		addWidget(newBoolean("respuestaExcluyente"));
		addWidget(newBoolean("usuariosRegistrados"));
		addWidget(newInput("respuestas", Encuesta.MIN_RESPUESTAS, Encuesta.MAX_RESPUESTAS));
		addWidget(newComboCategory("categorias", Encuesta.MIN_CATEGORIAS, Encuesta.MAX_CATEGORIAS));
		addWidget(newInput("tags"));

		String encuestaURL = this.getEncuestaURLFromRequest();
		if (encuestaURL == null) {
			List<CmsSelectWidgetOption> options=null;
			options = getTipoEdicionesComboOptions();
			addWidget(newCombo("publicacion","Publicacion",options));
		}
	}

	private CmsWidgetDialogParameter newComboCategory(String propertyName, int minOcurrences, int maxOcurrences) {
		return new CmsWidgetDialogParameter(this.encuesta, propertyName, "", PAGES[0],new CmsComboWidgetTfsla(
				ModuloEncuestas.getCategoriesParaCombo(this.getCms())), minOcurrences, maxOcurrences);
	}
	
	private CmsWidgetDialogParameter newCombo(String propertyName, String title, List<CmsSelectWidgetOption> options) {
		return new CmsWidgetDialogParameter(this.encuesta, propertyName, title, PAGES[0], new CmsSelectWidget(
				options));
	}

	private CmsWidgetDialogParameter newCombo(String propertyName) {
		return new CmsWidgetDialogParameter(this.encuesta, propertyName, "", PAGES[0], new CmsComboWidget(
				ModuloEncuestas.getGruposParaCombo(this.getCms())), 1, 1);
	}

	private CmsWidgetDialogParameter newInput(String propertyName, int minOcurrences, int maxOcurrences) {
		return new CmsWidgetDialogParameter(this.encuesta, propertyName, "", PAGES[0], new CmsInputWidget(),
				minOcurrences, maxOcurrences);
	}

	private CmsWidgetDialogParameter newBoolean(String propertyName) {
		return new CmsWidgetDialogParameter(this.encuesta, propertyName, "false", PAGES[0],
				new CmsCheckboxWidget(), 1, 1);
	}

	private CmsWidgetDialogParameter newVfsImageBox(String propertyName) {
		return new CmsWidgetDialogParameter(this.encuesta, propertyName, "", PAGES[0],
				new CmsImageGalleryWidget(), 1, 1);
	}

	private CmsWidgetDialogParameter newDateBox(String propertyName) {
		return new CmsWidgetDialogParameter(this.encuesta, propertyName, "", PAGES[0],
				new TfsCalendarWidget(), 1, 1);
	}

	private CmsWidgetDialogParameter newInput(String propertyName) {
		return this.newInput(propertyName, 1, 1);
	}

	private List<CmsSelectWidgetOption> getTipoEdicionesComboOptions() throws RuntimeException
	{
		List<CmsSelectWidgetOption> widgetOptions = new ArrayList<CmsSelectWidgetOption>();

		TipoEdicionService tDAO = new TipoEdicionService();

		String proyecto = openCmsService.getCurrentSite(this.getCms());

		try {
			List<TipoEdicion> tEdiciones = tDAO.obtenerTipoEdiciones(proyecto);

			for (Iterator it = tEdiciones.iterator(); it.hasNext();) {
				TipoEdicion tEdicion = (TipoEdicion) it.next();
				CmsSelectWidgetOption option = new CmsSelectWidgetOption("" + tEdicion.getId(),(this.encuesta.getPublicacion()!=null && this.encuesta.getPublicacion().equals(tEdicion.getId())),tEdicion.getDescripcion(),tEdicion.getDescripcion());
				widgetOptions.add(option);
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new RuntimeException(e);
		}
		return widgetOptions;
	}

	private void initEncuestaObject() {
		if (this.isInitialCall()) {
			String encuestaURL = this.getEncuestaURLFromRequest();
			// edit an existing encuesta, get the encuesta object from db
			if (encuestaURL == null) {
				this.encuesta = new Encuesta();
			}
			else {
				this.encuesta = this.getEncuestaFromVFS(encuestaURL);
			}
		}
		else {
			// this is not the initial call, get the project object from
			// session
			this.encuesta = (Encuesta) this.getDialogObject();
		}
	}

	private String getEncuestaURLFromRequest() {
		return this.getJsp().getRequest().getParameter(PARAM_ENCUESTA_URL);
	}

	@Override
	protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {
		// initialize parameters and dialog actions in super implementation
		super.initWorkplaceRequestValues(settings, request);

		// save the current state of the encuesta (may be changed because of the
		// widget values)
		this.setDialogObject(this.encuesta);
	}

	private Encuesta getEncuestaFromVFS(String encuestaURL) {
		try {
			CmsResource resource = this.getCms().readResource(encuestaURL);

			CmsFile readFile = this.getCms().readFile(resource);
			CmsXmlContent content = CmsXmlContentFactory.unmarshal(this.getCms(), readFile);

			return new Encuesta(content, this.getCms(), encuestaURL);
		}
		catch (CmsException e) {
			e.printStackTrace();
			throw new UnsupportedOperationException("Error en getEncuestaFromVFS. URL[" + encuestaURL + "]",
					e);
		}
	}

	private boolean isInitialCall() {
		return CmsStringUtil.isEmpty(getParamAction()) || CmsDialog.DIALOG_INITIAL.equals(getParamAction());
	}

	@Override
	protected String[] getPageArray() {
		return PAGES;
	}
}