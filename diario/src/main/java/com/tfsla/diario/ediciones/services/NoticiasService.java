package com.tfsla.diario.ediciones.services;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.collectors.I_CmsResourceCollector;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.xml.CmsXmlEntityResolver;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.I_CmsXmlContentValue;

import com.tfsla.diario.ediciones.data.NoticiasDAO;
import com.tfsla.diario.ediciones.exceptions.UndefinedTipoEdicion;
import com.tfsla.diario.ediciones.model.Edicion;
import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.model.TipoPublicacion;
import com.tfsla.opencms.dev.collector.DateFolder;
import com.tfsla.opencmsdev.module.TfsConstants;
import com.tfsla.utils.CmsResourceUtils;
/**
 * Clase que realiza la administracion de las noticias.
 * @author Victor Podberezski.
 *
 */
public class NoticiasService extends baseService {

	private static final Log LOG = CmsLog.getLog(NoticiasService.class);

	private String module="newsTypes";

	public List<String> obtenerTiposDeNoticia(CmsObject obj, int tipoEdicion) {
		String siteName = OpenCms.getSiteManager().getCurrentSite(obj).getSiteRoot();

		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
		
 		return config.getParamList(siteName, "" + tipoEdicion, module, "list");
	}

	public String obtenerPathTipoDeNoticia(CmsObject obj, int tipoEdicion, String newsType) {
		String siteName = OpenCms.getSiteManager().getCurrentSite(obj).getSiteRoot();

		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
		return config.getItemGroupParam(siteName, "" + tipoEdicion, module, newsType,"path","");
	}
	
	public String obtenerTermTypeDeTipoDeNoticia(CmsObject obj, String tipoEdicion, String newsType) {
		String siteName = OpenCms.getSiteManager().getCurrentSite(obj).getSiteRoot();

		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
		return config.getItemGroupParam(siteName, "" + tipoEdicion, module, newsType,"termsType","");
	}

	public String obtenerUrlFriendlyFormatTipoDeNoticia(CmsObject obj, int tipoEdicion, String newsType) {
		String siteName = OpenCms.getSiteManager().getCurrentSite(obj).getSiteRoot();

		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
		return config.getItemGroupParam(siteName, "" + tipoEdicion, module, newsType,"UrlFriendlyFormat","");
	}

	public String obtenerUrlFriendlyRegExpTipoDeNoticia(CmsObject obj, int tipoEdicion, String newsType) {
		String siteName = OpenCms.getSiteManager().getCurrentSite(obj).getSiteRoot();

		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
		return config.getItemGroupParam(siteName, "" + tipoEdicion, module, newsType,"UrlFriendlyRegExp","");
	}

	public String obtenerUrlFriendlyFormatTipoDeNoticiaListItem(CmsObject obj, int tipoEdicion, String newsType) {
		String siteName = OpenCms.getSiteManager().getCurrentSite(obj).getSiteRoot();

		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
		return config.getItemGroupParam(siteName, "" + tipoEdicion, module, newsType,"UrlFriendlyFormatNoticiaListItem","");
	}

	public String obtenerUrlFriendlyRegExpTipoDeNoticiaListItem(CmsObject obj, int tipoEdicion, String newsType) {
		String siteName = OpenCms.getSiteManager().getCurrentSite(obj).getSiteRoot();

		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
		return config.getItemGroupParam(siteName, "" + tipoEdicion, module, newsType,"UrlFriendlyRegExpNoticiaListItem","");
	}

	public String obtenerUrlFriendlyFormatNoticiaListItem(CmsObject obj, int tipoEdicion) {
		String siteName = OpenCms.getSiteManager().getCurrentSite(obj).getSiteRoot();

		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
		return config.getItemGroupParam(siteName, "" + tipoEdicion, module, "news","UrlFriendlyFormatNoticiaListItem","");
	}

