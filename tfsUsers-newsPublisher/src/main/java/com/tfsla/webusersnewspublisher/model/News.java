package com.tfsla.webusersnewspublisher.model;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.logging.Log;
import org.opencms.db.CmsResourceState;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsException;
import org.opencms.report.CmsLogReport;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.PrintfFormat;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
//import org.apache.commons.io.FilenameUtils;

import org.opencms.xml.types.I_CmsXmlContentValue;

import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.TipoEdicionService;
import com.tfsla.planilla.herramientas.PlanillaFormConstants;
import com.tfsla.utils.CmsResourceUtils;
import com.tfsla.webusersnewspublisher.service.NewsPublisherModerationManager;
import com.tfsla.webusersnewspublisher.service.UploadImageManager;

public class News {

	private Log LOG = CmsLog.getLog(this);
	private String title;
	private String subTitle;
	private String upperTitle;
	private String content;
	private String keywords;
	private String section;
	private List<String> categories = new ArrayList<String>();
	private Date dateCreated;
	private String internalUser;
	private List<FileItem> images = new ArrayList<FileItem>();
	private List<String> videosEmbedded = new ArrayList<String>();
	private List<String> fuentes = new ArrayList<String>();
	private boolean yearPathExists;
	private boolean monthPathExists;
	private boolean dayPathExists;
	private List<CmsResource> publishList = new ArrayList<CmsResource>();
	private String year = "";
	private String month = "";
	private String day = "";
	private String SITE = "";
	private String path = "";
	private boolean isPreview;
	private String publication = "";

	private CmsObject cmsObject = null;

	private String FOLDER = "";
	private String IMG_FOLDER = "";

	final String BASE_NAME = "/noticia_${number}.html";
	private TipoEdicion tipoEdicion = null;

	public News() {
		Date date = new Date();
		setDateCreated(date);
	}

	public void publish(String newsName) throws Exception {
		try {

			String folderName = "";
			boolean isEdit = false;

			if (newsName == null) {
				folderName = getFolderName();
				newsName = getNewsName(folderName);
				createNews(newsName);
			} else {
				isEdit = true;
			}

			setPath(newsName);

			loadNews(newsName, isEdit);

			if (!isEdit)
				publishFolder(folderName);

		} catch (Exception e) {
			throw e;
		}
	}

	private void lockTheFile(String file) throws CmsException {
		if (cmsObject.getLock(file).isUnlocked())
			cmsObject.lockResource(file);
		else {
			try {
				cmsObject.unlockResource(file);
				cmsObject.lockResource(file);
			} catch (Exception e) {
				cmsObject.changeLock(file);
			}
		}
	}

