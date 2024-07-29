package com.tfsla.diario.ediciones.services;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provides basic services to the embedded-videos components
 */
public class VideoEmbeddedService {

	/**
	 * Obtains the video code from an embedded code string
	 * @param emmbededCode mixed video embedded code, might contain HTML and/or JS
	 * @return the actual video code
	 */
	public String extractVideoCode(String emmbededCode) {
		String videoCode ="";
		
		if(emmbededCode!=null && !emmbededCode.equals("")){ 
			
			if(emmbededCode.indexOf("class=\"twitter-video\"")>-1){
				
				Pattern REGEX_ID = Pattern.compile("<a\\s+(?:[^>]*?\\s+)?href=\"([^\"]*)\"");
				Matcher matcherID = REGEX_ID.matcher(emmbededCode);
				
				if(matcherID.find())
					videoCode = matcherID.group(1);
				
			}else {
			
				int idx = emmbededCode.indexOf("src=") + 5;
				int endIdx = emmbededCode.indexOf("\"", idx);
				
				if(endIdx == -1)
					endIdx = emmbededCode.indexOf("'", idx);
				
				if(endIdx == -1)
					endIdx = emmbededCode.length() - 1;
				
				videoCode = emmbededCode.substring(idx, endIdx);
			}
			
		}
		
		return videoCode;
	}
	
}
