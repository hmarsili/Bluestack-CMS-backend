package com.tfsla.diario.ediciones.widgets;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsCategory;
import org.opencms.relations.CmsCategoryService;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.CmsInputWidget;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWidgetDialog;
import org.opencms.workplace.CmsWidgetDialogParameter;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.widgets.CmsSelectWidget;
import org.opencms.widgets.CmsSelectWidgetOption;

import com.tfsla.diario.ediciones.model.Categoria;
import com.tfsla.opencms.util.PropertiesProvider;

public class SubCategoriasEdit extends CmsWidgetDialog {

	public static final String PARAM_CATEGORIA_KEY = "pathCategoria";

	private static final String KEY_PREFIX = "categoria";

	private static final String[] PAGES = { "page1" };

	private static String TEMPLATE_NAME = "";

	private Categoria categoria;
	
	public SubCategoriasEdit(PageContext context, HttpServletRequest req, HttpServletResponse res) {
		super(context, req, res);
		
    	PropertiesProvider properties = new PropertiesProvider(this.getClass(), "subCategorias.properties");
    	
    	TEMPLATE_NAME = properties.get("SubCategoriasHome");
    	//"/system/modules/com.tfsla.opencmsdev/templates/categories/subSecciones_Home.jsp"
	}

	private boolean ConstraintsOk()
	{
		List<Exception> errors = new ArrayList<Exception>();

		if (categoria.getDescripcion()==null || categoria.getDescripcion().isEmpty())
		{
			CmsIllegalArgumentException err = new CmsIllegalArgumentException(
					Messages.get().container(
							Messages.ERR_DESCRIPCION_VALIDATION_0
							)
					);
			errors.add(err);

		}

		if (categoria.getNombre()==null || categoria.getNombre().isEmpty())
		{
			CmsIllegalArgumentException err = new CmsIllegalArgumentException(
					Messages.get().container(
							Messages.ERR_NOMBRE_VALIDATION_0
							)
					);
			errors.add(err);
		}

		if (categoria.getOrden() == null || categoria.getOrden().isEmpty())
		{
			CmsIllegalArgumentException err = new CmsIllegalArgumentException(
					Messages.get().container(
							Messages.ERR_ORDEN_VALIDATION_0
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
		List<Exception> errors = new ArrayList<Exception>();

		if (ConstraintsOk())
		{
			CmsCategoryService cService = CmsCategoryService.getInstance();

			if (categoria.isNuevaCategoria())
			{

				CmsCategory catParent;
				try {
					catParent = cService.readCategory(this.getCms(), categoria.getPadre(),null);
				
					cService.createCategory(this.getCms(), catParent, categoria.getNombre(), categoria.getDescripcion(), categoria.getDescripcion(), null);

					String currentSite = this.getCms().getRequestContext().getSiteRoot();
					
					this.getCms().getRequestContext().setSiteRoot("/");
					
					String newCategoryPath = "/system/categories/" + catParent.getPath() + categoria.getNombre();
					
					this.getCms().unlockResource(newCategoryPath);
					
					OpenCms.getPublishManager().publishResource(this.getCms(), newCategoryPath);
					
					this.getCms().getRequestContext().setSiteRoot(currentSite);
					
					//TODO: Crear la jerarquia de clases
					String path = 
						//"/SubSecciones/" + 
						categoria.getPadre() + 
						//(categoria.getPadre().equals("/SubSecciones/") ? "" : "/") + 
						categoria.getNombre();
										
					this.getCms().createResource(path,org.opencms.file.types.CmsResourceTypeFolder.getStaticTypeId());
					this.getCms().writePropertyObject(path, new CmsProperty("Title", categoria.getDescripcion(),null));
					this.getCms().writePropertyObject(path, new CmsProperty("NavText", categoria.getDescripcion(),null));
					this.getCms().writePropertyObject(path, new CmsProperty("NavPos", categoria.getOrden(),null));
					this.getCms().writePropertyObject(path, new CmsProperty("NavInfo", categoria.getDescripcionlarga(),null));
					this.getCms().writePropertyObject(path, new CmsProperty("Description", categoria.getDescripcionlarga(),null));
					this.getCms().writePropertyObject(path, new CmsProperty("Keywords", categoria.getTags(),null));
					this.getCms().unlockResource(path);

					int tipo_recursoXml = OpenCms.getResourceManager().getResourceType("xmlpage").getTypeId();
					String indexPage = path + "/index.html";
					this.getCms().createResource(indexPage, tipo_recursoXml);
					this.getCms().writePropertyObject(indexPage, new CmsProperty("template", TEMPLATE_NAME,null));
					this.getCms().writePropertyObject(indexPage, new CmsProperty("Title", categoria.getDescripcion(),null));
					this.getCms().writePropertyObject(indexPage, new CmsProperty("subSeccion", path.replace("/SubSecciones/", ""),null));
					this.getCms().unlockResource(indexPage);
					
					OpenCms.getPublishManager().publishResource(this.getCms(), path);
					//OpenCms.getPublishManager().publishResource(this.getCms(), indexPage);
					
				} catch (CmsException e) {
					CmsIllegalArgumentException err = new CmsIllegalArgumentException(
							Messages.get().container(
									e.getMessage()
							)
					);
					errors.add(err);

					this.setCommitErrors(errors);
				} catch (Exception e) {
					CmsIllegalArgumentException err = new CmsIllegalArgumentException(
							Messages.get().container(
									e.getMessage()
							)
					);
					errors.add(err);
					this.setCommitErrors(errors);
				}
	
			}
			else
			{
				String path = 
					categoria.getPadre() + 
					categoria.getNombre();

				try {
					
					String currentSite = this.getCms().getRequestContext().getSiteRoot();
					
					this.getCms().getRequestContext().setSiteRoot("/");
						
					String categoryPath = "/system/categories" + path;
					
					this.getCms().lockResource(categoryPath);
					this.getCms().writePropertyObject(categoryPath, new CmsProperty("Title", categoria.getDescripcion(),null));
					this.getCms().unlockResource(categoryPath);

					OpenCms.getPublishManager().publishResource(this.getCms(), categoryPath);
					
					this.getCms().getRequestContext().setSiteRoot(currentSite);
					
					this.getCms().lockResource(path);
					this.getCms().writePropertyObject(path, new CmsProperty("Title", categoria.getDescripcion(),null));
					this.getCms().writePropertyObject(path, new CmsProperty("NavText", categoria.getDescripcion(),null));
					this.getCms().writePropertyObject(path, new CmsProperty("NavPos", categoria.getOrden(),null));
					this.getCms().writePropertyObject(path, new CmsProperty("NavInfo", categoria.getDescripcionlarga(),null));
					this.getCms().writePropertyObject(path, new CmsProperty("Description", categoria.getDescripcionlarga(),null));
					this.getCms().writePropertyObject(path, new CmsProperty("Keywords", categoria.getTags(),null));
					this.getCms().unlockResource(path);

					String indexPage = path + "/index.html";
					this.getCms().lockResource(indexPage);
					this.getCms().writePropertyObject(indexPage, new CmsProperty("Title", categoria.getDescripcion(),null));
					this.getCms().unlockResource(indexPage);

					OpenCms.getPublishManager().publishResource(this.getCms(), path);

				} catch (CmsException e) {
					CmsIllegalArgumentException err = new CmsIllegalArgumentException(
							Messages.get().container(
									e.getMessage()
							)
					);
					errors.add(err);
					this.setCommitErrors(errors);
				} catch (Exception e) {
					CmsIllegalArgumentException err = new CmsIllegalArgumentException(
							Messages.get().container(
									e.getMessage()
							)
					);
					errors.add(err);
					this.setCommitErrors(errors);
				}

			}

		}
	}

	@Override
	protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {
		// initialize parameters and dialog actions in super implementation
		super.initWorkplaceRequestValues(settings, request);

		// save the current state of the seccion (may be changed because of the
		// widget values)
		this.setDialogObject(this.categoria);
	}

	@Override
	protected void defineWidgets() {
		setKeyPrefix(KEY_PREFIX);
		
		initCategoria();
		
		if (categoria.isNuevaCategoria())
		{
			addWidget(newInput("nombre","Nombre"));
		}

		addWidget(newInput("descripcion","Descripcion"));
		addWidget(newInput("tags","Tags"));
		addWidget(newInput("orden","Orden"));
		addWidget(newInput("descripcionlarga","Descripcion Larga"));
		
		if (categoria.isNuevaCategoria())
		{
			List<CmsSelectWidgetOption> options=null;
			options = getCategorias();
			addWidget(newCombo("padre","Categoria padre",options));
		}
	}

	private void initCategoria() {
		if (this.isInitialCall()) {
			String pathCategoria = this.getCategoriaFromRequest();
			
			if (pathCategoria == null) {
				this.categoria = new Categoria();
				this.categoria.setNuevaCategoria(true);
			}
			else {
				try {
					CmsResource cat = this.getCms().readResource(pathCategoria);
					CmsProperty descripcion = this.getCms().readPropertyObject(cat, "Title", false);
					CmsProperty orden = this.getCms().readPropertyObject(cat, "NavPos", false);	
					CmsProperty tags = this.getCms().readPropertyObject(cat, "Keywords", false);
					CmsProperty descripcionLarga = this.getCms().readPropertyObject(cat, "Description", false);
					
					String padre = pathCategoria.substring(0,pathCategoria.lastIndexOf("/"));

					this.categoria = new Categoria();

					this.categoria.setDescripcion(descripcion.getValue());
					this.categoria.setNombre(padre.substring(padre.lastIndexOf("/")+1));
					this.categoria.setDescripcionlarga(descripcionLarga.getValue());
					this.categoria.setOrden(orden.getValue());
					this.categoria.setTags(tags.getValue());
					
					padre = padre.substring(0,padre.lastIndexOf("/")+1);
					
					this.categoria.setPadre(padre);
					
				} catch (CmsException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
			}
		}
		else {
			// this is not the initial call, get the project object from
			// session
			this.categoria = (Categoria) this.getDialogObject();
		}

	}
	
	private String getCategoriaFromRequest() {
		return this.getJsp().getRequest().getParameter(PARAM_CATEGORIA_KEY);
	}

	private boolean isInitialCall() {
		return CmsStringUtil.isEmpty(getParamAction()) || CmsDialog.DIALOG_INITIAL.equals(getParamAction());
	}

	@Override
	protected String[] getPageArray() {
		return PAGES;
	}

	private CmsWidgetDialogParameter newInput(String propertyName, String title) {
		return new CmsWidgetDialogParameter(this.categoria, propertyName,title, PAGES[0], new CmsInputWidget());
	}

	private CmsWidgetDialogParameter newCombo(String propertyName, String title, List<CmsSelectWidgetOption> options) {
		return new CmsWidgetDialogParameter(this.categoria, propertyName, title, PAGES[0], new CmsSelectWidget(
				options));
	}

	private List<CmsSelectWidgetOption> getCategorias() throws RuntimeException
	{
		List<CmsSelectWidgetOption> widgetOptions = new ArrayList<CmsSelectWidgetOption>();
		
		List<CmsResource> files;
		try {
			files = this.getCms().getSubFolders("/SubSecciones/");
		

			for (int j=0;j<files.size();j++)
			{
				CmsResource file = files.get(j);
				
				files.addAll(this.getCms().getSubFolders(this.getCms().getRequestContext().removeSiteRoot(file.getRootPath())));
			}

			CmsSelectWidgetOption optionR = 
				new CmsSelectWidgetOption(
					"/SubSecciones/",
					(this.categoria.getPadre().equals("")),
					"Raiz");
			widgetOptions.add(optionR);

			for (Iterator it = files.iterator();it.hasNext();)
			{
				CmsResource file = (CmsResource) it.next();
				String path = this.getCms().getRequestContext().removeSiteRoot(file.getRootPath());
	
				CmsProperty descripcion = this.getCms().readPropertyObject(file, "Title", false);
	
				CmsSelectWidgetOption option = 
					new CmsSelectWidgetOption(
						path,
						(this.categoria.getPadre().equals(path)),
						descripcion.getValue());
	
				widgetOptions.add(option);
	
				
	
			}
		
		} catch (CmsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return widgetOptions;
	}
}