	public void delete(String fileName) throws Exception {
		lockTheFile(fileName);
		cmsObject
				.deleteResource(fileName, CmsResource.DELETE_PRESERVE_SIBLINGS);
		cmsObject.unlockResource(fileName);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void loadNews(String fileName, boolean isEdit)
			throws CmsException, Exception {

		lockTheFile(fileName);

		CmsFile file = cmsObject.readFile(fileName);
		CmsXmlContent xmlContent = CmsXmlContentFactory.unmarshal(cmsObject,
				file);

		List locales = xmlContent.getLocales();
		if (locales.size() == 0) {
			locales = OpenCms.getLocaleManager().getDefaultLocales(cmsObject,
					fileName);
		}

		Locale locale = OpenCms.getLocaleManager().getBestMatchingLocale(
				CmsLocaleManager.getLocale(""),
				OpenCms.getLocaleManager().getDefaultLocales(cmsObject,
						fileName), locales);

		// Establezco los valores.

		if (!isEdit) {
			xmlContent.getValue("urlFriendly", locale).setStringValue(
					cmsObject, removeInvalidXmlCharacters(getTitle()));

			xmlContent.getValue("estado", locale).setStringValue(cmsObject,
					PlanillaFormConstants.PUBLICADA_VALUE);
			// cmsObject.writePropertyObject(fileName, new
			// CmsProperty("state","publicada","publicada"));

			// xmlContent.addValue(cmsObject,"autor",locale,0);
			I_CmsXmlContentValue value = xmlContent.getValue(
					"autor/internalUser", locale);
			if (value == null) {
				xmlContent.addValue(cmsObject, "autor", locale, 0);
				xmlContent.getValue("autor/internalUser", locale)
						.setStringValue(cmsObject, getInternalUser());
			} else {
				value.setStringValue(cmsObject, getInternalUser());
			}
		} else if (xmlContent.getValue("urlFriendly", locale).toString()
				.equals("")
				&& getTitle() != null)
			xmlContent.getValue("urlFriendly", locale).setStringValue(
					cmsObject,
					removeInvalidXmlCharacters(xmlContent.getValue("titulo",
							locale).toString()));

		if (getSection() != null)
			xmlContent.getValue("seccion", locale).setStringValue(cmsObject,
					getSection());

		if (getDateCreated() != null)
			xmlContent.getValue("ultimaModificacion", locale).setStringValue(
					cmsObject, "" + getDateCreated().getTime());

		if (getTitle() != null)
			xmlContent.getValue("titulo", locale).setStringValue(cmsObject,
					removeInvalidXmlCharacters(getTitle()));

		if (getSubTitle() != null)
			xmlContent.getValue("copete", locale).setStringValue(cmsObject,
					removeInvalidXmlCharacters(getSubTitle()));

		if (getUpperTitle() != null)
			xmlContent.getValue("volanta", locale).setStringValue(cmsObject,
					removeInvalidXmlCharacters(getUpperTitle()));

		if (getContent() != null)
			xmlContent.getValue("cuerpo", locale).setStringValue(cmsObject,
					removeInvalidXmlCharacters(getContent()));

		if (getKeywords() != null)
			xmlContent.getValue("claves", locale).setStringValue(cmsObject,
					removeInvalidXmlCharacters(getKeywords()));

		int pos = 0;

		if (this.categories != null && this.categories.size() > 0) {
			for (String item : this.categories) {
				// if ( pos == 0)
				// {
				// if(!xmlContent.hasValue("Categorias", locale, 0))
				// xmlContent.addValue(cmsObject,"Categorias",locale,0);

				// xmlContent.getValue("Categorias[" + (pos + 1) +
				// "]",locale).setStringValue(cmsObject,item.toString());
				// }else{
				if (!xmlContent.hasValue("Categorias[" + (pos + 1) + "]",
						locale, pos))
					xmlContent.addValue(cmsObject, "Categorias[" + (pos + 1)
							+ "]", locale, pos);

				xmlContent.getValue("Categorias[" + (pos + 1) + "]", locale)
						.setStringValue(cmsObject, item.toString());
				// }
				pos++;
			}
		}

		pos = 0;

		// setting image fields values
		if (this.images != null && this.images.size() > 0) {
			for (FileItem item : this.images) {

				if (pos == 0) {
					if (!xmlContent.hasValue("imagenPrevisualizacion", locale,
							0))
						xmlContent.addValue(cmsObject,
								"imagenPrevisualizacion", locale, 0);

					xmlContent
							.getValue("imagenPrevisualizacion/imagen", locale)
							.setStringValue(cmsObject, getFileItemPath(item));
				}

				if (!xmlContent.hasValue("imagenesFotogaleria", locale, pos))
					xmlContent.addValue(cmsObject, "imagenesFotogaleria",
							locale, pos);

				xmlContent
						.getValue(
								"imagenesFotogaleria[" + (pos + 1) + "]/imagen",
								locale).setStringValue(cmsObject,
								getFileItemPath(item));

				pos++;
			}
		}

		// reinicializo la variable para videos
		pos = 0;

		if (this.videosEmbedded != null && this.videosEmbedded.size() > 0) {
			for (String item : this.videosEmbedded) {
				if (!xmlContent.hasValue("videoEmbedded", locale, pos))
					xmlContent
							.addValue(cmsObject, "videoEmbedded", locale, pos);

				xmlContent.getValue("videoEmbedded[" + (pos + 1) + "]/codigo",
						locale).setStringValue(cmsObject, item.toString());

				pos++;
			}
		}

		// reinicializo la variable para fuentes
		pos = 0;

		if (this.fuentes != null && this.fuentes.size() > 0) {
			for (String item : this.fuentes) {
				if (!xmlContent.hasValue("fuente", locale, pos))
					xmlContent.addValue(cmsObject, "fuente", locale, pos);

				xmlContent.getValue("fuente[" + (pos + 1) + "]/nombre", locale)
						.setStringValue(cmsObject, item.toString());

				pos++;
			}
		}

		file.setContents(xmlContent.marshal());

		cmsObject.getRequestContext().setSiteRoot(SITE + "/");
		cmsObject.writeFile(file);

		cmsObject.getRequestContext().setSiteRoot("/");

		if (!cmsObject.getLock(file).isUnlocked())
			cmsObject.unlockResource(fileName);

		cmsObject.getRequestContext().setSiteRoot(SITE + "/");
		NewsPublisherModerationManager.getInstance(cmsObject).premoderation(
				cmsObject, file, cmsObject.readUser(getInternalUser()),
				title + " " + content + " " + keywords);
		cmsObject.getRequestContext().setSiteRoot("/");

		try {
			if (dayPathExists || isEdit)
				publishList.add(cmsObject.readResource(fileName));
		} catch (Exception e) {
			throw e;
		}
	}

	public String getFileItemPath(String fileName) {
		return IMG_FOLDER + "/" + year + "/" + month + "/" + day + "/"
				+ UploadImageManager.getImageName(internalUser, fileName);
	}

	public String getFileItemPath(FileItem item) {
		return IMG_FOLDER
				+ "/"
				+ year
				+ "/"
				+ month
				+ "/"
				+ day
				+ "/"
				+ internalUser.replace("@", "").replace(".", "")
						.replace("webUser/", "")
				+ "_"
				+ cmsObject.getRequestContext().getFileTranslator()
						.translateResource(item.getName());
	}

	public String removeInvalidXmlCharacters(String input) {
		if (input == null) {
			return input;
		}
		char c;
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < input.length(); i++) {
			c = input.charAt(i);

			if (c == '\u200b') {
				continue;
			}
			if ((c == 0x9) || (c == 0xA) || (c == 0xD)
					|| ((c >= 0x20) && (c <= 0xD7FF))
					|| ((c >= 0xE000) && (c <= 0xFFFD))
					|| ((c >= 0x10000) && (c <= 0x10FFFF))) {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	@SuppressWarnings("rawtypes")
	protected void createNews(String fileName)
			throws CmsIllegalArgumentException, CmsLoaderException,
			CmsException {
		cmsObject.createResource(fileName, OpenCms.getResourceManager()
				.getResourceType("noticia").getTypeId(), null,
				new java.util.ArrayList());

		cmsObject.writePropertyObject(fileName, new CmsProperty("newsType",
				"post", "post", true));

	}

	public String getFolderName() throws Exception {
		yearPathExists = true;
		monthPathExists = true;
		dayPathExists = true;

		String baseUrl = getTipoEdicion().getBaseURL().replace("/contenidos/", "");
		String folderName = baseUrl;
		CmsResource folderToPublish = null;
		List<CmsResource> foldersPublishList = new ArrayList<CmsResource>();

		if (folderName.substring(folderName.length() - 1).equals("/")) {
			folderName = folderName.substring(0, folderName.length() - 1);
		}
		folderName += FOLDER + "/" + year;

		//System.out.println(folderName);
		//System.out.println("site "
		//		+ cmsObject.getRequestContext().getSiteRoot());
		if (!cmsObject.existsResource(folderName)) {
			try {
				folderToPublish = cmsObject.createResource(folderName,
						CmsResourceTypeFolder.RESOURCE_TYPE_ID);
				CmsResourceUtils.unlockResource(cmsObject, folderName, false);
				foldersPublishList.add(folderToPublish);
				yearPathExists = false;
			} catch(Exception e) {
				folderName = "/contenidos/" + year;
				if (!cmsObject.existsResource(folderName)) {
					folderToPublish = cmsObject.createResource(folderName,
							CmsResourceTypeFolder.RESOURCE_TYPE_ID);
					CmsResourceUtils.unlockResource(cmsObject, folderName, false);
					foldersPublishList.add(folderToPublish);
					yearPathExists = false;
				}
			}
		} else {
			folderToPublish = cmsObject.readResource(folderName);
			if (folderToPublish.getState() == CmsResourceState.STATE_NEW
					|| folderToPublish.getState() == CmsResourceState.STATE_CHANGED) {
				CmsResourceUtils.unlockResource(cmsObject, folderName, false);
				foldersPublishList.add(folderToPublish);
			}
		}

		folderName = folderName + "/" + month;

		if (!cmsObject.existsResource(folderName)) {
			folderToPublish = cmsObject.createResource(folderName,
					CmsResourceTypeFolder.RESOURCE_TYPE_ID);
			CmsResourceUtils.unlockResource(cmsObject, folderName, false);
			foldersPublishList.add(folderToPublish);
			monthPathExists = false;
		} else {
			folderToPublish = cmsObject.readResource(folderName);
			if (folderToPublish.getState() == CmsResourceState.STATE_NEW
					|| folderToPublish.getState() == CmsResourceState.STATE_CHANGED) {
				CmsResourceUtils.unlockResource(cmsObject, folderName, false);
				foldersPublishList.add(folderToPublish);
			}
		}

		folderName = folderName + "/" + day;

		if (!cmsObject.existsResource(folderName)) {
			folderToPublish = cmsObject.createResource(folderName,
					CmsResourceTypeFolder.RESOURCE_TYPE_ID);
			CmsResourceUtils.unlockResource(cmsObject, folderName, false);
			foldersPublishList.add(folderToPublish);
			dayPathExists = false;
		} else {
			folderToPublish = cmsObject.readResource(folderName);
			if (folderToPublish.getState() == CmsResourceState.STATE_NEW
					|| folderToPublish.getState() == CmsResourceState.STATE_CHANGED) {
				CmsResourceUtils.unlockResource(cmsObject, folderName, false);
				foldersPublishList.add(folderToPublish);
			}
		}

		if (foldersPublishList.size() > 0) {
			OpenCms.getPublishManager().publishProject(
					cmsObject,
					new CmsLogReport(Locale.getDefault(), this.getClass()),
					OpenCms.getPublishManager().getPublishList(cmsObject,
							foldersPublishList, false));
		}

		if(!folderName.startsWith(baseUrl)) {
			folderName = baseUrl + folderName;
		}
		return folderName.replace("//", "/");
	}

	protected void publishFolder(String folderName) throws Exception {
		folderName = getTipoEdicion().getBaseURL();
		folderName = folderName.replace("/contenidos/", "");

		if (folderName.substring(folderName.length() - 1).equals("/"))
			folderName = folderName.substring(0, folderName.length() - 1);

		try {
			if (!yearPathExists)
				publishList.add(cmsObject.readResource(folderName + FOLDER
						+ "/" + year));
			else if (!monthPathExists)
				publishList.add(cmsObject.readResource(folderName + FOLDER
						+ "/" + year + "/" + month));
			else if (!dayPathExists)
				publishList.add(cmsObject.readResource(folderName + FOLDER
						+ "/" + year + "/" + month + "/" + day));
		} catch (Exception e) {
			throw e;
		}
	}

	@SuppressWarnings("rawtypes")
	public String getNewsName(String folderName) throws Exception {
		List resources = new ArrayList<CmsResource>();
		if(!cmsObject.existsResource(folderName)) {
			try {
				ArrayList<CmsResource> folderPublish = new ArrayList<CmsResource>(); 
				folderPublish.add(cmsObject.createResource(folderName, CmsResourceTypeFolder.RESOURCE_TYPE_ID));
				CmsResourceUtils.unlockResource(cmsObject, folderName, false);
				OpenCms.getPublishManager().publishProject(
					cmsObject, 
					new CmsLogReport(Locale.getDefault(), this.getClass()),
					OpenCms.getPublishManager().getPublishList(cmsObject, folderPublish, false)
				);
			} catch(Exception e) {
				LOG.error("Error creating folder " + folderName, e);
			}
		} else {
			resources = cmsObject.readResources(folderName, CmsResourceFilter.ALL, false);
		}
		
		List<String> result = new ArrayList<String>(resources.size());
		for (int i = 0; i < resources.size(); i++) {
			CmsResource resource = (CmsResource) resources.get(i);
			result.add(resource.getRootPath());
		}
		
		String MACRO_NUMBER = "number";
		PrintfFormat NUMBER_FORMAT = new PrintfFormat("%0.4d");
		CmsMacroResolver resolver = CmsMacroResolver.newInstance();

		String fileName = folderName + BASE_NAME;
		int j = 0;
		String checkFileName = "";
		do {
			String number = NUMBER_FORMAT.sprintf(++j);
			resolver.addMacro(MACRO_NUMBER, number);
			checkFileName = resolver.resolveMacros(fileName);
		} while (result.contains(checkFileName));

		return checkFileName;
	}

	public void setSite(String site) {
		this.SITE = site;
	}

	public void setCmsObject(CmsObject cmsObject) {
		this.cmsObject = cmsObject;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		return this.title;
	}

	public void setSubTitle(String subTitle) {
		this.subTitle = subTitle;
	}

	public String getSubTitle() {
		return this.subTitle;
	}

	public void setUpperTitle(String upperTitle) {
		this.upperTitle = upperTitle;
	}

	public String getUpperTitle() {
		return this.upperTitle;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getContent() {
		return this.content;
	}

	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	public String getKeywords() {
		return this.keywords;
	}

	public void setSection(String section) {
		this.section = section;
	}

	public String getSection() {
		return this.section;
	}

	public void setVideosEmbedded(List<String> videosEmbedded) {
		this.videosEmbedded = videosEmbedded;
	}

	public List<String> getVideosEmbedded() {
		return this.videosEmbedded;
	}

	public void setFuentes(List<String> fuentes) {
		this.fuentes = fuentes;
	}

	public List<String> getFuentes() {
		return this.fuentes;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
		year = new SimpleDateFormat("yyyy").format(dateCreated);
		month = new SimpleDateFormat("MM").format(dateCreated);
		day = new SimpleDateFormat("dd").format(dateCreated);
	}

	public Date getDateCreated() {
		return this.dateCreated;
	}

	public void setInternalUser(String internalUser) {
		this.internalUser = internalUser;
	}

	public String getInternalUser() {
		return this.internalUser;
	}

	public void setImages(List<FileItem> images) {
		this.images = images;
	}

	public List<FileItem> getImages() {
		return this.images;
	}

	public List<CmsResource> getPublishList() {
		return this.publishList;
	}

	public String getPath() {
		return this.path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public boolean getIsPreview() {
		return this.isPreview;
	}

	public void setMode(boolean isPreview) {
		this.isPreview = isPreview;

		if (isPreview) {
			FOLDER = "/posts/tmp/contenidos";
			IMG_FOLDER = "/posts/tmp/img";
		} else {
			FOLDER = "/contenidos";
			IMG_FOLDER = "/img";
		}
	}

	public List<String> getCategory() {
		return categories;
	}

	public void setCategory(List<String> categories) {
		this.categories = categories;
	}

	protected TipoEdicion getTipoEdicion() {
		if (tipoEdicion == null) {
			TipoEdicionService tService = new TipoEdicionService();
			tipoEdicion = tService.obtenerTipoEdicion(Integer
					.parseInt(publication));
		}
		return tipoEdicion;
	}

	public String getPublication() {
		return publication;
	}

	public void setPublication(String publication) {
		this.publication = publication;
	}
}