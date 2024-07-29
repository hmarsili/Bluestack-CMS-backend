package org.opencms.jsp;


/**
 * Ejecuta el Contenido si el elemento es distinto al value pasado
 * <code>
 * <tfs:notEquals element="miContenido" value="valor">
 *       <!--si es distinto al valor se ejecuta esto -->
 * </tfs:notEquals>
 * </code> 
 * @author mpotelfeola
 */
public class OpenCmsNotEqualsEvaluator extends AbstractOpenCmsTag {

    private String value;
    
    @Override
    public int doStartTag() {
        if (this.getValue() == null) {
            return SKIP_BODY;
        }
        
        if (!this.getValue().equals(this.getContent())) {
            return EVAL_BODY_INCLUDE;
        }
        
        return SKIP_BODY;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }
    
}
