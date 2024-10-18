package com.tfsla.diario.friendlyTags;

import java.util.StringTokenizer;

import jakarta.servlet.jsp.JspException;

import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class TfsResumenTag extends A_TfsNoticiaValue {

	public TfsResumenTag(){
		maxlength = -1;
		fullword = false;
		endchars = null;
	}
	
    @Override
    public int doStartTag() throws JspException {

        I_TfsNoticia noticia = getCurrentNews();
        
        String content = getElementValue(noticia,TfsXmlContentNameProvider.getInstance().getTagName("news.summary"));  //resumen
        
        String contentStr1 = html2text(content);
        
        String contentStr2 ="";
        
        if(maxlength>-1 && content!=null){
			if(!content.equals(""))contentStr2 = getWrapText(contentStr1);
			
			if(!contentStr1.equals(contentStr2)) content = contentStr2;
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
	
	protected String html2text(String html) {
		
		String text = html.replaceAll("\\<.*?>","");
			   text = text.replaceAll("&nbsp;"," ");
			   text = text.replaceAll("&aacute;","á");
			   text = text.replaceAll("&eacute;","é");
			   text = text.replaceAll("&iacute;","í");
			   text = text.replaceAll("&oacute;","ó");
			   text = text.replaceAll("&uacute;","ú");
			   text = text.replaceAll("&ntilde;","ñ");
			   text = text.replaceAll("&Aacute;","Á");
			   text = text.replaceAll("&Eacute;","É");
			   text = text.replaceAll("&Iacute;","Í");
			   text = text.replaceAll("&Oacute;","Ó");
			   text = text.replaceAll("&Uacute;","Ú");
			   text = text.replaceAll("&Ntilde;","Ñ");
		
	    return text;
	}

}
