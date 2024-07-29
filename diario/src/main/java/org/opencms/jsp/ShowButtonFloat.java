package org.opencms.jsp;

public class ShowButtonFloat extends AbstractButtonsTag {

    private String image;
    private String functionJS;
    
    protected String getButton() {
        if (this.image == null) {
            return "";
        }
        return "<input type=\"image\" src=\"" + this.image
                + "\" onclick=\"javascript:" + this.functionJS + ";\" />";
    }

    public String getImage() {
        return this.image;
    }

    public void setImage(String image) {
        this.image = image;
    }
	
    public String getfunctionJS() {
        return this.functionJS;
    }

    public void setfunctionJS(String functionJS) {
        this.functionJS = functionJS;
    }	
}