package org.opencms.jsp;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.Tag;
import jakarta.servlet.jsp.tagext.TagSupport;


/**
 * Es un tag para evaluar un elemento, debe tener incluídos los tags 
 * OpenCMSThenBooleanTag y OpenCMSElseBooleanTag para que tenga sentido 
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
public class OpenCmsIfElseEvaluator extends AbstractOpenCmsBodyTag {

	public boolean isTrue() {
		return Boolean.valueOf(this.getContent());
	}

	public static boolean isTrue(Tag tag) throws JspException {
		OpenCmsIfElseEvaluator eval = (OpenCmsIfElseEvaluator) TagSupport
		.findAncestorWithClass(tag, OpenCmsIfElseEvaluator.class);
		try{
			return eval.isTrue();
		}
		catch(NullPointerException ex) {
			throw new JspException("El tag " + tag + " debe estar incluido dentro de un IfElse (" + OpenCmsIfElseEvaluator.class + ")"  , ex);
		}
	}

}