	public String obtenerUrlFriendlyRegExpNoticiaListItem(CmsObject obj, int tipoEdicion) {
		String siteName = OpenCms.getSiteManager().getCurrentSite(obj).getSiteRoot();

		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
		return config.getItemGroupParam(siteName, "" + tipoEdicion, module, "news","UrlFriendlyRegExpNoticiaListItem","");
	}

	
	public String obtenerDescripcionTipoDeNoticia(CmsObject obj, int tipoEdicion, String newsType) {
		String siteName = OpenCms.getSiteManager().getCurrentSite(obj).getSiteRoot();

		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
		
		return config.getItemGroupParam(siteName, "" + tipoEdicion, module, newsType,"niceName","");
	}

	public boolean noticiaConMombreManual(CmsObject obj, int tipoEdicion, String newsType) {

		String siteName = OpenCms.getSiteManager().getCurrentSite(obj).getSiteRoot();

 		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();

		return config.getBooleanItempGroupParam(siteName, "" + tipoEdicion, module, newsType,"nombreManual",false);

	}
	
	public boolean obtenerHabilitacion(CmsObject obj, int tipoEdicion, String newsType) {

		String siteName = OpenCms.getSiteManager().getCurrentSite(obj).getSiteRoot();

 		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();

		return config.getBooleanItempGroupParam(siteName, "" + tipoEdicion, module, newsType,"hide",false);

	}


	public boolean crearNoticia(CmsObject obj, String newsType,String fileName ) throws Exception
	{

		if (obj.existsResource(fileName))
			return false;
		
		int typeNoticia = OpenCms.getResourceManager().getResourceType("noticia").getTypeId();
		obj.createResource(fileName,typeNoticia);

		obj.writePropertyObject(fileName, new CmsProperty("newsType",newsType,newsType, true));
		return true;

	}

	public String crearNoticia(CmsObject obj, int tipoEdicion, String newsType, Map<String,String> parameters) throws Exception {
		return crearNoticia(obj, tipoEdicion, newsType, parameters, null);
	}
	
	public String crearNoticia(CmsObject obj, int tipoEdicion, String newsType, Map<String,String> parameters, Date date  ) throws Exception 
	{
		String siteName = OpenCms.getSiteManager().getCurrentSite(obj).getSiteRoot();

		TipoEdicionService tService = new TipoEdicionService();
		TipoEdicion tEdicion = tService.obtenerTipoEdicion(tipoEdicion);

		int shift=1;
		String fileName = "";
		boolean done = false;
		while (!done && shift <=10) {
			try {
				
			if (date==null)
				fileName = suggestFileName(obj, tipoEdicion, newsType,
					parameters, siteName, tEdicion,shift);
			else 
				fileName = suggestFileName(obj, tipoEdicion, newsType,
						parameters, siteName, tEdicion,shift,date);
			
			int typeNoticia = OpenCms.getResourceManager().getResourceType("noticia").getTypeId();
			obj.createResource(fileName,typeNoticia);
	
			obj.writePropertyObject(fileName, new CmsProperty("newsType",newsType,newsType, true));
			
			done=true;
			
			}
			catch (CmsException ex) {
				LOG.error("Intento " + shift + " - Error al crear la noticia " + fileName,ex);
				if (shift>=10)
					throw ex;
			} catch (Exception ex) {
				LOG.error("Intento " + shift + " - Error al crear la noticia " + fileName,ex);
				if (shift>=10)
					throw ex;
			}
			shift+=2;
		}
		return fileName;

	}
	
	public String getNewsByEdicion(CmsObject obj, int tipoEdicion, String newsType, Map<String,String> parameters ) throws Exception 
	{
		String siteName = OpenCms.getSiteManager().getCurrentSite(obj).getSiteRoot();

		TipoEdicionService tService = new TipoEdicionService();
		TipoEdicion tEdicion = tService.obtenerTipoEdicion(tipoEdicion);

		int shift=1;
		String fileName = "";
		boolean done = false;
		while (!done && shift <=10) {
			try {
	 		fileName = suggestFileName(obj, tipoEdicion, newsType,
					parameters, siteName, tEdicion,shift);
			
			done=true;
			
			}
			catch (CmsException ex) {
				LOG.error("Intento " + shift + " - Error al crear la noticia " + fileName,ex);
				if (shift>=10)
					throw ex;
			} catch (Exception ex) {
				LOG.error("Intento " + shift + " - Error al crear la noticia " + fileName,ex);
				if (shift>=10)
					throw ex;
			}
			shift+=2;
		}
		return fileName;

	}

