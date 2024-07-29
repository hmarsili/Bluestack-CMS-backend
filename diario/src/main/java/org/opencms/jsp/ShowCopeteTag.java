package org.opencms.jsp;

/**
 * Tag que remueve el <code><p></code> que aparece al principio del copete al pegar desde Word o Notepad.
 * 
 * @author mpotelfeola
 */
public class ShowCopeteTag extends AbstractOpenCmsTag {

	public ShowCopeteTag() {
		super();
	}
	
	@Override
	public int doStartTag() {
		String copete = this.getContent();
        
		copete = copete.replaceAll("</?(p|P)>", "");
        
        this.getWriter().print(copete);
		return SKIP_BODY;
	}
    
    @Override
    public String getElement() {
        return "copete";
    }

    
}
