package com.tfsla.webusersadspublisher.model;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.fileupload.FileItem;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.main.OpenCms;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsException;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.PrintfFormat;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.apache.commons.io.FilenameUtils;

import com.tfsla.utils.CmsResourceUtils;

public class Ads{
	
	//ad fields
	private String title;
	private String summary;
	private String content;
	private String keywords;
	private String category;
	private String price;
	private String currency;
	private String status;
	private String type;
	private Date endDate;
	private String location;
	//images y videos
	private List<FileItem> images = new ArrayList<FileItem>();
	private List<String> videosEmbedded = new ArrayList<String>();
	//seller fields
	private String internalUser;
	private String firstName;
	private String lastName;
	private String email;
	private String webSite;
	private String addressNumber;
	private String addressName;
	private String postalCode;
	private String city;
	private String state;
	private String country;
	private String phoneNumber;
	private String logoImage;
	private String logoDescription;
	private String logokeywords;
	private String map_x;
	private String map_y;
	private String socialFacebook;
	private String socialTwitter;
	private String socialOther;
	//extra fields
	
	
	//internal variables
	private List<CmsResource> publishList = new ArrayList<CmsResource>();
	private String path = "";
	private boolean yearPathExists;
	private boolean monthPathExists;
	private boolean dayPathExists;
	private String year = "";
	private String month = "";
	private String day = ""; 
	private String SITE = "";
	
	private CmsObject cmsObject = null;
	
	private String FOLDER = "";
	private String IMG_FOLDER = "";
	
	final String BASE_NAME = "/ad_${number}.html";	
	
	public Ads(){
	}
	
	public void publish(String adsName) throws Exception{
		try {
			
			String folderName = "";
			boolean isEdit = false;
			
			if(adsName == null){
				folderName = getFolderName();
				adsName = getAdsName(folderName);
				createAds(adsName);
			}
			else{
				isEdit = true;
			}
			
			setPath(adsName);
			
			loadAds(adsName, isEdit);
			
			if(!isEdit)
				publishFolder(folderName);
			
		} catch (Exception e) {
			throw e;
		}		
	}
	
	private void lockResource(String resource) throws CmsException
	{
		if (cmsObject.getLock(resource).isUnlocked())
			cmsObject.lockResource(resource);
        else
        {
        	try {
        		cmsObject.unlockResource(resource);
        		cmsObject.lockResource(resource);
        	}
        	catch (Exception e)
        	{
        		cmsObject.changeLock(resource);	            		
        	}
        }
	}	
	
	@SuppressWarnings("unchecked")
	public void delete(String fileName) throws Exception{
		lockResource(fileName);
		cmsObject.deleteResource(fileName, CmsResource.DELETE_PRESERVE_SIBLINGS);
		cmsObject.unlockResource(fileName);
	}		
	
