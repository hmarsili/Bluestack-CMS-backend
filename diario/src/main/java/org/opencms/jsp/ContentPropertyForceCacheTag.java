package org.opencms.jsp;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import jakarta.servlet.jsp.JspException;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.flex.CmsFlexController;
import org.opencms.main.CmsException;

/**
 * Imprime el resultado de una property, pero guarda en el bodyContext el valor de todas las properties de ese CmsResource, para evitar
 * hacer muchas busquedas
 * 
 * @author jpicasso
 *
 */
public class ContentPropertyForceCacheTag extends AbstractOpenCmsTag {
	private String name;

	@SuppressWarnings("unchecked")
	@Override
	public int doStartTag() throws JspException {
		CmsObject object = CmsFlexController.getController(this.getPageContext().getRequest()).getCmsObject();
		String resourceName = this.getAncestor().getResourceName();

		try {
			Map<String, String> map = (Map<String, String>) this.getPageContext().getAttribute(
					getContextKey(resourceName));
			String value = null;
			if (map == null) {
				map = createMap(object, resourceName);
			}
			value = map.get(name);
			this.getPageContext().getOut().print(value);
			return SKIP_BODY;
		}
		catch (Exception e) {
			throw new JspException("leyendo la propiedad " + getName() + " de " + resourceName, e);
		}
	}

	private Map<String, String> createMap(CmsObject object, String resourceName) throws JspException,
			CmsException {
		String contextKey = getContextKey(resourceName);
		Map<String, String> map;
		Iterator it = object.readPropertyObjects(resourceName, false).iterator();
		map = new HashMap<String, String>();
		while (it.hasNext()) {
			CmsProperty property = (CmsProperty) it.next();
			map.put(property.getName(), property.getValue());
		}
		this.getPageContext().setAttribute(contextKey, map);
		return map;
	}

	private String getContextKey(String resourceName) {
		return "PROPERTIES_" + resourceName;
	}

	private String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
