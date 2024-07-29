package org.opencms.jsp;

import javax.servlet.jsp.JspException;

import org.opencms.file.CmsFile;
import org.opencms.lock.CmsLock;
import org.opencms.lock.CmsLockType;
import org.opencms.main.TfsContext;


public class IsLockedTag extends AbstractOpenCmsBodyTag {

	public IsLockedTag() {
		super();
	}
	
	@Override
	public int doStartTag() throws JspException {
		return this.isLocked() ? EVAL_BODY_INCLUDE : SKIP_BODY;
	}

	private boolean isLocked() {
		CmsFile file = this.getAncestor().getXmlDocument().getFile();
		CmsLock lock;
		try {
			lock = TfsContext.getInstance().getCmsObject().getLock(file);
			return 	!lock.isNullLock() ||
					!(lock.getType() == CmsLockType.UNLOCKED);
		}
		catch (Exception e) {
			getLog().error("Ocurri� un error al intentar verificar si el recurso est� logueado.", e);
            return true;
		}
	}

}
