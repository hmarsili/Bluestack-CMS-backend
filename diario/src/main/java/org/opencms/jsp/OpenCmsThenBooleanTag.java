package org.opencms.jsp;

import javax.servlet.jsp.JspException;

/**
 * Es un tag para ejecutar sólo si un elemento booleano es true, 
 * debe estar incluído en un tag OpenCmsIfElseEvaluator 
 * para que funcione 
 * 
 * <code>
 * <tfs:ifelse element="miContenidoBooleano">
 * 		<tfs:then>
 *		      <!--si es true se ejecuta esto -->
 *		</tfs:then>
 * 		<tfs:else>
 *            <!--si es false se ejecuta esto -->
 *		</tfs:else>
 * </tfs:ifelse>
 * </code>
 * @author lgassman
 */

public class OpenCmsThenBooleanTag extends AbstractOpenCmsBodyTag {

	@Override
	public int doStartTag() throws JspException {
		return OpenCmsIfElseEvaluator.isTrue(this) ? EVAL_BODY_INCLUDE
				: SKIP_BODY;
	}

}
