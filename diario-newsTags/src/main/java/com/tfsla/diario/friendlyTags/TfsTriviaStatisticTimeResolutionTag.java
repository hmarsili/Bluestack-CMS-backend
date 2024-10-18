package com.tfsla.diario.friendlyTags;

import jakarta.servlet.jsp.JspException;

import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class TfsTriviaStatisticTimeResolutionTag extends A_TfsTriviaCollectionValue {

	/**
	 * 
	 */
	private static final long serialVersionUID = 725407952091036013L;

	private String type = null;
	
	public String getType(){
		return type;
	}
	
	public void setType(String type){
		this.type = type;
	}
	
	@Override
	 public int doStartTag() throws JspException {

		 setKeyName(TfsXmlContentNameProvider.getInstance().getTagName("trivia.timeResolution"));
		 I_TfsCollectionListTag collection = getCurrentCollection();
		 String content = collection.getCollectionValue(getCollectionPathName()); 

		 if(type==null) type = "seconds";
		 
		 if(type!=null && type.equals("descriptive") && !content.equals("")){
			 int seconds = (int) (Integer.valueOf(content) / 1000) % 60 ;
			 int minutes = (int) ((Integer.valueOf(content) / (1000*60)) % 60);
			 
			 content = minutes+"' "+seconds+"''";
		 }
		 
		 printContent(content);
		   
		 return SKIP_BODY;
	 }
}

