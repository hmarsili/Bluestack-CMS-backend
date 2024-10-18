package com.tfsla.diario.friendlyTags;

import jakarta.servlet.jsp.JspException;

import org.opencms.file.CmsObject;
import org.opencms.flex.CmsFlexController;
import org.opencms.main.OpenCms;

import com.tfsla.utils.UrlLinkHelper;

public class TfsFriendlyLinkNoNews extends A_TfsNoticiaValue {

	private String relative = "true";
	private String publicUrl = "false";
	
	public String getPublicUrl() {
		return publicUrl;
	}

	public void setPublicUrl(String publicUrl) {
		this.publicUrl = publicUrl;
	}

	public TfsFriendlyLinkNoNews() {
		relative = "true";
	}
	
	@Override
    public int doStartTag() throws JspException {

	    CmsObject cms = CmsFlexController.getCmsObject(pageContext.getRequest());

        I_TfsNoticia noticia = getCurrentNews();
        
        String content = "";
        content = UrlLinkHelper.getUrlFriendlyLinkRegex(noticia.getXmlDocument().getFile(), cms, 
        		Boolean.parseBoolean(relative),
        		Boolean.parseBoolean(publicUrl),getModule(noticia),getFormat(noticia),getRegex(noticia));
        
        printContent(content);

        relative = "true";
        
        return SKIP_BODY;
    }
	
    public String getRelative() {
		return relative;
	}

	public void setRelative(String relative) {
		if (relative!=null)
			this.relative = relative;
	}
	
	@Override
    public int doEndTag() {

        if (OpenCms.getSystemInfo().getServletContainerSettings().isReleaseTagsAfterEnd()) {
            // need to release manually, JSP container may not call release as required (happens with Tomcat)
            release();
        }
        
        return EVAL_PAGE;
    }
	
private String getModule (I_TfsNoticia noticia) {
    	
    	switch  (noticia.getXmlDocument().getContentDefinition().getTypeName()){
    		case "OpenCmsPlaylist":
    			return "vod";
    		case "OpenCmsSerie":
    			return "vod";
    		case "OpenCmsPelicula":
     			return "vod";
    		case "OpenCmsEpisodio":
     			return "vod";
    		case "OpenCmsTemporada":
     			return "vod";	
    		case "OpenCmsReceta":
				return "recipes";
    		case "OpenCmsEvento":
    			return "eventos";
    		case "OpenCmsTrivia":
    			return "trivias";
    		default:
    			return	"";
    			
    	}
      
    }
    
 private String getFormat (I_TfsNoticia noticia) {
    	
    	switch  (noticia.getXmlDocument().getContentDefinition().getTypeName()){
    	case "OpenCmsPlaylist":
			return "urlFriendlyFormatPlaylist";
		case "OpenCmsSerie":
			return "urlFriendlyFormatSerie";
		case "OpenCmsPelicula":
 			return "urlFriendlyFormatPelicula";
		case "OpenCmsEpisodio":
 			return "urlFriendlyFormatEpisodio";
		case "OpenCmsTemporada":
 			return "urlFriendlyFormatTemporada";	
 			
		default:
			return	"urlFriendlyFormat";
    			
    	}
      
    }
 
 private String getRegex (I_TfsNoticia noticia) {
 	
 	switch  (noticia.getXmlDocument().getContentDefinition().getTypeName()){
 		case "OpenCmsPlaylist":
			return "urlFriendlyRegexPlaylist";
		case "OpenCmsSerie":
		return "urlFriendlyRegexSerie";
		case "OpenCmsPelicula":
			return "urlFriendlyRegexPelicula";
		case "OpenCmsEpisodio":
			return "urlFriendlyRegexEpisodio";
		case "OpenCmsTemporada":
			return "urlFriendlyRegexTemporada";	
		default:
			return	"urlFriendlyRegex";			
 	}
   
 }


}
