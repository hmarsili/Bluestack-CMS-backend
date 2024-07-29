package com.tfsla.diario.ediciones.services;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.loader.CmsResourceManager;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.search.CmsSearchIndexSource;
import org.opencms.search.CmsSearchManager;
//import org.opencms.site.CmsSiteManager;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

import com.tfsla.diario.ediciones.data.EdicionesDAO;
import com.tfsla.diario.ediciones.data.SeccionDAO;
import com.tfsla.diario.ediciones.data.TipoEdicionesDAO;
import com.tfsla.diario.ediciones.exceptions.UndefinedTipoEdicion;
import com.tfsla.diario.ediciones.model.Edicion;
import com.tfsla.diario.ediciones.model.Seccion;
import com.tfsla.diario.ediciones.model.TipoEdicion;

/**
 * Clase que realiza la administracion de las ediciones.
 * @author Victor Podberezski
 *
 */
public class EdicionService extends baseService {

	/**
	 * Directorio donde se encuentran las definiciones de las homes
	 */
	public static String HOME_DIRECTORY = "homes/";
	/**
	 * Directorio donde se encuentran los templates.
	 */
	public static String TEMPLATE_DIRECTORY = "templates/";
	/**
	 * Directorio donde se encuentran las noticias.
	 */
	public static String CONTENIDOS_DIRECTORY = "contenidos/";
	/**
	 * Nombre del source del indice donde se agregan las ediciones.
	 */
	public static String INDEX_SOURCENAME = "EDICIONES_CONTENIDOS";

