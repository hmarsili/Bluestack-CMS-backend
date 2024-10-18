package com.tfsla.diario.friendlyTags;

import java.util.StringTokenizer;

import jakarta.servlet.jsp.JspException;

import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class TfsVolantaTag extends A_TfsNoticiaValue {
	
	public TfsVolantaTag(){
		maxlength = -1;
		fullword = false;
		endchars = null;
	}

    @Override
    public int doStartTag() throws JspException {

        I_TfsNoticia noticia = getCurrentNews();
        
        String content = getElementValue(noticia,TfsXmlContentNameProvider.getInstance().getTagName("news.uppertitle")); //volanta
        
        if(maxlength>-1 && content!=null){
			if(!content.equals(""))content = getWrapText(content);
		}
        
        printContent(content);

        return SKIP_BODY;
    }
    
    private int maxlength;
    private boolean fullword;
    private String endchars;
	
	public int getMaxlength(){
		return maxlength;
	}
	
	public void setMaxlength(int maxlength){
		this.maxlength = maxlength;
	}
	
	
	public boolean getFullword(){
		return fullword;
	}
	
	public void setFullword(boolean fullword){
		this.fullword = fullword;
	}
	
	public String getEndchars(){
		return endchars;
	}
	
	public void setEndchars(String endchars){
		this.endchars = endchars;
	}

protected String getWrapText(String content){
		
		String titulo = content;
		
		int finalLength = maxlength;
		
		if (endchars!=null){
			finalLength = maxlength - endchars.length();
		}else{
			finalLength = maxlength;
			endchars="";
		}
		
		if(fullword){
			StringTokenizer stk = new StringTokenizer(content, " ");
			String cadena = "";
			String texto = "";
			
			while (stk.hasMoreTokens()) {
				String palabra = stk.nextToken().trim();
				cadena = cadena+" "+palabra;
				
				if(cadena.trim().length() < finalLength){ 
					texto = texto+" "+palabra;
				}
			}
		    content = texto;
			
		}else{
			if(finalLength< content.length())
			content = content.substring(0,finalLength);
		}
		
		if(titulo.length()>finalLength) content = content+endchars;

	    return content;
	}

}
