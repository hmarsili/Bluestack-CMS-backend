package com.tfsla.diario.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.PageContext;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.flex.CmsFlexController;
import org.opencms.flex.CmsFlexResponse;
import org.opencms.loader.I_CmsResourceLoader;
import org.opencms.loader.I_CmsResourceStringDumpLoader;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;

public class TfsIncludeContentUtil {

	PageContext pageContext =null;
	HttpServletRequest req = null;
    HttpServletResponse response = null;
	CmsFlexController controller = null;
	
	Map<String,String[]> oldParameterMap = null;
	Map<String,String[]> parameters = new HashMap<String,String[]>();

	public TfsIncludeContentUtil(PageContext pageContext)
	{
		this.pageContext = pageContext; 
		req = (HttpServletRequest) pageContext.getRequest();
        response = (HttpServletResponse) pageContext.getResponse();
    	controller = CmsFlexController.getController(req);
		
	}

	public void setParameterToRequest(String name, String value)
	{
		parameters.put(name, new String[] {value});
	}
	
	public void saveRequestParameters()
	{
        oldParameterMap = req.getParameterMap();
	}
	
	public void restoreRequestPArameters()
	{
        if (oldParameterMap != null) {
            controller.getCurrentRequest().setParameterMap(oldParameterMap);
        }

	}
	
    public void includeNoCache(String target) throws JspException {

		saveRequestParameters();
    	try {
    		
            controller.getCurrentRequest().addParameterMap(parameters);

            // include is not cachable 
            CmsFile file = controller.getCmsObject().readFile(target);
            CmsObject cms = controller.getCmsObject();
            Locale locale = cms.getRequestContext().getLocale();

            
            // get the loader for the requested file 
            I_CmsResourceLoader loader = OpenCms.getResourceManager().getLoader(file);
            String content;
            if (loader instanceof I_CmsResourceStringDumpLoader) {
                // loader can provide content as a String
                I_CmsResourceStringDumpLoader strLoader = (I_CmsResourceStringDumpLoader)loader;
                content = strLoader.dumpAsString(cms, file, null, locale, req, response);
            } else {
                // get the bytes from the loader and convert them to a String
                byte[] result = loader.dump(
                    cms,
                    file,
                    null,
                    locale,
                    req,
                    response);
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
            t = controller.setThrowable(t, target);
            throw new JspException(t);
        } catch (IOException e) {
            // store original Exception in controller in order to display it later
            Throwable t = controller.setThrowable(e, target);
            throw new JspException(t);
        } catch (CmsException e) {
            // store original Exception in controller in order to display it later
            Throwable t = controller.setThrowable(e, target);
            throw new JspException(t);
        }
    	finally {
    		restoreRequestPArameters();
    	}
    }

    public void includeWithCache(String target) throws JspException {

		saveRequestParameters();

		
        try {
            // write out a FLEX_CACHE_DELIMITER char on the page, this is used as a parsing delimeter later
        	pageContext.getOut().print(CmsFlexResponse.FLEX_CACHE_DELIMITER);
            // add the target to the include list (the list will be initialized if it is currently empty)
            controller.getCurrentResponse().addToIncludeList(target, parameters);
            
            controller.getCurrentRequest().addParameterMap(parameters);
            
            // now use the Flex dispatcher to include the target (this will also work for targets in the OpenCms VFS)
            controller.getCurrentRequest().getRequestDispatcher(target).include(req, response);
        } catch (ServletException e) {
            // store original Exception in controller in order to display it later
            Throwable t = (e.getRootCause() != null) ? e.getRootCause() : e;
            t = controller.setThrowable(t, target);
            throw new JspException(t);
        } catch (IOException e) {
            // store original Exception in controller in order to display it later
            Throwable t = controller.setThrowable(e, target);
            throw new JspException(t);
        }
    	finally {
    		restoreRequestPArameters();
    	}
    }

}
