package org.opencms.jsp;

import javax.servlet.jsp.JspException;

/**
 * Es un tag de else. Necesita estar metido en un OpenCmsIfElseEvaluator <code>
 * <tfs:ifelse element="miContenidoBooleano">
 * 		<tfs:then>
 *		      <!--si es true se ejecuta esto -->
 *		</tfs:then>
 * 		<tfs:else>
 *            <!--si es false se ejecuta esto -->
 *		</tfs:else>
 * </tfs:ifelse>
 * </code>
 * 
 * @author lgassman
 */
public class OpenCMSElseBooleanTag extends AbstractOpenCmsBodyTag {

    @Override
    public int doStartTag() throws JspException {
        return !OpenCmsIfElseEvaluator.isTrue(this) ? EVAL_BODY_INCLUDE : SKIP_BODY;
    }

}