	public String getFullRootPath(CmsObject obj, int tipoEdicion,
			String newsType, Map<String, String> parameters, String siteName,
			TipoEdicion tEdicion,String filename)  throws Exception, CmsException {

		Date now = new Date();

		String path = 	getFullRootPath(obj, tipoEdicion,
				newsType, parameters, siteName,
				tEdicion, now);

		return path + filename;

	}
	public String getFullRootPath(CmsObject obj, int tipoEdicion,
			String newsType, Map<String, String> parameters, String siteName,
			TipoEdicion tEdicion, Date now)  throws Exception, CmsException {
		
		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();

		
		String path = config.getItemGroupParam(siteName, "" + tipoEdicion, module, newsType,"path","");
		String subFolderFormat = config.getItemGroupParam(siteName, "" + tipoEdicion, module, newsType,"subFolderFormat","");
		if (path.equals("")) {
			path = tEdicion.getBaseURL();
			
			path = obj.getRequestContext().removeSiteRoot(path);
			if (!path.contains(EdicionService.CONTENIDOS_DIRECTORY))
				path += EdicionService.CONTENIDOS_DIRECTORY;
		}
		

		String subFolder = getVFSSubFolderPath( obj, path, CmsResourceTypeFolder.RESOURCE_TYPE_ID, subFolderFormat, now, parameters);

		String fullFileName = path + "/" + subFolder;

		return fullFileName;
	}

	public String suggestFileName(CmsObject obj, int tipoEdicion,
			String newsType, Map<String, String> parameters, String siteName,
			TipoEdicion tEdicion) throws Exception, CmsException {
	
		return suggestFileName(obj, tipoEdicion,
				newsType, parameters, siteName,
				tEdicion,1 );
	}
	
	public String suggestFileName(CmsObject obj, int tipoEdicion,
			String newsType, Map<String, String> parameters, String siteName,
			TipoEdicion tEdicion, int shift) throws Exception, CmsException  {
		
		Date now = new Date();
		
		return suggestFileName(obj, tipoEdicion,
				newsType, parameters, siteName,
				tEdicion, shift, now);
	}
	
	public String suggestFileName(CmsObject obj, int tipoEdicion,
			String newsType, Map<String, String> parameters, String siteName,
			TipoEdicion tEdicion, int shift, Date date) throws Exception, CmsException {
		
		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();

		String path = config.getItemGroupParam(siteName, "" + tipoEdicion, module, newsType,"path","");
		String subFolderFormat = config.getItemGroupParam(siteName, "" + tipoEdicion, module, newsType,"subFolderFormat","yyyy/MM/dd");
		if (path.equals("")) {
			path = tEdicion.getBaseURL();
			
			path = obj.getRequestContext().removeSiteRoot(path);
			if (!path.contains(EdicionService.CONTENIDOS_DIRECTORY))
				path += EdicionService.CONTENIDOS_DIRECTORY;
		}
		
		

		String subFolder = getVFSSubFolderPath( obj, path, CmsResourceTypeFolder.RESOURCE_TYPE_ID, subFolderFormat, date, parameters);

		String fileNamePreffix = config.getItemGroupParam(siteName, "" + tipoEdicion, module, newsType,"fileNamePreffix","noticia");

		String fileName = createNewsName(obj, path.endsWith("/") ? path + subFolder : path + "/" + subFolder,fileNamePreffix, shift);
		return fileName;
	}
	

