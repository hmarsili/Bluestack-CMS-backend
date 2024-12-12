package com.tfsla.diario.ediciones.widgets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.PageContext;

import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.OpenCms;
import org.opencms.search.CmsSearchIndex;
import org.opencms.site.CmsSite;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.CmsInputWidget;
import org.opencms.widgets.CmsSelectWidget;
import org.opencms.widgets.CmsSelectWidgetOption;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWidgetDialog;
import org.opencms.workplace.CmsWidgetDialogParameter;
import org.opencms.workplace.CmsWorkplaceSettings;

import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.model.TipoPublicacion;
import com.tfsla.diario.ediciones.services.TipoEdicionService;
import com.tfsla.diario.ediciones.services.openCmsService;

/**
 * @author Victor Podberezski
 * @version 1.0
 */
public class TipoEdicionesEdit extends CmsWidgetDialog {


	TipoEdicion tipoEdicion;

	public static final String PARAM_EDICION_KEY = "tipoEdicionID";

	private static final String KEY_PREFIX = "tipoEdicion";

	private static final String[] PAGES = { "page1" };

	public TipoEdicionesEdit(PageContext context, HttpServletRequest req, HttpServletResponse res) {
		super(context, req, res);
	}

	private boolean ConstraintsOk() {
		List<Exception> errors = new ArrayList<Exception>();

		if (tipoEdicion.getDescripcion() == ""){
			CmsIllegalArgumentException err = new CmsIllegalArgumentException(
					Messages.get().container(
							Messages.ERR_DESCRIPCION_TIPO_EDICION_VALIDATION_0
							)
					);
			errors.add(err);
		}

		if (tipoEdicion.getNombre() == ""){
			CmsIllegalArgumentException err = new CmsIllegalArgumentException(
					Messages.get().container(
							Messages.ERR_NOMBRE_TIPO_EDICION_VALIDATION_0
							)
					);
			errors.add(err);
		}
		
		if (tipoEdicion.getId() == 0){
			if (tipoEdicion.getNombre() != ""){
				TipoEdicionService tService = new TipoEdicionService();
				if (tService.TipoEdicionExists(tipoEdicion.getNombre(), tipoEdicion.getProyecto())){
					CmsIllegalArgumentException err = new CmsIllegalArgumentException(
							Messages.get().container(
									Messages.ERR_NOMBRE_EXISTENTE_TIPO_EDICION_VALIDATION_0
									)
							);
					errors.add(err);
				}
			}
		
			try{
				if (TipoPublicacion.getTipoPublicacionByCode(tipoEdicion.getTipoPublicacion()).equals(TipoPublicacion.ONLINE_ROOT)){
					TipoEdicionService tService = new TipoEdicionService();
					if (tService.hasEdicionOnlineRoot(tipoEdicion.getProyecto())){
						CmsIllegalArgumentException err = new CmsIllegalArgumentException(
								Messages.get().container(
										Messages.ERR_ONLINE_ROOT_EXISTENTE_TIPO_EDICION_VALIDATION_0
										)
								);
						errors.add(err);
					}
				}
			}
			catch (Exception e) {
				try{
					CmsIllegalArgumentException err = new CmsIllegalArgumentException(
							Messages.get().container(e.getMessage()));
					errors.add(err);					
				}
				catch (Exception ex) {
					// TODO: handle exception
				}
			}
		}
		
		if (errors.size()>0)
			this.setCommitErrors(errors);

		return (errors.size() == 0);
	}

	@Override
	public void actionCommit() throws IOException, ServletException {
		// TODO Auto-generated method stub
		TipoEdicionService eService = new TipoEdicionService();

		List<Exception> errors = new ArrayList<Exception>();

		if (ConstraintsOk())
		{
			if (tipoEdicion.getId()==0)
			{
				if(!TipoPublicacion.getTipoPublicacionByCode(tipoEdicion.getTipoPublicacion()).equals(TipoPublicacion.ONLINE_ROOT)){
					tipoEdicion.setBaseURL("/sites/"
							+ tipoEdicion.getProyecto()
							+ "/"  + tipoEdicion.getNombre() + "/");					
				}
				else{
					tipoEdicion.setBaseURL("/sites/"
							+ tipoEdicion.getProyecto()
							+ "/contenidos/");					
				}
				
				eService.crearTipoEdicion(tipoEdicion, this.getCms());
				
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
				eService.actualizarTipoEdicion(tipoEdicion);
				
				if (eService.HasError())
					throw new RuntimeException(eService.getErrorDescription());
			}
		}
	}

