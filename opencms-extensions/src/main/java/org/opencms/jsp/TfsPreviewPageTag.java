package org.opencms.jsp;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.TagSupport;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.flex.CmsFlexCache;
import org.opencms.flex.CmsFlexController;
import org.opencms.flex.CmsFlexRequest;
import org.opencms.flex.CmsFlexResponse;
import org.opencms.loader.CmsTemplateLoaderFacade;
import org.opencms.loader.I_CmsResourceLoader;
import org.opencms.loader.I_CmsResourceStringDumpLoader;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.xml.content.CmsXmlContentFactory;

import com.tfsla.utils.TfsPreviewUserProvider;


public class TfsPreviewPageTag extends TagSupport {

	/**
	 * 
	 */
	
	private String resource;
	
	private static final long serialVersionUID = 2502888521332571052L;

	private CmsFlexController currentController;
	private CmsFlexController controller;
	private Map<String,String[]> oldParameterMap = null;
	Map<String,String[]> parameters = new HashMap<String,String[]>();

	
	@Override
	public int doStartTag() throws JspException {
		
		preserveController();
		
		try {
			CmsObject cmsPreview = getPreviewCmsObject();
			
			CmsResource res = getResource(cmsPreview);
			cmsPreview.getRequestContext().setUri(resource);
			
			controller = getController(res,cmsPreview);
			
			CmsXmlContentFactory.unmarshal(cmsPreview, res, (HttpServletRequest) pageContext.getRequest());
			
	        CmsTemplateLoaderFacade loaderFacade = OpenCms.getResourceManager().getTemplateLoaderFacade(
	        		cmsPreview,
	        		res,
	                getTemplatePropertyDefinition());
	            loaderFacade.getLoader().load(cmsPreview, loaderFacade.getLoaderStartResource(), (HttpServletRequest) pageContext.getRequest(), (HttpServletResponse) pageContext.getResponse());

			//OpenCms.getResourceManager().loadResource(cmsPreview, res, (HttpServletRequest) pageContext.getRequest(), (HttpServletResponse) pageContext.getResponse());
			
			//includeWithCache();
			//pageContext.getResponse().getOutputStream().write(getResourceContent(controller));
			
			
		} catch (CmsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ServletException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			restoreController();
		}
	
		// TODO Auto-generated method stub
		return super.doStartTag();
	}
	
    protected String getTemplatePropertyDefinition() {

        return CmsPropertyDefinition.PROPERTY_TEMPLATE_ELEMENTS;
    }
	@Override
	public int doEndTag() throws JspException {

		//restoreController();

		if (OpenCms.getSystemInfo().getServletContainerSettings().isReleaseTagsAfterEnd()) {
			release();
		}
		return super.doEndTag();
	}

	protected void preserveController()
	{
		currentController = CmsFlexController.getController(pageContext.getRequest());
	}
	
	protected void restoreController()
	{	
		CmsFlexController.setController(pageContext.getRequest(),currentController);
	}
	
	protected CmsFlexController getController(CmsResource resource, CmsObject cms) {
				CmsFlexCache m_cache = CmsFlexController.getController(pageContext.getRequest()).getCmsCache();
			
				boolean top = true;
				boolean streaming = false;
			
				CmsFlexController controller = null;
			    controller = new CmsFlexController(cms, resource, m_cache, (HttpServletRequest)pageContext.getRequest(), (HttpServletResponse)pageContext.getResponse(), streaming, top);
			    CmsFlexController.setController(pageContext.getRequest(), controller);
			    CmsFlexRequest f_req = new CmsFlexRequest((HttpServletRequest) pageContext.getRequest(), controller);
			    CmsFlexResponse f_res = new CmsFlexResponse((HttpServletResponse) pageContext.getResponse(), controller, streaming, true);
			    controller.push(f_req, f_res);
				
			    return controller;
			}

	public void saveRequestParameters()
	{
        oldParameterMap = pageContext.getRequest().getParameterMap();
	}
	