	protected String getVFSSubFolderPath(CmsObject cmsObject, String parentPath, int folderType, String subFolderFormat, Date now, Map<String,String> parameters) throws Exception
	{
		String subFolder="";
		if (subFolderFormat.trim().equals(""))
			return "";
		
		String partialFolder = parentPath.endsWith("/") ? parentPath : parentPath + "/";
		String firstFolderCreated  = "";
		String[] parts = subFolderFormat.split("/");
		
		for (String part : parts)
		{
			String subfolderName = "";
			if (parameters.get(part)!=null)
				subfolderName = parameters.get(part);
			else if (part.startsWith("\"") && part.endsWith("\"")) {
				subfolderName = part.replaceAll("\"", "");
			}
			else
			{
				SimpleDateFormat sdf = new SimpleDateFormat(part);
				subfolderName = sdf.format(now);
			}
			partialFolder += subfolderName;
			subFolder += subfolderName;
			
			if (!cmsObject.existsResource(partialFolder)) {
				cmsObject.createResource(partialFolder, folderType);
				if (firstFolderCreated.equals(""))
					firstFolderCreated  = partialFolder;
			}
			partialFolder += "/";
			subFolder += "/";
		}
		if (!firstFolderCreated.equals(""))
		{
			OpenCms.getPublishManager().publishResource(cmsObject, firstFolderCreated);
		}
		return subFolder;
	}


	/**
	 * Determina si existen noticias cargadas en una seccion determinada.
	 * @param obj
	 * @param sectionName
	 * @param tEdicion
	 * @return True si existen noticias cargadas en la seccion.
	 * @throws Exception
	 */
	public boolean existenNoticiasEnSeccion(CmsObject obj,String sectionName, TipoEdicion tEdicion) throws Exception
	{
		NoticiasDAO nDAO = new NoticiasDAO();
		return (nDAO.hasNoticiasOfflineInSection(sectionName,tEdicion.getBaseURL()) || nDAO.hasNoticiasOnflineInSection(sectionName,tEdicion.getBaseURL()));

	}

	public boolean existenNoticiasEnZona(CmsObject obj, String ZonaName, String pageName) throws Exception {
		String collectorName = "Contenidos";
		String param = "/contenidos/noticia_${number}.html|50|target:" + pageName + "|zone:" + ZonaName;

		I_CmsResourceCollector collector = OpenCms.getResourceManager().getContentCollector(collectorName);
		List collectorResult = null;

		collectorResult = collector.getResults(obj, collectorName, param);

		return (collectorResult.size()>0);

	}

	/**
	 * Retorna el path de todas las noticias de una edicion para una seccion determinada
	 * @param tipoEdicion
	 * @param numeroEdicion
	 * @param seccion
	 * @return Lista de los paths a las noticias.
	 * @throws Exception
	 */
	public List<String> obtenerPathNoticias(int tipoEdicion, int numeroEdicion, String seccion) throws Exception
	{
		EdicionService eService = new EdicionService();

		Edicion edicion = eService.obtenerEdicion(tipoEdicion, numeroEdicion);

		TipoEdicionService tService = new TipoEdicionService();
		edicion.setTipoEdicion(tService.obtenerTipoEdicion(edicion.getTipo()));

		String path = edicion.getbaseURL() + "contenidos/";
		NoticiasDAO nDAO = new NoticiasDAO();
		return nDAO.getNoticiasImpresas(path,seccion);
	}

	/**
	 * Obtiene el path de todas las noticias de una edicion.
	 * @param tipoEdicion
	 * @param numeroEdicion
	 * @return
	 * @throws Exception
	 */
	public List<String> obtenerPathNoticias(int tipoEdicion, int numeroEdicion) throws Exception
	{
		EdicionService eService = new EdicionService();

		Edicion edicion = eService.obtenerEdicion(tipoEdicion, numeroEdicion);

		TipoEdicionService tService = new TipoEdicionService();
		edicion.setTipoEdicion(tService.obtenerTipoEdicion(edicion.getTipo()));

		String path = edicion.getbaseURL() + "contenidos/";
		NoticiasDAO nDAO = new NoticiasDAO();
		return nDAO.getNoticiasImpresas(path);

	}


