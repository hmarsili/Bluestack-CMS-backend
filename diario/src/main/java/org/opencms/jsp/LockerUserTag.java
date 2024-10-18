package org.opencms.jsp;

import jakarta.servlet.jsp.JspException;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.lock.CmsLock;
import org.opencms.lock.CmsLockType;
import org.opencms.main.TfsContext;
import org.opencms.util.CmsUUID;

public class LockerUserTag extends AbstractOpenCmsTag {

	public LockerUserTag() {
		super();
	}

	/**
	 * Ok. esta programado recontra defensivo, hay que revisarlo
	 */
	@Override
	public int doStartTag() throws JspException {
		try {
			CmsResource resource = this.getAncestor().getXmlDocument().getFile();
			CmsObject cmsObject = TfsContext.getInstance().getCmsObject(); 
			CmsLock lock = cmsObject.getLock(resource);
			if(!lock.isNullLock() || lock.getType() != CmsLockType.UNLOCKED ) {
				CmsUUID userId = lock.getUserId();
				if(userId != null && !userId.equals(CmsUUID.getNullUUID())) {
					CmsUser user = cmsObject.readUser(userId);
					if(user != null) {
						this.getWriter().append(user.getName());
					}
				}
			}
			return SKIP_BODY;
		}
		catch (Exception e) {
            getLog().error("Ocurri� un error al intentar verificar si el recurso est� logueado.", e);
            return SKIP_BODY;
		}
		
	}
}
