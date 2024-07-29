package org.opencms.jsp;

import org.opencms.db.CmsResourceState;
import org.opencms.file.CmsResource;
import org.opencms.flex.CmsFlexController;

/**
 * Tag que evalua el body si es que el resource esta desincronizado con el online
 * 
 * @author lgassman
 */
public class IsModifiedTag extends AbstractOpenCmsBodyTag {

    public IsModifiedTag() {
        super();
    }

    public int doStartTag() {
        try {
            CmsResource file = CmsFlexController.getController(this.getPageContext().getRequest()).getCmsObject().readResource(
                    getAncestor().getResourceName());
            return isUnsynchronized(file) ? EVAL_BODY_INCLUDE : SKIP_BODY;
        }
        catch (Exception e) {
            this.getLog().error("Ha ocurrido un error al consultar si el recurso fue modificado.", e);
            return EVAL_BODY_INCLUDE;
        }
    }

    private boolean isUnsynchronized(CmsResource file) {
    	CmsResourceState state = file.getState();
        return state != CmsResource.STATE_UNCHANGED;
    }

}