	private String getNextNewsName(CmsObject obj,String location,int shift) throws CmsException
	{
		String fileName="";
		
		String[] niveles = location.split("/");
		
		//Solo si el contenido es de publicaciones de tipo ONLINE y ONLINE ROOT
		if (niveles.length <= 3)
			location = new DateFolder(location, true).getTodayFolder(obj);

		fileName = createNewsName(obj, location,"noticia", shift);

		return fileName;
	}

	private String createNewsName(CmsObject obj, String location, String fileNamePreffix, int shift)
			throws CmsException {
		String fileName;
		int maxNewsValue  = 0;
		List cmsFiles = obj.getResourcesInFolder(location, CmsResourceFilter.ALL);
		for (Iterator it = cmsFiles.iterator(); it.hasNext();) {
			CmsResource resource = (CmsResource) it.next();
			fileName = resource.getName();
			if (fileName.matches(".*" + fileNamePreffix + "_[0-9]{4}.html")) {
				String auxFileName =fileName.substring(fileName.indexOf(fileNamePreffix + "_"));
				int newsValue = Integer.parseInt(auxFileName.replace(fileNamePreffix + "_","").replace(".html",""));
				if (maxNewsValue<newsValue)
					maxNewsValue=newsValue;
			}
		}

		DecimalFormat df = new DecimalFormat("0000");
		fileName = location + fileNamePreffix + "_" + df.format(maxNewsValue+shift) + ".html"; // fileName.substring(0,fileName.indexOf("/noticia_")+9) + "${number}.html";
		return fileName;
	}


	/**
	 * Crea una noticia en el directorio indicado con la numeracion correspondiente.
	 * @param obj
	 * @param location
	 * @return Path y nombre de la noticia creada.
	 * @throws CmsIllegalArgumentException
	 * @throws CmsException
	 */
	public String crearNoticia(CmsObject obj,String location) throws CmsIllegalArgumentException, CmsException
	{
		int shift=1;
		String fileName = "";
		boolean done = false;
		while (!done && shift <=10) {
			try {
				fileName = getNextNewsName(obj,location,shift);

				int typeNoticia = OpenCms.getResourceManager().getResourceType("noticia").getTypeId();
				obj.createResource(fileName,typeNoticia);
		
				obj.writePropertyObject(fileName, new CmsProperty("newsType","news","news", true));
				done=true;
				
			}
			catch (CmsException ex) {
				LOG.error("Intento " + shift + " - Error al crear la noticia " + fileName,ex);
				if (shift>=10)
					throw ex;
			}
			shift+=2;
		}

		return fileName;
	}
	
	public String crearNoticia(CmsObject obj, int tipoEdicion, int numeroEdicion ) throws CmsIllegalArgumentException, CmsException, UndefinedTipoEdicion
	{
		TipoEdicionService tService = new TipoEdicionService();
		TipoEdicion tEdicion = tService.obtenerTipoEdicion(tipoEdicion);
		
		String path = tEdicion.getBaseURL();
		
		path = obj.getRequestContext().removeSiteRoot(path);
		if (tEdicion.isOnline())
		{
			if (!path.contains(EdicionService.CONTENIDOS_DIRECTORY))
				path += EdicionService.CONTENIDOS_DIRECTORY;
		}
		else
		{
			EdicionService eService = new EdicionService();
			Edicion edicion = eService.obtenerEdicion(tipoEdicion, numeroEdicion);
			path =  edicion.getbaseURL()+ EdicionService.CONTENIDOS_DIRECTORY;
		}
		
		return crearNoticia(obj, path);
	}
	
