package org.opencms.jsp;

import jakarta.servlet.jsp.JspException;

public class OpenCmsEqualsThenTag extends AbstractOpenCmsTag {

    /**
     * Es un tag para ejecutar sólo si un elemento es igual a un valor String, 
     * debe estar incluido en un tag OpenCmsEqualsIfElseEvaluator 
     * para que funcione 
     * 
     * <code>
     * <tfs:equalsOrElse element="miContenidoBooleano">
     *      <tfs:equalsThen>
     *            <!--si es igual se ejecuta esto -->
     *      </tfs:equalsThen>
     *      <tfs:equalsElse>
     *            <!--si es distinto se ejecuta esto -->
     *      </tfs:equalsElse>
     * </tfs:equalsOrElse>
     * </code>
     * @author mpotelfeola
     */
    @Override
    public int doStartTag() throws JspException {
        return OpenCmsEqualsOrElseEvaluator.equals(this) ? EVAL_BODY_INCLUDE
                : SKIP_BODY;
    }

}
