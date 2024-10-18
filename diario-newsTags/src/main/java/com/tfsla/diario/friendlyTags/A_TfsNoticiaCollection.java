package com.tfsla.diario.friendlyTags;

import jakarta.servlet.jsp.JspTagException;
import jakarta.servlet.jsp.tagext.BodyTagSupport;
import jakarta.servlet.jsp.tagext.Tag;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.opencms.file.CmsObject;
import org.opencms.flex.CmsFlexController;
import org.opencms.main.CmsLog;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.I_CmsXmlDocument;
import java.util.List;
import java.util.ArrayList;

public abstract class A_TfsNoticiaCollection extends BodyTagSupport implements I_TfsCollectionListTag {

    /** The log object for this class. */
    protected static final Log LOG = CmsLog.getLog(A_TfsNoticiaValue.class);

	protected int index=0;
	protected int lastElement=0;

	protected String keyName = "";

	protected String item="";

	protected List<Integer> selectedItems = null;

	protected boolean hasMoreContent() {
		index++;

		return (index<=lastElement);
	}

	public boolean isLast() {
		return (index==lastElement);
	}

	
/*	public String getCurrentKeyPrefix() {
		return keyName + "[" + (selectedItems!=null ? selectedItems.get(index-1) : index )  + "]/";
	}

	public String getCurrentKeyPrefix(int idx) {
		return keyName + "[" + (selectedItems!=null ? selectedItems.get(idx-1) : idx )  + "]/";
	}

	public String getCurrentKey() {
		return keyName + "[" + (selectedItems!=null ? selectedItems.get(index-1) : index )  + "]";
	}
*/
	public String getKeyName() {
		return keyName;
	}

