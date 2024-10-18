package com.tfsla.diario.friendlyTags;

import java.io.IOException;
import java.util.StringTokenizer;

import jakarta.servlet.jsp.JspException;

import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class TfsTitulosTag extends A_TfsNoticiaCollectionWithBlanks  {

	public TfsTitulosTag(){
		value = null;
		onblankusedefault = true;
		wordlist = null;
		maxlength = -1;
		fullword = false;
		endchars = null;
	}
	
	@Override
	public int doStartTag() throws JspException {

		keyControlName = TfsXmlContentNameProvider.getInstance().getTagName("news.title"); //"titulo";

		init(TfsXmlContentNameProvider.getInstance().getTagName("news.title"));
		
		I_TfsNoticia noticia = getCurrentNews();
		
		int index = 0;
		
		TfsTitulosNoticia s = new TfsTitulosNoticia();
		
		
		if(value!=null && value!=""){
		  
			value.trim();
			
			try {
				index = Integer.parseInt(value);
			} catch (NumberFormatException nfe){
				
			}
			
			if(index<1){
				 
				if(value.equals("home")){
					index = s.getTitleIndex(noticia, TfsXmlContentNameProvider.getInstance().getTagName("news.title.home"),pageContext); 
				}
				
				if(value.equals("detail")){
					index = s.getTitleIndex(noticia, TfsXmlContentNameProvider.getInstance().getTagName("news.title.detail"),pageContext); 
				}
				
				if(value.equals("section")){
					index = s.getTitleIndex(noticia, TfsXmlContentNameProvider.getInstance().getTagName("news.title.section"),pageContext); 
				}
				
				if(value.equals("seo")){
					index = s.getTitleIndex(noticia, TfsXmlContentNameProvider.getInstance().getTagName("news.title.seo"),pageContext);
					if (index == 0)
						index = s.getTitleIndex(noticia, TfsXmlContentNameProvider.getInstance().getTagName("news.title.home"),pageContext);
				}
				
				if(value.equals("most-relevant")){
				    index = s.getMostRelevant(noticia,wordlist, pageContext);
				}
			   
			}
		}else{
			index = 1;
		}
		
		setIndex(index);
		String content = getIndexElementValue(noticia); 
		
		if(index>1 && (content==null || content.equals("")) && onblankusedefault )
		{
			setIndex(1);
			content = getIndexElementValue(noticia); 
						
		}
		
		if(maxlength>-1 && content!=null){
			if(!content.equals(""))content = getWrapText(content);
		}
	    
		try {
			
			pageContext.getOut().print(content);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return SKIP_BODY;
	}
	
	private String value;
	
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	private boolean onblankusedefault;
	
	public boolean getOnblankusedefault(){
		return onblankusedefault;
	}
	
	public void setOnblankusedefault(boolean onblankusedefault){
		this.onblankusedefault = onblankusedefault;
	}
	
	private String wordlist;
	
	public String getWordlist(){
		return wordlist;
	}
	
	public void setWordlist(String wordlist){
		this.wordlist = wordlist;
	}
	
	private int maxlength;
	
	public int getMaxlength(){
		return maxlength;
	}
	
	public void setMaxlength(int maxlength){
		this.maxlength = maxlength;
	}
	
	private boolean fullword;
	
	public boolean getFullword(){
		return fullword;
	}
	
	public void setFullword(boolean fullword){
		this.fullword = fullword;
	}
	
	private String endchars;
	
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
