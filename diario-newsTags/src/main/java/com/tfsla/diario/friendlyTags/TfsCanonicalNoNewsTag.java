package com.tfsla.diario.friendlyTags;

import javax.servlet.jsp.JspException;

import org.opencms.file.CmsObject;
import org.opencms.flex.CmsFlexController;

import com.tfsla.utils.UrlLinkHelper;

public class TfsCanonicalNoNewsTag extends A_TfsNoticiaValue {
	
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
    public int doStartTag() throws JspException {
    	CmsObject cms = CmsFlexController.getCmsObject(pageContext.getRequest());

        I_TfsNoticia noticia = getCurrentNews();
        
        String content = "";
       
        content = UrlLinkHelper.getCanonicalLinkNoNews(noticia.getXmlDocument().getFile(), cms, this.pageContext.getRequest(), getModule(noticia),getFormat(noticia),getRegex(noticia));
        
        printContent(content);
    	
    	return SKIP_BODY;
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