	/**
	 * Copia las noticias indicadas a la publicacion y edicion indicada.
	 * Si se detalla una seccion (no es null) establece las noticias copiadas con esa seccion.
	 * @param obj
	 * @param news
	 * @param tipoEdicion
	 * @param numeroEdicion
	 * @param seccion
	 * @throws UndefinedTipoEdicion
	 * @throws CmsException
	 */
	public void copiarNoticias(CmsObject obj, String[] news, int tipoEdicion, int numeroEdicion, String seccion) throws UndefinedTipoEdicion, CmsException {
		TipoEdicionService tService = new TipoEdicionService();
		TipoEdicion tEdicion = tService.obtenerTipoEdicion(tipoEdicion);

		String path = "/contenidos/";
		if (!TipoPublicacion.getTipoPublicacionByCode(tEdicion.getTipoPublicacion()).equals(TipoPublicacion.ONLINE_ROOT)){
			EdicionService eService = new EdicionService();
			Edicion edicion = eService.obtenerEdicion(tipoEdicion, numeroEdicion);

			edicion.setTipoEdicion(tEdicion);

			path = obj.getRequestContext().removeSiteRoot(edicion.getbaseURL() + "contenidos/");

		}

		for (int j=0; j < news.length ; j++) {
			String noticiaOrigen = news[j];
			String noticiaDestino = getNextNewsName(obj,path,1);

			obj.copyResource(noticiaOrigen, noticiaDestino);

			
			CmsProperty prop = new CmsProperty();
			prop.setName(TfsConstants.STATE_PROPERTY);
			prop.setValue("parrilla",CmsProperty.TYPE_INDIVIDUAL);
			obj.writePropertyObject(noticiaDestino, prop);
			
			if (seccion!="" && seccion!=null) {
				CmsFile contentFile = obj.readFile(noticiaDestino);
				CmsXmlContent content = CmsXmlContentFactory.unmarshal(obj, contentFile);
				content.getValue("seccion", Locale.ENGLISH).setStringValue(obj, seccion);
				contentFile.setContents(content.marshal());
				obj.writeFile(contentFile);

				prop = new CmsProperty();
				prop.setName("seccion");
				prop.setValue(seccion,CmsProperty.TYPE_INDIVIDUAL);
				obj.writePropertyObject(noticiaDestino, prop);
			}
			obj.unlockResource(noticiaDestino);
		}
	}

	/**
	 * Crea un sibling de las noticias a la publicacion y edicion indicada.
	 * @param obj
	 * @param news
	 * @param tipoEdicion
	 * @param numeroEdicion
	 * @throws UndefinedTipoEdicion
	 * @throws CmsException
	 */
	public void compartirNoticias(CmsObject obj, String[] news, int tipoEdicion, int numeroEdicion) throws UndefinedTipoEdicion, CmsException {
		TipoEdicionService tService = new TipoEdicionService();
		TipoEdicion tEdicion = tService.obtenerTipoEdicion(tipoEdicion);

		String path = "/contenidos/";
		if (!TipoPublicacion.getTipoPublicacionByCode(tEdicion.getTipoPublicacion()).equals(TipoPublicacion.ONLINE_ROOT))
		{
			EdicionService eService = new EdicionService();
			Edicion edicion = eService.obtenerEdicion(tipoEdicion, numeroEdicion);

			edicion.setTipoEdicion(tEdicion);

			path = obj.getRequestContext().removeSiteRoot(edicion.getbaseURL() + "contenidos/");
		}

		for (int j=0; j < news.length ; j++) {
			String noticiaOrigen = news[j];
			String noticiaDestino = getNextNewsName(obj,path,1);
			List props = new ArrayList();

			obj.createSibling(noticiaOrigen, noticiaDestino, props);
			obj.unlockResource(noticiaDestino);
		}

	}
	