	@Override
	protected void defineWidgets() throws RuntimeException {
		setKeyPrefix(KEY_PREFIX);

		initZona();

		addWidget(newInput("descripcion","Descripcion"));

		if (tipoEdicion.getId()==0) {

			addWidget(newInput("nombre","Nombre"));
			
			List<CmsSelectWidgetOption> publicationTypes = null;
			publicationTypes = getTipoPublicacionesComboOptions();
			addWidget(newCombo("tipoPublicacion","Tipo",publicationTypes));			
			
			List<CmsSelectWidgetOption> sites = null;
			sites = getProyectosComboOptions();
			addWidget(newCombo("proyecto","Sitio",sites));
		}
		
		List<CmsSelectWidgetOption> indices = null;
		indices = getIndicesComboOptions(this.tipoEdicion.getNoticiasIndex());
		addWidget(newCombo("noticiasIndex","Indice de Noticias ONLINE",indices));

		indices = getIndicesComboOptions(this.tipoEdicion.getImagenesIndex());
		addWidget(newCombo("imagenesIndex","Indice de Imagenes ONLINE",indices));

		indices = getIndicesComboOptions(this.tipoEdicion.getVideosIndex());
		addWidget(newCombo("videosIndex","Indice de Videos ONLINE",indices));

		indices = getIndicesComboOptions(this.tipoEdicion.getAudiosIndex());
		addWidget(newCombo("audiosIndex","Indice de Audios ONLINE",indices));

		indices = getIndicesComboOptions(this.tipoEdicion.getEncuestasIndex());
		addWidget(newCombo("encuestasIndex","Indice de Encuestas ONLINE",indices));

		indices = getIndicesComboOptions(this.tipoEdicion.getTwitterFeedIndex());
		addWidget(newCombo("twitterFeedIndex","Indice de Twitter Feeds ONLINE",indices));

		indices = getIndicesComboOptions(this.tipoEdicion.getNoticiasIndexOffline());
		addWidget(newCombo("noticiasIndexOffline","Indice de Noticias OFFLINE",indices));

		indices = getIndicesComboOptions(this.tipoEdicion.getImagenesIndexOffline());
		addWidget(newCombo("imagenesIndexOffline","Indice de Imagenes OFFLINE",indices));

		indices = getIndicesComboOptions(this.tipoEdicion.getVideosIndexOffline());
		addWidget(newCombo("videosIndexOffline","Indice de Videos OFFLINE",indices));
		
		indices = getIndicesComboOptions(this.tipoEdicion.getAudiosIndexOffline());
		addWidget(newCombo("audiosIndexOffline","Indice de Audios OFFLINE",indices));

		indices = getIndicesComboOptions(this.tipoEdicion.getEncuestasIndexOffline());
		addWidget(newCombo("encuestasIndexOffline","Indice de Encuestas OFFLINE",indices));

		indices = getIndicesComboOptions(this.tipoEdicion.getTwitterFeedIndexOffline());
		addWidget(newCombo("twitterFeedIndexOffline","Indice de Twitter Feeds OFFLINE",indices));

		indices = getIndicesComboOptions(this.tipoEdicion.getEventosIndex());
		addWidget(newCombo("eventosIndex","Indice de Eventos ONLINE",indices));

		indices = getIndicesComboOptions(this.tipoEdicion.getEventosIndexOffline());
		addWidget(newCombo("eventosIndexOffline","Indice de eventos OFFLINE",indices));
				
		indices = getIndicesComboOptions(this.tipoEdicion.getVideoVodIndexOffline());
		addWidget(newCombo("videoVodIndexOffline","Indice de videos vod OFFLINE",indices));
		
		indices = getIndicesComboOptions(this.tipoEdicion.getVideosVodindexOnline());
		addWidget(newCombo("videosVodindexOnline","Indice de videos vod ONLINE",indices));
	
		indices = getIndicesComboOptions(this.tipoEdicion.getVodIndexOnline());
		addWidget(newCombo("vodIndexOnline","Indice de  vod  ONLINE",indices));
		
		indices = getIndicesComboOptions(this.tipoEdicion.getVodIndexOffline());
		addWidget(newCombo("vodIndexOffline","Indice de  vod  OFFLINE",indices));
		
		indices = getIndicesComboOptions(this.tipoEdicion.getPlaylistIndex());
		addWidget(newCombo("playlistIndex","Indice de playlist ONLINE",indices));

		indices = getIndicesComboOptions(this.tipoEdicion.getPlaylistIndexOffline());
		addWidget(newCombo("playlistIndexOffline","Indice de playlist OFFLINE",indices));
		
		indices = getIndicesComboOptions(this.tipoEdicion.getVodGenericIndexOnline());
		addWidget(newCombo("vodGenericIndexOnline","Indice de vod para todos los elementos ONLINE",indices));
		
		indices = getIndicesComboOptions(this.tipoEdicion.getVodGenericIndexOffline());
		addWidget(newCombo("vodGenericIndexOffline","Indice de vod para todos los elementos OFFLINE",indices));
		
		indices = getIndicesComboOptions(this.tipoEdicion.getTriviasIndex());
		addWidget(newCombo("triviasIndex","Indice de trivias ONLINE",indices));
		
		indices = getIndicesComboOptions(this.tipoEdicion.getTriviasIndexOffline());
		addWidget(newCombo("triviasIndexOffline","Indice de trivias OFFLINE",indices));
		
		indices = getIndicesComboOptions(this.tipoEdicion.getRecetaIndexOnline());
		addWidget(newCombo("recetaIndexOnline","Indice de recetas ONLINE",indices));

		indices = getIndicesComboOptions(this.tipoEdicion.getRecetaIndexOffline());
		addWidget(newCombo("recetaIndexOffline","Indice de recetas OFFLINE",indices));
		
		addWidget(newInput("videoYoutubeDefaultVFSPath","Carpeta de Videos Youtube"));

		addWidget(newInput("videoEmbeddedDefaultVFSPath","Carpeta de Videos Embedded"));
		
		addWidget(newInput("customDomain","Dominio personalizado"));
		
		addWidget(newInput("imagePath","Imagen personalizado"));
		
		addWidget(newInput("language","Idioma de la publicacion"));

	}

