package com.tfsla.diario.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jakarta.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsLog;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.I_CmsXmlDocument;

import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.NoticiasService;
import com.tfsla.diario.ediciones.services.TipoEdicionService;
import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class TfsEvento {

	
	  /** The log object for this class. */
    protected static final Log LOG = CmsLog.getLog(TfsEvento.class);

    protected transient I_CmsXmlDocument m_content;
    protected transient Locale locale;
    protected transient CmsFile resource;
    
    protected CmsObject cms = null;
    PageContext pageContext = null;
    
    private TipoEdicion tEdicion=null;
	Map<String,Boolean> categories=null;
	Map<String,Boolean> subCategories=null;
	List<String> lCategories=null;
	Map<String,String> elements=null;

	public TfsEvento(CmsObject m_cms, I_CmsXmlDocument m_content, Locale locale, PageContext pageContext) {
		categories=null;
		subCategories=null;
		lCategories=null;
		elements=null;
	    this.m_content = m_content;
	    this.locale = locale;
	    this.pageContext = pageContext;
	    resource = m_content.getFile();
	    cms = m_cms;
	    tEdicion = getTipoEdicion();
	}
	
	
	
	public I_CmsXmlDocument getM_content() {
		return m_content;
	}



	public void setM_content(I_CmsXmlDocument m_content) {
		this.m_content = m_content;
	}



	public Locale getLocale() {
		return locale;
	}



	public void setLocale(Locale locale) {
		this.locale = locale;
	}



	public CmsFile getResource() {
		return resource;
	}



	public void setResource(CmsFile resource) {
		this.resource = resource;
	}



	public CmsObject getCms() {
		return cms;
	}



	public void setCms(CmsObject cms) {
		this.cms = cms;
	}



	public PageContext getPageContext() {
		return pageContext;
	}



	public void setPageContext(PageContext pageContext) {
		this.pageContext = pageContext;
	}



	public TipoEdicion gettEdicion() {
		return tEdicion;
	}



	public void settEdicion(TipoEdicion tEdicion) {
		this.tEdicion = tEdicion;
	}



	public Map<String, Boolean> getHascategory() {
		if (categories==null)
		{
			
			String key = TfsXmlContentNameProvider.getInstance().getTagName("news.categories");
			categories = new HashMap<String, Boolean>();
			int lastElement =  m_content.getIndexCount(key, locale);
			for (int j=1;j<=lastElement;j++){
				try {
					String categoria = m_content.getStringValue(cms,  key + "[" + j +"]" , locale);
					categories.put(categoria, true);
				} catch (CmsXmlException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		}
			
		return categories;
	}


	public List<String> getCategories() {
		if (lCategories==null)
		{
			
			String key = TfsXmlContentNameProvider.getInstance().getTagName("news.categories");
			lCategories = new ArrayList<String>();
			int lastElement =  m_content.getIndexCount(key, locale);
			for (int j=1;j<=lastElement;j++){
				try {
					String categoria = m_content.getStringValue(cms,  key + "[" + j +"]" , locale);
					lCategories.add(categoria);
				} catch (CmsXmlException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		}
			
		return lCategories;
		
	}

	public Map<String, Boolean> getSubCategories() {
		return subCategories;
	}

	public void setSubCategories(Map<String, Boolean> subCategories) {
		this.subCategories = subCategories;
	}

	public List<String> getlCategories() {
		return lCategories;
	}

	public void setlCategories(List<String> lCategories) {
		this.lCategories = lCategories;
	}

	public Map<String, String> getElements() {
		return elements;
	}

	public void setElements(Map<String, String> elements) {
		this.elements = elements;
	}

	public Map<String,String> getGenericElementValue(){
		if (elements==null) {
			elements = new HashMap<String, String>();
			Locale locale = cms.getRequestContext().getLocale();
			for (String key :m_content.getNames(locale))
			{
				if (m_content.getValue(key, locale).isSimpleType())
						elements.put(key, getElementValue(key));
			}
		}
		return elements;
	}

	protected String getElementValue(String elementName) {    
		try {
	    	String value = m_content.getStringValue(cms, elementName, locale);
	    	if (value==null) {
	    		value = "";
	    		LOG.debug("Content value " + elementName + "not found on content" + m_content.getFile().getRootPath());
	    	}
			return value;
		} catch (CmsXmlException e) {
			LOG.error("Error reading content value " + elementName + " on content " + m_content.getFile().getRootPath(),e);
		}
	
		return "";
	}
	public TipoEdicion getTipoEdicion() {
        TipoEdicionService tService =  new TipoEdicionService();
        
        TipoEdicion tEdicion=null;
           
		try {
			tEdicion = tService.obtenerTipoEdicion(cms, cms.getSitePath(resource));
		} catch (Exception e) {
			LOG.error("Publication not found", e);
			
		}
		return tEdicion;
	}
	
	public String getState(){
		return String.valueOf(this.m_content.getFile().getState().getState());
	}
	
	public CmsFile getFile() {
		return m_content.getFile();
	}

	public String getTags() {
		return getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.keywords")); //claves
	}
	
	public String getPeople() {
		return getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.people")); //personas
	}
	
	public String getStyle() {
		return getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.event.style")); //estilo
	}

	
	public String getAddress() {
		return getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.event.address")); //direccion	
	}
	
	public String getLocality() {
		return getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.event.locality")); //localidad	
	}
	
	public String getRegion() {
		return getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.event.region")); //region	
	}
	
	public String getCountry() {
		return getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.event.country")); //pais	
	}
	
	public String getPostalCode() {
		return getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.event.postalCode")); //codigoPostal	
	}
	
	public String getPlace() {
		return getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.event.place")); //lugar
	}
	
	public String getGeoLocation() {
		return getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.event.geoLocation")); //geolocalizacion 
	}
	
	public String getPriceValue() {
		return getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.price.price") + "/" + TfsXmlContentNameProvider.getInstance().getTagName("news.price.value")); //Valor del precio	
	}
	
	public String getPriceCurrency() {
		return getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.price.price") + "/" + TfsXmlContentNameProvider.getInstance().getTagName("news.price.currency")); //currency del precio	
	}
	
	public String getPriceAvailability() {
		return getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.price.price") + "/" + TfsXmlContentNameProvider.getInstance().getTagName("news.price.availability")); //disponibilidad del precio	
	}
	
	public String getPriceValidDate() {
		return getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.price.price") + "/" + TfsXmlContentNameProvider.getInstance().getTagName("news.price.validDate")); //fecha valida del precio	
	}
	
	public String getPriceUrl() {
		return getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.price.price") + "/" + TfsXmlContentNameProvider.getInstance().getTagName("news.price.url")); //url donde comprar 
	}
	
	public Date getStartDate() {
		String startDate = getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.event.startDate")); // fecha desde 
		Date uModif = new Date(Long.parseLong(startDate));
        
		return uModif;
	}
	
	public Date getToDate() {
		String toDate= getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.event.toDate")); // fecha hasta 
		if (toDate.equals("0"))
			return null;
		Date uModif = new Date(Long.parseLong(toDate));
	        
		return uModif;
	}
	
	public String getBody() {
		return getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.body")); //cuerpo
	}
	
	public Date getLastModificationDate() {
		String dateLong = getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.lastmodification"));
        Date uModif = new Date(Long.parseLong(dateLong));
        
		return uModif;
	}
	
	public Date getCreationDate() {
		Long dateLong = resource.getDateCreated();
        Date DateCreate = new Date(dateLong);
        
		return DateCreate;
	}
	
	public Date getModificationDate() {
		Long dateLong = resource.getDateLastModified();
        Date DateModification = new Date(dateLong);
        
		return DateModification;
	}
	
	public String getUppertitle() {
		return getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.uppertitle")); //volanta
	}
	
	
	
	public String getTitle() {
		String content = "";
		String key = TfsXmlContentNameProvider.getInstance().getTagName("news.title");
		
		Locale locale = cms.getRequestContext().getLocale();
			
			try {
				content = m_content.getStringValue(cms, key + "[1]", locale);
			} catch (CmsXmlException e) {
				e.printStackTrace();
			}
			
		return content; 
	}
	
	public long getLongDateExpired() {
		return resource.getDateExpired();
	}
	
	public boolean isDateExpiredSet() {
		return (resource.getDateExpired() != CmsResource.DATE_EXPIRED_DEFAULT);
	}	
	
	public int getImagescount() {
		NoticiasService nService = new NoticiasService();
		return nService.cantidadDeImagenesEnFotogaleria(cms, resource);
	}
	
	public String getLocalpath()
	{
        return cms.getSitePath(resource);
	}
	
	public boolean getIsExpired() {
		Date date = new Date();
		if(isDateExpiredSet()) {
			if(getLongDateExpired() < date.getTime()) return true;
		}
		return false;
	}
	
}
