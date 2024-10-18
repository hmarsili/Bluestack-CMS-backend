package com.tfsla.templateManager.service;

import java.util.*;
import java.io.*;


import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.PageContext;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.flex.CmsFlexController;
import org.opencms.flex.CmsFlexResponse;
import org.opencms.loader.I_CmsResourceLoader;
import org.opencms.loader.I_CmsResourceStringDumpLoader;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.staticexport.CmsLinkManager;

public abstract class A_ItemProcessor {

	protected Map parameters;
	protected String target;
	protected HttpServletRequest req;
	protected HttpServletResponse response;
	protected PageContext page;
	protected String project;
	
	protected String itemType = "default";

	protected String rootPath;

	protected boolean cacheable = true;
	
	protected String getTarget()
	{
		if (parameters.get("target")!=null)
			return ((String[])parameters.get("target"))[0];
		else
			return rootPath + "/" + itemType + ".jsp";		
	}

	public A_ItemProcessor(String itemType)
	{
		this.itemType = itemType;
	}

	public A_ItemProcessor()
	{
	}
	
	public void includeItem() throws JspException
	{
        CmsFlexController controller = CmsFlexController.getController(req);

        target =  getTarget();
        target = CmsLinkManager.getAbsoluteUri(target, controller.getCurrentRequest().getElementUri());
        
	        
	        Map oldParameterMap = req.getParameterMap();
	
	        try {
		        // add parameters to set the correct element
		        controller.getCurrentRequest().addParameterMap(parameters);
	
		        
		        if (cacheable) {
		            // use include with cache
		            includeWithCache(controller, target);
		        } else {
		            // no cache required
		            includeNoCache(controller, target, null);
		        }
	        } finally {
	            // restore old parameter map (if required)
	            if (oldParameterMap != null) {
	                controller.getCurrentRequest().setParameterMap(oldParameterMap);
	            }
	        }
	}
	
    protected void includeNoCache(
            CmsFlexController controller,
            String target,
            String element) throws JspException {

            try {
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
                    content = strLoader.dumpAsString(cms, file, element, locale, req, response);
                } else {
                    // get the bytes from the loader and convert them to a String
                    byte[] result = loader.dump(
                        cms,
                        file,
                        element,
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
                page.getOut().print(content);

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
        }

    protected void includeWithCache(
            CmsFlexController controller,
            String target) throws JspException {

            try {
                // write out a FLEX_CACHE_DELIMITER char on the page, this is used as a parsing delimeter later
                page.getOut().print(CmsFlexResponse.FLEX_CACHE_DELIMITER);
                // add the target to the include list (the list will be initialized if it is currently empty)
                controller.getCurrentResponse().addToIncludeList(target, parameters);
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
        }

	public String getItemType() {
		return itemType;
	}

	abstract public A_ItemProcessor clone();

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((itemType == null) ? 0 : itemType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
//		if (getClass() != obj.getClass())
//			return false;
		final A_ItemProcessor other = (A_ItemProcessor) obj;
		if (itemType == null) {
			if (other.itemType != null)
				return false;
		} else if (!itemType.equals(other.itemType))
			return false;
		return true;
	}

	public PageContext getPage() {
		return page;
	}

	public void setPage(PageContext page) {
		this.page = page;
	}

	public Map getParameters() {
		return parameters;
	}

	public void setParameters(Map parameters) {
		this.parameters = parameters;
	}

	public HttpServletRequest getReq() {
		return req;
	}

	public void setReq(HttpServletRequest req) {
		this.req = req;
	}

	public HttpServletResponse getResponse() {
		return response;
	}

	public void setResponse(HttpServletResponse response) {
		this.response = response;
	}
	
	public String getRootPath() {
		return rootPath;
	}

	public void setRootPath(String rootPath) {
		this.rootPath = rootPath;
	}

	public String getProject() {
		return project;
	}

	public void setProject(String project) {
		this.project = project;
	}	

	public void printHTML() {

		try {
			if (project.equals("Offline"))
				page.getOut().print("<div class=\"dojoDndItem\">");
        } catch (IOException e) {
        	e.printStackTrace();
        }
        
		try {
			includeItem();
		} catch (JspException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			if (project.equals("Offline"))
				page.getOut().print("</div>");
	    } catch (IOException e) {
	    	e.printStackTrace();
	    }

	}

	abstract public void printDOJOGlobalConf();

	abstract public void printDOJOHTML();
}
