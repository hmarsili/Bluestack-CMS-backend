package com.tfsla.diario.friendlyTags;

import java.io.IOException;

import jakarta.servlet.jsp.JspException;

import org.apache.commons.validator.UrlValidator;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.flex.CmsFlexController;
import org.opencms.jsp.CmsJspTagLink;
import org.opencms.jsp.TfsJspTagLink;
import org.opencms.loader.CmsExternalImageScaler;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUriSplitter;

import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.TipoEdicionService;
import com.tfsla.diario.ediciones.services.openCmsService;
public class TfsUserImagePathTag extends A_TfsUsuarioValueTag {
	private static final String SCALE_ATTR_WIDTH = "width";
    private static final String SCALE_ATTR_HEIGHT = "height";
	private static final String SCALE_ATTR_TYPE = "scaletype";
    private static final String SCALE_ATTR_POSITION = "scaleposition";

    private static final String SCALE_ATTR_QUALITY = "scalequality";
    private static final String SCALE_ATTR_RENDERMODE = "scalerendermode";
    
    
    /** The given image scaler parameters. */
    private transient CmsExternalImageScaler m_scaler;
    private boolean mustScale = false;
    public TfsUserImagePathTag()
    {
    	// initialize the image scaler parameter container
        m_scaler = new CmsExternalImageScaler();
    }

	private static final long serialVersionUID = -8740189212186416201L;

	@Override
	    public int doStartTag() throws JspException {
	    	try {
	    		CmsUser user = getCurrentUser().getUser();
	    		CmsFlexController controller = CmsFlexController.getController(pageContext.getRequest());
	   		 	CmsObject cms = controller.getCmsObject();
	   		 	
				TipoEdicion tEdicion = getTipoEdicion(cms);
				String noPicture = getUserNoPicture(cms.getRequestContext().getSiteRoot(), tEdicion!=null ? String.valueOf(tEdicion.getId()) : "");

	    		if (user!=null){
	    			String content=user.getAdditionalInfo("USER_PICTURE")!=null?user.getAdditionalInfo("USER_PICTURE").toString():"";
	    			
	    			if (content==null || content.trim().length()==0) {
	    				content = noPicture;
	    			}
					
	    			CmsUriSplitter splitSrc = new CmsUriSplitter(content);
	    			
	    			if (cms.existsResource(splitSrc.getPrefix())) {
						CmsResource imageRes = cms.readResource(splitSrc.getPrefix());
		    					
		    			CmsExternalImageScaler reScaler = null;
		    					 if (splitSrc.getQuery() != null) {
		    			            // check if the original URI already has parameters, this is true if original has been cropped
		    			            String[] scaleStr = (String[])CmsRequestUtil.createParameterMap(splitSrc.getQuery()).get(
		    			                CmsExternalImageScaler.PARAM_SCALE);
		    			            if (scaleStr != null) {
		    			                // use cropped image as a base for scaling
		    			                reScaler = new CmsExternalImageScaler(scaleStr[0]);
		    			                m_scaler = reScaler.getCropScaler(m_scaler);
		    			            }
		    					 }
		    			            // calculate target scale dimensions (if required)  
		    			            if (mustScale && ((m_scaler.getHeight() <= 0) || (m_scaler.getWidth() <= 0))) {
		    			                // read the image properties for the selected resource
		    			                CmsExternalImageScaler original = new CmsExternalImageScaler(cms, imageRes);
		    			                if (original.isValid()) {
		    			                	m_scaler = original.getReScaler(m_scaler);
		    			                }
		    			            }
		    			            
		    			            String imageLink = cms.getSitePath(imageRes);
		    			            if (m_scaler.isValid()) {
		    			                // now append the scaler parameters
		    			                imageLink += m_scaler.toRequestParam();
		    			            }
		    			            pageContext.getOut().print(CmsJspTagLink.linkTagAction(imageLink, this.pageContext.getRequest()));
	    			}
	    			else {

	    			    UrlValidator urlValidator = new UrlValidator();
	    			    if (urlValidator.isValid(splitSrc.getPrefix())) {
    			            pageContext.getOut().print(content);
	    			    }
	    			    else {
	    			    	CmsResource imageRes = cms.readResource(noPicture);
    			            // calculate target scale dimensions (if required)  
    			            if (mustScale && ((m_scaler.getHeight() <= 0) || (m_scaler.getWidth() <= 0))) {
    			                // read the image properties for the selected resource
    			                CmsExternalImageScaler original = new CmsExternalImageScaler(cms, imageRes);
    			                if (original.isValid()) {
    			                	m_scaler = original.getReScaler(m_scaler);
    			                }
    			            }
    			            
    			            String imageLink = cms.getSitePath(imageRes);
    			            if (m_scaler.isValid()) {
    			                // now append the scaler parameters
    			                imageLink += m_scaler.toRequestParam();
    			            }
    			            pageContext.getOut().print(TfsJspTagLink.linkTagAction(imageLink, this.pageContext.getRequest()));
	    			    	
	    			    }
	    			    	
	    			}
	    		}
	    	}catch(CmsException cme){	
	    		cme.printStackTrace();

			} catch (IOException e) {
				throw new JspException(e);
			}
			
			return SKIP_BODY;
	    }
	 @Override
	    public void release() {
	        m_scaler = new CmsExternalImageScaler();
	        mustScale = false;
	        super.release();
	    }