	private CmsWidgetDialogParameter newCombo(String propertyName, String title, List<CmsSelectWidgetOption> options) {
		return new CmsWidgetDialogParameter(this.tipoEdicion, propertyName, title, PAGES[0], new CmsSelectWidget(
				options));
	}

	private CmsWidgetDialogParameter newInput(String propertyName, String title) {
		return new CmsWidgetDialogParameter(this.tipoEdicion, propertyName,title, PAGES[0], new CmsInputWidget());
	}
	
	private List<CmsSelectWidgetOption> getIndicesComboOptions(String selectedValue)
	{
		List<CmsSelectWidgetOption> widgetOptions = new ArrayList<CmsSelectWidgetOption>();
		
		boolean isSelected = false;
		for (CmsSearchIndex index : OpenCms.getSearchManager().getSearchIndexes())
		{
			if (selectedValue!=null && selectedValue.equals(index.getName()))
				isSelected = true;
			
	    	CmsSelectWidgetOption option = new CmsSelectWidgetOption(index.getName(), isSelected, index.getName(), index.getName());
			widgetOptions.add(option);		      

		}
		
		return widgetOptions;
	}
	
	private List<CmsSelectWidgetOption> getTipoPublicacionesComboOptions()
	{
		List<CmsSelectWidgetOption> widgetOptions = new ArrayList<CmsSelectWidgetOption>();

		Map<Integer, TipoPublicacion>tipoPublicaciones = TipoPublicacion.getTipoPublicaciones();
		Set<Integer> set = tipoPublicaciones.keySet();
		
		boolean isSelected = false;
			
		Iterator<Integer> itr = set.iterator();
	    while (itr.hasNext()) {
	    	TipoPublicacion tipoPublicacion = tipoPublicaciones.get(itr.next());
	    	
	    	if((this.tipoEdicion.getId() != 0 && tipoPublicacion.equals(TipoPublicacion.getTipoPublicacionByCode(this.tipoEdicion.getTipoPublicacion()))) || tipoPublicacion.equals(TipoPublicacion.ONLINE))
				isSelected = true;	    	
	    	
	    	CmsSelectWidgetOption option = new CmsSelectWidgetOption(String.valueOf(tipoPublicacion.getCode()), isSelected, tipoPublicacion.getDescription(), tipoPublicacion.getDescription());
			widgetOptions.add(option);		      
	    }
	    
		return widgetOptions;
	}

	private List<CmsSelectWidgetOption> getProyectosComboOptions() throws RuntimeException
	{
		List<CmsSelectWidgetOption> widgetOptions = new ArrayList<CmsSelectWidgetOption>();
		
		List<CmsSite> sites = OpenCms.getSiteManager().getAvailableSites(this.getCms(), true);
		
		boolean isSelected = false;
		
		for (Iterator it = sites.iterator(); it.hasNext();) {
			CmsSite cmsSite = (CmsSite) it.next();
			
			if(!cmsSite.getTitle().equals("/")){
				
				if(this.tipoEdicion.getProyecto() == openCmsService.getSiteName(cmsSite.getSiteRoot()) || this.getCms().getRequestContext().getSiteRoot().equals(cmsSite.getSiteRoot()))
					isSelected = true;
					
				CmsSelectWidgetOption option = new CmsSelectWidgetOption(openCmsService.getSiteName(cmsSite.getSiteRoot()), isSelected, cmsSite.getTitle(), cmsSite.getTitle());
				widgetOptions.add(option);
			}
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
		this.setDialogObject(this.tipoEdicion);
	}

	private void initZona() {
		if (this.isInitialCall()) {
			String strTipoEdicion = this.getEdicionIdFromRequest();
			// edit an existing encuesta, get the encuesta object from db
			if (strTipoEdicion == null) {
				this.tipoEdicion = new TipoEdicion();
			}
			else {

				TipoEdicionService tService = new TipoEdicionService();


				int id = Integer.parseInt(strTipoEdicion);
				this.tipoEdicion = tService.obtenerTipoEdicion(id);
			}
		}
		else {
			// this is not the initial call, get the project object from
			// session
			this.tipoEdicion = (TipoEdicion) this.getDialogObject();
		}
	}

	private String getEdicionIdFromRequest() {
		return this.getJsp().getRequest().getParameter(PARAM_EDICION_KEY);
	}

	private boolean isInitialCall() {
		return CmsStringUtil.isEmpty(getParamAction()) || CmsDialog.DIALOG_INITIAL.equals(getParamAction());
	}

}
