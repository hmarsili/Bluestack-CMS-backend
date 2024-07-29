package org.opencms.jsp;


/**
 * Ejecuta el Contenido si la condición es true
 * <code>
 * <tfs:if element="miContenidoBooleano">
 *		 <!--si es true se ejecuta esto -->
 * </tfs:if>
 * </code> 
 * @author lgassman
 */
public class OpenCmsBooleanTrueEvaluator extends AbstractOpenCmsTag {

	@Override
	public int doStartTag() {
		return Boolean.valueOf(this.getContent()) ? EVAL_BODY_INCLUDE : SKIP_BODY;
	}

}
