package com.tfsla.diario.friendlyTags;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.flex.CmsFlexController;
import org.opencms.jsp.TfsJspTagLink;
import org.opencms.loader.CmsExternalImageScaler;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUriSplitter;

public class TfsEncuestaOpcionImagenTag extends TagSupport {
	
	private static final String SCALE_ATTR_WIDTH = "width";
    private static final String SCALE_ATTR_HEIGHT = "height";
	private static final String SCALE_ATTR_TYPE = "scaletype";
    private static final String SCALE_ATTR_POSITION = "scaleposition";

    private static final String SCALE_ATTR_QUALITY = "scalequality";
    private static final String SCALE_ATTR_RENDERMODE = "scalerendermode";
    
    /** The given image scaler parameters. */
    private transient CmsExternalImageScaler m_scaler;
    private boolean mustScale = false;
    public TfsEncuestaOpcionImagenTag()
    {
    	// initialize the image scaler parameter container
        m_scaler = new CmsExternalImageScaler();
    }
    
	private static final long serialVersionUID = 4264160777731756651L;

	@Override
    public int doStartTag()  {
		
		CmsFlexController controller = CmsFlexController.getController(pageContext.getRequest());
		CmsObject cms = controller.getCmsObject();

			try {
				
				String opcion = getCurrentOpcionEncuesta();
				
				if (opcion==null || opcion.trim().length()==0)
			    	 return SKIP_BODY;
			     
				 CmsUriSplitter splitSrc = new CmsUriSplitter(opcion);
				 try {
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

			            pageContext.getOut().print(TfsJspTagLink.linkTagAction(imageLink, this.pageContext.getRequest()));

					} catch (CmsException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				//if (opcion!=null)
				//	pageContext.getOut().print(opcion);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JspTagException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return SKIP_BODY;

	}
	
	protected String getCurrentOpcionEncuesta() throws JspTagException {
		// get a reference to the parent "content container" class
	    Tag ancestor = findAncestorWithClass(this, TfsEncuestaOpcionesTag.class);
	    if (ancestor == null) {
	        throw new JspTagException("Tag Poll not accesible");
	    }
	    
	    TfsEncuestaOpcionesTag encuestaOpciones = (TfsEncuestaOpcionesTag) ancestor;
	    
	    return encuestaOpciones.getImage();
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


}
