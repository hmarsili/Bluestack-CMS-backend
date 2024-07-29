package org.opencms.jsp;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * El tag evalua si un String es igual a otro, una parte se ejecuta en cas
 * de que sean iguales y otra en caso de que sean distintos. Debe incluir
 * los tags OpenCMSEqualsThenTag y OpenCMSEqualsElseTag para que tenga sentido 
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
public class OpenCmsEqualsOrElseEvaluator extends AbstractOpenCmsBodyTag {

    private String value;
    
    public boolean equals() {
    	//System.out.println("Equals entre " + value + " y "+ this.getContent() + " es " + (this.value == null ? false : this.value.equals(this.getContent())));
        if (this.value == null) {
            return false;
        }
        
        return this.value.equals(this.getContent());
    }

    public static boolean equals(Tag tag) throws JspException {
        OpenCmsEqualsOrElseEvaluator eval = (OpenCmsEqualsOrElseEvaluator) TagSupport
        .findAncestorWithClass(tag, OpenCmsEqualsOrElseEvaluator.class);
        try{
            return eval.equals();
        }
        catch(NullPointerException ex) {
            throw new JspException("El tag " + tag + " debe estar incluido dentro de un equalsOrElse (" + OpenCmsEqualsOrElseEvaluator.class + ")"  , ex);
        }
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