	@SuppressWarnings("unchecked")
	protected void loadAds(String fileName, boolean isEdit) throws CmsException, Exception{
		
		lockResource(fileName);
		
		CmsFile file = cmsObject.readFile(fileName);
		CmsXmlContent xmlContent = CmsXmlContentFactory.unmarshal(cmsObject, file);
		
		List locales = xmlContent.getLocales();
		if (locales.size() == 0) {
			locales = OpenCms.getLocaleManager().getDefaultLocales(cmsObject,fileName);
		}

		Locale locale = OpenCms.getLocaleManager().getBestMatchingLocale(CmsLocaleManager.getLocale(""),
			OpenCms.getLocaleManager().getDefaultLocales(cmsObject, fileName),locales);

		// setting ad fields values
		if (getTitle()!=null)
			xmlContent.getValue("titulo",locale).setStringValue(cmsObject,removeInvalidXmlCharacters(getTitle()));
		
		if (getSummary()!=null)
			xmlContent.getValue("destaque",locale).setStringValue(cmsObject,removeInvalidXmlCharacters(getSummary()));
		
		if (getContent()!=null)
			xmlContent.getValue("cuerpo",locale).setStringValue(cmsObject,removeInvalidXmlCharacters(getContent()));
		
		if (getKeywords()!=null)
			xmlContent.getValue("claves",locale).setStringValue(cmsObject,removeInvalidXmlCharacters(getKeywords()));
		
		if (getCategory()!=null)
			xmlContent.getValue("Categorias",locale).setStringValue(cmsObject,removeInvalidXmlCharacters(getCategory()));
		
		if (getPrice()!=null)
			xmlContent.getValue("precio",locale).setStringValue(cmsObject,removeInvalidXmlCharacters(getPrice()));
		
		if (getCurrency()!=null)
			xmlContent.getValue("moneda",locale).setStringValue(cmsObject,removeInvalidXmlCharacters(getCurrency()));
		
		if (getStatus()!=null)
			xmlContent.getValue("estado",locale).setStringValue(cmsObject,removeInvalidXmlCharacters(getStatus()));
				
		if (getType()!=null)
			xmlContent.getValue("tipo",locale).setStringValue(cmsObject,removeInvalidXmlCharacters(getType()));
		
		if (getEndDate()!=null)
			xmlContent.getValue("fechaFin",locale).setStringValue(cmsObject,getEndDate().toString());
		
		if (getLocation()!=null)
			xmlContent.getValue("ubicacion",locale).setStringValue(cmsObject,removeInvalidXmlCharacters(getLocation()));
		
		int pos = 0;
		
		//setting image fields values
		if ( this.images!=null && this.images.size() > 0 )
		{
			for (FileItem item:this.images) {
				
				if ( pos == 0) {
					if(!xmlContent.hasValue("imagenPrevisualizacion", locale, 0))
						xmlContent.addValue(cmsObject,"imagenPrevisualizacion",locale,0);
					
					xmlContent.getValue("imagenPrevisualizacion/imagen",locale).setStringValue(cmsObject, IMG_FOLDER + "/" + year + "/" + month + "/" + day + "/" + internalUser.replace("@", "").replace(".","").replace("webUser/", "") + "_" + FilenameUtils.getName(item.getName()));
				}
				
				if(!xmlContent.hasValue("imagenesFotogaleria", locale, pos))
					xmlContent.addValue(cmsObject,"imagenesFotogaleria", locale, pos);
				
				xmlContent.getValue("imagenesFotogaleria[" + (pos + 1) + "]/imagen", locale).setStringValue(cmsObject, IMG_FOLDER + "/" + year + "/" + month + "/" + day + "/" + internalUser.replace("@", "").replace(".","").replace("webUser/", "") + "_" + FilenameUtils.getName(item.getName()));
			
				pos++;
			}
		}
				
		pos = 0;
		
		//setting video fields values
		if ( this.videosEmbedded != null && this.videosEmbedded.size() > 0 )
		{
			for ( String item:this.videosEmbedded )
			{
				if ( !xmlContent.hasValue("videoEmbedded", locale, pos) )
					xmlContent.addValue(cmsObject,"videoEmbedded", locale, pos);
				
				xmlContent.getValue("videoEmbedded[" + (pos + 1) + "]/codigo", locale).setStringValue(cmsObject, item.toString());
					
				pos++;
			}
		}
		
		//setting seller field values
		if (!isEdit && getInternalUser()!=null)
			xmlContent.getValue("internalUser",locale).setStringValue(cmsObject, getInternalUser());
		
		if (getFirstName()!=null)
			xmlContent.getValue("internalUser",locale).setStringValue(cmsObject, removeInvalidXmlCharacters(getFirstName()));
		
		if (getLastName()!=null)
			xmlContent.getValue("nombre",locale).setStringValue(cmsObject, removeInvalidXmlCharacters(getLastName()));
		
		if (getEmail()!=null)
			xmlContent.getValue("mail",locale).setStringValue(cmsObject, removeInvalidXmlCharacters(getEmail()));
		
		if (getWebSite()!=null)
			xmlContent.getValue("pagina_web",locale).setStringValue(cmsObject, removeInvalidXmlCharacters(getWebSite()));
		
		if (getAddressNumber()!=null)
			xmlContent.getValue("altura",locale).setStringValue(cmsObject, removeInvalidXmlCharacters(getAddressNumber()));
		
		if (getAddressName()!=null)
			xmlContent.getValue("calle",locale).setStringValue(cmsObject, removeInvalidXmlCharacters(getAddressName()));
			
		if (getPostalCode()!=null)
			xmlContent.getValue("codigo_postal",locale).setStringValue(cmsObject, removeInvalidXmlCharacters(getPostalCode()));
		
		if (getCity()!=null)
			xmlContent.getValue("localidad",locale).setStringValue(cmsObject, removeInvalidXmlCharacters(getCity()));
		
		if (getState()!=null)
			xmlContent.getValue("provincia",locale).setStringValue(cmsObject, removeInvalidXmlCharacters(getState()));
		
		if (getCountry()!=null)
			xmlContent.getValue("pais",locale).setStringValue(cmsObject, removeInvalidXmlCharacters(getCountry()));
		
		if (getPhoneNumber()!=null)
			xmlContent.getValue("telefono",locale).setStringValue(cmsObject, removeInvalidXmlCharacters(getPhoneNumber()));

		if (getLogoImage()!=null)
			xmlContent.getValue("logo/imagen",locale).setStringValue(cmsObject, getLogoImage());
		
		if (getLogoDescription()!=null)
			xmlContent.getValue("logo/descripcion",locale).setStringValue(cmsObject, removeInvalidXmlCharacters(getLogoDescription()));
		
		if (getLogokeywords()!=null)
			xmlContent.getValue("logo/keywords",locale).setStringValue(cmsObject, getLogokeywords());

		if (getMap_x()!=null)
			xmlContent.getValue("mapa/coordenada_X",locale).setStringValue(cmsObject, getMap_x());
		
		if (getMap_y()!=null)
			xmlContent.getValue("mapa/coordenada_y",locale).setStringValue(cmsObject, getMap_y());
		
		if (getSocialFacebook()!=null)
			xmlContent.getValue("redes_sociales/facebook",locale).setStringValue(cmsObject, removeInvalidXmlCharacters(getSocialFacebook()));
		
		if (getSocialTwitter()!=null)
			xmlContent.getValue("redes_sociales/twitter",locale).setStringValue(cmsObject, removeInvalidXmlCharacters(getSocialTwitter()));
		
		if (getSocialOther()!=null)
			xmlContent.getValue("redes_sociales/otro",locale).setStringValue(cmsObject, removeInvalidXmlCharacters(getSocialOther()));

				
		file.setContents(xmlContent.marshal());

		cmsObject.getRequestContext().setSiteRoot(SITE + "/");
		cmsObject.writeFile(file);
		cmsObject.getRequestContext().setSiteRoot("/");

		if(!cmsObject.getLock(file).isUnlocked())
			cmsObject.unlockResource(fileName);
		
		try{
			if(dayPathExists || isEdit)
				publishList.add(cmsObject.readResource(fileName));
		}
		catch (Exception e){	
			throw e;
		}
	}
	
