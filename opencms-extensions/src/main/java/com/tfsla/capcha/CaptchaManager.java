package com.tfsla.capcha;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;

import org.apache.commons.codec.binary.Base64;
import org.opencms.file.CmsObject;
import org.opencms.flex.CmsFlexController;
import org.opencms.main.OpenCms;

import com.octo.captcha.service.captchastore.FastHashMapCaptchaStore;
import com.octo.captcha.service.image.DefaultManageableImageCaptchaService;
import com.octo.captcha.service.image.ImageCaptchaService;
import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.TipoEdicionBaseService;

public class CaptchaManager
{
    private static Map<String, ImageCaptchaService> instances = new HashMap<String, ImageCaptchaService>();
    

	/**
	 * Verifica la correspondencia entre los ingresado por el usuario y el valor de la imagen del captcha.
	 * @return true si correcto lo ingresado.
	 */
	public static Boolean validateCapcha(String siteName, String publication, String captchaId, String response)
	{
		Boolean valid = getInstance(siteName,publication).validateResponseForID(captchaId, response);
		return valid;
	}

	public static ImageCaptchaService getInstance(PageContext context, HttpServletRequest req, HttpServletResponse res) throws Exception
    {
	    CmsFlexController m_controller = CmsFlexController.getController(req);
        return getInstance(m_controller.getCmsObject());
    }    
	
	public static ImageCaptchaService getInstance(CmsObject cms)
	{
    	String siteName = OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot();
    	String publication = "0";
     	TipoEdicionBaseService tService = new TipoEdicionBaseService();
    	try {
			TipoEdicion tEdicion = tService.obtenerTipoEdicion(cms, cms.getRequestContext().getUri());			
			if (tEdicion!=null)
				publication = "" + tEdicion.getId();
		} catch (Exception e) {
			e.printStackTrace();
		}

    	return getInstance(siteName,publication);
	}
	
    public static ImageCaptchaService getInstance(String siteName, String publication)
    {
    	String id = siteName + "||" + publication;
    	
    	ImageCaptchaService instance = instances.get(id);
    	
    	if (instance == null) {
	    	instance = new DefaultManageableImageCaptchaService(
	        	      new FastHashMapCaptchaStore(),
	        	      new CustomImageCaptchaEngine(siteName,publication),
	        	      180,
	        	      100000,
	        	      75000);

	    	instances.put(id, instance);
    	}
        return instance;
    }

    public static String getPathCaptchaImage(PageContext context, HttpServletRequest req, HttpServletResponse res) throws Exception
    {
	    CmsFlexController m_controller = CmsFlexController.getController(req);
        return getPathCaptchaImage(m_controller.getCmsObject());
    }    
    
    public static String getPathCaptchaImage(CmsObject cms)
    {
    	String path = OpenCms.getSystemInfo().getContextPath();
    	path += "/_req" + OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot();
    	TipoEdicionBaseService tService = new TipoEdicionBaseService();
    	try {
			TipoEdicion tEdicion = tService.obtenerTipoEdicion(cms, cms.getRequestContext().getUri());
			
			if (tEdicion!=null)
				path += "/" + tEdicion.getId();
			else
				path += "/0"; 

		} catch (Exception e) {
			e.printStackTrace();
			path += "/0"; 
		}
    	
    	byte[]   bytesEncoded = Base64.encodeBase64(cms.getRequestContext().getUri().getBytes());
    
    	path += "/" + new String(bytesEncoded);
    	
    	return path;
    }
    
    public static String getPathCaptchaImage(CmsObject cms, String publication)
    {
    	if (publication!=null){
    	    int publicationId = Integer.parseInt(publication);
    	    
    	    TipoEdicionBaseService tService = new TipoEdicionBaseService();
    	    String site = OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot();
    	    
    	    try {
    			TipoEdicion tEdicion = tService.obtenerTipoEdicion(publicationId);
    			                site = "/sites/"+tEdicion.getProyecto();
    	    } catch (Exception e) {
    			e.printStackTrace();
    		}
    			
    	    String path = OpenCms.getSystemInfo().getContextPath();
    	    path += "/_req" + site;
    	
			path += "/" + publication;
			byte[]   bytesEncoded = Base64.encodeBase64(cms.getRequestContext().getUri().getBytes());
			    
		    path += "/" + new String(bytesEncoded);
		    	
		    return path;
		}
		else{
			return getPathCaptchaImage(cms);
		}
		
    }
    
    public static String generateRandomCapchaKey()
    {
		int n = 20;
		char[] pw = new char[n];
		int c = 'A';
		int r1 = 0;
		for (int i = 0; i < n; i++) {
			r1 = (int) (Math.random() * 3);
			switch (r1) {
				case 0:
					c = '0' + (int) (Math.random() * 10);
					break;
				case 1:
					c = 'a' + (int) (Math.random() * 26);
					break;
				case 2:
					c = 'A' + (int) (Math.random() * 26);
					break;
			}
			pw[i] = (char) c;
		}
		return new String(pw);

    }
	
}