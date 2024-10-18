package com.tfsla.diario.friendlyTags;


import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspTagException;
import jakarta.servlet.jsp.tagext.Tag;

import org.opencms.file.CmsObject;
import org.opencms.flex.CmsFlexController;
import org.opencms.xml.CmsXmlException;

import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class TfsPeopleContentTag extends A_TfsNoticiaSplitElement{//A_TfsNoticiaCollectionWithBlanks{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	TfsPeople people = null;
	TfsPeople previousPeople = null;
	private String currentItem;

    protected transient CmsObject m_cms;
    protected CmsFlexController m_controller;
    int index=0;
    
    public static final String param_id = "id";
    private String id = null;
	@Override
	public int doStartTag() throws JspException {		
		items = null;

	    I_TfsNoticia noticia = getCurrentNews();
	    String content = getElementValue(noticia,TfsXmlContentNameProvider.getInstance().getTagName("news.people"));
	    if (content.trim().equals(""))
	    	return SKIP_BODY;
	    
	    items = content.split(separator);

		Tag ancestor = findAncestorWithClass(this, TfsPeopleListTag.class);
	    if (ancestor == null) {
	        throw new JspTagException("Tag peopleList not accesible");
	    }
	    TfsPeopleListTag tagList = (TfsPeopleListTag) ancestor;
	    pageContext.getRequest().setAttribute("peoplecontent", tagList.getCurrentItem()!=null?tagList.getCurrentItem():"");
	    currentItem = tagList.getCurrentItem();
	    
		return (hasMoreContent() ? EVAL_BODY_INCLUDE : SKIP_BODY );
	}
	
	@Override
	public boolean hasMoreContent() {
		index++;
		idx++;
		
		if (!currentItem.trim().equals("")){
			try {
				if(id!= null){
					exposePeople(id);
				}else{
					exposePeople(null);
				}
			} catch (CmsXmlException e) {
				e.printStackTrace();
			}
			return true;
		}else{
			index++;
			idx++;
			restorePeople();
		}		
		
		return false;
	}
	
	protected void restorePeople()
    {    	
		pageContext.getRequest().setAttribute("people", previousPeople);
    }    
    
    protected void exposePeople(String id) throws CmsXmlException
    {   	
		m_controller = CmsFlexController.getController(pageContext.getRequest());
        m_cms = m_controller.getCmsObject();
        currentItem = currentItem.replace(",", "").trim();
        
    	try {
    		if(id != null && id != ""){
    			long idPerson = Long.valueOf(id);
    			people = new TfsPeople(idPerson);
    		}else{
    			people = new TfsPeople(currentItem, 1, "");
    		}
		} catch (JspTagException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}		
		pageContext.getRequest().setAttribute("people", people);
    	
    }
    
	@Override
	public int getIndex() {
		return index -1;
	}	

	@Override
	public String getCollectionValue(String name) throws JspTagException {
		if (name.equals("lastmodified"))
			return "" + people.getLastmodified(); 

		String elementName = getElementName(name);

		if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.people.id_person")))
			return (String) (people.getId_person() > 0 ? people.getId_person() : "");
		
		if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.people.name")))
			return (people.getName() != null ? people.getName() : "");
		
		if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.people.email")))
			return (people.getEmail() != null ? people.getEmail() : "");
				
		if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.people.birthdate")))
			return (String) (people.getBirthdate() != null ? people.getBirthdate() : "");
				
		if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.people.nickname")))
			return (people.getNickname() != null ? people.getNickname() : "");
		
		if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.people.photo")))
			return (people.getPhoto() != null ? people.getPhoto() : "");

		if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.people.url")))
			return (people.getUrl() != null ? people.getUrl() : "");
		
		if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.people.nacionality")))
			return (people.getNacionality() != null ? people.getNacionality() : "");		

		if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.people.twitter")))
			return (people.getTwitter() != null ? people.getTwitter() : "");
		
		if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.people.facebook")))
			return (people.getFacebook() != null ? people.getFacebook() : "");
		
		if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.people.google")))
			return (people.getGoogle() != null ? people.getGoogle() : "");
		
		if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.people.linkedin")))
			return (people.getLinkedin() != null ? people.getLinkedin() : "");
		
		if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.people.shortdescription")))
			return (people.getShortdescription() != null ? people.getShortdescription() : "");
		
		if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.people.longdescription")))
			return (people.getLongdescription() != null ? people.getLongdescription() : "");

		if (elementName.contains(TfsXmlContentNameProvider.getInstance().getTagName("news.people.type")))
			return (people.getShortdescription() != null ? people.getShortdescription() : "");
		
		return "";
	}
	
	private String getElementName(String elementName)
	{
		String[] parts = elementName.split("/");
		String element = parts[parts.length-1];
		
		int end = element.indexOf("[");
		
		return element.substring(0, end);
		
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		if (id==null || id.trim().length()==0)
			this.id = null;
		else
			this.id = id;
	}

}