	public String removeInvalidXmlCharacters(String input){
		if (input == null) {
			return input;
		}
		char c;
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < input.length(); i++)
		{
			c = input.charAt(i);

			if (c == '\u200b') {
				continue;
			}
			if ((c == 0x9) || (c == 0xA) || (c == 0xD)
					|| ((c >= 0x20) && (c <= 0xD7FF))
					|| ((c >= 0xE000) && (c <= 0xFFFD))
					|| ((c >= 0x10000) && (c <= 0x10FFFF))
			) {
				sb.append(c);
			}
		}
		return sb.toString();
	}	
	
	protected void createAds(String fileName) throws CmsIllegalArgumentException, CmsLoaderException, CmsException {
		cmsObject.createResource(fileName, 
								OpenCms.getResourceManager().getResourceType("aviso").getTypeId(),
								null,
								new java.util.ArrayList());
	}	
	
	protected String getFolderName() throws Exception {
		yearPathExists = true;
		monthPathExists = true;
		dayPathExists = true;
		
		Date date = new Date();
		year = new SimpleDateFormat("yyyy").format(date);
		month = new SimpleDateFormat("MM").format(date);
		day = new SimpleDateFormat("dd").format(date);
		
		String folderName =  SITE + FOLDER + "/" + year;
		
		if (!cmsObject.existsResource(folderName)) {
			cmsObject.createResource(folderName, CmsResourceTypeFolder.RESOURCE_TYPE_ID);
			CmsResourceUtils.unlockResource(cmsObject, folderName, false);
			yearPathExists = false;
		}
		
		folderName = folderName + "/" + month;

		if (!cmsObject.existsResource(folderName)) {
			cmsObject.createResource(folderName, CmsResourceTypeFolder.RESOURCE_TYPE_ID);
			CmsResourceUtils.unlockResource(cmsObject, folderName, false);
			monthPathExists = false;
		}	
		
		folderName = folderName + "/" + day;

		if (!cmsObject.existsResource(folderName)) {
			cmsObject.createResource(folderName, CmsResourceTypeFolder.RESOURCE_TYPE_ID);
			CmsResourceUtils.unlockResource(cmsObject, folderName, false);
			dayPathExists = false;
		}
		
		return folderName;
	}
	
	protected void publishFolder(String folderName) throws Exception
	{
		try
		{
			if (!yearPathExists)
				publishList.add(cmsObject.readResource(SITE + FOLDER + "/" + year));
			else if (!monthPathExists)
				publishList.add(cmsObject.readResource(SITE + FOLDER + "/" + year + "/" + month));
			else if (!dayPathExists)
				publishList.add(cmsObject.readResource(SITE + FOLDER + "/" + year + "/" + month + "/" + day));
		}
		catch(Exception e){
			throw e;
		}
	}
		
	public String getAdsName(String folderName) throws CmsException {;
		List resources = cmsObject.readResources(folderName, CmsResourceFilter.ALL, false);
	    List<String> result = new ArrayList<String>(resources.size());
	    for (int i = 0; i < resources.size(); i++) {
	        CmsResource resource = (CmsResource)resources.get(i);
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
	    } 
	    while (result.contains(checkFileName));
	    
	    return checkFileName;
	}	
	
	public void setSite(String site) {
		this.SITE = site;
	}
	
	public void setCmsObject(CmsObject cmsObject){
		this.cmsObject = cmsObject;
	}	

	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getTitle() {
		return this.title;
	}
	
	public void setSummary(String summary){
		this.summary = summary;
	}

	public String getSummary(){
		return this.summary;
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
			
	public void setPrice(String price){
		this.price = price;
	}
	
	public String getPrice(){
		return this.price;
	}
	
	public void setCurrency(String currency){
		this.currency = currency;
	}
	
	public String getCurrency(){
		return this.currency;
	}
	
	public void setType(String type){
		this.type = type;
	}
	
	public String getType(){
		return this.type;
	}
	
	public void setVideosEmbedded(List<String> videosEmbedded) {
		this.videosEmbedded = videosEmbedded;
	}
	
	public List<String> getVideosEmbedded() {
		return this.videosEmbedded;
	}
	
	public void setInternalUser(String internalUser){
		this.internalUser = internalUser;
	}	
	
	public String getInternalUser(){
		return this.internalUser;
	}	
	
	public void setImages(List<FileItem> images){
		this.images = images;
	}	
	
	public List<FileItem> getImages(){
		return this.images;
	}	
	
	public List<CmsResource> getPublishList(){
		return this.publishList;
	}
	
	public String getPath(){
		return this.path;
	}
	
	public void setPath(String path) {
		this.path = path;
	}
	
	public void setMode(boolean isPreview){
		if (isPreview){
			FOLDER = "/avisos/tmp/contenidos";
			IMG_FOLDER = "/avisos/tmp/img";
		}else{
			FOLDER = "/avisos/contenidos";
			IMG_FOLDER = "/avisos/img";
		}
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirtName(String firtName) {
		this.firstName = firtName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getWebSite() {
		return webSite;
	}

	public void setWebSite(String webSite) {
		this.webSite = webSite;
	}

	public String getAddressNumber() {
		return addressNumber;
	}

	public void setAddressNumber(String addressNumber) {
		this.addressNumber = addressNumber;
	}

	public String getAddressName() {
		return addressName;
	}

	public void setAddressName(String addressName) {
		this.addressName = addressName;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getLogoImage() {
		return logoImage;
	}

	public void setLogoImage(String logoImage) {
		this.logoImage = logoImage;
	}

	public String getLogoDescription() {
		return logoDescription;
	}

	public void setLogoDescription(String logoDescription) {
		this.logoDescription = logoDescription;
	}

	public String getLogokeywords() {
		return logokeywords;
	}

	public void setLogokeywords(String logokeywords) {
		this.logokeywords = logokeywords;
	}

	public String getMap_x() {
		return map_x;
	}

	public void setMap_x(String map_x) {
		this.map_x = map_x;
	}

	public String getMap_y() {
		return map_y;
	}

	public void setMap_y(String map_y) {
		this.map_y = map_y;
	}

	public String getSocialFacebook() {
		return socialFacebook;
	}

	public void setSocialFacebook(String socialFacebook) {
		this.socialFacebook = socialFacebook;
	}

	public String getSocialTwitter() {
		return socialTwitter;
	}

	public void setSocialTwitter(String socialTwitter) {
		this.socialTwitter = socialTwitter;
	}

	public String getSocialOther() {
		return socialOther;
	}

	public void setSocialOther(String socialOther) {
		this.socialOther = socialOther;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

}