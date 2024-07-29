package org.opencms.jsp;

import org.opencms.main.CmsException;
import org.opencms.main.TfsContext;

import com.tfsla.exceptions.ApplicationException;
import com.tfsla.utils.CmsResourceUtils;

public class TfsPropertyTag extends AbstractOpenCmsTag {

	private String property;
    private String search;
	
	public TfsPropertyTag() {
		super();
	}
	
	@Override
	public int doStartTag() {
		String fileName = null;
		boolean searchValue = false;
        if ("true".equals(this.search)) searchValue = true;
        
		try {
			fileName = CmsResourceUtils.getLink(this.getAncestor().getXmlDocument().getFile());
            String value = TfsContext.getInstance().getCmsObject().readPropertyObject(fileName, this.getProperty(), searchValue).getValue();
	        this.getWriter().append(value);
			return SKIP_BODY;
		}
		catch (CmsException e) {
			throw new ApplicationException("No se pudo leer la propiedad " + this.getProperty() + "del resource " + fileName, e);
		}
	}
	
	public String getProperty() {
		return this.property;
	}
	
	public void setProperty(String property) {
		this.property = property;
	}

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

}