	/**
	 * Retorna la cantidad de imagenes de fotogaleria que tiene una noticia.
	 * @param cmsObject (CmsObject)
	 * @param noticia (CmsResource)
	 * @return cantidad de imagenes cargadas (int)
	 */
	public int cantidadDeImagenesEnFotogaleria(CmsObject cmsObject, CmsResource noticia)
	{
		
		int nroImagenes=0;
		
		CmsFile contentFile;
		try {
			contentFile = cmsObject.readFile(noticia);
		
		//CmsXmlUtils.validateXmlStructure(contentFile.getContents(), new CmsXmlEntityResolver(cmsObject));
		//CmsXmlContent content = CmsXmlContentFactory.unmarshal(cmsObject, contentFile);

		CmsXmlContent content = CmsXmlContentFactory.unmarshal(cmsObject, contentFile);

		if (!cmsObject.getRequestContext().currentProject().isOnlineProject())
		{
			try {	
				CmsXmlUtils.validateXmlStructure(contentFile.getContents(), new CmsXmlEntityResolver(cmsObject));
			} catch (CmsException e) {
				CmsResourceUtils.forceLockResource(cmsObject, cmsObject.getSitePath(noticia));
				content.setAutoCorrectionEnabled(true);
		        content.correctXmlStructure(cmsObject);
		        CmsResourceUtils.unlockResource(cmsObject, cmsObject.getSitePath(noticia), false);
			}
		}
		
		int nroImagen = 1;
		String xmlName ="imagenesFotogaleria[" + nroImagen + "]";
		I_CmsXmlContentValue value = content.getValue(xmlName, Locale.ENGLISH);

		while (value!=null)
		{

			xmlName ="imagenesFotogaleria[" + nroImagen + "]/imagen[1]";
			value = content.getValue(xmlName, Locale.ENGLISH);

			if (value!=null)
			{
				String pathImagen = value.getStringValue(cmsObject);

				if (pathImagen!=null && pathImagen.length()>0)
					nroImagenes++;
			}
			nroImagen++;
			xmlName ="imagenesFotogaleria[" + nroImagen + "]";
			value = content.getValue(xmlName, Locale.ENGLISH);

		}
		} catch (CmsException e) {
			LOG.error("Error al intentar determinar la cantidad de imagenes en la noticia " + noticia.getRootPath() ,e);
		}
	
		return nroImagenes;
	}
	
	/**
	 * Retorna si la noticia tiene cargada una imagen de previsualizacion
	 * @param cmsObject (CmsObject)
	 * @param noticia (CmsResource)
	 * @return (int)
	 */
	public boolean tieneImagenPrevisualizacion(CmsObject cmsObject, CmsResource noticia, String fieldName) 
	{
		
		CmsFile contentFile;
		try {
			contentFile = cmsObject.readFile(noticia);
		
			CmsXmlContent content = CmsXmlContentFactory.unmarshal(cmsObject, contentFile);

			if (!cmsObject.getRequestContext().currentProject().isOnlineProject())
			{
				try {	
					CmsXmlUtils.validateXmlStructure(contentFile.getContents(), new CmsXmlEntityResolver(cmsObject));
				} catch (CmsException e) {
					CmsResourceUtils.forceLockResource(cmsObject, cmsObject.getSitePath(noticia));
					content.setAutoCorrectionEnabled(true);
			        content.correctXmlStructure(cmsObject);
			        CmsResourceUtils.unlockResource(cmsObject, cmsObject.getSitePath(noticia), false);
				}
			}
		//int nroImagen = 1;
		//String xmlName ="imagenPrevisualizacion[" + nroImagen + "]";
		String xmlName = fieldName;
		I_CmsXmlContentValue value = content.getValue(xmlName, Locale.ENGLISH);

		//if (value!=null)
		//{
		//	xmlName ="imagenesFotogaleria[" + nroImagen + "]/imagen[1]";
		//	value = content.getValue(xmlName, Locale.ENGLISH);

			if (value!=null)
			{
				String pathImagen = value.getStringValue(cmsObject);

				if (pathImagen!=null && pathImagen.length()>0)
					return true;
			}

		//}
		} catch (CmsException e) {
			LOG.error("Error al intentar determinar la existencia de imagen de previsualizacion de la noticia " + noticia.getRootPath(),e);
		}
	
		return false;
	}