	public int getIndex() {
		return index-1;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	protected void initSelectedItems()
	{
		selectedItems = null;
		//LOG.debug("initSelectedItems: " + item);
		if (!item.trim().equals(""))
		{
			String items[] = item.split(",");
			selectedItems = new ArrayList<Integer>();
			for (String value : items)
			{
				try
				{
					int iValue = Integer.parseInt(value);
					if (iValue<=lastElement)
						selectedItems.add(iValue);
				}
				catch (Exception e)
				{
					LOG.error("Invalidad data format in item", e);
				}
			}
			lastElement = selectedItems.size();
		}
	}
	
	protected void init(String key) throws JspTagException {
		keyName=key;
	
		index=0;
		lastElement = getLastElement();
		
		LOG.debug("A_TfsNoticiaCollection (INIT) - " + keyName + ": " + lastElement);
		initSelectedItems();
	}

	protected int getLastElement() throws JspTagException {
		
		return getCollectionIndexSize("", true);
		//I_TfsNoticia noticia = getCurrentNews();
			
		//return noticia.getXmlDocument().getIndexCount(getCollectionGeneralPathName(), noticia.getXmlDocumentLocale());
	}

 
	public String getCollectionValue(String name) throws JspTagException
	{
		// get a reference to the parent "content container" class
	    Tag ancestor = findAncestorWithClass(this, I_TfsCollectionListTag.class);
	    if (ancestor == null) {
	        throw new JspTagException("Tag " + this.getClass().getName() + " in wrong context.");
	    }

	    I_TfsCollectionListTag parentCollection = (I_TfsCollectionListTag) ancestor;

	    return parentCollection.getCollectionValue(name);

		//return getIndexElementValue(this.getCurrentNews(),name);
	}
	
/*	public int getCollectionIndexSize(String name) {
		try {
			return getIndexElementValuesSize(this.getCurrentNews(),name);
		} catch (JspTagException e) {
			// TODO Auto-generated catch block
			return 0;
		}
	}
*/
public int getCollectionIndexSize(String name, boolean isCollectionPart) throws JspTagException {

	
	String pathName = name;
	
	if (!pathName.equals(""))
		pathName = "/" + pathName;

	if (!isCollectionPart)
		pathName = "[" + (selectedItems!=null ? selectedItems.get(index-1) : index )  + "]" + pathName;

	pathName = keyName + pathName;
	
		
	// get a reference to the parent "content container" class
    Tag ancestor = findAncestorWithClass(this, I_TfsCollectionListTag.class);
    if (ancestor == null) {
        throw new JspTagException("Tag " + this.getClass().getName() + " in wrong context.");
    }

    I_TfsCollectionListTag parentCollection = (I_TfsCollectionListTag) ancestor;

    return parentCollection.getCollectionIndexSize(pathName, false);
    
}
	public String getCollectionIndexValue(String name, int index) throws JspTagException {
		return getIndexElementValues(this.getCurrentNews(),name,index);
	}
	
	protected I_TfsNoticia getCurrentNews() throws JspTagException {
		// get a reference to the parent "content container" class
	    Tag ancestor = findAncestorWithClass(this, I_TfsNoticia.class);
	    if (ancestor == null) {
	        throw new JspTagException("Tag News not accesible");
	    }
	
	    I_TfsNoticia noticia = (I_TfsNoticia) ancestor;
		return noticia;
	}
	
	
	public int getIndexValueInCollection(I_TfsNoticia noticia, String elementName, String value) throws JspTagException {
		
		int count = getLastElement();
	    index++;
	    //LOG.debug(noticia.getXmlDocument().getFile().getRootPath() + "| getIndexValueInCollection -> index:" + index + " - lastElement: " + count + " - items: " + (selectedItems!=null ? StringUtils.join(selectedItems.toArray(),",") : "null"));
		
	    while (index<=count) {
	    	
			String valueInIdx = getIndexElementValue(noticia,elementName);
			//LOG.debug(noticia.getXmlDocument().getFile().getRootPath() + "| getIndexValueInCollection -> (" + index + "): " + valueInIdx + " == " + value );
			if (valueInIdx.equals(value))
				return (index);
			
			index++;
		}
		
		return 0;
		
	}
	
	public String getIndexElementValue(I_TfsNoticia noticia, String elementName) {
	    // get the current users OpenCms context
	    CmsObject cms = CmsFlexController.getCmsObject(pageContext.getRequest());
	
	    // get loaded content from content container
	    I_CmsXmlDocument xmlContent = noticia.getXmlDocument();

	    String path="";
		try {
			path = getCollectionPathName();
		
		    if (!path.equals("") && !elementName.equals(""))
		    	path += "/";
		    
		    path += elementName;
		    
		    //LOG.debug("getIndexElementValue -> path:" + path);
		    return xmlContent.getStringValue(cms,  path, noticia.getXmlDocumentLocale());
		} catch (CmsXmlException e) {
			LOG.error("Error reading content value " + path,e);
		}
		catch (JspTagException e) {
			LOG.error("Error reading content value " + path,e);
		}
		return "";
	}

	public String getIndexElementValues(I_TfsNoticia noticia, String elementName,int index) {
	    // get the current users OpenCms context
	    CmsObject cms = CmsFlexController.getCmsObject(pageContext.getRequest());
	
	    // get loaded content from content container
	    I_CmsXmlDocument xmlContent = noticia.getXmlDocument();

	    String path="";
		try {
			path = getCollectionPathName();
		
		    if (!path.equals("") && !elementName.equals(""))
		    	path += "/";
		    
		    path += elementName;
		    
		    return xmlContent.getStringValue(cms,  path, noticia.getXmlDocumentLocale(),index);
		} catch (CmsXmlException e) {
			LOG.error("Error reading content value " + path,e);
		}
		catch (JspTagException e) {
			LOG.error("Error reading content value " + path,e);
		}
	
		return "";
	}

	public int getIndexElementValuesSize(I_TfsNoticia noticia, String elementName) {
	    // get the current users OpenCms context
	    CmsObject cms = CmsFlexController.getCmsObject(pageContext.getRequest());
	
	    // get loaded content from content container
	    I_CmsXmlDocument xmlContent = noticia.getXmlDocument();

	    String path="";

		try {
			path = getCollectionPathName();
		} catch (JspTagException e) {
			e.printStackTrace();
		}
		
	    if (!path.equals("") && !elementName.equals(""))
	    	path += "/";
	    
	    path += elementName;

		return xmlContent.getValues(path, noticia.getXmlDocumentLocale()).size();
	}

	public String getIndexElementValue(I_TfsNoticia noticia) {
	    // get the current users OpenCms context
	    CmsObject cms = CmsFlexController.getCmsObject(pageContext.getRequest());
	
	    // get loaded content from content container
	    I_CmsXmlDocument xmlContent = noticia.getXmlDocument();
	    
	    String path = "";
	    try {
	    	path = getCollectionPathName() ;
			return xmlContent.getStringValue(cms,  path, noticia.getXmlDocumentLocale());
		} catch (CmsXmlException e) {
			LOG.error("Error reading content value " + path ,e);
		} catch (JspTagException e) {
			LOG.error("Error reading content value " + path ,e);
		}
	
		return "";
	}

	
	protected String getIndexElementValue(I_TfsNoticia noticia, String elementName, int index) {
	    // get the current users OpenCms context
	    CmsObject cms = CmsFlexController.getCmsObject(pageContext.getRequest());
	    
	    // get loaded content from content container
	    I_CmsXmlDocument xmlContent = noticia.getXmlDocument();
	    
	    String path="";
	    try {
	    	path = getCollectionGeneralPathName() + "[" + (selectedItems!=null && !selectedItems.isEmpty() && selectedItems.size() >= index ? selectedItems.get(index-1) : index )  + "]" + (!elementName.equals("") ? "/" : "") + elementName;
	    	
	    	LOG.debug("getIndexElementValue > path:" + path);
			return xmlContent.getStringValue(cms,  path, noticia.getXmlDocumentLocale());
		} catch (CmsXmlException e) {
			LOG.error("Error reading content value " + path,e);
		} catch (JspTagException e) {
			LOG.error("Error reading content value " + path,e);
		}
	
		return "";
	}

	public String getItem() {
		return item;
	}

	public void setItem(String item) {
		this.item = item;
	}


	public String getCollectionGeneralPathName() throws JspTagException {
		// get a reference to the parent "content container" class
	    Tag ancestor = findAncestorWithClass(this, I_TfsCollectionListTag.class);
	    if (ancestor == null) {
	        throw new JspTagException("Tag " + this.getClass().getName() + " in wrong context.");
	    }

	    I_TfsCollectionListTag parentCollection = (I_TfsCollectionListTag) ancestor;
	    String pathName = parentCollection.getCollectionPathName();

	    if (!pathName.equals(""))
	    	pathName += "/";
	    
	    pathName += keyName;

	    return pathName;
	}
	
	public String getCollectionPathName() throws JspTagException {
		// get a reference to the parent "content container" class
	    Tag ancestor = findAncestorWithClass(this, I_TfsCollectionListTag.class);
	    if (ancestor == null) {
	        throw new JspTagException("Tag " + this.getClass().getName() + " in wrong context.");
	    }

	    I_TfsCollectionListTag parentCollection = (I_TfsCollectionListTag) ancestor;
	    String pathName = parentCollection.getCollectionPathName();
	    
	    if (!pathName.equals(""))
	    	pathName += "/";
	    try {
	    pathName += keyName + "[" + (selectedItems!=null && !selectedItems.isEmpty() ? selectedItems.get(index-1) : index )  + "]";
	    }
	    catch (java.lang.IndexOutOfBoundsException e) {
	    	LOG.error("error de indice - index:" + index + " - current pathName " + pathName + " in element " + keyName + " - items "  + (selectedItems!=null ? StringUtils.join(selectedItems.toArray(),",") : "null"),e);
	    }
	    
	    return pathName;
	}

}
