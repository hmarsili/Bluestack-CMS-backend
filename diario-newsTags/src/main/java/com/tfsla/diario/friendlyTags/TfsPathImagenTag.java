package com.tfsla.diario.friendlyTags;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspTagException;

import org.apache.commons.logging.Log;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.flex.CmsFlexController;
import org.opencms.jsp.CmsJspTagLink;
import org.opencms.jsp.TfsJspTagLink;
import org.opencms.loader.CmsExternalImageScaler;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUriSplitter;

import com.tfsla.diario.imageVariants.ImageFinder;
import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class TfsPathImagenTag extends A_TfsNoticiaCollectionValue {
	
	//TODO: no usar scaler si no se carga nada por parametro.
	//TODO: agregar escalas extras solicitadas por hernan
	private static final String SCALE_ATTR_WIDTH = "width";
    private static final String SCALE_ATTR_HEIGHT = "height";
	private static final String SCALE_ATTR_TYPE = "scaletype";
    private static final String SCALE_ATTR_POSITION = "scaleposition";

    private static final String SCALE_ATTR_QUALITY = "scalequality";
    private static final String SCALE_ATTR_RENDERMODE = "scalerendermode";
    protected static final Log LOG = CmsLog.getLog(TfsPathImagenTag.class);

    
    /** The given image scaler parameters. */
    private transient CmsExternalImageScaler m_scaler;
    private boolean mustScale = false;
    
    
    private String _width= "";
    private String _heigth ="";
    private String _scaleType= "";
    private String _scalePosition="";
    private String _bgColor="";
    
    public TfsPathImagenTag()
    {
    	// initialize the image scaler parameter container
        m_scaler = new CmsExternalImageScaler();
    }
    
	 @Override
	 public int doStartTag() throws JspException {
		 CmsFlexController controller = CmsFlexController.getController(pageContext.getRequest());
		 CmsObject cms = controller.getCmsObject();

		 //A_TfsNoticiaCollection collection = getCurrentCollectionNews();
		 //String content = collection.getIndexElementValue(collection.getCurrentNews(), "imagen");

		 setKeyName(TfsXmlContentNameProvider.getInstance().getTagName("news.image.image"));
		 I_TfsCollectionListTag collection = getCurrentCollection();
		 
		 String content = collection.getCollectionValue(getCollectionPathName()); //imagen
		 
		 setKeyName(TfsXmlContentNameProvider.getInstance().getTagName("news.image.focalPoint"));
		 String focalPoint = collection.getCollectionValue(getCollectionPathName()); //focalPoint
		 
		 CmsUriSplitter splitSrc = new CmsUriSplitter(content);
		 try {
			 CmsResource imageRes = cms.readResource(splitSrc.getPrefix());
			 
			 String imageVariantPath = "";
			 try {
				 LOG.debug("TfsPathImagenTag --> imageFinder");
				 ImageFinder imageFinder = new ImageFinder();
				 String parameters = imageFinder.toVariantString(_width,_heigth,_scaleType, _scalePosition, _bgColor, focalPoint.replace(",","-").replace(":","_"));
				 imageVariantPath = imageFinder.getImageVariantPath(cms, this.getCurrentNews().getXmlDocument().getFile(), imageRes, parameters);
				 LOG.debug("* parameters --> " + parameters);
				 
				 LOG.debug("* imageVariantPath --> " + imageVariantPath);
				 if (imageVariantPath.length()>0) {
					 printContent(imageVariantPath);
					 return SKIP_BODY;
				 }
			 } catch (JspTagException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			 
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
	            
	            //this.getCurrentNews().getXmlDocument().getFile().getDateCreated();
	            
	            
	            if (focalPoint != null && !focalPoint.equals("")) {
	            	LOG.debug("Busca el punto focal de la imagen en la nota");
	            	String[] fp = focalPoint.split(",");
	            	if (fp.length == 2) {
	            		String fpx=fp[0].replace("fpx:","").replaceAll("\\s","");
	            		String fpy=fp[1].replace("fpy:","").replaceAll("\\s","");
	            		String[] fpAux;
	            		if (fpx.indexOf(".") > -1 ) {
	            			fpAux = fpx.split("\\.");
	            			fpx = fpAux[0];
	            		}
	            		if (fpy.indexOf(".") > -1 ) {
	            			fpAux = fpy.split("\\.");
	            			fpy = fpAux[0];
	            		}
	            		try {
	            			m_scaler.setFocalPoint(Integer.valueOf(fpx),Integer.valueOf(fpy));
	            		}
	            		catch (NumberFormatException e) {
	            			LOG.error("Focal point format exception. The value '" + focalPoint + "' is invalid for the image '" + imageRes + "'",e);
	            		}
	            	}
	            }
	            
	            String imageLink = cms.getSitePath(imageRes);
	            if (m_scaler.isValid()) {
	                // now append the scaler parameters
	                imageLink += m_scaler.toRequestParam();
	            }

	            LOG.debug("Antes de ejecutar linkTagAction" + imageLink);
		        printContent(TfsJspTagLink.linkTagAction(imageLink, this.pageContext.getRequest()));

	            LOG.debug("Despues de ejecutar linkTagAction" + TfsJspTagLink.linkTagAction(imageLink, this.pageContext.getRequest()));
			 
	     
			} catch (CmsException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
		
	    _width= width;
		m_scaler.setWidth(CmsStringUtil.getIntValue(width, 0, SCALE_ATTR_WIDTH));
		mustScale = true;
	}

	public String getHeight() {
		return String.valueOf(m_scaler.getHeight());
	}

	public void setHeight(String height) {

	    _heigth =height;
		m_scaler.setHeight(CmsStringUtil.getIntValue(height, 0, SCALE_ATTR_HEIGHT));
		mustScale = true;
	}

	public String getScaletype() {
		return String.valueOf(m_scaler.getType());
	}

	public void setScaletype(String scaletype) {
		_scaleType= scaletype;
	   
        m_scaler.setType(CmsStringUtil.getIntValue(scaletype, 0, SCALE_ATTR_TYPE));
		mustScale = true;
	}

	public String getBgcolor() {
		return m_scaler.getColorString();
	}

	public void setBgcolor(String bgcolor) {
		_bgColor=bgcolor;

        m_scaler.setColor(bgcolor);
		mustScale = true;

	}
	
	public void setScaleposition(String scaleposition) {
		
		_scalePosition=scaleposition;
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