	/**
	 * Retorna la cantidad de videos que tiene cargado una noticia
	 * @param cmsObject (CmsObject)
	 * @param noticia (CmsResource)
	 * @return (int)
	 */
	public int cantidadDeVideos(CmsObject cmsObject, CmsResource noticia)
	{		
		int nroVideos=0;
		
		CmsFile contentFile;
		try {
			contentFile = cmsObject.readFile(noticia);
		
			CmsXmlContent content = CmsXmlContentFactory.unmarshal(cmsObject, contentFile);

			if (!cmsObject.getRequestContext().currentProject().isOnlineProject())
			{

				try {	
					CmsXmlUtils.validateXmlStructure(contentFile.getContents(), new CmsXmlEntityResolver(cmsObject));
				} catch (CmsException e) {
					CmsResourceUtils.forceLockResource(cmsObject, cmsObject.getSitePath(noticia));
					content.setAutoCorrectionEnabled(true);
			        content.correctXmlStructure(cmsObject);
			        CmsResourceUtils.unlockResource(cmsObject, cmsObject.getSitePath(noticia), false);
				}
			}
		int nro = 1;
		String xmlName ="videos[" + nro + "]";
		I_CmsXmlContentValue value = content.getValue(xmlName, Locale.ENGLISH);

		while (value!=null)
		{

			xmlName ="videos[" + nro + "]/youtubeid[1]";
			value = content.getValue(xmlName, Locale.ENGLISH);

			if (value!=null)
			{
				String pathImagen = value.getStringValue(cmsObject);

				if (pathImagen!=null && pathImagen.length()>0)
					nroVideos++;
			}
			nro++;
			xmlName ="videos[" + nro + "]";
			value = content.getValue(xmlName, Locale.ENGLISH);

		}

		nro = 1;
		xmlName ="videoFlash[" + nro + "]";
		value = content.getValue(xmlName, Locale.ENGLISH);

		while (value!=null)
		{

			xmlName ="videoFlash[" + nro + "]/video[1]";
			value = content.getValue(xmlName, Locale.ENGLISH);

			if (value!=null)
			{
				String pathImagen = value.getStringValue(cmsObject);

				if (pathImagen!=null && pathImagen.length()>0)
					nroVideos++;
			}
			nro++;
			xmlName ="videoFlash[" + nro + "]";
			value = content.getValue(xmlName, Locale.ENGLISH);

		}

		nro = 1;
		xmlName ="videoDownload[" + nro + "]";
		value = content.getValue(xmlName, Locale.ENGLISH);

		while (value!=null)
		{

			xmlName ="videoDownload[" + nro + "]/video[1]";
			value = content.getValue(xmlName, Locale.ENGLISH);

			if (value!=null)
			{
				String pathImagen = value.getStringValue(cmsObject);

				if (pathImagen!=null && pathImagen.length()>0)
					nroVideos++;
			}
			nro++;
			xmlName ="videoDownload[" + nro + "]";
			value = content.getValue(xmlName, Locale.ENGLISH);

		}

		nro = 1;
		xmlName ="videoEmbedded[" + nro + "]";
		value = content.getValue(xmlName, Locale.ENGLISH);

		while (value!=null)
		{

			xmlName ="videoEmbedded[" + nro + "]/codigo[1]";
			value = content.getValue(xmlName, Locale.ENGLISH);

			if (value!=null)
			{
				String pathImagen = value.getStringValue(cmsObject);

				if (pathImagen!=null && pathImagen.length()>0)
					nroVideos++;
			}
			nro++;
			xmlName ="videoEmbedded[" + nro + "]";
			value = content.getValue(xmlName, Locale.ENGLISH);

		}

		} catch (CmsException e) {
			LOG.error("Error al intentar determinar la cantidad de videos en la noticia " + noticia.getRootPath() ,e);
		}
		return nroVideos;

		}

}
