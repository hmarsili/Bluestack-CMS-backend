package com.tfsla.diario.friendlyTags;

import javax.servlet.jsp.JspException;

import com.tfsla.trivias.model.TfsTrivia;


public class TfsTriviaUserResolutionTimeTag extends A_TfsTriviaUserValueTag{

	private static final long serialVersionUID = -6679836842427584583L;
	
	private String type = null;
	
	public String getType(){
		return type;
	}
	
	public void setType(String type){
		this.type = type;
	}
	
	@Override
    public int doStartTag() throws JspException {
		
		 TfsTrivia trivia = getCurrentTrivia();
		 
		 int timeResolution = trivia.getTimeResolution();
		 
		 if(type==null) type = "seconds";
		 
		 String content = "";
		 
		 if(type!=null && type.equals("descriptive")){
			 int seconds = (int) (timeResolution / 1000) % 60 ;
			 int minutes = (int) ((timeResolution / (1000*60)) % 60);
			 
			 content = minutes+"' "+seconds+"''";
		 }else{
			 content = ""+timeResolution;
		 }
		 
         printContent(content);

		 return SKIP_BODY;
    }


}
