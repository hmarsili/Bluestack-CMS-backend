package org.opencms.jsp;

import org.opencms.main.TfsContext;

/**
 * Agrega el bot�n para ocultar y mostrar los botones en el proyecto que no es online Por ahora se modific� para que
 * muestre juntos el bot�n de "botones" y el de "nuevo". M�s adelante ir�n separados.
 * 
 * @author lgassman
 */
public class ShowButtonsTag extends AbstractButtonsTag {

    public String image;
    public String newButtonSource = "/system/modules/com.tfsla.opencmsdev/elements/showButtonNew.html";
    
    protected String getButton() {
        return this.getBotonesButton() + "<br><br>" + this.getNuevoButton();
    }

    public String getImage() {
        return this.image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    private String getBotonesButton() {
        
    	String button = "";
    	       button += "<script type=\"text/javascript\">";
    	       button += "function toggleTfsButtons(){";
	           button += "$('.ocms_de_bt').toggle();";
	           button += "toggleVisibleOcms();";
	           button += "}";
	           button += "</script>";
    	
    	if (this.image == null) {
    		button += "<button id=\"changeButtonVisibility\" onclick=\"javascript:ttoggleTfsButtons();\">Botones</button>";
        }
        
        
    	button += "<input type=\"image\" src=\"" + this.image
                + "\" id=\"changeButtonVisibility\" onclick=\"javascript:toggleTfsButtons();\" />";
        
        return button;
    }

    private String getNuevoButton() {
        String uri = TfsContext.getInstance().getCmsObject().getRequestContext().getUri();
        return "<Iframe src=\"" + this.getNewButtonSource() + "?uri="
                + uri
                + "\" width=\"100\" height=\"30\" scrolling=\"no\" frameborder=\"0\" marginheight=\"0\" marginwidth=\"0\"></Iframe>";
    }

    public String getNewButtonSource() {
        return newButtonSource;
    }

    public void setNewButtonSource(String newButtonSource) {
        this.newButtonSource = newButtonSource;
    }

}