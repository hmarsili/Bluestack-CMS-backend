package com.tfsla.diario.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsLog;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.I_CmsXmlDocument;

import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.TipoEdicionService;
import com.tfsla.diario.ediciones.services.TriviasService;
import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class TfsTrivia {

	
	  /** The log object for this class. */
    protected static final Log LOG = CmsLog.getLog(TfsTrivia.class);

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
	Map<Integer,String> questionImage =null;
	Map<Integer,String> questionImageOpt =null;
	Map<Integer,Map<Integer,String>> questionOptionImage =null;
	Map<Integer,Map<Integer,Map<Integer,String>>> questionOption =null;
	Map<Integer,String> questionOptionPointsPoints =null;
	Map<Integer,Map<Integer,String>> questionOptionPoints =null;
	
	Map<Integer,Map<Integer,Map<Integer,String>>> questionOptionClassification =null;
	Map<Integer,String> questionOptionPointsPointsClassification =null;
	Map<Integer,Map<Integer,String>> questionOptionPointsClassification =null;
	
	
	Map<Integer,Integer> maxOptions =null;

	public TfsTrivia(CmsObject m_cms, I_CmsXmlDocument m_content, Locale locale, PageContext pageContext) {
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
			
			String key = TfsXmlContentNameProvider.getInstance().getTagName("trivia.categories");
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
	
	public Map<String,Boolean> getIsinsidecategory()
	{
		if (subCategories==null)
		{
			String key = TfsXmlContentNameProvider.getInstance().getTagName("trivia.categories");

			subCategories = new HashMap<String, Boolean>();
			int lastElement =  m_content.getIndexCount(key, locale);
			for (int j=1;j<=lastElement;j++){
				try {
					String categoria = m_content.getStringValue(cms,  key + "[" + j +"]" , locale);
					
					String[] subCategoria = categoria.split("/");
					String categ = "";
					for (String part : subCategoria)
					{
						categ += part + "/";
						subCategories.put(categ, true);
					}
				} catch (CmsXmlException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		}
			
		return subCategories;
	}

	public List<String> getCategories() {
		if (lCategories==null)
		{
			
			String key = TfsXmlContentNameProvider.getInstance().getTagName("trivia.categories");
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
		return getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("trivia.status"));
	}
	
	public CmsFile getFile() {
		return m_content.getFile();
	}

	public String getTags() {
		return getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("trivia.keywords")); 
	}
	
	public String getStyle() {
		return getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("trivia.style")); 
	}
	
	public Date getStartDate() {
		String startDate = getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("trivia.startDate"));   
		
		if(!startDate.equals("0")){
			Date uModif = new Date(Long.parseLong(startDate));
	        
			return uModif;
		}else
			return null;
	}
	
	public Date getCloseDate() {
		String toDate= getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("trivia.closeDate")); 
		
		if(!toDate.equals("0")){
			Date uModif = new Date(Long.parseLong(toDate));
		        
			return uModif;
		}else
			return null;
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
	
	public String getTitle() {
		String content = "";
		String key = TfsXmlContentNameProvider.getInstance().getTagName("trivia.title");
		
		Locale locale = cms.getRequestContext().getLocale();
			
			try {
				content = m_content.getStringValue(cms, key + "[1]", locale);
			} catch (CmsXmlException e) {
				e.printStackTrace();
			}
			
		return content; 
	}
	
	public String getDescription() {
		String content = "";
		String key = TfsXmlContentNameProvider.getInstance().getTagName("trivia.description");
		
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
	
	public String getLocalpath()
	{
        return cms.getSitePath(resource);
	}
	
	public String getImagePath()
	{
        return getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("trivia.image"));
	}
	
	public boolean isMultipleGame() {
		return Boolean.parseBoolean(getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("trivia.multipleGame")));
	}
	
	public boolean isRegisteredUser() {
		return Boolean.parseBoolean(getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("trivia.registeredUser")));
	}

	public boolean isStoreResults() {
		return Boolean.parseBoolean(getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("trivia.storeResults")));
	}
	
	public String getResultType(){
		return getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("trivia.resultsType"));
	}
	
	public String getParticipants() {
		 TriviasService service  = new TriviasService();
		 return String.valueOf(service.getCantUsers(cms.getRequestContext().removeSiteRoot(resource.getRootPath())));
	}
	
	public Map<Integer,Integer> getQuestionMaxOptions() {
		
		if (maxOptions==null) {
			String key = TfsXmlContentNameProvider.getInstance().getTagName("trivia.questions");
			maxOptions = new HashMap<Integer,Integer>();
			int lastElement =  m_content.getIndexCount(key, locale);
			for (int j=1;j<=lastElement;j++){
				try {
					String maxOptionsStr = m_content.getStringValue(cms,  key + "[" + j +"]/" + TfsXmlContentNameProvider.getInstance().getTagName("trivia.questions.maxOptions") , locale);
					
					maxOptions.put(j,Integer.parseInt(maxOptionsStr));
					
				} catch (CmsXmlException e) {
					LOG.error("Error al buscar las imagenes de las preguntas de la trivia", e);
				}
			}
		}
		return maxOptions;
	}
	
	public Map<Integer,String> getQuestionImagePath() {
		
		if (questionImage==null) {
			String key = TfsXmlContentNameProvider.getInstance().getTagName("trivia.questions");
			questionImage = new HashMap<Integer,String>();
			int lastElement =  m_content.getIndexCount(key, locale);
			for (int j=1;j<=lastElement;j++){
				try {
					String imageStr = m_content.getStringValue(cms,  key + "[" + j +"]/" + TfsXmlContentNameProvider.getInstance().getTagName("trivia.questions.image") , locale);
					
					questionImage.put(j,imageStr);
					
				} catch (CmsXmlException e) {
					LOG.error("Error al buscar las imagenes de las preguntas de la trivia", e);
				}
			}
		}
		return questionImage;
	}
	
	public Map<Integer,Map<Integer,String>> getQuestionOptionImagePath() {
		
		if (questionOptionImage==null) {
			String key = TfsXmlContentNameProvider.getInstance().getTagName("trivia.questions");
			
			questionOptionImage = new LinkedHashMap<Integer,Map<Integer,String>>();
			int lastElement =  m_content.getIndexCount(key, locale);
			
			for (int j=1;j<=lastElement;j++){
				try {
					
					String keyOption = TfsXmlContentNameProvider.getInstance().getTagName("trivia.questions.options");
					questionImageOpt = new HashMap<Integer,String>();
					int lastQuestionElement =  m_content.getIndexCount(key + "[" + j +"]/" + keyOption, locale);
					
					for (int o=1;o<=lastQuestionElement;o++){
						String imageStr = m_content.getStringValue(cms,  key + "[" + j +"]/" + keyOption + "[" + o +"]/" + TfsXmlContentNameProvider.getInstance().getTagName("trivia.questions.option.image") , locale);
						
						questionImageOpt.put(o,imageStr);
						
					}
					questionOptionImage.put(j, questionImageOpt);
					
				} catch (CmsXmlException e) {
					LOG.error("Error al buscar las imagenes de las opciones de las preguntas de la trivia", e);
				}
			}
		}
		return questionOptionImage;
	}
	
	public Integer getCountQuestions(){
		
		return getElementCountWithValue(TfsXmlContentNameProvider.getInstance().getTagName("trivia.questions"),TfsXmlContentNameProvider.getInstance().getTagName("trivia.questions.question"));
	}
	
	protected int getElementCountWithValue(String key, String controlKey)
	{
		Locale locale = cms.getRequestContext().getLocale();
		int total = m_content.getIndexCount(key, locale);
		
		int blank = 0;
		for (int j=1;j<=total;j++)
		{
			String controlValue;
			try {
				controlValue = m_content.getStringValue(cms, key + "[" + j + "]/" + controlKey, locale);
			
				if (controlValue==null || controlValue.trim().equals(""))
					blank ++;
			} catch (CmsXmlException e) {
				LOG.debug("Error reading content value " + key + "[" + j + "]/" + controlKey + " on content " + m_content.getFile().getRootPath(),e);

			}
		}
		
		
		return total - blank;
	}
	
	public String getJsonStatistic () {
		TriviasService triviaService = new TriviasService();
		return triviaService.getStatisticsString(cms.getRequestContext().removeSiteRoot(resource.getRootPath()), getResultType());
	}
	
	public Map<Integer,Map<Integer,Map<Integer,String>>> getQuestionOptionPointsPoints() {
		
		if (questionOption==null) {
			String key = TfsXmlContentNameProvider.getInstance().getTagName("trivia.questions");
			questionOption = new LinkedHashMap<Integer,Map<Integer,Map<Integer,String>>>();
			int lastElement =  m_content.getIndexCount(key, locale);
			
			for (int j=1;j<=lastElement;j++){
				try {
					String keyOption = TfsXmlContentNameProvider.getInstance().getTagName("trivia.questions.options");
					int lastQuestionElement =  m_content.getIndexCount(key + "[" + j +"]/" + keyOption, locale);
					questionOptionPoints = new HashMap<Integer,Map<Integer,String>>();
					
					for (int o=1;o<=lastQuestionElement;o++){
						questionOptionPointsPoints = new HashMap<Integer,String>();
						
						String keyPoints = TfsXmlContentNameProvider.getInstance().getTagName("trivia.questions.option.points") ;
						int lastOptionElement =  m_content.getIndexCount(key + "[" + j +"]/" + keyOption + "[" + o +"]/" + keyPoints, locale);
						for (int h=1;h<=lastOptionElement;h++){
							String imageStr = m_content.getStringValue(cms,  key + "[" + j +"]/" + keyOption + "[" + o +"]/" + keyPoints+ "[" + h+ "]/" + keyPoints,locale);
							questionOptionPointsPoints.put(h,imageStr);
						}
						questionOptionPoints.put(o, questionOptionPointsPoints);
					}
					questionOption.put(j, questionOptionPoints);
					
				} catch (CmsXmlException e) {
					LOG.error("Error al buscar los puntos de clasificacion de la trivia", e);
				}
			}
		}
		return questionOption;
	}
	
	public Map<Integer,Map<Integer,Map<Integer,String>>> getQuestionOptionPointsClassification() {
		
		if (questionOptionClassification==null) {
			String key = TfsXmlContentNameProvider.getInstance().getTagName("trivia.questions");
			questionOptionClassification = new LinkedHashMap<Integer,Map<Integer,Map<Integer,String>>>();
			int lastElement =  m_content.getIndexCount(key, locale);
			
			for (int j=1;j<=lastElement;j++){
				try {
					String keyOption = TfsXmlContentNameProvider.getInstance().getTagName("trivia.questions.options");
					int lastQuestionElement =  m_content.getIndexCount(key + "[" + j +"]/" + keyOption, locale);
					questionOptionPointsClassification = new HashMap<Integer,Map<Integer,String>>();
					
					for (int o=1;o<=lastQuestionElement;o++){
						questionOptionPointsPointsClassification = new HashMap<Integer,String>();
						
						String keyPoints = TfsXmlContentNameProvider.getInstance().getTagName("trivia.questions.option.points") ;
						String keyClassification = TfsXmlContentNameProvider.getInstance().getTagName("trivia.questions.option.points.classification");
						
						int lastOptionElement =  m_content.getIndexCount(key + "[" + j +"]/" + keyOption + "[" + o +"]/" + keyPoints, locale);
						for (int h=1;h<=lastOptionElement;h++){
							String imageStr = m_content.getStringValue(cms,  key + "[" + j +"]/" + keyOption + "[" + o +"]/" + keyPoints+ "[" + h+ "]/" + keyClassification,locale);
							questionOptionPointsPointsClassification.put(h,imageStr);
						}
						questionOptionPointsClassification.put(o, questionOptionPointsPointsClassification);
					}
					questionOptionClassification.put(j, questionOptionPointsClassification);
					
				} catch (CmsXmlException e) {
					LOG.error("Error al buscar los puntos de clasificacion de la trivia", e);
				}
			}
		}
		return questionOptionClassification;
	}
	
}
