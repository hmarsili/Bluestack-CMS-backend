package com.tfsla.diario.friendlyTags;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;

import org.apache.commons.logging.Log;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.flex.CmsFlexController;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.I_CmsXmlDocument;
import org.opencms.xml.content.CmsXmlContentFactory;

import com.tfsla.trivias.model.TfsTrivia;
import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.TipoEdicionService;
import com.tfsla.diario.ediciones.services.TriviasService;
import com.tfsla.diario.model.TfsListaTrivias;


public class TfsTriviasUsersTag extends A_TfsTriviaUserValueTag implements I_TfsTrivia, I_TfsCollectionListTag, I_TfsTriviaUser {

	private static final long serialVersionUID = 1984143153049823965L;

	private static final Log LOG = CmsLog.getLog(TfsTriviasUsersTag.class);
	TfsTrivia previousTrivia = null;
	TfsListaTrivias previousListaTrivias = null;

	private CmsObject cms;

	protected I_CmsXmlDocument m_content;
    protected Locale m_contentLocale;
    protected Locale m_locale;
	
	private String size="10";
	private String page="1";
	private String site=null;
	private String publication=null;
	private String path =null;
	private String order =null;
	
	private CmsUser user;
	
	private String from=null;
	private String to=null;
	
	private String resultsType = null;
	
	private String tags=null;
	private String category=null;
	private String status=null;
	
	private List<TfsTrivia> trivias=null;
	
	private int index = 0;
	
	private TfsTrivia trivia;
	
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
	
	public String getOrder() {
		return order;
	}

	public void setOrder(String order) {
		this.order = order;
	}
	
	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		if (tags!=null && tags.equals(""))
			tags=null;
		this.tags = tags;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		if (category!=null && category.equals(""))
			category=null;
	