	/**
	 * Crea una edicion.
	 * @param edicion
	 * @param cms
	 */
	public void crearEdicion(Edicion edicion, CmsObject cms) {

		EdicionesDAO eDAO = new EdicionesDAO();

		String siteRoot = cms.getRequestContext().getSiteRoot();

		try {

			cms.getRequestContext().setSiteRoot("/");

			if (edicion.isAutoNumerico())
			{
				edicion.setNumero(eDAO.getNextEdicionNumber(edicion.getTipo()));
			}
			eDAO.insertEdicion(edicion);

			TipoEdicion tEdicion;
			TipoEdicionesDAO tDAO = new TipoEdicionesDAO();
			tEdicion = tDAO.getTipoEdicion(edicion.getTipo());
			edicion.setTipoEdicion(tEdicion);


			Calendar cal = new GregorianCalendar();
			cal.setTime(edicion.getFecha());

			boolean yearPathExists = true;
			boolean monthPathExists = true;
			String yearPath = "";

			String path = tEdicion.getBaseURL() + cal.get(Calendar.YEAR) + "/";

			if (!cms.existsResource(path))
			{
				cms.createResource(path,org.opencms.file.types.CmsResourceTypeFolder.getStaticTypeId());
				cms.unlockResource(path);
				yearPathExists = false;
				yearPath = path;
				//OpenCms.getPublishManager().publishResource(cms, path);
			}
			path += (cal.get(Calendar.MONTH)+1) + "/";
			if (!cms.existsResource(path))
			{
				cms.createResource(path,org.opencms.file.types.CmsResourceTypeFolder.getStaticTypeId());
				cms.unlockResource(path);
				monthPathExists = false;
				//OpenCms.getPublishManager().publishResource(cms, path);
			}

			if (!yearPathExists)
				OpenCms.getPublishManager().publishResource(cms, yearPath);
			else if (!monthPathExists)
				OpenCms.getPublishManager().publishResource(cms, path);

			cms.copyResource(edicion.getTipoEdicion().getBaseURL() + "baseTemplate",edicion.getbaseURL(), CmsResource.COPY_AS_NEW );
			cms.unlockResource(edicion.getbaseURL());

			crearSeccionesEdicion(edicion,cms);
			crearIndexSecciones(edicion,cms,siteRoot);
			crearIndexHome(edicion,cms,siteRoot);
			crearTitulares(edicion,cms,siteRoot);
			agregarIndice(edicion);
			establecerTemplateNotaEdicion(edicion,cms,siteRoot);
			//cms.publishResource(edicion.getbaseURL());

			hasError = false;

		} catch (SQLException e) {
			if (e.getMessage().toLowerCase().indexOf("duplicate")>=0)
			{
				hasError = true;
				errorDescription = "Existe una edicion con el número " + edicion.getNumero();
			}
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		finally {
			cms.getRequestContext().setSiteRoot(siteRoot );
		}

	}

	/**
	 * Elimina una edicion.
	 * @param edicion
	 * @param cms
	 */
	public void borrarEdicion(Edicion edicion, CmsObject cms) {

		EdicionesDAO eDAO = new EdicionesDAO();

		String siteRoot = cms.getRequestContext().getSiteRoot();

		try {

			cms.getRequestContext().setSiteRoot("/");

			TipoEdicion tEdicion;

			edicion = eDAO.getEdicion(edicion.getTipo(), edicion.getNumero());

			TipoEdicionesDAO tDAO = new TipoEdicionesDAO();
			tEdicion = tDAO.getTipoEdicion(edicion.getTipo());

			edicion.setTipoEdicion(tEdicion);

			cms.lockResource(edicion.getbaseURL());

			cms.deleteResource(edicion.getbaseURL(),CmsResource.DELETE_PRESERVE_SIBLINGS );
			//cms.publishResource(edicion.getTipoEdicion().getBaseURL());

			eDAO.deleteEdicion(edicion.getTipo(),edicion.getNumero());

			quitarIndice(edicion);

			hasError = false;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		finally {
			cms.getRequestContext().setSiteRoot(siteRoot );
		}


	}

	/**
	 * Actualiza los datos de una edicion.
	 * @param edicion
	 */
	public void actualizarEdicion(Edicion edicion) {
		EdicionesDAO eDAO = new EdicionesDAO();
		try {
			eDAO.updateEdicion(edicion);

			hasError = false;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	/**
	 * En base al numero y publicacion retorna una edicion.
	 * @param tipo
	 * @param numero
	 * @return Edicion
	 */
	public Edicion obtenerEdicion(int tipo, int numero)
	{
		EdicionesDAO eDAO = new EdicionesDAO();
		try {
			Edicion edicion = eDAO.getEdicion(tipo,numero);

			hasError = false;

			return edicion;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * En base al tipo de edicion y fecha de publicacion retorna una edicion.
	 * @param tipo
	 * @param fecha
	 * @return Edicion
	 */
	public Edicion obtenerEdicion(int tipo, Date fecha)
	{
		EdicionesDAO eDAO = new EdicionesDAO();
		try {
			Edicion edicion = eDAO.getEdicion(tipo,fecha);

			hasError = false;

			return edicion;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Retorna todas las ediciones de una publicacion.
	 * @param tipo
	 * @return
	 */
	public List<Edicion> obtenerEdiciones(int tipo)
	{
		EdicionesDAO eDAO = new EdicionesDAO();
		try {
			List<Edicion> ediciones = eDAO.getEdiciones(tipo);

			hasError = false;

			return ediciones;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Retorna todas las ediciones de una publicacion con fecha de edicion menor o igual a la suministrada.
	 * @param tipo
	 * @param fecha
	 * @return List<edicion>
	 */
	public List<Edicion> obtenerEdiciones(int tipo,Date fecha)
	{
		EdicionesDAO eDAO = new EdicionesDAO();
		try {
			List<Edicion> ediciones = eDAO.getEdiciones(tipo,fecha);

			hasError = false;

			return ediciones;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Retorna todas las ediciones de cada una de las publicaciones activas en una fecha determinada
	 * @param fecha
	 * @return List<Edicion>
	 */
	public List<Edicion> obtenerEdiciones(Date fecha)
	{
		EdicionesDAO eDAO = new EdicionesDAO();
		try {
			List<Edicion> ediciones = eDAO.getEdiciones(fecha);

			hasError = false;

			return ediciones;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


	/**
	 * Publica una edicion.
	 * @param tipo
	 * @param numero
	 * @param cms
	 * @throws Exception
	 */
	public void publicarEdicion(int tipo, int numero, CmsObject cms) throws Exception
	{
		String siteRoot = cms.getRequestContext().getSiteRoot();

		try {

			cms.getRequestContext().setSiteRoot("/");

			Edicion edicion = obtenerEdicion(tipo,numero);

			TipoEdicionesDAO tDAO = new TipoEdicionesDAO();
			TipoEdicion tEdicion = tDAO.getTipoEdicion(edicion.getTipo());

			edicion.setTipoEdicion(tEdicion);


			cms.lockResource(edicion.getbaseURL());
			cms.unlockResource(edicion.getbaseURL());
			//cms.publishResource(edicion.getbaseURL());
			OpenCms.getPublishManager().publishResource(cms, edicion.getbaseURL());
			
			hasError = false;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		finally {
			cms.getRequestContext().setSiteRoot(siteRoot );
		}

	}

	/**
	 * De acuerdo al path retorna la edicion impresa a la que corresponde.
	 * @param cms
	 * @param path
	 * @return Edicion
	 * @throws Exception
	 */
	public Edicion obtenerEdicionImpresa(CmsObject cms, String path) throws Exception
	{

		Edicion edicion = new Edicion();

		TipoEdicionService tService = new TipoEdicionService();
		TipoEdicion tEdicion = tService.obtenerTipoEdicion(cms, path);

		String edicionStr = path;
		edicionStr = edicionStr.replaceFirst("/", "");
		edicionStr = edicionStr.replace(tEdicion.getNombre() + "/", "");
		edicionStr = edicionStr.substring(edicionStr.indexOf("/") +1);
		edicionStr = edicionStr.substring(edicionStr.indexOf("/") +1);
		edicionStr = edicionStr.substring(0, edicionStr.indexOf("/"));
		edicionStr = edicionStr.replace("edicion_", "");

		EdicionesDAO eDAO = new EdicionesDAO();
    	edicion = eDAO.getEdicion(tEdicion.getId(), Integer.parseInt(edicionStr));
    	edicion.setTipoEdicion(tEdicion);

    	return edicion;

	}


	/*
		Private methods
	 */

	protected void agregarIndice(Edicion edicion) throws UndefinedTipoEdicion
	{
		CmsSearchManager searchManager = OpenCms.getSearchManager();
		CmsSearchIndexSource indexSource = searchManager.getIndexSource(INDEX_SOURCENAME);
		indexSource.addResourceName(edicion.getbaseURL() + CONTENIDOS_DIRECTORY);

	}

	protected void quitarIndice(Edicion edicion) //throws UndefinedTipoEdicion
	{
		// TODO: Implementar quitar contenido del indice al eliminar una edicion.
	}

	protected void crearIndexHome(Edicion edicion, CmsObject cms, String siteRoot)
	{
		CmsResourceManager res_manager = OpenCms.getResourceManager();

		try {

			// Obtengo el tipo de recurso.
			int tipo_recurso = res_manager.getResourceType("indexHome").getTypeId();
			int tipo_recursoXml = res_manager.getResourceType("xmlpage").getTypeId();


			String templateFile = edicion.getbaseURL() + TEMPLATE_DIRECTORY + "homeIndex.jsp";
			templateFile = templateFile.replace(siteRoot, "");
			

			String fileName= edicion.getbaseURL() + HOME_DIRECTORY + "index.xml";

				//crear el recurso de los datos
				cms.createResource(fileName, tipo_recurso);
				cms.unlockResource(fileName);

				//crear home que apunta a template.
				String HomeName = edicion.getbaseURL() + "index.html";
				cms.createResource(HomeName, tipo_recursoXml);

				cms.writePropertyObject(HomeName, new CmsProperty("template", templateFile,null));
				cms.unlockResource(HomeName);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	protected void crearTitulares(Edicion edicion, CmsObject cms, String siteRoot)
	{
		CmsResourceManager res_manager = OpenCms.getResourceManager();

		try {

			// Obtengo el tipo de recurso.
			int tipo_recurso = res_manager.getResourceType("todasLasNoticias").getTypeId();
			int tipo_recursoXml = res_manager.getResourceType("xmlpage").getTypeId();


			String templateFile = edicion.getbaseURL() + TEMPLATE_DIRECTORY + "titulares.jsp";
			templateFile = templateFile.replace(siteRoot, "");


			String fileName= edicion.getbaseURL() + HOME_DIRECTORY + "titulos.xml";

				//crear el recurso de los datos
				cms.createResource(fileName, tipo_recurso);
				cms.unlockResource(fileName);

				//crear home que apunta a template.
				String HomeName = edicion.getbaseURL() + "titulares.html";
				cms.createResource(HomeName, tipo_recursoXml);

				cms.writePropertyObject(HomeName, new CmsProperty("template", templateFile,null));
				cms.unlockResource(HomeName);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	protected void crearIndexSecciones(Edicion edicion, CmsObject cms, String siteRoot) throws Exception
	{
		SeccionDAO sDAO = new SeccionDAO();

		CmsResourceManager res_manager = OpenCms.getResourceManager();

		try {

			// Obtengo el tipo de recurso.
			int tipo_recurso = res_manager.getResourceType("sectionHome").getTypeId();

			int tipo_recursoXml = res_manager.getResourceType("xmlpage").getTypeId();

			String templateFile = edicion.getbaseURL() + TEMPLATE_DIRECTORY + "sectionIndex.jsp";
			templateFile = templateFile.replace(siteRoot, "");

			for (Iterator iter = sDAO.getSeccionesByTipoEdicionId(edicion.getTipo()).iterator(); iter.hasNext();) {
				Seccion seccion = (Seccion) iter.next();

				String fileName= edicion.getbaseURL() + HOME_DIRECTORY + seccion.getPage().replace(".html", "").replace(".htm", "").replace(".HTML", "").replace(".HTM", "") + ".xml";

				//crear el recurso de los datos
				cms.createResource(fileName, tipo_recurso);
				cms.unlockResource(fileName);

				//crear home que apunta a template.
				String HomeName = edicion.getbaseURL() + seccion.getPage();
				
				if (!HomeName.contains(".html"))
					HomeName += ".html";
				cms.createResource(HomeName, tipo_recursoXml);

				cms.writePropertyObject(HomeName, new CmsProperty("template", templateFile,null));
				cms.unlockResource(HomeName);

			}
		} catch (Exception e) {}

	}

	protected void crearSeccionesEdicion(Edicion edicion, CmsObject cms) throws CmsIllegalArgumentException, CmsRuntimeException, Exception
	{
		String fileName= edicion.getbaseURL() + HOME_DIRECTORY + "secciones.xml";

		cms.lockResource(fileName);

		CmsFile contentFile = cms.readFile(fileName);
		CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, contentFile);

		SeccionDAO sDAO = new SeccionDAO();

		for (Iterator iter = sDAO.getSeccionesByTipoEdicionId(edicion.getTipo()).iterator(); iter.hasNext();) {
			Seccion seccion = (Seccion) iter.next();

			content.addValue(cms, "seccion", Locale.ENGLISH, 0); // adds in the position number 0
			content.getValue("seccion[1]", Locale.ENGLISH).setStringValue(cms, seccion.getName());
		}

		contentFile.setContents(content.marshal());
		cms.writeFile(contentFile);

		cms.unlockResource(fileName);

	}

	protected void establecerTemplateNotaEdicion(Edicion edicion, CmsObject cms, String siteRoot) throws UndefinedTipoEdicion, CmsException
	{
		String templateFile = edicion.getbaseURL() + TEMPLATE_DIRECTORY + "nota.jsp";
		templateFile = templateFile.replace(siteRoot, "");


		String contenidosDirectory = edicion.getbaseURL() + CONTENIDOS_DIRECTORY;

		cms.lockResource(contenidosDirectory);
		cms.writePropertyObject(contenidosDirectory, new CmsProperty("template-elements", templateFile,null));
		cms.unlockResource(contenidosDirectory);

	}

	/**
	 * Actualiza la fecha de publicacion de la edicion.
	 * @param edicion
	 */
	public void establecerFechaPublicacionEdicion(Edicion edicion) {
		EdicionesDAO eDAO = new EdicionesDAO();
		try {
			eDAO.updateFechaPublicacionEdicion(edicion);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Obtiene aquellas ediciones a publicar.
	 * @param now
	 * @return
	 */
	public List<Edicion> obtenerEdicionesAPublicar(Date now) {
		EdicionesDAO eDAO = new EdicionesDAO();
		List<Edicion> Result = null;
		try {
			Result = eDAO.getEdicionesAPublicar(now);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Result;
	}
	
	public List<Edicion> obtenerEdiciones(int tipo, Date desde, Date hasta)
	 {
	  EdicionesDAO eDAO = new EdicionesDAO();
	  try {
	    List<Edicion> ediciones = eDAO.getEdiciones(tipo, desde, hasta);
	    this.hasError = false;
	    return ediciones;
	  } catch (Exception e) {
	    throw new RuntimeException(e);
	  }
	 }

}