	public void restoreRequestPArameters()
	{
        if (oldParameterMap != null) {
            controller.getCurrentRequest().setParameterMap(oldParameterMap);
        }

	}

	
    public void includeWithCache() throws JspException {

		saveRequestParameters();

		
        try {
            // write out a FLEX_CACHE_DELIMITER char on the page, this is used as a parsing delimeter later
        	pageContext.getOut().print(CmsFlexResponse.FLEX_CACHE_DELIMITER);
            // add the target to the include list (the list will be initialized if it is currently empty)
            controller.getCurrentResponse().addToIncludeList(resource, parameters);
            
            controller.getCurrentRequest().addParameterMap(parameters);
            
            // now use the Flex dispatcher to include the target (this will also work for targets in the OpenCms VFS)
            controller.getCurrentRequest().getRequestDispatcher(resource).include((HttpServletRequest)pageContext.getRequest(),
                    (HttpServletResponse)pageContext.getResponse());
        } catch (ServletException e) {
            // store original Exception in controller in order to display it later
            Throwable t = (e.getRootCause() != null) ? e.getRootCause() : e;
            t = controller.setThrowable(t, resource);
            throw new JspException(t);
        } catch (IOException e) {
            // store original Exception in controller in order to display it later
            Throwable t = controller.setThrowable(e, resource);
            throw new JspException(t);
        }
    	finally {
    		restoreRequestPArameters();
    	}
    }

    public void includeNoCache() throws JspException {

		saveRequestParameters();
    	try {
    		
            controller.getCurrentRequest().addParameterMap(parameters);

            // include is not cachable 
            CmsFile file = controller.getCmsObject().readFile(resource);
            CmsObject cms = controller.getCmsObject();
            Locale locale = cms.getRequestContext().getLocale();

            
            // get the loader for the requested file 
            I_CmsResourceLoader loader = OpenCms.getResourceManager().getLoader(file);
            String content;
            if (loader instanceof I_CmsResourceStringDumpLoader) {
                // loader can provide content as a String
                I_CmsResourceStringDumpLoader strLoader = (I_CmsResourceStringDumpLoader)loader;
                content = strLoader.dumpAsString(cms, file, null, locale, (HttpServletRequest)pageContext.getRequest(), (HttpServletResponse)pageContext.getResponse());
            } else {
                // get the bytes from the loader and convert them to a String
                byte[] result = loader.dump(
                    cms,
                    file,
                    null,
                    locale,
                    (HttpServletRequest)pageContext.getRequest(),
                    (HttpServletResponse)pageContext.getResponse());
                // use the encoding from the property or the system default if not available
                String encoding = cms.readPropertyObject(file, CmsPropertyDefinition.PROPERTY_CONTENT_ENCODING, true).getValue(
                    OpenCms.getSystemInfo().getDefaultEncoding());
                // If the included target issued a redirect null will be returned from loader 
                if (result == null) {
                    result = new byte[0];
                }
                content = new String(result, encoding);
            }
            // write the content String to the JSP output writer
            pageContext.getOut().print(content);

        } catch (ServletException e) {
            // store original Exception in controller in order to display it later
            Throwable t = (e.getRootCause() != null) ? e.getRootCause() : e;
            t = controller.setThrowable(t, resource);
            throw new JspException(t);
        } catch (IOException e) {
            // store original Exception in controller in order to display it later
            Throwable t = controller.setThrowable(e, resource);
            throw new JspException(t);
        } catch (CmsException e) {
            // store original Exception in controller in order to display it later
            Throwable t = controller.setThrowable(e, resource);
            throw new JspException(t);
        }
    	finally {
    		restoreRequestPArameters();
    	}
    }

	protected static byte[] getResourceContent(CmsFlexController controller) throws ServletException, IOException 
	{
        CmsFlexRequest f_req = controller.getCurrentRequest();
        CmsFlexResponse f_res = controller.getCurrentResponse();

        
        f_req.getRequestDispatcher(controller.getCmsObject().getSitePath(controller.getCmsResource())).include(
            f_req,
            f_res);
        
        byte[] result = null;
        
        result = f_res.getWriterBytes();
        
        return result;
	}

	public CmsObject getPreviewCmsObject() throws CmsException
	{
        CmsObject currentCmsObject = CmsFlexController.getController(pageContext.getRequest()).getCmsObject();
   
		CmsObject previewCmsObject = OpenCms.initCmsObject(currentCmsObject);
		previewCmsObject.loginUser(TfsPreviewUserProvider.getInstance().getUserName(), TfsPreviewUserProvider.getInstance().getPassword());
		previewCmsObject.getRequestContext().setCurrentProject(previewCmsObject.readProject("Offline"));
		
		return previewCmsObject;
		
	}
	
	public CmsResource getResource(CmsObject cmsObject) throws CmsException
	{
		return cmsObject.readResource(resource);
	}
	
	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}



}
