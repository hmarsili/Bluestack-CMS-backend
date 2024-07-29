package org.opencms.jsp;

import javax.servlet.jsp.JspException;

/**
 * Ejecuta el Contenido si la condición es false
 * <code>
 * <tfs:ifnot element="miContenidoBooleano">
 *		 <!--si es false se ejecuta esto -->
 * </tfs:ifnot>
 * </code> 
 * @author lgassman
 */
public class OpenCmsBooleanFalseEvaluator extends AbstractOpenCmsTag {
	
	@SuppressWarnings("unused")
	@Override
	public int doStartTag() throws JspException {
		return Boolean.valueOf(this.getContent()) ?  SKIP_BODY : EVAL_BODY_INCLUDE ;
	}
}