		this.category = category;
	}
	
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		if (status!= null && status.equals(""))
			status =null;
		this.status = status;
	}
	
	public CmsObject getCms() {
		return cms;
	}

	public void setCms(CmsObject cms) {
		this.cms = cms;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public String getPage() {
		return page;
	}

	public void setPage(String page) {
		if (page==null || page.trim().length()==0)
			this.page = "1";
		else
			this.page = page;
	}

	public String getPublication() {
		return publication;
	}

	public void setPublication(String publication) {
		this.publication = publication;
	}
	
	public String getSite() {
		return site;
	}

	public void setSite(String site) {
		this.site = site;
	}

	public String getFrom() {
		return from;
	}
	
	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		if (to!= null && to.equals(""))
			to =null;
		this.to = to;
	}
	
	public void setFrom(String from) {
		if (from!= null && from.equals(""))
			from =null;
		this.from = from;
	}
	
	public void setResultsType(String resultsType) {
		if (resultsType!= null && resultsType.equals(""))
			resultsType =null;
		this.resultsType = resultsType;
	}
	
	public String getResultsType() {
		return resultsType;
	}
	
	public void exposeTrivia() {
		
		TfsListaTrivias listaTrivias = new TfsListaTrivias(this.trivias.size(),this.index+1,Integer.parseInt(this.size),Integer.parseInt( this.page));
	    
		pageContext.getRequest().setAttribute("trivia", trivia);
		pageContext.getRequest().setAttribute("triviaList", listaTrivias);
		
	}
	
	@Override
	public int doStartTag() throws JspException {
			
		index = 0;
		
	    cms = CmsFlexController.getCmsObject(pageContext.getRequest());
    	findTrivias();
    				
		if (index<trivias.size()) {
			init(trivias.get(index));
			exposeTrivia();
			
			return EVAL_BODY_INCLUDE;
		}
		
		return SKIP_BODY;
	}
	
	@Override
	public int doAfterBody() throws JspException {

		index++;

		if (index==trivias.size())
			restoreTrivia();
		
		if (index<trivias.size()) {
			init(trivias.get(index));
			exposeTrivia();
			
			return EVAL_BODY_AGAIN;
		}
		
		if (OpenCms.getSystemInfo().getServletContainerSettings().isReleaseTagsAfterEnd()) {
			release();
		}
		return SKIP_BODY;
	}

   protected void restoreTrivia() {
    	pageContext.getRequest().setAttribute("trivia", previousTrivia);
       	pageContext.getRequest().setAttribute("triviaList", previousListaTrivias );
    }
	
	@Override
	public int doEndTag() {
		
		restoreTrivia();

		if (OpenCms.getSystemInfo().getServletContainerSettings().isReleaseTagsAfterEnd()) {
			release();
		}
		return EVAL_PAGE;
	}
	
	protected void findTrivias() throws JspTagException {
		
		List<TfsTrivia> triviasTemp=null;
		
		trivias = null;
		index=0;
	
	 	previousTrivia = (TfsTrivia) pageContext.getRequest().getAttribute("trivia");
    	pageContext.getRequest().setAttribute("trivia",null);

    	user = getCurrentUser().getUser();
    	TriviasService triviaService = new TriviasService();
    	
    	int publicationInt = 0;
    	
    	if(publication!=null)
    		publicationInt = Integer.parseInt(publication);
    	
    	TipoEdicion tEdicion = null;
    	
    	if(publication==null || (publication!=null && publication.equals("")) || site==null || (site!=null && site.equals("")))
    	{
    		String contextPath = cms.getRequestContext().getUri();
    		
    		TipoEdicionService tEService = new TipoEdicionService();
    		
    		try {
    			tEdicion = tEService.obtenerTipoEdicion(cms, contextPath);
    			
    			if(publication==null || (publication!=null && publication.equals("")))
    					publicationInt = tEdicion.getId();
    			
    			if(site==null || (site!=null && site.equals("")))
    				site = tEdicion.getProyecto();
    			
    		} catch (Exception e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
    	}
    		
    	triviasTemp = triviaService.getTriviasByUser(site,publicationInt,user.getId().toString(),Integer.parseInt(size), Integer.parseInt(page),resultsType,path,from,to,order);
	
    	if((status!=null && !status.equals("")) || (tags!=null && !tags.equals("")) || (category!=null && !category.equals(""))){
    		
    		TfsTrivia triviaTemp = null ;
    		trivias = new ArrayList<TfsTrivia>();
    		
    		for(int i=0;i<=triviasTemp.size()-1;i++){
    			triviaTemp = triviasTemp.get(i);
    			
    			com.tfsla.diario.model.TfsTrivia triviaContent = getTriviaContent(triviaTemp.getPath());
    			
    			String statusTrivia = triviaContent.getState();
    			String tagsTrivia = triviaContent.getTags();
    			List<String> categoriesTrivia = triviaContent.getCategories();
    			
    			boolean hasStatus = true;
    			
    			if(status!=null && !status.toLowerCase().equals(statusTrivia)){
    				hasStatus = false;
    			}
    			
    			boolean hasTags = false;
    			
    			if(tags!=null && !tags.equals(""))
    			{
    				String[] parts = tags.split(",");
    				
    				for(String part: parts){
    					if(tagsTrivia.indexOf(part)>-1)
    						hasTags = true;
    				}
    				
    			}else
    				hasTags = true;
    			
    			boolean hasCategories = false;
    			
    			if(category!=null && !category.equals("")){
    				String[] partsC = category.split("|");
    				
    				for(String partC: partsC){
    					
    					for(String cat:categoriesTrivia){
    						if(cat.indexOf(partC)>-1)
        						hasCategories = true;
    					}
    				}
    			}else
    				hasCategories = true;
    			
    			if(hasStatus && hasTags && hasCategories)
    			  trivias.add(triviaTemp);	
    			
    		}
    		
    	}else{
    		trivias = triviasTemp;
    	}
	}

	public int getIndex() {
		return index;
	}

	public boolean isLast() {
		return (index==trivias.size()-1);
	}
	
	public String getCollectionValue(String name) throws JspTagException {
		try {
			return getXmlDocument().getStringValue(cms, name, m_locale);
		} catch (CmsXmlException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}
	
	public int getCollectionIndexSize(String name, boolean isCollectionPart) {
		return getXmlDocument().getValues(name, m_locale).size();
	}

	public String getCollectionIndexValue(String name, int index) {
		try {
			return getXmlDocument().getStringValue(cms, name, m_locale, index);
		} catch (CmsXmlException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}

	public String getCollectionPathName() {
		return "";
	}
	
	protected void init(TfsTrivia triviaUsers)
	{
		trivia = new TfsTrivia();
		trivia = triviaUsers;
	 
		String resourcePath = trivia.getPath();
		
		com.tfsla.diario.model.TfsTrivia triviaContent = getTriviaContent(resourcePath);
		
		if(triviaContent!=null)
		  trivia.setTitle(triviaContent.getTitle());
		
	}
	
	private com.tfsla.diario.model.TfsTrivia getTriviaContent(String path){
		
		 try {
				CmsFile file = cms.readFile(path, CmsResourceFilter.ALL);
				
			       m_content = CmsXmlContentFactory.unmarshal(cms, file, pageContext.getRequest());

			        if (m_locale == null) {
			            // no locale set, use locale from users request context
			            m_locale = cms.getRequestContext().getLocale();
			        }

			        // check if locale is available
			        m_contentLocale = m_locale;
			        if (!m_content.hasLocale(m_contentLocale)) {
			            Iterator it = OpenCms.getLocaleManager().getDefaultLocales().iterator();
			            while (it.hasNext()) {
			                Locale locale = (Locale)it.next();
			                if (m_content.hasLocale(locale)) {
			                    // found a matching locale
			                    m_contentLocale = locale;
			                    break;
			                }
			            }
			        }
			        
			        com.tfsla.diario.model.TfsTrivia triviaContent = new com.tfsla.diario.model.TfsTrivia(cms,m_content,m_contentLocale,pageContext);
		
			        return triviaContent;
		 
		 } catch (CmsException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		 }  
		 
		 return null;
		
	}
	

	@Override
	public I_CmsXmlDocument getXmlDocument() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Locale getXmlDocumentLocale() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public com.tfsla.trivias.model.TfsTrivia getTrivia() {
		
		return trivia;
	}
}