		public String getWidth() {
			return String.valueOf(m_scaler.getWidth());
		}

		public void setWidth(String width) {
			m_scaler.setWidth(CmsStringUtil.getIntValue(width, 0, SCALE_ATTR_WIDTH));
			mustScale = true;
		}

		public String getHeight() {
			return String.valueOf(m_scaler.getHeight());
		}

		public void setHeight(String height) {
			m_scaler.setHeight(CmsStringUtil.getIntValue(height, 0, SCALE_ATTR_HEIGHT));
			mustScale = true;
		}

		public String getScaletype() {
			return String.valueOf(m_scaler.getType());
		}

		public void setScaletype(String scaletype) {
	        m_scaler.setType(CmsStringUtil.getIntValue(scaletype, 0, SCALE_ATTR_TYPE));
			mustScale = true;
		}

		public String getBgcolor() {
			return m_scaler.getColorString();
		}

		public void setBgcolor(String bgcolor) {
	        m_scaler.setColor(bgcolor);
			mustScale = true;

		}
		
		public void setScaleposition(String scaleposition) {
	        m_scaler.setPosition(CmsStringUtil.getIntValue(scaleposition, 0, SCALE_ATTR_POSITION));
			mustScale = true;
		}
		
		public String getScaleposition() {
			return String.valueOf(m_scaler.getPosition());
		}

	    public void setScalerendermode(String value) {
	        m_scaler.setRenderMode(CmsStringUtil.getIntValue(value, 0, SCALE_ATTR_RENDERMODE));
	        mustScale = true;
	    }
	    
	    public String getScalerendermode() {
			return String.valueOf(m_scaler.getRenderMode());
		}
	    
	    public void setScalequality(String value) {
	        m_scaler.setQuality(CmsStringUtil.getIntValue(value, 0, SCALE_ATTR_QUALITY));
	        mustScale = true;
	    }
	    
	    public String getScalequality() {
			return String.valueOf(m_scaler.getQuality());
		}
	    

		@Override
		public int doEndTag() throws JspException {
			if (OpenCms.getSystemInfo().getServletContainerSettings().isReleaseTagsAfterEnd()) {
	            // need to release manually, JSP container may not call release as required (happens with Tomcat)
	            release();
	        }

			return super.doEndTag();
		}
		
		private String getUserNoPicture(String siteName, String publicationName)
		{
	    	String module = "newsTags";
	 		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();

			String paramName = "userNoPicture";

			return config.getParam(siteName, publicationName, module, paramName, "");

		}

		
		private TipoEdicion getTipoEdicion(CmsObject cms) {
			
			TipoEdicionService tEService = new TipoEdicionService();
			TipoEdicion tEdicion = null;
			try {
				tEdicion = tEService.obtenerTipoEdicion(cms,cms.getRequestContext().getUri());
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}			
			
			if (tEdicion==null)
			{
				String siteName = openCmsService.getSiteName(cms.getRequestContext().getSiteRoot());
				try {
					tEdicion = tEService.obtenerEdicionOnlineRoot(siteName);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return tEdicion;
		}

}
